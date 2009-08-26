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
 * Implementation of an 8-bit color
 */
public class Color8Bit implements Color {
	/**
	 * Bit mask for red <p>
	 * Constant has the value 0x000000E0
	 */
	public static final int RED_MASK = 0x000000E0;
	
	/**
	 * Bit mask for green <p>
	 * Constant has the value 0x0000001C
	 */
	public static final int GREEN_MASK = 0x0000001C;
	
	/**
	 * Bit mask for blue <p> 
	 * Constant has the value 0x00000003
	 */
	public static final int BLUE_MASK = 0x00000003;
	
	/**
	 * Bits used for red <p>
	 * Constant has the value 3
	 */
	public static final int BITS_RED = 3;
	
	/**
	 * Bits used for green <p>
	 * Constant has the value 3
	 */
	public static final int BITS_GREEN = 3;
	
	/**
	 * Bits used for blue <p>
	 * Constant has the value 2
	 */
	public static final int BITS_BLUE = 2;
	
	/**
	 * Bits used for color depth <p>
	 * Constant has the value 8
	 */
	public static final int COLOR_DEPTH = 8;
	
	/**
	 * position of bits for red
	 * Constant has the value 5
	 */
	public static final int OFFSET_RED = 5;
	
	/**
	 * position of bits for green
	 * Constant has the value 2
	 */
	public static final int OFFSET_GREEN = 2;
	
	/**
	 * position of bits for blue
	 * Constant has the value 0
	 */
	public static final int OFFSET_BLUE = 0;
	
	/**
	 * Color Black
	 * Constant has the value 0x00000000
	 */
	public static final int BLACK 	= 0x00000000;
	
	/**
	 * Color white
	 * Constant has the value 0x000000FF
	 */
	public static final int WHITE	= 0x000000FF;
	
	/**
	 * Color red
	 * Constant has the value 0x000000E0
	 */
	public static final int RED 	= 0x000000E0;
	
	/**
	 * Color green
	 * Constant has the value 0x0000001C
	 */
	public static final int GREEN 	= 0x0000001C;
	
	/**
	 * Color blue
	 * Constant has the value 0x00000002
	 */
	public static final int BLUE 	= 0x00000002;
	
	private int color = 0;
	
	/**
	 * Creates a new Color
	 * @param red red value
	 * @param green green value
	 * @param blue blue value
	 */
	public Color8Bit(int red, int green, int blue) {
		this.color = Color8Bit.convert(red,green,blue);
	}
	
	/**
	 * Creates a new Color
	 * @param color color value
	 */
	public Color8Bit(int color) {
		this.color = Color8Bit.convert(color);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#getAlphaMask()
	 */
	public int getAlphaMask() {
		return 0;
	}

	/**
	 * @see com.jopdesign.jopui.helper.Color#getBitsAlpha()
	 */
	public int getBitsAlpha() {
		return 0;
	}

	/**
	 * @see com.jopdesign.jopui.helper.Color#getBitsBlue()
	 */
	public int getBitsBlue() {
		return BITS_BLUE;
	}

	/**
	 * @see com.jopdesign.jopui.helper.Color#getBitsGreen()
	 */
	public int getBitsGreen() {
		return BITS_GREEN;
	}

	/**
	 * @see com.jopdesign.jopui.helper.Color#getBitsRed()
	 */
	public int getBitsRed() {
		return BITS_RED;
	}

	/**
	 * @see com.jopdesign.jopui.helper.Color#getBlueMask()
	 */
	public int getBlueMask() {
		return BLUE_MASK;
	}

	/**
	 * @see com.jopdesign.jopui.helper.Color#getColorDepth()
	 */
	public int getColorDepth() {
		return COLOR_DEPTH;
	}

	/**
	 * @see com.jopdesign.jopui.helper.Color#getGreenMask()
	 */
	public int getGreenMask() {
		return GREEN_MASK;
	}

	/**
	 * @see com.jopdesign.jopui.helper.Color#getRedMask()
	 */
	public int getRedMask() {
		return RED_MASK;
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#setRed(int)
	 */
	public void setRed(int value) {
		color = Color8Bit.setRed(color, value);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#setGreen(int)
	 */
	public void setGreen(int value) {
		color = Color8Bit.setGreen(color, value);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#setBlue(int)
	 */
	public void setBlue(int value) {
		color = Color8Bit.setBlue(color, value);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#setAlpha(int)
	 */
	public void setAlpha(int value) {
		return;
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#setColor(int)
	 */
	public void setColor(int color) {
		color = Color8Bit.convert(color);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#setColor(int, int, int)
	 */
	public void setColor(int red, int green, int blue) {
		color = Color8Bit.convert(red, green, blue);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#setColor(int, int, int, int)
	 */
	public void setColor(int red, int green, int blue, int alpha) {
		color = Color8Bit.convert(color);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#getRed()
	 */
	public int getRed() {
		return Color8Bit.getRed(color);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#getGreen()
	 */
	public int getGreen() {
		return Color8Bit.getGreen(color);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#getBlue()
	 */
	public int getBlue() {
		return Color8Bit.getBlue(color);
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#getAlpha()
	 */
	public int getAlpha() {
		return 0;
	}
	
	/**
	 * @see com.jopdesign.jopui.helper.Color#getColor()
	 */
	public int getColor() {
		return color;
	}
	
	/**
	 * Converts a color to an 8 bit color
	 * @param color color to convert 
	 * @return converted color
	 */
	public static int convert(int color) {
		return (color & (RED_MASK | GREEN_MASK | BLUE_MASK));
	}
	
	/**
	 * Converts a color to an 8 bit color
	 * @param r red value
	 * @param g green value
	 * @param b blue value
	 * @return converted color
	 */
	public static int convert(int r, int g, int b) {
		int color = 0;
		color |= (RED_MASK & (r << (OFFSET_RED)));
		color |= (GREEN_MASK & (g << (OFFSET_GREEN)));
		color |= (BLUE_MASK & b);
		return color;
	}
	
	/**
	 * Set the red value of the color
	 * @param color color to set 
	 * @param value red value
	 * @return return color
	 */
	public static int setRed(int color, int value) {
		color &= (~RED_MASK);
		color |= (RED_MASK & (value << (OFFSET_RED)));
		return color;
	}
	
	/**
	 * Set the green value of the color
	 * @param color color to set 
	 * @param value green value
	 * @return return color
	 */
	public static int setGreen(int color, int value) {
		color &= (~GREEN_MASK);
		color |= (GREEN_MASK & (value << (OFFSET_GREEN)));
		return color;
	}
	
	/**
	 * Set the blue value of the color
	 * @param color color to set 
	 * @param value blue value
	 * @return return color
	 */
	public static int setBlue(int color, int value) {
		color &= (~BLUE_MASK);
		color |= (BLUE_MASK & value);
		return color;
	}
	
	/**
	 * Returns red component
	 * @param color for that color
	 * @return red component
	 */
	public static int getRed(int color) {
		return ((color & RED_MASK) >> (OFFSET_RED));
	}
	
	/**
	 * Returns green component
	 * @param color for that color
	 * @return green component
	 */
	public static int getGreen(int color) {
		return ((color & GREEN_MASK) >> (OFFSET_GREEN));
	}
	
	/**
	 * Returns blue component
	 * @param color for that color
	 * @return blue component
	 */
	public static int getBlue(int color) {
		return (color & BLUE_MASK);
	}
}
