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

/*
 * Created on 20.12.2007
 *
 * Test der Synchronisation von mehreren CPUs.
 * CPU1 und CPU2 schreiben auf ein shared Array. 
 * CPU0 gibt das Feld aus.
 * 
 */

package cmp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import jbe.LowLevel;
import cmp.IntegerClass;

public class Sync {		
	
	static Object lock1;
	static IntegerClass whoAmI;
	
	public static void main(String[] args) {

		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		
		if (cpu_id == 0x00000000)
		{
			whoAmI = new IntegerClass();
			whoAmI.numberArray = new int [5];
			lock1 = new Object();
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			int ausgabe = 3;
			
			while(true)
			{
				synchronized(lock1)
				{	
					if(whoAmI.numberArray[0] != ausgabe)
					{
						for (int i=0; i<5; i++)
						{
							print_02d(whoAmI.numberArray[i]);
						}
						ausgabe = whoAmI.numberArray[0];
					}
				}
				
				for(int i = 0; i<102197; i++); // 1627
				
			}
		}
		else
		{
			if (cpu_id == 0x00000001)
			{	 
				while(true)
				{
					for(int i = 0; i<499; i++); //499

					synchronized(lock1)
					{
						for (int i=0; i<5; i++)
						{
							whoAmI.numberArray[i] = cpu_id;
						}
					}
				}
			}
			else
			{
				while(true)
				{
					for(int i = 0; i<317; i++); //317
					
					synchronized(lock1)
					{
						for (int i=0; i<5; i++)
						{
							whoAmI.numberArray[i] = cpu_id;
						}
					}
				}
			}	
		}
	}
	
	
		static void print_02d(int i) {

		//int j;
		//for (j=0;i>9;++j) i-= 10;
		//print_char(j+'0');
		print_char(i+'0');
	}
	

	static void print_char(int i) {

		System.out.print((char) i);
		/*
		wait_serial();
		Native.wr(i, Const.IO_UART);
		*/
	}
	
	
}