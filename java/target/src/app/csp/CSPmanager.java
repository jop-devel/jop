package csp;

import com.jopdesign.io.I2Cport;

public class CSPmanager {

	static CRC32 crc32 = new CRC32();

	public static final int CSP_PING = 0;

	public CSPmanager() {

	}

	/**
	 *
	 *
	 * @param conn
	 *            : Reference to CSP connection
	 * @param data
	 *            : Payload for the CSP packet
	 */
	public static void i2c_send(CSPconnection conn, int[] data) {

		// Get a CSP buffer
		CSPbuffer buffer = CSP.getCSPbuffer();

		if(data == null){
			buffer.data = null;
		}

		if (data != null) {
			buffer.length[0] = data.length >>> 8;
			buffer.length[1] = data.length & 0x000000FF;
		}

		// Header data
		buffer.header[0] = (conn.prio << 6) | (conn.source << 1)
				| ((conn.destination & 0x00000010) >>> 4);
		buffer.header[1] = ((conn.destination & 0x0000000F) << 4)
				| ((conn.dest_port & 0x0000003C) >>> 2);
		buffer.header[2] = ((conn.dest_port & 0x00000003) << 6)
				| (conn.source_port);
		buffer.header[3] = (conn.res_flags);

		// Payload data
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				buffer.data[i] = data[i];
			}
		}

		if (Conf.CSP_USE_CRC32) {

			// Set the CRC32 field to zero for its calculation
			for (int i = 0; i < buffer.crc32.length; i++) {
				buffer.crc32[i] = 0;
			}

			// Calculate the CRC value and add it to the frame:
			crc32.reset();

			crc32.update(buffer.length);
			crc32.update(buffer.crc32);
			crc32.update(buffer.header);
			crc32.update(buffer.data);

			int crc_value = crc32.getValue();

			buffer.crc32[0] = (crc_value & 0xFF000000) >>> 24;
			buffer.crc32[1] = (crc_value & 0x00FF0000) >>> 16;
			buffer.crc32[2] = (crc_value & 0x0000FF00) >>> 8;
			buffer.crc32[3] = (crc_value & 0x000000FF);

		}

		// Now the i2cFrame should be ready
		conn.tx_port.CSPwrite(conn.destination * 2, buffer);

		System.out.println("Packet sent " + conn.destination);

		// Return only when the data has been transmitted
		while ((conn.tx_port.status & I2Cport.BUS_BUSY) == 1);

		CSP.freeCSPbuffer(buffer);
	}

	/**
	 *
	 *
	 * @param packet
	 *            : A CSP packet
	 * @param port
	 *            : The I2C port where the packet was received
	 */
	public static void i2c_callback(CSPconnection conn, CSPbuffer buffer) {

		conn.rx_port.CSPreadBuffer(buffer);

	}

	// public void csp_service_handler(CSPpacket packet, I2Cport port) {
	//
	// switch (packet.header.dest_port) {
	//
	// case CSP_PING:
	// /* A ping means, just echo the packet, so no changes */
	// i2c_send(packet, port);
	// return;
	//
	// default:
	// System.out.println("Unknown port");
	// return;
	// }
	//
	// }

}
