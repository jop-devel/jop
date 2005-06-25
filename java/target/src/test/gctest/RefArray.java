/*
 * Created on 24.06.2005
 *
 */
package gctest;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class RefArray {

	static RefArray ra[];
	
	int abc;
	
	public RefArray(int val) {
		abc = val;
	}
	
	public int getVal() {
		return abc;
	}
	
	public static void main(String[] args) {
		
		for (int i=0; i<4000; i+=10) {
			
			System.out.print("*");
			ra = new RefArray[i];
			for (int j=0; j<i; ++j) {
				ra[j] = new RefArray(i*1000+j);
			}
			
			for (int j=0; j<i; ++j) {
				if (ra[j].abc != i*1000+j) {
					System.out.println("Error: RefArray problem.");
					System.exit(1);
				}
			}
		}
	}
}
