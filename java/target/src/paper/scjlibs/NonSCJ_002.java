package scjlibs;

import com.jopdesign.sys.Native;

import scjlibs.util.Vector;

public class NonSCJ_002 {
	
	Vector<Object> fixture = new Vector<Object>();

	public static void main(String[] args) {
		
		String hi = "Hi";
		
		int string_address = Native.toInt(hi);
		int a = Native.rdMem(string_address);
		
		System.out.println("String object address: "+string_address);
		System.out.println(a);

//		NonSCJ_002 app = new NonSCJ_002();
//
//		int[] nums = new int[100];
//
//		try {
//			app.throwException(nums);
//		} catch (Exception e) {
//			System.out.println("Exception of type: " + e);
//		}
	}

	public void throwException(int[] nums) throws MyException {

		if (nums.length > 10) {
			throw new MyException();
		}

		System.out.println("Ok");

	}
	
	public void vectorAddTest(){
		Vector<Object> vector = new Vector<Object>();
		vector.add(new Object());
	}

	public void vectorAddTestIndex(){
		Vector<Object> vector = new Vector<Object>();
		vector.add(5, new Object());
	}

	public void vectorClearTest(){
		
		fixture.add(new Object());
		fixture.clear();
	}
	
	static class MyException extends RuntimeException {

	}

}
