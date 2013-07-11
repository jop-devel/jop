package csp.test;

import joprt.RtThread;

import com.jopdesign.io.I2Cport;
import csp.CSP;
import csp.CSPbuffer;
import csp.CSPconnection;
import csp.CSPmanager;

public class Client extends RtThread {

	public Client(int prio, int us) {
		super(prio, us);
	}

//	int src_address;
//	int des_address;

	CSPconnection conn;
	public int[] data;

	int source;

//	Client(I2Cport tx_port, I2Cport rx_port) {
//
//		this.conn = new CSPconnection(SRC_ADDRESS, DES_ADDRESS, 0, 0,
//				CSP.CSP_PRIO_NORM, 0, tx_port, rx_port);
//		this.data = new int[5];
//
//	}

	public void connBind(CSPconnection conn){

		this.conn = conn;

	}

	@Override
	public void run() {

		for(;;){



		conn.tx_port.masterTX();

		// Payload data

		for (int i = 0; i < data.length; i++) {
			data[i] = i;
		}

		// Send CSP packet
		CSPmanager.i2c_send(conn, data);

		// Wait until we have valid data in the rx buffer
//		while (((conn.rx_port.status & I2Cport.DATA_VALID)) == 0);
//
//		// Get one free CSPbuffer
//		CSPbuffer buffer = CSP.getCSPbuffer();
//
//		// Read the data in the RX buffer
//		CSPmanager.i2c_callback(conn, buffer);
//
//		// Print received data
//		System.out.println("Received data:");
//
//		for (int i=0; i < buffer.length.length; i++){
//			System.out.println(buffer.length[i]);
//		}
//
//		for (int i=0; i < buffer.crc32.length; i++){
//			System.out.println(buffer.crc32[i]);
//		}
//
//		for (int i=0; i < buffer.header.length; i++){
//			System.out.println(buffer.header[i]);
//		}
//
//		for (int i=0; i < buffer.data.length; i++){
//			System.out.println(buffer.data[i]);
//		}

//		conn.rx_port.flushFifo();

		// Wait until bus is free and change to slave mode, flush buffers

//		while((conn.tx_port.status & I2Cport.BUS_BUSY) == 1);
//		while((conn.tx_port.status & I2Cport.BUS_BUSY) == 1);

		// Can this instruction execute fast enough to avoid curruption of
		// data in the RX buffer?
		conn.tx_port.flushFifo();
		conn.tx_port.slaveMode();

//		long now = System.currentTimeMillis();
//		long timeout = 1; // 1 msecond timeout

		sleepMs(1000);


//		while((System.currentTimeMillis() < (now + timeout)) && ((conn.tx_port.status & I2Cport.DATA_VALID) == 0)) {

//		}

		if (((conn.tx_port.status & I2Cport.DATA_VALID)) == 0){

			System.out.println("Request timeout");

		}else {

			// Get one free CSPbuffer
			CSPbuffer buffer = CSP.getCSPbuffer();

			// Read the data in the RX buffer
			CSPmanager.i2c_callback(conn, buffer);

			// Process header
			conn.prio = buffer.header[0] >>> 6;

			source = (buffer.header[0] >>> 1) & (0x1F);

//			destination = ( (buffer.header[0] << 4) & 0x10 ) | ((buffer.header[1] >>> 4) & 0x0F);

//			dest_port = ( (buffer.header[1] << 2) & 0x3C ) | (buffer.header[2] >>> 6);

//			source_port = buffer.header[2] & 0x3F;

//			res_flags = buffer.header[3];

			// Print received data
			System.out.println("Rply from "+source );

			CSP.freeCSPbuffer(buffer);

//			for (int i=0; i < buffer.length.length; i++){
//				System.out.println(buffer.length[i]);
//			}
//
//			for (int i=0; i < buffer.crc32.length; i++){
//				System.out.println(buffer.crc32[i]);
//			}
//
//			for (int i=0; i < buffer.header.length; i++){
//				System.out.println(buffer.header[i]);
//			}
//
//			for (int i=0; i < buffer.data.length; i++){
//				System.out.println(buffer.data[i]);
//			}

		}

		waitForNextPeriod();

	}

	// Should never reach this part

}

}
