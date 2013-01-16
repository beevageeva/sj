package view.comp.model;

import java.util.ArrayList;
import java.util.List;

import view.model.CacheAdapter;
import view.model.CacheEvent;

public class PageAgingThread implements Runnable  {

	private int incUnits , memRefToBeRun;
	private CacheModel[] cacheModels;
	private int[][] age;
	private int memRef;
	
	private List<PageAgingListener> pAgList = new ArrayList<PageAgingListener>();

	
	public PageAgingThread(CacheModel[] cacheModels , int incUnits , int memRefToBeRun){
		this.memRefToBeRun = memRefToBeRun;
		this.incUnits = incUnits;
		this.cacheModels = cacheModels;
		age = new int[cacheModels.length][];
		for(int i = 0 ; i<cacheModels.length ; i++){
			age[i] = new int[cacheModels[i].getCache().getNumberEntries()];
			cacheModels[i].getCache().addCacheListener(new PageAgingCacheListener(i));
		}

	}
	
	public void run() {
		for(int j = 0 ; j<cacheModels.length;j++){
			for(int i = 0 ; i<age.length ; i++){
				if(cacheModels[j].getCache().getEntry(i)!=null){
					if(age[j][i]<=0){
						cacheModels[j].getCache().removeRow(i);
						for(int k = 0 ; k<pAgList.size();k++){
							pAgList.get(k).pageRemoved(j , i);
						}
					}
					age[j][i]--;
				}
			}
		}
		//clear var
		memRef=0;

		
	}
	
	private void startThread(){
		new Thread(this).start();
	}
	
	public void addPageAgingListener(PageAgingListener l){
		pAgList.add(l);
	}
	
	public interface PageAgingListener{
		public void pageRemoved(int cacheIndex , int pageIndex);
	}
	
	class PageAgingCacheListener extends CacheAdapter{
		private int cIndex;
		
		public PageAgingCacheListener(int cIndex){
			this.cIndex = cIndex;
		}
		public void objectRead(CacheEvent e) {
			age[cIndex][e.getIndex()]+=incUnits;
			memRef++;
			if(memRef>=memRefToBeRun){
				startThread();
			}
			
		}

		public void objectIsToBeModified(CacheEvent e) {
			objectRead(e);
		}

	}
	
	
}
