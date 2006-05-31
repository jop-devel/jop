package wcet;

import wcet.kflapp.Mast;

public class StartKfl {


	public static void main(String[] args) {

		// initialization
		Mast.main(null);
		// KFL main loop
		for (int i=0; i<100; ++i) { // @WCA loop=100
			Mast.loop();
		}
	}
			
}
