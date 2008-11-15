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

import joprt.RtThread;

/**
 * Test program for local scope caching.
 * 
 * @author Christof Pitter, Martin Schoeberl
 * 
 */
public class LocalMatrixCalc extends RtThread {

	// Shared Variables
	public static int[][] arrayA;
	public static int[][] arrayB;
	public static int[][] arrayC;
	public static int rowCounter = 0;
	public static int endCalculation = 0;
	public static final int M = 100; // Number of rows of A
	public static final int N = 100; // Number of columns of A, number of rows of B
	public static final int P = 100; // Number of columns of B	
	static Object lock = new Object();
	static final int PRIORITY = 1;
	static final int PERIOD = 1000;
	
	static final boolean USE_SCOPE = false;
	volatile static boolean go = false;
	
	static int[] stats;

	int cpu_id;

	public LocalMatrixCalc(int identity) {
		super(PRIORITY, PERIOD);
		cpu_id = identity;
	}
	
	// Questions to CP: why abs and %max in initialize?
	// why not a local var in MAC operation?
	// Make a local copy of the working row
	// Use row in second index to avoid dual array access


	static void initializeArrays(long seed) {

		Random r = new Random(seed);

		arrayA = new int[M][N];
		arrayB = new int[N][P];
		arrayC = new int[M][P];

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				arrayA[i][j] = r.nextInt();
			}
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < P; j++) {
				arrayB[i][j] = r.nextInt();
			}
		}

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < P; j++) {
				arrayC[i][j] = 0;
			}
		}
	}

	static int processCalculation() {

		int val;
		int[] colB, localRow;
		int j, i;
		
		int myRow = 0;
		int counter = 0;
		
		localRow = new int[M];

		while (true) {
			synchronized (lock) {
				if (rowCounter == N) {
					break;
				} else {
					myRow = rowCounter;
					rowCounter++;
				}
			}

			for (j = 0; j < M; j++) {
				localRow[j] = arrayA[j][myRow];
			}
			
			for (i = 0; i < P; i++) { // column
				val = 0;
				colB = arrayB[i];
				for (j = 0; j < M; j++) {
					val += localRow[j] * colB[j];
				}
				arrayC[i][myRow] = val;
			}

			synchronized (lock) {
				endCalculation++;
			}

			counter++;
		}
		return counter;
	}

	public void run() {		
		Runnable r = new Runnable() {
			public void run() {
				stats[cpu_id] = processCalculation();				
			}
		};
		PrivateScope scope = null;
		if (USE_SCOPE) {
			scope = new PrivateScope(1000);			
		}
		while (!go) {
			waitForNextPeriod();
		}
		if (USE_SCOPE) {
			scope.enter(r);
		} else {
			r.run();
		}
	}
	
	public static void main(String[] args) {

		// Initialization for benchmarking 
		int start = 0;
		int stop = 0;
		int time = 0;

		System.out.println("Matrix Benchmark:");

		long seed = 13;
		initializeArrays(seed);

		int nrCpu = Runtime.getRuntime().availableProcessors();
		stats = new int[nrCpu];
		// nrCpu = 2;

		for (int i = 0; i < nrCpu; i++) {
			RtThread rtt = new LocalMatrixCalc(i);
			rtt.setProcessor(i);
		}

		// Start threads on this and other CPUs
		RtThread.startMission();

		// give threads time to setup their memory
		RtThread.sleepMs(100);
		System.out.println("Start calculation");
		// Start of measurement
		start = (int) System.currentTimeMillis();
		go = true;

		// This main thread will be blocked till the
		// worker thread has finished

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
		for (int i=0; i<nrCpu; ++i) {
			System.out.println("CPU "+i+" calculated "+stats[i]+" rows");
		}

	}

}