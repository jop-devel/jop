/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Christof Pitter

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

package examples.scopes;

import java.util.Random;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

/**
 * Test program for local scope caching.
 * 
 * @author Christof Pitter, Martin Schoeberl
 * 
 */
public class LocalMatrixCalc implements Runnable {

	// Shared Variables
	public static int[][] arrayA;
	public static int[][] arrayB;
	public static int[][] arrayC;
	public static int rowCounter = 0;
	public static int endCalculation = 0;
	public static int M = 100; // Number of rows of A
	public static int N = 100; // Number of columns of A, number of rows of B
	public static int P = 100; // Number of columns of B	
	static Object lock;

	int cpu_id;

	public LocalMatrixCalc(int identity) {
		cpu_id = identity;
	}


	static void initializeMultiplication(long seed) {
		// Initialize the Arrays
		int max = 10;
		Random r = new Random(seed);

		arrayA = new int[M][N];
		arrayB = new int[N][P];
		arrayC = new int[M][P];

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				arrayA[i][j] = (Math.abs(r.nextInt())) % max;
			}
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < P; j++) {
				arrayB[i][j] = (Math.abs(r.nextInt())) % max;
			}
		}

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < P; j++) {
				arrayC[i][j] = 0;
			}
		}
	}

	static int processCalculation() {
		int myRow = 0;
		int counter = 0;

		while (true) {
			synchronized (lock) {
				if (rowCounter == N) {
					break;
				} else {
					myRow = rowCounter;
					rowCounter++;
				}
			}

			for (int i = 0; i < P; i++) { // column
				arrayC[myRow][i] = 0;

				for (int j = 0; j < M; j++) {
					arrayC[myRow][i] = arrayC[myRow][i] + arrayA[myRow][j]
							* arrayB[j][i];
				}
			}

			synchronized (lock) {
				endCalculation++;
			}

			counter++;
		}
		return counter;
	}

	public void run() {
		int test = processCalculation();
	}
	
	public static void main(String[] args) {

		// Initialization for benchmarking 
		int start = 0;
		int stop = 0;
		int time = 0;

		System.out.println("Matrix Benchmark:");

		long seed = 13;
		initializeMultiplication(seed);

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		int nrCpu = sys.nrCpu;
		nrCpu = 2;

		for (int i = 0; i < nrCpu - 1; i++) {
			Runnable r = new LocalMatrixCalc(i + 1);
			Startup.setRunnable(r, i);
		}

		System.out.println("Start calculation");
		// Start of measurement
		start = (int) System.currentTimeMillis();

		// Start of all other CPUs
		sys.signal = 1;

		// Start of CPU0
		int test0 = processCalculation();

		while (true) {
			synchronized (lock) {
				if (endCalculation == N)
					break;
			}
		}

		// End of measurement
		stop = (int) System.currentTimeMillis();

		System.out.println("StartTime: " + start);
		System.out.println("StopTime: " + stop);
		time = stop - start;
		System.out.println("TimeSpent: " + time);

	}

}