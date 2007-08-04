package test;

public class DoAll {

	/**
	 * Invoke all tests we have so far
	 * @param args
	 */
	public static void main(String[] args) {
		
		jvm.DoAll.main(args);
		jdk.DoAll.main(args);
		Flash.main(args);
		jbe.DoAll.main(args);
		
	}

}
