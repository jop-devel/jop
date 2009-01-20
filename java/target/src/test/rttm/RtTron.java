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
package rttm;

import java.util.Random;

import joprt.RtThread;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

/**
 * A real-time threaded version of Hello World for CMP
 * 
 * @author martin
 *
 */
public class RtTron {
	private static final int MAGIC = -10000;	
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static final int PLAYERS = 8;

	static final int SIZE = 10;
	static int[][] array = new int[SIZE][SIZE];
	
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
		Random r = new Random();
		TronPlayerThread[] p = new TronPlayerThread[PLAYERS];
		for (int i=0; i<PLAYERS; ++i) {
			int x,y;
			do {
				x = Math.abs( r.nextInt()%SIZE );
				y = Math.abs( r.nextInt()%SIZE );
			} while(array[x][y] != EMPTY);
			
			p[i] = new TronPlayerThread(i, x, y);
			p[i].setProcessor(i%sys.nrCpu);
			System.out.println("Tron Player " + i + " at Core" + (i%sys.nrCpu) + " starting at (" + x + "/" + y + ")");
		}
		for(int i=PLAYERS; i<sys.nrCpu; ++i) {
			RtThread th = new RtThread(1, 1000*1000);
			th.setProcessor(i);
			System.out.println("Dummy for Core"+i);
		}
		
		// print initial game table
		printGameTable();
				
		// start mission and other CPUs
		RtThread.startMission();
		System.out.println("Mission started");
		
		// check for game end
		int activePlayers = PLAYERS;
		StringBuffer s = new StringBuffer("\nActive Players: ");
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
		for(;;) {
			RtThread.sleepMs(1000);
		}
		
	}

	private static void printGameTable() {		
		System.out.print('\n');
		for(int y=-1; y<SIZE; y++) {
			for(int x=-1; x<SIZE; x++) {
				if(x > -1) {
					if(y > -1) {
						if(array[x][y] == EMPTY) { System.out.print('.'); }
						else { System.out.print(array[x][y]); }
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
	
	public static class TronPlayerThread extends RtThread {
		boolean active = true;
		
		private int playerno;
		
		private int x,y;
		private int dx, dy;		
		
		private boolean[][] setPossible = new boolean[3][3];

		private int moves = 0;
		
		private Random r = new Random();
		
		public TronPlayerThread(int p, int startx, int starty) {
			super(1, 1*1000);
			
			playerno = p;
			x = startx;
			y = starty;
			
			array[x][y] = p;
			
			// set an initial direction
			randDir();
			// initial reset - everything is possible
			reset();
		}

		public void run() {
			int m;
			
			while(anyMovePossible()) {
				// test for borders
				if( x+dx >= 0 && x+dx < SIZE && y+dy >= 0 && y+dy < SIZE) {
					m = moves;
					
					Native.wrMem(1, MAGIC);
						// test direction
						if( array[x+dx][y+dy] == EMPTY) {
							// found a hole
							reset();
						
							// advance					
							x += dx;
							y += dy;
							array[x][y] = playerno;	// set my marker
							
							moves++;
						}
					Native.wrMem(0, MAGIC);
					
					if(m == moves) {	// no move made
						setNotPossible();
						randDir();
					}
				}
				else {					
					setNotPossible();
					randDir();
				}
				
				waitForNextPeriod();
			}
			
			if(!end) {
				this.active = false;				
			}
			
			for(;;) {
				waitForNextPeriod();
			}
		}
		
		private boolean anyMovePossible() {
			// first check if there is any place to go			
			boolean any = false;
			int kx, ky;
			for(int ny=-1; ny<=1; ++ny) {
				for(int nx=-1; nx<=1; ++nx) {
					kx = x+nx;
					ky = y+ny;
					if( (kx >= 0) && 
						(kx < SIZE) && 
						(ky >= 0) && 
						(ky < SIZE) ) {

						if(array[kx][ky] == EMPTY) {
							any = true;
						}
					}
				}
			}
			if(!any) { return false; }
			
			for(int y=0; y<3; ++y) {
				for(int x=0; x<3; ++x) {
					if(setPossible[x][y] == true)						
						return true;
				}
			}
			return false;
		}

		/**
		 * x: -1 0 1
		 * y: -1 0 1
		 * @return
		 */
		private void randDir() {
			dx = r.nextInt()%2;
			dy = r.nextInt()%2;
		}
		
		private void reset() {
			for(int y=0; y<3; ++y) {
				for(int x=0; x<3; ++x) {
					setPossible[x][y] = true;
				}
			}				
			// middle is never possible
			setPossible[1][1] = false;
		}
		
		private void setNotPossible() {
			// set current dir as not possible
			setPossible[dx+1][dy+1] = false;
		}		
		
		public void stats() {
			System.out.println("Player " + playerno + " stopped at (" + x + " | " + y +") with " + moves + " moves");
		}
	}
	

}
