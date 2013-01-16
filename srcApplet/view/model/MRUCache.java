package view.model;

public class MRUCache extends LRUCache{

	
	private static final long serialVersionUID = 1L;
	
	
	
	public MRUCache(int maxNumberEntries , boolean isExt) {
		super(maxNumberEntries , isExt);
	}

	@Override
	protected int findNodeToEvict(CacheEntry cacheEntry) {
		int min = 30000;
		int index = 0;
		for(int i = region.getStartIndex(cacheEntry) ; i<region.getEndIndex(cacheEntry);i++){
			if (region.isInRegion(i , cacheEntry)) {
				if(numberTimesNotUsed[i]<min){
					index = i;
					min = numberTimesNotUsed[i];
				}
			}
		}
		numberTimesNotUsed[index] = 0;
		return index;
	}
}
