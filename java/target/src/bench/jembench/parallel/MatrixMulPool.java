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

import jembench.NumeratedParallelPool;

/**
 * Matrix multiplication is very easy to parallelize.
 * 
 * @author martin, tom
 * 
 */
public class MatrixMulPool extends NumeratedParallelPool {

	private final static int N = 20;

	private final int[][] arrayA;
	private final int[][] arrayB;
	private final int[][] arrayC;
	private int todo;

	public MatrixMulPool() {
		arrayA = new int[N][N];
		arrayB = new int[N][N];
		arrayC = new int[N][N];
		int val = 0;
		// set some values in the source matrices
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
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

	public int getCheckSum() {
		int sum = 0;
		for (int i = N; --i >= 0;) {
			for (int j = N; --j >= 0;) {
				sum += ((i ^ j) + 1) * arrayC[i][j];
			}
		}
		return sum;
	}

	protected int getSize() {
		return N;
	}

	protected void execute(int row) {
		final int[] rowA = arrayA[row];
		for (int col = 0; col < N; col++) {
			int val = 0;
			for (int i = 0; i < N; i++) {
				val += rowA[i] * arrayB[i][col];
			}
			arrayC[row][col] = val;
		}
	}
}
