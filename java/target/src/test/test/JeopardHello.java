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
		ia[3] = 333;

		// bytecode getfield from Java:
		int ref = Native.toInt(jp);
		int val = Native.getField(ref, FIELD_OFF);
		System.out.println(val);
		
		// bytecode putfield from Java:
		Native.putField(ref, FIELD_OFF, 456);
		System.out.println(jp.field);
		

		// how to cast a integer reference (address) to an Object
		Object o = Native.toObject(ref);

		// get the reference of the array as integer (cast)
		ref = Native.toInt(ia);
		
		// bytecode arraylength in Java
		int length = Native.arrayLength(ref);
		System.out.println(length);

		// bytecode iaload from Java:
		val = Native.arrayLoad(ref, 3);
		System.out.println(val);
		
		// bytecode iastore from Java:
		Native.arrayStore(ref, 3, 333333);
		System.out.println(ia[3]);

		// Access to I/O devices via hardware objects
		
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
