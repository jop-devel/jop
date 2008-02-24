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

package jbe;

import jbe.micro.*;

/**
 * Embedded Java Benchmark - JavaBenchEmbedded
 * 
 * Invoke all benchmarks
 * 
 * Versions:
 * 		V1.0	Used for the JOP thesis and various papers
 * 				Main applications are Kfl and UdpIp
 * 		V1.1	2007-04-11 cleanup of LowLevel - other devices
 * 				are included in the single LowLevel.java in comments
 * 
 * @author admin
 *
 */
public class DoMicro {

	public static void main(String[] args) {

		LowLevel.msg("Micro Benchmarks:");
		LowLevel.lf();

//		Execute.perform(new Fadd());
		Execute.perform(new Add());
		Execute.perform(new Iinc());
		Execute.perform(new Ldc());
		Execute.perform(new BranchTaken());
		Execute.perform(new BranchNotTaken());
		Execute.perform(new GetField());
		Execute.perform(new GetStatic());
		Execute.perform(new Array());
		Execute.perform(new InvokeVirtual());
		Execute.perform(new InvokeStatic());
		Execute.perform(new InvokeInterface());
	}
			
}
