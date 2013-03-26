package scjlibs;

import javax.realtime.Clock;

public class Logger {

	int totalLoggedEvents = 0;
	static long[] logMilis = null;
	static int[] logNanos = null;
	static StringBuffer[] logEvent = null;

	Logger() {

		logMilis = new long[Constants.MAX_LOG_EVENTS];
		logNanos = new int[Constants.MAX_LOG_EVENTS];
		logEvent = new StringBuffer[Constants.MAX_LOG_EVENTS];
		for (int i = 0; i < logEvent.length; i++) {
			logEvent[i] = new StringBuffer();
		}

	}

	public void addEvent(String message) {

		if (Constants.ENABLE_LOG) {
			Clock.getRealtimeClock().getTime(ImmortalEntry.clk);

			logMilis[totalLoggedEvents] = ImmortalEntry.clk.getMilliseconds();
			logNanos[totalLoggedEvents] = ImmortalEntry.clk.getNanoseconds();
			logEvent[totalLoggedEvents].append(message);

			totalLoggedEvents++;

			if (totalLoggedEvents == Constants.MAX_LOG_EVENTS) {
				totalLoggedEvents = 0;
			}

			if (ImmortalEntry.eventsLogged < Constants.MAX_LOG_EVENTS) {
				ImmortalEntry.eventsLogged++;
			}
		}
	}
	
	public void addEvent(int message) {

		if (Constants.ENABLE_LOG) {
			Clock.getRealtimeClock().getTime(ImmortalEntry.clk);

			logMilis[totalLoggedEvents] = ImmortalEntry.clk.getMilliseconds();
			logNanos[totalLoggedEvents] = ImmortalEntry.clk.getNanoseconds();
			logEvent[totalLoggedEvents].append(message);

			totalLoggedEvents++;

			if (totalLoggedEvents == Constants.MAX_LOG_EVENTS) {
				totalLoggedEvents = 0;
			}

			if (ImmortalEntry.eventsLogged < Constants.MAX_LOG_EVENTS) {
				ImmortalEntry.eventsLogged++;
			}
		}
	}

	/** Dumps the time content of a particular entry
	 * 
	 * @param index Index of the entry to be dumped
	 * 
	 */
	public void dumpEntry(int index) {

		ImmortalEntry.term.writeln("[" + logMilis[index] + " : "
				+ logNanos[index] + "]" + " :: " + logEvent[index]);
	}
	
	/** Dumps the time difference between two entries
	 * 
	 * @param i Index of the second entry to be compared
	 * @param j Index of the first entry to be compared
	 */
	public void dumpDelta(int i, int j){
		
		long deltaMilis = logMilis[j] - logMilis[i];
		long deltaNanos = logNanos[j] - logNanos[i];
		
		ImmortalEntry.term.writeln("["+deltaMilis+" : "+deltaNanos+"]");
	}

}
