package libcsp.csp.interfaces;

import libcsp.csp.ImmortalEntry;
import libcsp.csp.core.Node;
import libcsp.csp.core.PacketCore;

import com.jopdesign.io.I2Cport;

public abstract class InterfaceI2C implements IMACProtocol {
	
	private static final byte INT_SIZE_IN_BYTES = 4;
	private static final byte BYTE_SHIFT_COUNTER = INT_SIZE_IN_BYTES - 1;  
	private static final byte FRAME_SIZE_IN_BYTES = (INT_SIZE_IN_BYTES * 2);
	
	protected int frameByteIndex;
	protected I2Cport i2cPort;
	
	/*
	 * The hardware object for I2C and the corresponding microcontroller
	 * works by writing or reading single bytes at a time to the tx/rx register.
	 * Transmitting a packet therefore needs to divide the whole 32bit header
	 * and 32bit data into single bytes that are written to the tx register.
	 * The result is the transmit buffer containing the first 7 bits with
	 * the I2C address, the next 32 bits is the packet header 
	 * and the final 32 bits the data
	 */
	@Override
	public void transmitPacket(PacketCore packet) {
		
		int[] frame = new int[FRAME_SIZE_IN_BYTES];
		Node packetDSTNode = ImmortalEntry.routeTable[packet.getDST()];
		
		// This is not needed when using I2C. I2C's write method will insert the 
		// destination address
		// insertNextHopAddressIntoFrame(frame, packetDSTNode.nextHopMacAddress);
		sliceDataIntoBytesAndInsertIntoFrame(frame, packet.header);
		sliceDataIntoBytesAndInsertIntoFrame(frame, packet.data);
		
		i2cPort.write(packetDSTNode.nextHopMacAddress, frame);
		
		/* Set the I2C interface to slave mode to be able to receive messages */
		i2cPort.slaveMode();
		
		frameByteIndex = 0;
		ImmortalEntry.resourcePool.putPacket(packet);
		
	}

	/*
	 * The incoming I2C frame contains the whole packet, here we extract this
	 * from the data register and assemble the packet to be delivered
	 * 
	 * Context: ISR - invoked by the aperiodic event handler created during
	 * initialization for the I2C interface
	 */
	@Override
	public void receiveFrame() {
		
		int header = mergeNextDataBytesReceivedAndInsertIntoInteger();
		int data = mergeNextDataBytesReceivedAndInsertIntoInteger();
		
		PacketCore packet = ImmortalEntry.resourcePool.getPacket(ImmortalEntry.TIMEOUT_SINGLE_ATTEMPT);
		packet.header = header;
		packet.data = data;
		ImmortalEntry.packetsToBeProcessed.enqueue(packet);		
	}
	
	public int mergeNextDataBytesReceivedAndInsertIntoInteger() {
		int result = 0;
		
		for (byte b=0; b < 4; b++){
			result |= i2cPort.rx_fifo_data << position(b);
		}
		
		return result;
	}
	
	public void insertNextHopAddressIntoFrame(int[] frame, byte nextHopAddress) {
		frame[frameByteIndex] = nextHopAddress << 1;
		frameByteIndex++;
	}

	public void sliceDataIntoBytesAndInsertIntoFrame(int[] frame, int data) {
		int dataMask;
		for(byte b = 0 ; b < INT_SIZE_IN_BYTES; b++) {
			dataMask = 0xFF000000 >>>  b*8;
			frame[frameByteIndex] = (data & dataMask) >>> position(b);
			frameByteIndex++;
		}
	}
	
	public int position(int index) {
		return (BYTE_SHIFT_COUNTER - index)*8;
	}


}
