package ru.suvrik.gestureshabrahabr;

import android.graphics.Point;

public class Finger {
	public int ID;
	public Point Now;
	public Point Before;
	public long wasDown;
	boolean enabled = false;
	public boolean enabledLongTouch = true;
	Point startPoint;

	public Finger(int id, int x, int y){
	    wasDown = System.currentTimeMillis();
	    ID = id;
	    Now = Before = startPoint = new Point(x, y);
	}

	public void setNow(int x, int y){
	    if(!enabled){
	        enabled = true;
	        Now = Before = startPoint = new Point(x, y);
	    }
	    Before = Now;
	    Now = new Point(x, y);
	    if(ApplicationView.checkDistance(Now, startPoint) > ApplicationView.density * 25)
	        enabledLongTouch = false;
	}
}