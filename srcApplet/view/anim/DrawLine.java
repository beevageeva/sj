package view.anim;

import java.awt.Graphics;

public interface DrawLine {

	public int getLength();
	public void drawAtIndex(int index , Graphics g , int x , int y ,int d , short orientation);
	
}
