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
 * Implementation for keyboard events
 */

public class KeyboardEvent extends JopEvent{

	/**
	 * For pressed keys<p>
	 * Constant has the value 0
	 */
	public static int KEY_PRESSED = 0;
	
	/**
	 * For released keys<p>
	 * Constant has the value 1
	 */
	public static int KEY_RELEASED = 1;
	
	private int action;
	private char character;
	private int scancode;
	
	/**
	 * Creates a new KeyboardEvent
	 * @param action has to be KEY_PRESSED or KEY_RELEASED
	 * @param character ascii character for the key
	 * @param scancode scancode for the key
	 */
	public KeyboardEvent(int action, char character, int scancode){
		this.action = action;
		this.character = character;
		this.scancode = scancode;
	}
	
	/**
	 * Returns the action for the key
	 * @return KEY_PRESSED or KEY_RELEASED
	 */
	public int getAction() {
		return action;
	}

	/**
	 * Returns the ascii character for the key  
	 * @return ascii character
	 */
	public char getCharacter() {
		return character;
	}

	/**
	 * Returns the scancode for the key
	 * @return scancode
	 */
	public int getScanCode() {
		return scancode;
	}

	/**
	 * Returns the type of the event <p>
	 * @see com.jopdesign.jopui.event.JopEvent#getEventType()
	 */
	public int getEventType() {
		return JopEvent.KEYBOARD_EVENT;
	}

}
