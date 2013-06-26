package scjlibs;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.safetycritical.Terminal;

import com.jopdesign.sys.Memory;

public class ImmortalEntry {

	static AbsoluteTime clk = null;
	public static Logger log = null;
	public static int eventsLogged = 0;
	public static DumpLog dumpLog = null;
	public static Terminal term = null;

	static public StringBuffer memArea = null;
	static public Memory mem = null;

	static public Object initMonitor = new Object();

	public static void setup() {

		clk = Clock.getRealtimeClock().getTime();
		log = new Logger();
		dumpLog = new DumpLog();
		memArea = new StringBuffer();

		term = Terminal.getTerminal();
		term.writeln("Setup ok...");

	}

	public static void memStats(Memory mem) {
		
		memArea.delete(0, memArea.length());

		switch (mem.level) {

		case 0:
			memArea.append("Immortal");
			break;
		case 1:
			memArea.append("Mission");
			break;
		case 2:
			memArea.append("Private");
			break;
		default:
			memArea.append("Nested private");
			break;

		}

		System.out.println("");
		System.out.println("============ " + memArea + " memory stats ============");
		System.out.println("Mem. size: " + mem.size());
		System.out.println("Mem. remaining: " + mem.memoryRemaining());
		System.out.println("Mem. consumed: " + mem.memoryConsumed());
		System.out.println("Bs. remaining: " + mem.bStoreRemaining());
		System.out.println("============ " + memArea + " memory stats ============");
		System.out.println("");
		
	}
	
	public static void memStats() {

		memStats(Memory.getCurrentMemory());

	}

	public static class DumpLog implements Runnable {

		/**
		 * An entry in the log table
		 */
		public int logEntry = 0;
		/**
		 * Choose what type of information to print
		 */
		public int selector = 0;

		@Override
		public void run() {

			switch (selector) {
			case 0:
				log.dumpEntry(logEntry);
				break;
			case 1:
				log.dumpDelta(logEntry, logEntry + 1);
			default:
				break;
			}

		}

	}

}
