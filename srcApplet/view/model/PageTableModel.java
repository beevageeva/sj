package view.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class PageTableModel extends AbstractTableModel{

	protected Object[] entries;
	protected int maxNumberEntries;
	protected List<PageModelListener> pageModelListeners;
	
	
	public abstract void newEntry(int n);
	
	protected PageTableModel(int maxNumberEntries){
		super();
		this.maxNumberEntries = maxNumberEntries;
		entries = new Object[maxNumberEntries];
		pageModelListeners = new ArrayList<PageModelListener>();
	}

	public int getRowCount() {
		return maxNumberEntries;
	}

	
	public void clear(){
		for(int i = 0 ; i<maxNumberEntries; i++){
			entries[i] = null;
		}
		fireTableRowsUpdated(0 , maxNumberEntries);
	}
	
	public void clear(int index){
		entries[index] = null;
		fireTableRowsUpdated(index, index);
	}
	
	public void addPageModelListener(PageModelListener l){
		pageModelListeners.add(l);
	}
	
	public void removePageModelListener(PageModelListener l){
		pageModelListeners.remove(l);
	}
	
	public void fireRowUpdated(int row){
		for(int i = 0 ; i<pageModelListeners.size(); i++){
			pageModelListeners.get(i).rowSet(row);
		}
		fireTableRowsUpdated(row,row);
	}
	
	public void fireRowRead(int row){
		for(int i = 0 ; i<pageModelListeners.size(); i++){
			pageModelListeners.get(i).rowRead(row);
		}
	}
	
}
