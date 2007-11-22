
package cmp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;


public class Sync {		
	
	public static void main(String[] args) {

		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		int whoAmI = 33;
		Object lock1 = new Object();
		
		
		if (cpu_id == 0x00000000)
		{
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			
			while(true)
			{
				synchronized(lock1)
				{
					print_02d(whoAmI);
					whoAmI = 00;
				}
				for(int i = 0; i<10000; i++);
			}
		}
		else
		{
			if (cpu_id == 0x00000001)
			{	
				while(true)
				{
					synchronized(lock1)
					{
						whoAmI = 11;
					}
				}
			}
			else
			{
				while(true)
				{
					synchronized(lock1)
					{
						whoAmI = 22;
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