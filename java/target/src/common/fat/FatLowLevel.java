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

public interface FatLowLevel {

	/**
	 * initialize underlying hardware
	 * @return !=0 on error
	 */
	public int Init();
	
	/**
	 * Flush buffered blocks
	 */
	public void Flush();
	
	/**
	 * returns the number of total usable bytes on the medium
	 * @return size
	 */
	public int GetTotalBytes();
	
	/**
	 * read 512 bytes
	 * @param addr
	 * @param buffer Note: this is an int array with size 512, but one entry is max 1 byte
	 * @return !=0 on error
	 */
	public int ReadSector(int addr, int[] buffer);
	
	/**
	 * write 512 bytes
	 * @param addr
	 * @param buffer Note: this is an int array with size 512, but one entry is max 1 byte
	 * @return !=0 on error
	 */
	public int WriteSector(int addr, int[] buffer);
	
	/**
	 * Clears the medium for formatting
	 */
	public void ClearMedium();
}
