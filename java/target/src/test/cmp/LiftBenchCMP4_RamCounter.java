package cmp;

import jbe.BenchLift;
import jbe.BenchMark;
import jbe.LowLevel;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class LiftBenchCMP4_RamCounter {

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
			int count0 = 0;
			int count1 = 0;
			int us0 = 0;
			int us1 = 0;
			int count_result = 0;
			int us_result = 0;
			
			bm0 = new BenchLift();
			bm1 = new BenchLift();
			bm2 = new BenchLift();
			bm3 = new BenchLift();
			
			System.out.println("Bandwidth:");
			
			// Startpoint of measuring
			count0 = Native.rdMem(Const.IO_RAMCNT);
			us0 = Native.rdMem(Const.IO_CNT); // Clockcycles
			
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
				
			bm0.test(cnt/4);
			while(true){
				synchronized(lock)
				{
					if (signal == 2)
						break;
				}
			}
			
			// Endpoint of measuring
			us1 = Native.rdMem(Const.IO_CNT); // Clockcycles
			count1 = Native.rdMem(Const.IO_RAMCNT);
			
			count_result = count1 - count0;
			us_result = us1 - us0;
				
			LowLevel.msg("RAM Accesses:", count_result);
			LowLevel.lf();
			LowLevel.msg("Time us:", us_result);
			LowLevel.lf();
			LowLevel.msg("in %:", count_result/(us_result/100));
			LowLevel.lf();
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
					bm3.test(cnt/4);
					synchronized(lock){
						signal++;}
					
					while(true);
				}
			}	
		}
	}		
}

