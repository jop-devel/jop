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

package com.jopdesign.jopui.event;

/**
 * Implementation for Mouse Events
 */
public class MouseEvent extends JopEvent {

	/**
	 * If mouse button is down <p>
	 * Constant has the value 0
	 */
	public static final int MOUSE_DOWN = 0;
	
	/**
	 * If mouse button is up <p>
	 * Constant has the value 1
	 */
	public static final int MOUSE_UP = 1;
	
	/**
	 * Eventstate for left mouse button <p>
	 * Constant has the value 0 
	 */
	public static final int LEFT_BUTTON = 0;
	
	/**
	 * Eventstate for middle mouse button <p>
	 * Constant has the value 1 
	 */
	public static final int MIDDLE_BUTTON = 1;
	
	/**
	 * Eventstate for right mouse button <p>
	 * Constant has the value 2 
	 */
	public static final int RIGHT_BUTTON = 2;
	
	private int x;
	private int y;
	private int action;
	private int button;
	
	/**
	 * Creates a new MouseEvent
	 * @param x x-coordinate where mouse is pointing
	 * @param y y-coordinate where mouse is pointing
	 * @param action action has to be MOUSE_UP or MOUSE_DOWN
	 * @param button button has to be LEFT_BUTTON or MIDDLE_BUTTON or RIGHT_BUTTON 
	 */
	public MouseEvent(int x, int y, int action, int button) {
		this.y = y;
		this.x = x;
		this.action = action;
		this.button = button;
	}
	
	/**
	 * Return the x-coordinate of the mouse pointer
	 * @return x-coordinate
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Return the y-coordinate of the mouse pointer
	 * @return y-coordinate
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Returns the action for the mouse key
	 * @return MOUSE_UP or MOUSE_DOWN
	 */
	public int getAction() {
		return action;
	}
	
	/**
	 * Returns the used mouse button
	 * @return LEFT_BUTTON or MIDDLE_BUTTON or RIGHT_BUTTON
	 */
	public int getButton() {
		return button;
	}
	
	/**
	 * Returns the type of the event <p>
	 * @see com.jopdesign.jopui.event.JopEvent#getEventType()
	 */
	public int getEventType() {
		return JopEvent.MOUSE_EVENT;
	}

}
