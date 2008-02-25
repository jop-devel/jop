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

package lego.utils;

import util.Amd;

public class TrivialFS
{
	public static int getFileCount()
	{
		int index = -4;
		int length = 0;
		int count = 0;
		
		while (true)
		{
			index += 4 + length;
			
			length = Amd.read(index+0) << 24;
			length |= Amd.read(index+1) << 16;
			length |= Amd.read(index+2) << 8;
			length |= Amd.read(index+3);
			
			if (length != 0)
				count++;
			else
				break;
		}
		
		return count;		
	}
	
	public static int getFileAddress(int fileNumber)
	{
		int index = -4;
		int length = 0;
		
		for (int i = 0; i <= fileNumber; i++)
		{
			index += 4 + length;
			
			length = Amd.read(index+0) << 24;
			length |= Amd.read(index+1) << 16;
			length |= Amd.read(index+2) << 8;
			length |= Amd.read(index+3);
		}
		
		return index + 4;
	}
	
	public static int getFileSize(int fileNumber)
	{
		int index = -4;
		int length = 0;
		
		for (int i = 0; i <= fileNumber; i++)
		{
			index += 4 + length;
			
			length = Amd.read(index+0) << 24;
			length |= Amd.read(index+1) << 16;
			length |= Amd.read(index+2) << 8;
			length |= Amd.read(index+3);
		}
		
		return length;		
	}
}
