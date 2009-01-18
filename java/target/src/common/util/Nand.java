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

import oebb.BgTftp;

import com.jopdesign.sys.Native;

/**
 * Interface to NAND memory on the Cycore board.
 * 
 * NAND organization (8-bit device): page = 2x256 Bytes + 16 Bytes spare, block =
 * 32 pages = 16 KB
 * 
 * Addressing is without A8 => for 8-bit devices there are two different address
 * commands that contain A8. A0-A7 Column Address, A9-A13 Address in Block, A14-A26
 * Block Address, A9-A26 Page Address
 * 
 * Bad blocks are marked with the 6th byte of the spare area with data != 0xff
 * on shipment.
 * 
 * All commands use integer arrays. We use the network order with high byte first.
 * 
 * @author Martin Schoeberl
 * 
 * TODO:
 * 		Having the RDY signal as bit 8 is not such a good idea -- change memory
 * 			interface
 * 
 */
public class Nand {

	public final static int NAND_ADDR = 0x100000;
	final static int CLE = NAND_ADDR + 1; // command latch enable
	final static int ALE = NAND_ADDR + 2; // address latch enable

	final static int ERASE = 0x60;
	final static int ERASE_CONFIRM = 0xD0;
	final static int STATUS = 0x70;
	final static int SIGNATURE = 0x90;
	final static int PROGRAM = 0x80;
	final static int PROGRAM_CONFIRM = 0x10;
	final static int POINTA = 0x00;
	final static int POINTB = 0x01;
	final static int POINTC = 0x50;

	final static int RDY_MSK = 0x100;
	
	final static int PAGES_PER_BLOCK = 32;
	final static int WORDS = 128;	// one page in 32 bit words
	final static int SPARE_WORDS = 4;
	

	/**
	 * Size in blocks. One block contains 32 pages or 16 KB.
	 */
	int nrOfBlocks;

	/**
	 * Check the size of the NAND flash.
	 */
	public Nand() {
		
		Native.wrMem(SIGNATURE, CLE);
		Native.wrMem(0x00, ALE);
		int man = Native.rdMem(NAND_ADDR); // Manufacturer
		int size = Native.rdMem(NAND_ADDR); // Size

		if (size == 0x173) {
			nrOfBlocks = 1024;
		} else if (size == 0x175) {
			nrOfBlocks = 2048;
		} else if (size == 0x176) {
			nrOfBlocks = 4096;
		} else if (size == 0x179) {
			nrOfBlocks = 8192;
		} else {
			nrOfBlocks = 0;
		}

	}
	/**
	 * @return true if no error is signaled by the error bit
	 */
	boolean cmdOk() {
		Native.wrMem(STATUS, CLE);
		// S0=error bit S6=controller inactive S7=wr protection S8=nReady/Busy
		// Signal
		return !((Native.rdMem(NAND_ADDR) & 0x01) == 0x01);
	}

	/**
	 * waits until the NAND is ready
	 */
	void waitForReady() {
		while ((Native.rdMem(NAND_ADDR) & RDY_MSK) != RDY_MSK) {
			; // watch rdy signal
		}
	}
	

	/**
	 * Erase one block.
	 * @param block
	 * @return true if ok.
	 */
	boolean eraseBlock(int block) {
		
		int a1, a2;
		
		a1 = (block << 5);
		a2 = (block >> 5);
		Native.wrMem(ERASE, CLE);
		Native.wrMem(a1, ALE);
		Native.wrMem(a2, ALE);
		Native.wrMem(ERASE_CONFIRM, CLE);
		
		waitForReady();
		
		return cmdOk();
	}
	
	/**
	 * Erase the whole NAND flash.
	 * @return true if no error.
	 */
	boolean eraseAll() {
		boolean ret = true;
		for (int i=0; i<nrOfBlocks; ++i) {
			ret &= eraseBlock(i);
		}
		return ret;
	}

	/**
	 * Write one page and spare into the NAND.
	 * @param data
	 * @param block
	 * @param page
	 * @return true if no error.
	 */
	boolean writePage(int[] data, int[] spare, int block, int page) {
		
		int i, val;
		int a1, a2;
		int[] buf = data;
		boolean ret = true;
		int cnt = WORDS;
		
		a1 = (block << 5) + page;
		a2 = block >> 3;
		for (int seq=0; seq<2; ++seq) {
			if (seq==0) {
				Native.wrMem(POINTA, CLE);			
			} else {
				Native.wrMem(POINTC, CLE);
				buf = spare;
				cnt = SPARE_WORDS;
			}
			if (buf==null) {
				continue;
			}
			Native.wrMem(PROGRAM, CLE);
			Native.wrMem(0, ALE);		// we read a whole page, a0=0
			Native.wrMem(a1, ALE);
			Native.wrMem(a2, ALE);
			for (i=0; i<cnt; ++i) {
				val = buf[i];
				Native.wr(val>>24, NAND_ADDR);
				Native.wr(val>>16, NAND_ADDR);
				Native.wr(val>>8, NAND_ADDR);
				Native.wr(val, NAND_ADDR);
			}
			Native.wrMem(PROGRAM_CONFIRM, CLE);
			
			waitForReady();
			ret &= cmdOk();
		}
		
		return ret;
	}

	/**
	 * Read one page and spare from the NAND.
	 * @param data
	 * @param block
	 * @param page
	 * @return
	 */
	boolean readPage(int[] data, int[] spare, int block, int page) {
		
		int i, j, val;
		int a1, a2;
		int[] buf = data;
		boolean ret = true;
		int cnt = WORDS;
		
		a1 = (block << 5) + page;
		a2 = block >> 3;
		
		for (int seq=0; seq<2; ++seq) {
			if (seq==0) {
				Native.wrMem(POINTA, CLE);				
			} else {
				Native.wrMem(POINTC, CLE);
				buf = spare;
				cnt = SPARE_WORDS;
			}
			if (buf==null) {
				continue;
			}
			Native.wrMem(0, ALE);		// we read a whole page, a0=0
			Native.wrMem(a1, ALE);
			Native.wrMem(a2, ALE);
			for (i=0; i<cnt; ++i) {
				val = 0;
//				if (i==0) {
					while ((val=Native.rdMem(NAND_ADDR)) < RDY_MSK) {
						; // watch rdy signal
					}					
//				} else {
//					val = Native.rdMem(NAND_ADDR);
//				}
				val = (val<<8) + (Native.rdMem(NAND_ADDR) & 0xff);
				val = (val<<8) + (Native.rdMem(NAND_ADDR) & 0xff);
				val = (val<<8) + (Native.rdMem(NAND_ADDR) & 0xff);
				buf[i] = val;
			}
			
			ret &= cmdOk();
		}
		
		return ret;
	}

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
		if (i == 0x198) {
			System.out.print("Toshiba ");
		} else if (i == 0x120) {
			System.out.print("ST ");
		} else {
			System.out.println("Unknown manufacturer");
		}

		if (j == 0x173) {
			System.out.println("16 MB");
		} else if (j == 0x175) {
			System.out.println("32 MB");
		} else if (j == 0x176) {
			System.out.println("64 MB");
		} else if (j == 0x179) {
			System.out.println("128 MB");
		} else {
			System.out.println("error reading NAND");
			ret = false;
		}

		//
		// read status, should be 0xc0
		//
		Native.wrMem(STATUS, CLE);
		i = Native.rdMem(NAND_ADDR) & 0x1c1;
		j = Native.rdMem(NAND_ADDR) & 0x1c1;
		System.out.print(i);
		System.out.print(" ");
		System.out.print(j);
		System.out.print(" ");
		if (i == 0x1c0 && j == 0x1c0) {
			System.out.println("status OK");
		} else {
			System.out.println("error reading NAND status");
			ret = false;
		}

		return ret;
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
		int testCnt = nrOfBlocks;
		
		time = (int) System.currentTimeMillis();
		System.out.println("Erase");
		if (eraseAll()) {
			System.out.println("Not all blocks erased");
		}
		time = ((int) System.currentTimeMillis()) - time;
		System.out.println("Erase finished after "+time+" ms");
		
		System.out.println("Program");
		time = (int) System.currentTimeMillis();
		for (block=0; block<testCnt; ++block) {
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
				}
			}
		}
		time = ((int) System.currentTimeMillis()) - time;
		System.out.println();
		System.out.println("Program finished after "+time+" ms");

		System.out.println("Read back");
		time = (int) System.currentTimeMillis();
		for (block=0; block<testCnt; ++block) {
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
				}
				for (int i=0; i<WORDS; ++i) {
					magic = OFF+i+(block<<7)+(page<<2);
					if (data[i] != magic) {
//						System.out.println("Read error data "+(block*32+page));
						++badPage;
						break;
					}
				}
				for (int i=0; i<SPARE_WORDS; ++i) {
					magic = OFF+i+(block<<7)+(page<<2)+0xabcd;
					if (spare[i] != magic) {
//						System.out.println("Read error spare "+(block*32+page));
						++badSpare;
						break;
					}
				}
			}
		}
		time = ((int) System.currentTimeMillis()) - time;
		System.out.println();
		System.out.println("Read back finished after "+time+" ms");

		System.out.println("Bad pages: "+badPage+" from "+(testCnt*32));
		System.out.println("Bad spares: "+badSpare+" from "+(testCnt*32));
	}

	final static int OFF = 1234;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int block, page;
		
		test();
		Nand n = new Nand();
		System.out.println(n.nrOfBlocks + " blocks");
		n.testFull();
//		System.out.println("Erase NAND");
////		n.eraseAll();
//		int[] data = new int[WORDS];
//		int[] spare = new int[SPARE_WORDS];
//		
//		for (block=0; block<1; ++block) {
//			for (page=0; page<PAGES_PER_BLOCK; ++page) {
//				n.readPage(data, spare, block, page);
//				System.out.println("block "+block+" page "+page+" data "+data[0]+" spare "+spare[0]);
//			}
//		}
//		block = 0;
//		page = 0;
//		for (int i=0; i<WORDS; ++i) {
//			data[i] = OFF+i;
//		}
//		if (!n.writePage(data, null, block, page)) {
//			System.out.println("Error during write");
//		}
//		for (int i=0; i<WORDS; ++i) {
//			data[i] = 0;
//		}
//		if (!n.readPage(data,  null, block, page)) {
//			System.out.println("Error during read");
//		}
//		for (int i=0; i<WORDS; ++i) {
//			if (data[i] != OFF+i) {
//				System.out.println("Error "+data[i]);
//			}
//		}
//		
//		for (int i=0; i<SPARE_WORDS; ++i) {
//			spare[i] = OFF+i;
//		}
//		if (!n.writePage(null, spare, block, page)) {
//			System.out.println("Error during write");
//		}
//		for (int i=0; i<SPARE_WORDS; ++i) {
//			spare[i] = 0;
//		}
//		if (!n.readPage(null,  spare, block, page)) {
//			System.out.println("Error during read");
//		}
//		for (int i=0; i<SPARE_WORDS; ++i) {
//			if (spare[i] != OFF+i) {
//				System.out.println("Error "+spare[i]);
//			}
//		}

	}

}
