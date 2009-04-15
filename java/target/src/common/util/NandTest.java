/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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
package util;

import com.jopdesign.sys.Native;

/**
 * Test program for the NAND Flash.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class NandTest extends Nand {


	/**
	 * Test the NAND Flash. A static method to be used in the
	 * board test.
	 * 
	 * @return true when NAND Flash is available.
	 */
	public static boolean test() {

		int i, j;
		boolean ret = true;
		/* read ID and status from NAND */
		Native.wrMem(SIGNATURE, CLE);
		Native.wrMem(0x00, ALE);
		//
		// should read 0x98 and 0x73
		//
		i = Native.rdMem(NAND_ADDR); // Manufacturer
		j = Native.rdMem(NAND_ADDR); // Size
		System.out.print("NAND ");
		System.out.print(i);
		System.out.print(" ");
		System.out.print(j);
		System.out.print(" ");
		if (i == 0x98) {
			System.out.print("Toshiba ");
		} else if (i == 0x20) {
			System.out.print("ST ");
		} else {
			System.out.println("Unknown manufacturer");
		}

		if (j == 0x73) {
			System.out.println("16 MB");
		} else if (j == 0x75) {
			System.out.println("32 MB");
		} else if (j == 0x76) {
			System.out.println("64 MB");
		} else if (j == 0x79) {
			System.out.println("128 MB");
		} else {
			System.out.println("error reading NAND");
			ret = false;
		}

		//
		// read status, should be 0xc0
		//
		Native.wrMem(STATUS, CLE);
		i = Native.rdMem(NAND_ADDR) & 0xc1;
		j = Native.rdMem(NAND_ADDR) & 0xc1;
		System.out.print(i);
		System.out.print(" ");
		System.out.print(j);
		System.out.print(" ");
		if (i == 0xc0 && j == 0xc0) {
			System.out.println("status OK");
		} else {
			System.out.println("error reading NAND status");
			ret = false;
		}

		return ret;
	}
	
	void findBadBlocks() {
		
		int[] spare = new int[SPARE_WORDS];

		for (int i=0; i<getNrOfBlocks(); ++i) {
			for (int j=0; j<PAGES_PER_BLOCK; ++j) {
				readPage(null, spare, i, j);
				boolean bad = false;
				for (int k=0; k<SPARE_WORDS; ++k) {
					if (spare[k]!=-1) {
						bad = true;
					}
				}
				if (bad) {
					System.out.println("Bad block "+i);
					for (int k=0; k<SPARE_WORDS; ++k) {
						System.out.println(Integer.toHexString(spare[k]));
					}
				}
			}
		}
	}
	/**
	 * Program the whole NAND flash and check it.
	 * @return
	 */
	void testFull() {
		
		int block, page;

		int[] data = new int[WORDS];
		int[] spare = new int[SPARE_WORDS];
		int magic;
		int badPage=0;
		int badSpare=0;
		int time;
		int nrBlocks = getNrOfBlocks();
		int[] badBlocks = new int[nrBlocks];
		
//		nrBlocks = 100;
		
		time = (int) System.currentTimeMillis();
		System.out.println("Erase");
		if (eraseAll()) {
			System.out.println("Not all blocks erased");
		}
		time = ((int) System.currentTimeMillis()) - time;
		System.out.println("Erase finished after "+time+" ms");
		
		System.out.println("Program");
		time = (int) System.currentTimeMillis();
		for (block=0; block<nrBlocks; ++block) {
			System.out.print("+");
			for (page=0; page<PAGES_PER_BLOCK; ++page) {
				for (int i=0; i<WORDS; ++i) {
					magic = OFF+i+(block<<7)+(page<<2);
					data[i] = magic;
				}
				for (int i=0; i<SPARE_WORDS; ++i) {
					magic = OFF+i+(block<<7)+(page<<2)+0xabcd;
					spare[i] = magic;
				}
				if (!writePage(data, spare, block, page)) {
					System.out.println("Error on write at block "+block+" page "+page);
					badBlocks[block]++;
				}
			}
		}
		time = ((int) System.currentTimeMillis()) - time;
		System.out.println();
		System.out.println("Program finished after "+time+" ms");

		System.out.println("Read back");
		time = (int) System.currentTimeMillis();
		for (block=0; block<nrBlocks; ++block) {
			System.out.print(".");
			for (page=0; page<PAGES_PER_BLOCK; ++page) {
				for (int i=0; i<WORDS; ++i) {
					data[i] = 0;
				}
				for (int i=0; i<SPARE_WORDS; ++i) {
					spare[i] = 0;
				}

				if (!readPage(data, spare, block, page)) {
					System.out.println("Error on read at block "+block+" page "+page);
					badBlocks[block]++;
				}
				for (int i=0; i<WORDS; ++i) {
					magic = OFF+i+(block<<7)+(page<<2);
					if (data[i] != magic) {
//						System.out.println("Read error data "+(block*32+page));
						badBlocks[block]++;
						++badPage;
						break;
					}
				}
				for (int i=0; i<SPARE_WORDS; ++i) {
					magic = OFF+i+(block<<7)+(page<<2)+0xabcd;
					if (spare[i] != magic) {
//						System.out.println("Read error spare "+(block*32+page));
						badBlocks[block]++;
						++badSpare;
						break;
					}
				}
			}
		}
		time = ((int) System.currentTimeMillis()) - time;
		System.out.println();
		System.out.println("Read back finished after "+time+" ms");

		System.out.println("Bad pages: "+badPage+" from "+(nrBlocks*32));
		System.out.println("Bad spares: "+badSpare+" from "+(nrBlocks*32));
		int cnt = 0;
		for (int i=0; i<badBlocks.length; ++i) {
			if (badBlocks[i]!=0) {
				++cnt;
			}
		}
		System.out.println("Bad blocks: "+cnt+" from "+nrBlocks);
	}

	final static int OFF = 1234;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int block, page;
		
		test();
		NandTest n = new NandTest();
		System.out.println(n.getNrOfBlocks() + " blocks");
		System.out.println(n.size() + " usable blocks");
		System.out.println(n.size()*16 + " KB");
//		System.out.println("Physical block "+n.getPhysicalBlock(3));
//		System.out.println("Physical block "+n.getPhysicalBlock(488));
//		System.out.println("Physical block "+n.getPhysicalBlock(1000));
//		System.out.println("Physical block "+n.getPhysicalBlock(1920));
//		n.eraseAll();

//		for (int i=0; i<1000; ++i) {
//			n.erase(i);
//			System.out.print('.');
//		}
//		n.testFull();
//		n.findBadBlocks();
		
//		System.out.println("Start wearing out test");
//		int data[] = new int[128];
//		for (int cnt=0; cnt<10000; ++cnt) {
//			System.out.print(" ");
//			System.out.print(cnt);
//			for (int i=450; i<500; ++i) {
//				for (int j=0; j<PAGES_PER_BLOCK; ++j) {
//					for (int k=0; k<128; ++k) {
//						data[k] = i*128*PAGES_PER_BLOCK+j*PAGES_PER_BLOCK+k+cnt;
//					}
//					n.write(data, i, j, 512);				
//				}
//			}
//			for (int i=450; i<500; ++i) {
//				for (int j=0; j<PAGES_PER_BLOCK; ++j) {
//					n.read(data, i, j);				
//					for (int k=0; k<128; ++k) {
//						if (data[k] != i*128*PAGES_PER_BLOCK+j*PAGES_PER_BLOCK+k+cnt) {
//							System.out.println("Data error! "+i+" "+j+" "+(k+cnt));
//							System.exit(-1);
//						}
//					}
//				}
//			}
//			for (int i=450; i<500; ++i) {
//				n.erase(i);
//			}
//		}
		
	}

}
