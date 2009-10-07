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

package com.jopdesign.jopui.core;

import com.jopdesign.jopui.event.JopEvent;
import com.jopdesign.jopui.event.JopUiEventListener;

/**
 * Every GUI object implements this abstract class
 */
public abstract class Component {

	/**
	 * Object identifier for a label <p>
	 * This constant has the value 0
	 */
	public static final int LABEL = 0;
	
	/**
	 * Object identifier for a canvas <p>
	 * This constant has the value 1
	 */
	public static final int CANVAS = 1;
	
	/**
	 * Object identifier for a button <p>
	 * This constant has the value 2
	 */
	public static final int BUTTON = 2;
	
	/**
	 * Object identifier for a textfield <p>
	 * This constant has the value 3
	 */
	public static final int TEXTFIELD = 3;
	
	/**
	 * Object identifier for a checkbox <p>
	 * This constant has the value 4
	 */
	public static final int CHECKBOX = 4;
	
	/**
	 * Object identifier for a option element <p>
	 * This constant has the value 5
	 */
	public static final int OPTION = 5;
	
	/**
	 * Object identifier for a imagebox <p>
	 * This constant has the value 6
	 */
	public static final int IMAGEBOX = 6;
	
	
	protected Image img;
	protected Bounds bounds;
	protected boolean changed = false;
	private JopUiEventListener eventListener = null;
	private String command = "";
	private boolean focus = false;
	private Canvas canvas = null;
	
	/**
	 * Returns the bounds of the current GUI component
	 * @return bound rectangle
	 */
	public Bounds getBounds() {
		return bounds;		
	}
	
	protected void setCanvas(Canvas c) {
		canvas = c;
	}
	
	protected void setFocus(boolean focus) {
		if(this.focus == focus)
			return;

		changed = true;
		this.focus = focus;
	}
	
	/**
	 * Tests if component has the focus
	 * @return returns true if component has the focus otherwise false.
	 */
	public boolean isFocus() {
		return focus;
	}
	
	protected void makeFocus() {
		if((canvas != null) && (this.focus == false)) {
			changed = true;
			canvas.setComponentFocus(this);
		}
	}
	
	protected Image create() {
		changed = false;
		img = Image.createImage(bounds.getWidth(),bounds.getHeight());
		img.setColorKey(Theme.colorKey);
		return img;
	}
	
	/**
	 * Registers an eventlistener for this component.
	 * 
	 * @param eventListener an eventlistener for the component
	 * @param command the triggered command
	 */
	public void register(JopUiEventListener eventListener, String command) {
		this.eventListener = eventListener;
		this.command = command;
	}
	
	/**
	 * If listener is registered, the JopEvent will be passed 
	 * @param ev the JopEvent to handle
	 * @return True if a redraw is necessary, otherwise false 
	 */
	public boolean distribute(JopEvent ev) {
		if(eventListener != null)  {
			ev.setCommand(command);
			return eventListener.notify(ev) || changed;
		}
		return changed;
	}
	
	/**
	 * Draws the component
	 * @param g graphics object
	 */
	public void paint(Graphics g) {
		if((img == null) || changed)
			create();
		
		g.drawImage(img, bounds.getX(), bounds.getY());
	}
	
	/**
	 * Returns the component identifier
	 * @return type of the component
	 */
	public abstract int getComponentType();
	
}
