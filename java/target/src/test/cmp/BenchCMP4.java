package cmp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import jbe.Execute;
import jbe.LowLevel;
import jbe.BenchMark;
import jbe.BenchLift;
import jbe.BenchKfl;
import jbe.BenchUdpIp;

// Benchmark of CMP with 4 JOPs

public class BenchCMP4 {

	public static int signal = 0; 
	public static int lift0 = 0;
	public static int lift1 = 0;	
	public static int lift2 = 0;	
	public static int lift3 = 0;	
	public static BenchMark bm0;
	public static BenchMark bm1;
	public static BenchMark bm2;
	public static BenchMark bm3;



	public static void main(String[] args) {

		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		if (cpu_id == 0x00000000)
		{
			bm0 = new BenchLift();
			bm1 = new BenchLift();
			bm2 = new BenchLift();
			bm3 = new BenchLift();


			Native.wrMem(0x00000001, Const.IO_SIGNAL);		
			
			LowLevel.msg("Application benchmarks:");
			LowLevel.lf();
			lift0 = Execute.performResult(bm0);
			while(signal != 3);
			
			LowLevel.msg("Lift on JOP0:", lift0);
			LowLevel.lf();
			LowLevel.msg("Lift on JOP1:", lift1);
			LowLevel.lf();
			LowLevel.msg("Lift on JOP2:", lift2);
			LowLevel.lf();
			LowLevel.msg("Lift on JOP3:", lift3);
			LowLevel.lf();
		}
		else
		{	
			if (cpu_id == 0x00000001)
			{	
				lift1 = Execute.performResult(bm1);
				signal++;
			}
			else 
			{
				if (cpu_id == 0x00000002)
				{
					lift2 = Execute.performResult(bm2);
					signal++;
				}
				else
				{
					lift3 = Execute.performResult(bm3);
					signal++;
				}
			}	
		}
		
	}
}
