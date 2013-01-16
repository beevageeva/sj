package view.model;

import java.util.ArrayList;
import java.util.List;

public class InversePageTableEntry {

	private List<PageTableEntry> vpns;
	
	public InversePageTableEntry(){
		vpns = new ArrayList<PageTableEntry>();
	}
	
	/**
	 * @param vpn
	 * @param inMemory
	 * @return the index in vpns array where the entry is added
	 */
	public int add(int vpn, boolean inMemory){
		vpns.add(new PageTableEntry(vpn , inMemory));
		return vpns.size()-1;
	}
	
	public int indexOf(int vpn){
		return vpns.indexOf(new PageTableEntry(vpn));
	}
	/**
	 * @return the index in vpns array where the entry with inMemory = true is found
	 */
	public int setInMemoryFalse(){
		PageTableEntry entry ;
		for(int i = 0 ; i<vpns.size();i++){
			entry = vpns.get(i);
			if(entry.isInMemory()){
				entry.setInMemory(false);
				return i;
			}
		}
		return -1;
	}
	
	
	public void setInMemory(int indexVPNArray , boolean inMemory){
		vpns.get(indexVPNArray).setInMemory(inMemory);
	}
	
	public boolean isInMemory(int indexVPNArray){
		return vpns.get(indexVPNArray).isInMemory();
	}
	
	public String getStringRepr(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i<vpns.size() -1 ; i++){
			sb.append(getEntryStringRepr(vpns.get(i)));
			sb.append(",");
		}
		if(vpns.size()>0){
			sb.append(getEntryStringRepr(vpns.get(vpns.size()-1)));
		}
		return sb.toString();
	}
	
	private String getEntryStringRepr(PageTableEntry entry){
		StringBuffer sb = new StringBuffer();
		sb.append(entry.getPageNumber());
		sb.append("(");
		sb.append(entry.isInMemory()?"1":"0");
		sb.append(")");
		return sb.toString();
	}
}
