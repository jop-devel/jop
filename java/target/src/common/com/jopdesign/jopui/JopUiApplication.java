/*
 * This file is a part of the Jop-UI 
 * Copyright (C) 2009, 	Stefan Resch (e0425306@student.tuwien.ac.at)
 * 						Stefan Rottensteiner (e0425058@student.tuwien.ac.at)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.jopui;

import com.jopdesign.jopui.core.Canvas;
import com.jopdesign.jopui.core.Graphics;
import com.jopdesign.jopui.event.JopEvent;
import com.jopdesign.jopui.event.JopUiEventListener;

/**
 * Super class for all JopUi Applications, contains canvas and implements EventListeners
 */
public abstract class JopUiApplication implements JopUiEventListener {

	protected Canvas canvas;
	
	/**
	 * Creates a new JopUiApplication
	 */
	public JopUiApplication () {
		canvas = new Canvas();
	}
	
	/**
	 * Initialize the application
	 * @return true if successful
	 */
	public abstract boolean init();	
	
	/**
	 * needed for implementation for eventlistener 
	 * @param ev 
	 */
	public abstract boolean notify(JopEvent ev);
	
	/**
	 * is called on program exit
	 */
	public abstract void terminate();
	
	/**
	 * draws the canvas to the screen
	 * @param g graphics object 
	 */
	public void paint(Graphics g) {
		canvas.paint(g);
	}	
	
	/**
	 * passed the events to the canvas
	 * @param e jopevent
	 * @return True if a redraw is necessary, otherwise false
	 */
	public boolean distribute(JopEvent e) {
		return canvas.distribute(e);
	}
}
