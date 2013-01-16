package view.comp.run;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import trace.Instr;
import u.Graph;
import view.comp.run.ProcessMgView.ProcQueueView;

public class ListRenderer extends AbstractCellEditor implements
		TableCellRenderer, ActionListener, TableCellEditor {

	private JPanel parentPanel;

	private static final long serialVersionUID = 1L;

	private JButton button;

	private List value;

	public ListRenderer(JPanel parentPanel) {
		button = new JButton("view");
		button.addActionListener(this);
		button.setBorderPainted(false);
		this.parentPanel = parentPanel;
	}

	public void actionPerformed(ActionEvent e) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < value.size(); i++) {
			sb.append(i);
			sb.append(" : ");
			sb.append(value.get(i).toString());
			sb.append("\n");
		}
		JTextArea ta = new JTextArea(sb.toString(), 20, 20);
		ta.setEditable(false);
		showPopup(new JScrollPane(ta));

	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
		if (table.getModel().getValueAt(row, col) == null
				|| table.getModel().getValueAt(row, col).equals("-")) {
			return null;
		}
		return button;
	}

	@SuppressWarnings("unchecked")
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int col) {
		if (table.getModel().getValueAt(row, col) == null
				|| table.getModel().getValueAt(row, col).equals("-")) {
			return null;
		}
		this.value = (List) value;
		return button;
	}

	public Object getCellEditorValue() {
		return null;
	}

	private void showPopup(Component comp) {
		JOptionPane.showMessageDialog(parentPanel, comp);
	}

}
