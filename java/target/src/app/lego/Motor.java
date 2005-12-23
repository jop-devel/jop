/*
 * Created on 22.12.2005
 *
 */
package lego;

import com.jopdesign.sys.*;

public class Motor {

	final static int IO_MOTOR = Const.IO_LEGO;

	final static int M_EN = 4;
	final static int M_L1 = 2;
	final static int M_L2 = 1;

	private static int val;
	
	private int shift;
	
	public Motor(int nr) {
		shift = nr*3;
	}
	
	public void forward() {
		int mask = ~(0x7<<shift);
		val &= mask;
		val |= (M_EN|M_L1)<<shift;
		Native.wr(val, IO_MOTOR);
	}
	public void backward() {
		int mask = ~(0x7<<shift);
		val &= mask;
		val |= (M_EN|M_L2)<<shift;
		Native.wr(val, IO_MOTOR);
	}
	public void stop() {
		int mask = ~(0x7<<shift);
		val &= mask;
		val |= (M_EN)<<shift;
		Native.wr(val, IO_MOTOR);
	}
	public void open() {
		int mask = ~(0x7<<shift);
		val &= mask;
		Native.wr(val, IO_MOTOR);
	}
}
