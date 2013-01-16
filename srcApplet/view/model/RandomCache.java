package view.model;

public class RandomCache extends Cache{

	private static final long serialVersionUID = 1L;

	public RandomCache(int maxNumberEntries , boolean isExt) {
		super(maxNumberEntries , isExt);
	}

	@Override
	protected int findNodeToEvict(CacheEntry cacheEntry) {
		int k = (int) (Math.random()*region.getRegionNumberEntries(cacheEntry));
		int j =0;
		for(int i = region.getStartIndex(cacheEntry) ; i<region.getEndIndex(cacheEntry);i++){
			if (region.isInRegion(i , cacheEntry)) {
				if(k==j){
					return i;
				}
				j++;
			}
		}
		return -1;
	}

	public void objectRead(CacheEvent e) {
		
	}

	public void objectIsToBeModified(CacheEvent e) {
		
	}

	public void objectPut(CacheEvent e) {
		
	}

	public void objectIsToBeEvicted(CacheEvent e) {
		
	}

	public void objectIsToBeRemoved(CacheEvent e) {
		
	}

	@Override
	public void clearAdditFields(int i) {
	}

	
	

}
