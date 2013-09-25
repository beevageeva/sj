package view.comp.run;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import view.comp.config.CachesConfigPanel;
import view.comp.config.ConfigPanel;
import view.comp.config.GeneralConfigPanel;
import view.comp.config.PageTableConfigPanel;
import view.model.Errors;
import config.ConfigHolder;
import config.DirectMappedPageTableConfig;
import config.InverseMappedPageTableCfg;


public class Configuration extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private ConfigPanel[] panels;
	private int currentIndex = -1;
	
	private JButton nextButton;
	private JButton prevButton;
	public JButton okButton;
	
	private App app;
	
	private static int numberConfigPanels = 3;
	
	
	private boolean panelsEnabled = true ;
	
	public Configuration(App app){
		super();
		this.app = app;
		setLayout(new BorderLayout());
		setSize(new Dimension(800,600));
		panels= new ConfigPanel[numberConfigPanels];
		panels[0] = new GeneralConfigPanel();
		panels[1] = new PageTableConfigPanel();
		panels[2] = new CachesConfigPanel();
		for(int i = 0 ; i<numberConfigPanels;i++){
			panels[i].updateFieldsFromConfiguration();
		}
		nextButton = new JButton("next");
		nextButton.addActionListener(new ModifyIndexAL(true));
		prevButton = new JButton("prev");
		prevButton.addActionListener(new ModifyIndexAL(false));
		JPanel buttonsPanel = new JPanel();
		if(app.configFileName==null){
			okButton = new JButton("ok");
			okButton.setEnabled(false);
			okButton.addActionListener(this);
			buttonsPanel.add(okButton);
		}
		buttonsPanel.add(prevButton);
		buttonsPanel.add(nextButton);
		setCurrentIndex(0 , false);
		add(buttonsPanel, BorderLayout.NORTH);
		setVisible(true);
	}
	
	
	
	private void setCurrentIndex(int newIndex , boolean saveCurrentPanel){
		boolean setIndex = true;
		if(currentIndex!=-1 && saveCurrentPanel && panelsEnabled){
			setIndex = panels[currentIndex].exitPanel();
		}
		if(!setIndex){
			return;
		}
		if(currentIndex !=-1){
			remove(panels[currentIndex]);
		}
		if(currentIndex==numberConfigPanels-1){
			nextButton.setEnabled(true);
		}
		if(currentIndex == 0){
			prevButton.setEnabled(true);
		}
		if(newIndex == numberConfigPanels-1){
			nextButton.setEnabled(false);
			//P0 ->P1
			//it doesn check for the first pass , it will always set it enabled
			//if receives params ok button does't appear
			if(!app.reconfigItem.isEnabled() && app.configFileName==null){
				okButton.setEnabled(true);
			}
		}
		if(newIndex == 0){
			prevButton.setEnabled(false);
		}
		add(panels[newIndex],BorderLayout.CENTER);
		
		currentIndex = newIndex;
		updateUI();
	}
	class ModifyIndexAL implements ActionListener{
		
		private boolean next;
		
		public ModifyIndexAL(boolean  next){
			this.next = next;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			if(next){
				setCurrentIndex(currentIndex + 1 , true);
			}
			else{
				setCurrentIndex(currentIndex - 1 , true);
			}
		}
		
	}
	
	public void setPanelsEnabled(boolean enable){
		this.panelsEnabled = enable;
		for(int i = 0 ; i<numberConfigPanels ; i++){
			panels[i].setFieldsEnabled(enable);
		}
		
	}
	
	
	//phase 1 -> phase 2
	public void actionPerformed(ActionEvent e) {
		if(panels[currentIndex].exitPanel() && glValidate()){
			okButton.setEnabled(false);
			setPanelsEnabled(false);
			app.startItem.setEnabled(true);
			app.loadLocalConfigItem.setEnabled(false);
			app.loadServerConfigItem.setEnabled(false);
			app.reconfigItem.setEnabled(true);
			app.saveconfigItem.setEnabled(true);
		}
	}
	
	public boolean glValidate(){
		Errors err = new Errors();
		int nVirtpagesNBits = ConfigHolder.generalCfg.getVirtualAddrNBits() - ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].getBlockSizeNBits()[0] ;
		if(ConfigHolder.pageTableCfg.isDirectMapped()){
			DirectMappedPageTableConfig dptCfg = (DirectMappedPageTableConfig) ConfigHolder.pageTableCfg.getAddCfg();
			if(dptCfg.getOffsetsLength().length > nVirtpagesNBits){
				err.addError("number of levels must be le than number of bits to repr a virt page ");
			}
			int s = 0;
				for(int i =0 ; i<dptCfg.getOffsetsLength().length ; i++){
					s+=dptCfg.getOffsetsLength()[i];
				}
				if(s!=nVirtpagesNBits){
					err.addError("sum of offsets length must be eq to number of bits to repr a virt page");
				}
				
			}
		else{
			InverseMappedPageTableCfg icfg = (InverseMappedPageTableCfg) ConfigHolder.pageTableCfg.getAddCfg();
			if(ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].getNumberEntriesNBits()>icfg.getHashAnchorSizeNBits() || icfg.getHashAnchorSizeNBits()>nVirtpagesNBits+ ConfigHolder.generalCfg.getNumberProcessesNBits()){
				err.addError("Hash anchor size must be le than number of virt pages and ge than number of phys pages");
			}
		}
		
		if(ConfigHolder.getPageSizeNBits()<ConfigHolder.cacheCfgs[ConfigHolder.numberCaches-1].getBlockSizeNBits()[0] || (ConfigHolder.cacheCfgs[ConfigHolder.numberCaches-1].getBlockSizeNBits().length==2) && ConfigHolder.getPageSizeNBits()<ConfigHolder.cacheCfgs[ConfigHolder.numberCaches-1].getBlockSizeNBits()[1]){
			err.addError("main mem page size must be ge than cache block size");
		}
		
		if(ConfigHolder.generalCfg.getNumberProcessesNBits()>ConfigHolder.generalCfg.getVirtualAddrNBits() - ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].getBlockSizeNBits()[0]){
			err.addError("number of processes(p2) must be le than virt addr nbits - offset n bits");
		}
		if(err.isEmpty()){
			return true;
		}
		else{
			JOptionPane.showMessageDialog(this,  err.displayErrors(),"errors", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
	}
	
	
	public void configureFromFile(){
		for(int i = 0 ; i<numberConfigPanels;i++){
			panels[i].updateFieldsFromConfiguration();
		}
		if(app.configFileName==null){
			okButton.setEnabled(false);
		}
		else{
			//put here the disabled panel as updateFieldsFromConfiguration makes the panel that have
			//enable button enabled if enabled can be true
			setPanelsEnabled(false);
		}
		setCurrentIndex(0 , false);

	}
	
	
}
