package lego;

import com.jopdesign.sys.*;
import joprt.RtThread;
import lego.lib.Microphone;

public class MicroTest
{
	protected static final int COUNT = 100; 
	
	public static void main(String[] args)
	{
		//int[] measure = new int[COUNT];
		
		//for (int i = 0; i < COUNT; i++)
		while (true)
		{
			//measure[i] = Native.rd(Microphone.IO_MICROPHONE);
			System.out.println(Native.rd(Microphone.IO_MICROPHONE));
			RtThread.sleepMs(1000);	// wait
		}
		
		//for (int i = 0; i < COUNT; i++)
		//	System.out.println(measure[i]);
	}
}
