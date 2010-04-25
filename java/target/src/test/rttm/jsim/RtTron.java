/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

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

import rttm.jsim.Tron.TronRunner;

import joprt.RtThread;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Startup;

/**
 *	@author Michael Muck
 * 	rttm version - threaded
 *
 */
public class RtTron {
		
	// read a random number from IO
	private static final int IO_RAND = Const.IO_CPUCNT+1;	// its likely that this var needs to be changed!
	// read a positive random number from IO
	private static final int IO_PRAND = IO_RAND+1;	
	
	private static final int MAGIC = -10000;	
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	// Game Array Size
	static final int SIZE = 15;
	static int[][] array = new int[SIZE][SIZE];
	
	// how many players do we have
	static final int PLAYERS = 8;
	// the player threads
	static TronPlayerThread[] p = new TronPlayerThread[PLAYERS];
	
	static final int EMPTY = -1;
	
	static volatile boolean end = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// set game array empty
		for(int y=0; y<SIZE; ++y) {
			for(int x=0; x<SIZE; ++x) {
				array[x][y] = EMPTY;
			}
		}
		
		// create player threads
		createPlayers();
		
		// print initial game table
		printGameTable();
				
		Watcher w = new Watcher();
		w.setProcessor(0);
		
		// start mission and other CPUs
		RtThread.startMission();
		System.out.println("Mission started");
		
		/*
		// check for game end
		int activePlayers = PLAYERS;
		//StringBuffer s = new StringBuffer("\nActive Players: ");
		while( activePlayers > 1 ) {
			RtThread.sleepMs(50);
			activePlayers = 0;
			for(int i=0; i<PLAYERS; ++i) {
				if(p[i].active) {
					activePlayers++;
				}
			}

//			Native.wrMem(1, Const.IO_FLUSH);	// enable Buffer
//				System.out.print(s);
//				System.out.print(activePlayers);
//				System.out.print('\n');
//				printGameTable();
//			Native.wrMem(0, Const.IO_FLUSH);	// disable Buffer & flush it
		}
		
		// end game
		end = true;
		
		// print last status
		printGameTable();
		
		// print stats
		for(int i=0; i<PLAYERS; ++i) {
			p[i].stats();
		}
		
		boolean any = false;
		for(int i=0; i<PLAYERS; ++i) {
			if(p[i].active) {
				any = true;
				System.out.println("Player " + p[i].playerno + " has won the game (last active npc)");
			}
		}
		if(!any) {
			System.out.println("All Players were destroyed ...");
		}
		
		// write the magic string to stop the simulation
		System.out.println("\r\nJVM exit!\r\n");

		System.exit(0);
		*/
		for(;;) {
			RtThread.sleepMs(1000);
		}
		
	}
	
	private static void createPlayers() {
		int threads = PLAYERS;
		int no = 0;
		int sector = 0;
		int x = 0, y = 0;
		int d = (PLAYERS/4)%SIZE;
		if(PLAYERS%4 != 0) { d += 1; }
		int step = SIZE/d;
		int grenze = PLAYERS/4 + PLAYERS%4 ;
		
		System.out.println("d: " + d + " - step: " + step);
		
		for (int i=0; i<4; ++i) {			
			for( int k=0; k<grenze; ++k) {			
				System.out.println("no: " + no);
				if(threads > 0) {
					p[no] = new TronPlayerThread(no, x, y);
					p[no].setProcessor(no%sys.nrCpu);
					System.out.println("Tron Player " + no + " at Core" + (no%sys.nrCpu) + " starting at (" + x + "/" + y + ")");

					threads--;
				}
					
				// position update
				if(sector == 0) {	// sector0
					x += step;
					if(x >= SIZE-1) {
						System.out.println("sector = 1");
						sector = 1;
					}
				}
				else if(sector == 1) {	// sector1
					y += step;
					if(y >= SIZE-1) {
						System.out.println("sector = 2");
						sector = 2;
					}
				}
				else if(sector == 2) {
					x -= step;
					if(x <= 0) {
						System.out.println("sector = 3");
						sector = 3;
					}
				}
				else if(sector == 3) {
					y -= step;
					if(y <= 0) {
						System.out.println("sector = 0");
						sector = 0;
					}				
				}	
				
				no++;	// cpu count
			}
		}		
		
		for(int i=PLAYERS; i<sys.nrCpu; ++i) {
			RtThread th = new RtThread(1, 1000*1000);
			th.setProcessor(i);
			System.out.println("Dummy for Core"+i);
		}
		
	}

	private static void printGameTable() {		
		System.out.print('\n');
		for(int y=-1; y<SIZE; y++) {
			for(int x=-1; x<SIZE; x++) {
				if(x > -1) {
					if(y > -1) {
						if(array[x][y] == EMPTY) { System.out.print('.'); }
						else {
							System.out.print("\33[3"+array[x][y]+"m");
							System.out.print(array[x][y]); 
							System.out.print("\33[30m");
						}
						System.out.print('\t');
					}
					else {
						System.out.print('\t');
						System.out.print(x);
					}
				}
				else if(y > -1) {
					System.out.print(y);
					System.out.print('\t');
				}
			}
			System.out.print('\n');
		}
	}
	
	public static class Watcher extends RtThread {
		public Watcher() {
			 super(2, 1*1000);			
		}
		
		public void run() {
			// check for game end
			int activePlayers = PLAYERS;
			//StringBuffer s = new StringBuffer("\nActive Players: ");
			while( activePlayers > 1 ) {
				this.waitForNextPeriod();
				activePlayers = 0;
				for(int i=0; i<PLAYERS; ++i) {
					if(p[i].active) {
						activePlayers++;
					}
				}			

//				Native.wrMem(1, Const.IO_FLUSH);	// enable Buffer
//					System.out.print(s);
//					System.out.print(activePlayers);
//					System.out.print('\n');
//					printGameTable();
//				Native.wrMem(0, Const.IO_FLUSH);	// disable Buffer & flush it
			}
			
			// end game
			end = true;
			
			// print last status
			printGameTable();
			
			// print stats
			for(int i=0; i<PLAYERS; ++i) {
				p[i].stats();
			}
			
			boolean any = false;
			for(int i=0; i<PLAYERS; ++i) {
				if(p[i].active) {
					any = true;
					System.out.println("Player " + p[i].playerno + " has won the game (last active npc)");
				}
			}
			if(!any) {
				System.out.println("All Players were destroyed ...");
			}
			
			// write the magic string to stop the simulation
			System.out.println("\r\nJVM exit!\r\n");
		}
	}
	
	public static class TronPlayerThread extends RtThread {
		boolean active = true;
		
		private int playerno;
		
		private int x,y;
		private int dx, dy;
		
		private int randIterations = 0;
		
		private boolean forceDirection = false;
		private int lastlr = 0;
		
		private int moves = 1;
		
		private boolean[] setPossible = new boolean[9];
					
		public TronPlayerThread(int p, int startx, int starty) {
			super(1, 1*3000);
			
			playerno = p;
			x = startx;
			y = starty;
			
			array[x][y] = p;
			
			// set an initial direction
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
				
				waitForNextPeriod();
				
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
				if(randIterations > 20) {
					game_over = true;
				}
				
				waitForNextPeriod();
			}				
						
			if(!end) {
				this.active = false;	
				System.out.println("Player" + playerno + " dead");
			}
			
			for(;;) {
				waitForNextPeriod();
			}
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
