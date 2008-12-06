/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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
 * 
 */
package com.jopdesign.tools;

/**
 * Extension of JopSim to simulation real-time transactional memory (RTTM)
 * 
 * @author Martin Schoeberl
 *
 */
public class TMSim extends JopSim {

	TMSim(String fn, IOSimMin ioSim, int max) {
		super(fn, ioSim, max);
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {

		IOSimMin io;

		int maxInstr = getArgs(args);
		
		for (int i=0; i<nrCpus; ++i) {
			io = new IOSimMin();			
			io.setCpuId(i);
			js[i] = new JopSim(args[0], io, maxInstr);
			io.setJopSimRef(js[i]);			
		}
		
		runSimulation();
	}

}
