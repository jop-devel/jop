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
public class NandLowLevel {
	
	final static boolean LOG = true;

	public final static int NAND_ADDR = 0x100000;	// data port
	final static int CLE = NAND_ADDR + 1; // command latch enable
	final static int ALE = NAND_ADDR + 2; // address latch enable
	final static int RDY = NAND_ADDR + 4; // ready signal

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
	
	public final static int PAGES_PER_BLOCK = 32;
	public final static int WORDS = 128;	// one page in 32 bit words
	public final static int SPARE_WORDS = 4;
	

	/**
	 * Size in blocks. One block contains 32 pages or 16 KB.
	 */
	private int nrOfBlocks;

	protected int[] localSpare = new int[SPARE_WORDS];


	/**
	 * Check the size of the NAND flash.
	 */
	NandLowLevel() {
		
		Native.wrMem(SIGNATURE, CLE);
		Native.wrMem(0x00, ALE);
		int man = Native.rdMem(NAND_ADDR); // Manufacturer
		int size = Native.rdMem(NAND_ADDR); // Size

		if (size == 0x73) {
			nrOfBlocks = 1024;
		} else if (size == 0x75) {
			nrOfBlocks = 2048;
		} else if (size == 0x76) {
			nrOfBlocks = 4096;
		} else if (size == 0x79) {
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
		// S0=error bit S6=controller inactive S7=wr protection
		// Signal
		return !((Native.rdMem(NAND_ADDR) & 0x01) == 0x01);
	}

	/**
	 * waits until the NAND is ready
	 */
	void waitForReady() {
		while (Native.rdMem(RDY)==0) {
			; // watch rdy signal
		}
	}
	

	/**
	 * Erase one block and read back to check if cleaned.
	 * @param block
	 * @return true if ok.
	 */
	boolean eraseBlock(int block) {
		
		int a1, a2;
		boolean ret = true;
		int cnt;
		
		a1 = block << 5;
		a2 = block >> 3;
		Native.wrMem(ERASE, CLE);
		Native.wrMem(a1, ALE);
		Native.wrMem(a2, ALE);
		Native.wrMem(ERASE_CONFIRM, CLE);
		
		waitForReady();
		ret &= cmdOk();
		
		// check page
		for (int seq=0; seq<2; ++seq) {
			if (seq==0) {
				Native.wrMem(POINTA, CLE);
				cnt = WORDS*4;
			} else {
				Native.wrMem(POINTC, CLE);
				cnt = SPARE_WORDS*4;
			}
			Native.wrMem(0, ALE);		// we read a whole page, a0=0
			Native.wrMem(a1, ALE);
			Native.wrMem(a2, ALE);
			waitForReady();
			for (int i=0; i<cnt; ++i) {
				if (Native.rdMem(NAND_ADDR)!=0xff) {
					ret = false;
				}
			}			
			if (LOG) {
				if (!ret) {
					System.out.println("not erased");					
				}
			}
			Timer.wd();
		}

		
		return ret;
	}
	
	/**
	 * Erase the whole NAND flash.
	 * @return true if no error.
	 */
	boolean eraseAll() {
		boolean ret = true;
		for (int i=0; i<nrOfBlocks; ++i) {
			ret &= eraseBlock(i);
			System.out.print(".");
		}
		return ret;
	}

	/**
	 * Write one page and spare into the NAND. Also read back for a check.
	 * @param data
	 * @param block
	 * @param page
	 * @return true if no error.
	 */
	boolean writePage(int[] data, int[] spare, int block, int page) {
		
		int i, val;
		int a1, a2;
		int[] buf;
		boolean ret = true;
		int cnt;
		
		a1 = (block << 5) + page;
		a2 = block >> 3;
		
		// write page
		for (int seq=0; seq<2; ++seq) {
			if (seq==0) {
				Native.wrMem(POINTA, CLE);
				buf = data;
				cnt = WORDS;
			} else {
				Native.wrMem(POINTC, CLE);
				buf = spare;
				cnt = SPARE_WORDS;
			}
			if (buf==null) {
				continue;
			}
			Native.wrMem(PROGRAM, CLE);
			Native.wrMem(0, ALE);		// we write a whole page, a0=0
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
		
		// check page
		for (int seq=0; seq<2; ++seq) {
			if (seq==0) {
				Native.wrMem(POINTA, CLE);
				buf = data;
				cnt = WORDS;
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
			waitForReady();
			for (i=0; i<cnt; ++i) {
				val = Native.rdMem(NAND_ADDR);
				val = (val<<8) + Native.rdMem(NAND_ADDR);
				val = (val<<8) + Native.rdMem(NAND_ADDR);
				val = (val<<8) + Native.rdMem(NAND_ADDR);
				ret &= buf[i] == val;
			}
			
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
			Native.wrMem(0, ALE);		// we read a whole page, a0=0
			Native.wrMem(a1, ALE);
			Native.wrMem(a2, ALE);
			waitForReady();
			for (i=0; i<cnt; ++i) {
				val = Native.rdMem(NAND_ADDR);
				val = (val<<8) + Native.rdMem(NAND_ADDR);
				val = (val<<8) + Native.rdMem(NAND_ADDR);
				val = (val<<8) + Native.rdMem(NAND_ADDR);
				buf[i] = val;
			}
			
			ret &= cmdOk();
		}
		
		return ret;
	}
	
	// TODO: add ECC check
	boolean readPage(int[] data, int block, int page) {
		return readPage(data, localSpare, block, page);
	}
	
	/**
	 * Returns size in number of 16 KB blocks. Zero if no NAND
	 * Flash is on board.
	 * @return
	 */
	public int getNrOfBlocks() {
		return nrOfBlocks;
	}
}
