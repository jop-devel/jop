package fpu;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.SoftFloat;

public class Basic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int a, b, c;
		a = SoftFloat.int32_to_float32(3);
		b = SoftFloat.int32_to_float32(2);
		Native.wrMem(a, Const.FPU_A);
		Native.wrMem(b, Const.FPU_B);
		Native.wrMem(Const.FPU_OP_ADD, Const.FPU_OP);
		c = Native.rdMem(Const.FPU_RES);
		c = SoftFloat.float32_to_int32_round_to_zero(c);
		System.out.println(c);
	}

}
