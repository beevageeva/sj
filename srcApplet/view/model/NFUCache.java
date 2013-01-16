package view.model;

public class NFUCache extends LFUCache{

	private static final long serialVersionUID = 1L;
	protected boolean[] referenced;
	
	
	public NFUCache(int maxNumberEntries , boolean isExt) {
		super(maxNumberEntries , isExt);
		referenced = new boolean[maxNumberEntries];
		for(int i =0 ;i<maxNumberEntries ; i++){
			referenced[i] = false;
		}
	}


	@Override
	protected int findNodeToEvict(CacheEntry cacheEntry) {
		int min = 60000;
		int index = -1;
		for(int i = region.getStartIndex(cacheEntry) ; i<region.getEndIndex(cacheEntry);i++){
			if (region.isInRegion(i , cacheEntry)) {
				if(numberTimesUsed[i] + (referenced[i]?1:0)<min){
					index = i;
					min = numberTimesUsed[i]+ (referenced[i]?1:0);
				}
			}
		}
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
		return "r";
	}


	@Override
	public Object getValueAt(int row, int col) {
		if(col<getColumnCount() -1){
		return super.getValueAt(row, col);
		}
		if(entries[row] == null){
			return -1;
		}
		return referenced[row]?"1":"0";
	}


	@Override
	public void objectIsToBeModified(CacheEvent e) {
		super.objectIsToBeModified(e);
		referenced[e.getIndex()] = true;
	}


	@Override
	public void objectRead(CacheEvent e) {
		objectIsToBeModified(e);
		fireTableRowsUpdated(e.getIndex() , e.getIndex());
	}
	
	@Override
	public void clearAdditFields() {
		for (int i = 0; i < maxNumberEntries; i++) {
			referenced[i] = false;
		}
	}


	
	
}
