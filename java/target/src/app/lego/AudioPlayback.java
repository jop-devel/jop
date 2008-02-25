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

public class AudioPlayback
{

//	static final int SAMPLERATE = 22050;
//	static final int SAMPLERATE = 48000;
	static final int SAMPLERATE = 44100;

	static final int US_PER_SAMPLE = 23; // more correct for 44100 Hz 
	//1000000/SAMPLERATE;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		new RtThread(10, 1000000/SAMPLERATE)
//		{
//		public void run()
		{
			System.out.print("Samplerate: ");
			System.out.println(SAMPLERATE);
			System.out.print("us/sample: ");
			System.out.println(US_PER_SAMPLE);

			while (true)
			{
				int count = TrivialFS.getFileCount();

				for (int i = 0; i < count; i++)
					lego.utils.AudioPlayback.playFromTrivialFS(i, US_PER_SAMPLE);
			}
//			}
//			};

//			RtThread.startMission();
//			}
		}
	}
}