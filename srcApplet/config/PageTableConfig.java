package config;


public class PageTableConfig implements Config{

	private CacheConfig tlbConfig = new CacheConfig();
	private Config addCfg;
	
	public void setDirectMapped(boolean direct){
		if(direct){
			addCfg = new DirectMappedPageTableConfig();
		}
		else{
			addCfg = new InverseMappedPageTableCfg();
		}
	}
	
	public boolean isDirectMapped(){
		return (addCfg!=null  && addCfg instanceof DirectMappedPageTableConfig);
	}
	
	public boolean isInverseMapped(){
		return (addCfg!=null  && addCfg instanceof InverseMappedPageTableCfg);
	}
	

	public Config getAddCfg() {
		return addCfg;
	}

	public void setAddCfg(Config addCfg) {
		this.addCfg = addCfg;
	}

	public CacheConfig getTlbConfig() {
		return tlbConfig;
	}

	public void setTlbConfig(CacheConfig tlbConfig) {
		this.tlbConfig = tlbConfig;
	}

	public String getStringInfo() {
		// TODO Auto-generated method stub
		return null;
	}


	

	
}
