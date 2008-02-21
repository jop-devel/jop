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