package org.tourgune.apptrack.bean;

import java.util.ArrayList;
import java.util.Collection;


/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 * Bean que almacena una lista de puntos
 */
public class PointList {
	
	private Collection<Point> points;
	
    public PointList() {
		super();
		points = new ArrayList<Point>();
	}

	public Collection<Point> getPoints() {
		return points;
	}

	public void setPoints(Collection<Point> points) {
		this.points = points;
	}
	
	public boolean addPoints(Point point){
		return this.points.add(point);
	}
	

   
   

    

}
