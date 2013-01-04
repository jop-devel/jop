package csp.scj.watchdog;

import csp.Buffer;
import csp.Constants;
import csp.IODevice;
import csp.IOInterface;
import csp.ImmortalEntry;

public class I2CInterface implements IOInterface {

	I2CBusController port = null;

	public I2CInterface(I2CBusController port) {
		this.port = port;
	}

	@Override
	public IODevice getIODevice() {
		return port;
	}

	@Override
	public synchronized void write(Buffer buffer) {
		

		int destination = ((buffer.header[0] & 0x00000001) << 4)
				| ((buffer.header[1] & 0x000000F0) >>> 4);

		port.tx_fifo_data.put(destination * 2);
		
		for (int i = 0; i < buffer.size.length; i++) { // @WCA loop = Constants.CSP_PACKET_SIZE
			port.tx_fifo_data.put(buffer.size[i]);
		}

		// What is the position of the CRC-32 field??
		if (Constants.CSP_USE_CRC32) {
			// ImmortalEntry.log.addEvent("Writing CRC to buffer...");
			for (int i = 0; i < buffer.crc32.length; i++) {
				port.tx_fifo_data.put(buffer.crc32[i]);
			}
		}

		// ImmortalEntry.log.addEvent("Writing header to buffer...");
		for (int i = 0; i < buffer.header.length; i++) { // @WCA loop = Constants.CSP_HEADER_SIZE
			port.tx_fifo_data.put(buffer.header[i]);
		}

		if (buffer.data != null) {
			// ImmortalEntry.log.addEvent("Writing data to buffer...");
			for (int i = 0; i < buffer.data.length; i++) { // @WCA loop = Constants.MAX_PAYLOAD_SIZE
				port.tx_fifo_data.put(buffer.data[i] >>> 24);
				port.tx_fifo_data.put(buffer.data[i] >>> 16);
				port.tx_fifo_data.put(buffer.data[i] >>> 8);
				port.tx_fifo_data.put(buffer.data[i]);
			}
		}

		port.write(buffer.length);
		
		while ((port.status.get() & I2CBusController.BUS_BUSY) == I2CBusController.BUS_BUSY) {
			;
		}
		
		port.flushTXBuff();

	}

	@Override
	public synchronized Buffer read() {

		if ((port.status.get() & I2CBusController.DATA_RDY) == I2CBusController.DATA_RDY) {

			int dataCount = (port.rx_occu.get() & I2CBusController.OCCU_RD) >>> 16;
			Buffer buff = ImmortalEntry.bufferPool.getCSPbuffer();

			for (int i = 0; i < buff.size.length; i++) { // @WCA loop = 2
				buff.size[i] = port.rx_fifo_data.get();
			}

			int dataSize = dataCount - Constants.HEADER_SIZE;

			// What is the position of the CRC-32 field??
			if (Constants.CSP_USE_CRC32) {
				dataSize = dataSize - 4;
				for (int i = 0; i < buff.crc32.length; i++) { // @WCA loop = 4
					buff.crc32[i] = port.rx_fifo_data.get();
				}
			}

			for (int i = 0; i < buff.header.length; i++) { // @WCA loop = 4
				buff.header[i] = port.rx_fifo_data.get();
			}

			for (int i = 0; i < (Math.ceil(dataSize / 4)); i++) { // @WCA loop = 7
				int a, b, c, d;
				a = port.rx_fifo_data.get() << 24;
				b = port.rx_fifo_data.get() << 16;
				c = port.rx_fifo_data.get() << 8;
				d = port.rx_fifo_data.get();

				buff.data[i] = a | b | c | d;
			}
			return buff;
		} else {
			return null;
		}

	}

}
