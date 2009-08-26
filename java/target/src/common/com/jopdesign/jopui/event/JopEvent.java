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
 * Events known by the Jop-UI
 */
public abstract class JopEvent {

	private String command = "";
	
	/**
	 * Event id for mouse <p>
	 * Constant has the value 0
	 */
	public static final int MOUSE_EVENT  = 0;
	
	/**
	 * Event id for keyboard <p>
	 * Constant has the value 1
	 */
	public static final int KEYBOARD_EVENT = 1;
	
	/**
	 * Returns the event type
	 * @return event type
	 */
	abstract public int getEventType();
	
	/**
	 * Set the command string
	 * @param command string for the command
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
	/**
	 * Returns the command string
	 * @return command string
	 */
	public String getCommand() {
		return command;
	}
	
}
