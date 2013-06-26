package csp;

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
			
			if(i == Constants.MAX_LOG_EVENTS){
				i = 0;
			}
			
			if (ImmortalEntry.eventsLogged < Constants.MAX_LOG_EVENTS){
				ImmortalEntry.eventsLogged++;
			}
		}
	}
	
	public void printEntry(int index){
		
		ImmortalEntry.term.writeln("["+logMilis[index]+" : "+logNanos[index]+"]"+" :: "+logEvent[index]);
	}

	public void printNodeEntry(int index){
		
		ImmortalEntry.term.writeln("[Node " + ImmortalEntry.slaves[index].address
				+ "] : sent " + ImmortalEntry.slaves[index].getPacketsSent()
				+ " ---" + " received " + ImmortalEntry.slaves[index].getPacketsReceived());
	}

	public static void printBuffer(Buffer buffer) {

		for (int i = 0; i < buffer.size.length; i++) {
//			ImmortalEntry.term.writeln(buffer.size[i]);
		}

		if (Constants.CSP_USE_CRC32) {
			for (int i = 0; i < buffer.crc32.length; i++) {
				System.out.println(buffer.crc32[i]);
			}
		}

		for (int i = 0; i < buffer.header.length; i++) {
			System.out.println(buffer.header[i]);
		}

		for (int i = 0; i < buffer.data.length; i++) {
			System.out.println(buffer.data[i]);
		}

	}


}
