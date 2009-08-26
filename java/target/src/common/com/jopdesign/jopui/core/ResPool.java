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

import java.util.Hashtable;

/**
 * Stores references to loaded images. Makes them accessible through string identifiers
 */
public class ResPool {

	private static Hashtable res = new Hashtable();
	
	/**
	 * Add an image with corresponding string to list 
	 * @param identifier string id
	 * @param img image 
	 */
	public static void add(String identifier, Image img) {
		res.put(identifier, img);	
	}
	
	/**
	 * Returns the corresponding image for the string id 
	 * @param identifier string id
	 * @return image
	 */
	public static Image get(String identifier) {
		return (Image)res.get(identifier);
	}
	
	/**
	 * Deletes an image from list
	 * @param identifier image to be deleted
	 */
	public static void delete(String identifier) {
		res.remove(identifier);	
	}
}
