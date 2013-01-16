package config;

import view.model.CacheEntry;

public class CacheConfig implements Config{

	public static final short RANDOM_POLICY = 0;
	public static final short FIFO_POLICY = 1;
	public static final short LRU_POLICY = 2;
	public static final short LFU_POLICY = 3;
	public static final short NRU_POLICY = 4;
	public static final short NFU_POLICY = 5;
	public static final short OPT_POLICY = 6;
	public static final short MRU_POLICY = 7;
	
	
	private int numberEntriesNBits = -1;
	private short evictionPolicy = RANDOM_POLICY;
	private int numberSetsNBits = 0;
	public int[] blockSizeNBits = new int[1];
	private int busSize=0;
	private int accessTimeUnits = 0;
	private boolean writeThroughHitPolicy;
	private boolean writeAllocateMissPolicy;
	public boolean isWriteAllocateMissPolicy() {
		return writeAllocateMissPolicy;
	}
	public void setWriteAllocateMissPolicy(boolean writeAllocateMissPolicy) {
		this.writeAllocateMissPolicy = writeAllocateMissPolicy;
	}
	public boolean isWriteThroughHitPolicy() {
		return writeThroughHitPolicy;
	}
	public void setWriteThroughHitPolicy(boolean writeThroughHitPolicy) {
		this.writeThroughHitPolicy = writeThroughHitPolicy;
	}


	
	public int getAccessTimeUnits() {
		return accessTimeUnits;
	} 

	public void setAccessTimeUnits(int accessTimeUnits) {
		this.accessTimeUnits = accessTimeUnits;
	}

	//if not found in the cache , the nb of tmes to get the data from other 
	public int getTimesToGetData(short instrType){
		if(busSize==0){
			return 1;
		}
		return Math.round((int)Math.pow(2 ,getBlockSizeNBits(instrType))/busSize);
	}
	
	public boolean isEnabled(){
		return numberEntriesNBits!=-1;
	}
	
	public int[] getBlockSizeNBits(){
		return blockSizeNBits;
	}
	
	public int getBusSize() {
		return busSize;
	}
	public void setBusSize(int busSize) {
		this.busSize = busSize;
	}
	public int getBlockSizeNBits(short instrType) {
		if(blockSizeNBits.length==2 && instrType==CacheEntry.FETCHINSTR){
			return blockSizeNBits[1];
		}
		return blockSizeNBits[0];
	}
	
	public void setBlockSizeNBits(int[] offsetNBits) {
		this.blockSizeNBits = offsetNBits;
	}
	public int getNumberSets() {
		return (int) Math.pow(2,numberSetsNBits);
	}
	
	public int getNumberSetsNBits() {
		return numberSetsNBits;
	}
	
	public void setNumberSetsNBits(int numberSetsNBits) {
		this.numberSetsNBits = numberSetsNBits;
	}
	public short getEvictionPolicy() {
		return evictionPolicy;
	}
	public void setEvictionPolicy(short evictionPolicy) {
		this.evictionPolicy = evictionPolicy;
	}
	public int getNumberEntriesNBits() {
		return numberEntriesNBits;
	}
	
	public int getNumberEntries(){
		if(numberEntriesNBits == -1){
			return 0;
		}
		return (int) Math.pow(2 , numberEntriesNBits);
	}
	
	public void setNumberEntriesNBits(int numberEntries) {
		this.numberEntriesNBits = numberEntries;
	}
	
	public String getStringInfo(){
		StringBuffer sb = new StringBuffer();
		sb.append("max number entries = 2**");
		sb.append(numberEntriesNBits);
		sb.append("\nnumber sets = 2**");
		sb.append(numberSetsNBits);
		sb.append("\nblock size : ");
		if(blockSizeNBits.length==2){
			sb.append("DATA(2**");
			sb.append(blockSizeNBits[0]);
			sb.append(") , INSTR(2**");
			sb.append(blockSizeNBits[1]);
			sb.append(") ");
		}
		else{
			sb.append("2**");
			sb.append(blockSizeNBits[0]);
		}
		sb.append("\nbus size = ");
		sb.append(busSize);
		sb.append("\ndata and instr separated = ");

		sb.append(blockSizeNBits.length==2?"true":"false");
		sb.append("\neviction policy = ");
		switch(evictionPolicy){
		case RANDOM_POLICY:
			sb.append("RANDOM");
			break;
		case FIFO_POLICY:
			sb.append("FIFO");
			break;
		case LRU_POLICY:
			sb.append("LRU");
			break;
		case LFU_POLICY:
			sb.append("LFU");
			break;
		case NRU_POLICY:
			sb.append("NRU");
			break;
		case NFU_POLICY:
			sb.append("NFU");
			break;
		}
		sb.append("\ntime access units = ");
		sb.append(accessTimeUnits);
		sb.append("\nwrite miss policy = ");
		sb.append(writeAllocateMissPolicy?"WRITE-ALLOCATE":"NO WRITE ALLOCATE");

		return sb.toString();
		
	}
	
	public boolean isDataInstrSeparated(){
		return blockSizeNBits.length==2;
	}
}
