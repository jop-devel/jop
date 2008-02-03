package cmp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import jbe.LowLevel;
import jbe.BenchMark;
import jbe.BenchLift;

public class LiftBenchCMP4 {

	public static int signal = 0; 
	public static int lift0 = 0;
	public static int lift1 = 0;	
	public static int lift2 = 0;
	public static int lift3 = 0;	
	public static BenchMark bm0;
	public static BenchMark bm1;
	public static BenchMark bm2;
	public static BenchMark bm3;
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
			bm2 = new BenchLift();
			bm3 = new BenchLift();
			
			System.out.println("Application benchmarks:");
			
			start = LowLevel.timeMillis();	
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			bm0.test(cnt/4);
			while(true){
				synchronized(lock)
				{
					if (signal == 3)
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
				bm1.test(cnt/4);
				synchronized(lock){
					signal++;}
				
				while(true);
			}
			else
			{
				if (cpu_id == 0x00000002)
				{	
					bm2.test(cnt/4);
					synchronized(lock){
						signal++;}
					
					while(true);
				}
				else
				{
					if (cpu_id == 0x00000003)
					{	
						bm3.test(cnt/4);
						synchronized(lock){
							signal++;}
						
						while(true);
					}
				
				}
			}
		}	
	}		
}
