package view.model;

/**
 * keeps for each entry a bit of reference( 1 if the page was ref , 0 otherwise )
 * in eviction algorithm verifies the modified bit and the reference bit and thus comparts
 * the entries in 4 classes (r=0 , m=0; r=0,m=1 ; r=1,m=0; r=1 , m=1 ) then selects randomly
 * an entry from the first class that appears in this order
 *
 */
public class NRUCache extends Cache {

	private static final long serialVersionUID = 1L;
	protected boolean[] referenced;

	public NRUCache(int maxNumberEntries , boolean isExt) {
		super(maxNumberEntries , isExt);
		referenced = new boolean[maxNumberEntries];
		for (int i = 0; i < maxNumberEntries; i++) {
			referenced[i] = false;
		}

	}

	@Override
	protected int findNodeToEvict(CacheEntry cacheEntry) {
		int n = 0;
		int type = 3;
		for(int i = region.getStartIndex(cacheEntry) ; i<region.getEndIndex(cacheEntry);i++){
			if (region.isInRegion(i , cacheEntry)) {
				if (!referenced[i]) {
					if (!isModified(i)) {
						if (0 < type) {
							type = 0;
							n = 1;
						} else {
							n++;
						}
					} else {
						if (1 < type) {
							type = 1;
							n = 1;
						} else {
							n++;
						}
					}
				} else {
					if (!isModified(i)) {
						if (2 < type) {
							type = 0;
							n = 1;
						} else {
							n++;
						}
					} else {
						n++;
					}
				}
			}
		}
		int rn = (int) (Math.random() * n);
		int k = 0;
		for (int i = 0; i < maxNumberEntries; i++) {
			if (region.isInRegion(i , cacheEntry)) {
				if ((type == 0 && !referenced[i] && !isModified(i))
						|| (type == 1 && !referenced[i] && isModified(i))
						|| (type == 2 && referenced[i] && !isModified(i))
						|| (type == 3 && referenced[i] && isModified(i))) {
					if (k == rn) {
						return i;
					}
					k++;
				}
			}
		}
		return -1;
	}

	public void objectRead(CacheEvent e) {
		referenced[e.getIndex()] = true;
		fireTableRowsUpdated(e.getIndex() , e.getIndex());
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
	public int getColumnCount() {
		return super.getColumnCount() + 1;
	}

	@Override
	public String getColumnName(int col) {
		if(col<getColumnCount() -1){
			return super.getColumnName(col);
		}
		return "r";
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(col<getColumnCount() -1){
			return super.getValueAt(row ,col);
		}
		if(entries[row]==null){
			return -1;
		}
		return referenced[row]?"1":"0";
	}

	@Override
	public void clearAdditFields(int i) {
		referenced[i] = false;
	}

	

}
