package view.model;

public interface CacheListener {
	
	public void objectRead(CacheEvent e);
	public void objectIsToBeModified(CacheEvent e);
	public void objectPut(CacheEvent e);
	public void objectIsToBeEvicted(CacheEvent e);
	public void objectIsToBeRemoved(CacheEvent e);
	
}
