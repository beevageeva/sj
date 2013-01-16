package view.comp.model;

import config.ConfigHolder;
import config.InverseMappedPageTableCfg;
import u.Helper;
import u.Logger;
import view.comp.run.App;
import view.model.CacheEntry;
import view.model.CacheEvent;
import view.model.InversePageTableEntry;
import view.model.InversePageTableModel;

public class InverseMappedPageTableModel extends PageTableModel{

	private InversePageTableModel tableModel;
	
	// this requires additional structure to keep the offset from the pages that
	// there are not in the phys memory(swap file)

	
	
	public InverseMappedPageTableModel(CacheModel mainMemoryModel, App app) {
		super(mainMemoryModel, app);
		int hashAnchorSizeNBits = ((InverseMappedPageTableCfg)ConfigHolder.pageTableCfg.getAddCfg()).getHashAnchorSizeNBits();
		tableModel = new InversePageTableModel((int) Math.pow(
				2, hashAnchorSizeNBits), (int) Math
				.pow(2, ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]
						.getNumberEntriesNBits()));
	}

	@Override
	protected int getPhysicalPageNumberFromPageTable(int pid ,int  vPN ,short instrType) {
		int vpnNBits = ConfigHolder.generalCfg.getVirtualAddrNBits()
		- ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].getBlockSizeNBits(instrType);
		int hashAnchorSizeNBits = ((InverseMappedPageTableCfg)ConfigHolder.pageTableCfg.getAddCfg()).getHashAnchorSizeNBits();
		String vpnBinary = Helper.convertDecimalToBinary(vPN, vpnNBits
				+ ConfigHolder.generalCfg.getNumberProcessesNBits());
		int index = Integer.parseInt(vpnBinary.substring(vpnNBits
				+ ConfigHolder.generalCfg.getNumberProcessesNBits() - hashAnchorSizeNBits), 2);
		int rpn = index % ((int) Math.pow(2, ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].getNumberEntriesNBits()));
		InversePageTableEntry entry = tableModel.getEntry(index);
		if (entry == null) {
			tableModel.newEntry(index);
			entry = tableModel.getEntry(index);
		}
		int indexInVpnArray = entry.indexOf(vPN);
		if (indexInVpnArray != -1) {
			if (!entry.isInMemory(indexInVpnArray)) {
				// vpn is allocated , but is in the swap file
				// is in the swap file ,bring it from diskin main memory and
				// set true inMemory
				// a node will be evicted , put a cache listener to set in
				// memory in the inverse table to false
				getPageFromDisk(pid , rpn , instrType , vPN);
				tableModel.setInMemory(index, indexInVpnArray, true);
			}
		} else {
			// the vpn hasn't been yet allocated
			getPageFromDisk(pid ,rpn , instrType , vPN);
			tableModel.addVpn(index, vPN, true);
		}
		return rpn;
	}

	@Override
	protected void clearPageTable() {
		tableModel.clear();
		
	}

	public void objectIsToBeEvicted(CacheEvent e) {
		CacheEntry cacheEntry = e.getCache().getEntry(e.getIndex());
		if(cacheEntry==null){
			//prev removed by another thread(page aging , PFF)
			Logger.log("SYNC meth put IMPTM");
			return;
		}
		int vPN = ((Integer) e.getCache().getEntry(e.getIndex()).getKey()).intValue();
		if (tlbModel.getCfg().isEnabled()) {
			tlbModel.getCache().removeEntry(new CacheEntry(-1 ,vPN  , CacheEntry.READDATA));
		}
		// search in all table:in entries that have
		// i%framePageNumberNBits = index
		InversePageTableEntry entry;
		int hashNumber = (int) Math.pow(2, ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]
		                                  						.getNumberEntriesNBits());
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			if (i % hashNumber == e.getIndex()) {
				entry = tableModel.getEntry(i);
				if (entry != null) {
					tableModel.setInMemoryFalse(i);

				}
			}
		}
	}
	
	

	public InversePageTableModel getTableModel() {
		return tableModel;
	}

	@Override
	protected void clearPageTable(int pid) {
		//TODO remove all for this pid
	}
	
}
