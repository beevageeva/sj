package view.model;



public class DirectPageDirectoryModel extends PageTableModel {
	
	private static final long serialVersionUID = -7407255616586708706L;
	
	
	public DirectPageDirectoryModel(int maxNumberEntries) {
		super(maxNumberEntries);
	}

	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int col) {
		switch(col){
		case 0:
			return "i";
		case 1:
			return "p";
		}
		return null;
	}

	public Object getValueAt(int row, int col) {
		DirectPageDirectoryEntry entry = (DirectPageDirectoryEntry) entries[row];
		switch(col){
		case 0:
			return row;
		case 1:
			if(entry!=null){
				return entry.getPageNumber();
			}
			return -1;
		}
		return null;
	}
	
	public DirectPageDirectoryEntry readEntry(int row){
		if(entries[row]!=null){
			fireRowRead(row);
		}
		return (DirectPageDirectoryEntry) entries[row];
	}
	
	public DirectPageDirectoryEntry getEntry(int row){
		return (DirectPageDirectoryEntry) entries[row];
	}

	@Override
	public void newEntry(int n) {
		entries[n] = new DirectPageDirectoryEntry();
		
	}

	public void setEntryPageNumber(int row ,int pageNumber){
		if(entries[row]!=null){
			((DirectPageDirectoryEntry)entries[row]).setPageNumber(pageNumber);
			fireRowUpdated(row);
		}
		
	}

	
}
