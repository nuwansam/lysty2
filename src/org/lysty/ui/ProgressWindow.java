package org.lysty.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.lysty.core.SongIndexer;
import org.lysty.core.UpdateListener;
import org.lysty.exceptions.FeatureExtractionException;

public class ProgressWindow extends LFrame implements UpdateListener {

	private static final String TEXT_OK = "Ok";
	private static final String TEXT_CANCEL = "Cancel";
	JLabel lblMessage;
	JButton btnCancel;
	JProgressBar progressBar;
	JList lstErrors;
	JScrollPane scroller;
	private DefaultListModel<String> errorListModel;
	private int errorCount = 0;
	private long successCount;
	private boolean isCancelEnabled;

	public ProgressWindow(String title, boolean isCancelEnabled) {
		super(title);
		this.isCancelEnabled = isCancelEnabled;
		createUI();
		layoutUI();
	}

	private void layoutUI() {
		MigLayout layout = new MigLayout("insets 6 6 6 6");
		JPanel panel = new JPanel(layout);

		panel.add(lblMessage, "growx");
		panel.add(btnCancel, "push, al right, wrap");
		panel.add(progressBar, "span");
		panel.add(scroller, "south");

		if (!isCancelEnabled) {
			btnCancel.setEnabled(false);
		}
		this.setContentPane(panel);
		this.pack();
		this.setVisible(true);
	}

	private void createUI() {
		lblMessage = new JLabel("Preparing to Index songs...");
		lblMessage.setPreferredSize(new Dimension(150, 10));
		lblMessage.setMinimumSize(new Dimension(150, 10));

		btnCancel = new JButton(new AbstractAction("Cancel") {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (TEXT_OK.equalsIgnoreCase(btnCancel.getText())) {
					ProgressWindow.this.dispose();
				} else if (TEXT_CANCEL.equalsIgnoreCase(btnCancel.getText())) {
					SongIndexer.cancel();
					btnCancel.setText(TEXT_OK);
					lblMessage.setText("Cancelled Indexing");
				}
			}
		});
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(400, 20));
		progressBar.setStringPainted(true);
		lstErrors = new JList();
		errorListModel = new DefaultListModel<String>();
		lstErrors.setModel(errorListModel);
		scroller = new JScrollPane(lstErrors);
	}

	@Override
	public void setSize(long size) {
		lblMessage.setText(size + " songs found for indexing...");
		progressBar.setMaximum((int) size);
	}

	@Override
	public void notifyUpdate(long currentProgress, String customMessage) {
		progressBar.setValue((int) currentProgress);
		lblMessage.setText(customMessage);
	}

	@Override
	public void notifyError(Exception e) {
		if (e instanceof FeatureExtractionException) {
			errorCount++;
			FeatureExtractionException e1 = (FeatureExtractionException) e;
			errorListModel.addElement("Error indexing: "
					+ e1.getFile().getAbsolutePath());
		}
	}

	@Override
	public void notifyComplete() {
		lblMessage.setText("Indexing Complete. " + successCount
				+ " files indexed out of " + (successCount + errorCount));
		progressBar.setValue(progressBar.getMaximum());
		btnCancel.setText(TEXT_OK);
	}

	@Override
	public void notifyCurrentSuccessCount(long successCount) {
		progressBar.setString("Successfully indexed: " + successCount
				+ " files");
		this.successCount = successCount;
	}
}
