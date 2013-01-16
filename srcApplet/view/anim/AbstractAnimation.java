package view.anim;

import java.awt.Graphics;

public abstract class AbstractAnimation {
	
	protected int currentFrame;
	protected String message;
	
	public AbstractAnimation(){}
	public AbstractAnimation(String message){
		this.message = message;
	}
	
	public void init(){
		currentFrame = 0;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
	

	protected void updateVariables(){
		currentFrame++;
	}
	public abstract void drawInContainerGraphics(Graphics g);
	public abstract int getNumberFrames();


	public int getCurrentFrame() {
		return currentFrame;
	}
	
	
	
}
