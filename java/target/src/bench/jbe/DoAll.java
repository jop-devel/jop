package jbe;


/**
 * Embedded Java Benchmark - JavaBenchEmbedded
 * 
 * Invoke all benchmarks
 * 
 * Versions:
 * 		V1.0	Used for the JOP thesis and various papers
 * 				Main applications are Kfl and UdpIp
 * 		V1.1	2007-04-11
 * 					Cleanup of LowLevel - other devices are included
 * 						in the single LowLevel.java in comments
 * 					Less verbose output
 * 					Print clock cycles when LowLevel.FREQU is set
 * 					Smaller packet sizes in ejip for memory constraint devices
 * 					Added Lift to the application benchmarks
 * 
 * @author admin
 *
 */
public class DoAll {

	public static void main(String[] args) {

		LowLevel.msg("JavaBenchEmbedded V1.1");
		LowLevel.lf();
/*
		Jitter.test(new BenchKfl());
		Jitter.test(new BenchUdpIp());
*/

		DoMicro.main(null);
		DoKernel.main(null);
		DoApp.main(null);
	}
			
}
