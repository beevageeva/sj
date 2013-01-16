package view.model;

public class CacheEntry {

	public static final short MODIFDATA = 0;
	public static final short READDATA = 1;
	public static final short FETCHINSTR = 2;
	
	protected  int key;
	protected int pid;
	protected short state;
	
	public CacheEntry(int pid , int key ,short state){
		this.pid = pid;
		this.key = key;
		this.state = state;
	}
	
	public boolean isDataEntry(){
		return state == MODIFDATA || state == READDATA;
	}
	
	public CacheEntry() {}
	
	public CacheEntry(CacheEntry entry){
		this.pid = entry.getPid();
		this.state = entry.state;
		this.key =  entry.key;
	}
	
	public short getState() {
		return state;
	}
	public void setState(short state) {
		if( (state==FETCHINSTR && (this.state==MODIFDATA || this.state == READDATA))||  (this.state==FETCHINSTR && (state==MODIFDATA || state == READDATA))){
			//TODO
		}
		this.state = state;
	}
	public int getKey() {
		return key;
	}
	public void setKey(int value) {
		this.key = value;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}
	
	
}
