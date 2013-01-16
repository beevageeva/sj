package view.model;

public class PageTableEntry extends DirectPageDirectoryEntry{

	private boolean inMemory;
	
	public PageTableEntry(int pageNumber) {
		super(pageNumber);
		this.inMemory = true;
	}

	public boolean isInMemory() {
		return inMemory;
	}

	public void setInMemory(boolean inMemory) {
		this.inMemory = inMemory;
	}

	public PageTableEntry(int pageNumber , boolean inMemory){
		this(pageNumber);
		this.inMemory = inMemory;
	}

	public PageTableEntry() {
		
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof PageTableEntry)){
			return false;
		}
		PageTableEntry entry = (PageTableEntry)o;
		return pageNumber==entry.getPageNumber();
	}
	
	
}
