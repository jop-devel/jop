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

public class DoApp {

	public static void main(String[] args) {

		LowLevel.msg("JavaBenchEmbedded V1.0");
		LowLevel.lf();
		Execute.perform(new BenchKfl());
		Execute.perform(new BenchUdpIp());
	}
			
}
