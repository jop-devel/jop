package csp.test;

import com.jopdesign.io.I2CFactory;
import com.jopdesign.io.I2Cport;
import com.jopdesign.sys.RtThreadImpl;

import csp.CSP;
import csp.CSPconnection;

public class TestServer {

	public static final int SRC_ADDRESS = 7;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		I2CFactory fact = I2CFactory.getFactory();

		// Source IIC
		I2Cport portA = fact.getI2CportA();
		portA.initConf(SRC_ADDRESS);

		// Initialize CSP buffer pool
		CSP.initBufferPool();

		Server server = new Server(RtThreadImpl.MAX_PRIORITY, 0);

		CSPconnection conn = new CSPconnection(SRC_ADDRESS, 0, 0, 0, portA);
		server.connBind(conn);

		RtThreadImpl.startMission();

	}

}
