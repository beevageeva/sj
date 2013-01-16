package view.anim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import u.Logger;



import config.ConfigHolder;

public class BPanel extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;

	public static final short RIGHT = 0;

	public static final short LEFT = 1;

	public static final short UP = 2;

	public static final short DOWN = 3;

	private static final int DELAY = 200;

	//length of lines between caches (how many circles can be on this way)
	private static final int[] o = { 8, 7, 6, 5 };

	//width of lines between caches
	private static final int d[] = { 6, 8, 10, 12 };

	private static final int ox = 10;

	private static final int oy = 300;

	private boolean ht;

	private int[] ci;

	private int[] bus;

	private JPanel[] p;
	
	private JButton start;
	
	private JTextArea messageTextArea;
	
	public int getNumberCaches(){
		return bus.length;
	}


	private AbstractAnimation animation;

	private volatile boolean isanim = false;

	// lines length will be described by n , or : line length = d*n
	
	
	private JPanel createPanel(String label){
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createRaisedBevelBorder());
		panel.add(new JLabel(label));
		return panel;

	}

	public BPanel() {
		super();
		this.ht = ConfigHolder.pageTableCfg.getTlbConfig().isEnabled();
		
		bus  = new int[ConfigHolder.getNumberEnabledCaches()];
		String[] labels = new String[bus.length];
		int k = 0;
		for(int i = 0 ; i<=ConfigHolder.numberCaches ; i++){
			if(ConfigHolder.cacheCfgs[i].isEnabled()){
				bus[k] = ConfigHolder.cacheCfgs[i].getBusSize();
				if(i<ConfigHolder.numberCaches){
					labels[k] = "C" + (i+1);
				}
				else{
					labels[k] = "Mem";
				}
				k++;
			}
		}

		
		if (bus.length == 0) {
			Logger.log("at least mm ");
			return;
		}
		// get ci
		int m;
		ci = new int[bus.length];
		for (int i = 0; i < bus.length; i++) {
			m = 0;
			for (int j = 0; j < bus.length; j++) {
				if (j != i && bus[j] < bus[i]) {
					m++;
				}
			}
			ci[i] = m;
			p = new JPanel[bus.length + 3 + (ht ? 1 : 0)];
		}
		setSize(new Dimension(800, 600));
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		start = new JButton("start");
		start.addActionListener(this);
		add(start);
		layout.putConstraint(SpringLayout.WEST, start, 0,
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, start, 0,
				SpringLayout.NORTH, this);
		
		p[0] = createPanel("CPU");
		add(p[0]);
		layout.putConstraint(SpringLayout.WEST, p[0], ox,
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, p[0], oy,
				SpringLayout.NORTH, this);

		p[1] = createPanel("PT");
		add(p[1]);

		layout.putConstraint(SpringLayout.WEST, p[1], 2 * o[0]
				* d[0], SpringLayout.EAST, p[0]);
		layout.putConstraint(SpringLayout.NORTH, p[1],
				-(o[0] * d[0]), SpringLayout.NORTH, p[0]);
		int i = 2;
		if (ht) {
			p[2] = createPanel("TLB");

			add(p[2]);

			layout.putConstraint(SpringLayout.WEST, p[2], 2 * o[0]
					* d[0], SpringLayout.EAST, p[0]);
			layout.putConstraint(SpringLayout.NORTH, p[2], 0,
					SpringLayout.NORTH, p[0]);
			i++;
		}

		p[i] = createPanel(labels[0]);
		add(p[i]);

		layout.putConstraint(SpringLayout.WEST, p[i], 2 * o[0]
				* d[0], SpringLayout.EAST, p[1]);
		layout.putConstraint(SpringLayout.NORTH, p[i], 0,
				SpringLayout.NORTH, p[1]);
		for (int j = 1; j < bus.length; j++) {
			p[j + i] = createPanel(labels[j]);
			add(p[j + i]);

			layout.putConstraint(SpringLayout.WEST, p[j + i],
					o[ci[j - 1]] * d[ci[j - 1]], SpringLayout.EAST,
					p[j + i - 1]);
			layout.putConstraint(SpringLayout.NORTH, p[j + i], 0,
					SpringLayout.NORTH, p[j + i - 1]);

		}
		p[p.length - 1] = createPanel("Disk");
		add(p[p.length - 1]);

		layout.putConstraint(SpringLayout.WEST, p[p.length - 1],
				o[ci[bus.length - 1]] * d[ci[bus.length - 1]], SpringLayout.EAST,
				p[i + bus.length - 1]);
		layout.putConstraint(SpringLayout.NORTH, p[p.length - 1], 0,
				SpringLayout.NORTH, p[i + bus.length - 1]);
		messageTextArea = new JTextArea(3,20);
		messageTextArea.setEditable(false);
		JScrollPane sp = new JScrollPane(messageTextArea);
		add(sp);
		layout.putConstraint(SpringLayout.WEST, sp, 500,
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, sp, 10,
				SpringLayout.NORTH, this);
		setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.fillRect(p[0].getX() + p[0].getWidth(), p[0]
				.getY(), d[0] * o[0], d[0]);
		g.fillRect(p[1].getX() - d[0] * (o[0] + 1), p[1]
				.getY(), d[0], d[0] * o[0]);
		g.fillRect(p[1].getX() - d[0] * o[0],
				p[1].getY(), d[0] * o[0], d[0]);
		g.fillRect(p[1].getX() + p[1].getWidth(), p[1]
				.getY(), 2 * d[0] * o[0], d[0]);
		int j = 2;
		if (ht) {
			g.fillRect(p[2].getX() - d[0] * o[0], p[2]
					.getY(), d[0] * o[0], d[0]);
			g.fillRect(p[2].getX() + p[2].getWidth(),
					p[2].getY(), d[0] * o[0], d[0]);
			g.fillRect(p[2].getX() + p[2].getWidth()
					+ d[0] * (o[0] - 1), p[2].getY() - d[0]
					* (o[0] - 1), d[0], d[0] * (o[0] - 1));
			j++;
		}
		for (int i = 0; i < bus.length -1 ; i++) {
			g.fillRect(p[j + i].getX()
					+ p[j + i].getWidth(), p[j + i]
					.getY(), d[ci[i]] * o[ci[i]], d[0]);
			g.fillRect(p[j + i].getX()
					+ p[j + i].getWidth(), p[j + i]
					.getY()
					+ p[j + i].getHeight() - d[ci[i]], d[ci[i]]
					* o[ci[i]], d[ci[i]]);
		}
		// to disk there is no address
		g.fillRect(p[j + bus.length - 1].getX()
				+ p[j + bus.length - 1].getWidth(), p[j + bus.length
				- 1].getY()
				+ p[j + bus.length - 1].getHeight()
				- d[ci[bus.length - 1]], d[ci[bus.length - 1]]
				* o[ci[bus.length - 1]], d[ci[bus.length - 1]]);
		g.fillRect(p[0].getX(), p[0].getY()
				+ p[0].getHeight(), d[0], d[0] * o[0]);
		g.fillRect(p[0].getX() + d[0], p[0].getY()
				+ p[0].getHeight() + d[0] * (o[0] - 1), p[j]
				.getX()
				- p[0].getX(), d[0]);
		g.fillRect(p[j].getX(), p[j].getY()
				+ p[j].getHeight(), d[0], p[0].getY()
				+ p[0].getHeight() + d[0] * (o[0] - 1)
				- p[j].getY() - p[j].getHeight());

		if (isanim) {
			animation.drawInContainerGraphics(g);
			if(animation.getMessage()!=null){
				messageTextArea.setText(animation.getMessage());
			}
		}
	}

	
	public void setAnimation(AbstractAnimation animation){
		this.animation = animation  ;
	}
	
	
	public void animate() {
		if(animation == null){
			Logger.log("animation is null setAnimation first");
			return;
		}
		isanim = true;
		start.setEnabled(false);
		animation.init();
		
		for (int i = 0; i < animation.getNumberFrames(); i++) {
			animation.updateVariables();
			repaint();
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		isanim = false;
		start.setEnabled(true);
	}

	
	public AbstractAnimation cpuToCacheIndiv(boolean fromCpu) {
		Lines lines = new Lines();
		lines.d = d[0];
		lines.components = new Line[3];
		int i = ht ? 3 : 2;
		lines.startPoint.x = p[fromCpu ? 0 : i].getX();
		lines.startPoint.y = p[fromCpu ? 0 : i].getY()
				+ p[0].getHeight();
		for (int j = 0; j < 3; j++) {
			lines.components[j] = new Line();
		}
		lines.components[fromCpu ? 0 : 2].n = o[0];

		if (fromCpu) {
			lines.components[0].orientation = DOWN;
			lines.components[0].n = o[0] - 1;
		} else {
			lines.components[2].n = o[0];
			lines.components[2].orientation = UP;
		}
		lines.components[1].n = (p[i].getX() - p[0]
				.getX())
				/ d[0];
		lines.components[1].orientation = fromCpu ? RIGHT : LEFT;
		lines.components[fromCpu ? 2 : 0].n = (p[0].getY()
				+ p[0].getHeight() - p[i].getY() - p[i]
				.getHeight())
				/ d[0] + (o[0] - 1);
		if (fromCpu) {
			lines.components[2].orientation = UP;
			lines.components[2].n++;
		} else {
			lines.components[0].orientation = DOWN;
		}
		//return Animations.linesAnimation(lines , new DrawCirclesLine(1));
		return Animations.simpleLinesAnimation(lines , null);
	}

	
	/**
	 * @param i
	 * @param left
	 * @param blocknumber if -1 only put the data , address won't appear(like in the case disk <-> memory) 
	 * @return
	 */
	public AbstractAnimation cacheToCache(int i , boolean left , int blocknumber , int numberTimesToGetData){
		//AbstractAnimation an1 = Animations.linesAnimation(cacheToCache(i , left , true) , new DrawCirclesLine(nt[i]));
		AbstractAnimation an1 = Animations.simpleLinesAnimation(cacheToCache(i , left , true) , String.valueOf(numberTimesToGetData));
		if(blocknumber !=-1){
			//AbstractAnimation an2 = Animations.linesAnimation(cacheToCache(i , left , false) , new DrawNumberLine(blocknumber));
			AbstractAnimation an2 = Animations.simpleLinesAnimation(cacheToCache(i , left , false) , String.valueOf(blocknumber));
			return Animations.parallelAnimation(new AbstractAnimation[]{an1 , an2});
		}
		return an1;
	}
	
	
	/**
	 * @param i :
	 *            if !left then i must be in [0..l-1] ,otherwise in[1..l](if is
	 *            data l = length otherwise length -1 as the last panel is the
	 *            disk)
	 * @param left
	 *            if true then move from cache[i+1] to cache[i] else move from
	 *            cache[i] to cache[i+1]
	 */
	
	private Lines cacheToCache(int i, boolean left, boolean isData) {
		int st = ht ? 3 : 2;
		
		Lines lines = new Lines();
		lines.startPoint.x = left ? p[st + i + 1].getX() : p[st + i]
				.getX()
				+ p[st + i].getWidth();
		lines.startPoint.y = isData ? p[st + i].getY()
				+ p[st + i].getHeight() - d[ci[i]] : p[st + i]
				.getY();
		lines.components = new Line[1];
		lines.components[0] = new Line();
		if (left) {
			lines.components[0].orientation = LEFT;
		} else {
			lines.components[0].orientation = RIGHT;
		}
		if (isData) {
			lines.d = d[ci[i]];
			lines.components[0].n = o[ci[i]];
		} else {
			lines.d = d[0];
			lines.components[0].n = (d[ci[i]] * o[ci[i]]) / d[0];
		}
		
		return lines;
	}

	public AbstractAnimation cpuToCache(boolean foundinTLB  ,int virtPageNumber, int cacheBlockNumber) {
		
		AbstractAnimation[] anim ;
		
		
		Lines lines;
		
		lines = new Lines(d[0]);
		lines.startPoint.x = p[0].getX()
				+ p[0].getWidth();
		lines.startPoint.y = p[0].getY();
		//DrawNumberLine dnlvp =  new DrawNumberLine(virtPageNumber);
		//DrawNumberLine dnlrp = new DrawNumberLine(cacheBlockNumber);
		if (ht) {
			if(!foundinTLB){
				anim = new AbstractAnimation[5];
			}
			else{
				anim = new AbstractAnimation[3];
			}
			lines.components = new Line[1];
			lines.components[0] = new Line(2 * o[0], RIGHT);
			//anim[0] = Animations.linesAnimation(lines , dnlvp);
			anim[0] = Animations.simpleLinesAnimation(lines , String.valueOf(virtPageNumber));
			anim[1] = Animations.panelAnimation(p[2] , foundinTLB ? Animations.PanelAnimation.FOUND_ACTION
					: Animations.PanelAnimation.NOT_FOUND_ACTION);
			if (!foundinTLB) {
				lines = new Lines(d[0]);
				lines.startPoint.x = p[2].getX();
				lines.startPoint.y = p[2].getY();
				lines.components = new Line[3];
				lines.components[0] = new Line(o[0] + 1, LEFT);
				lines.components[1] = new Line(o[0], UP);
				lines.components[2] = new Line(o[0] - 1, RIGHT);
				//anim[2] = Animations.linesAnimation(lines , dnlvp);
				anim[2] = Animations.simpleLinesAnimation(lines , String.valueOf(virtPageNumber));
				anim[3] = Animations.panelAnimation(p[1] ,Animations.PanelAnimation.FOUND_ACTION);
				
				lines = new Lines(d[0]);
				lines.startPoint.x = p[1].getX()
						+ p[1].getWidth();
				lines.startPoint.y = p[1].getY();
				lines.components = new Line[1];
				lines.components[0] = new Line(2 * o[0], RIGHT);
				
				//anim[4] = Animations.linesAnimation(lines , dnlrp);
				anim[4] = Animations.simpleLinesAnimation(lines , String.valueOf(cacheBlockNumber));

			} else {
				lines = new Lines(d[0]);
				lines.startPoint.x = p[2].getX()
						+ p[2].getWidth();
				lines.startPoint.y = p[2].getY();
				lines.components = new Line[3];
				lines.components[0] = new Line(o[0] - 1, RIGHT);
				lines.components[1] = new Line(o[0], UP);
				lines.components[2] = new Line(o[0]
						+ (p[1].getWidth() - p[2]
								.getWidth()) / o[0], RIGHT);
				
				//anim[2] = Animations.linesAnimation(lines , dnlrp);
				anim[2] = Animations.simpleLinesAnimation(lines , String.valueOf(cacheBlockNumber));

			}
		} else {
			anim = new AbstractAnimation[3];
			lines.components = new Line[3];
			lines.components[0] = new Line(o[0] - 1, RIGHT);
			lines.components[1] = new Line(o[0], UP);
			lines.components[2] = new Line(o[0], RIGHT);
			//anim[0] = Animations.linesAnimation(lines , dnlvp);
			anim[0] = Animations.simpleLinesAnimation(lines , String.valueOf(virtPageNumber));

			anim[1] = Animations.panelAnimation(p[1] , Animations.PanelAnimation.FOUND_ACTION);
			lines = new Lines(d[0]);
			lines.startPoint.x = p[1].getX()
					+ p[1].getWidth();
			lines.startPoint.y = p[1].getY();
			lines.components = new Line[1];
			lines.components[0] = new Line(2 * o[0], RIGHT);
			//anim[2] = Animations.linesAnimation(lines , dnlrp);
			anim[2] = Animations.simpleLinesAnimation(lines , String.valueOf(cacheBlockNumber));

		}
		return Animations.sequencialAnimation(anim);

	}

	public void actionPerformed(ActionEvent e) {
		new Thread(){
				public void run(){
					animate();
				}
			}.start();
	}

	
	
	/*
	class DrawCirclesLine implements DrawLine{

		int length;
		
		public DrawCirclesLine(int length){
			this.length = length;
		}
		
		
		public int getLength() {
			return 1;
		}

		public void drawAtIndex(int index, Graphics g, int x, int y, int d , short orientation) {
			g.fillOval(x , y , d , d);
			g.drawString(String.valueOf(length) , x , y);
		}
	}
	
	
	class DrawNumberLine implements DrawLine{

		private String number;
		private int length;
		
		public DrawNumberLine(int number){
			this.number = String.valueOf(number);
			this.length =this.number.length();
			
		}
		
		
		public int getLength() {
			return length;
		}

		public void drawAtIndex(int index, Graphics g, int x, int y, int d , short orientation) {
			g.setFont(g.getFont().deriveFont(d));
			if(orientation==Line.DOWN || orientation == Line.LEFT){
				g.drawString(String.valueOf(number.charAt(length-1-index)) , x , y);
			}
			else{
				g.drawString(String.valueOf(number.charAt(index)) , x , y);
			}
		}
		
	}
*/
}
