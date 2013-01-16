package view.model;


public class InversePageTableModel extends PageTableModel{

	private static final long serialVersionUID = 1L;
	/**
	 * this should be 2**(framePageNumberNBits) because an entry here has the size hashAnchorSizeNBits
	 * when hashAnchorSizeNBits == physicalAddressSizeNBits the anchor is eliminated , thus
	 * only one mem reference
	 * the hash anchor table makes the collision chain shorter without modifying the inverse page size
	 * won't consider the case where inverse page table has a size larger than physical memory entries 
	 * without a hash anchor(-> the page frame number cannot be deducted anymore from the index 
	 * and must be stored in the table) 
	 */
	protected int hashNumber;
	
	protected InversePageTableModel(int maxNumberEntries) {
		super(maxNumberEntries);
	}
	
	public InversePageTableModel(int maxNumberEntries , int hashNumber){
		this(maxNumberEntries);
		this.hashNumber = hashNumber;
	}
	
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int col) {
		switch(col){
		case 0:
			return "i";
		case 1:
			return "PPN";
		case 2:
			return "VPNs";
			
			
		}
		return null;
	}

	public Object getValueAt(int row, int col) {
		InversePageTableEntry entry = (InversePageTableEntry) entries[row];
		switch(col){
		case 0:
			return row;
		case 1:
			return row%hashNumber;
		case 2:
			if(entry!=null){
				return entry.getStringRepr();
			}
			return -1;

		}
		return null;
	}
	
	public InversePageTableEntry getEntry(int row){
		if(entries[row]!=null){
			fireRowRead(row);
		}
		return (InversePageTableEntry) entries[row];
	}

	@Override
	public void newEntry(int n) {
		entries[n] = new InversePageTableEntry();
	}
	
	public void setInMemory(int row , int index , boolean inMemory){
		((InversePageTableEntry)entries[row]).setInMemory(index , inMemory);
		fireRowUpdated(row);
	}
	public void setInMemoryFalse(int row){
		((InversePageTableEntry)entries[row]).setInMemoryFalse();
		fireRowUpdated(row);
	}

	
	public void addVpn(int row , int vpn , boolean inMemory){
		((InversePageTableEntry)entries[row]).add(vpn , inMemory);
		fireRowUpdated(row);
	}

	
}
