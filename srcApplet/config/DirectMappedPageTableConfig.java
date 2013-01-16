package config;

public 	class DirectMappedPageTableConfig implements Config{

	private int[] offsetsLength;

	private boolean searchMethodTopDown;

	public int[] getOffsetsLength() {
		return offsetsLength;
	}

	public void setOffsetsLength(int[] offsetsLength) {
		this.offsetsLength = offsetsLength;
	}

	public boolean isSearchMethodTopDown() {
		return searchMethodTopDown;
	}

	public void setSearchMethodTopDown(boolean searchMethodTopDown) {
		this.searchMethodTopDown = searchMethodTopDown;
	}

	public String getStringInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append("Search Method : ");
		sb.append(searchMethodTopDown?"TOP-DOWN" : "BOTTOM-UP");
		sb.append("\noffsets lengths : ");
		for(int i = 0 ; i<offsetsLength.length-1;i++){
			sb.append(offsetsLength[i]);
			sb.append(",");
		}
		sb.append(offsetsLength[offsetsLength.length-1]);
		return sb.toString();
	}
	
}

