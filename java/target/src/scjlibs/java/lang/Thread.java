/**
*	Thread.java (only for EmbeddedCoffeinMark)
*/

package java.lang;

public class Thread implements Runnable {

	public final static int MIN_PRIORITY = 1;
	public final static int NORM_PRIORITY = 5;
	public final static int MAX_PRIORITY = 10;
	
	public Thread(Runnable r) {
		
	}

	public static void yield() {
		;					// do nothing
	}

	public void run() {
		;					// do nothing
	}
	
	public void start() {
		
	}

	public static void sleep(long l) throws InterruptedException {

		int tim = (int) l;
		joprt.RtThread.sleepMs(tim);
	}
}
