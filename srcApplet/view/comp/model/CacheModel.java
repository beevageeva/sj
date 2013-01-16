package view.comp.model;

import view.comp.run.App;
import view.model.Cache;
import view.model.CacheEvent;
import view.model.CacheListener;
import view.model.FIFOCache;
import view.model.LFUCache;
import view.model.LRUCache;
import view.model.MRUCache;
import view.model.NFUCache;
import view.model.NRUCache;
import view.model.OPTCache;
import view.model.RandomCache;
import view.model.cacheregion.DataInstrSepCacheRegion;
import view.model.cacheregion.SetsCacheRegion;
import config.CacheConfig;

public class CacheModel implements CacheListener {

	private Cache cache;

	private CacheConfig cfg;

	private CacheStat cacheStat;

	public CacheModel(CacheConfig cfg, App app) {
		this(cfg, false, app);
	}

	public CacheModel(CacheConfig cfg, boolean isExt, App app) {
		this.cfg = cfg;
		cacheStat = new CacheStat();
		int numberEntries = cfg.isDataInstrSeparated() ? cfg.getNumberEntries() * 2
				: cfg.getNumberEntries();
		switch (cfg.getEvictionPolicy()) {
		case CacheConfig.RANDOM_POLICY:
			cache = new RandomCache(numberEntries, isExt);
			break;
		case CacheConfig.FIFO_POLICY:
			cache = new FIFOCache(numberEntries, isExt);
			break;
		case CacheConfig.LFU_POLICY:
			cache = new LFUCache(numberEntries, isExt);
			break;
		case CacheConfig.LRU_POLICY:
			cache = new LRUCache(numberEntries, isExt);
			break;
		case CacheConfig.NFU_POLICY:
			cache = new NFUCache(numberEntries, isExt);
			break;
		case CacheConfig.NRU_POLICY:
			cache = new NRUCache(numberEntries, isExt);
			break;
		case CacheConfig.OPT_POLICY:
			cache = new OPTCache(numberEntries, app.procQueueMg, isExt);
			break;
		case CacheConfig.MRU_POLICY:
			cache = new MRUCache(numberEntries, isExt);
			break;

		}

		cache.addCacheListener(this);
		if (cfg.isDataInstrSeparated()) {
			cache
					.addRegionIntersect(new DataInstrSepCacheRegion(
							numberEntries));
		}
		if (cfg.getNumberSetsNBits() != 0) {
			cache.addRegionIntersect(new SetsCacheRegion(numberEntries, cfg
					.getNumberSets()));
		}

	}

	public void objectRead(CacheEvent e) {
		cacheStat.addRead();
	}

	public void objectIsToBeModified(CacheEvent e) {
		cacheStat.addModif();
	}

	public void objectPut(CacheEvent e) {
		cacheStat.addPut();
	}

	public void objectIsToBeEvicted(CacheEvent e) {
		cacheStat.addEvict();
	}

	public void objectIsToBeRemoved(CacheEvent e) {
		cacheStat.addRemoved();
	}

	public CacheStat getCacheStat() {
		return cacheStat;
	}

	public CacheConfig getCfg() {
		return cfg;
	}

	public Cache getCache() {
		return cache;
	}

	public void clearCache() {
		cache.clearCache();
		cacheStat.clear();
	}

	public void clearCache(int pid) {
		cache.clearCacheByPid(pid);
	}

	
	public class CacheStat {

		int put, read, modif, evict, removed;

		public void addPut() {
			put++;
		}

		public void addRead() {
			read++;
		}

		public void addModif() {
			modif++;
		}

		public void addEvict() {
			evict++;
		}

		public void addRemoved() {
			removed++;
		}

		public String getStatString() {
			StringBuffer sb = new StringBuffer();
			sb.append("misses : ");
			sb.append(put);
			sb.append("\nread : ");
			sb.append(read);
			sb.append("\nmodified : ");
			sb.append(modif);
			sb.append("\nevicted : ");
			sb.append(evict);
			sb.append("\nremoved : ");
			sb.append(removed);
			return sb.toString();
		}

		public void clear() {
			put = read = modif = evict = removed = 0;
		}

	}

}
