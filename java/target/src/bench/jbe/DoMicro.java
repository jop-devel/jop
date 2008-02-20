package jbe;

import jbe.micro.*;

/**
 * Embedded Java Benchmark - JavaBenchEmbedded
 * 
 * Invoke all benchmarks
 * 
 * Versions:
 * 		V1.0	Used for the JOP thesis and various papers
 * 				Main applications are Kfl and UdpIp
 * 		V1.1	2007-04-11 cleanup of LowLevel - other devices
 * 				are included in the single LowLevel.java in comments
 * 
 * @author admin
 *
 */
public class DoMicro {

	public static void main(String[] args) {

		LowLevel.msg("Micro Benchmarks:");
		LowLevel.lf();

//		Execute.perform(new Fadd());
		Execute.perform(new Add());
		Execute.perform(new Iinc());
		Execute.perform(new Ldc());
		Execute.perform(new BranchTaken());
		Execute.perform(new BranchNotTaken());
		Execute.perform(new GetField());
		Execute.perform(new GetStatic());
		Execute.perform(new Array());
		Execute.perform(new InvokeVirtual());
		Execute.perform(new InvokeStatic());
		Execute.perform(new InvokeInterface());
	}
			
}
