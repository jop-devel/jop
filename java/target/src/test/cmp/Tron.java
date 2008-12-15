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
package cmp;

import java.util.Random;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

/**
 * @author Michael Muck
 * cmp version
 */
public class Tron {
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static final int EMPTY = -1;
	
	static final int SIZE = 14;
	static int array[][] = new int[SIZE][SIZE];
	
	static boolean running[] = new boolean[sys.nrCpu];
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// reset running array
		for(int i=0; i<sys.nrCpu; ++i) {
			running[i] = true;
		}		
		
		// set array empty			
		for(int y=0; y<SIZE; y++) {
			for(int x=0; x<SIZE; x++) {
				array[x][y] = EMPTY;
			}
		}
		
		// Initialize Players
		TronRunner s[] = new TronRunner[sys.nrCpu];		
				
		Random r = new Random();
		for (int i=0; i<sys.nrCpu; ++i) {
			int x,y;
			// get a start point for this player
			do {
				x = Math.abs(r.nextInt()%SIZE);
				y = Math.abs(r.nextInt()%SIZE);
			} while(array[x][y] != EMPTY);

			array[x][y] = i;
			if(i < sys.nrCpu-1) {
				s[i] = new TronRunner(i, x, y);
				Startup.setRunnable(s[i], i);
			}
			else {
				s[i] = new TronRunner(sys.nrCpu-1, x, y);
			}
		}			
		
		// print initial game table
		printGameTable();
		
		// start the other CPUs
		sys.signal = 1;

		s[sys.nrCpu-1].run();
		
		// wait for other CPUs to finish
		boolean allDone = false;
		while (!allDone) {
			allDone = true;
			for (int i=0; i<sys.nrCpu-1; ++i) {
				allDone &= s[i].finished;
			}			
		}
				
		// print out "picture"
		printGameTable();
		
		// print last player position
		for(int i=0; i<sys.nrCpu; ++i) {
			s[i].printLastPosition();
		}
		
		// print last npc standing = winner
		for(int i=0; i<sys.nrCpu; ++i) {
			if(running[i] == true) {
				System.out.println("Player " + s[i].playerno + " has won the game (last active npc)!");
			}
		}
	}
	
	private static void printGameTable() {
		System.out.println();
		for(int y=-1; y<SIZE; y++) {
			for(int x=-1; x<SIZE; x++) {
				if(x > -1) {
					if(y > -1) {
						if(array[x][y] == EMPTY) { System.out.print("."); }
						else { System.out.print(array[x][y]); }
						System.out.print("\t");
					}
					else {
						System.out.print("\t" + x);
					}
				}
				else if(y > -1) {
					System.out.print(y + "\t");
				}
			}
			System.out.println();
		}	
		System.out.println();
	}
	
	static class TronRunner implements Runnable {
		
		private static Random r = new Random();
		
		private boolean finished = false;
		
		private int playerno;
		
		private int x, y;
		private int dx, dy;
		
		private int moves = 0;
		
		private boolean[][] setPossible = new boolean[3][3];
		
		public TronRunner(int playerno, int startx, int starty) {
			this.playerno = playerno;			
			
			x = startx;
			y = starty;
			
			// set an initial direction
			randDir();
			// initial reset - everything is possible
			reset();
			
			System.out.println("Player " + playerno + " at (" + x + " | " + y + ")");
		}
		
		/**
		 * 7 0 1
		 * 6 X 2
		 * 5 4 3
		 * @return
		 */
		private void randDir() {
			dx = r.nextInt()%2;
			dy = r.nextInt()%2;
		}
		
		public void run() {					
			while(anyMovePossible()) {
				// test direction
				if( x+dx >= 0 && x+dx < SIZE && y+dy >= 0 && y+dy < SIZE) {
					synchronized(array) {
						if( array[x+dx][y+dy] == EMPTY) {
							// found a hole
							reset();
						
							// advance					
							x += dx;
							y += dy;
							array[x][y] = playerno;	// set my marker
							
							moves++;							
						}
						else {
							nextMove();
						}
					}
				}
				else {
					nextMove();
				}
				
				// check if there are other players left
				synchronized(running) {
					boolean loose = false;
					for(int i=0; i<sys.nrCpu; ++i) {
						if(running[i] == true && i != playerno) {
							loose = true;
						}
					}
					if(loose) {
						running[playerno] = false;
					}
				}
			}
			
			finished = true;
		}
		
		private void nextMove() {
			// set current dir as not possible
			setPossible[dx+1][dy+1] = false;
			
			randDir();
		}
		
		private void reset() {
			for(int x=0; x<3; ++x) {
				for(int y=0; y<3; ++y) {
					setPossible[x][y] = true;
				}
			}				
			// middle is never possible
			setPossible[1][1] = false;
		}
		
		private boolean anyMovePossible() {
			for(int x=0; x<3; ++x) {
				for(int y=0; y<3; ++y) {
					if(setPossible[x][y] == true)
						return true;
				}
			}
			return false;
		}
		
		public void printLastPosition() {
			System.out.println("Player " + playerno + " stopped at (" + x + " | " + y +") with " + moves + " moves");
		}
	}

}
