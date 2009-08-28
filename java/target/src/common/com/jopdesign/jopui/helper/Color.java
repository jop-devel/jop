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

/**
 * Interface defining a color
 */
public interface Color {
	/**
	 * Returns the color depth of the color
	 * @return color depth
	 */
	int getColorDepth();
	
	/**
	 * Returns the number of bits used by the red component of the color
	 * @return num. of bits for red
	 */
	int getBitsRed();
	
	/**
	 * Returns the number of bits used by the green component of the color
	 * @return num. of bits for green
	 */
	int getBitsGreen();
	
	/**
	 * Returns the number of bits used by the blue component of the color
	 * @return num. of bits for blue
	 */
	int getBitsBlue();
	
	/**
	 * Returns the number of bits used by the alpha component of the color
	 * @return num. of bits for alpha channel
	 */
	int getBitsAlpha();
	
	/**
	 * Returns the bit mask for the red component
	 * @return bitmask for red
	 */
	int getRedMask();
	
	/**
	 * Returns the bit mask for the green component
	 * @return bitmask for green
	 */
	int getGreenMask();
	
	/**
	 * Returns the bit mask for the blue component
	 * @return bitmask for blue
	 */
	int getBlueMask();
	
	/**
	 * Returns the bit mask for the alpha component
	 * @return bitmask for alpha channel
	 */
	int getAlphaMask();
	
	/**
	 * Returns the red component value
	 * @return red value
	 */
	int getRed();
	
	/**
	 * Returns the green component value
	 * @return green value
	 */
	int getGreen();
	
	/**
	 * Returns the blue component value
	 * @return blue value
	 */
	int getBlue();
	
	/**
	 * Returns the alpha component value
	 * @return alpha value
	 */
	int getAlpha();
	
	/**
	 * Returns the color
	 * @return color value
	 */
	int getColor();
	
	/**
	 * Set the red component of the color
	 * @param value red value
	 */
	void setRed(int value);
	
	/**
	 * Set the green component of the color
	 * @param value green value
	 */
	void setGreen(int value);
	
	/**
	 * Set the blue component of the color
	 * @param value blue value
	 */
	void setBlue(int value);
	
	/**
	 * Set the alpha component of the color
	 * @param value alpha channel value
	 */
	void setAlpha(int value);
	
	/**
	 * Set the color
	 * @param value color value
	 */
	void setColor(int value);
	
	/**
	 * Set the color
	 * @param red red component value
	 * @param green green component value
	 * @param blue blue component value
	 */
	void setColor(int red, int green, int blue);
	
	/**
	 * Set the color
	 * @param red red component value
	 * @param green green component value
	 * @param blue blue component value
	 * @param alpha alpha channel value
	 */
	void setColor(int red, int green, int blue, int alpha);
}
