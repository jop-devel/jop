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
 * <p>Usage of spare bytes:<br>
 * B0-B1: type<br>
 * B2-B3: size for the last page<br>
 * B4-B5: logical block number<br>
 * B6-B7: bad block marker (!= 0xffff); in 1st and 2nd page<br>
 * B8-B9: TBD<br>
 * B10-B11: TBD<br>
 * B12-B13: TBD<br>
 * B14-B15: checksum<br>
 * 
 * Idea: we could go down on BB table (mapBlock) till meeting the actual used
 * blocks (badCount).
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
	int[] localData = new int[WORDS];
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
		System.out.println("Read bad block table");
		if (!readBBTable()) {
			System.out.println("Initializing NAND Flash");
			initMap();
		}
		System.out.print(badCount());
		System.out.println(" bad blocks:");
		for (int i = 0; i < badCount(); ++i) {
			System.out.println("\t" + (remap[i] & 0xffff) + " -> "
					+ (remap[i] >>> 16));
		}
		System.out.println("map block " + mapBlock);
	}

	public boolean isAvailable() {
		return getNrOfBlocks() != 0;
	}

	/**
	 * Size in usable 16 KB blocks.
	 * 
	 * @return
	 */
	public int size() {
		return getNrOfBlocks() - remap.length - 2;
	}

	/**
	 * Erase one block.
	 * 
	 * @param block
	 */
	public void erase(int block) {
		block = getPhysicalBlock(block);
		if (!eraseBlock(block)) {
			addBadBlock(block);
			markBad(block);
			block = getPhysicalBlock(block);
			eraseBlock(block);
		}
	}

	/**
	 * Write one block marked as data type.
	 * 
	 * @param data
	 * @param block
	 * @param page
	 * @return
	 */
	public boolean write(int[] data, int block, int page, int size) {

		int origBlock = getPhysicalBlock(block);
		fillSpare(localSpare, TYPE_DATA, origBlock, size);
		block = getPhysicalBlock(block);
		boolean ret = writePage(data, localSpare, block, page);

		retry: while (!ret) {
			// int markBlock = block;
			// copy content
			addBadBlock(block);
			if (LOG) {
				System.out.print("Copy write block ");
				System.out.print(block);
			}
			block = getPhysicalBlock(block);
			if (LOG) {
				System.out.print(" to ");
				System.out.print(block);
			}
			for (int i = 0; i < page; ++i) {
				readPage(localData, origBlock, i);
				fillSpare(localSpare, TYPE_DATA, origBlock);
				if (!writePage(localData, localSpare, block, i)) {
					markBad(block);
					continue retry;
				}
			}
			// write page
			fillSpare(localSpare, TYPE_DATA, origBlock, size);
			if (!writePage(data, localSpare, block, page)) {
				continue retry;
			}
			// mark the former black as bad
			// not now as dual bad block would destroy the
			// original block
			// markBad(markBlock);
			ret = true;
		}

		return ret;
	}
	
	/**
	 * Read one page and return the size of the page.
	 * 
	 * @param data
	 * @param block
	 * @param page
	 * @return
	 */
	public int read(int[] data, int block, int page) {
		block = getPhysicalBlock(block);
		boolean ok = readPage(data, localSpare, block, page);
		return ok ? (localSpare[0] & 0xffff) : -1;
	}


	private void fillSpare(int[] sp, int type, int logicBlock) {
		for (int i = 0; i < SPARE_WORDS; ++i) {
			sp[i] = -1;
		}
		sp[0] = (type << 16) | 0xffff;
		sp[1] = (logicBlock << 16) | 0xffff;
	}

	private void fillSpare(int[] sp, int type, int logicBlock, int size) {
		for (int i = 0; i < SPARE_WORDS; ++i) {
			sp[i] = -1;
		}
		sp[0] = (type << 16) | size;
		sp[1] = (logicBlock << 16) | 0xffff;
	}

	int getPhysicalBlock(int block) {
		for (int maxIndir = 0; maxIndir < 3; ++maxIndir) {
			for (int i = 0; i < badCount(); ++i) {
				if ((remap[i] & 0xffff) == block) {
//					if (LOG) {
//						System.out.print("remap ");
//						System.out.print(block);
//					}
					block = remap[i] >> 16;
//					if (LOG) {
//						System.out.print(" -> ");
//						System.out.println(block);
//					}
				}
			}
		}
		return block;
	}

	/**
	 * Detect if BB table exists and read it in.
	 * 
	 * @return
	 */
	private boolean readBBTable() {
		readPage(null, localSpare, mapBlock, 0);
		if ((localSpare[0] >>> 16) != TYPE_BBMAP) {
			return false;
		}
		int page = 0;
		for (int i = 0; i < remap.length; i++) {
			if (i % (512 / 4) == 0) {
				if (!readPage(localData, mapBlock, page)) {
					return false;
				}
				++page;
			}
			remap[i] = localData[i % (512 / 4)];
		}
		return true;
	}

	private boolean isBad(int block) {

		boolean bad = false;
		for (int i = 0; i < 2; ++i) {
			readPage(null, localSpare, block, i);
			if ((localSpare[1] & 0xffff) != 0xffff) {
				bad = true;
			}
		}
		return bad;
	}

	private void markBad(int block) {
		for (int i = 0; i < SPARE_WORDS; ++i) {
			localSpare[i] = -1;
		}
		localSpare[1] &= 0xffff0000;
		eraseBlock(block);
		writePage(null, localSpare, block, 0);
		writePage(null, localSpare, block, 1);

	}

	private void initMap() {
		for (int i = 0; i < getNrOfBlocks() - 2; ++i) {
			if (isBad(i)) {
				addBadBlock(i);
			}
		}
		updateMap();
	}

	private void addBadBlock(int block) {

		if (LOG) {
			System.out.print("Mark bad block ");
			System.out.println(block);
		}
		int cnt = badCount();
		int reservedStart = getNrOfBlocks() - remap.length - 2;
		
		// Check whether cnt is not higher than the maximum number of
		// spare blocks. Otherwise referencing remap[cnt] will result
		// in an ArrayIndexOutOfBoundsException.
		if (cnt >= getNrOfBlocks() / 16 - 2) {
			System.out.println("Not enough spare blocks available.");
			System.exit(1);
		}
		
		remap[cnt] = ((reservedStart + cnt) << 16) + block;
		// if not yet marked in the spare do it.
		updateMap();
	}

	private void updateMap() {

		if (!updateMap(mapBlock)) {
			markBad(mapBlock);
			mapBlock--;
			if (mapBlock < getNrOfBlocks() - 2 || isBad(mapBlock)) {
				System.out.println("NAND Flash is dead!");
			}
			if (!updateMap(mapBlock)) {
				markBad(mapBlock);
				System.out.println("NAND Flash is dead!");
			}
		}
	}

	/**
	 * Write the mapping of bad blocks into one block.
	 * 
	 * @param mapIdx
	 *            Block to be used
	 * @return returns false if not possible (e.g., due to use a bad block for
	 *         the mapping)
	 */
	private boolean updateMap(int mapIdx) {
		if (!eraseBlock(mapIdx)) {
			return false;
		}
		int cnt = 0;
		int nrBad = badCount();
		fillSpare(localSpare, TYPE_BBMAP, -1);
		// write mapping table, but at least an empty one
		for (int i = 0; i < PAGES_PER_BLOCK && cnt <= nrBad; ++i) {
			for (int j = 0; j < WORDS; ++j, ++cnt) {
				if (cnt < nrBad) {
					localData[j] = remap[cnt];
				} else {
					localData[j] = -1;
				}
			}
			// TODO: add ECC
			if (!writePage(localData, localSpare, mapBlock, i)) {
				return false;
			}
		}
		return true;
	}

	private int badCount() {
		int i;
		for (i = 0; i < remap.length && remap[i] != -1; ++i) {
			;
		}
		return i;
	}
	
	public static void main(String args[]) {
		
		Nand nand = new Nand();
		System.out.println(nand.readBBTable());
		// nand.eraseAll();
		
	}
}
