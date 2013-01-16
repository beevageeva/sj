package view.model.cacheregion;

import java.util.ArrayList;
import java.util.List;

import view.model.CacheEntry;

public class SetsCacheRegion extends CacheRegion {

	protected int numberSets;
	
	public SetsCacheRegion(int maxNumberEntries , int numberSets) {
		super(maxNumberEntries);
		this.numberSets = numberSets;
	}

	@Override
	public boolean isInRegion(int index, CacheEntry entry) {
		return index%numberSets == entry.getKey()%numberSets;
	}

	@Override
	public int getAdditionalColumnCount() {
		return 2;
	}

	@Override
	public String[] getAdditionalColumnNames() {
		return new String[]{"TAG" , "SET"};
	}
	
	

	@Override
	public String[] getAdditionalColumnTooltipString() {
		return new String[]{"Tag", "Set"};
	}

	@Override
	public Object getValue(int addCol , CacheEntry entry) {
		switch(addCol){
		case 0:
			return entry.getKey()/numberSets;
		case 1:
			return entry.getKey()%numberSets;
		}
		return entry;
	}

	@Override
	public List<List<Integer>> getPartitionSets() {
		List<List<Integer>> sets = new ArrayList<List<Integer>>();
		List<Integer> set;
		for(int i = 0 ; i<numberSets ; i++){
			set = new ArrayList<Integer>();
			for(int j = 0 ; j<maxNumberEntries/numberSets; j++){
				set.add(i + j * numberSets);
			}
			sets.add(set);
		}
		return sets;
	}
	
	

}
