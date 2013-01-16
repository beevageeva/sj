package view.model;

import java.util.ArrayList;
import java.util.List;

import config.ConfigHolder;

import trace.InstrReader;
import view.model.ProcQueueMg.ProcessListener;

public class ProcessMg {

	public MainMemAllocator memAlloc;

	public ProcQueueMg pQueueMg;

	public static final short TOO_MANY_TRACE_FILES = 0;

	public static final short NO_MEM_AVAILABLE = 1;

	public static final short PROCESS_ALOCATED = 2;

	private int numberOfProcesses=0;
	
	private List<Integer> freePids = new ArrayList<Integer>();
	/**
	 * @param pqm
	 * @param mall
	 *            may be null if it is the global alloc policy
	 */
	public ProcessMg(ProcQueueMg pqm, MainMemAllocator mall) {
		this.pQueueMg = pqm;
		this.memAlloc = mall;
	}

	
	public void remove(int pid){
		pQueueMg.remove(pid);
		if (memAlloc != null) {
			memAlloc.remove(pid);
		}
		freePids.add(new Integer(pid));
	}

	
	
	public short addProcess(InstrReader ir, int tUnits) {
		int pid;
		boolean gotFromFreePids;
		if(freePids.size()>0){
			pid = freePids.get(0);
			gotFromFreePids = true;
		}
		else{
			pid = numberOfProcesses;
			gotFromFreePids = false;
		}
		if (pid<ConfigHolder.generalCfg.getNumberProcesses()) {
			if (memAlloc != null) {
				if (!memAlloc.allocateProcess(pid)) {
					return NO_MEM_AVAILABLE;
				}
			}
			pQueueMg.addProc(pid , ir, tUnits);
			if(gotFromFreePids){
				freePids.remove(0);
			}
			else{
				numberOfProcesses++;
			}
			return PROCESS_ALOCATED;
		}
		return TOO_MANY_TRACE_FILES;
	}

	
	public void reinit(){
		numberOfProcesses=0;
		pQueueMg.reinit();
		if(memAlloc!=null){
			memAlloc.reinit();
		}
	}


}
