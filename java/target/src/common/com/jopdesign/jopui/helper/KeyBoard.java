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

import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

/**
PS2 Keyboard driver

Features:	Read SCAN Code
			Read scan code which has been converted to an ASCII sign
			Read controle register
**/
public final class KeyBoard  {

	//Bit masks in ctrl register
	public static final int MSK_PARITY_ERR = 0x01;
	public static final int MSK_ASCII_RDY = 0x02;
	public static final int MSK_SND_RDY = 0x04;
	public static final int MSK_CAPS_LOCK = 0x08;
	public static final int MSK_SCC_RDY = 0x10;
	public static final int MSK_KEY_REL = 0x20;
	
	/**
		Reads from the KB_CTRL Register which provides status information like occured parity error or newly received data
	**/
	final public int getCtrlReg() {
		return Native.rd(Const.KB_CTRL);
	}
	
	/**
		Reads from the KB_DATA Register which provides the converted Scan Code
		Does NOT check if new data is available
	**/
	final public int getAscii() {
			return Native.rd(Const.KB_DATA);
	}
	
	/**
		Reads from the KB_DATA Register which provides the converted Scan Code
		Single probes the KB_CTRL register, returns 0 if no new word is ready
	**/
	final public int tryAscii() {
		if((Native.rd(Const.KB_CTRL) & MSK_ASCII_RDY) != 0) {
			return Native.rd(Const.KB_DATA);
		}
		
		return 0;
	}
		
	/**
		Reads from the KB_DATA Register which provides the converted Scan Code
		Polls the CTRL Register untill new data has been received
	**/
	final public int pollAscii() {
		while((Native.rd(Const.KB_CTRL) & MSK_ASCII_RDY) == 0);
			return Native.rd(Const.KB_DATA);
	}
	
	
	
	/**
		Reads from the KB_SCANCODE Register which provides the received Scan Code
		Does NOT check if new data is available
	**/
	final public int getScanCode() {
		return Native.rd(Const.KB_SCANCODE);
	}
	
	/**
		Reads from the KB_SCANCODE Register which provides the converted Scan Code
		Single probes the KB_CTRL register, returns 0 if no new word is ready
	**/
	final public int tryScanCode() {
		if((Native.rd(Const.KB_CTRL) & MSK_SCC_RDY) != 0) {
			return Native.rd(Const.KB_DATA);
		}
		
		return 0;
	
	}
	
	/**
		Reads from the KB_SCANCODE Register which provides the converted Scan Code
		Polls the CTRL Register untill new data has been received
	**/
	final public int pollScanCode() {
		while((Native.rd(Const.KB_CTRL) & MSK_SCC_RDY) == 0);
			return Native.rd(Const.KB_SCANCODE);
	}
	
	


}
