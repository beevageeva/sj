package view.model;

public class DirectPageDirectoryEntry {

	protected int pageNumber;
	
	public DirectPageDirectoryEntry(int pageNumber){
		this.pageNumber = pageNumber;
	}
	
	public DirectPageDirectoryEntry() {
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	
}
