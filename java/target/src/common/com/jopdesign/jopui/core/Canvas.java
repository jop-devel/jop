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

import java.util.Enumeration;
import java.util.Vector;

import com.jopdesign.jopui.event.JopEvent;
import com.jopdesign.jopui.event.KeyboardEvent;

/**
 * Main component for Jop-UI applications to draw components to the screen.
 */
public class Canvas extends Component{
	
	Vector components = null;
	Component focus = null;
	
	/**
	 * Creates a new Canvas component
	 */
	public Canvas() {
	
	}
	
	/**
	 * Add a new component to the canvas
	 * @param component component to add
	 */
	public void add(Component component) {
		if (components == null) {
			components = new Vector();
		}
							// neither labels nor images can have the focus
		if (focus == null && component.getComponentType() != Component.LABEL && component.getComponentType() != Component.IMAGEBOX) {
			focus = component;
			focus.setFocus(true);
		}
		
		component.setCanvas(this);
		components.addElement(component);
	}
	
	/**
	 * Set the focus to the specific component
	 * @param c component which get the focus
	 */
	public void setComponentFocus(Component c) {
		focus.setFocus(false);
		focus = c;
		focus.setFocus(true);
	}
	
	/**
	 * passes all events to the specific components <p>
	 * @see com.jopdesign.jopui.core.Component#distribute(JopEvent)
	 */
	public boolean distribute(JopEvent ev) {
		if(components == null)
			return false;
		
		boolean ret = false;
		
		if(ev.getEventType() == JopEvent.KEYBOARD_EVENT) { // distribute KEYBOARD_EVENTS in special way (focus)
		
			KeyboardEvent kbev = (KeyboardEvent) ev;			
			
			if(focus != null && 
			    kbev.getAction() == KeyboardEvent.KEY_PRESSED &&
				kbev.getCharacter() == 0x09) {

				ret = true;
				
				focus.setFocus(false);
				do {
					int pos = components.indexOf(focus) + 1;
					if(pos >= components.size())
						pos = 0;
					focus = (Component) components.elementAt(pos);
				} while (focus.getComponentType() == Component.LABEL || focus.getComponentType() == Component.IMAGEBOX);
				focus.setFocus(true);
				
			} else {
				ret |= focus.distribute(ev);
			}
			
		} else {	// all others go everywhere
			
			Enumeration e = components.elements();
			Component c;
		
			while(e.hasMoreElements()) {
				c = (Component) e.nextElement();
				ret |= c.distribute(ev);
			}
		}
		
		ret |= super.distribute(ev);
		
		return ret;
	}
	
	/**
	 * Override the method from component class
	 * @see com.jopdesign.jopui.core.Component#paint(Graphics)
	 */
	public void paint(Graphics g) {
		if(components == null)
			return;
		
		Enumeration e = components.elements();
		Component c;
				
		while(e.hasMoreElements()) {
			c = (Component) e.nextElement();
			c.paint(g);
			
		}
	}

	/**
	 * Returns the type of the component
	 * @see com.jopdesign.jopui.core.Component#getComponentType()
	 */
	public int getComponentType() {
		return CANVAS;
	}
}
