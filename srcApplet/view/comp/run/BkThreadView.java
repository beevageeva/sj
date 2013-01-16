package view.comp.run;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import view.comp.model.PageAgingThread.PageAgingListener;
import view.model.MainMemAllocator.ReallocPageListener;

public class BkThreadView extends JPanel implements PageAgingListener , ReallocPageListener{

	private static final long serialVersionUID = 1L;
	private JTextArea pAgTA;
	private JTextArea rpTA;
	
	public BkThreadView(){
		super();
		pAgTA= addThrViewPanel("page aging thread");
		rpTA = addThrViewPanel("reall pages (PFF)");
		
	}
	
	
	public void pageRemoved(int cacheIndex, int pageIndex) {
		pAgTA.setText(pAgTA.getText()+ "\npage aging thread : page " + pageIndex + " removed");
	}

	public void pageReallocated(int fromPid, int toPid ,int nPage) {
		rpTA.setText(rpTA.getText() + "\n page  " + nPage + " alloc from proc "+ fromPid + " to proc "+ toPid);
	}
	
	private JTextArea addThrViewPanel(String label){
		JTextArea ta = new JTextArea(10 , 20);
		ta.setEditable(false);
		JLabel title = new JLabel(label);
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(title , BorderLayout.NORTH);
		panel.add(new JScrollPane(ta) , BorderLayout.CENTER);
		add(panel);
		return ta;
	}

}
