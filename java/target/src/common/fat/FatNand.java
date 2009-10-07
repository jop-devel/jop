/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Jens Kager, Fritz Praus

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

package fat;

import util.Nand;
import util.NandLowLevel;

public class FatNand implements FatLowLevel {

	static final int block_offset = 0; // where in NAND first block starts; for testing to avoid wrinting to same block again and again
	static final boolean LOG = false; // LOG output on/off
	static final boolean useBuffer = true; // use buffering of blocks
	static final boolean readBeforeWrite = true; // apply readBeforeWrite method
	
	static Nand nand;
	static boolean avail;

	// A 3-dimensional array would have been nice, unfortunately
	// this is unsupported by the JOP. :-(
	final static int NumBufferedBlocks = 5;
	static boolean[][] BufferFilled = new boolean[NumBufferedBlocks][NandLowLevel.PAGES_PER_BLOCK];
	static int[][] BufferedBlocks = new int[NumBufferedBlocks][NandLowLevel.PAGES_PER_BLOCK*NandLowLevel.WORDS];
		
	final static int size = 512;
	static int[] readBuffer = new int[size];
	static int[] tmpBuffer = new int[NandLowLevel.WORDS];

	public int Init() {
		
		if (nand==null) {
			nand = new Nand();
		}
		avail = nand.isAvailable();

		if (avail) {
			return 0;
		}
		else return -1; // unknown error 
	}
	
	public void Flush() {
		
		if (useBuffer == false)
			return;
		
		if (LOG)
			System.out.println("Flushing buffers");
		
		for (int block = 0; block < NumBufferedBlocks; block++) {
			for (int page = 0; page < NandLowLevel.PAGES_PER_BLOCK; page++) {
				if (BufferFilled[block][page]) {
					
					if (LOG)
						System.out.println("Flushing block " + block + ", page " + page);

					for (int i = 0; i < NandLowLevel.WORDS; i++) {
						tmpBuffer[i] = BufferedBlocks[block][(page*NandLowLevel.WORDS)+i];
					}
					nand.write(tmpBuffer, block + block_offset, page, size);
				}
			}
		}
	}
	
	public int ReadSector(int addr, int[] buffer) {
		int ret=-1;
		
		// NAND available, continue
		if (avail) {
			int block = addr / NandLowLevel.PAGES_PER_BLOCK;
			int page  = addr % NandLowLevel.PAGES_PER_BLOCK;
			
			// Read first sectors from buffer
			if (useBuffer && block < NumBufferedBlocks) {
				// Get page from buffer
				if (BufferFilled[block][page]) {
					if (LOG)
						System.out.println("Using cached block " + block + ", page " + page);

					for (int i = 0; i < NandLowLevel.WORDS; i++) {
						tmpBuffer[i] = BufferedBlocks[block][(page*NandLowLevel.WORDS)+i];
					}

					ret = 1; // read successful; (not 0!, simulate nand.read() behavior)
					
				// Page is not yet in buffer, fill from medium
				} else {
					if (LOG)
						System.out.println("Filling block " + block + ", page " + page);
					
					ret = nand.read(tmpBuffer, block + block_offset, page);
				
					// Fill buffer from medium
					for (int i = 0; i < NandLowLevel.WORDS; i++) {
						BufferedBlocks[block][(page*NandLowLevel.WORDS)+i] = tmpBuffer[i];
					}
					BufferFilled[block][page] = true;
				}
			} else {
				if (LOG)
					System.out.print("Reading unbuffered block " + block + ", page " + page);
				
				ret = nand.read(tmpBuffer, block + block_offset, page);
			}
			
			if (ret>0) {
				// NAND read successful
				
				// Spread the bytes which are combined into one 32 bit integer in tmpBuffer
				// into separate fields of buffer.
			
				int tmpIndex = 0;
				for (int i = 0; i < size; i += 4) {
					// Invert bytes before writing 
					// (see ClearMedium() for explanation)
					buffer[i+3] = ((~tmpBuffer[tmpIndex]) & 0xFF000000) >>> 24;
					buffer[i+2] = ((~tmpBuffer[tmpIndex]) & 0x00FF0000) >>> 16;
					buffer[i+1] = ((~tmpBuffer[tmpIndex]) & 0x0000FF00) >>>  8;
					buffer[i+0] = ((~tmpBuffer[tmpIndex]) & 0x000000FF);
					
					tmpIndex++;
				}
				
				ret=0;
			}
			
			if (LOG)
				System.out.println(" ...done.");
			
		} else {
			System.out.println("NAND Flash not available!");
			ret = -1;
		}

		return ret;
	}
	
	public int WriteSector(int addr, int[] buffer) {
		return WriteSector(addr, buffer, true);
	}

	public int WriteSector(int addr, int[] buffer, boolean buffered) {
		int ret=-1;
		
		// Compare current content of NAND to contents to be written.
		if (readBeforeWrite) {			
			ReadSector(addr, readBuffer);
			int readEqualsWrite = 1;
			for (int i = 0; i < size; i++) {
				if (readBuffer[i] != buffer[i]) {
					readEqualsWrite = 0;
					break;
				}
			}
			
			// Sector already contains the data we wanted to write -
			// abort write and report success.
			if (readEqualsWrite == 1)
				return 0;
		}
		
		// according to FatLowLevel interface the buffer size must be 512
		if (buffer.length!=size) {
			System.out.println("FatNand.WriteSector: buffer size " + size + " not correct!");
			return -1;
		}
		
		// Combine every four entries of the buffer (each of which must be no larger than
		// one byte) into one entry of compressedBuffer.
		int[] compressedBuffer = new int[NandLowLevel.WORDS];
		int compIndex = 0;
		for (int i = 0; i < size; i += 4) {
			compressedBuffer[compIndex] = (buffer[i])         +
			                              (buffer[i+1] << 8)  +
			                              (buffer[i+2] << 16) +
			                              (buffer[i+3] << 24);
			
			// Invert bytes before reading (see ClearMedium() for explanation)
			compressedBuffer[compIndex] = ~compressedBuffer[compIndex];
			compIndex++;
		}
		
		// NAND available, continue
		if (avail) {
			int block = addr / NandLowLevel.PAGES_PER_BLOCK;
			int page  = addr % NandLowLevel.PAGES_PER_BLOCK;
			
			// Write first sectors only into buffer
			if (useBuffer && (buffered == true) && (block < NumBufferedBlocks)) {
				
				if (LOG)
					System.out.print("Writing buffered block " + block + ", page " + page);
				
				for (int i = 0; i < NandLowLevel.WORDS; i++) {
					BufferedBlocks[block][(page*NandLowLevel.WORDS)+i] = compressedBuffer[i];
				}
				ret = 0;
				
			} else {
				
				if (LOG)
					System.out.print("Writing unbuffered block " + block + ", page " + page);
				
				if (nand.write(compressedBuffer, block + block_offset, page, size)) {
					// NAND write successful
					ret=0;
				}
			}
		}
		
		if (LOG)
			System.out.println(" ...done.");
		
		return ret;
	}

	public void ClearMedium() {
		for (int i = block_offset; i < nand.getNrOfBlocks(); i++) {
			// Erasing sets all the bytes to 0xFF. To compensate for that,
			// the ReadSector() and WriteSector() methods invert the 
			// read or written bytes.
			nand.erase(i);
		}
	}
	
	public int GetTotalBytes() {
		return (nand.size()-block_offset)*16*1024;
	}

}
