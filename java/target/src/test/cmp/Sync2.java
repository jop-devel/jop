/*
 * Created on 28.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cmp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import jbe.LowLevel;
import cmp.IntegerClass;

public class Sync2 {		
	
	static Object lock1;
	static IntegerClass whoAmI;
	
	public static void main(String[] args) {

		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		
		if (cpu_id == 0x00000000)
		{
			whoAmI = new IntegerClass();
			lock1 = new Object();
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			
			while(true)
			{
				
//				synchronized(lock1)
//				{
					print_02d(whoAmI.number);
//				}
				
				for(int i = 0; i<100; i++);
				
//				synchronized(lock1)
//				{
//					whoAmI.number = cpu_id;
//				}
				
			}
		}
		else
		{
			if (cpu_id == 0x00000001)
			{	
				while(true)
				{
					for(int i = 0; i<31; i++);

					synchronized(lock1)
					{
						whoAmI.number = cpu_id;
					}
				}
			}
			else
			{
				while(true)
				{
					for(int i = 0; i<7; i++);
					
					synchronized(lock1)
					{
						whoAmI.number = cpu_id;
					}
				}
			}
				
		}
	}
	
	
		static void print_02d(int i) {

		int j;
		for (j=0;i>9;++j) i-= 10;
		print_char(j+'0');
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