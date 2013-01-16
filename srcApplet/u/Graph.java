package u;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import config.CacheConfig;
import view.comp.config.RadioGroupPanel;
import view.model.CacheEntry;




public class Graph {

	public static JPanel createPanel(JTable table, int width , int height , Component title){
		JPanel panel = new JPanel(new BorderLayout());
		JScrollPane sp = new JScrollPane(table);
		sp.setPreferredSize(new Dimension(width,height));
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		panel.add(sp, BorderLayout.CENTER);
		if(title!=null){
			panel.add(title, BorderLayout.NORTH);
		}
		return panel;
	}
	
	public static LabelPanel createPanel(JTable table, int width , int height , String title){
		return new LabelPanel(table, width, height, title);
	}	
	
	
	
	
	public static JPanel createPanel(JTextField tf, String label1 , String label2){
		JPanel panel = new JPanel();
				panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		panel.add(new JLabel(label1));
		panel.add(tf);
		if(label2!=null){
			panel.add(new JLabel(label2));
		}
		return panel;
	}
	
	public static RadioGroupPanel createEvictionPolicyPanel(int selValue){
		return createEvictionPolicyPanel(selValue , false);
	}
	
	
	public static RadioGroupPanel createEvictionPolicyPanel(int selValue , boolean isHorizontal){
		String[] labels = {"random" , "FIFO" , "LRU" , "LFU" , "NRU" , "NFU" , "OPT" , "MRU"};
		short[] values = {CacheConfig.RANDOM_POLICY , CacheConfig.FIFO_POLICY ,CacheConfig.LRU_POLICY ,CacheConfig.LFU_POLICY ,CacheConfig.NRU_POLICY ,CacheConfig.NFU_POLICY ,CacheConfig.OPT_POLICY,  CacheConfig.MRU_POLICY};
		return new RadioGroupPanel("eviction policy",labels, values , selValue,isHorizontal);
	}
	
	
	public static String getLabelText(short instrType){
		StringBuffer sb = new StringBuffer();
		switch(instrType){
		case CacheEntry.FETCHINSTR:
			sb.append("FETCH");
			break;
		case CacheEntry.MODIFDATA:
			sb.append("MEMWRITE");
			break;
		case CacheEntry.READDATA:
			sb.append("MEMREAD");
			break;
		}
		return sb.toString();
	}
	
	public  static class LabelPanel extends JPanel{
		
		JLabel titleLabel;
		
		public LabelPanel(JTable table, int width , int height, String title){
			super(new BorderLayout());
			JScrollPane sp = new JScrollPane(table);
			sp.setPreferredSize(new Dimension(width,height));
			setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			add(sp, BorderLayout.CENTER);
			titleLabel = new JLabel();
			if(title!=null){
				titleLabel.setText(title);
			}
			add(titleLabel, BorderLayout.NORTH);
		}
		
		public void changeLabel(String newLabel){
			titleLabel.setText(newLabel);
			repaint();
		}
		
	}

	
}
