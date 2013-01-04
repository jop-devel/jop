package csp;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.RawMemory;
import javax.safetycritical.Terminal;

import com.jopdesign.io.I2CFactory;
import com.jopdesign.sys.Const;

import csp.scj.watchdog.GeneralIOFactory;
import csp.scj.watchdog.I2CBusController;
import csp.scj.watchdog.I2CInterface;

public class ImmortalEntry {

	static AbsoluteTime clk = null;
	public static Logger log = null;
	public static int eventsLogged = 0;
	public static DumpLog dumpLog = null;

	public static Terminal term = null;
	static I2CFactory fact = null;

	// public static I2Cport portA = null;
	// public static I2Cport portB = null;
	public static I2CBusController portA = null;
	public static I2CBusController portB = null;

	public static I2CInterface i2c_a = null;
	public static I2CInterface i2c_b = null;
	
	public static Node[] slaves;

	public static BufferPool bufferPool = null;
	public static ConnectionPool connectionPool = null;

	public static CRC32 crc32 = null;

	public static Connection ping = null;

	public static void setup() {

		clk = Clock.getRealtimeClock().getTime();
		log = new Logger();
		dumpLog = new DumpLog();

		term = Terminal.getTerminal();
		term.writeln("Startup...");

		fact = I2CFactory.getFactory();

		GeneralIOFactory factory = new GeneralIOFactory();
		RawMemory.registerAccessFactory(factory);

		// Source IIC
		// portA = fact.getI2CportA();
		// portA.initialize(Constants.DEVICE_A_ADDR, false);
		portA = new I2CBusController(Const.I2C_A_BASE);
		portA.initialize(Constants.DEVICE_A_ADDR, false);
		i2c_a = new I2CInterface(portA);

		// Destination IIC
		// portB = fact.getI2CportB();
		// portB.initialize(Constants.DEVICE_B_ADDR, false);
		portB = new I2CBusController(Const.I2C_B_BASE);
		portB.initialize(Constants.DEVICE_B_ADDR, false);
		i2c_b = new I2CInterface(portB);

		
		// Initialize CSP buffer pool
		bufferPool = new BufferPool();

		// Initialize CSP connection pool
		connectionPool = new ConnectionPool();

		if (Constants.CSP_USE_CRC32) {
			crc32 = new CRC32();
		}

		ping = connectionPool.getConnection(Constants.DEVICE_A_ADDR, 0,
				Constants.CSP_PING, 0, Constants.CSP_PRIO_NORM, 0, i2c_a, 1);

		slaves = new Node[Constants.NUM_SLAVES];

		for (int i = 0; i < Constants.NUM_SLAVES; i++) {
			slaves[i] = new Node(i*3);
		}

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
				log.printNodeEntry(logEntry);

			default:
				break;
			}
			
		}

	}
	
	public static void initialParams(){
		term.writeln("Max. CSP buffers: "+Constants.MAX_BUFFER_COUNT);
		term.writeln("Max. Connections: "+Constants.CSP_MAX_CONNECTIONS);
		term.writeln("Max. payload (words): "+ Constants.MAX_PAYLOAD_SIZE);
	}

}
