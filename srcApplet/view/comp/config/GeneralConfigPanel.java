package view.comp.config;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import u.DefTextField;
import u.Graph;
import view.model.Errors;
import config.ConfigHolder;



public class GeneralConfigPanel extends ConfigPanel{

	
	private static final long serialVersionUID = 1L;
	private DefTextField vmSize;
	private DefTextField npSize;
	private DefTextField diskAccessTimeTF;;
	private PageAgingPanel pageAgingPanel;
	private MainMemoryAllocPanel memAllocPanel;
	private CacheConfigPanel mainMemoryPanel;
	
	
	public GeneralConfigPanel(){
		super();
		vmSize = new DefTextField(2);
		npSize = new DefTextField(2);
		diskAccessTimeTF = new DefTextField(6);
		pageAgingPanel = new PageAgingPanel();
		memAllocPanel = new MainMemoryAllocPanel();
		setSize(600,400);
		setLayout(new BorderLayout());
		JPanel textPanel = new JPanel(new GridLayout(1 , 3));
		textPanel.add(Graph.createPanel(vmSize,"virtual memory size : 2**","B"));
		textPanel.add(Graph.createPanel(npSize,"max number processes : 2**",null));
		textPanel.add(Graph.createPanel(diskAccessTimeTF,"disk access TU :",null));
		add(textPanel , BorderLayout.NORTH);
		JPanel memPolicy = new JPanel(new GridLayout(1 ,4));
		memPolicy.add(pageAgingPanel);
		memPolicy.add(memAllocPanel);
		add(memPolicy , BorderLayout.CENTER);
		mainMemoryPanel = new CacheConfigPanel(ConfigHolder.cacheCfgs[ConfigHolder.numberCaches] , "Main Memory" ,new boolean[] {false , true, false , true , false , false} , new String[] {"number pages" , "page size"} );
		add(mainMemoryPanel , BorderLayout.SOUTH);

	}
	
	public void updateFieldsFromConfiguration() {
		vmSize.setText(String.valueOf(ConfigHolder.generalCfg.getVirtualAddrNBits()));
		npSize.setText(String.valueOf(ConfigHolder.generalCfg.getNumberProcessesNBits()));
		mainMemoryPanel.updateFieldsFromConfiguration();
		diskAccessTimeTF.setText(String.valueOf(ConfigHolder.generalCfg.getDiskAccessTime()));
		pageAgingPanel.updateFieldsFromConfiguration();
		memAllocPanel.updateFieldsFromConfiguration();
	}





	public void saveFields(){
		mainMemoryPanel.saveFields();
		ConfigHolder.generalCfg.setVirtualAddrNBits(Integer.parseInt(vmSize.getText()));
		ConfigHolder.generalCfg.setNumberProcessesNBits(Integer.parseInt(npSize.getText()));
		ConfigHolder.generalCfg.setDiskAccessTime(Integer.parseInt(diskAccessTimeTF.getText()));
		pageAgingPanel.saveFields();
		memAllocPanel.saveFields();
	}

	@Override
	public void setFieldsEnabled(boolean enable) {
		vmSize.setEnabled(enable);
		npSize.setEnabled(enable);
		mainMemoryPanel.setFieldsEnabled(enable);
		diskAccessTimeTF.setEnabled(enable);
		pageAgingPanel.setFieldsEnabled(enable);
		memAllocPanel.setFieldsEnabled(enable);
		
	}

	@Override
	public void validateFields(Errors err) {
		mainMemoryPanel.validateFields(err);
		npSize.validateField("max number of processes(p2)" ,err ,0 ,8);
		vmSize.validateField("virtual memory size " , err ,Integer.parseInt(mainMemoryPanel.numberEntriesTF.getText()) - Integer.parseInt(npSize.getText()) + Integer.parseInt(mainMemoryPanel.blockSizeTF.getText()) , 32);
		diskAccessTimeTF.validateField("disk access time units" , err , 0 ,500000);
		pageAgingPanel.validateFields(err);
		memAllocPanel.validateFields(err);
	
	}
	
	class PageAgingPanel extends ConfigPanel{
		private static final long serialVersionUID = 1L;
		private DefTextField pageAgingIncTF;
		private DefTextField memRefToBeRunTF;
		private RadioGroupPanel enabled;
		
		
		public PageAgingPanel(){
			super();
			setLayout(new BorderLayout());
			add(new JLabel("page aging") , BorderLayout.NORTH);
			pageAgingIncTF = new DefTextField(2);
			memRefToBeRunTF = new DefTextField(2);
			enabled = new RadioGroupPanel("enabled",
					new String[] { "yes", "no" }, new short[] { 0, 1 }, 0 ,true);
			enabled.addActionListener(0, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pageAgingIncTF.setEnabled(true);
					memRefToBeRunTF.setEnabled(true);
				}
			});
			enabled.addActionListener(1, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pageAgingIncTF.setEnabled(false);
					memRefToBeRunTF.setText("-1");
					memRefToBeRunTF.setEnabled(false);
				}
			});
			
			JPanel compPanel = new JPanel(new GridLayout(3 , 1)); 
			compPanel.add(enabled);
			compPanel.add(Graph.createPanel(pageAgingIncTF , "ref inc units" , null));
			compPanel.add(Graph.createPanel(memRefToBeRunTF , "will run after" , "mem ref"));
			add(compPanel,  BorderLayout.CENTER);
		}
		@Override
		protected void saveFields() {
			ConfigHolder.generalCfg.pageAgingConfig.setPageAgingIncrease(Integer.parseInt(pageAgingIncTF.getText()));
			ConfigHolder.generalCfg.pageAgingConfig.setMemRefToBeRun(Integer.parseInt(memRefToBeRunTF.getText()));
			
		}

		@Override
		public void setFieldsEnabled(boolean enable) {
			enabled.setEnabled(enable);
			pageAgingIncTF.setEnabled(enable && ConfigHolder.generalCfg.pageAgingConfig.isEnabled());
			memRefToBeRunTF.setEnabled(enable && ConfigHolder.generalCfg.pageAgingConfig.isEnabled());
		}

		@Override
		protected void validateFields(Errors err) {
			if(enabled.getValue()==0){
				pageAgingIncTF.validateField("page aging ref inc units" , err , 1 , 20);
				memRefToBeRunTF.validateField("mem ref" , err , 1 , 50);
				
			}
		}

		@Override
		public void updateFieldsFromConfiguration() {
			enabled.setSelectedValue((short) (ConfigHolder.generalCfg.pageAgingConfig.isEnabled() ? 0 : 1));
			pageAgingIncTF.setText(String.valueOf(ConfigHolder.generalCfg.pageAgingConfig.getPageAgingIncrease()));
			memRefToBeRunTF.setText(String.valueOf(ConfigHolder.generalCfg.pageAgingConfig.getMemRefToBeRun()));
			pageAgingIncTF.setEnabled(ConfigHolder.generalCfg.pageAgingConfig.isEnabled());
			memRefToBeRunTF.setEnabled(ConfigHolder.generalCfg.pageAgingConfig.isEnabled());
		}
		
	}

	
	class MainMemoryAllocPanel extends ConfigPanel{
		private static final long serialVersionUID = 1L;
		private DefTextField minPFFTF;
		private DefTextField maxPFFTF;
		private DefTextField evNodesToBeRunTF;
		
		private RadioGroupPanel enabled;
		
		
		public MainMemoryAllocPanel(){
			super();
			setLayout(new BorderLayout());
			add(new JLabel("mem alloc") , BorderLayout.NORTH);
			minPFFTF = new DefTextField(2);
			maxPFFTF = new DefTextField(2);
			evNodesToBeRunTF = new DefTextField(2);
			enabled = new RadioGroupPanel("alloc policy",
					new String[] { "local", "global" }, new short[] { 0, 1 }, 0 ,true);
			enabled.addActionListener(0, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					minPFFTF.setEnabled(true);
					maxPFFTF.setEnabled(true);
					evNodesToBeRunTF.setEnabled(true);
				}
			});
			enabled.addActionListener(1, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evNodesToBeRunTF.setEnabled(false);
					evNodesToBeRunTF.setText("-1");
					minPFFTF.setEnabled(false);
					maxPFFTF.setEnabled(false);
				}
			});
			
			JPanel compPanel = new JPanel(new GridLayout(4 , 1)); 
			compPanel.add(enabled);
			compPanel.add(Graph.createPanel(minPFFTF , "min PFF" , null));
			compPanel.add(Graph.createPanel(maxPFFTF , "max PFF" , null));
			compPanel.add(Graph.createPanel(evNodesToBeRunTF , "will run after" , "ev nodes"));
			add(compPanel,  BorderLayout.CENTER);
		}
		@Override
		protected void saveFields() {
			ConfigHolder.generalCfg.memAllocConfig.setMinPFF(Integer.parseInt(minPFFTF.getText()));
			ConfigHolder.generalCfg.memAllocConfig.setMaxPFF(Integer.parseInt(maxPFFTF.getText()));
			ConfigHolder.generalCfg.memAllocConfig.setNEvictedNodesToRun(Integer.parseInt(evNodesToBeRunTF.getText()));
			
		}

		@Override
		public void setFieldsEnabled(boolean enable) {
			enabled.setEnabled(enable);
			
			minPFFTF.setEnabled(enable && ConfigHolder.generalCfg.memAllocConfig.isEnabled());
			maxPFFTF.setEnabled(enable && ConfigHolder.generalCfg.memAllocConfig.isEnabled());
			evNodesToBeRunTF.setEnabled(enable && ConfigHolder.generalCfg.memAllocConfig.isEnabled());
		}

		@Override
		protected void validateFields(Errors err) {
			if(enabled.getValue()==0){
				if(minPFFTF.validateField("min PFF" , err , 1 , 50)){
					maxPFFTF.validateField("max PFF" , err , Integer.parseInt(minPFFTF.getText()) , 50);
				}
				evNodesToBeRunTF.validateField("n ev nodes" , err , 1 , 20);
			}
		}

		@Override
		public void updateFieldsFromConfiguration() {
			enabled.setSelectedValue((short) (ConfigHolder.generalCfg.memAllocConfig.isEnabled() ? 0 : 1));
			minPFFTF.setText(String.valueOf(ConfigHolder.generalCfg.memAllocConfig.getMinPFF()));
			maxPFFTF.setText(String.valueOf(ConfigHolder.generalCfg.memAllocConfig.getMaxPFF()));
			evNodesToBeRunTF.setText(String.valueOf(ConfigHolder.generalCfg.memAllocConfig.getNEvictedNodesToRun()));
			minPFFTF.setEnabled(ConfigHolder.generalCfg.memAllocConfig.isEnabled());
			maxPFFTF.setEnabled(ConfigHolder.generalCfg.memAllocConfig.isEnabled());
			evNodesToBeRunTF.setEnabled(ConfigHolder.generalCfg.memAllocConfig.isEnabled());
		}
		
	}

	
}
