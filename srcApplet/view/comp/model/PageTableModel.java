package view.comp.model;

import view.anim.AbstractAnimation;
import view.anim.Animations;
import view.comp.run.App;
import view.model.CacheEntry;
import view.model.CacheEvent;
import view.model.CacheListener;
import view.model.ExtCacheEntry;
import view.model.ProcQueueMg;
import config.ConfigHolder;

public abstract class PageTableModel implements CacheListener {

	protected CacheModel mainMemoryModel;
	protected CacheModel tlbModel;
	protected boolean putInWait;

	
	protected App app;
	
	public CacheModel getMainMemoryModel() {
		return mainMemoryModel;
	}

	public CacheModel getTlbModel() {
		return tlbModel;
	}

	public PageTableModel(CacheModel mainMemoryModel , App app){
		this.mainMemoryModel = mainMemoryModel;
		mainMemoryModel.getCache().addCacheListener(this);
		this.app = app;
		tlbModel = new CacheModel(ConfigHolder.pageTableCfg.getTlbConfig() , true , app);
	}
	
	
	
	public int getPhysicalPageNumber(int pid , int vPN , short instrType){
		putInWait = false;
		if (tlbModel.getCfg().isEnabled()) {
			int index = tlbModel.getCache().resolve(new CacheEntry(pid ,vPN , instrType));
			if (index!= -1) {
				int rpn = ((ExtCacheEntry)tlbModel.getCache().getEntry(index)).getValue();
				app.procQueueMg.addTimeUnits(ConfigHolder.pageTableCfg.getTlbConfig().getAccessTimeUnits());
				if(app.isStep1()){
					AbstractAnimation cpuToCache = app.bPanel.cpuToCache(true , vPN , rpn);
					if(instrType == CacheEntry.MODIFDATA){
						app.animations.add(Animations.parallelAnimation(new AbstractAnimation[]{cpuToCache , app.bPanel.cpuToCacheIndiv(true)}));
					}
					else{
						app.animations.add(cpuToCache);
					}
				}
				return rpn;
			}
		}
		int res = getPhysicalPageNumberFromPageTable(pid , vPN , instrType);
		if(app.isStep1()){
			AbstractAnimation cpuToCache = app.bPanel.cpuToCache(false , vPN , res);
			if(instrType == CacheEntry.MODIFDATA){
				app.animations.add(Animations.parallelAnimation(new AbstractAnimation[]{cpuToCache , app.bPanel.cpuToCacheIndiv(true)}));
			}
			else{
				app.animations.add(cpuToCache);
			}
		}
		if(tlbModel.getCfg().isEnabled()){
			tlbModel.getCache().put(new ExtCacheEntry(pid ,vPN , instrType , res));
		}
		return res;
	}

	
	/**
	 * @param vpn the virtual page number
	 * @param rpn the real page number
	 * @return the number of the page that was allocated in the main memory (index)
	 */
	protected int getPageFromDisk(int pid ,int key, short instrType , int vPN){
		if(app.isStep1()){
			app.animations.add(app.bPanel.cacheToCache(app.bPanel.getNumberCaches()-1 , true , -1 ,  ConfigHolder.pageTableCfg.getTlbConfig().getTimesToGetData(instrType)) );
		}
		putInWait = true;
		int rpn = mainMemoryModel.getCache().put(new ExtCacheEntry(pid , key , instrType , vPN) );
		return rpn;

	}

	
	
	public void objectIsToBeRemoved(CacheEvent e) {
		objectIsToBeEvicted(e);
	}

	public void objectIsToBeModified(CacheEvent e) {
	}

	public void objectPut(CacheEvent e) {
	}

	public void objectRead(CacheEvent e) {
	}

	protected abstract int getPhysicalPageNumberFromPageTable(int pid ,int  vPN ,short instrType);
	protected abstract void clearPageTable();
	protected abstract void clearPageTable(int pid);

	public void clear(){
		tlbModel.clearCache();
		clearPageTable();
	}
	
	public void clear(int pid){
		tlbModel.clearCache(pid);
		clearPageTable(pid);
	}
	
	
	public boolean putInWait(){
		return putInWait;
	}

	public void setMainMemoryModel(CacheModel mainMemoryModel) {
		this.mainMemoryModel = mainMemoryModel;
	}
	
	
}
