package org.lysty.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.lysty.ui.exception.SongNotIndexedException;
import org.lysty.ui.model.PlaylistProfileModel;

public class TableDragDropListener implements DropTargetListener {
	JTable table;
	private Logger logger = Logger.getLogger(TableDragDropListener.class);
	public static final double ROW_HEIGHT = 15;

	public TableDragDropListener(JTable table) {
		this.table = table;
	}

	@Override
	public void drop(DropTargetDropEvent event) {

		// Accept copy drops
		event.acceptDrop(DnDConstants.ACTION_COPY);

		// Get the transfer which can provide the dropped item data
		Transferable transferable = event.getTransferable();

		// Get the data formats of the dropped item
		DataFlavor[] flavors = transferable.getTransferDataFlavors();
		boolean done = false;
		// Loop through the flavors
		int row = (int) (event.getLocation().getY() / ROW_HEIGHT);
		for (DataFlavor flavor : flavors) {
			try {

				// If the drop items are files
				if (flavor.isFlavorJavaFileListType()) {

					// Get all of the dropped files
					List<File> files = (List) transferable
							.getTransferData(flavor);
					// Loop them through
					SongDroppable model = (SongDroppable) table.getModel();
					for (File file : files) {
						model.addSong(file, row);
						row++;
					}
					done = true;
				}

			} catch (SongNotIndexedException e) {
				logger.error("Song not indexed" + e.getFile());
				int c = JOptionPane.showOptionDialog(table,
						"The selected song is not indexed. Would you like to index: "
								+ e.getFile().getParent(), "Song not indexed",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, null, JOptionPane.YES_OPTION);
				if (c == JOptionPane.YES_OPTION) {
					IndexerWindow.getInstance().setVisible(true);
					IndexerWindow.getInstance().setFiles(
							new File[] { e.getFile().getParentFile() });
					IndexerWindow.getInstance().index();
				} else {

				}
			} catch (Exception e) {
				logger.error("Song drop issue", e);
			}

			if (flavor.getRepresentationClass() == Integer.class) {
				// row transfer
				Integer fromIndex;
				try {
					fromIndex = (Integer) transferable.getTransferData(flavor);
					((Reorderable) table.getModel()).reorder(fromIndex, row);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}

		if (done) {

			// Inform that the drop is complete
			event.dropComplete(true);
		}
	}

	@Override
	public void dragEnter(DropTargetDragEvent event) {
	}

	@Override
	public void dragExit(DropTargetEvent event) {
	}

	@Override
	public void dragOver(DropTargetDragEvent event) {
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent event) {
	}

}
