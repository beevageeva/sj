package view.comp.run;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import u.Graph;
import u.Helper;
import u.Graph.LabelPanel;
import view.comp.model.DirectMappedPageTableModel;
import view.comp.model.DirectMappedPageTableModelListener;
import view.model.DirectPageDirectoryModel;
import config.ConfigHolder;
import config.DirectMappedPageTableConfig;

public class DirectMappedPageTable extends PageTable implements DirectMappedPageTableModelListener{


	private static final long serialVersionUID = 1L;
	private JPanel[] levelPanels;
	protected AddrRepr vpnPanel;

	

	public DirectMappedPageTable(DirectMappedPageTableModel model) {
		super(model);
		// init tables
		int nOffsets = ((DirectMappedPageTableConfig)ConfigHolder.pageTableCfg.getAddCfg()).getOffsetsLength().length;
		levelPanels = new JPanel[nOffsets +1];
		JPanel right = new JPanel(new BorderLayout());
		vpnPanel = new AddrRepr(nOffsets, "Virtual page number");
		right.add(vpnPanel, BorderLayout.NORTH);
		JPanel panel = new JPanel(new GridLayout(nOffsets + 1, 1));
		JScrollPane sp = new JScrollPane(panel);
		sp.setPreferredSize(new Dimension(500, 600));
		right.add(sp, BorderLayout.CENTER);
		add(right, BorderLayout.EAST);
		for (int i = 0; i < nOffsets+1; i++) {
			levelPanels[i] = new JPanel();
			levelPanels[i].setSize(new Dimension(500,
					550 / nOffsets));
			panel.add(levelPanels[i]);
		}
		//add tables from model
		for(int i = 0;i<model.getModelList().length;i++){
			for(int j = 0 ; j<model.getModelList()[i].size();j++){
				tableAdded(model.getModelList()[i].get(j) , i);
			}
		}
		model.addDirectMappedPageTableModelListener(this);
	}
	
	private void setAddrRepr(int vPN){
		String vpnBinary = Helper.convertDecimalToBinary(vPN, ConfigHolder.generalCfg.getVirtualAddrNBits() - ConfigHolder.getPageSizeNBits());
		int[] offsetsLength = ((DirectMappedPageTableConfig)ConfigHolder.pageTableCfg.getAddCfg()).getOffsetsLength();
		vpnPanel.setLabel("VPN B: " + vpnBinary + " D: " + vPN);
		int index = 0;
		int len;
		String nBin;
		for (int i = 0; i < offsetsLength.length; i++) {
			len = offsetsLength[i];
			nBin = vpnBinary.substring(index, index + len);
			vpnPanel.setText(i, nBin, Integer.parseInt(nBin, 2));
			index += offsetsLength[i];
		}
	}
	
	@Override
	public int getPhysicalPageNumber(int vPN, short instrType , int pid){
		int res = super.getPhysicalPageNumber(vPN , instrType , pid);
		int vpnNBits = ConfigHolder.generalCfg.getVirtualAddrNBits() - ConfigHolder.getPageSizeNBits();
		setAddrRepr((int) (vPN%Math.pow(2,vpnNBits)));
		return res;
	}
	


	public void tableAdded(DirectPageDirectoryModel model, int level) {
		JTable table = new JTable(model);
		 model.addPageModelListener(new TablePageModelListener(table));
		LabelPanel panel = Graph.createPanel(table, 200, 100, createLastLabel(level));
		levelPanels[level].add(panel);
		setVisible(true);
		
	}

	public void tablesCleared() {
		for(int i = 1;i<levelPanels.length;i++){
			levelPanels[i].removeAll();
		}
	}

	private String createLastLabel(int level){
		StringBuffer label = new StringBuffer("Level : ");
		label.append(level);
		label.append(" ");
		if(level==0){
			label.append("Root");
		}
		else if(level==1){
			label.append("Process ");
		}
		else{
			if (level == ((DirectMappedPageTableConfig)ConfigHolder.pageTableCfg.getAddCfg()).getOffsetsLength().length) {
				label.append("PT");
			} else {
				label.append("PD");
			}
		}
		label.append(levelPanels[level].getComponentCount());
		return label.toString();
	}
	
	
	@Override
	public void tablesCleared(int level, int index) {
		levelPanels[level].remove(index);
		for(int i = index ; i<levelPanels[level].getComponentCount();i++){
			assert levelPanels[level].getComponent(i) instanceof LabelPanel; 
			((LabelPanel)levelPanels[level].getComponent(i)).changeLabel(createLastLabel(level));
		}
	}


}
