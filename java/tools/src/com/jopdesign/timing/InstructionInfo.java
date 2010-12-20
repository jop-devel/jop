/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber (benedikt.huber@gmail.com)
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.timing;

import org.apache.bcel.Constants;

/**
 * Class for encapsulating a instruction to be analyzed.
 * Can be specialized for specific processor models.
 */
public class InstructionInfo {
	/** return opcodes are hardcoded: {A,D,F,I,L,_}RETURN */
	public static final int[] RETURN_OPCODES = {
		Constants.ARETURN,
		Constants.DRETURN,
		Constants.FRETURN,
		Constants.IRETURN,
		Constants.LRETURN,
		Constants.RETURN };

	public static boolean isReturnOpcode(int opcode) {
		for(int rop : RETURN_OPCODES) {
			if(rop == opcode) return true;
		}
		return false;
	}
	private int opcode;

	public InstructionInfo(int opcode) {
		this.opcode = opcode;
	}
	public int getOpcode() {
		return opcode;
	}
}