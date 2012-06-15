package csp.test;

import joprt.RtThread;

import com.jopdesign.io.I2CFactory;
import com.jopdesign.io.I2Cport;
import com.jopdesign.sys.RtThreadImpl;

import csp.CSP;
import csp.CSPconnection;
//import csp.CSPmanager;

public class TestClient {

	public static final int SRC_ADDRESS = 7;
	public static final int DES_ADDRESS = 1;

	//public static BufferPool b_pool;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		I2CFactory fact = I2CFactory.getFactory();

		// Source IIC
		I2Cport portA = fact.getI2CportA();
		portA.initConf(SRC_ADDRESS);

		// Destination IIC
//		I2Cport portB = fact.getI2CportB();
//		portB.initConf(5);
//		portB.slaveMode();

		// Initialize CSP buffer pool
		CSP.initBufferPool();

		//CSPmanager manager = new CSPmanager();

//		final Client client = new Client(portA, portB);
		Client client = new Client(RtThreadImpl.MAX_PRIORITY, 2000000);
//		Server server = new Server(RtThreadImpl.MAX_PRIORITY-1, 2000);

//		CSPconnection conn = new CSPconnection(SRC_ADDRESS, DES_ADDRESS, 0, 0, CSP.CSP_PRIO_NORM, 0, portA, portB);
		CSPconnection conn = new CSPconnection(SRC_ADDRESS, DES_ADDRESS, 0, CSP.CSP_PING, CSP.CSP_PRIO_NORM, 0, portA);
		client.connBind(conn);

		client.data = new int[11];

//		for (int i = 0; i < client.data.length; i++) {
//			System.out.println("good");
//			client.data[i] = i;
//		}


		RtThreadImpl.startMission();

//		new RtThread(client, RtThreadImpl.MAX_PRIORITY, 2000){
//
//			public void run(){
//
//				for(;;){
//
//				client.run();
//				waitForNextPeriod();
//
//				}
//
//			}
//
//
//		};




//		manager.i2c_send(conn, data);

	}

}
