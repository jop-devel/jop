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

/**
 * Implements a group of option elements
 */
public class OptionGroup {

	private Option cur = null;
	
	/**
	 * Creates a new OptionGroup
	 */
	public OptionGroup() {
	}
	
	/**
	 * Select an option element from the group
	 * @param o option element
	 */
	public void sendMark(Option o) {
	
		if(o == cur)	// if the focus doesn't change return
			return;
			
		if(cur != null) // if the current object is marked, unmark it
			cur.setState(Option.UNMARKED);
	
		cur = o;	// make the new object current
	}
}
