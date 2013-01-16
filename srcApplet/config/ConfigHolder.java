package config;

public class ConfigHolder {

	public static final int  numberCaches = 3;
	
	public static GeneralCfg generalCfg = new GeneralCfg();
	public static CacheConfig[] cacheCfgs = new CacheConfig[numberCaches+1];
	public static PageTableConfig pageTableCfg = new PageTableConfig();
	
	static{
		for(int i = 0 ; i<=numberCaches;i++){
			cacheCfgs[i] = new CacheConfig();
		}
	}
	
	
	
	public static int getNumberEnabledCaches(){
		int n = 0;
		for(int i = 0 ; i<cacheCfgs.length ; i++){
			if(cacheCfgs[i].isEnabled()){
				n++;
			}
		}
		return n;
	}
	
	public static int getPageSizeNBits(){
		//because inmain memory data and instr are not separated
		return cacheCfgs[numberCaches].getBlockSizeNBits()[0];
	}
	
}
