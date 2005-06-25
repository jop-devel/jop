package vmtest;


public class Array {

	public static void main(String[] agrgs) {

		boolean ok = true;
		
		System.out.print("Array test ");
		int ia[] = new int[3];
		int val = 1;
/*
		System.out.println("iaload");
		val = ia[0];
		val = ia[2];

		System.out.println("iastore");
		ia[0] = val;
		ia[2] = val;
*/
		if (ia.length!=3) {
			System.out.println("Error - array.length");
			ok = false;
		}
		for (int i=0; i<ia.length; ++i) {
			ia[i] = ~i;
		}
		for (int i=0; i<ia.length; ++i) {
			if (ia[i] != ~i) {
				System.out.println("Error in array");
				ok = false;
			}
		}

//		System.out.println("iaload bound");
//		val = ia[-1];
//		val = ia[3];

//		System.out.println("iastore bound");
//		ia[-1] = val;
//		ia[3] = val;

//		np(null);
		if (ok) {
			System.out.println("ok");			
		}

	}

	public static void np(int[] ia) {

		int val = 1;
		System.out.println("iaload null pointer");
//		val = ia[1];
		System.out.println("iastore null pointer");
		ia[1] = val;
	}

}
