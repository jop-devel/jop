package jbe;

public class DoApp {

	public static void main(String[] args) {

		LowLevel.msg("Application benchmarks:");
		LowLevel.lf();
		Execute.perform(new BenchKfl());
		Execute.perform(new BenchUdpIp());
		Execute.perform(new BenchLift());
	}
			
}
