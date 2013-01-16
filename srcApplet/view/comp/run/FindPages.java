package view.comp.run;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import view.comp.model.CacheModel;
import view.comp.model.CachesModel;
import config.ConfigHolder;


public class FindPages extends JPanel implements ActionListener  , ChangeListener{


	private static final long serialVersionUID = 1L;
	
	private CachesModel cachesModel;
		
	public JButton stopButton;
	public JButton pauseButton;
	public JButton playButton;
	public JButton step1Button;
	
	
	private App app;
	
	public FindPages(App app) {
		super();
		this.app = app;
		setVisible(true);
		setSize(new Dimension(800,600));
		cachesModel = new CachesModel(app);
		setLayout(new BorderLayout());
		add(getActionButtons() , BorderLayout.NORTH );
		add(new CachesPanel(cachesModel) ,  BorderLayout.CENTER);
	}

	
	private JPanel getActionButtons(){
		JPanel buttonPanel = new JPanel(); 
		stopButton = new JButton("stop");
		stopButton.setActionCommand("stop");
		stopButton.addActionListener(this);
		stopButton.setEnabled(false);
		buttonPanel.add(stopButton);
		pauseButton = new JButton("pause");
		pauseButton.setActionCommand("pause");
		pauseButton.addActionListener(this);
		pauseButton.setEnabled(false);
		buttonPanel.add(pauseButton);
		playButton = new JButton("play");
		playButton.setActionCommand("play");
		playButton.addActionListener(this);
		buttonPanel.add(playButton);
		step1Button = new JButton("step");
		step1Button.setActionCommand("step1");
		step1Button.addActionListener(this);
		buttonPanel.add(step1Button);
		InfoButton infoButton = new InfoButton(this , ConfigHolder.generalCfg.getStringInfo());
		buttonPanel.add(infoButton);
		JSlider delaySlider = new JSlider(JSlider.HORIZONTAL,
                App.MAX_DELAY/10, App.MAX_DELAY, App.MAX_DELAY);
		delaySlider.addChangeListener(this);
		delaySlider.setMajorTickSpacing(10);
		delaySlider.setPaintTicks(true);
		
		//Create the label table.
		Hashtable<Integer , JLabel> labelTable = new Hashtable<Integer , JLabel>();
		//PENDING: could use images, but we don't have any good ones.
		labelTable.put(new Integer( App.MAX_DELAY/10 ),
		new JLabel("Fast") );
		//new JLabel(createImageIcon("images/fast.gif")) );
		labelTable.put(new Integer( App.MAX_DELAY ),
		new JLabel("Slow") );
		//new JLabel(createImageIcon("images/slow.gif")) );
		delaySlider.setLabelTable(labelTable);
		
		delaySlider.setPaintLabels(true);
		delaySlider.setBorder(
		BorderFactory.createEmptyBorder(0,0,0,10));

		buttonPanel.add(delaySlider);	
		
		return buttonPanel;
	}
	
	
	
	public CacheModel getMemoryCacheModel() {
		return cachesModel.getCacheModel(cachesModel.getLength()-1);
	}
	


	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("stop")){
			app.reinit();
		}
		else if(e.getActionCommand().equals("pause")){
			app.setPause(true);
			stopButton.setEnabled(true);
			pauseButton.setEnabled(false);
			playButton.setEnabled(true);
			step1Button.setEnabled(true);

		}

		else if(e.getActionCommand().equals("play")){
			app.setStep1(false);
			pauseButton.setEnabled(true);
			start();
		}
		else if(e.getActionCommand().equals("step1")){
			app.setStep1(true);
			pauseButton.setEnabled(false);
			start();
		}
		
	}
	
	private void start(){
		if(app.isStop()){
			app.setStop(false);
			if (app.procQueueMg.getSize() == 0) {
				JOptionPane
						.showMessageDialog(this, "There is no trace file loaded");
				return;
			}
				new Thread(){
					public void run(){
						app.startInstr();
					}
				}.start();
		}
		app.setPause(false);
		stopButton.setEnabled(true);
		playButton.setEnabled(false);
		step1Button.setEnabled(false);
		app.desktop.setEnabledAt(3 , false);

	}
	
	
	public void clear(){
		cachesModel.clearCaches();
	}
	
	public void removeByPid(int pid){
		cachesModel.clearCachesByPid(pid);
	}



    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (source.getValueIsAdjusting()) {
            int delay = (int)source.getValue();
            app.setDelay(delay);
        }
    }


	public CachesModel getCachesModel() {
		return cachesModel;
	}



	
	
}
