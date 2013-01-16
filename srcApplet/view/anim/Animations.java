package view.anim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JPanel;

public final class Animations {

	private Animations() {

	}

	public static AbstractAnimation parallelAnimation(AbstractAnimation[] anim) {
		return new ParallelAnimation(anim);
	}

	public static AbstractAnimation sequencialAnimation(AbstractAnimation[] anim) {
		return new SequentialAnimation(anim);
	}

	public static AbstractAnimation linesAnimation(Lines lines , DrawLine drawLine) {
		return new LinesAnimation(lines , drawLine);
	}
	
	public static AbstractAnimation panelAnimation(JPanel panel , int actionid) {
		return new PanelAnimation(panel , actionid);
	}

	public static AbstractAnimation simpleLinesAnimation(Lines lines , String label) {
		return new SimpleLinesAnimation(lines , label);
	}

	static class ParallelAnimation extends AbstractAnimation {
		public AbstractAnimation[] anim;

		public ParallelAnimation(AbstractAnimation[] anim) {
			this.anim = anim;
		}
		@Override
		public void updateVariables() {
			super.updateVariables();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < anim.length; i++) {
				if (currentFrame < anim[i].getNumberFrames()) {
					anim[i].updateVariables();
					if(anim[i].message !=null){
						sb.append(anim[i].message);
						sb.append("\n");
					}
				}
			}
		}

		
		@Override
		public void drawInContainerGraphics(Graphics g) {
			for (int i = 0; i < anim.length; i++) {
				if (currentFrame < anim[i].getNumberFrames()) {
					anim[i].drawInContainerGraphics(g);
				}
			}
		}

		public int getNumberFrames() {
			int max = anim[0].getNumberFrames();
			for (int i = 1; i < anim.length; i++) {
				if (max < anim[i].getNumberFrames()) {
					max = anim[i].getNumberFrames();
				}
			}
			return max;
		}
		
		@Override
		public void init() {
			super.init();
			for(int i = 0 ; i<anim.length ; i++){
				anim[i].init();
			}
		}


	}

	static class SequentialAnimation extends AbstractAnimation {

		private AbstractAnimation[] anim;

		protected int animIndex;

		public SequentialAnimation(AbstractAnimation[] anim) {
			this.anim = anim;
		}

		protected void checkLength() {
			if (anim[animIndex].getCurrentFrame() == anim[animIndex].getNumberFrames() && animIndex<anim.length-1) {
				animIndex++;
				message = anim[animIndex].message;
			}
		}
		

		public void updateVariables() {
			super.updateVariables();
			checkLength();
			anim[animIndex].updateVariables();
		}

		public void drawInContainerGraphics(Graphics g) {
			checkLength();
			anim[animIndex].drawInContainerGraphics(g);

		}

		public int getNumberFrames() {
			int sum = 0;
			for (int i = 0; i < anim.length; i++) {
				sum += anim[i].getNumberFrames();
			}
			return sum;
		}

		@Override
		public void init() {
			super.init();
			animIndex = 0;
			for(int i = 0 ; i<anim.length ; i++){
				anim[i].init();
			}
			if(anim.length>0){
				message = anim[0].message;
			}
		}
	}

	
	static class SimpleLinesAnimation extends AbstractAnimation{
		
		private Lines lines;
		private int x, y , pos , lineIndex  ;
		private String label;
		
		
		
		public SimpleLinesAnimation(Lines lines, String label){
			this.lines = lines;
			this.label = label;
		}
		
		@Override
		public void init() {
			super.init();
			lineIndex = 0;
			pos =-1;
			x = lines.startPoint.x;
			y = lines.startPoint.y;

		}
		
		
		@Override
		protected void updateVariables() {
			super.updateVariables();
			if (pos == lines.components[lineIndex].n-1 && lineIndex<lines.components.length -1) {
				lineIndex++;
				pos = 0;
			}
			else{
				pos++;
			}
			switch(lines.components[lineIndex].orientation){
			case Line.LEFT:
				x-=lines.d;
				break;
			case Line.RIGHT:
				x+=lines.d;
				break;
			case Line.UP:
				y-=lines.d;
				break;
			case Line.DOWN:
				y+=lines.d;
				break;
			}
		}

		@Override
		public void drawInContainerGraphics(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillOval(x , y , lines.d , lines.d);
			if(label!=null){
			switch(lines.components[0].orientation){
				case Line.LEFT:
					g.drawString(label , lines.startPoint.x - (int) g.getFont().getStringBounds(label , ((Graphics2D)g).getFontRenderContext()).getX() - lines.d , lines.startPoint.y - lines.d);
					break;
				case Line.RIGHT:
					g.drawString(label , lines.startPoint.x  +  lines.d , lines.startPoint.y -lines.d);
					break;
				case Line.UP:
					g.drawString(label , lines.startPoint.x  +  lines.d , lines.startPoint.y -lines.d);
					break;
				case Line.DOWN:
					g.drawString(label , lines.startPoint.x  +  lines.d , lines.startPoint.y + lines.d);
					break;
				}
			}
			
		}

		@Override
		public int getNumberFrames() {
			int s = 0;
			for (int i = 0; i < lines.components.length; i++) {
				s += lines.components[i].n;
			}
			return s;
		}
		
	}
	
	static class LinesAnimation extends AbstractAnimation {

		private Lines lines;

		private int lineIndex, pos, x, y;
		private DrawLine drawLine;

		public LinesAnimation(Lines lines, DrawLine drawLine) {
			this.lines = lines;
			this.drawLine = drawLine;
		}

		@Override
		public void init() {
			super.init();
			lineIndex = 0;
			pos = -drawLine.getLength() -1;
			x = lines.startPoint.x;
			y = lines.startPoint.y;

		}

		public void updateVariables() {
			super.updateVariables();
			if (pos == lines.components[lineIndex].n-1 && lineIndex<lines.components.length -1) {
				lineIndex++;
				pos = 0;
			}
			else{
				pos++;
			}

		}

		public void drawInContainerGraphics(Graphics g) {
			g.setColor(Color.BLACK);
			if (lineIndex == 0 && pos < 0) {
				drawInFront(lineIndex, 0, drawLine.getLength() + pos, x, y, g);
			} else if (lineIndex == lines.components.length - 1
					&& pos > lines.components[lines.components.length - 1].n
							- drawLine.getLength() + 1) {
				Point pp = drawInFront(lineIndex, pos,
						lines.components[lines.components.length - 1].n - pos
								- 1, x, y, g);
				if (pp != null) {
					x = pp.x;
					y = pp.y;
				}
			} else {
				Point pp = drawInFront(lineIndex, pos, drawLine.getLength(), x, y, g);
				x = pp.x;
				y = pp.y;

			}
		}

		private Point drawInFront(int li, int p, int l, int x, int y, Graphics g) {
			if (l == 0) {
				return null;
			}
			int op = p, oli = li, ox = x, oy = y;
			drawLine.drawAtIndex(0 , g , x, y , lines.d , lines.components[oli].orientation);
			if (oli < lines.components.length - 1
					&& op == lines.components[oli].n) {
				oli++;
				op = 0;
			}
			switch (lines.components[oli].orientation) {
			case Line.LEFT:
				ox -= lines.d;
				break;
			case Line.RIGHT:
				ox += lines.d;
				break;
			case Line.UP:
				oy -= lines.d;
				break;
			case Line.DOWN:
				oy += lines.d;
				break;
			}
			op++;
			Point pp = new Point(ox, oy);
			for (int i = 1; i < l; i++) {
				drawLine.drawAtIndex(i , g , ox, oy, lines.d , lines.components[oli].orientation);
				if (oli < lines.components.length - 1
						&& op == lines.components[oli].n) {
					oli++;
					op = 0;
				}
				switch (lines.components[oli].orientation) {
				case Line.LEFT:
					ox -= lines.d;
					break;
				case Line.RIGHT:
					ox += lines.d;
					break;
				case Line.UP:
					oy -= lines.d;
					break;
				case Line.DOWN:
					oy += lines.d;
					break;

				}
				op++;
			}
			return pp;

		}

		public int getNumberFrames() {
			int s = 0;
			for (int i = 0; i < lines.components.length; i++) {
				s += lines.components[i].n;
			}
			return s + drawLine.getLength();
		}

	}
	
	static class PanelAnimation extends AbstractAnimation{

		public static final int FOUND_ACTION = 0;
		public static final int NOT_FOUND_ACTION = 1;
		
		private JPanel panel;
		private int actionid;
		
		public PanelAnimation(JPanel panel , int actionid){
			this.actionid = actionid;
			this.panel = panel;

		}
		
		@Override
		protected void updateVariables() {
			super.updateVariables();
		}

		@Override
		public void drawInContainerGraphics(Graphics g) {
			if(currentFrame==0){
				if(actionid==FOUND_ACTION ){
					panel.setBackground(Color.GREEN);
				}
				else{
					panel.setBackground(Color.RED);
				}
			}
			else{
				panel.setBackground(javax.swing.plaf.ColorUIResource.LIGHT_GRAY);
			}
		}

		@Override
		public int getNumberFrames() {
			return 2;
		}
		
		public JPanel getPanel(){
			return panel;
		}
		
	}


}
