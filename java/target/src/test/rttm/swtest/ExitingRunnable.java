package rttm.swtest;

public class ExitingRunnable implements Runnable {

	public void run() {
		// do nothing
		
		rttm.Diagnostics.saveStatistics();
	}

}
