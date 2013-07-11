package libcsp.csp;

import javax.realtime.Clock;

import libcsp.csp.util.Const;

/**
 * 
 * @author jrri
 *
 */
public class Logger {
	
	static int i = 0;
	static long[] logMilis = null;
	static int[] logNanos = null;
	static String[] logEvent = null;

	
	Logger(){

		logMilis = new long[Const.MAX_LOG_EVENTS];
		logNanos = new int[Const.MAX_LOG_EVENTS];
		logEvent = new String[Const.MAX_LOG_EVENTS];

	}
	
	
	public void addEvent(String message){
		
		if(Const.ENABLE_LOG){
			Clock.getRealtimeClock().getTime(ImmortalEntry.clk);
			
			logMilis[i] = ImmortalEntry.clk.getMilliseconds();
			logNanos[i] = ImmortalEntry.clk.getNanoseconds();
			logEvent[i] = message;
			
			i++;
			
			if(i == Const.MAX_LOG_EVENTS){
				i = 0;
			}
			
			if (ImmortalEntry.eventsLogged < Const.MAX_LOG_EVENTS){
				ImmortalEntry.eventsLogged++;
			}
		}
	}
	
	public void printEntry(int index){
		
		ImmortalEntry.term.writeln("["+logMilis[index]+" : "+logNanos[index]+"]"+" :: "+logEvent[index]);
	}
}
