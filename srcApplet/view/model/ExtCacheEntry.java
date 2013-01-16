package view.model;

public class ExtCacheEntry extends CacheEntry{

	protected int value;
	
	public ExtCacheEntry(int pid , int key ,short state , int value){
		super(pid , key , state);
		this.value  = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	
	
}
