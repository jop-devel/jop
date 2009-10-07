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
 * Simple class holding the bounds of a rectangle.
 */
public class Bounds {
	
	/**
	 * x-coordinate of the upper left point 
	 */
	public int x;
	
	/**
	 * y-coordinate of the upper left point 
	 */
	public int y;
	
	/**
	 * width of the rectangle 
	 */
	public int width;
	
	/**
	 * height of the rectangle
	 */
	public int height;
	
	/**
	 * Creates a new Bound rectangle
	 * @param x x-coordinate of the upper left point 
	 * @param y y-coordinate of the upper left point
	 * @param width width of the rectangle
	 * @param height height of the rectangle
	 */
	public Bounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Returns the x-coordinate of the upper left point
	 * @return x-coordinate in pixel
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Set the x-coordinate of the upper left point
	 * @param x x-coordinate in pixel
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Returns the y-coordinate of the upper left point
	 * @return y-coordinate in pixel
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set the y-coordinate of the upper left point
	 * @param y y-coordinate in pixel
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Returns the width of the rectangle
	 * @return width of the rectangle in pixel
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Set the width of the rectangle
	 * @param width width of the rectangle in pixel
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Returns the height of the rectangle
	 * @return height of the rectangle in pixel
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Set the height of the rectangle
	 * @param height height of the rectangle in pixel
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
	 * Set a complete rectangle
	 * @param x x-coordinate in pixel
	 * @param y y-coordinate in pixel
	 * @param width width in pixel
	 * @param height height in pixel
	 */
	public void set(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
