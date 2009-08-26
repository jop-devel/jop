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

import com.jopdesign.jopui.helper.Transformation;

/**
 * Image objects holding graphical data.
 */
public class Image {
	
	protected int [] data;	//holding 4 pixel per integer.
	protected int colorKey;
	protected int width;
	protected int height;
	
	protected Image(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = new int [((width * height)>>2)+1];
	}
	
	protected Image(Image copy) {
		this.width = copy.width;
		this.height = copy.height;
		this.data = new int [((width * height)>>2)+1];
		System.arraycopy(copy.data, 0, data, 0, copy.data.length);
		this.colorKey = copy.colorKey;
	}
	
	/**
	 * Creates a new image with a given width and height <p>
	 * Returns null if width or height &#60;= 0
	 * @param width image width in pixel
	 * @param height image height in pixel
	 * @return a new Image object or null if an error occurred
	 */
	public static Image createImage(int width, int height) {
		if(width <= 0 || height <= 0)
			return null;
		return new Image(width, height);
	}
	
	/**
	 * Creates a new image from an other one <p>
	 * Returns null if source is null
	 * @param source image to be copied  
	 * @return a new Image object or null if an error occurred
	 */
	public static Image createImage(Image source) {
		if(source == null)
			return null;
		return new Image(source);
	}
	
	/**
	 * Creates a new image from a specified region of a source image, with the given transformation.<p>
	 * Conditions: <br>
	 * if source == null, null will be returned <br>
	 * if x or y &#60; 0, null will be returned <br>
	 * if width or height &#60;= 0, null will be returned <br>
	 * if x+width &#62; source image width, null will be returned <br>
	 * if y+height &#62; source image height, null will be returned <br>
	 * if transformation is unknown, null will be returned
	 * @param source source image to be copied 
	 * @param x x-coordinate of the upper left corner of the region to be copied
	 * @param y y-coordinate of the upper left corner of the region to be copied
	 * @param width width of the region to be copied
	 * @param height height of the region to be copied
	 * @param transform transformation for the region
	 * @return a new Image object or null if an error occurred 
	 */
	public static Image createImage(Image source, int x, int y, int width, int height, int transform) {
		if(source == null)
			return null;
		if((x < 0) ||  (y < 0) || (width <= 0) || (height <= 0))
			return null;
		if((x+width > source.getWidth()) || (y+height > source.getHeight()))
			return null;
		
		int [] data = new int[width * height];
		source.getRGB(data, 0, source.width, x, y, width, height);
		Image tmp = Image.createRGBImage(data, width, height); 
		
		switch(transform) {
			case Transformation.TRANS_NONE: return tmp;
			case Transformation.TRANS_ROT90: return Transformation.rotate90(tmp);				
			case Transformation.TRANS_ROT180: return Transformation.rotate180(tmp);
			case Transformation.TRANS_ROT270: return Transformation.rotate270(tmp);
			case Transformation.TRANS_MIRROR: return Transformation.mirror(tmp);
			case Transformation.TRANS_MIRROR_ROT90: return Transformation.mirrorRotate90(tmp);
			case Transformation.TRANS_MIRROR_ROT180: return Transformation.mirrorRotate180(tmp);
			case Transformation.TRANS_MIRROR_ROT270: return Transformation.mirrorRotate270(tmp);
			default: return null;
		}
	}
	
	/**
	 * Creates a new image from an array.
	 * @param rgb array holding pixeldata for the image. one pixel per int
	 * @param width image width in pixel
	 * @param height image height in pixel
	 * @return a new Image object or null if an error occurred
	 */
	public static Image createRGBImage(int[] rgb, int width, int height) {		
		if(rgb == null)
			return null;
		if(width <= 0 || height <= 0)
			return null;
		if((width * height) != rgb.length)
			return null;
		
		Image tmp = new Image(width, height);
		int absolute_adr=0;
		for(int i=0; i<rgb.length; i+=4) {
			//save 4 pixel per integer
			tmp.data[absolute_adr++]= rgb[i]<<24 | rgb[i+1]<<16 | rgb[i+2]<<8 | rgb[i+3];     
		}
		return tmp;
	}
	
	/**
	 * Stores images pixel data to the rgbData array.<p>
	 * Conditions: <br>
	 * if rgbData == null, no action will be performed <br>
	 * if x or y or offset &#60; 0, no action will be performed <br>
	 * if width or height &#60;= 0, no action will be performed <br>
	 * if scanlength &#60; width, no action will be performed <br>
	 * if x + width &#62; current image width, no action will be performed <br>
	 * if y + height &#62; current image height, no action will be performed <br>
	 * if width * height + offset &#62; length of rgbData, no action will be performed
	 * 
	 * @param rgbData pixeldata stored to this array. one pixel per integer
	 * @param offset the starting index where pixeldata will be stored in the rgbData array.
	 * @param scanlength 
	 * @param x x-coordinate of the upper left corner of the region to be copied
	 * @param y y-coordinate of the upper left corner of the region to be copied
	 * @param width width of the region to be copied
	 * @param height width of the region to be copied
	 */
	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
		if(rgbData == null)
			return;
		if(x < 0 || y < 0 || width <= 0 || height <= 0 || offset < 0 || scanlength < width)
			return;
		if((x + width > this.width) || (y + height > this.height))
			return;
		if((width*height + offset) > rgbData.length)
			return;
		
		for(int j=y; j<height; ++j) {
			for(int i=x; i<width; ++i) {
				int adr = (i+j*width);
				int absolute_adr = adr>>2;
				int adroffset = 8*(adr & 0x03);
				rgbData[offset + (i-x) + (j-y)*scanlength] = 0xFF & (data[absolute_adr] >> adroffset);
			}
		}
	}
	
	/**
	 * Creates a new Graphics object for the current image
	 * @return a new Graphics object for this image
	 */
	public Graphics getGraphics() {
		return new Graphics(this);
	}
	
	/**
	 * Returns the height of the current image in pixel. 
	 * @return height in pixel
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Returns the width of the current image in pixel. 
	 * @return width in pixel
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Returns the pixel data of the image with 4 pixel per integer 
	 * @return pixel data
	 */
	public int [] getData() {
		return data;
	}

	/**
	 * Sets the colorkey to the color c
	 * @param c colorvalue
	 */
	public void setColorKey(int c) {
		this.colorKey = c;
	}
	
	/**
	 * Returns the colorkey value
	 * @return colorkey color
	 */
	public int getColorKey() {
		return this.colorKey;
	}
}
