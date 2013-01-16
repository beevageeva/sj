package config;

public class MainMemoryAllocConfig {

	private int minPFF,maxPFF , nEvictedNodesToRun=-1;

	public int getMaxPFF() {
		return maxPFF;
	}

	public void setMaxPFF(int maxPFF) {
		this.maxPFF = maxPFF;
	}

	public int getMinPFF() {
		return minPFF;
	}

	public void setMinPFF(int minPFF) {
		this.minPFF = minPFF;
	}

	public int getNEvictedNodesToRun() {
		return nEvictedNodesToRun;
	}

	public void setNEvictedNodesToRun(int evictedNodesToRun) {
		nEvictedNodesToRun = evictedNodesToRun;
	}
	
	public boolean isEnabled(){
		return nEvictedNodesToRun!=-1;
	}
	
	public void disable(){
		nEvictedNodesToRun = -1;
	}

}
