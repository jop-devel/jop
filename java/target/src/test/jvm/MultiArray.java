package jvm;

public class MultiArray extends TestCase {

	public String getName() {
		return "MultiArray";
	}
	
	public boolean test() {

		boolean ok = true;

		int ia[] = new int[11];
		int val = 1;
		
		MultiArray ma[][] = new MultiArray[3][4];
		
//		Array a[] = new Array[2];
		Array a2[][] = new Array[4][5];
		


		for (int i=0; i<ia.length; ++i) {
			ia[i] = ~i;
		}
		for (int i=0; i<ia.length; ++i) {
			if (ia[i] != ~i) {
				System.out.println("Error");
				ok = false;
			}
		}
		
		int ia2[][] = new int[3][5];
		
		if (ia2.length != 3) {
			System.out.println("Size Error");
			ok = false;
		}
		for (int i=0; i<ia2.length; ++i) {
			for (int j=0; j<5; ++j) {
				ia2[i][j] = i+j;
			}
		}
		for (int i=0; i<3; ++i) {
			for (int j=0; j<5; ++j) {
				if (ia2[i][j] != i+j) {
					ok = false;
				}
			}
		}
		
		return ok;
	}


}
