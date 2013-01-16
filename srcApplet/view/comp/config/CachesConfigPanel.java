package view.comp.config;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import config.ConfigHolder;

import u.DefTextField;
import u.Graph;
import view.model.Errors;

public class CachesConfigPanel extends ConfigPanel {

	private static final long serialVersionUID = 8048549049293533332L;
	private CacheConfigPanel[] cacheConfigPanels;

	public void updateFieldsFromConfiguration() {
		for (int i = 0; i < cacheConfigPanels.length; i++) {
			cacheConfigPanels[i].updateFieldsFromConfiguration();
		}
	}

	public CachesConfigPanel() {
		super();
		setSize(800, 600);
		setLayout(new BorderLayout());
		JPanel cachesPanel = new JPanel(new GridLayout(1, 3));
		cachesPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));
		cacheConfigPanels = new CacheConfigPanel[ConfigHolder.numberCaches];
		cacheConfigPanels[0] = new CacheConfigPanel(ConfigHolder.cacheCfgs[0],
				"cache L1",
				new boolean[] { true, true, true, true, true, true },
				new String[] { "number entries", "block size" });
		for (int i = 1; i < ConfigHolder.numberCaches; i++) {
			cacheConfigPanels[i] = new CacheConfigPanel(
					ConfigHolder.cacheCfgs[i], "cache L"
							+ String.valueOf(i + 1), new boolean[] { true,
							true, true, true, false, true }, new String[] {
							"number entries", "block size" });
		}
		for (int i = 0; i < cacheConfigPanels.length; i++) {
			cachesPanel.add(cacheConfigPanels[i]);
		}
		add(cachesPanel, BorderLayout.CENTER);
		setVisible(true);
		repaint();
		invalidate();
		setSize(800, 600);

	}

	public void saveFields() {
		for (int i = 0; i < ConfigHolder.numberCaches; i++) {
			cacheConfigPanels[i].saveFields();
		}
	}

	@Override
	public void setFieldsEnabled(boolean enable) {
		for (int i = 0; i < cacheConfigPanels.length; i++) {
			cacheConfigPanels[i].setFieldsEnabled(enable);
		}

	}

	private static CacheConfigPanel[] getEnabledCacheConfigPanels(
			CacheConfigPanel[] ccfgPanels) {
		int n = 0;
		for (int i = 0; i < ccfgPanels.length; i++) {
			if (ccfgPanels[i].mustValidateFields()) {
				n++;
			}
		}
		CacheConfigPanel[] res = new CacheConfigPanel[n];
		n = 0;
		for (int i = 0; i < ccfgPanels.length; i++) {
			if (ccfgPanels[i].mustValidateFields()) {
				res[n] = ccfgPanels[i];
				n++;
			}
		}
		return res;
	}

	@Override
	public void validateFields(Errors err) {
		CacheConfigPanel[] enabledCacheConfigPanels = getEnabledCacheConfigPanels(cacheConfigPanels);
		for (int i = 0; i < enabledCacheConfigPanels.length; i++) {
			enabledCacheConfigPanels[i].validateFields(err);
		}
		if (!err.isEmpty()) {
			return;
		}
		Comparator<CacheConfigPanel> comp = new CacheConfigPanel.BlockSizeComparator();
		for (int i = 0; i < enabledCacheConfigPanels.length - 1; i++) {
			if(comp.compare(enabledCacheConfigPanels[i], enabledCacheConfigPanels[i+1])<0){
				err.addError("check block size(p2) of "+String.valueOf(i+1) + " relative to bock size(p2) of next enabled ");
				return;
			}
			
			}
		if(enabledCacheConfigPanels.length > 0){
			enabledCacheConfigPanels[enabledCacheConfigPanels.length - 1].blockSizeTF
				.validateField("block size(p2) ", err, 0, ConfigHolder
						.getPageSizeNBits());
		}
	}
}
