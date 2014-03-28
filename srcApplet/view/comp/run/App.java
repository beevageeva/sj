package view.comp.run;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import u.MainFrame;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;


import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import trace.Instr;
import trace.InstrReader;
import u.Constants;
import u.Helper;
import u.Logger;
import u.SjProperties;
import view.anim.AbstractAnimation;
import view.anim.Animations;
import view.anim.BPanel;
import view.comp.model.CacheModel;
import view.comp.model.DirectMappedPageTableModel;
import view.comp.model.InverseMappedPageTableModel;
import view.comp.model.PageAgingThread;
import view.model.MainMemAllocator;
import view.model.ProcQueueMg;
import view.model.ProcessMg;
import view.model.ProcQueueMg.ProcessListener;
import config.ConfigHolder;
import config.ConfigReader;


/**
 * @author root
 *
 */
public class App extends JApplet implements ActionListener, ProcessListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JTabbedPane desktop;

	private Configuration configuration;

	private PageTable pageTable;

	private FindPages findPages;

	public ArrayList<AbstractAnimation> animations;

	private JFileChooserTF traceChooser;

	private JFileChooser configChooser;

	private ServerChooseListTF traceListChooser;

	private ServerChooseList configListChooser;

	private volatile boolean pause = true;

	private volatile boolean stop = true;

	private volatile boolean step1 = false;

	private ProcessMgView processMgView;

	public ProcQueueMg procQueueMg;

	JMenuItem startItem;

	JMenuItem loadServerTraceItem;

	JMenuItem loadLocalTraceItem;

	JMenuItem viewLoadedTracesItem;

	JMenuItem loadServerConfigItem;

	JMenuItem loadLocalConfigItem;

	JMenuItem reconfigItem;
	JMenuItem saveconfigItem;

	public BPanel bPanel;

	private BkThreadView thrView;

	public static final int MAX_DELAY = 3000;

	private volatile int delay = MAX_DELAY;

	private HelpBroker helpBroker;
	
	//used for questions , state
	private short state = 3;
	String configFileName=null;
	private List<String> traceFileNames = null;
	private List<Integer> traceIntUnits = null;
	

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public boolean isStarted() {
		return findPages != null;
	}

	/**
	 * @param instrReader
	 * @return true if a new process can be added (if the number of processes
	 *         does not overflow the max number of processes) false otherwise
	 */
	public boolean addInstrReader(InstrReader instrReader, int tUnits) {
		short status = processMgView.processMg.addProcess(instrReader, tUnits);
		switch (status) {
		case ProcessMg.NO_MEM_AVAILABLE:
			JOptionPane.showMessageDialog(this,
					"No memory available to add another process");
			return false;
		case ProcessMg.TOO_MANY_TRACE_FILES:
			JOptionPane
					.showMessageDialog(this,
							"Too many trace files(exceed configured number of processes");
		case ProcessMg.PROCESS_ALOCATED:
			return true;
		}
		return true;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
	
	//the param for the trace must be in the following form:
	//confiGFileName=<filename>
	//traceFiles=<filename1>,<tUnits1>;<filename2>,<tUnits2>   ...
	public void init() {
		// Make the big window be indented 50 pixels from each edge
		// of the screen.
		// TODO remove this when put in browser
		// Constants.host = getCodeBase().getHost();
		configFileName = this.getParameter("configFileName");
		if(configFileName!=null){
			String traceFileST= this.getParameter("traceFiles");
			String nameTUnitsToken;
			if(traceFileST!=null){
				StringTokenizer st = new StringTokenizer(traceFileST, ";");
				traceFileNames = new ArrayList<String>();
				traceIntUnits = new ArrayList<Integer>();
				while(st.hasMoreTokens()){
					nameTUnitsToken = st.nextToken();
					try{
						traceFileNames.add(nameTUnitsToken.substring(0, nameTUnitsToken.indexOf(",")));
						traceIntUnits.add(Integer.parseInt(nameTUnitsToken.substring(nameTUnitsToken.indexOf(",")+1)));
					}
					catch(ArrayIndexOutOfBoundsException e1){
						e1.printStackTrace();
						//TODO
					}
					catch(NumberFormatException e2){
						e2.printStackTrace();
						//TODO
					}
				}
			}
			else{
				//must be both diff of null
				//TODO must remove this
				configFileName=null;
			}
		}
		int inset = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height
				- inset * 2);

		// Set up the GUI.
		desktop = new JTabbedPane();
		configuration = new Configuration(this);
		desktop.addTab("conf", configuration);
		setContentPane(desktop);
		setJMenuBar(createMenuBar());

		traceChooser = new JFileChooserTF();
		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("trd");
		filter.setDescription("trace files");
		traceChooser.setFileFilter(filter);
		traceListChooser = new ServerChooseListTF(this, "http://"
				+ Constants.host + "/" + Constants.context +"/sendFileNames.html?fileType=trace");

		configChooser = new JFileChooser();
		filter = new ExampleFileFilter();
		filter.addExtension("xml");
		filter.setDescription("config files");
		configChooser.setFileFilter(filter);
		configListChooser = new ServerChooseList(this, "http://"
				+ Constants.host + "/" + Constants.context +"/sendFileNames.html?fileType=conf");

		animations = new ArrayList<AbstractAnimation>();
		procQueueMg = new ProcQueueMg();
		procQueueMg.addProcessListener(this);

		ClassLoader thisClassLoader = this.getClass().getClassLoader();
		try {
			HelpSet hs = new HelpSet(thisClassLoader, HelpSet.findHelpSet(
					thisClassLoader, "sj"));
			helpBroker = hs.createHelpBroker();
		} catch (HelpSetException e) {
			e.printStackTrace();
		}




	}

	public void startFindPages() {
		findPages = new FindPages(this);
		if (ConfigHolder.pageTableCfg.isDirectMapped()) {
			pageTable = new DirectMappedPageTable(
					new DirectMappedPageTableModel(
							findPages.getCachesModel().getCacheModel(
									findPages.getCachesModel().getLength() - 1),
							this));
		} else {
			pageTable = new InverseMappedPageTable(
					new InverseMappedPageTableModel(
							findPages.getCachesModel().getCacheModel(
									findPages.getCachesModel().getLength() - 1),
							this));
		}
		thrView = null;
		if (ConfigHolder.generalCfg.pageAgingConfig.isEnabled()) {
			PageAgingThread pagThr = new PageAgingThread(
					new CacheModel[] { findPages.getMemoryCacheModel() },
					ConfigHolder.generalCfg.pageAgingConfig
							.getPageAgingIncrease(),
					ConfigHolder.generalCfg.pageAgingConfig.getMemRefToBeRun());
			if (thrView == null) {
				thrView = new BkThreadView();
			}
			pagThr.addPageAgingListener(thrView);
		}
		MainMemAllocator malloc = null;
		if (ConfigHolder.generalCfg.memAllocConfig.isEnabled()) {
			malloc = new MainMemAllocator(findPages.getMemoryCacheModel(),
					ConfigHolder.generalCfg.memAllocConfig.getMinPFF(),
					ConfigHolder.generalCfg.memAllocConfig.getMaxPFF(),
					ConfigHolder.generalCfg.memAllocConfig
							.getNEvictedNodesToRun());
			if (thrView == null) {
				thrView = new BkThreadView();
			}
			malloc.addReallocPageListener(thrView);
		}
		bPanel = new BPanel();
		desktop.addTab("findPages", findPages);
		desktop.addTab("pageTable", pageTable);
		desktop.addTab("bp", bPanel);
		processMgView = new ProcessMgView(new ProcessMg(procQueueMg, malloc));
		desktop.addTab("proc", processMgView);
		if (thrView != null) {
			desktop.add("bkthr", thrView);
		}
		// bp not enabled
		desktop.setEnabledAt(3, false);
		startItem.setEnabled(false);
		loadLocalTraceItem.setEnabled(true);
		loadServerTraceItem.setEnabled(true);
		viewLoadedTracesItem.setEnabled(true);

	}

	public void startInstr() {
		try {
			Instr instr = procQueueMg.getCurrentInstr();
			int vpn;
			int rpn;
			int vpnNBits = ConfigHolder.generalCfg.getVirtualAddrNBits()
					- ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]
							.getBlockSizeNBits(instr.type);
			String vaBinaryRepr;
			String raBinaryRepr = null;
			int pid;
			while (!stop && instr != null) {
				while (pause) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				pid = procQueueMg.getPid();
				if (procQueueMg.getType() == ProcQueueMg.INSTR1) {

					vaBinaryRepr = instr.addressBinaryRepr;
					vpn = Integer.parseInt(Helper.convertDecimalToBinary(pid,
							ConfigHolder.generalCfg.getNumberProcessesNBits())
							+ vaBinaryRepr.substring(0, vpnNBits), 2);

					// desktop.setSelectedIndex(2);
					setDesktopPanel(2);
					rpn = pageTable.getPhysicalPageNumber(vpn, instr.type, pid);
					raBinaryRepr = Helper.convertDecimalToBinary(rpn,
							ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]
									.getNumberEntriesNBits())
							+ vaBinaryRepr.substring(vpnNBits);
					if (pageTable.putInWait()) {
						processMgView.processMg.pQueueMg.putInWait(
								ConfigHolder.generalCfg.getDiskAccessTime(),
								instr.type, raBinaryRepr);
					}
				} else if (processMgView.processMg.pQueueMg.getType() == ProcQueueMg.INSTR2) {
					raBinaryRepr = instr.addressBinaryRepr;
				}
				if (processMgView.processMg.pQueueMg.getType() != ProcQueueMg.INSTR1INTER) {
					if (processMgView.processMg.pQueueMg.getType() == ProcQueueMg.INSTR1) {
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// desktop.setSelectedIndex(1);
					setDesktopPanel(1);

					findPages.getCachesModel().resolve(raBinaryRepr,
							instr.type, pid);
				}
				if (step1) {
					pause = true;
					// anim
					AbstractAnimation[] an = (AbstractAnimation[]) animations
							.toArray(new AbstractAnimation[animations.size()]);
					animations.clear();
					bPanel.setAnimation(Animations.sequencialAnimation(an));
					desktop.setEnabledAt(3, true);
					// end anim

					findPages.step1Button.setEnabled(true);
					findPages.playButton.setEnabled(true);
				} else {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				processMgView.processMg.pQueueMg.finishInstr();
				instr = processMgView.processMg.pQueueMg.getCurrentInstr();

			}
			JOptionPane.showMessageDialog(this, "terminated \n"
					+ findPages.getCachesModel().getAllCachesStats());
			reinit();
		} catch (Exception e) {
			Logger.log(e);
		}
	}

	protected JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("Config");
		menu.setMnemonic(KeyEvent.VK_C);
		menuBar.add(menu);
		if(configFileName!=null){
			menu.setEnabled(false);
			configureFromServerFile(configFileName);
		}
		loadLocalConfigItem = new JMenuItem("Load local config file");
		loadLocalConfigItem.setActionCommand("loadLocalConfig");
		loadLocalConfigItem.addActionListener(this);
		menu.add(loadLocalConfigItem);
		loadServerConfigItem = new JMenuItem("Load server config file");
		loadServerConfigItem.setActionCommand("loadServerConfig");
		loadServerConfigItem.addActionListener(this);
		menu.add(loadServerConfigItem);
		reconfigItem = new JMenuItem("Reconfig");
		reconfigItem.setActionCommand("reconfig");
		reconfigItem.addActionListener(this);
		reconfigItem.setEnabled(false);
		menu.add(reconfigItem);
		saveconfigItem = new JMenuItem("Export config to XML");
		saveconfigItem.setActionCommand("exportXml");
		saveconfigItem.addActionListener(this);
		saveconfigItem.setEnabled(false);
		menu.add(saveconfigItem);

		menu = new JMenu("Trace");
		if(configFileName!=null){
			menu.setEnabled(false);
		}
		menu.setMnemonic(KeyEvent.VK_T);
		menuBar.add(menu);
		loadLocalTraceItem = new JMenuItem("Load local trace file");
		loadLocalTraceItem.setActionCommand("loadLocalTrace");
		loadLocalTraceItem.addActionListener(this);
		loadLocalTraceItem.setEnabled(false);
		menu.add(loadLocalTraceItem);
		loadServerTraceItem = new JMenuItem("Load trace file from server");
		loadServerTraceItem.setActionCommand("loadServerTrace");
		loadServerTraceItem.addActionListener(this);
		loadServerTraceItem.setEnabled(false);
		menu.add(loadServerTraceItem);
		viewLoadedTracesItem = new JMenuItem("View loaded trace files");
		viewLoadedTracesItem.setActionCommand("viewLoadedTrace");
		viewLoadedTracesItem.addActionListener(this);
		viewLoadedTracesItem.setEnabled(false);
		menu.add(viewLoadedTracesItem);

		menu = new JMenu("Actions");
		menuBar.add(menu);
		startItem = new JMenuItem("Start");
		startItem.setActionCommand("start");
		startItem.addActionListener(this);
		if(configFileName!=null){
			startItem.setEnabled(true);
		}
		else{
			startItem.setEnabled(false);
		}
		menu.add(startItem);
		JMenuItem menuItem = new JMenuItem("Quit");
		menuItem.setMnemonic(KeyEvent.VK_Q);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				ActionEvent.ALT_MASK));
		menuItem.setActionCommand("quit");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// HELP MENU
		menu = new JMenu("Help");
		menuBar.add(menu);
		menuItem = new JMenuItem("Help");
		menuItem.setActionCommand("help");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		return menuBar;
	}

	// React to menu selections.
	public void actionPerformed(ActionEvent e) {
		if ("loadLocalTrace".equals(e.getActionCommand())) {
			int returnVal = traceChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = traceChooser.getSelectedFile();
				if (file != null) {
					try {
						addInstrReader(new InstrReader(
								new FileInputStream(file),
								ConfigHolder.generalCfg.getVirtualAddrNBits(),
								file.getName()), traceChooser.getValue());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		} else if ("loadServerTrace".equals(e.getActionCommand())) {
			traceListChooser.showDialog(null, "open", "open file", -1, null);
			String filename = traceListChooser.getName();
			if (filename != null) {
				loadServerTraceFile(filename, traceListChooser
							.getValue());
			}
		}

		else if ("loadLocalConfig".equals(e.getActionCommand())) {
			int returnVal = configChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {

					File file = configChooser.getSelectedFile();
					ConfigReader.setConfig(new FileInputStream(file));
					configuration.configureFromFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		} else if ("loadServerConfig".equals(e.getActionCommand())) {
			configListChooser.showDialog(null, "open", "open file", -1, null);
			String serverFileName = configListChooser.getName();
			if (serverFileName != null) {
				configureFromServerFile(serverFileName);
			}

		}

		else if ("viewLoadedTrace".equals(e.getActionCommand())) {
			String[] instrTrNames = processMgView.processMg.pQueueMg.getNames();
			ListDialog.showDialog(this, null, "view", "view trace files",
					instrTrNames, -1, null, "Remove");
			final int index = ListDialog.getSelectedIndex();
			if (index != -1) {
				new Thread() {
					public void run() {
						int pid = processMgView.processMg.pQueueMg.getPidAtIndex(index);
						removePid(pid);
					}
				}.start();
			}

		}

		else if ("quit".equals(e.getActionCommand())) {
			quit();
		}

		else if ("start".equals(e.getActionCommand())) {
			startFindPages();
			//load now the trace files that come as param
			if(configFileName!=null){
				for(int i = 0 ; i<traceFileNames.size();i++){
					try {
						addInstrReader(new InstrReader(new URL("http://"
								+ Constants.host + "/" +Constants.context  + "/files/trace/" + traceFileNames.get(i))
								.openStream(), ConfigHolder.generalCfg
								.getVirtualAddrNBits(), traceFileNames.get(i)), traceIntUnits.get(i));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}

		else if ("exportXml".equals(e.getActionCommand())) {
			String message = ConfigReader.exportXml();
			JTextArea ta = new JTextArea(message, 40, 50);
		  ta.setEditable(false);
			JOptionPane.showMessageDialog(null,new JScrollPane(ta), "Configuration XML", JOptionPane.INFORMATION_MESSAGE);
		}

		else if ("reconfig".equals(e.getActionCommand())) {
			reconfig();
		} else if ("help".equals(e.getActionCommand())) {
			// TODO remove this cond , it's for appletviewre
			if (helpBroker != null)
				helpBroker.setDisplayed(true);
		}
	}
	
	private void configureFromServerFile(String filename){
		try {
			ConfigReader.setConfig(new URL("http://" + Constants.host
					+ "/" + Constants.context + "/files/conf/" + filename).openStream());
			configuration.configureFromFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void loadServerTraceFile(String filename , int timeUnits){
		try {
			addInstrReader(new InstrReader(new URL("http://"
					+ Constants.host + "/" + Constants.context + "/files/trace/" + filename)
					.openStream(), ConfigHolder.generalCfg
					.getVirtualAddrNBits(), filename), timeUnits);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	// Quit the application.
	protected void quit() {
		System.exit(0);
	}

	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public void setStep1(boolean step1) {
		this.step1 = step1;
	}

	public void reconfig() {
		if (findPages != null) {
			desktop.remove(findPages);
			findPages = null;
		}
		if (pageTable != null) {
			desktop.remove(pageTable);
			pageTable = null;
		}
		if (bPanel != null) {
			desktop.remove(bPanel);
			bPanel = null;

		}
		//reset queue defined in constructor
		procQueueMg.reset();
		if (processMgView != null) {
			desktop.remove(processMgView);
			processMgView = null;
		}
		if (thrView != null) {
			desktop.remove(thrView);
			thrView = null;

		}
		configuration.setPanelsEnabled(true);
		repaint();
		loadLocalTraceItem.setEnabled(false);
		loadServerTraceItem.setEnabled(false);
		viewLoadedTracesItem.setEnabled(false);
		configuration.okButton.setEnabled(true);
		reconfigItem.setEnabled(false);
		saveconfigItem.setEnabled(false);
		loadServerConfigItem.setEnabled(true);
		loadLocalConfigItem.setEnabled(true);
	}

	public void reinitBeforeOpt() {
		processMgView.processMg.reinit();

	}

	public void reinit() {
		stop = true;
		pause = true;
		findPages.stopButton.setEnabled(false);
		findPages.pauseButton.setEnabled(false);
		findPages.step1Button.setEnabled(true);
		findPages.playButton.setEnabled(true);
		pageTable.clear();
		findPages.clear();
		processMgView.processMg.reinit();

	}
	
	private void removePid(int pid){
		pageTable.clear(pid);
		findPages.removeByPid(pid);
		processMgView.processMg.remove(pid);
		
	}

	public boolean isStep1() {
		return step1;
	}

	private void setDesktopPanel(final int index) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					desktop.setSelectedIndex(index);

				}

			});

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	
	   //
    // The following allows App to be run as an application
    // as well as an applet
    //
    public static void main(String[] args) {
    	new MainFrame(new App(), 800, 600);
    }

	@Override
	public void processAdded(int pid, InstrReader instrReader, int units) {
	}

	@Override
	public void processFinished(int pid) {
		removePid(pid);		
	}
}
