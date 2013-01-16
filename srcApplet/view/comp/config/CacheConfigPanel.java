package view.comp.config;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import u.DefTextField;
import u.Graph;
import view.model.Errors;
import config.CacheConfig;

public class CacheConfigPanel extends ConfigPanel {

	private static final long serialVersionUID = 1L;

	boolean[] display;

	RadioGroupPanel enabled;// 0 , "enabled"

	DefTextField numberEntriesTF;// 1 -

	DefTextField blockSizeTF;// 0 , -

	DefTextField blockSizeTFAd;// 0 , -

	DefTextField numberSetsTF;// 0 "number Sets"

	DefTextField busSizeTF;// 0 "bus Size"

	RadioGroupPanel evictionPolicyRG;// 1 , eviction policy

	RadioGroupPanel dataInstrSeparatedRG;// 0 , data and instructions

	// separated

	DefTextField accessTimeTF;

	private RadioGroupPanel missWritePolicy;

	private RadioGroupPanel hitWritePolicy;

	private CacheConfig cfg;

	/**
	 * @param label
	 * @param display :
	 *            boolean [6] : 0 if enabled dislayed , 1 if
	 *            blockSizeTFdisplayed , 2 if numbersets displayed ,3 if bus
	 *            size displayed,4 if datainstrSepRG displayed , 5 if
	 *            hitWritePolicy displayed
	 * @param labels :
	 *            String[2] : 0 numberEntries , 1 block size
	 * @param cfg
	 */
	public CacheConfigPanel(CacheConfig cfg, String label,
			final boolean[] display, final String[] labels) {
		super();
		this.cfg = cfg;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		this.display = display;
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));
		labelPanel.add(new JLabel(label));
		if (display[0]) {
			ActionListener alD = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setiFieldsEnabled(false);
					numberEntriesTF.setText("-1");
				}
			};
			ActionListener alE = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setiFieldsEnabled(true);
				}
			};
			enabled = new RadioGroupPanel("enabled",
					new String[] { "yes", "no" }, new short[] { 0, 1 }, 0, true);
			enabled.addActionListener(0, alE);
			enabled.addActionListener(1, alD);
			labelPanel.add(enabled, BorderLayout.SOUTH);
		}

		add(labelPanel, BorderLayout.NORTH);

		JPanel cPanel = new JPanel(new GridLayout(1, 2));
		cPanel
				.setBorder(BorderFactory
						.createEtchedBorder(EtchedBorder.LOWERED));

		int n = 0;
		for (int i = 1; i < display.length; i++) {
			if (display[i]) {
				n++;
			}
		}

		JPanel ctfPanel = new JPanel(new GridLayout(2 + n, 1));

		numberEntriesTF = new DefTextField(2);
		ctfPanel.add(Graph.createPanel(numberEntriesTF, labels[0] + " : 2**",
				null));

		if (display[4]) {
			dataInstrSeparatedRG = new RadioGroupPanel("data instr separated",
					new String[] { "yes", "no" }, new short[] { 0, 1 }, 0, true);
			ctfPanel.add(dataInstrSeparatedRG);
		}
		if (display[1]) {
			JPanel blockSizePanel = new JPanel(new BorderLayout());
			blockSizePanel.add(Graph.createPanel(
					blockSizeTF = new DefTextField(2), labels[1] + " : 2**",
					"B"), BorderLayout.NORTH);
			if(display[4]){
			blockSizePanel.add(Graph.createPanel(
					blockSizeTFAd = new DefTextField(2),
					labels[1] + "(I): 2**", "B"), BorderLayout.SOUTH);
			}
			if (display[4]) {
				dataInstrSeparatedRG.addActionListener(0, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						blockSizeTFAd.setEnabled(true);
					}
				});
				dataInstrSeparatedRG.addActionListener(1, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						blockSizeTFAd.setEnabled(false);
					}
				});

			}
			ctfPanel.add(blockSizePanel);
		}

		if (display[2]) {
			numberSetsTF = new DefTextField(2);
			ctfPanel.add(Graph.createPanel(numberSetsTF, "number sets : 2**",
					null));
		}
		if (display[3]) {
			busSizeTF = new DefTextField(2);
			ctfPanel.add(Graph.createPanel(busSizeTF, "bus size : ", null));
		}
		accessTimeTF = new DefTextField(5);
		ctfPanel
				.add(Graph.createPanel(accessTimeTF, "access time units", null));

		evictionPolicyRG = Graph
				.createEvictionPolicyPanel(CacheConfig.RANDOM_POLICY);
		cPanel.add(ctfPanel);
		cPanel.add(evictionPolicyRG);
		add(cPanel, BorderLayout.CENTER);
		JPanel policiesPanel = new JPanel(new GridLayout(1, 2));
		if (display[5]) {
			hitWritePolicy = new RadioGroupPanel("write hit policy",
					new String[] { "write-through", "write-back" },
					new short[] { 0, 1 }, 0);
			missWritePolicy = new RadioGroupPanel("write miss policy",
					new String[] { "write-allocate", "no write-allocate" },
					new short[] { 0, 1 }, 0);
			policiesPanel.add(hitWritePolicy);
			policiesPanel.add(missWritePolicy);
			add(policiesPanel, BorderLayout.SOUTH);
		}
	}

	public void updateFieldsFromConfiguration() {
		if (display[0]) {
			enabled.setSelectedValue((short) (cfg.getNumberEntries() == 0 ? 1
					: 0));
			if (cfg.getNumberEntries() == 0) {
				setiFieldsEnabled(false);
			} else {
				setiFieldsEnabled(true);
			}
		}
		numberEntriesTF.setText(String.valueOf(cfg.getNumberEntriesNBits()));
		if (display[4]) {
			dataInstrSeparatedRG.setSelectedValue((short) (cfg
					.isDataInstrSeparated() ? 0 : 1));
		}
		if (display[1]) {
			blockSizeTF.setText(String.valueOf(cfg.getBlockSizeNBits()[0]));
			if (display[4] && cfg.isDataInstrSeparated()) {
				blockSizeTFAd.setEnabled(true);
				blockSizeTFAd.setText(String
						.valueOf(cfg.getBlockSizeNBits()[1]));
			} else {
				if(display[4])
				blockSizeTFAd.setEnabled(false);
			}
		}
		if (display[2]) {
			numberSetsTF.setText(String.valueOf(cfg.getNumberSetsNBits()));
		}
		if (display[3]) {
			busSizeTF.setText(String.valueOf(cfg.getBusSize()));
		}
		accessTimeTF.setText(String.valueOf(cfg.getAccessTimeUnits()));
		evictionPolicyRG.setSelectedValue(cfg.getEvictionPolicy());
		if (display[5]) {
			missWritePolicy.setSelectedValue((short) (cfg
					.isWriteAllocateMissPolicy() ? 0 : 1));
			hitWritePolicy.setSelectedValue((short) (cfg
					.isWriteThroughHitPolicy() ? 0 : 1));
		}
	}

	private void setiFieldsEnabled(boolean enable) {
		numberEntriesTF.setEnabled(enable);
		if (display[4]) {
			dataInstrSeparatedRG.setEnabled(enable);
		}
		if (display[1]) {
			blockSizeTF.setEnabled(enable);
			if (display[4]) {
				blockSizeTFAd.setEnabled(enable
						&& dataInstrSeparatedRG.getValue() == 0);
			}
		}
		if (display[2]) {
			numberSetsTF.setEnabled(enable);
		}
		if (display[3]) {
			busSizeTF.setEnabled(enable);
		}
		accessTimeTF.setEnabled(enable);
		evictionPolicyRG.setEnabled(enable);
		if (display[5]) {
			hitWritePolicy.setEnabled(enable);
			missWritePolicy.setEnabled(enable);
		}
		repaint();

	}

	@Override
	public void setFieldsEnabled(boolean enable) {
		setiFieldsEnabled(enable
				&& (cfg.getNumberEntries() != 0 || !(display[0])));
		if (display[0]) {
			enabled.setEnabled(enable);
		}
	}

	@Override
	public void saveFields() {
		cfg.setNumberEntriesNBits(Integer.parseInt(numberEntriesTF.getText()));
		if (display[4]) {
			cfg.blockSizeNBits = new int[(dataInstrSeparatedRG.getValue() == 0) ? 2
					: 1];
		} else {
			cfg.blockSizeNBits = new int[1];
		}
		if (display[1]) {
			cfg.blockSizeNBits[0] = Integer.parseInt(blockSizeTF.getText());
			if (display[4] && dataInstrSeparatedRG.getValue() == 0) {
				cfg.blockSizeNBits[1] = Integer.parseInt(blockSizeTFAd
						.getText());
			}
		}

		if (display[2]) {
			cfg.setNumberSetsNBits(Integer.parseInt(numberSetsTF.getText()));
		}
		cfg.setAccessTimeUnits(Integer.parseInt(accessTimeTF.getText()));
		if (display[3]) {
			cfg.setBusSize(Integer.parseInt(busSizeTF.getText()));
		}
		cfg.setEvictionPolicy(evictionPolicyRG.getValue());
		if (display[5]) {
			cfg.setWriteThroughHitPolicy(hitWritePolicy.getValue() == 0);
			cfg.setWriteAllocateMissPolicy(missWritePolicy.getValue() == 0);
		}
	}

	public boolean mustValidateFields() {
		return display[0] && enabled.getValue() == 0 || !display[0];
	}

	@Override
	public void validateFields(Errors err) {
		numberEntriesTF.validateField("number of entries ", err, 1, 32);
		if (display[1]) {
			blockSizeTF.validateField("block size ", err, 1, 20);
			if (display[4] && dataInstrSeparatedRG.getValue() == 0) {
				blockSizeTFAd.validateField("block size(for instr) ", err, 1,
						20);
			}
		}

		if (display[2]) {
			numberSetsTF.validateField("number of sets", err, 0, Integer
					.parseInt(numberEntriesTF.getText()));
		}
		accessTimeTF.validateField("access time units", err, 0, 50000);
		if (display[3]) {
			busSizeTF.validateField("bus size ", err, 8, 128);
		}

	}

	static class BlockSizeComparator implements Comparator<CacheConfigPanel> {

		@Override
		public int compare(CacheConfigPanel ccp1, CacheConfigPanel ccp2) {
			assert (ccp1.blockSizeTF != null && ccp2.blockSizeTF != null);
			int a, b, c, d;
			a = Integer.parseInt(ccp1.blockSizeTF.getText());
			b = Integer.parseInt(ccp2.blockSizeTF.getText());
			if (b - a < 0) {
				return -1;
			}
			if (ccp1.blockSizeTFAd != null) {
				c = Integer.parseInt(ccp1.blockSizeTFAd.getText());
				if (ccp2.blockSizeTFAd != null) {
					d = Integer.parseInt(ccp2.blockSizeTFAd.getText());
					return d - c;
				} else {
					return b - c;
				}
			} else {
				if (ccp2.blockSizeTFAd != null) {
					d = Integer.parseInt(ccp2.blockSizeTFAd.getText());
					return d - a;
				} else {
					return b - a;
				}

			}

		}

	}

}
