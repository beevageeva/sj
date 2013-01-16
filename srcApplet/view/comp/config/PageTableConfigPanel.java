package view.comp.config;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;


import config.ConfigHolder;
import config.DirectMappedPageTableConfig;
import config.InverseMappedPageTableCfg;

import u.DefTextField;
import u.Graph;
import view.model.Errors;

public class PageTableConfigPanel extends ConfigPanel {

	private static final long serialVersionUID = 1L;

	private CacheConfigPanel tlbConfigPanel;

	private RadioGroupPanel directMappedRG;

	private DirectConfigPanel direct;

	private InverseConfigPanel inverse;

	private JPanel mappingPanel;

	
	public void updateFieldsFromConfiguration(){
		tlbConfigPanel.updateFieldsFromConfiguration();
		directMappedRG.setSelectedValue((short) (ConfigHolder.pageTableCfg.isDirectMapped() ? 0 : 1));
		if(ConfigHolder.pageTableCfg.isDirectMapped()){
			direct.updateFieldsFromConfiguration();
			mappingPanel.remove(inverse);
			mappingPanel.add(direct);

		}
		else{
			if(ConfigHolder.pageTableCfg.getAddCfg()!=null){
				inverse.updateFieldsFromConfiguration();
			}
			mappingPanel.remove(direct);
			mappingPanel.add(inverse);
		}
	}
	
	public PageTableConfigPanel() {
		super();
		setLayout(new BorderLayout());
		tlbConfigPanel = new CacheConfigPanel(ConfigHolder.pageTableCfg.getTlbConfig() , "TLB", new boolean[] { true,
				false, true, false, true , false }, new String[] { "number entries",
				"block size" } );
		add(tlbConfigPanel, BorderLayout.NORTH);
		directMappedRG = new RadioGroupPanel("mapping type", new String[] {
				"direct", "inverse" }, new short[] { 0, 1 }, 0);
		mappingPanel = new JPanel(new GridLayout(2, 1));
		mappingPanel.add(directMappedRG);
		direct = new DirectConfigPanel();
		inverse = new InverseConfigPanel();
		add(mappingPanel);
		directMappedRG.addActionListener(0, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				mappingPanel.remove(inverse);
				mappingPanel.add(direct);
				mappingPanel.revalidate();
				mappingPanel.repaint();
				updateUI();
			}
		});
		directMappedRG.addActionListener(1, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				mappingPanel.remove(direct);
				mappingPanel.add(inverse);
				mappingPanel.revalidate();
				mappingPanel.repaint();
				updateUI();
			}
		});
		setVisible(true);
	}

	public void saveFields() {
		tlbConfigPanel.saveFields();
		if (directMappedRG.getValue() == 0) {
			// direct
			direct.saveFields();
		} else {
			// inverse
			inverse.saveFields();
		}
	}

	class DirectConfigPanel extends ConfigPanel {
		private static final long serialVersionUID = 1L;

		private DefTextField numberLevelsTF;

		private JPanel bottomPanel;

		private JPanel lengthsPanel;

		private JButton cfgButton;

		private JLabel lengthsLabel;

		private RadioGroupPanel searchMethod;

		private DefTextField[] lengthOffsets;


		public void updateFieldsFromConfiguration(){
			DirectMappedPageTableConfig dptCfg = (DirectMappedPageTableConfig) ConfigHolder.pageTableCfg.getAddCfg();
			searchMethod.setSelectedValue((short) (dptCfg.isSearchMethodTopDown() ? 0 : 1));
			if(dptCfg.getOffsetsLength()!=null){
				numberLevelsTF.setText(String
						.valueOf(dptCfg.getOffsetsLength().length));
				lengthOffsets = new DefTextField[dptCfg.getOffsetsLength().length];
				for (int i = 0; i < lengthOffsets.length; i++) {
					lengthOffsets[i] = new DefTextField(2);
					lengthOffsets[i].setText(String.valueOf(dptCfg
							.getOffsetsLength()[i]));
				}
				updateLabelText();

			}
			
			
		}
		
		public DirectConfigPanel() {
			super();
			setLayout(new GridLayout(1, 2));
			setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

			JPanel leftPanel = new JPanel(new GridLayout(3, 1));

			leftPanel.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));
			JPanel upperPanel = new JPanel();
			upperPanel.add(new JLabel("number of levels"));
			numberLevelsTF = new DefTextField(2);
			upperPanel.add(numberLevelsTF);
			cfgButton = new JButton("config");
			cfgButton.addActionListener(new CfgActionListener());
			upperPanel.add(cfgButton);
			leftPanel.add(upperPanel);

			JPanel lp = new JPanel();
			lengthsLabel = new JLabel();
			lp.add(lengthsLabel);
			leftPanel.add(lp);

			bottomPanel = new JPanel();
			lengthsPanel = new JPanel();
			lengthsPanel.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));
			JButton okButton = new JButton("ok");
			okButton.addActionListener(new OkActionListener());
			bottomPanel.add(lengthsPanel);
			bottomPanel.add(okButton);
			bottomPanel.setVisible(false);
			leftPanel.add(bottomPanel);
			add(leftPanel);
			searchMethod = new RadioGroupPanel("search method", new String[] {
					"top-down", "bottom-up" }, new short[] { 0, 1 },
					0);
			add(searchMethod);

			setVisible(true);
		}

		private void updateLabelText() {
			StringBuffer sb = new StringBuffer("lengths :");
			int[] lengths = getNumberLevels();
			for (int i = 0; i < lengths.length; i++) {
				sb.append(" ");
				sb.append(lengths[i]);
			}

			lengthsLabel.setText(sb.toString());
		}

		public boolean isSearchMethodTopDown() {
			return searchMethod.getValue() == 0;
		}

		public int[] getDefNumberLevels() {
			int nLevels = Integer.parseInt(numberLevelsTF.getText());
			if(nLevels==0){
				//TODO another method to force nLevels>0
				nLevels=1;
				numberLevelsTF.setText("1");
				JOptionPane.showMessageDialog(this , "The number of levels must be greater than 0");
			}
			int[] res = new int[nLevels];
			int vpnNBits = ConfigHolder.generalCfg.getVirtualAddrNBits()
					- ConfigHolder.getPageSizeNBits();
			int defLength = nLevels==0?0:vpnNBits / nLevels;
			res[0] = vpnNBits - (nLevels - 1) * defLength;
			for (int i = 1; i < nLevels; i++) {
				res[i] = defLength;
			}
			return res;
		}

		private int[] getNumberLevels() {
			if (lengthOffsets == null) {
				return getDefNumberLevels();
			}
			int[] res = new int[lengthOffsets.length];
			for (int i = 0; i < lengthOffsets.length; i++) {
				res[i] = Integer.parseInt(lengthOffsets[i].getText());
			}
			return res;
		}

		public void saveFields() {
			if (! (ConfigHolder.pageTableCfg.getAddCfg() instanceof DirectMappedPageTableConfig)) {
				ConfigHolder.pageTableCfg.setAddCfg(new DirectMappedPageTableConfig());
			}
			DirectMappedPageTableConfig dptCfg = (DirectMappedPageTableConfig) ConfigHolder.pageTableCfg.getAddCfg();
			dptCfg.setOffsetsLength(direct.getNumberLevels());
			dptCfg.setSearchMethodTopDown(direct.isSearchMethodTopDown());
			ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].setNumberSetsNBits(0);
		}

		class CfgActionListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				lengthsPanel.removeAll();
				int[] defValues = getDefNumberLevels();
				lengthOffsets = new DefTextField[defValues.length];
				for (int i = 0; i < lengthOffsets.length; i++) {
					lengthOffsets[i] = new DefTextField( 2);
					lengthOffsets[i].setText(String
							.valueOf(defValues[i]));
					lengthsPanel.add(lengthOffsets[i]);
				}
				updateLabelText();
				bottomPanel.setVisible(true);
				cfgButton.setEnabled(false);
				numberLevelsTF.setEnabled(false);
			}

		}

		class OkActionListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				updateLabelText();
				bottomPanel.setVisible(false);
				cfgButton.setEnabled(true);
				numberLevelsTF.setEnabled(true);
			}

		}

		@Override
		public void setFieldsEnabled(boolean enable) {
			numberLevelsTF.setEnabled(enable);
			cfgButton.setEnabled(enable);
			searchMethod.setEnabled(enable);
			
		}

		@Override
		public void validateFields(Errors err) {
			numberLevelsTF.validateField("number levels " , err , 1 , ConfigHolder.generalCfg.getVirtualAddrNBits() -ConfigHolder.getPageSizeNBits());
			if(lengthOffsets == null){
				err.addError("offsets must be defined");
				return;
			}
			int sum = 0;
			try{
				for(int i =0 ; i<lengthOffsets.length; i++){
					sum +=Integer.parseInt(lengthOffsets[i].getText());
					
				}
			}
			catch(NumberFormatException e){
				err.addError("lengths must be numbers");
			}
			if(sum!=ConfigHolder.generalCfg.getVirtualAddrNBits() - ConfigHolder.getPageSizeNBits()){
				err.addError("sum of lengths must be eq to number of virtual pages");
			}
		}
	}

	class InverseConfigPanel extends ConfigPanel {

		private static final long serialVersionUID = 1L;
		private DefTextField hashAnchorSizeNBTF;


		public void updateFieldsFromConfiguration(){
			InverseMappedPageTableCfg iCfg = (InverseMappedPageTableCfg) ConfigHolder.pageTableCfg
			.getAddCfg();
			hashAnchorSizeNBTF.setText(String.valueOf(iCfg
					.getHashAnchorSizeNBits()));

		}
		
		public InverseConfigPanel() {
			super();
			setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			hashAnchorSizeNBTF = new DefTextField(2);
			add(Graph.createPanel(hashAnchorSizeNBTF,
					"hash anchor number entries : 2**", null));
			setVisible(true);
		}

		public int getHashAnchorSizeNBits() {
			return Integer.parseInt(hashAnchorSizeNBTF.getText());
		}

		public void saveFields() {
			if (! (ConfigHolder.pageTableCfg.getAddCfg() instanceof InverseMappedPageTableCfg)) {
				ConfigHolder.pageTableCfg.setAddCfg(new InverseMappedPageTableCfg());
			}
			InverseMappedPageTableCfg iCfg = (InverseMappedPageTableCfg) ConfigHolder.pageTableCfg.getAddCfg();
			iCfg.setHashAnchorSizeNBits(Integer.parseInt(hashAnchorSizeNBTF
					.getText()));
			ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].setNumberSetsNBits(ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].getNumberEntriesNBits());
		}

		@Override
		public void setFieldsEnabled(boolean enable) {
			hashAnchorSizeNBTF.setEnabled(enable);
		}

		@Override
		public void validateFields(Errors err) {
			hashAnchorSizeNBTF.validateField("hash anchor zize(p2)" , err , ConfigHolder.cacheCfgs[ConfigHolder.numberCaches].getNumberEntriesNBits() , ConfigHolder.generalCfg.getVirtualAddrNBits() - ConfigHolder.getPageSizeNBits()+ ConfigHolder.generalCfg.getNumberProcessesNBits());
		}

	}

	@Override
	public void setFieldsEnabled(boolean enable) {
		tlbConfigPanel.setFieldsEnabled(enable);
		directMappedRG.setEnabled(enable);
		if(ConfigHolder.pageTableCfg.isDirectMapped()){
			direct.setFieldsEnabled(enable);
		}
		else{
			inverse.setFieldsEnabled(enable);
		}
		
	}

	@Override
	public void validateFields(Errors err) {
		if(tlbConfigPanel.mustValidateFields()){
			tlbConfigPanel.validateFields(err);
		}
		if (directMappedRG.getValue() == 0) {
			direct.validateFields(err);
		}
		else{
			inverse.validateFields(err);
		}

	}


}
