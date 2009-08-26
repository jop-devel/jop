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

/**
 * Class holding images for the UI
 */
public class ImageBox extends Component {

	/**
	 * Creates a new ImageBox
	 * 
	 * @param x x-coordinate of the upper left corner in pixel
	 * @param y x-coordinate of the upper left corner in pixel
	 * @param width width of the imagebox in pixel
	 * @param height height of the imagebox in pixel
	 * @param img the image hold by the imagebox
	 */
	public ImageBox(int x, int y, int width, int height, Image img) {
		super.bounds = new Bounds(x, y, width, height);
		this.img = img;
	}
	
	/**
	 * Creates a new ImageBox
	 * 
	 * @param bounds the boundaries of the imagebox
	 * @param img the image hold by the imagebox
	 */
	public ImageBox(Bounds bounds, Image img) {
		super.bounds = bounds;
		this.img = img;
	}
	
	protected Image create() {
		return img;
	}
	
	/**
	 * Returns the type of component
	 */
	public int getComponentType() {
		return IMAGEBOX;
	}
	
	/**
	 * Draw the image
	 * @see com.jopdesign.jopui.core.Component#paint(Graphics)
	 */
	public void paint(Graphics g) {
		if(img == null)
			return;

		Graphics.disable(Graphics.COLOR_KEY);
		g.drawImage(img, bounds.getX(), bounds.getY());
		Graphics.enable(Graphics.COLOR_KEY);
	}
	
	/**
	 * Returns the stored image
	 * @return stored image
	 */
	public Image getImage() {
		return img;
	}

	/**
	 * Store an image
	 * @param img image to be stored
	 */
	public void setImage(Image img) {
		this.img = img;
	}
	
}
