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

/**
 * NAND interface with bad block mapping.
 * 
 * About 6% (1/16) of the NAND is reserved mapping of bad blocks. The bad block
 * map is stored in the last or last-1 block and kept in memory.<br>
 * 
 * Usage of spare bytes:<br>
 * B0-B1: type<br>
 * B2-B3: TBD<br>
 * B4-B5: TBD<br>
 * B6-B7: bad block marker (!= 0xffff)<br>
 * B8-B9: TBD<br>
 * B10-B11: TBD<br>
 * B12-B13: TBD<br>
 * B14-B15: checksum<br>
 * 
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 * 
 */
public class Nand extends NandLowLevel {
	
	final static int TYPE_BBMAP = 1;
	final static int TYPE_DATA = 2;

	/**
	 * Mapping of bad blocks. Lower 16 bits is the logical block number, upper
	 * 16 bits are the block in the reserved block area if mapped.
	 */
	int remap[];
	int[] data = new int[WORDS];
	int mapBlock;

	public Nand() {
		super();
		int maxRemap = getNrOfBlocks() / 16 - 2;
		if (maxRemap < 0) {
			// we have not found a NAND Flash
			return;
		}
		remap = new int[maxRemap];
		for (int i = 0; i < remap.length; i++) {
			remap[i] = -1;
		}
		mapBlock = getNrOfBlocks() - 1;
		if (isBad(mapBlock)) {
			--mapBlock;
			if (isBad(mapBlock)) {
				System.out.println("NAND Flash is dead!");
			}
		}
		readPage(null, localSpare, mapBlock, 0);
		if ((localSpare[0]>>>16)!=TYPE_BBMAP) {
			System.out.println("Initializing NAND Flash");
			initMap();
		} else {
			System.out.println("Read bad block table");
			int page = 0;
			for (int i = 0; i < remap.length; i++) {
				if (i % (512 / 4) == 0) {
					readPage(data, mapBlock, page);
				}
				remap[i] = data[i % (512 / 4)];
			}
		}
		System.out.print(badCount());
		System.out.println(" bad blocks:");
		for (int i=0; i<badCount(); ++i) {
			System.out.println("\t" + (remap[i]&0xffff) + " -> " + (remap[i]>>>16));
		}
	}

	boolean isAvailable() {
		return getNrOfBlocks() != 0;
	}

	boolean isBad(int block) {

		boolean bad = false;
		for (int i = 0; i < 2; ++i) {
			readPage(null, localSpare, block, i);
			if ((localSpare[1] & 0xffff) != 0xffff) {
				bad = true;
			}
		}
		return bad;
	}

	private void initMap() {
		for (int i=0; i<getNrOfBlocks(); ++i) {
			if (isBad(i)) {
				addBadBlock(i);
			}
		}
		updateMap();
	}
	
	private void addBadBlock(int block) {
		int cnt = badCount();
		int reservedStart = getNrOfBlocks()-remap.length-2;
		remap[cnt] = ((reservedStart+cnt)<<16) + block;
	}
	
	private boolean updateMap() {
		if (!eraseBlock(mapBlock)) {
			if (mapBlock!=getNrOfBlocks()-1) {
				System.out.println("NAND Flash is dead!");
				return false;
			}
			--mapBlock;
			if (!eraseBlock(mapBlock)) {
				System.out.println("NAND Flash is dead!");
				return false;
			}
		}
		int cnt = 0;
		int nrBad = badCount();
		for (int i=0; i<SPARE_WORDS; ++i) {
			localSpare[i] = -1;
		}
		localSpare[0] = (TYPE_BBMAP<<16) | 0xffff;
		// write mapping table, but at least an empty one
		for (int i=0; i<PAGES_PER_BLOCK & cnt<=nrBad; ++i) {
			for (int j=0; j<WORDS; ++j, ++cnt) {
				if (cnt<nrBad) {
					data[j] = remap[cnt];
				} else {
					data[j] = -1;					
				}
			}
			if (!writePage(data, localSpare,mapBlock, i)) {
				return false;
			}
		}
		return true;
	}

	private int badCount() {
		int i;
		for (i=0; i<remap.length && remap[i]!=-1; ++i) {
			;
		}
		return i;
	}
}
