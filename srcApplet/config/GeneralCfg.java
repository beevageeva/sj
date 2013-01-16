package config;

public class GeneralCfg implements Config{

	private int virtualAddrNBits=0;
	private int numberProcessesNBits = 0;
	private int diskAccessTime = 0;
	
	//TODO put in null , only create if enabled
	public PageAgingConfig pageAgingConfig = new PageAgingConfig();
	public MainMemoryAllocConfig memAllocConfig = new MainMemoryAllocConfig();
	
	
	
	
	
	public int getDiskAccessTime() {
		return diskAccessTime;
	}
	public void setDiskAccessTime(int diskAccessTime) {
		this.diskAccessTime = diskAccessTime;
	}
	public int getNumberProcessesNBits() {
		return numberProcessesNBits;
	}
	public void setNumberProcessesNBits(int numberProcessesNBits) {
		this.numberProcessesNBits = numberProcessesNBits;
	}
	public int getVirtualAddrNBits() {
		return virtualAddrNBits;
	}
	public void setVirtualAddrNBits(int virtualAddrNBits) {
		this.virtualAddrNBits = virtualAddrNBits;
	}
	public String getStringInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append("virtual memory number of bits = ");
		sb.append(virtualAddrNBits);
		sb.append("\nmax number of processes = 2**");
		sb.append(numberProcessesNBits);
		sb.append("\ndisk access Time Units = ");
		sb.append(diskAccessTime);
		return sb.toString();
	}
	public int getNumberProcesses() {
		return (int) Math.pow(2 , numberProcessesNBits);
	}

	
	
}
