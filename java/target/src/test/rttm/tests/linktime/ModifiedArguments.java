package rttm.tests.linktime;

import rttm.Atomic;

public class ModifiedArguments {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ModifiedArguments().foo(null, 0, 0, 0);
	}

	@Atomic void foo(Object modified, int notModified, long modifiedLong, 
			int modifiedByIncrement) {
		int notAnArgument = 0xf00;		
		
		modifiedLong = 2;
		modified = this;
		modifiedByIncrement++;
	}
}
