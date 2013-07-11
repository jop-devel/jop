package csp.test;

import joprt.RtThread;

import com.jopdesign.io.I2Cport;

import csp.CSP;
import csp.CSPbuffer;
import csp.CSPconnection;
import csp.CSPmanager;

public class Watchdog extends RtThread {

	public Watchdog(int prio, int us) {
		super(prio, us);
	}

	CSPconnection conn;
	public int[] slaves;
	int source;

	public void connBind(CSPconnection conn) {

		this.conn = conn;

	}

	@Override
	public void run() {

		for (;;) {

			for (int i = 0; i < TestWatchdog.NUM_SLAVES; i++) {

				conn.destination = slaves[i];

				conn.tx_port.masterTX();

				// Send CSP ping packet
				CSPmanager.i2c_send(conn, null);

				// Can this instruction execute fast enough to avoid corruption
				// of data in the RX buffer?
				conn.tx_port.slaveMode();

				sleepMs(TestWatchdog.TIMEOUT);

				if (((conn.tx_port.status & I2Cport.DATA_VALID)) == 0) {

					System.out.println("Timeout " + conn.destination);
					// Slave not responding, take actions

				} else {

					// Get one free CSPbuffer
					CSPbuffer buffer = CSP.getCSPbuffer();

					// Read the data in the RX buffer
					CSPmanager.i2c_callback(conn, buffer);

					// Process header
					conn.prio = buffer.header[0] >>> 6;

					source = (buffer.header[0] >>> 1) & (0x1F);

					// destination = ( (buffer.header[0] << 4) & 0x10 ) |
					// ((buffer.header[1] >>> 4) & 0x0F);

					// dest_port = ( (buffer.header[1] << 2) & 0x3C ) |
					// (buffer.header[2] >>> 6);

					// source_port = buffer.header[2] & 0x3F;

					// res_flags = buffer.header[3];

					// Print received data
					System.out.println("Reply from " + source);

					CSP.freeCSPbuffer(buffer);

				}
			}

			waitForNextPeriod();

		}

		// Should never reach this part

	}

}
