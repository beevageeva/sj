package view.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import trace.Instr;
import trace.InstrReader;
import u.Logger;
import view.model.ProcQueueMg.Proc;
import view.model.ProcQueueMg.ProcContext;
import view.model.ProcQueueMg.ProcessListener;

public class OPTCache extends Cache implements ProcessListener{

	private static final long serialVersionUID = 1L;
	
	private ProcQueueMg pQueueMg; 
	private int[] putNI;
	
	private Map<Integer , List<List<Integer>>> sameKeyIndexes = new HashMap<Integer,List<List<Integer>>>();
	
	
	public OPTCache(int maxNumberEntries , ProcQueueMg pQueueMg  , boolean isExt) {
		super(maxNumberEntries , isExt);
		this.pQueueMg = pQueueMg;
		pQueueMg.addProcessListener(this);
		putNI = new int[maxNumberEntries];
	}
	

	@Override
	protected int findNodeToEvict(CacheEntry cacheEntry) {
		int currentPid;
		int currentPidNi;
		int currentPidTotalTU;
		List<List<Integer>> pidIndexes;
		float max = 0;
		int maxIndex = -1;
		boolean stop;
		ProcContext pct;
		Proc p;
		synchronized(pQueueMg.lock){
			for(int i = region.getStartIndex(cacheEntry) ; i<region.getEndIndex(cacheEntry);i++){
				if (region.isInRegion(i , cacheEntry)) {
					currentPid = entries[i].getPid();
					pct = pQueueMg.getProcessContext(currentPid);
					if(pct == null){
						Logger.log("OPTCache process with pid = "+currentPid+" not in queues");
						continue;
					}
					currentPidNi = pct.ni;
					p = pQueueMg.getProcess(currentPid);
					if(p==null){
						Logger.log("OPTCache process with pid = "+currentPid+" not in proc list");
						continue;
					}
					currentPidTotalTU = p.tUnits;
					//search the next index from currentPidNi in the hashmap in an array that contains putNI[i]
					pidIndexes = sameKeyIndexes.get(currentPid);
					stop = false;
					for(int j = 0 ; j<pidIndexes.size() && !stop;j++){
						if(pidIndexes.get(j).contains(putNI[i])){
							for(int k = 0 ; k<pidIndexes.get(j).size()&&!stop; k++){
								if(pidIndexes.get(j).get(k)>=currentPidNi){
									stop = true;
									if((float)(pidIndexes.get(j).get(k)/currentPidTotalTU)>max){
										max = (float)(pidIndexes.get(j).get(k)/currentPidTotalTU);
										maxIndex = i;
									}
								}
							}
							if(!stop){
								stop = true;
								if((float)(pQueueMg.getProcess(currentPid).instrReader.getInstructions().size()/currentPidTotalTU)>max){
									max = (float)(pQueueMg.getProcess(currentPid).instrReader.getInstructions().size()/currentPidTotalTU);
									maxIndex = i;
								}
								
							}
						}
					}
				}
			}
		}
		return maxIndex;
	}
			
		

	
	@Override
	public int getColumnCount() {
		return super.getColumnCount() + 1;
	}

	@Override
	public String getColumnName(int col) {
		if(col<getColumnCount()-1){
			return super.getColumnName(col);
		}
		return "ni";
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(col<getColumnCount()-1){
			return super.getValueAt(row, col);
		}
		if(entries[row] == null){
			return -1;
		}
		return putNI[row];
	}

	
	
	public void objectRead(CacheEvent e) {
	}

	public void objectIsToBeModified(CacheEvent e) {
	}

	public void objectPut(CacheEvent e) {
		putNI[e.getIndex()] = pQueueMg.getProcessContext(e.getCache().getEntry(e.getIndex()).getPid()).ni;
	}

	public void objectIsToBeEvicted(CacheEvent e) {
	}

	public void objectIsToBeRemoved(CacheEvent e) {
	}



	public void processFinished(int pid) {
		sameKeyIndexes.remove(pid);
	}


	public void processAdded(int pid, InstrReader instrReader, int tUnits) {
		List<List<Integer>> pidIndexes = new ArrayList<List<Integer>>();
		List<Integer> pidElemIndexes;
		Instr instr ;
		boolean found;
		for(int i = 0 ; i<instrReader.getInstructions().size() ;i++){
			instr = instrReader.getInstructions().get(i);
			//look for i in order not to be in previous lists
			found = false;
			for(int j = 0 ; j<pidIndexes.size()&&!found ; j++){
				if(pidIndexes.get(j).contains(i)){
					found = true;
				}
			}
			//if doesn't exist create a list with all occurences of the va present at index i
			if(!found){
				pidElemIndexes = new ArrayList<Integer>();
				pidElemIndexes.add(i);
				for(int j = i+1 ; j<instrReader.getInstructions().size();j++){
					if(instr.addressBinaryRepr.equals(instrReader.getInstructions().get(j).addressBinaryRepr)){
						pidElemIndexes.add(j);
					}
				}
				pidIndexes.add(pidElemIndexes);
			}
		}
		sameKeyIndexes.put(pid , pidIndexes);
	}
	
	@Override
	public void clearAdditFields(int i) {
		putNI[i] = 0;
	}

}
