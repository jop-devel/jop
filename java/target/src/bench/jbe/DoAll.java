package jbe;

import jbe.micro.*;

public class DoAll {

	public static void main(String[] args) {

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
		Execute.perform(new BenchSieve());
		Execute.perform(new BenchKfl());
		Execute.perform(new BenchUdpIp());
	}
			
}
