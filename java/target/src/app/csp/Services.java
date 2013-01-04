package csp;

public class Services {

	public static final int CSP_PING = 0;

	public Services() {

	}

	/**
	 * Sends a CSP packet using the specified connection. The data is packed into a CSP buffer.
	 *
	 * @param connection
	 *            Reference to CSP connection
	 * @param data
	 *            Payload for the CSP packet
	 */
	public static void sendPacket(Connection connection, int[] data) {

		// Get a free CSP buffer
		Buffer buffer = ImmortalEntry.bufferPool.getCSPbuffer();

		if(data == null){
			buffer.data = null;
		}else{
			buffer.size[0] = data.length >>> 8;
			buffer.size[1] = data.length & 0x000000FF;
		}

		// Header data
		buffer.header[0] = (connection.prio << 6) | (connection.source << 1)
				| ((connection.destination & 0x00000010) >>> 4);
		buffer.header[1] = ((connection.destination & 0x0000000F) << 4)
				| ((connection.dest_port & 0x0000003C) >>> 2);
		buffer.header[2] = ((connection.dest_port & 0x00000003) << 6)
				| (connection.source_port);
		buffer.header[3] = (connection.res_flags);

		// Payload data
		if (data != null) {
			for (int i = 0; i < data.length; i++) {  //@WCA loop = Constants.MAX_PAYLOAD_SIZE
				buffer.data[i] = data[i];
			}
		}

		if (Constants.CSP_USE_CRC32) {

			// Set the CRC32 field to zero for its calculation
			for (int i = 0; i < buffer.crc32.length; i++) {
				buffer.crc32[i] = 0;
			}

			// Calculate the CRC value and add it to the frame:
			ImmortalEntry.crc32.reset();

			ImmortalEntry.crc32.update(buffer.size);
			ImmortalEntry.crc32.update(buffer.crc32);
			ImmortalEntry.crc32.update(buffer.header);
			ImmortalEntry.crc32.update(buffer.data);

			int crc_value = ImmortalEntry.crc32.getValue();

			buffer.crc32[0] = (crc_value & 0xFF000000) >>> 24;
			buffer.crc32[1] = (crc_value & 0x00FF0000) >>> 16;
			buffer.crc32[2] = (crc_value & 0x0000FF00) >>> 8;
			buffer.crc32[3] = (crc_value & 0x000000FF);

		}

		// Now the i2cFrame should be ready
		
//		CSPwrite(conn, buffer); 
		connection.iface.write(buffer);
		
//		ImmortalEntry.i2c_a.write(conn, buffer);

//		System.out.println("Packet sent " + conn.destination);

		// Return only when the data has been transmitted. What to do if 
		// bus is busy? Hold back and attempt transmission later? Is it
		// OK to wait here or should we poll in a periodic fashion the device
		// driver?
//		I2CBusController ic = (I2CBusController) conn.tx_port;
//		
//		while ((ic.status.get() & I2CBusController.BUS_BUSY) == I2CBusController.BUS_BUSY){
//			;
//		}
//		ic.flushTXBuff();
//		conn.tx_port.flushTXBuff();
//		ImmortalEntry.log.addEvent("Packet sent"); 

		ImmortalEntry.bufferPool.freeCSPbuffer(buffer);
	}


	/**
	 * If a packet has been received in the specified interface, the packet is
	 * queued in the queue associated with the connection to which the packet
	 * belongs.
	 * 
	 * @param iface
	 *            The interface to check for a received packet.
	 */
	public static void receivePacket(IOInterface iface) {
		
		Buffer buffer = iface.read();

		if (buffer != null) {
			int source = (buffer.header[0] >>> 1) & 0x0000001F;
			int destination = ((buffer.header[0] & 0x00000001) << 4)
					| ((buffer.header[1] & 0x000000F0) >>> 4);
			int dest_port = ((buffer.header[1] & 0x0000000F) << 2)
					| ((buffer.header[2] & 0x000000C0) >>> 6);
			int src_port = ((buffer.header[2] & 0x0000003F));

			// ------ Debug -----
			int rec_source = destination;
			int rec_destination = source;
			int rec_dest_port = src_port;
			int rec_src_port = dest_port;

			// System.out.println(rec_source);
			// System.out.println(rec_destination);
			// System.out.println(rec_dest_port);
			// System.out.println(rec_src_port);
			//
			Connection con = ImmortalEntry.connectionPool.findConnection(
					rec_source, rec_destination, rec_src_port, rec_dest_port);
			// ------ End Debug -----

			// Connection con =
			// ImmortalEntry.connectionPool.findConnection(source, destination,
			// src_port, dest_port);
			if (!(con == null)) {
				con.queue.enq(buffer);
			}
		}
	}
	
//	public static void CSPwrite(Connection connection, Buffer buffer){
//
////		I2Cport port = connection.tx_port;
//		I2CBusController port = (I2CBusController) connection.tx_port;
//		
//		port.tx_fifo_data.put(connection.destination * 2);
//
//		for(int i = 0; i < buffer.size.length; i++){  //@WCA loop = Constants.CSP_PACKET_SIZE
//			port.tx_fifo_data.put(buffer.size[i]);
//		}
//
//		// What is the position of the CRC-32 field??
//		if (Constants.CSP_USE_CRC32) {
////			ImmortalEntry.log.addEvent("Writing CRC to buffer...");
//			for (int i = 0; i < buffer.crc32.length; i++) {
//				port.tx_fifo_data.put(buffer.crc32[i]);
//			}
//		}
//
////		ImmortalEntry.log.addEvent("Writing header to buffer...");
//		for(int i = 0; i < buffer.header.length; i++){  //@WCA loop = Constants.CSP_HEADER_SIZE
//			port.tx_fifo_data.put(buffer.header[i]);
//		}
//
//		if (buffer.data != null) {
////			ImmortalEntry.log.addEvent("Writing data to buffer...");
//			for (int i = 0; i < buffer.data.length; i++) {  //@WCA loop = Constants.MAX_PAYLOAD_SIZE
//				port.tx_fifo_data.put(buffer.data[i] >>> 24);
//				port.tx_fifo_data.put(buffer.data[i] >>> 16);
//				port.tx_fifo_data.put(buffer.data[i] >>> 8);
//				port.tx_fifo_data.put(buffer.data[i]);
//			}
//		}
//		
//		port.write(buffer.length);
//
//	}

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
