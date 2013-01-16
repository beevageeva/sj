package view.model;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import u.Logger;
import view.comp.model.CacheModel;
import view.model.cacheregion.CacheRegion;
import config.ConfigHolder;

public class MainMemAllocator extends CacheRegion implements TableModel,
		CacheListener, Runnable {

	private CacheModel cacheModel;

	private int minPFF, maxPFF, nEvictedNodesToRun;

	private int nEvictedNodes = 0;

	private List<ReallocPageListener> rpList = new ArrayList<ReallocPageListener>();

	public MainMemAllocator(CacheModel cacheModel, int minPFF, int maxPFF,
			int nEvictedNodesToRun) {
		super(cacheModel.getCache().getNumberEntries());
		this.cacheModel = cacheModel;
		cacheModel.getCache().addCacheListener(this);
		cacheModel.getCache().addRegionIntersect(this);
		this.minPFF = minPFF;
		this.maxPFF = maxPFF;
		this.nEvictedNodesToRun = nEvictedNodesToRun;
	}

	private Map<Integer, ProcAllocPages> p = new HashMap<Integer, ProcAllocPages>();

	private boolean isPageNeeded(int pageNumber) {
		ProcAllocPages pp;
		Iterator<Integer> pidsIterator = p.keySet().iterator();
		int pid;
		while (pidsIterator.hasNext()) {
			pid = pidsIterator.next();
			pp = p.get(pid);
			for (int j = 0; j < pp.allocPages.size(); j++) {
				if (pp.allocPages.get(j).pageNumber == pageNumber) {
					return (pp.allocPages.get(j).isNeeded);
				}
			}
		}
		return false;
	}

	private void removePageFronOthProc(int pageNumber) {
		ProcAllocPages pp;
		Iterator<Integer> pidsIterator = p.keySet().iterator();
		int pid;
		while (pidsIterator.hasNext()) {
			pid = pidsIterator.next();
			pp = p.get(pid);
			for (int j = 0; j < pp.allocPages.size(); j++) {
				if (pp.allocPages.get(j).pageNumber == pageNumber) {
					pp.allocPages.remove(j);
					return;
				}
			}
		}
	}

	public int[] hasFreeBlocks() {
		Cache cache = cacheModel.getCache();
		// region cannot be null , it has at least this region added
		List<List<Integer>> sets = cache.region.getPartitionSets();
		int[] freeBlocks = new int[sets.size()];
		List<Integer> set;
		boolean hasFreeBlocks = true;
		for (int k = 0; k < sets.size() && hasFreeBlocks; k++) {
			set = sets.get(k);
			hasFreeBlocks = false;
			for (int l = 0; l < set.size() && !hasFreeBlocks; l++) {
				if (cache.entries[set.get(l)] == null
						&& !isPageNeeded(set.get(l))) {
					freeBlocks[k] = set.get(l);
					hasFreeBlocks = true;
				}
			}
		}
		return hasFreeBlocks ? freeBlocks : null;
	}

	/**
	 * @param pid
	 *            the pid that should be allocated to this process
	 * @return true if has alloc memory , false if there is no memory(there must
	 *         be at least one free page for each set , in each region) thst
	 *         means that for main mem caches that are inverse mapped can only
	 *         have one time a single process
	 * 
	 * 
	 */
	public synchronized boolean allocateProcess(int pid) {
		int[] freeBlocks = hasFreeBlocks();
		if (freeBlocks == null) {
			return false;
		}

		ProcAllocPages pp = new ProcAllocPages();
		// put free found pages?

		for (int i = 0; i < freeBlocks.length; i++) {
			removePageFronOthProc(freeBlocks[i]);
			pp.allocPages.add(new AllocPage(freeBlocks[i], true));
		}

		// check if there are free pages not allocated yet this will be run only
		// for the first time
		int nMaxPagesToTake = (cacheModel.getCache().maxNumberEntries / (p
				.size() + 1))
				- freeBlocks.length;
		int k = 0;
		int pageNumber;
		for (int i = 0; i < cacheModel.getCache().maxNumberEntries
				&& k < nMaxPagesToTake; i++) {
			if (cacheModel.getCache().entries[i] == null
					&& getPidAllocCacheIndex(i) == -1
					&& !pp.allocPages.contains(new AllocPage(i))) {
				pp.allocPages.add(new AllocPage(i, false));
				k++;
			}
		}
		// take all proc and reall pages
		Iterator<Integer> pidsIterator = p.keySet().iterator();
		int cPid;
		while (pidsIterator.hasNext() && k < nMaxPagesToTake) {
			cPid = pidsIterator.next();
			for (int j = 0; j < p.get(cPid).allocPages.size()
					&& k < nMaxPagesToTake; j++) {
				pageNumber = p.get(cPid).allocPages.get(j).pageNumber;
				// if not needed and the entry in cache is empty
				if (!p.get(cPid).allocPages.get(j).isNeeded
						&& cacheModel.getCache().entries[pageNumber] == null) {
					k++;
					p.get(cPid).allocPages.remove(j);
					pp.allocPages.add(new AllocPage(pageNumber, false));
				}
			}
		}
		p.put(new Integer(pid), pp);
		fireTableRowsUpdated(0, p.size() - 1);
		return true;
	}

	public synchronized void remove(int pid) {
		if (p.size() > 1) {
			// reallocate the pages from this process
			List<AllocPage> allocPages = p.get(pid).allocPages;
			Cache cache = cacheModel.getCache();
			int n = allocPages.size() / (p.size() - 1);
			Iterator<Integer> pidsIterator = p.keySet().iterator();
			int cPid;
			boolean foundPPid = false;
			int npTk = n;
			int lastNptk = n + allocPages.size() - (p.size() - 1) * n;
			int index = 0;
			while (pidsIterator.hasNext()) {
				cPid = pidsIterator.next();
				if (pid == cPid) {
					foundPPid = true;
				} else {
					if ((index == p.size() - 2 && !foundPPid)
							|| (index == p.size() - 1 && foundPPid)) {
						npTk = lastNptk;
					}
					for (int k = 0; k < npTk; k++) {
						allocPages.get(0).isNeeded = false;
						p.get(cPid).allocPages.add(allocPages.get(0));
						if (cache.getEntry(allocPages.get(0).pageNumber) != null) {
							cache.removeRow(allocPages.get(0).pageNumber);
						}
						allocPages.remove(0);
					}
				}
				index++;
			}
		}
		p.remove(pid);
		fireTableRowsUpdated(0, p.size());
	}

	public void objectRead(CacheEvent e) {

	}

	public void objectIsToBeModified(CacheEvent e) {

	}

	public void objectPut(CacheEvent e) {
	}

	public void objectIsToBeEvicted(CacheEvent e) {
		if (e.getCache().getEntry(e.getIndex()) == null) {
			Logger.log("SYNC IN MAINMEMALLOC");
			return;
		}
		int pidEvNode = e.getCache().getEntry(e.getIndex()).getPid();
		p.get(pidEvNode).pageFaults++;
		fireTableRowsUpdated(pidEvNode, pidEvNode);
		nEvictedNodes++;
		if (nEvictedNodes >= nEvictedNodesToRun) {
			new Thread(this).start();
		}
	}

	public void objectIsToBeRemoved(CacheEvent e) {

	}

	private int getPidAllocCacheIndex(int index) {
		ProcAllocPages pp;
		Iterator<Integer> keysIt = p.keySet().iterator();
		int pid;
		while (keysIt.hasNext()) {
			pid = keysIt.next();
			pp = p.get(pid);
			for (int j = 0; j < pp.allocPages.size(); j++) {
				if (pp.allocPages.get(j).pageNumber == index) {
					return pid;
				}
			}
		}
		return -1;
	}

	class AllocPage {
		public AllocPage(int pageNumber) {
			this(pageNumber, false);
		}

		public AllocPage(int pageNumber, boolean isNeeded) {
			this.pageNumber = pageNumber;
			this.isNeeded = isNeeded;
		}

		public int pageNumber;

		public boolean isNeeded;

		@Override
		public String toString() {
			StringBuffer res = new StringBuffer();
			res.append(pageNumber);
			if (isNeeded) {
				res.append("(*)");
			}
			return res.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof AllocPage)) {
				return false;
			}
			return ((AllocPage) o).pageNumber == pageNumber;
		}

		@Override
		public int hashCode() {
			return pageNumber;
		}

	}

	public class ProcAllocPages {
		public List<AllocPage> allocPages = new ArrayList<AllocPage>();

		public int pageFaults = 0;

	}

	@Override
	public boolean isInRegion(int index, CacheEntry instrInfo) {
		return getPidAllocCacheIndex(index) == instrInfo.getPid();
	}

	private int getRandomPidWithLowPFF() {
		int k = (int) (Math.random() * p.size());
		int pid;
		for (int j = k; j < p.size(); j++) {
			pid = getPidAtIndex(j);
			if (p.get(pid).pageFaults <= minPFF && hasPagesNotNeeded(p.get(pid))) {
				return pid;
			}
		}
		for (int j = k - 1; j >= 0; j--) {
			pid = getPidAtIndex(j);
			if (p.get(pid).pageFaults <= minPFF && hasPagesNotNeeded(p.get(pid))) {
				return pid;
			}
		}
		return -1;
	}
	
	private boolean hasPagesNotNeeded(ProcAllocPages palloc){
		for(int i = 0 ; i<palloc.allocPages.size();i++){
			if(!palloc.allocPages.get(i).isNeeded){
				return true;
			}
		}
		return false;
	}
	

	/*
	 * normal values of PFF is between minPFF and maxPFF if a process has
	 * PFF>maxPFF try to find one that has PFF < minPFF if none encountered ?
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		int pidWithLowPFF;
		Iterator<Integer> keysIt = p.keySet().iterator();
		int pid;
		while (keysIt.hasNext()) {
			pid = keysIt.next();
			if (p.get(pid).pageFaults >= maxPFF) {
				pidWithLowPFF = getRandomPidWithLowPFF();
				if (pidWithLowPFF != -1) {
					reallocatePages(pidWithLowPFF, pid);
					fireTableRowsUpdated(pidWithLowPFF, pidWithLowPFF);
					fireTableRowsUpdated(0, p.size());

				} else {
					// TODO no process with low pff , swap?
				}
			}
		}
		// clear var
		nEvictedNodes = 0;
		keysIt = p.keySet().iterator();
		while (keysIt.hasNext()) {
			p.get(keysIt.next()).pageFaults = 0;
		}

	}

	// reallocate one page random from process pFrom to process pTo
	private void reallocatePages(int pFrom, int pTo) {
		// remove a page that is not needed
		int k = 0;
		List<AllocPage> fromAllocPages = p.get(pFrom).allocPages;
		for (int i = 0; i < fromAllocPages.size(); i++) {
			if (!fromAllocPages.get(i).isNeeded) {
				k++;
			}
		}
		int randomIndex = (int) (Math.random() * k);
		k = 0;
		int pageNumber;
		for (int i = 0; i < fromAllocPages.size(); i++) {
			if (!fromAllocPages.get(i).isNeeded) {
				if (k == randomIndex) {
					pageNumber = fromAllocPages.get(i).pageNumber;
					// add to other process
					p.get(pTo).allocPages.add(new AllocPage(pageNumber, false));
					// remove from this process
					fromAllocPages.remove(i);
					// remove from main mem
					cacheModel.getCache().removeRow(pageNumber);
					// notify listeners
					if (rpList.size() > 0) {
						for (int j = 0; j < rpList.size(); j++) {
							rpList.get(j).pageReallocated(pFrom, pTo,
									pageNumber);
						}
					}
					return;
				}
				k++;
			}
		}
	}

	@Override
	public int getAdditionalColumnCount() {
		return 0;
	}

	public String getStringInfo() {
		StringBuffer sb = new StringBuffer("minPFF = ");
		sb.append(minPFF);
		sb.append("\nmaxPFF = ");
		sb.append(maxPFF);
		sb.append("\nNEvNodesToBeRunPFF = ");
		sb.append(nEvictedNodesToRun);
		sb.append("\nN evicted nodes = ");
		sb.append(nEvictedNodes);
		return sb.toString();

	}

	// AbstractTableModel implementation

	/** List of listeners */
	protected EventListenerList listenerList = new EventListenerList();

	//
	// Default Implementation of the Interface
	//

	/**
	 * Returns a column given its name. Implementation is naive so this should
	 * be overridden if this method is to be called often. This method is not in
	 * the <code>TableModel</code> interface and is not used by the
	 * <code>JTable</code>.
	 * 
	 * @param columnName
	 *            string containing name of column to be located
	 * @return the column with <code>columnName</code>, or -1 if not found
	 */
	public int findColumn(String columnName) {
		for (int i = 0; i < getColumnCount(); i++) {
			if (columnName.equals(getColumnName(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns <code>Object.class</code> regardless of
	 * <code>columnIndex</code>.
	 * 
	 * @param columnIndex
	 *            the column being queried
	 * @return the Object.class
	 */
	public Class<?> getColumnClass(int columnIndex) {
		return Object.class;
	}

	/**
	 * Returns false. This is the default implementation for all cells.
	 * 
	 * @param rowIndex
	 *            the row being queried
	 * @param columnIndex
	 *            the column being queried
	 * @return false
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * This empty implementation is provided so users don't have to implement
	 * this method if their data model is not editable.
	 * 
	 * @param aValue
	 *            value to assign to cell
	 * @param rowIndex
	 *            row of cell
	 * @param columnIndex
	 *            column of cell
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	//
	// Managing Listeners
	//

	/**
	 * Adds a listener to the list that's notified each time a change to the
	 * data model occurs.
	 * 
	 * @param l
	 *            the TableModelListener
	 */
	public void addTableModelListener(TableModelListener l) {
		listenerList.add(TableModelListener.class, l);
	}

	/**
	 * Removes a listener from the list that's notified each time a change to
	 * the data model occurs.
	 * 
	 * @param l
	 *            the TableModelListener
	 */
	public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(TableModelListener.class, l);
	}

	/**
	 * Returns an array of all the table model listeners registered on this
	 * model.
	 * 
	 * @return all of this model's <code>TableModelListener</code>s or an
	 *         empty array if no table model listeners are currently registered
	 * 
	 * @see #addTableModelListener
	 * @see #removeTableModelListener
	 * 
	 * @since 1.4
	 */
	public TableModelListener[] getTableModelListeners() {
		return (TableModelListener[]) listenerList
				.getListeners(TableModelListener.class);
	}

	//
	// Fire methods
	//

	/**
	 * Notifies all listeners that all cell values in the table's rows may have
	 * changed. The number of rows may also have changed and the
	 * <code>JTable</code> should redraw the table from scratch. The structure
	 * of the table (as in the order of the columns) is assumed to be the same.
	 * 
	 * @see TableModelEvent
	 * @see EventListenerList
	 * @see javax.swing.JTable#tableChanged(TableModelEvent)
	 */
	public void fireTableDataChanged() {
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Notifies all listeners that the table's structure has changed. The number
	 * of columns in the table, and the names and types of the new columns may
	 * be different from the previous state. If the <code>JTable</code>
	 * receives this event and its <code>autoCreateColumnsFromModel</code>
	 * flag is set it discards any table columns that it had and reallocates
	 * default columns in the order they appear in the model. This is the same
	 * as calling <code>setModel(TableModel)</code> on the <code>JTable</code>.
	 * 
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableStructureChanged() {
		fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	/**
	 * Notifies all listeners that rows in the range
	 * <code>[firstRow, lastRow]</code>, inclusive, have been inserted.
	 * 
	 * @param firstRow
	 *            the first row
	 * @param lastRow
	 *            the last row
	 * 
	 * @see TableModelEvent
	 * @see EventListenerList
	 * 
	 */
	public void fireTableRowsInserted(int firstRow, int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	/**
	 * Notifies all listeners that rows in the range
	 * <code>[firstRow, lastRow]</code>, inclusive, have been updated.
	 * 
	 * @param firstRow
	 *            the first row
	 * @param lastRow
	 *            the last row
	 * 
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableRowsUpdated(int firstRow, int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
	}

	/**
	 * Notifies all listeners that rows in the range
	 * <code>[firstRow, lastRow]</code>, inclusive, have been deleted.
	 * 
	 * @param firstRow
	 *            the first row
	 * @param lastRow
	 *            the last row
	 * 
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableRowsDeleted(int firstRow, int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
	}

	/**
	 * Notifies all listeners that the value of the cell at
	 * <code>[row, column]</code> has been updated.
	 * 
	 * @param row
	 *            row of cell which has been updated
	 * @param column
	 *            column of cell which has been updated
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableCellUpdated(int row, int column) {
		fireTableChanged(new TableModelEvent(this, row, row, column));
	}

	/**
	 * Forwards the given notification event to all
	 * <code>TableModelListeners</code> that registered themselves as
	 * listeners for this table model.
	 * 
	 * @param e
	 *            the event to be forwarded
	 * 
	 * @see #addTableModelListener
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableChanged(TableModelEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TableModelListener.class) {
				((TableModelListener) listeners[i + 1]).tableChanged(e);
			}
		}
	}

	/**
	 * Returns an array of all the objects currently registered as
	 * <code><em>Foo</em>Listener</code>s upon this
	 * <code>AbstractTableModel</code>. <code><em>Foo</em>Listener</code>s
	 * are registered using the <code>add<em>Foo</em>Listener</code>
	 * method.
	 * 
	 * <p>
	 * 
	 * You can specify the <code>listenerType</code> argument with a class
	 * literal, such as <code><em>Foo</em>Listener.class</code>. For
	 * example, you can query a model <code>m</code> for its table model
	 * listeners with the following code:
	 * 
	 * <pre>
	 * TableModelListener[] tmls = (TableModelListener[]) (m
	 * 		.getListeners(TableModelListener.class));
	 * </pre>
	 * 
	 * If no such listeners exist, this method returns an empty array.
	 * 
	 * @param listenerType
	 *            the type of listeners requested; this parameter should specify
	 *            an interface that descends from
	 *            <code>java.util.EventListener</code>
	 * @return an array of all objects registered as
	 *         <code><em>Foo</em>Listener</code>s on this component, or an
	 *         empty array if no such listeners have been added
	 * @exception ClassCastException
	 *                if <code>listenerType</code> doesn't specify a class or
	 *                interface that implements
	 *                <code>java.util.EventListener</code>
	 * 
	 * @see #getTableModelListeners
	 * 
	 * @since 1.3
	 */
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}

	// Implementation of the TAbleModel Interface

	public int getRowCount() {
		return (int) Math.pow(2, ConfigHolder.generalCfg
				.getNumberProcessesNBits());
	}

	public int getColumnCount() {
		return 3;
	}

	private int getPidAtIndex(int index) {
		Iterator<Integer> keys = p.keySet().iterator();
		int k = 0;
		int key;
		while (keys.hasNext()) {
			key = keys.next();
			if (k == index) {
				return key;
			}
			k++;
		}
		return -1;
	}

	public Object getValueAt(int row, int col) {
		if (row < p.size()) {
			int pid = getPidAtIndex(row);
			ProcAllocPages pp = p.get(pid);
			switch (col) {
			case 0:
				return pid;
			case 1:
				return pp.pageFaults;
			case 2:
				return pp.allocPages;
			}
		}
		return "-";

	}

	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "pid";
		case 1:
			return "pageFault";
		case 2:
			return "allocated pages";
		}
		return "";
	}

	public void addReallocPageListener(ReallocPageListener l) {
		rpList.add(l);
	}

	public interface ReallocPageListener {
		public void pageReallocated(int fromPid, int toPid, int nPage);
	}

	public void reinit() {
		int[] pids = new int[p.size()];
		Iterator<Integer> it = p.keySet().iterator();
		int k = 0;
		while (it.hasNext()) {
			pids[k++] = it.next();
		}
		p.clear();
		for (int i = 0; i < pids.length; i++) {
			allocateProcess(pids[i]);
		}
		fireTableRowsUpdated(0, p.size() - 1);
	}

}
