package view.model.cacheregion;

import view.model.CacheEntry;

public class DefaultCacheRegion extends CacheRegion{

	public DefaultCacheRegion(int maxNumberEntries) {
		super(maxNumberEntries);
	}

	@Override
	public boolean isInRegion(int index, CacheEntry instrInfo) {
		return true;
	}

	@Override
	public int getAdditionalColumnCount() {
		return 0;
	}


}
