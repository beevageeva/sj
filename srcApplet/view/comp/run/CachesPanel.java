package view.comp.run;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import u.Graph;
import view.comp.model.CachesModel;
import view.comp.model.CachesModelListener;

public class CachesPanel extends JPanel implements CachesModelListener{

	private static final long serialVersionUID = 1L;
	private CachePanelShowAddr[] cachePanels;
	private JLabel instrLabel;
	private CachesModel cachesModel;

	
	public CachesPanel(CachesModel cachesModel){
		super(new BorderLayout());
		this.cachesModel = cachesModel;
		cachesModel.addCachesModelListener(this);
		JPanel cPanels = new JPanel(new GridLayout(1 , cachesModel.getLength() ));
		setSize(800,600);
		cachePanels = new CachePanelShowAddr[cachesModel.getLength()];
		for(int i=0;i<cachesModel.getLength() ; i++){
			cachePanels[i] = new CachePanelShowAddr(cachesModel.getCacheModel(i) , i==cachesModel.getCacheIndexes().length-1?"MainMem":("Cache "+(cachesModel.getCacheIndexes()[i]+1)) , 200 , 500);
			cPanels.add(cachePanels[i]);
		}
		add(cPanels , BorderLayout.CENTER);
		instrLabel = new JLabel("instruction");
		add(instrLabel , BorderLayout.SOUTH);
	}
	

	public void resolve(String binaryAddress, short instrType) {
		int indOffset ;
		for(int i = 0 ; i<cachesModel.getLength(); i++){
			indOffset = binaryAddress.length()-cachesModel.getCacheModel(i).getCfg().getBlockSizeNBits(instrType);
			cachePanels[i].setAddrRepr(binaryAddress.substring(0 , indOffset) , binaryAddress.substring(indOffset));
		}
		instrLabel.setText(Graph.getLabelText(instrType));
	}
	

}
