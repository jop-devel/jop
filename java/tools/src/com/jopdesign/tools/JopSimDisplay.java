/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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


/**
*	JopSim.java
*
*	Simulation of JOP JVM.
*
*		difference between JOP and JopSim:
*			loadBc (and invokestatic)
*
*		2001-12-03	I don't need a fp!?
*/

package com.jopdesign.tools;

import java.io.*;


import com.jopdesign.sys.*;

public class JopSimDisplay extends JopSim {
	
	// Display
	JopDisplay sd;

	JopSimDisplay(String fn, IOSimMin ioSim, int max) {
		super(fn, ioSim, max);

		System.out.println("init simulation display");
		sd = new JopDisplay();
		
	}

	JopSimDisplay(String fn, IOSimMin ioSim) {
		super(fn, ioSim, 0);
		System.out.println("init simulation display");
		sd = new JopDisplay();
	}


	int readMem(int addr, Access type) {
//		System.out.println("readMem: 0x" + Integer.toHexString(addr) + " ... " + addr);
		if ((addr == sd.KB_CTRL) || (addr == sd.KB_DATA) || (addr == sd.KB_SCANCODE)
		    || (addr == sd.MOUSE_STATUS) || (addr == sd.MOUSE_FLAG)|| (addr == sd.MOUSE_X_INC)
		    || (addr == sd.MOUSE_Y_INC))
			return sd.read(addr);

		return super.readMem(addr, type);
	}

	void writeMem(int addr, int data, Access type) {
//		System.out.println("writeMem: 0x" + Integer.toHexString(addr) + " ... " + addr);
		if(!sd.write(addr, data))
			super.writeMem(addr, data, type);
	}

	public static void main(String args[]) {

		IOSimMin io;

		int maxInstr = getArgs(args);
		
		String ioDevice = System.getProperty("ioclass");
		
		for (int i=0; i<nrCpus; ++i) {
			// select the IO simulation
			if (ioDevice!=null) {
				try {
					io = (IOSimMin) Class.forName("com.jopdesign.tools."+ioDevice).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					io = new IOSimMin();
				}			
			} else {
				io = new IOSimMin();			
			}
			io.setCpuId(i);
			js[i] = new JopSimDisplay(args[0], io, maxInstr);
		}
		
		runSimulation();
	}

}
