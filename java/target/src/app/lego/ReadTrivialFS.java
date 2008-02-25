/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Peter Hilber and Alexander Dejaco

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

package lego;

import lego.utils.TrivialFS;

public class ReadTrivialFS
{
	static final int BLOCK_SIZE = 0x10000;
	static final int FLASH_SIZE = BLOCK_SIZE * 8;
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int count = TrivialFS.getFileCount();
		System.out.print("File count: ");
		System.out.println(count);
		
		for (int i = 0; i < count; i++)
		{
			System.out.print("File address: ");
			System.out.print(TrivialFS.getFileAddress(i));
			System.out.print(", file size: ");
			System.out.println(TrivialFS.getFileSize(i));
		}
	}
}
