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


import lego.lib.Speaker;
import util.Amd;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class AudioPlayback
{
	public static final int SIZE_OFFSET = 40;
	public static final int DATA_OFFSET = 44;

	
	/**
	 * Blocking.
	 * @param fileNumber Standard (PCM) WAVE file.
	 */
	public static void playFromTrivialFS(int fileNumber, int usPerSample)
	{
//		int size = TrivialFS.getFileSize(fileNumber);
							
		int address = TrivialFS.getFileAddress(fileNumber);

		int size = Amd.read(address+SIZE_OFFSET+0);
		size |= Amd.read(address+SIZE_OFFSET+1) << 8;
		size |= Amd.read(address+SIZE_OFFSET+2) << 16;
		size |= Amd.read(address+SIZE_OFFSET+3) << 24;
							
		int nextStop = Native.rd(Const.IO_US_CNT) + usPerSample;
		int current;

		for (int j = DATA_OFFSET; j < size; j++)
		{
			lego.lib.Speaker.write(Amd.read(address+DATA_OFFSET+j));
			while ((current = Native.rd(Const.IO_US_CNT)) < nextStop);

			nextStop += usPerSample;

//			waitForNextPeriod();
		}
	}

}
