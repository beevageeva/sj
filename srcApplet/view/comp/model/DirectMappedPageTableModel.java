package view.comp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.swing.table.AbstractTableModel;

import u.Helper;
import u.Logger;
import view.comp.run.App;
import view.model.CacheEntry;
import view.model.CacheEvent;
import view.model.DirectPageDirectoryEntry;
import view.model.DirectPageDirectoryModel;
import view.model.DirectPageTableModel;
import view.model.ExtCacheEntry;
import view.model.PageTableEntry;
import config.ConfigHolder;
import config.DirectMappedPageTableConfig;

public class DirectMappedPageTableModel extends PageTableModel {

	private ArrayList<DirectPageDirectoryModel>[] tables;

	private List<DirectMappedPageTableModelListener> l = new ArrayList<DirectMappedPageTableModelListener>();

	/*
	 * l(1), .. l(n) number bits used to split the virtual address for each
	 * level(levels are from 1..n-1 ; level(-1) is the root table) number page
	 * level(0) the process root, l(0) = 2**maxNumberOfProcesses
	 * table on each level = N(i) = 2**(l(0)+l(1) + l(2) + .. + l(i)) for i = 0..n-1 ,
	 * N(-1) = 1, each block of size 2**l(i+1) * sizeOfPageTableEntry
	 * (virtualAddressNBits/8) in the case of bottom up search method to calc
	 * the virtual address of block m of level k (assuming that the memory is
	 * continuously allocated for the page tables) T: va =
	 * BasePTLevelk|m|0(sizeOfPageTableEntryNBits) ; put restriction for interm
	 * page tables that have the size of a page and then take the vpn of the
	 * page table S: search for vpn = N(-1)+ + N(1) + .. N(k-1) + m ; take care that
	 * trace does not contain generated va with vpn in [0..(N(0) + N(1) +
	 * ...N(n-1))] (BasePTlevelk is usually kept in a register ,can be calc:
	 * N(-1) * rootPageTableSize + (N(0)+N(1)+ .. N(k-1))* pageSize)
	 * 
	 */

	@SuppressWarnings("unchecked")
	public DirectMappedPageTableModel(CacheModel mainMemoryModel, App app) {
		super(mainMemoryModel, app);
		// init tables
		int nOffsets = ((DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
				.getAddCfg()).getOffsetsLength().length;
		tables = new ArrayList[nOffsets + 1];
		for (int i = 0; i < nOffsets + 1; i++) {
			tables[i] = new ArrayList<DirectPageDirectoryModel>();
		}
		createTable(0);
	}

	public int[] getOffsets(int vPN, short instrType) {
		int vpnNBits = ConfigHolder.generalCfg.getVirtualAddrNBits()
				- ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]
						.getBlockSizeNBits(instrType);
		//System.out.println("getOffsets: InstrType = " + String.valueOf(instrType) +", vPN = " + String.valueOf(vPN)  + ",vpnNBits=" +  String.valueOf(vpnNBits));
		int[] offsetsLength = ((DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
				.getAddCfg()).getOffsetsLength();
		String vpnBinary = Helper.convertDecimalToBinary(vPN, vpnNBits);
		int index = 0;
		int len;
		String nBin;
		int[] offsets = new int[offsetsLength.length];
		for (int i = 0; i < offsetsLength.length; i++) {
			len = offsetsLength[i];
			nBin = vpnBinary.substring(index, index + len);
			offsets[i] = Integer.parseInt(nBin, 2);
			index += len;
		}
		return offsets;
	}


	private int getPhysicalPageNumberFromPageBU(int pid , int vPN , short instrType) {
		int number = vPN ;
		int ptVPN;
		int tlbIndexValue;
		int tlbValue;
		Stack<Integer> ptVPNs = new Stack<Integer>();
		int[] offsetsLength = ((DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
				.getAddCfg()).getOffsetsLength();
		int[] offsets = getOffsets((int) (vPN % Math.pow(2, ConfigHolder.generalCfg.getVirtualAddrNBits()
				- ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]
				 						.getBlockSizeNBits(instrType))) , instrType);
		int[] B = new int[offsetsLength.length];
		int[] N = new int[offsetsLength.length];
		for(int i = 0 ; i<offsetsLength.length ;i++){
			if(i==0){
				B[0] = pid;
				N[0] = ConfigHolder.generalCfg.getNumberProcesses();
			}
			else{
				B[i] = (int) (Math.pow(2 , offsetsLength[i-1]) *  B[i-1]) + offsets[i-1];
				N[i] =  (int) ( N[i-1] * Math.pow(2 , offsetsLength[i]));
			}
		}
		for (int k = offsetsLength.length-1 ; k >= 0; k--) {
			//ptVPN = getPTVirtualPageNumber(k, number);
			ptVPN = 1;
			for(int i = 0 ; i<k ; i++){
				ptVPN += N[i];
			}
			ptVPN+=B[k];
			ptVPNs.push(new Integer(ptVPN));
			tlbIndexValue = tlbModel.getCache().resolve(new CacheEntry(pid , ptVPN , instrType));
			if (tlbIndexValue != -1) {
				tlbValue = ((ExtCacheEntry)tlbModel.getCache().getEntry(tlbIndexValue)).getValue();
				return getPhysicalPageNumberFromPageTD(tlbValue, k+1, pid , vPN , instrType, ptVPNs);
			}
			number /= Math.pow(2, offsetsLength[k]);
		}
		return getPhysicalPageNumberFromPageTD(0, 0, pid , vPN , instrType, ptVPNs);
	}

	/**
	 * @param indexPageTable the index in the table array of level startLevel
	 * @param startLevel
	 * @param pid
	 * @param vPN
	 * @param instrType
	 * @param ptVPNs
	 * @return
	 */
	private int getPhysicalPageNumberFromPageTD(int indexPageTable,
			int startLevel, int pid , int vPN , short instrType, Stack<Integer> ptVPNs) {
		int[] offsetsLength = ((DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
				.getAddCfg()).getOffsetsLength();
		int vpnNBits = ConfigHolder.generalCfg.getVirtualAddrNBits()
				- ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]
						.getBlockSizeNBits(instrType);
		int lastNumber = indexPageTable;
		DirectPageDirectoryModel model;
		DirectPageDirectoryEntry entry;
		int[] offsets = getOffsets((int) (vPN % Math.pow(2, vpnNBits)) , instrType);
		/*System.out.println("getPhysicalPageNumberFromPageTD instrType=" + String.valueOf(instrType) + ", offsets:");
		for(int i = 0; i < offsets.length; i++){
			System.out.print(String.valueOf(offsets[i]) + " ");
		}
		System.out.println("");
		*/
		int n;
		for (int i = startLevel; i < offsetsLength.length + 1; i++) {
			n = (i == 0 ? (int) (vPN / Math.pow(2, vpnNBits)) : offsets[i - 1]);
			if(lastNumber<0){
				System.out.println("Level "+i);
			}
			model = tables[i].get(lastNumber);
			entry = model.readEntry(n);
			if (entry == null) {
				model.newEntry(n);
				if (i == offsetsLength.length) {
					// read first time from disk
					lastNumber = getPageFromDisk(pid ,-1, instrType , vPN);
					//System.out.println("getPhysicalPageNumberFromPageTD instrType=" + String.valueOf(instrType) + ", lastNumber1=" + String.valueOf(lastNumber));
					// put the key , value in a swap file
					((DirectPageTableModel) model)
							.setEntry(n, lastNumber, true);
				} else {
					lastNumber = tables[i + 1].size();
					//System.out.println("getPhysicalPageNumberFromPageTD instrType=" + String.valueOf(instrType) + ", lastNumber3=" + String.valueOf(lastNumber));
					model.setEntryPageNumber(n, lastNumber);
					createTable(i + 1);
				}
			} else {
				if (i == offsetsLength.length) {
					// check if it is in a swap file
					if (!((PageTableEntry) entry).isInMemory()) {
						// the page number is an offset of a swap file, get from
						// there
						lastNumber = getPageFromDisk(pid , -1 ,instrType , vPN);
						//System.out.println("getPhysicalPageNumberFromPageTD instrType=" + String.valueOf(instrType) + ", lastNumber2=" + String.valueOf(lastNumber));
						((DirectPageTableModel) model).setEntry(n, lastNumber,
								true);
					}
					else{
						lastNumber = entry.getPageNumber();
						//System.out.println("getPhysicalPageNumberFromPageTD instrType=" + String.valueOf(instrType) + ", lastNumber5=" + String.valueOf(lastNumber));
					}
				} else {
					lastNumber = entry.getPageNumber();
					//System.out.println("getPhysicalPageNumberFromPageTD instrType=" + String.valueOf(instrType) + ", lastNumber4=" + String.valueOf(lastNumber));
				}
			}
			if (ptVPNs != null && i>0) {
				tlbModel.getCache().put(new ExtCacheEntry(pid , ptVPNs.pop().intValue() , instrType , lastNumber));
			}
		}
		return lastNumber;

	}

	// the pageNumber will be in main memory
	public int getPhysicalPageNumberFromPageTable(int pid ,int  vPN ,short instrType) {
		if (((DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
				.getAddCfg()).isSearchMethodTopDown() || !tlbModel.getCfg().isEnabled()) {
			return getPhysicalPageNumberFromPageTD(0, 0, pid, vPN , instrType, null);
		}
		return getPhysicalPageNumberFromPageBU(pid, vPN , instrType);
	}

	private void createTable(int level) {
		DirectPageDirectoryModel model;
		int[] offsetsLength = ((DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
				.getAddCfg()).getOffsetsLength();
		if (level == offsetsLength.length) {
			model = new DirectPageTableModel((int) Math.pow(2,
					offsetsLength[level - 1]));
		} else if (level > 0) {
			model = new DirectPageDirectoryModel((int) Math.pow(2,
					offsetsLength[level - 1]));
		} else {
			model = new DirectPageDirectoryModel((int) Math.pow(2,
					ConfigHolder.generalCfg.getNumberProcessesNBits()));
		}
		tables[level].add(model);
		for (int i = 0; i < l.size(); i++) {
			l.get(i).tableAdded(model, level);
		}

	}

	public void clearPageTable() {
		for (int i = 1; i < tables.length; i++) {
			tables[i].clear();
		}
		tables[0].get(0).clear();
		// fire tables cleared
		for (int i = 0; i < l.size(); i++) {
			l.get(i).tablesCleared();
		}
	}
	
	
	
	public void clearPageTable(int pid){
		List<Integer>[] toRemove = new List[tables.length - 1];
		toRemove[0] = new ArrayList<Integer>();
		toRemove[0].add(tables[0].get(0).getEntry(pid).getPageNumber());
		for(int i = 1;i<tables.length - 1;i++){
			toRemove[i] = new ArrayList<Integer>();
			for(int j = 0;j<toRemove[i-1].size();j++){
				for(int k = 0;k<tables[i-1].get(toRemove[i-1].get(j)).getRowCount();k++){
					if(tables[i].get(toRemove[i-1].get(j)).getEntry(k)!=null){
						toRemove[i].add(tables[i-1].get(toRemove[i-1].get(j)).getEntry(k).getPageNumber());
					}
				}
			}
			Collections.sort(toRemove[i]);
		}
		for(int i = toRemove.length - 2;i>=1;i--){
			for(int j = 0;j<toRemove[i].size();j++){
				tables[i].remove(toRemove[i].get(j) - j);
				for (int k = 0; k < l.size(); k++) {
					l.get(k).tablesCleared(i+1, toRemove[i].get(j) - j);
				}
			}

		}
		
		//removeTableAtIndex(1, indexLevel1);
		//update in table level0(pid table)
		for(int i = 0;i<tables[0].get(0).getRowCount();i++){
			if(tables[0].get(0).getEntry(i)!=null && tables[0].get(0).getEntry(i).getPageNumber() > toRemove[0].get(0)){
				tables[0].get(0).getEntry(i).setPageNumber(tables[0].get(0).getEntry(i).getPageNumber() - 1);
			}
		}
		tables[0].get(0).clear(pid);
	}

	public void objectIsToBeEvicted(CacheEvent e) {
		// modify the page table entry
		CacheEntry cacheEntry = e.getCache().getEntry(e.getIndex());
		if(cacheEntry==null){
			//prev removed by another thread(page aging , PFF)
			Logger.log("SYNC meth put DMPTM ");
			return;
		}
		int vpnNBits = ConfigHolder.generalCfg.getVirtualAddrNBits()
		- ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]
				.getBlockSizeNBits(cacheEntry.getState());

		//pid not needed
		int[] offsetsLength = ((DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
				.getAddCfg()).getOffsetsLength();
		int vPN =  ((ExtCacheEntry)e.getCache().getEntry(e.getIndex())).getValue();
		if (tlbModel.getCfg().isEnabled()) {
			tlbModel.getCache().removeEntry(e.getCache().getEntry(e.getIndex()));
		}
		int[] offsets = getOffsets((int) (vPN % Math.pow(2, vpnNBits)) , cacheEntry.getState());
		int lastNumber = 0;
		AbstractTableModel model;
		DirectPageDirectoryEntry entry;
		int n;
		for (int i = 0; i < offsetsLength.length + 1; i++) {
			n = (i == 0 ? (int) (vPN / Math.pow(2, vpnNBits)) : offsets[i - 1]);

			model = tables[i].get(lastNumber);
			entry = ((DirectPageDirectoryModel) model).readEntry(n);
			if (entry == null) {
				System.out
						.println("ENTRY NULL page must be in page table entry");
			} else {
				if (i == offsetsLength.length) {
					((DirectPageTableModel) model).setEntryInMemory(n, false);
				} else {
					lastNumber = entry.getPageNumber();
				}
			}
		}

	}

	public void addDirectMappedPageTableModelListener(
			DirectMappedPageTableModelListener list) {
		l.add(list);
	}

	public List<DirectPageDirectoryModel>[] getModelList() {
		return tables;
	}


}
