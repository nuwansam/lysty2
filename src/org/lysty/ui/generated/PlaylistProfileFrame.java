package org.lysty.ui.generated;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import java.awt.FlowLayout;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JScrollPane;

public class PlaylistProfileFrame extends JFrame {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PlaylistProfileFrame frame = new PlaylistProfileFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public PlaylistProfileFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(null);
		getContentPane().setLayout(null);
		
		JPanel pnlControl = new JPanel();
		pnlControl.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		pnlControl.setBounds(10, 6, 414, 35);
		getContentPane().add(pnlControl);
		pnlControl.setLayout(new MigLayout("", "[19px][29px][][46px][][][][][28px][][]", "[20px]"));
		
		JLabel lblSize = new JLabel("Size");
		pnlControl.add(lblSize, "cell 0 0,alignx left,aligny center");
		
		JSpinner spnSize = new JSpinner();
		pnlControl.add(spnSize, "cell 1 0 2 1,alignx left,aligny top");
		
		JLabel lblSizeType = new JLabel("Size Type");
		pnlControl.add(lblSizeType, "cell 3 0,alignx left,aligny center");
		
		JComboBox cmbSizeType = new JComboBox();
		pnlControl.add(cmbSizeType, "cell 4 0 2 1,alignx left,aligny top");
		
		JButton btnFillPlay = new JButton("Fill & Play");
		btnFillPlay.setFont(new Font("Tahoma", Font.BOLD, 12));
		pnlControl.add(btnFillPlay, "cell 10 0,alignx right");
		
		JPanel pnlStrategy = new JPanel();
		pnlStrategy.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		pnlStrategy.setBounds(10, 46, 414, 35);
		getContentPane().add(pnlStrategy);
		pnlStrategy.setLayout(new MigLayout("", "[][grow]", "[]"));
		
		JLabel lblFillMethod = new JLabel("Fill Method");
		pnlStrategy.add(lblFillMethod, "cell 0 0,alignx trailing");
		
		JComboBox cmbStrategy = new JComboBox();
		pnlStrategy.add(cmbStrategy, "flowx,cell 1 0,alignx left");
		
		JButton btnSettings = new JButton("Settings...");
		pnlStrategy.add(btnSettings, "cell 1 0");
		
		JScrollPane scroller = new JScrollPane();
		scroller.setBounds(10, 92, 414, 159);
		getContentPane().add(scroller);
	}
}
