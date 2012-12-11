package test.cyclic;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.safetycritical.Terminal;

import com.jopdesign.io.I2CFactory;
import com.jopdesign.io.I2Cport;

public class ImmortalEntry {
	
	static AbsoluteTime clk = null;
	public static Logger log = null;
	public static int eventsLogged = 0;
	public static DumpLog dumpLog = null;

	public static Terminal term = null;

	public static void setup() {
		
		clk = Clock.getRealtimeClock().getTime();
		log = new Logger();
		dumpLog = new DumpLog();
		
		term = Terminal.getTerminal();
		term.writeln("Startup...");
		
		term.writeln("Startup...");
		term.writeln("Setup ok...");

	}
	
	public static class DumpLog implements Runnable {
		
		public int logEntry = 0;
		public int selector = 0;

		@Override
		public void run() {
			switch (selector) {
			case 0:
				log.printEntry(logEntry);
				break;
				
			case 1:
				log.printDelta(logEntry, logEntry+1);
				break;
				
			case 2:
				log.printXXX(logEntry);
				break;

			default:
				break;
			}
			
		}
		
	}

}
