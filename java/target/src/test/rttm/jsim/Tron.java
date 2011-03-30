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
package rttm.jsim;

import java.util.Random;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

/**
 * @author Michael Muck
 * rttm version of Tron
 */
public class Tron {
	
	// read a random number from IO
	private static final int IO_RAND = Const.IO_CPUCNT+1;	// its likely that this var needs to be changed!
	// read a positive random number from IO
	private static final int IO_PRAND = IO_RAND+1;	
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static final int EMPTY = -1;
	
	// Game Array Size
	static final int SIZE = 15;
	static int array[][] = new int[SIZE][SIZE];
	
	// running field
	static boolean running[] = new boolean[sys.nrCpu];
	// tronRunners
	static TronRunner s[] = new TronRunner[sys.nrCpu];		
	
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

		// create the runnables for all cores
		createPlayers();
				
		// print initial game table
		printGameTable();
		
		// measure time
		int startTime, endTime;
		startTime = Native.rd(Const.IO_US_CNT);	
		
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
		
		endTime = Native.rd(Const.IO_US_CNT);	
		
		System.out.print("Time: ");
		System.out.print(endTime-startTime);
		System.out.println("\n");
				
		// print out "picture"
		printGameTable();
		
		// print last player positions
		for(int i=0; i<sys.nrCpu; ++i) {
			s[i].stats();
		}
		
		// print out winner
		boolean any = false;
		for(int i=0; i<sys.nrCpu; ++i) {
			if(running[i] == true) {
				any = true;
				System.out.println("Player " + s[i].playerno + " has won the game (last active npc)!");
			}
		}
		if(!any) {
			System.out.println("All Players were destroyed ...");
		}
	}
	
	private static void createPlayers() {
		int cores = sys.nrCpu;
		int no = 0;
		int sector = 0;
		int x = 0, y = 0;
		int d = (sys.nrCpu/4)%SIZE;
		if(sys.nrCpu%4 != 0) { d += 1; }
		int step = SIZE/d;
		int grenze = sys.nrCpu/4 + sys.nrCpu%4 ;
		
		System.out.println("d: " + d + " - step: " + step);
		
		for (int i=0; i<4; ++i) {			
			for( int k=0; k<grenze; ++k) {			
				System.out.println("no: " + no);
				if(cores > 0) {
					if(no < sys.nrCpu-1) {
						s[no] = new TronRunner(no+1, x, y);
						Startup.setRunnable(s[no], no);
					}
					else {
						s[no] = new TronRunner(0, x, y);
					}
					cores--;
				}
					
				// position update
				if(sector == 0) {	// sector0
					x += step;
					if(x >= SIZE-1) {
						x = SIZE-1;
						System.out.println("x=" + x + " sector = 1");
						sector = 1;
					}
				}
				else if(sector == 1) {	// sector1
					y += step;
					if(y >= SIZE-1) {
						y = SIZE-1;
						System.out.println("sector = 2");
						sector = 2;
					}
				}
				else if(sector == 2) {
					x -= step;
					if(x <= 0) {
						x = 0;
						System.out.println("sector = 3");
						sector = 3;
					}
				}
				else if(sector == 3) {
					y -= step;
					if(y <= 0) {
						y = 0;
						System.out.println("sector = 0");
						sector = 0;
					}				
				}	
				
				no++;	// cpu count
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
						else {
							System.out.print("\33[3"+array[x][y]+"m");
							System.out.print(array[x][y]); 
							System.out.print("\33[30m");
						}
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

		private static final int MAGIC = -10000;		
			
		private boolean finished = false;
		
		private int playerno;
		
		private int x, y;
		private int dx, dy;
		
		private int randIterations = 0;
		
		private boolean forceDirection = false;
		private int lastlr = 0;
		
		private int moves = 1;
		
		private boolean[] setPossible = new boolean[9];
		
		public TronRunner(int p, int startx, int starty) {
			playerno = p;			
			
			x = startx;
			y = starty;

			// set start field
			array[x][y] = p;
			
			// set the initial direction
			dx = ((SIZE-1)/2)-x;
			if(dx<0) { dx /= -dx; }
			else if(dx == 0) { dx = 0; }
			else { dx /= dx; }
			dy = ((SIZE-1)/2)-y;
			if(dy<0) { dy /= -dy; }
			else if(dy == 0) { dy = 0; }
			else { dy /= dy; }
			
			// initial reset - everything is possible
			for(int n=0; n<9; ++n) {
				setPossible[n] = true;
			}
			setPossible[4] = false;
			
			System.out.println("Player " + playerno + " at (" + x + " | " + y + ") with (" + dx + " | " + dy + ")");
		}
				
		public void run() {		
			boolean game_over = false;			
			int n;
			
			while(!game_over) {
				// we have a starting field, an initial direction - now go
				
				Native.wrMem(1, MAGIC);
					// test for game field boundaries and test our field for emptyness 
					if( x+dx >= 0 && x+dx < SIZE && y+dy >= 0 && y+dy < SIZE 
							&& 
						array[x+dx][y+dy] == EMPTY) {
						
						// step forward
						x += dx; y += dy;
													
						// set our landmark
						array[x][y] = this.playerno;
							
						moves++;
						randIterations = 0;	
						
						//resetPossibilities(); -> problems with method invocation -> do inline
						// reset possible directions
						for(n=0; n<9; ++n) {
							setPossible[n] = true;
						}
						setPossible[4] = false;
							
						// mark move impossible from where we came
						setPossible[(-dx+1)+((-dy+1)*3)] = false;
					}
					else { // if not possible, choose another direction to go
						setPossible[(dx+1)+((dy+1)*3)] = false;
						
						//randDirection();	-> problems with method invocation during a transaction
					}
				Native.wrMem(0, MAGIC);
				
				// if setPossible[direction] = false -> search new direction
				if(setPossible[(dx+1)+((dy+1)*3)] == false) {
					randDirection();
				}
				
				// see if we have options where to go left
				game_over = true;
				for(n=0; n<9; ++n) {
					if(setPossible[n] == true) {
						game_over = false;
					}
				}
				
				// check the randIterations
				if(randIterations > 5) {
					forceDirection = true;
				}
				if(randIterations > 20) {
					game_over = true;
				}		
			}	
			
			// check if there are other players left
			/*
			Native.wrMem(1, MAGIC);
				running[playerno] = false;
			Native.wrMem(0, MAGIC);
			*/
			//System.out.println("player"+playerno + " dead");
			
			boolean anyOther = false;
			Native.wrMem(1, MAGIC);
				for(n=0; n<sys.nrCpu; ++n) {				
					if(running[n] == true && n != this.playerno) {	// uh we are not the last player
						anyOther = true;					// we lost :(
					}				
				}
					
				if(!anyOther) {
					running[playerno] = true;
				}
				else {
					running[playerno] = false;
				}
			Native.wrMem(0, MAGIC);

			finished = true;			
		}
		
		private void randDirection() {
			/*	  dy/dx
			 * 				-1		0		1
			 * 		-1		0		1		2		0
			 * 		0		3		4		5		3
			 * 		1		6		7		8		6
			 * 				0		1		2	
			 * 										x/y
			 */
			
			int lr;
			
			if(forceDirection) {
				lr = lastlr; 	// do not turn around!
			}
			else {
				lr = Native.rdMem(IO_PRAND)%2;
			}
			
			if(lr == 0) {	// turn left		
				if(dx > 0) { 
					if(dy > 0) {
						dy = 0;
					}
					else if(dy == 0) {
						dy = -1;
					}
					else {
						dx = 0;
					}					
				}
				else if(dx == 0) { 
					if(dy > 0) {
						dx = 1;
					}
					/*	-> impossible!
					else if(dy == 0) {
						dy = -1;
					}
					*/
					else {
						dx = -1;
					}
				}
				else { 
					if(dy > 0) {
						dx = 0;
					}					
					else if(dy == 0) {
						dy = 1;
					}					
					else {
						dy = 0;
					} 
				}
			}
			else {	// turn right		
				if(dx > 0) { 
					if(dy > 0) {
						dx = 0;
					}
					else if(dy == 0) {
						dy = 1;
					}
					else {
						dy = 0;
					}					
				}
				else if(dx == 0) { 
					if(dy > 0) {
						dx = -1;
					}
					/*	-> impossible!
					else if(dy == 0) {
						dy = -1;
					}
					*/
					else {
						dx = 1;
					}
				}
				else { 
					if(dy > 0) {
						dy = 0;
					}					
					else if(dy == 0) {
						dy = -1;
					}					
					else {
						dx = 0;
					} 
				}
			}	
			
			lastlr = lr;	// save lastlr
			randIterations++;
		}
				
		public void stats() {
			System.out.println("Player " + playerno + " stopped at (" + x + " | " + y +") with " + moves + " moves");
		}
	}

}
