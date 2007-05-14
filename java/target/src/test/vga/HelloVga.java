package vga;

import util.*;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

public class HelloVga {

	public static void main(String[] agrgs) {
    
    int addr = 0;
		int offset = 0x28000;
		boolean j = true;
		
		Native.wr(0, Const.IO_WD);		// make WD happy
		Native.wr(1, Const.IO_WD);
		Native.wr(0, Const.IO_WD);
		Native.wrMem(0x11111111, 0x28000); // first VGA Address	
		Native.wrMem(0x11111111, 0x3ffff); // last VGA Address
		
		for (int i=0x0; i<0x18000; i++)
		{
			addr = i + offset;
			if( i <= 1024)
			{
				Native.wrMem(0x22222222, addr); // rot
			}
			else if ( (i > 1024) && (i <= 2048) )
			{
				Native.wrMem(0xffffffff, addr); // gelb
			}
			else if ( (i > 2048) && (i <= 3072) ) 
			{
				Native.wrMem(0x33333333, addr); // orange
			}
			else if ( (i > 97280) && (i <= 98304) )
			{
				Native.wrMem(0xffffffff, addr); // gelb
			}
			else
			{
				Native.wrMem(0xCCCCCCCC, addr); // grün 
			}
		}
		          		
		System.out.println("Hello World from JOP!");
 
		for (;;) {
			Timer.wd();
			int i = Timer.getTimeoutMs(500);
			while (!Timer.timeout(i)) {
				;
			}
		}
	}
}
