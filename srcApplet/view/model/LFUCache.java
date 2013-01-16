package view.model;


public class LFUCache extends Cache{

	private static final long serialVersionUID = 1L;
	protected int[] numberTimesUsed;
	
	
	public LFUCache(int maxNumberEntries , boolean isExt) {
		super(maxNumberEntries , isExt);
		numberTimesUsed = new int[maxNumberEntries];
		for(int i =0 ;i<maxNumberEntries; i++){
			numberTimesUsed[i]=0;
		}
	}

	@Override
	protected int findNodeToEvict(CacheEntry cacheEntry) {
		int min = 60000;
		int index = 0;
		for(int i = region.getStartIndex(cacheEntry) ; i<region.getEndIndex(cacheEntry);i++){
			if(region.isInRegion(i , cacheEntry)){
				if(numberTimesUsed[i]<min){
					index = i;
					min = numberTimesUsed[i];
				}
			}
		}
		numberTimesUsed[index] = 0; 
		return index;
	}

	public void objectRead(CacheEvent e) {
		objectIsToBeModified(e);
		fireTableRowsUpdated(e.getIndex(), e.getIndex());
	}

	public void objectIsToBeModified(CacheEvent e) {
		numberTimesUsed[e.getIndex()]++;
	}

	public void objectPut(CacheEvent e) {
	}

	public void objectIsToBeEvicted(CacheEvent e) {
	}

	public void objectIsToBeRemoved(CacheEvent e) {
	}

	@Override
	public int getColumnCount() {
		return super.getColumnCount() + 1;
	}

	@Override
	public String getColumnName(int col) {
		String name = super.getColumnName(col);
		if(name == null){
			return "used";
		}
		return name;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(entries[row]==null){
			return -1;
		}
		Object value = super.getValueAt(row, col);
		if(value == null){
			return numberTimesUsed[row];
			
		}
		return value;
	}

	@Override
	public void clearAdditFields(int i) {
		numberTimesUsed[i]=0;
	}


}
