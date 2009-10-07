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
 * Jop Font is a font with ascii characters, drawn to a 8*10 pixel grid. 
 */
public final class JopFont implements Font {
	
	private static JopFont singleInstance = null;
	
	/**
	 * Width of character grid <p>
	 * Constant value is 8
	 */
	public static final int JOPFONT_WIDTH = 8;
	
	/**
	 * Height of character grid <p>
	 * Constant value is 10
	 */
	public static final int JOPFONT_HEIGHT = 10;
	
	/* 
	 * every character has size 8*10, and is stored to an integer array of length 3 
	 * some characters have 0x0 as 2nd or 3rd element, so this will not be saved. it will be extended in drawChar
	 */
	private static int [][] fontData = {
		{0x427e0000,0x7e424242}, 		//null box
		{0x0,0x0,0x0}, 					//space
		{0x8080800, 0x8000808}, 		//exclamation mark
		{0x141414}, 					//quote
		{0x3e141400, 0x14143e14}, 		//number sign
		{0x20a3c08, 0x1e28201c, 0x8}, 	//dollar 
		{0x12254200, 0x42a448}, 		//percent
		{0x18242418, 0x4c322254}, 		//and
		{0x80808}, 						//semi_quote
		{0x8081000, 0x10080808}, 		//left parenthesis
		{0x10100800, 0x8101010}, 		//right parenthesis
		{0x1c2a0800, 0x82a1c08}, 		//asterisk
		{0x8080000, 0x8083e},  			//plus
		{0x0, 0x18180000, 0x810}, 		//comma
		{0x0, 0x3e}, 					//minus
		{0x0, 0x18180000}, 				//dot
		{0x8101000, 0x4040808}, 		//slash 
		{0x32221c00, 0x1c22262a}, 		//0
		{0x8080c00, 0x3e080808}, 		//1
		{0x20221c00, 0x3e040810}, 		//2
		{0x20201e00, 0x1e20201c}, 		//3
		{0x4081000, 0x8083e0a}, 		//4
		{0x2023e00, 0x1e20201e}, 		//5
		{0x2221c00, 0x1c22221e}, 		//6
		{0x20223e00, 0x8081010}, 		//7
		{0x22221c00, 0x1c22221c}, 		//8
		{0x22221c00, 0x1c22203c}, 		//9
		{0x18000000, 0x18180018}, 		//colon
		{0x18000000, 0x18180018, 0x810},//semicolon  
		{0xc300000, 0x300c02}, 			//less
		{0x3e000000, 0x3e00}, 			//equals
		{0x18060000, 0x61820}, 			//greater
		{0x60663c00, 0x18001830}, 		//question mark
		{0x99423c00, 0x279a5a5, 0x7c}, 	//at
		{0x42423c00, 0xe742427e}, 		//a
		{0x42423f00, 0x3f42423e}, 		//b
		{0x2423c00, 0x3c420202}, 		//c
		{0x42423f00, 0x3f424242}, 		//d
		{0x2427f00, 0x7f42021e}, 		//e
		{0x2427f00, 0x702021e}, 		//f
		{0x1817e00, 0x7e8181f1}, 		//g
		{0x4242e700, 0xe742427e}, 		//h
		{0x8081c00, 0x1c080808}, 		//i
		{0x4040e000, 0x3c424040}, 		//j
		{0x12227700, 0x7722120e}, 		//k
		{0x2020700, 0x7f424202}, 		//l
		{0x66664200, 0xe7425a5a}, 		//m
		{0x4a46e200, 0x4762524a}, 		//n
		{0x42423c00, 0x3c424242}, 		//o
		{0x42423f00, 0x702023e}, 		//p
		{0x42423c00, 0xfc624242}, 		//q
		{0x42423f00, 0xe742423e}, 		//r
		{0x2027c00, 0x3e40403c}, 		//s
		{0x8497f00, 0x1c080808}, 		//t
		{0x4242e700, 0x3c424242}, 		//u
		{0x22227700, 0x8081414}, 		//v
		{0x22227700, 0x1c2a2a2a}, 		//w
		{0x14227700, 0x77221408}, 		//x
		{0x14227700, 0x1c080808}, 		//y
		{0x10227e00, 0x7e420408}, 		//z
		{0x8081800, 0x18080808}, 		//left bracket
		{0x8040400, 0x10100808}, 		//backslash
		{0x10101800, 0x18101010}, 		//right bracket
		{0x221408}, 					//circumflex
		{0x0, 0x0, 0xff},	 			//underscore
		{0x100804}, 					//accent
		{0x0, 0x5c22223c}, 				//A
		{0x2020200, 0x3e42423e}, 		//B 
		{0x0, 0x3c02023c}, 				//C
		{0x40404000, 0x7c42427c},		//D
		{0x1c000000, 0x1c023e22},		//E
		{0x4241800, 0x404040e, 0x4},	//F
		{0x0, 0x7c42423c, 0x3e40},		//G
		{0x4040400, 0x2424241c},		//H
		{0x8000000, 0x8080800},			//I
		{0x8000000, 0x8080c00, 0x408},	//J
		{0x24040400, 0x24140c14},		//K
		{0x4040400, 0x8040404},			//L
		{0x0, 0x2a2a2a16},				//M
		{0x0, 0xa0a0a06},				//N
		{0x0, 0x1c22221c},				//O
		{0x0, 0x3c44443c, 0x404},		//P
		{0x0, 0x3c22223c, 0x2020},		//Q
		{0x0, 0x4042c14},				//R
		{0x38000000, 0x1c201804},		//S
		{0x1c080000, 0x10080808},		//T
		{0x0, 0x38242424},				//U
		{0x0, 0x8141422},				//V
		{0x0, 0x142a2a2a},				//W
		{0x0, 0x24181824},				//X
		{0x0, 0x10282844, 0x810},		//Y
		{0x0, 0x3c08103c},				//Z
		{0x8081000, 0x10080804},		//left brace
		{0x8080800, 0x8080808},			//pipe
		{0x10100800, 0x8101020},		//right brace
		{0x0, 0x324c} 					//tilde
	};
	
	/**
	 * Create the JopFont object
	 * @return the JopFont
	 */
	public static JopFont getInstance() {
		if(singleInstance == null)
			singleInstance = new JopFont();
		return singleInstance;
	}
	
	private JopFont() {}
	
	/**
	 * Get the corresponding integer array for the character  
	 * @param c specific character
	 * @return bitmap character or a null box for not drawable chars
	 */
	public int[] getChar(char c) {
		if(c < 0x20 || c > 0x7F) { //not drawable chars
			return fontData[0];
		}
		else{
			return fontData[c-0x1F];
		}
	}

	/**
	 * Get the standard character height 
	 * @return character height
	 */
	public int getHeight() {
		return JOPFONT_HEIGHT;
	}

	/**
	 * Get the height for the specific character 
	 * @param c specific character
	 * @return height of the character
	 */
	public int getHeight(char c) {
		return JOPFONT_HEIGHT;
	}

	/**
	 * Get the standard character width  
	 * @return character width
	 */
	public int getWidth() {
		return JOPFONT_WIDTH;
	}

	/**
	 * Get the width for the specific character 
	 * @param c specific character
	 * @return width of the character
	 */
	public int getWidth(char c) {
		return JOPFONT_WIDTH;
	}
}
