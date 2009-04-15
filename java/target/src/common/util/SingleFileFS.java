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
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class SingleFileFS {
	
	int block;
	int page;
	int mode;
	
	private final int CLOSED=0;
	private final int READ=1;
	private final int WRITE=2;
	
	IllegalStateException exc;
	boolean avail;
	
	Nand nand;
	public SingleFileFS() {
		nand = new Nand();
		block = page = 0;
		exc = new IllegalStateException("SingleFileFS");
		avail = nand.isAvailable();
	}
	
	public boolean isAvailable() {
		return avail;
	}
	
	public void openRead() {
		if (!avail) {
			throw exc;
		}
		block = page = 0;
		mode = READ;
	}
	public int readPage(int data[]) {
		if (!avail || mode!=READ) {
			throw exc;
		}
		int size = nand.read(data, block, page);
		++page;
		if (page==NandLowLevel.PAGES_PER_BLOCK) {
			page=0;
			++block;
		}
		if (size!=512) {
			mode = CLOSED;
		}
		return size;
	}

	public void openWrite() {
		if (!avail) {
			throw exc;
		}
		block = page = 0;
		mode = WRITE;
	}
	
	public void writePage(int data[]) {
		if (!avail || mode!=WRITE) {
			throw exc;
		}
		nand.write(data, block, page, 512);
		++page;
		if (page==NandLowLevel.PAGES_PER_BLOCK) {
			page=0;
			++block;
		}
	}
	
	public void writeLastPage(int data[], int size) {
		if (!avail || mode!=WRITE) {
			throw exc;
		}
		nand.write(data, block, page, size);
		mode = CLOSED;
	}
	
	public void erase() {
		if (!avail || mode!=CLOSED) {
			throw exc;
		}
		int size;
		for (int i=0; i<nand.size(); ++i) {
			size = nand.read(null, i, 0);
			if (size!=-1) {
				nand.erase(i);
			}
		}
	}

	/**
	 * Erase just the first part for faster BG boot
	 */
	public void eraseStart() {
		if (!avail || mode!=CLOSED) {
			throw exc;
		}
		int size;
		int end = nand.size()/10;
		for (int i=0; i<end; ++i) {
			size = nand.read(null, i, 0);
			if (size!=-1) {
				nand.erase(i);
			}
		}
	}

	public static void main(String[] args) {
		
		int i;
		SingleFileFS fs = new SingleFileFS();
		
		int data[] = new int[128];
		System.out.println("write file");
		fs.openWrite();
		for (i=0; i<12345; ++i) {
			data[i%128] = i+4711;
			if ((i%128)==127) {
				fs.writePage(data);
			}
		}
		fs.writeLastPage(data, (i%128)*4);
		System.out.println("read file");
		fs.openRead();
		for (i=0;;) {
			int size = fs.readPage(data);
			if (size<=512) {
				for (int j=0; j<size/4; ++j) {
					if (data[j]!=i+4711) {
						System.out.println("Error in file "+size+" "+i+" "+data[j]);
					}
					++i;
				}				
			}
			if (size!=512) {
				break;
			}
		}
		if (i!=12345) {
			System.out.println("Error wrong siz");
		}
		System.out.println("erase file");
		fs.erase();
	}
}
