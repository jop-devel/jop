package csp.scj;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

import joprt.RtThread;

import com.jopdesign.io.I2Cport;

import csp.CSP;
import csp.CSPbuffer;
import csp.CSPconnection;
import csp.CSPmanager;

public class WatchdogHandler extends PeriodicEventHandler{

	private CSPconnection conn;
	int source;

	public WatchdogHandler(PriorityParameters priority, PeriodicParameters parameters,
			StorageParameters scp, long scopeSize) {
		super(priority, parameters, scp, scopeSize);

		init();

	}

	public void init(){

		conn = new CSPconnection(WatchDogSaflet.SRC_ADDRESS, 0, 0, CSP.CSP_PING,
				CSP.CSP_PRIO_NORM, 0, WatchDogSaflet.portA);

	}

	@Override
	public void handleAsyncEvent() {

		for (int i = 0; i < WatchDogSaflet.NUM_SLAVES; i++) {

			conn.destination = WatchDogSaflet.slaves[i];

			conn.tx_port.masterTX();

			// Send CSP ping packet
			CSPmanager.i2c_send(conn, null);

			conn.tx_port.slaveMode();

			RtThread.sleepMs(WatchDogSaflet.WD_TIMEOUT);

			if (((conn.tx_port.status & I2Cport.DATA_VALID)) == 0) {

				System.out.println("Tout : " + conn.destination);


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
				System.out.println("OK : " + source);

				CSP.freeCSPbuffer(buffer);

			}
		}


	}




}
