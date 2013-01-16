package view.comp.run;

import java.awt.Color;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import view.model.PageModelListener;

public class TablePageModelListener implements PageModelListener {

	private JTable table;

	public TablePageModelListener(JTable table) {
		this.table = table;
	}

	private void moveScrollBar(final int index) {
		Runnable r = new Runnable() {
			public void run() {
				JViewport viewPort = (JViewport) table.getParent();
				JScrollBar vsb = ((JScrollPane) viewPort.getParent())
						.getVerticalScrollBar();
				vsb.setValue((8 * index * vsb.getBlockIncrement()) / 5);
			}
		};
		SwingUtilities.invokeLater(r);

	}

	public void rowRead(int index) {
		table.setSelectionBackground(new Color(128, 128, 128));
		table.setRowSelectionInterval(index, index);
		moveScrollBar(index);

	}

	public void rowSet(int index) {
		table.setSelectionBackground(new Color(184, 207, 229));
		table.setRowSelectionInterval(index, index);
		moveScrollBar(index);

	}

}
