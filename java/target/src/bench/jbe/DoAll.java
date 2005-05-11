package jbe;

import jbe.micro.Add;
import jbe.micro.Array;
import jbe.micro.BranchNotTaken;
import jbe.micro.BranchTaken;
import jbe.micro.GetField;
import jbe.micro.GetStatic;
import jbe.micro.Iinc;
import jbe.micro.InvokeInterface;
import jbe.micro.InvokeStatic;
import jbe.micro.InvokeVirtual;
import jbe.micro.Ldc;

public class DoAll {

	public static void main(String[] args) {

/*
		Jitter.test(new BenchKfl());
		Jitter.test(new BenchUdpIp());
*/

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
