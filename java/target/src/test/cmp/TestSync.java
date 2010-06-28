/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Christof Pitter

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

package cmp;

import com.jopdesign.sys.*;
import java.util.Random;

public class TestSync {

    static int cnt = 0;
    static Object mutex;

    public static void main(String[] args) {

	int cpu_id;
	cpu_id = Native.rdMem(Const.IO_CPU_ID);

	if (cpu_id == 0x00000000) 
	{
		
	    mutex = new Object();

	    System.out.println("Synchronization Test!");
	    Native.wrMem(0x00000001, Const.IO_SIGNAL);

	    Random rand = new Random();
	    
	  	for (;;) {
				synchronized (mutex) 
				{
				    int i = ++cnt;
				    if (i != cnt) 
				    {
							System.err.println("Synchronization problem.");
				    }
				}
				int r = rand.nextInt() & 0xFFFF;
				for(int j=0; j<r; j++);
	  	}
	} 
	else 
	{
	  if (cpu_id == 0x00000001) 
	  {
		  int blink = 0;
		  
		  for (;;) 
		  {
			synchronized (mutex) 
			{
				int i = --cnt;
				if (i != cnt) 
				{
					blink = ~blink;
					Native.wr(blink, Const.IO_WD);
				}
			}
		  }
	  }
	}
  }
}