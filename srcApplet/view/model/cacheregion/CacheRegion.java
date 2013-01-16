package view.model.cacheregion;

import java.util.ArrayList;
import java.util.List;

import view.model.CacheEntry;

public abstract class CacheRegion {
	
	protected  int maxNumberEntries;
	
	public CacheRegion(int maxNumberEntries){
		this.maxNumberEntries = maxNumberEntries;
	}
	
	public int getStartIndex(CacheEntry entry) {
		return 0;
	}

	public int getEndIndex(CacheEntry entry) {
		return maxNumberEntries;
	}

	public abstract boolean isInRegion(int index, CacheEntry entry);
	
	public abstract int getAdditionalColumnCount();
	
	
	public List<List<Integer>> getPartitionSets(){
		List<Integer> set = new ArrayList<Integer>();
		for(int i = 0 ; i<maxNumberEntries;i++){
			set.add(i);
		}
		List<List<Integer>> sets = new ArrayList<List<Integer>>();
		sets.add(set);
		return sets;
	}
	
	
	public  String[] getAdditionalColumnNames(){
		String[] res = new String[getAdditionalColumnCount()];
		for(int i = 0 ; i<getAdditionalColumnCount();i++){
			res[i] = "addCol"+i;
		}
		return res;
	}
	
	public String[] getAdditionalColumnTooltipString(){
		return getAdditionalColumnNames();
	}
	
	public  Object getValue(int addCol , CacheEntry entry){
		return null;
	}
	
	
	
	public int getRegionNumberEntries(CacheEntry entry){
		int n = 0;
		for (int i = getStartIndex(entry); i < getEndIndex(entry); i++) {
			if (isInRegion(i , entry)) {
				n++;
			}
		}
		return n;
	}
	
}
