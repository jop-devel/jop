package csp.test;

import com.jopdesign.io.I2CFactory;
import com.jopdesign.io.I2Cport;
import com.jopdesign.sys.RtThreadImpl;

import csp.CSP;
import csp.CSPconnection;

public class TestWatchdog {

	static final int SRC_ADDRESS = 7;
	public static final int NUM_SLAVES = 10;
	public static final int TIMEOUT = 1;


	public static void main(String[] args) {

		I2CFactory fact = I2CFactory.getFactory();

		// Source IIC
		I2Cport portA = fact.getI2CportA();
		portA.initConf(SRC_ADDRESS);

		// Initialize CSP buffer pool
		CSP.initBufferPool();

		Watchdog WD = new Watchdog(RtThreadImpl.MAX_PRIORITY, 20000);
		CSPconnection conn = new CSPconnection(SRC_ADDRESS, 0, 0, CSP.CSP_PING, CSP.CSP_PRIO_NORM, 0, portA);

		WD.connBind(conn);
		WD.slaves = new int[NUM_SLAVES];

		// Fill in slave addresses
		for(int i = 0; i < NUM_SLAVES; i++){
			WD.slaves[i] = i;
		}

		RtThreadImpl.startMission();

	}

}
