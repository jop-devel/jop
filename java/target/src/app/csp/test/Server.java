package csp.test;

import com.jopdesign.io.I2Cport;

import joprt.RtThread;
import csp.CSP;
import csp.CSPbuffer;
import csp.CSPconnection;
import csp.CSPmanager;

public class Server extends RtThread {



public Server(int prio, int us) {
		super(prio, us);
	}


	CSPconnection conn;
	public int[] data;

	public void connBind(CSPconnection conn){

		this.conn = conn;

	}

	@Override
	public void run() {

		for(;;){

			conn.rx_port.flushFifo();

		// Wait until we have valid data in the rx buffer
		while (((conn.rx_port.status & I2Cport.DATA_VALID)) == 0);

		// Get one free CSPbuffer
		CSPbuffer buffer = CSP.getCSPbuffer();

		// Read the data in the RX buffer
		CSPmanager.i2c_callback(conn, buffer);

		// Process header and set missing connection parameters
		conn.prio = buffer.header[0] >>> 6;

		// Swap source and destination addresses
		conn.destination = (buffer.header[0] >>> 1) & (0x1F);
		conn.source = ( (buffer.header[0] << 4) & 0x10 ) | ((buffer.header[1] >>> 4) & 0x0F);

		// Swap source and destination ports
		conn.source_port = ( (buffer.header[1] << 2) & 0x3C ) | (buffer.header[2] >>> 6);
		conn.dest_port = buffer.header[2] & 0x3F;

		conn.res_flags = buffer.header[3];

		System.out.println("Received from " + conn.destination);

		// Change to master mode, flush buffers
//		conn.rx_port.flushFifo();
		conn.rx_port.masterTX();

		// Send CSP packet
		CSPmanager.i2c_send(conn, buffer.data);

		while((conn.tx_port.status & I2Cport.BUS_BUSY) == 1);

		// Free RX buffer
		CSP.freeCSPbuffer(buffer);

		// Can this instruction execute fast enough to avoid corruption of
		// data in the RX buffer?
		conn.rx_port.flushFifo();
		conn.rx_port.slaveMode();

		waitForNextPeriod();

	}

	// Should never reach this part

}


}
