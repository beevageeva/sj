package view.comp.model;

import java.util.ArrayList;
import java.util.List;

import u.Logger;
import view.anim.AbstractAnimation;
import view.comp.run.App;
import view.model.CacheAdapter;
import view.model.CacheEntry;
import view.model.CacheEvent;
import config.ConfigHolder;

public class CachesModel {

	private CacheModel[] cacheModels;

	private int[] cacheIndexes;

	private App app;

	private List<CachesModelListener> l = new ArrayList<CachesModelListener>();

	public CachesModel(App app) {
		this.app = app;
		cacheModels = new CacheModel[ConfigHolder.getNumberEnabledCaches()];
		cacheIndexes = new int[ConfigHolder.getNumberEnabledCaches()];
		int j = 0;
		for (int i = 0; i <= ConfigHolder.numberCaches; i++) {
			if (ConfigHolder.cacheCfgs[i].isEnabled()) {
				cacheModels[j] = new CacheModel(ConfigHolder.cacheCfgs[i], app);
				cacheIndexes[j] = i;
				if (j != ConfigHolder.numberCaches) {
					if (ConfigHolder.cacheCfgs[j].isWriteThroughHitPolicy()) {
						cacheModels[j].getCache().addCacheListener(
								new WriteThroughCacheListener(j));
					} else {
						cacheModels[j].getCache().addCacheListener(
								new WriteBackCacheListener(j));
					}
				}
				j++;
			}
		}
	}

	public void resolve(String binaryAddress, short instrType, int pid) {
		int key;
		// search in caches(0->..)
		int i = 0;
		boolean foundValue = false;
		CacheEntry cacheEntry;
		while (!foundValue && i < cacheModels.length) {
			key = calculateCacheKeyFromAddress(i, binaryAddress, instrType);
			cacheEntry = new CacheEntry(pid, key, instrType);
			foundValue = cacheModels[i].getCache().resolve(cacheEntry) != -1;
			i++;
		}
		if (!foundValue) {
			Logger
					.log("Page not in main memory pageTable should have done this");
			return;
		}
		// bring from MM ->C3->C2->C1

		if (instrType != CacheEntry.MODIFDATA) {
			// from index i-1 .. 0 put the value
			for (int k = i - 2; k >= 0; k--) {
				// copy values from k-1 to k
				key = calculateCacheKeyFromAddress(k, binaryAddress , instrType);
				notifyFwdObjectRes(k, key, pid, instrType);
			}
		} else if ((i >= 2) && instrType == CacheEntry.MODIFDATA) {
			//bring in f Caches until reaches a cache with no Write Allocate policy
			boolean stopBring = false;
			for (int k = i - 2; k >= 0 && !stopBring; k--) {
				if (ConfigHolder.cacheCfgs[cacheIndexes[k]]
						.isWriteAllocateMissPolicy()) {
					key = calculateCacheKeyFromAddress(k, binaryAddress , instrType);
					notifyFwdObjectRes(k, key, pid, instrType);
				} else {
					stopBring = true;
				}
			}
		}

		if (app.isStep1() && instrType != CacheEntry.MODIFDATA) {
			app.animations.add(app.bPanel.cpuToCacheIndiv(false));
		}

		for (i = 0; i < l.size(); i++) {
			l.get(i).resolve(binaryAddress, instrType);
		}
	}

	public void addCachesModelListener(CachesModelListener list) {
		l.add(list);
	}

	public int getLength() {
		return cacheModels.length;
	}

	public CacheModel getCacheModel(int index) {
		return cacheModels[index];
	}

	private void notifyFwdObjectRes(int cacheIndex, int value, int pid,
			short instrType) {
		CacheEntry cacheEntry = new CacheEntry(pid, value, instrType);
		putInCache(cacheIndex, cacheEntry);
		if (app.isStep1()) {
			AbstractAnimation anim = app.bPanel.cacheToCache(cacheIndex, true,
					value , ConfigHolder.cacheCfgs[cacheIndexes[cacheIndex]].getTimesToGetData(instrType));
			anim.setMessage("bring from back caches (as the key wasn't found)");
			app.animations.add(anim);
		}
	}

	private void notifyBackObjectModify(int cacheIndex, int value, int pid , short instrType) {
		if (cacheIndex < cacheModels.length - 1) {
			int cKey;
			cKey = (int) (value * Math.pow(2, cacheModels[cacheIndex].getCfg()
					.getBlockSizeNBits(instrType)
					- cacheModels[cacheIndex + 1].getCfg().getBlockSizeNBits(instrType)));
			if (app.isStep1()) {
				AbstractAnimation anim = app.bPanel.cacheToCache(cacheIndex,
						false, cKey , ConfigHolder.cacheCfgs[cacheIndexes[cacheIndex]].getTimesToGetData(instrType));
				anim
						.setMessage(" write to back caches (due to write hit policy) ");
				app.animations.add(anim);
			}
			int val = cacheModels[cacheIndex + 1].getCache().resolve(
					new CacheEntry(pid, cKey, CacheEntry.MODIFDATA));
			if (val == -1) {
				putInCache(cacheIndex + 1, new CacheEntry(pid, cKey,
						CacheEntry.MODIFDATA));
				// TODO put an animation because this must be brought
			}

		}
	}

	private int calculateCacheKeyFromAddress(int i, String binaryAddress , short instrType) {
		return Integer.parseInt(binaryAddress.substring(0, binaryAddress
				.length()
				- cacheModels[i].getCfg().getBlockSizeNBits(instrType)), 2);
	}

	private void putInCache(int index, CacheEntry cacheEntry) {
		cacheModels[index].getCache().put(cacheEntry);
		app.procQueueMg.addTimeUnits(cacheModels[index].getCfg()
				.getAccessTimeUnits());
	}

	public void clearCaches() {
		for (int i = 0; i < cacheModels.length; i++) {
			cacheModels[i].clearCache();
		}
	}

	public void clearCachesByPid(int pid) {
		for (int i = 0; i < cacheModels.length; i++) {
			cacheModels[i].clearCache(pid);
		}
	}

	
	class WriteThroughCacheListener extends CacheAdapter {

		private int cacheIndex;

		public WriteThroughCacheListener(int cacheIndex) {
			this.cacheIndex = cacheIndex;
		}

		public void objectIsToBeModified(CacheEvent e) {
			CacheEntry cacheEntry = e.getCache()
			.getEntry(e.getIndex());
			notifyBackObjectModify(cacheIndex, cacheEntry.getKey(), cacheEntry.getPid() , cacheEntry.getState());
		}

	}

	class WriteBackCacheListener extends CacheAdapter {

		private int cacheIndex;

		public WriteBackCacheListener(int cacheIndex) {
			this.cacheIndex = cacheIndex;
		}

		public void objectIsToBeEvicted(CacheEvent e) {
			CacheEntry cacheEntry = e.getCache().getEntry(e.getIndex());
			if (e.getCache().getEntry(e.getIndex()).getState() == CacheEntry.MODIFDATA) {
				notifyBackObjectModify(cacheIndex, cacheEntry.getKey(), cacheEntry.getPid() , cacheEntry.getState());
			}
		}

	}

	public String getAllCachesStats() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cacheModels.length; i++) {
			sb.append(cacheModels[i].getCacheStat().getStatString());
			sb.append("\n\n");
		}
		return sb.toString();
	}

	public int[] getCacheIndexes() {
		return cacheIndexes;
	}

}
