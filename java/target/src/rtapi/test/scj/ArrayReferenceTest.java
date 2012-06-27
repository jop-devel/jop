package test.scj;

import javax.safetycritical.Terminal;

import com.jopdesign.sys.GC;
import com.jopdesign.sys.Memory;
import com.jopdesign.sys.Native;

public class ArrayReferenceTest {
	
	public static Terminal terminal;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		terminal = Terminal.getTerminal();
		
		terminal.writeln("Hello world");
		
		MyWorker mw = new MyWorker();
		
		Memory mem = new Memory(1024, 2048);
		mem.enter(mw);
	}
}


class MyWorker implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		ArrayReferenceTest.terminal.writeln("Hello from scope");
		
		Object[] Array = new Object[10];
		
		// Add GC.getOffSpace() method to GC.java
		int i = Native.rdMem(Native.toInt(Array) + GC.OFF_SCOPE_LEVEL);
		System.out.println(Native.toInt(Array) + GC.OFF_SCOPE_LEVEL);
		ArrayReferenceTest.terminal.writeln("Array level: " +i); // Expected value: 1
		
		for (int j = 0; j < Array.length; j++){
			Array[j] = new Object();
			
		}
		
		// Pick a random element in the array...
		int j = Native.rdMem(Native.toInt(Array[0]) + GC.OFF_SCOPE_LEVEL);
		System.out.println(Native.toInt(Array[0]) + GC.OFF_SCOPE_LEVEL);
		ArrayReferenceTest.terminal.writeln("Array object level: " + j); // Expected value: 1
		
		i = Native.rdMem(Native.toInt(Array) + GC.OFF_SCOPE_LEVEL);
		System.out.println(Native.toInt(Array) + GC.OFF_SCOPE_LEVEL);
		ArrayReferenceTest.terminal.writeln("Array level after storing elements: " +i); 
												// Expected value: 1, as storing references
												// in array elements should not change the 
												// array object itself.
	}
	
	
	
	
}
