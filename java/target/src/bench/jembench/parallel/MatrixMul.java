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
package jembench.parallel;

import jembench.EnumeratedParallelBenchmark;

/**
 * Matrix multiplication is very easy to parallelize.
 * 
 * @author martin
 *
 */
public class MatrixMul extends EnumeratedParallelBenchmark {

	final static int N = 20;
	
	private int[][] arrayA;
	private int[][] arrayB;
	private int[][] arrayC;

	public static int rowCounter = 0;
	public static int endCalculation = 0;
	
	public MatrixMul() {
		arrayA = new int[N][N];
		arrayB = new int[N][N];
		arrayC = new int[N][N];
		int val = 0;
		// set some values in the source matrices
		for (int i=0; i<N; ++i) {
			for (int j=0; j<N; ++j) {
				arrayA[i][j] = val;
				val += 12345;
				arrayB[i][j] = val;
				val += 67890;
			}
		}
	}
	
	public String toString() {

		return "matrix multiplication";
	}
	/**
	 * Here comes the workload. Do one vector multiplication.
	 */
	public void executeUnit(int nr) {
		
		int i, j, val;
		int[] colB;
				
		for (i = 0; i < N; i++) { // column
			val = 0;
			colB = arrayB[i];
			for (j = 0; j < N; j++) {
				val += arrayA[j][nr] * colB[j];
			}
			arrayC[i][nr] = val;
		}
	}
	


	/**
	 * return number of independent tasks
	 */
	public int getNrOfUnits() {
		return N;
	}

}
