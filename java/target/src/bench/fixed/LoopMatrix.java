/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

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
package fixed;

import jembench.*;

/**
 * Run JemBench benchmarks with a fixed iteration count.
 * 
 * @author martin
 *
 */
public class LoopMatrix {
	
	public static final int FREQ = 60000000;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		EnumeratedParallelBenchmark bench = new jembench.parallel.MatrixMul();
		System.out.println(bench);
		int t1 = (int) System.currentTimeMillis();
		int units = bench.getNrOfUnits();
		for (int i=0; i<100; ++i) {
			for (int j=0; j<units; ++j) {
				bench.executeUnit(j);
			}
		}
		int t2 = (int) System.currentTimeMillis();
		System.out.print(t2-t1);			
		System.out.println(" ms");
		System.out.print((t2-t1)*(FREQ/1000));
		System.out.println(" cycles");

		// TODO Auto-generated method stub

	}

}
