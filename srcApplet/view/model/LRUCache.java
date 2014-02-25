package view.model;

public class LRUCache extends Cache{

	
	private static final long serialVersionUID = 1L;
	protected int[] numberTimesNotUsed;
	
	
	
	public LRUCache(int maxNumberEntries , boolean isExt) {
		super(maxNumberEntries , isExt);
		numberTimesNotUsed = new int[maxNumberEntries];
		for(int i =0 ;i<maxNumberEntries ; i++){
			numberTimesNotUsed[i] = 0;
		}
	}

	@Override
	protected int findNodeToEvict(CacheEntry cacheEntry) {
		int max = 0;
		int index = 0;
		for(int i = region.getStartIndex(cacheEntry) ; i<region.getEndIndex(cacheEntry);i++){
			if (region.isInRegion(i , cacheEntry)) {
				if(numberTimesNotUsed[i]>max){
					index = i;
					max = numberTimesNotUsed[i];
				}
			}
		}
		numberTimesNotUsed[index] = 0;
		return index;
	}

	@Override
	public int getColumnCount() {
		return super.getColumnCount() + 1;
	}

	@Override
	public String getColumnName(int col) {
		if(col<getColumnCount()-1){
			return super.getColumnName(col);
		}
		return "notUsed";
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(col<getColumnCount()-1){
			return super.getValueAt(row, col);
		}
		if(entries[row] == null){
			return -1;
		}
		return numberTimesNotUsed[row];
	}

	public void objectRead(CacheEvent e) {
		objectIsToBeModified(e);
	}

	public void objectIsToBeModified(CacheEvent e) {
		CacheEntry entry = e.getCache().getEntry(e.getIndex());
		//increment only for the nodes that are in the same region , as then when to evict the alg
		//will only look for those in this one
		for(int i = region.getStartIndex(entry) ; i<region.getEndIndex(entry);i++){
			if(e.getIndex() !=i && entries[i]!=null && region.isInRegion(i , entry)/* && numberTimesNotUsed[i]<numberTimesNotUsed[e.getIndex()] */){
				numberTimesNotUsed[i]++;
				fireTableRowsUpdated(i , i);
			}
		}
		//feb 2014: must set 0 for current entry (Lorenzo!!)
		numberTimesNotUsed[e.getIndex()]=0;

	}

	public void objectPut(CacheEvent e) {
		objectIsToBeModified(e);
	}

	public void objectIsToBeEvicted(CacheEvent e) {
		
	}

	public void objectIsToBeRemoved(CacheEvent e) {
		
	}

	@Override
	public void clearAdditFields(int i) {
		numberTimesNotUsed[i] = 0;
	}

	
	
}
