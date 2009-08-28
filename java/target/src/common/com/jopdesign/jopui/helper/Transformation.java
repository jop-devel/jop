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
package com.jopdesign.jopui.helper;

import com.jopdesign.jopui.core.Image;

/**
 * Transformation for images
 */
public class Transformation {
	/**
	 * No transformation<p>
	 * Constant has the value 0
	 */
	public static final int TRANS_NONE = 0;
	
	/**
	 * Mirror and rotate 180 degrees <p>
	 * Constant has the value 1
	 */
	public static final int TRANS_MIRROR_ROT180 = 1;
	
	/**
	 * Mirror image  <p>
	 * Constant has the value 2
	 */
	public static final int TRANS_MIRROR = 2;
	
	/**
	 * Rotate 180 degrees <p>
	 * Constant has the value 3
	 */
	public static final int TRANS_ROT180 = 3;
	
	/**
	 * Mirror and rotate 270 degrees <p>
	 * Constant has the value 4
	 */
	public static final int TRANS_MIRROR_ROT270 = 4;
	
	/**
	 * Rotate 90 degrees <p>
	 * Constant has the value 5
	 */
	public static final int TRANS_ROT90 = 5;
	
	/**
	 * Rotate 270 degrees <p>
	 * Constant has the value 6
	 */
	public static final int TRANS_ROT270 = 6;
	
	/**
	 * Mirror and rotate 90 degrees <p>
	 * Constant has the value 7
	 */
	public static final int TRANS_MIRROR_ROT90 = 7;
	
	/**
	 * Rotate 90 degrees
	 * @param image source
	 * @return new image
	 */
	public static Image rotate90(Image image) {
		if(image == null)
			return null;
		int ow = image.getWidth();
		int oh = image.getHeight();
		int nw = oh;
		int nh = ow;
		int [] oldData = new int[ow*oh];
		int [] newData = new int[nw*nh];
		image.getRGB(oldData, 0, ow, 0, 0, ow, oh);
		for(int i=0; i<oh; ++i) {
			for(int j=0; j<ow; ++j) {
				newData[nw-1-i + j*nw] = oldData[j + i*ow];
			}
		}
		return Image.createRGBImage(newData, nw, nh);
	}
	
	/**
	 * Rotate 180 degrees
	 * @param image source
	 * @return new image
	 */
	public static Image rotate180(Image image) {
		if(image == null)
			return null;
		int w = image.getWidth();
		int h = image.getHeight();
		int [] oldData = new int[w*h];
		int [] newData = new int[w*h];
		image.getRGB(oldData, 0, w, 0, 0, w, h);
		for(int i=0; i<h; ++i) {
			for(int j=0; j<w; ++j) {
				newData[w-1-j + (h-1-i)*w] = oldData[j + i*w];
			}
		}
		return Image.createRGBImage(newData, w, h);
	}
	
	/**
	 * Rotate 270 degrees
	 * @param image source
	 * @return new image
	 */
	public static Image rotate270(Image image) {
		if(image == null)
			return null;
		int ow = image.getWidth();
		int oh = image.getHeight();
		int nw = oh;
		int nh = ow;
		int [] oldData = new int[ow*oh];
		int [] newData = new int[nw*nh];
		image.getRGB(oldData, 0, ow, 0, 0, ow, oh);
		for(int i=0; i<oh; ++i) {
			for(int j=0; j<ow; ++j) {
				newData[(i) + (nh-1-j)*nw] = oldData[j + i*ow];
			}
		}
		return Image.createRGBImage(newData, nw, nh);
	}
	
	/**
	 * Mirror
	 * @param image source
	 * @return new image
	 */
	public static Image mirror(Image image) {
		if(image == null)
			return null;
		int w = image.getWidth();
		int h = image.getHeight();
		int [] oldData = new int[w*h];
		int [] newData = new int[w*h];
		image.getRGB(oldData, 0, w, 0, 0, w, h);
		for(int i=0; i<h; ++i) {
			for(int j=0; j<w; ++j) {
				//newData[j+ (h-1-i)*w] = oldData[j + i*w]; // horizontal mirror
				newData[w-1-j + i*w] = oldData[j + i*w];
			}
		}
		return Image.createRGBImage(newData, w, h);
	}
	
	/**
	 * Mirror and Rotate 90 degrees
	 * @param image source
	 * @return new image
	 */
	public static Image mirrorRotate90(Image image) {
		if(image == null)
			return null;
		return rotate90(mirror(image));
	}
	
	/**
	 * Mirror and Rotate 180 degrees
	 * @param image source
	 * @return new image
	 */
	public static Image mirrorRotate180(Image image) {
		if(image == null)
			return null;
		return rotate180(mirror(image));
	}
	
	/**
	 * Mirror and Rotate 270 degrees
	 * @param image source
	 * @return new image
	 */
	public static Image mirrorRotate270(Image image) {
		if(image == null)
			return null;
		return rotate270(mirror(image));
	}
}
