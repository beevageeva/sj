package view.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import trace.Instr;
import trace.InstrReader;
import u.Logger;
import config.ConfigHolder;

public class ProcQueueMg extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private Map<Integer, Proc> proc = null;

	private List<ProcInterruptedContext> waitingProc = null;

	private List<ProcContext> toExecProc = null;

	private short type;

	private int pid;

	private int timeUnits;

	public static final short INSTR1 = 0;

	public static final short INSTR1INTER = 1;

	public static final short INSTR2 = 2;

	public Object lock = new Object();

	private static List<ProcessListener> procListeners = null;

	//initialize here the hasmaps and arrays : TODO
	public ProcQueueMg(){
		proc = new HashMap<Integer, Proc>();
		waitingProc = new ArrayList<ProcInterruptedContext>();
		toExecProc = new ArrayList<ProcContext>();
		procListeners = new ArrayList<ProcessListener>();
	}


	public short getType() {
		return type;
	}

	public int getPid() {
		return pid;
	}


	public void reset(){
		proc.clear();
		waitingProc.clear();
		toExecProc.clear();
		procListeners.clear();
	}

	public synchronized void addProc(int pid, InstrReader instrReader,
			int tUnits) {
		proc.put(new Integer(pid), new Proc(instrReader, tUnits));
		toExecProc.add(new ProcContext(pid, 0, tUnits));
		fireProcessAdded(pid , instrReader , tUnits);
		fireTableRowsUpdated(0, proc.size());
	}


	public synchronized void remove(int cPid) {
		synchronized (lock) {
			proc.remove(cPid);
			boolean foundInQueue = false;
			for (int i = 0; i < waitingProc.size() && !foundInQueue; i++) {
				if (waitingProc.get(i).pid == cPid) {
					waitingProc.remove(i);
					foundInQueue = true;
				}
			}
			for (int i = 0; i < toExecProc.size() && !foundInQueue; i++) {
				if (toExecProc.get(i).pid == cPid) {
					toExecProc.remove(i);
					foundInQueue = true;
				}
			}
		}
		
		fireTableRowsUpdated(0, ConfigHolder.generalCfg.getNumberProcesses());

	}

	public int getSize() {
		return proc.size();
	}

	public List<Instr> getInstr(int nProc) {
		return proc.get(nProc).instrReader.getInstructions();
	}

	public String[] getNames() {
		String[] instrTrNames = new String[proc.size()];
		Iterator<Integer> keysIt = proc.keySet().iterator();
		int pid;
		int index = 0;
		while(keysIt.hasNext()){
			pid = keysIt.next();
			instrTrNames[index++] = proc.get(pid).instrReader.getName();
		}
		return instrTrNames;
	}

	public void putInWait(int timeUnits, short instrType, String realAddr) {
		ProcContext ct;
		if (toExecProc.size() == 0) {
			return;
		}
		ct = toExecProc.remove(0);
		waitingProc.add(new ProcInterruptedContext(ct.pid, ct.ni, timeUnits,
				instrType, realAddr));
		type = INSTR1INTER;
		fireTableRowsUpdated(0, proc.size());
	}

	private void moveToExec() {
		ProcInterruptedContext pic;
		pic = waitingProc.remove(0);
		pic.timeUnitsLeft = proc.get(pic.pid).tUnits;
		toExecProc.add(pic);
		fireTableRowsUpdated(0, ConfigHolder.generalCfg.getNumberProcesses());

	}

	public Instr getCurrentInstr() {
		synchronized (lock) {
			if (toExecProc.size() == 0) {
				if (waitingProc.size() == 0) {
					return null;
				}
				// Wait
				moveToExec();
				fireTableRowsUpdated(0, proc.size());
			}
			ProcContext ct = toExecProc.get(0);
			pid = ct.pid;
			if (ct instanceof ProcInterruptedContext) {
				type = INSTR2;
				if(proc.get(ct.pid)== null){
					Logger.log("in getCurrentInstr for pid = "+ ct.pid +" proc.get(pid) is null");
					return null;
				}
				if (ct.ni < proc.get(ct.pid).instrReader.getInstructions()
						.size()) {
					toExecProc.remove(0);
					toExecProc.add(0, new ProcContext(ct.pid, ct.ni,
							ct.timeUnitsLeft));
					fireTableRowsUpdated(0 , proc.size());
				} else {
					fireProcFinished(toExecProc.get(0).pid);
				}
				return ((ProcInterruptedContext) ct).instrInter;
			}
			type = INSTR1;
			return proc.get(ct.pid).instrReader.getInstructions().get(ct.ni);
		}
	}

	public void finishInstr() {
		synchronized (lock) {
			if (type != INSTR1INTER) {
				// if there is only 1 proc , this might be removed in
				// the pause
				if (toExecProc.size() > 0) {
					ProcContext ct = toExecProc.get(0);
					ct.timeUnitsLeft -= timeUnits;
					if (ct.ni < proc.get(ct.pid).instrReader.getInstructions()
							.size() - 1) {
						ct.ni++;
						if (ct.timeUnitsLeft <= 0) {
							toExecProc.remove(0);
							ct.timeUnitsLeft = proc.get(ct.pid).tUnits;
							toExecProc.add(ct);
						}
					} else {
						fireProcFinished(toExecProc.get(0).pid);
					}
				}

				boolean s = true;
				int tu = timeUnits;

				while (s && waitingProc.size() > 0) {
					if (waitingProc.get(0).timeUnitsLeft < tu) {
						tu -= waitingProc.get(0).timeUnitsLeft;
						moveToExec();
					} else {
						waitingProc.get(0).timeUnitsLeft -= tu;
						s = false;
					}
				}
			}
		}
		fireTableRowsUpdated(0,ConfigHolder.generalCfg.getNumberProcesses() );
		timeUnits = 0;
	}

	public void addTimeUnits(int tu) {
		timeUnits += tu;
	}

	class Proc {
		Proc(InstrReader instrReader, int tUnits) {
			this.instrReader = instrReader;
			this.tUnits = tUnits;
		}

		InstrReader instrReader;

		int tUnits;
	}

	class ProcContext {
		protected int pid;

		protected int ni;

		protected int timeUnitsLeft;

		public ProcContext(int pid, int ni, int timeUnitsLeft) {
			this.pid = pid;
			this.ni = ni;
			this.timeUnitsLeft = timeUnitsLeft;
		}

	}

	class ProcInterruptedContext extends ProcContext {
		public ProcInterruptedContext(int pid, int ni, int timeUnitsLeft) {
			super(pid, ni, timeUnitsLeft);
		}

		public ProcInterruptedContext(int pid, int ni, int timeUnitsLeft,
				short instrType, String rAddress) {
			this(pid, ni, timeUnitsLeft);
			this.instrInter = new Instr(instrType, rAddress);
		}

		Instr instrInter;

	}

	public String getString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WAITING QUEUE :\n");
		ProcInterruptedContext pic;
		for (int i = 0; i < waitingProc.size(); i++) {
			pic = waitingProc.get(i);
			sb.append("P");
			sb.append(pic.pid);
			sb.append(" ,instrNumber : ");
			sb.append(pic.ni);
			sb.append(" , timeToWait : ");
			sb.append(pic.timeUnitsLeft);
			sb.append("\n");
		}
		sb.append("\nEXECUTING QUEUE :\n");
		ProcContext pc;
		for (int i = 0; i < toExecProc.size(); i++) {
			pc = toExecProc.get(i);
			sb.append("P");
			sb.append(pc.pid);
			sb.append(" ,instrNumber : ");
			sb.append(pc.ni);
			sb.append(" , timeUnitsLeft : ");
			sb.append(pc.timeUnitsLeft);
			sb.append("\n");
		}
		return sb.toString();
	}

	public synchronized void clear() {
		proc.clear();
		waitingProc.clear();
		toExecProc.clear();
		fireTableRowsUpdated(0, ConfigHolder.generalCfg.getNumberProcesses());
	}

	public synchronized void reinit() {
		waitingProc.clear();
		toExecProc.clear();
		Iterator<Integer> it = proc.keySet().iterator();
		int key;
		while (it.hasNext()) {
			key = it.next();
			toExecProc.add(new ProcContext(key, 0, proc.get(key).tUnits));
		}
		fireTableRowsUpdated(0, ConfigHolder.generalCfg.getNumberProcesses());
	}

	public int getRowCount() {
		return (int) Math.pow(2, ConfigHolder.generalCfg
				.getNumberProcessesNBits());
	}

	public int getColumnCount() {
		return 7;
	}

	public int getPidAtIndex(int index) {
		Iterator<Integer> keys = proc.keySet().iterator();
		int k = 0;
		int key;
		while (keys.hasNext()) {
			key = keys.next();
			if (k == index) {
				return key;
			}
			k++;
		}
		return -1;
	}

	public Object getValueAt(int row, int col) {
		if (row < proc.size()) {
			int cPid = getPidAtIndex(row);
			ProcContext pc = null;
			int j;
			for (j = 0; j < waitingProc.size() && pc == null; j++) {
				if (waitingProc.get(j).pid == cPid) {
					pc = waitingProc.get(j);
				}
			}
			if (pc == null) {
				for (j = 0; j < toExecProc.size() && pc == null; j++) {
					if (toExecProc.get(j).pid == cPid) {
						pc = toExecProc.get(j);
					}
				}
			}
			switch (col) {
			case 0:
				return cPid;
			case 1:
				return proc.get(cPid).instrReader.getInstructions();

			case 2:
				return proc.get(cPid).tUnits;
			case 3:
				return pc.ni;
			case 4:
				return pc.timeUnitsLeft;
			case 5:
				return (pc instanceof ProcInterruptedContext ? "W" : "E");
			case 6:
				return j;
			}
		}
		return "-";

	}
	
	public ProcContext getProcessContext(int pid){
		ProcContext pc = null;
		for (int j = 0; j < waitingProc.size() && pc == null; j++) {
			if (waitingProc.get(j).pid == pid) {
				pc = waitingProc.get(j);
			}
		}
		for (int j = 0; j < toExecProc.size() && pc == null; j++) {
			if (toExecProc.get(j).pid == pid) {
				pc = toExecProc.get(j);
			}
		}
		return pc;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return "pid";
		case 1:
			return "instr";
		case 2:
			return "TU";
		case 3:
			return "ni";
		case 4:
			return "cTuLeft";
		case 5:
			return "cQueue";
		case 6:
			return "cQInd";
		}
		return null;
	}

	public interface ProcessListener {
		public void processFinished(int pid);
		public void processAdded(int pid, InstrReader instrReader,
			int tUnits);
	}

	public void addProcessListener(ProcessListener l) {
		procListeners.add(l);
	}

	private void fireProcessAdded(int pid, InstrReader instrReader,
			int tUnits){
		for (int i = 0; i < procListeners.size(); i++) {
			procListeners.get(i).processAdded(pid, instrReader , tUnits);
		}
		
	}
	
	private void fireProcFinished(int pid) {
		for (int i = 0; i < procListeners.size(); i++) {
			procListeners.get(i).processFinished(pid);
		}
	}
	
	public Proc getProcess(int pid){
		return proc.get(pid);
	}
	
}
