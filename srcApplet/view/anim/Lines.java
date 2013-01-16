package view.anim;

import java.awt.Point;




class Line {
	
	public static final short RIGHT = 0;
	public static final short LEFT = 1;
	public static final short UP = 2;
	public static final short DOWN = 3;

	
	public Line(int n, short orientation ) {
		this.n = n;
		this.orientation = orientation;
	}

	public Line() {
	}

	int n;
	short orientation;
}

public class Lines {
	
	public Lines(){}
	public Lines(int d){
		this.d = d;
	}
	
	Line[] components;
	Point startPoint = new Point();
	int d;
}

