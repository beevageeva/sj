package view.comp.run;

import java.awt.BorderLayout;

import view.comp.model.CacheModel;

public class CachePanelShowAddr extends CachePanel{
	private static final long serialVersionUID = 1L;
	private AddrRepr addrRepr;

	public CachePanelShowAddr(CacheModel model, String label, int width, int height) {
		super(model, label, width, height);
		addrRepr = new AddrRepr(2 , "Address repr");
		northPanel.add(addrRepr , BorderLayout.NORTH);
	}

	
	public void setAddrRepr(String entryBinaryRepr, String offsetBinaryRepr){
		addrRepr.setText(0 ,entryBinaryRepr);
		addrRepr.setText(1 ,offsetBinaryRepr);
	}
	
	
	
	
}
