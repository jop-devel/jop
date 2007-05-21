package lego;

import joprt.RtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class PLDTest {
	
	public static void main(String[] args) 
	{
		while (true)
		{	
			//System.out.println("Version 0");
			int i = 0;
			while (true)
			{
				Native.wr(i, Const.IO_LEGO);
				System.out.println(Native.rd(Const.IO_LEGO));
				RtThread.sleepMs(300);
				i += 0x2000;
			}
		}
	}
}
