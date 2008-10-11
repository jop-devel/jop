package javax.realtime;

// we have no java.lang.Thread on JOP - shortcut to Runnable

public class RealtimeThread implements Runnable {
	
	public RealtimeThread() {
		
	}

	public static RealtimeThread currentRealtimeThread() {
		// TODO: we need to map the CPUID to a RealtimeThread object
		// it's a fake, but should be ok for our current model
		return null;
	}
	
	public void start() {
		
	}

	public void run() {
		
	}
}
