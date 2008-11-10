// Put this in the make file
// P2=wcet
// P3=StartDSVM
// WCET_METHOD=main
package wcet;

import wcet.dsvmfp.*;

public class StartDSVM {

	public static void main(String[] args) {

		/* Enable one of the following lines */

		// Analyze the RT part
		TestSMO.deployRT();

		// or

		// run the whole thing
		//TestSMO.goAll();

	}

}
