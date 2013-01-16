package view.model;

public class CacheEvent {
	
	private Cache cache;
	private int index;
	
	public CacheEvent(Cache cache , int index){
		this.cache = cache;
		this.index = index;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	
}
