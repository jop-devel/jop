package cmp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import jbe.LowLevel;
import jbe.BenchMark;
import jbe.BenchLift;

public class LiftBenchCMP2 {

	public static boolean signal = false; 
	public static int lift0 = 0;
	public static int lift1 = 0;	
	public static BenchMark bm0;
	public static BenchMark bm1;
	public static int cnt = 16384;
	static Object lock;

	public static void main(String[] args) {

		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		if (cpu_id == 0x00000000)
		{
			// Initialization for benchmarking 
			int start = 0;
			int stop = 0;
			int time = 0;
			
			bm0 = new BenchLift();
			bm1 = new BenchLift();
			
			System.out.println("Application benchmarks:");
			
			start = LowLevel.timeMillis();	
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			bm0.test(cnt/2);
			while(true){
				synchronized(lock)
				{
					if (signal == true)
						break;
				}
			}
			stop = LowLevel.timeMillis();
		
			System.out.println("StartTime: " + start);
			System.out.println("StopTime: " + stop);
			time = stop-start;
			System.out.println("TimeSpent: " + time);
		}
		else
		{		
			if (cpu_id == 0x00000001)
			{	
				bm1.test(cnt/2);
				synchronized(lock){
					signal=true;}
				
				while(true);
			}
		}	
	}		
}
