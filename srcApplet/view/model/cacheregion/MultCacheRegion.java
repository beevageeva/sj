package view.model.cacheregion;

import java.util.ArrayList;
import java.util.List;

import view.model.CacheEntry;


public class MultCacheRegion extends CacheRegion {
	private CacheRegion[] regions;

	public MultCacheRegion(int maxNumberEntries ,CacheRegion[] regions) {
		super(maxNumberEntries);
		this.regions = regions;
	}

	
	
	
	
		@Override
		public int getEndIndex(CacheEntry entry) {
			//return the min
			int endIndex = maxNumberEntries;
			for(int i = 0 ; i<regions.length ; i++){
				if(regions[i].getEndIndex(entry) < endIndex){
					endIndex = regions[i].getEndIndex(entry);
				}
			}
			return endIndex;
		}

		@Override
		public int getStartIndex(CacheEntry entry) {
			//return the max of startIndexes
			int startIndex = 0;
			for(int i = 0 ; i<regions.length ; i++){
				if(regions[i].getStartIndex(entry) > startIndex){
					startIndex = regions[i].getStartIndex(entry);
				}
			}
			return startIndex;

		}

		@Override
		public boolean isInRegion(int index, CacheEntry entry) {
			//return true if it's true for all regions
			boolean isInRegion = true;
			for(int i = 0 ; i<regions.length && isInRegion; i++){
				isInRegion = isInRegion && regions[i].isInRegion(index , entry);
			}
			return isInRegion;

		}

		@Override
		public int getAdditionalColumnCount() {
			//the sum of number of all region  addit columns
			int addColCount = 0;
			for(int i = 0 ; i<regions.length ; i++){
				addColCount += regions[i].getAdditionalColumnCount();
			}
			return addColCount;
		}

		@Override
		public String[] getAdditionalColumnNames() {
			//an array of strings 
			String[] res = new String[getAdditionalColumnCount()];
			int k = 0;
			int jColCount;
			for(int j = 0 ; j<regions.length ; j++){
				jColCount = regions[j].getAdditionalColumnCount();
				for (int i = 0; i < jColCount; i++) {
					res[i + k] = regions[j].getAdditionalColumnNames()[i];
				}
				k+=jColCount;
			}
			return res;
		}
		
		@Override
		public String[] getAdditionalColumnTooltipString() {
			//an array of strings 
			String[] res = new String[getAdditionalColumnCount()];
			int k = 0;
			int jColCount;
			for(int j = 0 ; j<regions.length ; j++){
				jColCount = regions[j].getAdditionalColumnCount();
				for (int i = 0; i < jColCount; i++) {
					res[i + k] = regions[j].getAdditionalColumnTooltipString()[i];
				}
				k+=jColCount;
			}
			return res;
		}

		
		@Override
		public Object getValue(int addCol , CacheEntry entry) {
			int k = 0;
			int bColCount = 0;
			int colCount = 0;
			while(addCol>=colCount && k<regions.length){
				bColCount = colCount;
				colCount+=regions[k].getAdditionalColumnCount();
				k++;
			}
			k--;
			return regions[k].getValue(addCol - bColCount, entry);
		}





		@Override
		public List<List<Integer>> getPartitionSets() {
			if(regions.length==0){
				return null;
			}
			List<List<Integer>> res = regions[0].getPartitionSets();
			for(int i = 1 ; i<regions.length ; i++){
				res = join2Part(res , regions[i].getPartitionSets());
			}
			return res;
		}
		
		private List<List<Integer>> join2Part(List<List<Integer>> sets1 , List<List<Integer>>  sets2){
			List<List<Integer>> res = new ArrayList<List<Integer>>();
			List<Integer> set1, set2 , set;
			for(int i = 0 ; i<sets1.size();i++){
				set1 = sets1.get(i);
				for(int j = 0 ; j<sets2.size(); j++){
					set2 = sets2.get(j);
					set = new ArrayList<Integer>();
					for(int k = 0 ; k<set1.size();k++){
						for(int l = 0 ; l<set2.size(); l++){
							if(set1.get(k).intValue() == set2.get(l).intValue()){
								set.add(set1.get(k));
							}
						}
					}
					if(set.size()>0){
						res.add(set);
					}
				}
			}
			return res;
			
		}

		
		

}
