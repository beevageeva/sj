package view.comp.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import sun.swing.table.DefaultTableCellHeaderRenderer;
import u.Graph;
import view.comp.model.CacheModel;
import view.model.CacheEvent;
import view.model.CacheListener;

public class CachePanel extends JPanel implements
		CacheListener  , MouseListener{

	private static final long serialVersionUID = 1L;

	private JTable table;
	protected JPanel northPanel;
	protected CacheModel model;



	public CachePanel(final CacheModel model, String label, int width,
			int height) {
		super(new BorderLayout());
		this.model = model;
		table = new JTable(model.getCache());
		//set tooltips for the columns
		
		table.getTableHeader().setDefaultRenderer(new DefaultTableCellHeaderRenderer(){

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				this.setToolTipText(model.getCache().getColumnTooltipString(column));
				return result;
			}});
		
		
		model.getCache().addCacheListener(this);
		northPanel = new JPanel();
		this.add(northPanel , BorderLayout.NORTH);
		JPanel title = new JPanel();
		title.add(new JLabel(label));
		title.add(new InfoButton(this, model.getCfg()
				.getStringInfo()));
		this.add(Graph.createPanel(table, width, height, title),
				BorderLayout.CENTER);
		JLabel stL = new JLabel("st");
		stL.addMouseListener(this);
		this.add(stL, BorderLayout.SOUTH);

	}


	public void objectRead(CacheEvent e) {
		/*
		table.setSelectionBackground(new Color(128, 128, 128));
		table.setRowSelectionInterval(e.getIndex(), e.getIndex());
		moveScrollBar(e.getIndex());
		*/
		setRowColor(e.getIndex() , new Color(128, 128, 128));
	}

	public void objectIsToBeModified(CacheEvent e) {
		/*
		table.setSelectionBackground(new Color(128, 128, 0));
		table.setRowSelectionInterval(e.getIndex(), e.getIndex());
		moveScrollBar(e.getIndex());
		*/
		setRowColor(e.getIndex() , new Color(128, 128, 0));
	}

	public void objectPut(CacheEvent e) {
		/*
		table.setSelectionBackground(new Color(184, 207, 229));
		table.setRowSelectionInterval(e.getIndex(), e.getIndex());
		moveScrollBar(e.getIndex());
		*/
		setRowColor(e.getIndex() , new Color(184, 207, 229));
	}

	public void objectIsToBeEvicted(CacheEvent e) {
		/*
		table.setSelectionBackground(new Color(200, 0, 0));
		table.setRowSelectionInterval(e.getIndex(), e.getIndex());
		moveScrollBar(e.getIndex());
		*/
		setRowColor(e.getIndex() , new Color(200, 0, 0));

				try {
				Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

	}

	public void objectIsToBeRemoved(CacheEvent e) {
		/*
		table.setSelectionBackground(new Color(200, 200, 0));
		table.setRowSelectionInterval(e.getIndex(), e.getIndex());
		moveScrollBar(e.getIndex());
		*/
		setRowColor(e.getIndex() ,new Color(200, 200, 0));

				try {
				Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

	}
	
	private void setRowColor(final int index , final Color color){
	//	try {
			//SwingUtilities.invokeAndWait(new Runnable(){
				//public void run() {
					table.setSelectionBackground(color);
					table.setRowSelectionInterval(index, index);
				//}});
	
	/*
	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		Runnable r = new Runnable() {
			public void run() {
				JViewport viewPort = (JViewport) table.getParent();
				JScrollBar vsb = ((JScrollPane) viewPort.getParent())
						.getVerticalScrollBar();
				// TODO 8, 5
				vsb.setValue((8 * index * vsb.getBlockIncrement()) / 5);
			}
		};
		SwingUtilities.invokeLater(r);

		
	}

	public void mouseClicked(MouseEvent e) {
		JOptionPane.showMessageDialog(this, model.getCacheStat().getStatString());
	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
