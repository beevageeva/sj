package view.model;


public class DirectPageTableModel extends DirectPageDirectoryModel {

	private static final long serialVersionUID = 1L;


	public DirectPageTableModel(int maxNumberEntries) {
		super(maxNumberEntries);
	}

	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int col) {
		String res = super.getColumnName(col);
		if(res == null){
			switch(col){
			case 2:
				return "mem";
			}
		}
		return res;
	}

	public Object getValueAt(int row, int col) {
		Object res = super.getValueAt(row , col);
		if(res == null){
			PageTableEntry entry = (PageTableEntry) entries[row];
			switch(col){
			case 2:
				if(entry!=null){
					return entry.isInMemory();
				}
				return -1;
			}
		}
		return res;
	}
	
	public PageTableEntry readEntry(int row){
		return (PageTableEntry) super.readEntry(row);
	}
	
	
	public void setEntryInMemory(int row ,boolean inMemory){
		if(entries[row]!=null){
			((PageTableEntry)entries[row]).setInMemory(inMemory);
			fireRowUpdated(row);
		}
		
	}

	public void setEntry(int row , int pageNumber,boolean inMemory){
		if(entries[row]!=null){
			((PageTableEntry)entries[row]).setPageNumber(pageNumber);
			((PageTableEntry)entries[row]).setInMemory(inMemory);
			fireRowUpdated(row);
		}
		
	}

	
	
	public void newEntry(int n){
		entries[n] = new PageTableEntry();
	}
	
}
