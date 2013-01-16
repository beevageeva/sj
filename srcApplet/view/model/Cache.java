package view.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import u.Logger;
import view.model.cacheregion.CacheRegion;
import view.model.cacheregion.DefaultCacheRegion;
import view.model.cacheregion.MultCacheRegion;

public abstract class Cache extends AbstractTableModel implements CacheListener {

	protected int maxNumberEntries;

	protected CacheEntry[] entries;

	protected List<CacheListener> listeners;

	protected CacheRegion region;
	
	protected boolean isExt;

	public int getNumberEntries() {
		return maxNumberEntries;
	}

	/**
	 * @param key
	 *            the key that will be put
	 * @return the index of the node that will be evicted
	 */
	protected abstract int findNodeToEvict(CacheEntry entry);

	// TODO remove check
	// node removed by a bk thread
	// System.out.println("SYNC meth put ");

	
	public Cache(int maxNumberEntries , boolean isExt){
		super();
		this.maxNumberEntries = maxNumberEntries;
		region = new DefaultCacheRegion(maxNumberEntries);
		entries = new CacheEntry[maxNumberEntries];
		listeners = new ArrayList<CacheListener>();
		this.isExt = isExt;
	}

	public int getIndex(CacheEntry entry) {
		for (int i = region.getStartIndex(entry); i < region.getEndIndex(entry); i++) {
			if (region.isInRegion(i, entry) && entries[i] != null
					&& entries[i].getKey()==entry.getKey()) {
				return i;
			}
		}
		return -1;
	}

	public void addRegionIntersect(CacheRegion newRegion) {
		region = new MultCacheRegion(maxNumberEntries, new CacheRegion[] {
				region, newRegion });
	}

	protected boolean isModified(int index) {
		return entries[index] != null
				&& entries[index].getState() == CacheEntry.MODIFDATA;
	}

	public CacheEntry getEntry(int index) {
		return entries[index];
	}

	/**
	 * @return the index where occured
	 */
	public int resolve(CacheEntry entry) {
		int index = getIndex(entry);
		if (index != -1) {
			if (entry.getState() == CacheEntry.MODIFDATA) {
				fireObjectIsToBeModified(index);
				entries[index].setState(CacheEntry.MODIFDATA);
				fireTableRowsUpdated(index, index);
			} else {
				fireObjectRead(index);
			}
			return index;
		}
		return -1;
	}

	/**
	 * @return the index where the entry was put , or -1 if there is no place to
	 *         put this object The process should be swaped It shouldn't occur
	 *         as the mem allocat and sched mg should not put this process
	 *         unless there free mem ayt least for each set and for each (data
	 *         and instr separated)
	 */
	public int put(CacheEntry entry) {
		for (int i = region.getStartIndex(entry); i < region.getEndIndex(entry); i++) {
			if (entries[i] == null && region.isInRegion(i, entry)) {
				entries[i] = entry;
				if(entries[i].getKey()==-1){
					entries[i].setKey(i);
				}
				fireObjectPut(i);
				fireTableRowsUpdated(i, i);
				return i;

			}
		}
		int index = findNodeToEvict(entry);
		if (index == -1) {
			// TODO should not occur
			return -1;
		}
		fireObjectIsToBeEvicted(index);
		if (entries[index] == null) {
			// a thread has removed this
			Logger.log("in Cache SYNC meth put PUT ");
			entries[index] = new CacheEntry();
		}
		entries[index] = entry;
		if(entries[index].getKey()==-1){
			entries[index].setKey(index);
		}

		fireObjectPut(index);
		fireTableRowsUpdated(index, index);
		return index;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return "i";
		case 1:
			return "KEY";
		case 2:
			return "m";
		case 3:
			return "pid";
			
		}
		if(isExt && col==4){
			return "VAL";
		}
		if (region != null && col - (isExt?5:4) < region.getAdditionalColumnCount()) {
			return region.getAdditionalColumnNames()[col - (isExt?5:4)];
		}
		return null;
	}
	
	public String getColumnTooltipString(int col){
		switch (col) {
		case 0:
			return "entry number";
		case 1:
			return "KEY";
		case 2:
			return "it will be 1 if the entry was already modified, -1 if the entry is not used yet, 0 if the entry is not modif";
		case 3:
			return "process id";
			
		}
		if(isExt && col==4){
			return "value";
		}
		if (region != null && col - (isExt?5:4) < region.getAdditionalColumnCount()) {
			return region.getAdditionalColumnTooltipString()[col - (isExt?5:4)];
		}
		return null;
	}

	public int getRowCount() {
		return maxNumberEntries;
	}

	public int getColumnCount() {
		return (isExt?5:4) + region.getAdditionalColumnCount();
	}

	public Object getValueAt(int row, int col) {
		CacheEntry entry = entries[row];
		if (entry == null) {
			return -1;
		}
		switch (col) {
		case 0:
			return row;
		case 1:
			return entry.getKey();
		case 2:
			switch (entry.getState()) {
			case CacheEntry.MODIFDATA:
				return 1;
			case CacheEntry.READDATA:
				return 0;
			case CacheEntry.FETCHINSTR:
				return "-";
			}
		case 3:
			return entry.getPid();
		}
		if(isExt && col==4){
			return ((ExtCacheEntry)entry).getValue();
		}
		if (region != null && col - (isExt?5:4) < region.getAdditionalColumnCount()) {
			return region.getValue(col - (isExt?5:4), entry);
		}

		return null;

	}

	public void removeRow(int row) {
		synchronized (entries) {
			fireObjectIsToBeRemoved(row);
			entries[row] = null;
			fireTableRowsUpdated(row, row);
		}
	}

	public void removeEntry(CacheEntry entry) {
		int indexKey = getIndex(entry);
		// the key might already be removed by a previous evict
		if (indexKey != -1) {
			removeRow(indexKey);
		}
	}

	protected void fireObjectRead(int index) {
		CacheEvent e = new CacheEvent(this, index);
		objectRead(e);
		for (int i = 0; i < listeners.size(); i++) {
			((CacheListener) listeners.get(i)).objectRead(e);
		}
	}

	protected void fireObjectIsToBeModified(int index) {
		CacheEvent e = new CacheEvent(this, index);
		objectIsToBeModified(e);
		for (int i = 0; i < listeners.size(); i++) {
			((CacheListener) listeners.get(i)).objectIsToBeModified(e);
		}
	}

	protected void fireObjectPut(int index) {
		CacheEvent e = new CacheEvent(this, index);
		objectPut(e);
		for (int i = 0; i < listeners.size(); i++) {
			((CacheListener) listeners.get(i)).objectPut(e);
		}
	}

	protected void fireObjectIsToBeEvicted(int index) {
		CacheEvent e = new CacheEvent(this, index);
		objectIsToBeEvicted(e);
		for (int i = 0; i < listeners.size(); i++) {
			((CacheListener) listeners.get(i)).objectIsToBeEvicted(e);
		}
	}

	protected void fireObjectIsToBeRemoved(int index) {
		CacheEvent e = new CacheEvent(this, index);
		objectIsToBeRemoved(e);
		for (int i = 0; i < listeners.size(); i++) {
			((CacheListener) listeners.get(i)).objectIsToBeRemoved(e);
		}
	}

	public void addCacheListener(CacheListener l) {
		listeners.add(l);
	}

	public void removeCacheListener(CacheListener l) {
		listeners.remove(l);
	}

	public void clearCache() {
		for (int i = 0; i < maxNumberEntries; i++) {
			entries[i] = null;
		}
		clearAdditFields();
		fireTableRowsUpdated(0, maxNumberEntries);
	}
	
	public void clearCacheByPid(int pid){
		for (int i = 0; i < maxNumberEntries; i++) {
			if(entries[i] !=null && entries[i].getPid() == pid){
				entries[i] = null;
				clearAdditFields(pid);
			}
		}
		fireTableRowsUpdated(0, maxNumberEntries);
	}

	public void clearAdditFields(){
		for(int i =0 ;i<maxNumberEntries; i++){
			clearAdditFields(i);
		}

	}
	
	public abstract void clearAdditFields(int i);

	public List<CacheListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<CacheListener> listeners) {
		this.listeners = listeners;
	}


	public CacheRegion getRegion() {
		return region;
	};

}
