package test;

import com.jopdesign.io.JeopardIOFactory;
import com.jopdesign.io.SerialPort;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public class JeopardHello {

	final static int FIELD_OFF = 0;
	int field;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int ia[] = new int[5];
		
		JeopardHello jp = new JeopardHello();
		jp.field = 123;
		
		int ref = Native.toInt(jp);
		
		/************/
		
		Object o = Native.toObject(ref);
		
		// TODO: Native.getField(ref, off);
		
		// TODO: null pointer check
		/* handle indirection */
		int addr = Native.rdMem(ref);
		int val = Native.rdMem(addr+FIELD_OFF);
		System.out.println(val);
		
		
		ref = Native.toInt(ia);
		int length = Native.rdMem(ref+GC.OFF_MTAB_ALEN);
		addr = Native.rdMem(ref);
		System.out.println(addr);
		System.out.println(length);
		
		// let's get the control channel from the factory
		SerialPort cc = JeopardIOFactory.getJeopardIOFactory().getControlPort();
		
		// writing is easy:
		cc.data = 123;
		
		// but we should check the status - now as busy wait
		while ((cc.status & SerialPort.MASK_TDRE) == 0) {
			;
		}
		cc.data = 456;
		
		// poll for read
		if ((cc.status & SerialPort.MASK_RDRF) != 0) {
			val = cc.data;
		}
		
		
		// TODO: Native.getArrayElement(ref, index);
		
	}

}
