package view.comp.run;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;


import u.Graph;
import u.Helper;
import view.comp.model.InverseMappedPageTableModel;
import config.ConfigHolder;

public class InverseMappedPageTable extends PageTable {

	private static final long serialVersionUID = 1L;
	private JLabel vpnLabel;

	public InverseMappedPageTable(InverseMappedPageTableModel model) {
		super(model);
		JPanel right = new JPanel(new BorderLayout());
		vpnLabel = new JLabel("Virtual page number");
		right.add(vpnLabel, BorderLayout.NORTH);
		JTable inverseTable = new JTable(model.getTableModel());
		model.getTableModel().addPageModelListener(new TablePageModelListener(inverseTable));
		right.add(Graph.createPanel(inverseTable, 300, 200,
				"Inverse mapped page table"));
		add(right, BorderLayout.EAST);
		setVisible(true);

	}

	@Override
	public int getPhysicalPageNumber(int vPN, short instrType , int pid) {
		int rpn = super.getPhysicalPageNumber(vPN , instrType , pid);
		int pvpnBits = ConfigHolder.generalCfg.getVirtualAddrNBits() - ConfigHolder.getPageSizeNBits()+ConfigHolder.generalCfg.getNumberProcessesNBits();
		vpnLabel.setText("B: " + Helper.convertDecimalToBinary(vPN, pvpnBits) + " D: " + vPN);
		return rpn;
	}



}
