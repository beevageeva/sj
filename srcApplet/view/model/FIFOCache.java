package view.model;

public class FIFOCache extends Cache {

	private static final long serialVersionUID = 1L;
	private long[] timestamp ;
	
	
	public FIFOCache(int maxNumberEntries , boolean isExt) {
		super(maxNumberEntries , isExt);
		timestamp = new long[maxNumberEntries];
		for(int i = 0 ; i<maxNumberEntries; i++){
			timestamp[i] = i;
		}
	}
	
	

	@Override
	protected int findNodeToEvict(CacheEntry cacheEntry) {
		long min = System.currentTimeMillis();
		int indexMin=-1;
		for (int i = region.getStartIndex(cacheEntry); i < region.getEndIndex(cacheEntry); i++) {
			if(region.isInRegion(i , cacheEntry)){
				if(timestamp[i]<min){
					min = timestamp[i];
					indexMin = i;
				}
			}
		}
		return indexMin;
	}

	@Override
	public int getColumnCount() {
		return super.getColumnCount() +1;
	}

	@Override
	public String getColumnName(int col) {
		if(col<getColumnCount()-1){
			return super.getColumnName(col);
		}
		return "timestamp";
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(col<getColumnCount()-1){
			return super.getValueAt(row, col);
		}
		if(entries[row]== null){
			return -1;
		}
		return timestamp[row];
	}

	public void objectRead(CacheEvent e) {
		
		
	}

	public void objectIsToBeModified(CacheEvent e) {
		
		
	}

	public void objectPut(CacheEvent e) {
		int i = e.getIndex();
		timestamp[i] = System.currentTimeMillis();
		fireTableRowsUpdated(i , i);
	}

	
	
	public void objectIsToBeEvicted(CacheEvent e) {
		
	}

	public void objectIsToBeRemoved(CacheEvent e) {
	}

	@Override
	public void clearAdditFields(int i) {
		timestamp[i] = 0;
	}



}
