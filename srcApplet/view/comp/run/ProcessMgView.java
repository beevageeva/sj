package view.comp.run;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import trace.Instr;
import u.Graph;
import view.model.MainMemAllocator;
import view.model.ProcQueueMg;
import view.model.ProcessMg;

public class ProcessMgView extends JPanel {

	
	private static final long serialVersionUID = 1L;
	
	public ProcessMg processMg;
	
	public ProcessMgView(ProcessMg procMg){
		super(new BorderLayout());
		this.processMg = procMg;
		add(new ProcQueueView(processMg.pQueueMg) , BorderLayout.CENTER);
		if(processMg.memAlloc!=null){
			add(new MemAllocView(processMg.memAlloc) , BorderLayout.SOUTH);
		}
	}
	

	class MemAllocView extends JPanel{

		private static final long serialVersionUID = 1L;
		
		public MemAllocView(MainMemAllocator allocModel){
			InfoButton info = new InfoButton(this , allocModel.getStringInfo());
			JPanel title = new JPanel();
			title.add(info);
			title.add(new JLabel("mem alloc (PFF) "));
			JTable memAllocTable = new JTable(allocModel){
				private static final long serialVersionUID = 1L;
				
				/*
		         * JTable uses this method to determine the default renderer/
		         * editor for each cell.  
		         */
		        @SuppressWarnings("unchecked")
				public Class getColumnClass(int c) {
		        	return getValueAt(0, c).getClass();
		            
		        }
		        public boolean isCellEditable(int row, int col) {
		            //Note that the data/cell address is constant,
		            //no matter where the cell appears onscreen.
		            if (col ==2) {
		                return true;
		            } else {
		                return false;
		            }
		        }
			};
			
			memAllocTable.setDefaultRenderer(ArrayList.class , new ListRenderer(this));
			memAllocTable.setDefaultEditor(ArrayList.class , new ListRenderer(this));
			
			add(Graph.createPanel(memAllocTable , 500 ,200  , title));
			
		}
	}
	
	class ProcQueueView extends JPanel {
		private static final long serialVersionUID = 1L;
		public ProcQueueMg  pmg;
		
		
		public ProcQueueView(ProcQueueMg procQueueMg){
			super();
			this.pmg = procQueueMg;
			JTable procTable =  new JTable(procQueueMg){
				private static final long serialVersionUID = 1L;
			
			/*
	         * JTable uses this method to determine the default renderer/
	         * editor for each cell.  
	         */
	        @SuppressWarnings("unchecked")
			public Class getColumnClass(int c) {
	        	return getValueAt(0, c).getClass();
	            
	        }
	        public boolean isCellEditable(int row, int col) {
	            //Note that the data/cell address is constant,
	            //no matter where the cell appears onscreen.
	            if (col ==1) {
	                return true;
	            } else {
	                return false;
	            }
	        }

			};


			procTable.setDefaultRenderer(ArrayList.class , new ListRenderer(this));
			procTable.setDefaultEditor(ArrayList.class , new ListRenderer(this));

			add(Graph.createPanel(procTable , 500 , 300 ,"Processes"));
		}
		
	}
		
		
	
}
