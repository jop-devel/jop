package test.cyclic;

import javax.realtime.Clock;

public class Logger {
	
	static int i = 0;
	static long[] logMilis = null;
	static int[] logNanos = null;
	static String[] logEvent = null;

	
	Logger(){

		logMilis = new long[Constants.MAX_LOG_EVENTS];
		logNanos = new int[Constants.MAX_LOG_EVENTS];
		logEvent = new String[Constants.MAX_LOG_EVENTS];

	}
	
	
	public void addEvent(String message){
		
		if(Constants.ENABLE_LOG){
			Clock.getRealtimeClock().getTime(ImmortalEntry.clk);
			
			logMilis[i] = ImmortalEntry.clk.getMilliseconds();
			logNanos[i] = ImmortalEntry.clk.getNanoseconds();
			logEvent[i] = message;
			
			i++;
			if(i >= Constants.MAX_LOG_EVENTS){
				i = 0;
			}
			
			if (ImmortalEntry.eventsLogged < Constants.MAX_LOG_EVENTS){
				ImmortalEntry.eventsLogged++;
			}
		}
	}
	
	public void printDelta(int i, int j){
		long deltaMilis = logMilis[j] - logMilis[i];
		long deltaNanos = logNanos[j] - logNanos[i];
		
		System.out.println("["+deltaMilis+" : "+deltaNanos+"]");
	}


	public void printEntry(int index){
		
		System.out.println("["+logMilis[index]+" : "+logNanos[index]+"]"+" :: "+logEvent[index]);
	}
	
	public void printXXX(int index){
		
		long reference = logMilis[0];
		long next = reference + 1500*index;
		
		long delta = logMilis[index] - next;
		
		System.out.println("["+delta+"]");
	}

}
