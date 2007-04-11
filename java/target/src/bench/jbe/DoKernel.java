package jbe;


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
public class DoKernel {

	public static void main(String[] args) {

		LowLevel.msg("Kernel Benchmarks:");
		LowLevel.lf();
		Execute.perform(new BenchSieve());
	}
			
}
