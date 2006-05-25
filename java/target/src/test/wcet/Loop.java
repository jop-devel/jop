package wcet;

public class Loop {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		loop(true, 123);
	}

	public static int loop(boolean b, int val) {
		
		for (int i=0; i<10; ++i) {	//@WCA loop=10
			if (b) {
				for (int j=0; j<3; ++j) {	//@WCA loop=3
					val *= val;
				}
			} else {
				for (int j=0; j<7; ++j) {	//@WCA loop=7
					val += val;
				}
			}
		}
		
		return val;
	}
}
