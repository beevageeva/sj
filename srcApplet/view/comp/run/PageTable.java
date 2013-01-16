package view.comp.run;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import view.comp.model.PageTableModel;
import view.model.CacheEntry;

public abstract class PageTable extends JPanel{
	
	
	
	private PageTableModel model;
	
	
	public PageTableModel getModel() {
		return model;
	}
	public PageTable(PageTableModel model){
		super();
		this.model = model;
		setSize(800, 600);
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		if(model.getTlbModel().getCfg().isEnabled()){
			panel.add(new CachePanel(model.getTlbModel(), "TLB",
					150, 150), BorderLayout.NORTH);
		}
		panel.add(new CachePanel(model.getMainMemoryModel(), "Main Memory", 150,150), BorderLayout.CENTER);
		add(panel);
	}
	public int getPhysicalPageNumber(int vPN, short instrType , int pid){
		return model.getPhysicalPageNumber(pid , vPN , instrType);
	}
	
	public void clear(){
		model.clear();
	}
	
	public void clear(int pid){
		model.clear(pid);
	}
	
	public boolean putInWait(){
		return model.putInWait();
	}
	
	

	
}
