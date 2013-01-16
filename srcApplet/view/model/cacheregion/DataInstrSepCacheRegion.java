package view.model.cacheregion;

import java.util.ArrayList;
import java.util.List;

import view.model.CacheEntry;

public class DataInstrSepCacheRegion extends CacheRegion {

	public DataInstrSepCacheRegion(int maxNumberEntries) {
		super(maxNumberEntries);
	}

	public @Override
	String[] getAdditionalColumnNames() {
		return new String[] { "type" };
	}
	
	public @Override
	String[] getAdditionalColumnTooltipString() {
		return new String[] { "if the entry repr a data or an instruction" };
	}

	
	@Override
	public int getEndIndex(CacheEntry entry) {
		if (entry.isDataEntry()) {
			return maxNumberEntries / 2;
		} else {
			return maxNumberEntries;
		}

	}

	@Override
	public int getStartIndex(CacheEntry entry) {
		if (entry.isDataEntry()) {
			return 0;
		} else {
			return maxNumberEntries / 2;
		}
	}

	@Override
	public Object getValue(int addCol, CacheEntry entry) {
		if (addCol == 0) {
			if (entry.isDataEntry()) {
				return "D";
			} else {
				return "I";
			}
		}
		return null;
	}

	@Override
	public boolean isInRegion(int index, CacheEntry entry) {
		if (entry.isDataEntry()) {
			return index < maxNumberEntries / 2;
		} else {
			return index >= maxNumberEntries / 2;
		}
	}

	@Override
	public int getAdditionalColumnCount() {
		return 1;
	}

	@Override
	public List<List<Integer>> getPartitionSets() {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		List<Integer> set = new ArrayList<Integer>();
		for (int i = 0; i < maxNumberEntries / 2; i++) {
			set.add(i);
		}
		res.add(set);
		set = new ArrayList<Integer>();
		for (int i = maxNumberEntries / 2; i < maxNumberEntries; i++) {
			set.add(i);
		}
		res.add(set);
		return res;
	}

}
