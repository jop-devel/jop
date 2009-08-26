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
 * Needed methods for every font.
 */
public interface Font {
	/**
	 * Get the standard character width  
	 * @return character width
	 */
	public int getWidth();
	
	/**
	 * Get the standard character height 
	 * @return character height
	 */
	public int getHeight();
	
	/**
	 * Get the width for the specific character 
	 * @param c specific character
	 * @return width of the character
	 */
	public int getWidth(char c);
	
	/**
	 * Get the height for the specific character 
	 * @param c specific character
	 * @return height of the character
	 */
	public int getHeight(char c);
	
	/**
	 * Get the corresponding integer array for the character  
	 * @param c specific character
	 * @return bitmap character
	 */
	public int [] getChar(char c);
}