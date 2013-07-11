package com.jopdesign.io;

import javax.realtime.RawInt;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import csp.Buffer;
import csp.Constants;

public class I2Cport extends HardwareObject implements RawInt{
	
	// TX/RX buffer size in bytes. This size is the size of the FIFO's in
	// the hardware controller. To modify it you should modify the VHDL files.
	public static final int BUFFER_SIZE = 32;
	
	// Status constants
	public static final int TBUF_ERR  = 0x00000040;
	public static final int RBUF_ERR  = 0x00000020;
	public static final int ACK_ERR   = 0x00000010;
	public static final int HDR_ERR   = 0x00000008;
	public static final int BUS_BUSY  = 0x00000004;
	public static final int STOP_STAT = 0x00000002;
	public static final int DATA_RDY  = 0x00000001;
	
	public static final int OCCU_RD  = 0xFFFF0000;
	public static final int OCCU_WR  = 0x0000FFFF;

	// Control constants
	public static final int TX_FLUSH  = 0x00000080;
	public static final int RX_FLUSH  = 0x00000040;
	public static final int ACK       = 0x00000020;
	public static final int STRT      = 0x00000010;
	public static final int STOP      = 0x00000008;
	public static final int MASL      = 0x00000004;
	public static final int RSTA      = 0x00000002;
	public static final int ENABLE    = 0x00000001;
	
	public static final int CLEAR_STRT = 0xFFFFFFEF;

	// Configuration constants
	public static final int MASTER = ENABLE | MASL;
	public static final int SLAVE = ENABLE;


	public static final int NOT_FLUSH = 0xFFFFFF7F;

	// Count base for SCL timings, adjust according to 
	// uP clock frequency
	public static final int CNT_BASE = 28;

	// These constants define the timings of the SCL signal
	public static final int T_HOLD_START = 5 * CNT_BASE - 1;
	public static final int T_RSTART = 5 * CNT_BASE - 1;
	public static final int T_LOW = 5 * CNT_BASE - 1;
	public static final int T_HIGH = 5 * CNT_BASE - 1;

	public static final int T_HALF_HIGH = 2 * CNT_BASE - 1;
	public static final int T_SUSTO = 4 * CNT_BASE - 1;
	public static final int T_WAIT = 8 * CNT_BASE;
	
	// Control register
	public volatile int control;

	// Status register
	public volatile int status;
	
	// Host slave address
	public volatile int devadd;
	
	// Size of the message (in bytes) to be transmitted
	public volatile int msg_size;
	
	// Data to send
	public volatile int tx_fifo_data;
	
	// Data to receive
	public volatile int rx_fifo_data;
	
	// Timing high
	public volatile int th;

	// Timing low
	public volatile int tl;
	
	// Tx buffer occupancy
	public volatile int tx_occu;

	// Rx buffer occupancy
	public volatile int rx_occu;
	
	/**
	 * Load the initial configuration to the I2C device.
	 * 
	 * @param devAdd
	 * @param isMaster
	 */
	final public void initialize(int devAdd, boolean isMaster){
		devadd = devAdd;
		
		if(isMaster){
			control = MASTER;
		}else{
			control = SLAVE;
		}
		
		int temp = (T_HOLD_START << 24) + (T_RSTART << 16) + (T_LOW << 8) + (T_HIGH);
		th =   temp;
		
		temp = (T_HALF_HIGH << 24) + (T_SUSTO << 16) + (T_WAIT << 8) + (0);
		tl =   temp;

	}
	
	/**
	 * Set device in slave mode without changing the device address
	 */
	public void slaveMode(){
		control = SLAVE;
	}
	
	/**
	 * Display the contents of the control, status and address registers
	 */
	final public void dumpRegisters(){
		
		System.out.println("[control]: "+control);
		System.out.println("[status]: "+status);
		System.out.println("[address]: "+devadd);
		
	}
	
	/**
	 * Clear the transmit buffer
	 */
	public void flushTXBuff(){

		int controlOld = control;
		control = control | TX_FLUSH;
		control = controlOld;

	}

	/**
	 * Clear the receive buffer
	 */
	public void flushRXBuff(){

		int controlOld = control;
		control = control | RX_FLUSH;
		control = controlOld;

	}

	/**
	 * Write the elements of the data array into the transmit buffer
	 * 
	 * @param data
	 */
	public void writeBuffer(int[] data) {

		// Check that there is still space available in buffer
		int occu = (tx_occu & OCCU_WR);
		int space = BUFFER_SIZE - occu ;
		
		if((data.length <= space) & (data.length < BUFFER_SIZE)){
			for (int i=0; i< data.length; i++){
				tx_fifo_data = data[i];
			}
		}else{
			System.out.println("Not enough space in buffer");
		}

	}
	
	/**
	 * Read ALL the bytes in the transmit buffer. Data is stored in the data
	 * array. If buffer is empty, the array is filled with zeros.
	 * 
	 * @param data
	 */
	public void readBuffer(int[] data) {

		// Read all data in buffer
		int occu = (rx_occu & OCCU_RD ) >>> 16;
		
		for (int i=0; i < occu; i++){
			data[i] = rx_fifo_data;
		}
	}
	
	/**
	 * Write "size" bytes to the slave identified with by the address in the first
	 * byte of the transmit buffer.This method is particularly useful if you have
	 * previously written data to the transmit buffer. It assumes that the first seven
	 * bits of the byte to whom the transmit buffer points is the slave address. The LSB
	 * of this same byte must be zero. This is a non-blocking operation. Once the 
	 * transmission is started the hardware will take care of finishing it and
	 * clearing the BUS_BUSY flag in the status register.
	 * 
	 * @param size
	 *            How many bytes will be written to the slave. 
	 */
	public void write(int size){
		
		// Clear STRT bit in case there was a previous transaction
		control = control & CLEAR_STRT;
		
		if((status & BUS_BUSY) == 0){
			// Set I2C to master
			control = MASTER;
			
			if(size > 1){
				msg_size = size + 1;
			}else{
				msg_size = 1;
			}

			// Initiate transmission, set STRT bit = 1
			control = control | STRT;

		}else{
			System.out.println("Can't start transmission, bus busy");
		}
		
	}

	/**
	 * Write one byte to the slave identified with slAddress address. This is 
	 * a non-blocking operation. Once the transmission is started the hardware 
	 * will take care of finishing it and clearing the BUS_BUSY flag in the 
	 * status register.
	 * 
	 * @param slAddress
	 *            Address of the slave target.
	 * @param data
	 *            Byte to be written.
	 * 
	 */
	public void write(int slAddress, int data){
		
		// Clear STRT bit in case there was a previous transaction
		control = control & CLEAR_STRT;
		
		if((status & BUS_BUSY) == 0){
			// To write, the LSB of the address is set to zero
			// and the first position of buffer is used to store the
			// address of the slave we wish to communicate with.
			tx_fifo_data = slAddress*2;
			
			// Write data to tx buffer
			tx_fifo_data = data;
			}
				
			// Set I2C to master
			control = MASTER;
			
			msg_size = 1;

			// Initiate transmission, set STRT bit = 1
			control = control | STRT;

	}

	/**
	 * Write "N" bytes to the slave identified with slAddress address. "N" is
	 * the size of the "data" array. This is a non-blocking operation. Once the
	 * transmission is started the hardware will take care of finishing it and
	 * clearing the BUS_BUSY flag in the status register.
	 * 
	 * @param slAddress
	 *            Address of the slave target.
	 * @param data
	 *            Array of data to be written.
	 * 
	 */
	public void write(int slAddress, int[] data){
		
		// Clear STRT bit in case there was a previous transaction
		control = control & CLEAR_STRT;
		
		if((status & BUS_BUSY) == 0){
			// To write, the LSB of the address is set to zero
			// and the first position of buffer is used to store the
			// address of the slave we wish to communicate with.
			tx_fifo_data = slAddress*2;
			
			// Write data to tx buffer
			if(data.length > BUFFER_SIZE-1){
				System.out.println("Data bigger than buffer size");
			}else{
				for(int i = 0; i<data.length; i++){
					tx_fifo_data = data[i];
				}
			}
				
			// Set I2C to master
			control = MASTER;
			
			msg_size = data.length + 1;

			// Initiate transmission, set STRT bit = 1
			control = control | STRT;

		}else{
			System.out.println("Can't start transmission, bus busy");
		}
		
	}

	/**
	 * This is a very common function in I2C devices, where the master writes
	 * one byte of data to a slave, usually to set a base address to read from,
	 * and then it performs a read operation. This is a non-blocking operation.
	 * The method will return after the read operation is initiated. The
	 * hardware will take care of finishing, clearing the BUS_BUSY flag, and
	 * setting the DATA_RDY flag in the status register.
	 * 
	 * @param slAddress
	 *            Address of the slave target.
	 * @param data
	 *            Base address of slave. 
	 *            
	 * @param readSize Size in bytes of the
	 *            read transaction.
	 * 
	 */
	public void writeRead(int slAddress, int data, int readSize){
		
		// Clear STRT bit in case there was a previous transaction
		control = control & CLEAR_STRT;
		
		if((status & BUS_BUSY) == 0){
			// To write, the LSB of the address is set to zero
			// and the first position of buffer is used to store the
			// address of the slave we wish to communicate with.
			tx_fifo_data = slAddress*2;
			tx_fifo_data = data;
			tx_fifo_data = slAddress*2 + 1;

			// Set I2C to master and initiate transmission
			control = MASTER | STRT;
			
			// It is safe to set the repeated start bit here since we wish
			// to transmit only one byte. Setting the repeated start bit 
			// takes effect in the next WAIT_ACK or SEND_ACK state 
			control = control | RSTA;
			
			// Message size is reduced by one since byte 0 counts as first
			// received byte
			msg_size = readSize - 1;
			
			// Now we need to wait until the controller leaves the first 
			// ACK_HEADER state to set the master rx mode

		}else{
			System.out.println("Can't start transmission, bus busy");
		}
		
	}

	/**
	 * Read "N" bytes from the slave identified with slAddress address. "N" is
	 * specified before starting the read operation. This is a non-blocking
	 * operation, once it is started, the hardware will take care of finishing
	 * it, clearing the BUS_BUSY flag and setting the DATA_RDY flag in the
	 * status register. Data in the receive buffer has to be moved explicitly to
	 * e.g. an array for its later use.
	 * 
	 * @param slAddress
	 *            Target slave address.
	 * @param readSize
	 *            Size in bytes of the read transaction.
	 */
	public void read(int slAddress, int readSize){
		
		// Clear STRT bit in case there was a previous transaction
		control = control & CLEAR_STRT;
		
		// A read operation starts by transmitting the slave address.
		// Add 1 to indicate a read transaction.
		tx_fifo_data = slAddress*2 + 1;
		
		control = MASTER;
		
		msg_size = readSize - 1;
		
		control = control | STRT;
		
	}
	
	int getRegister(int address){
		
		switch (address) {
		case 0:
			return control;
		case 1:	
			return status;
		default:
			return 0;
		}
		
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public int get() {
		
		return 0;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public void put(int value) {
		// TODO Auto-generated method stub
		
	}
	
	
//	// Multi-byte write
//	//public void write(int slAddress, int[] data){
//	public void write(int[] data){
//		
//		int j = BUFFER_SIZE - 1;
//		
//		// To write, the LSB of the address is set to zero
//		// and the first position of buffer is used to store the
//		// address of the slave we wish to communicate with.
//		//tx_fifo_data = slAddress*2;
//		tx_fifo_data = data[0];
//		
//		// Fill tx buffer
//		if (data.length <= BUFFER_SIZE - 1){
//			for (int i = 1; i < data.length; ++i){
//				tx_fifo_data = data[i];
//			};
//			// Set I2C to master transmitter and set STRT bit
//			control = 0x00000003;
//			while ((status & BUS_BUSY) == 0){
//				;
//			}
//			// Reset STRT bit to avoid starting a new transfer after
//			// finishing the current one.
//			control = 0x00000001;
//
//		}else{
//			for (int i = 1; i < BUFFER_SIZE ; ++i){
//				tx_fifo_data = data[i];
//			};
//			control = 0x00000003;
//			while ((status & BUS_BUSY) == 0){
//				;
//			}
//			control = 0x00000001;
//
//			while( j < data.length ){
//				while((status & TX_FIFO_FULL) == 0){
//					tx_fifo_data = data[j];
//					j = j + 1;
//					}
//			}
//			
//		}
//	}
//	
//	public void CSPwrite(int slave, CSPbuffer buffer){
//
//		tx_fifo_data = slave;
//
////		for(int i = 0; i < buffer.length.length; i++){
////			tx_fifo_data = buffer.length[i];
////		}
//
//		if (Conf.CSP_USE_CRC32) {
//			debug_msg("Writing CRC to buffer...");
//			for (int i = 0; i < buffer.crc32.length; i++) {
//				tx_fifo_data = buffer.crc32[i];
//			}
//		}
//
//		System.out.println("Writing header to buffer...");
//		for(int i = 0; i < buffer.header.length; i++){
//			tx_fifo_data = buffer.header[i];
//		}
//
//		if (buffer.data != null) {
//			debug_msg("Writing data to buffer...");
//			for (int i = 0; i < buffer.data.length; i++) {
//				tx_fifo_data = buffer.data[i];
//			}
//		}
//
//		// Set I2C to master transmitter and set STRT bit
//		control = 0x00000003;
//
//		while ((status & BUS_BUSY) == 0){
//				;
//			}
//
//		// Reset STRT bit to avoid starting a new transfer after
//		// finishing the current one.
//		control = 0x00000001;
//
//		//TODO: Check transmission was successful to free buffer.
//
//	}
//
//
//	private void debug_msg(String string) {
//		// TODO Auto-generated method stub
//		System.out.println(string);
//	}
//
//	
//	public void slaveMode(){
//		control = control | FLUSH_FIFO;
//		control = control & NOT_FLUSH_FIFO;
//		control = SLAVE;
//	}
//
//	public void masterTX(){
//		control = control | FLUSH_FIFO;
//		control = control & NOT_FLUSH_FIFO;
//		control = MASTER_TX;
//	}
//
//	public void masterRX(){
//		control = MASTER_RX;
//	}
//
//
//	public void CSPreadBuffer(CSPbuffer buffer) {
//
////		for (int i=0; i < buffer.length.length; i++){
////			buffer.length[i] = rx_fifo_data;
////		}
//
//		// If CRC is enabled, it uses the first 4 bytes
//		// of the received packet
//		if (Conf.CSP_USE_CRC32) {
//			for (int i=0; i < buffer.crc32.length; i++){
//				buffer.crc32[i] = rx_fifo_data;
//			}
//		}
//
//		// Header is always 4 bytes
//		for (int i=0; i < buffer.header.length; i++){
//			buffer.header[i] = rx_fifo_data;
//		}
//
////		for (int i=0; i < buffer.data.length; i++){
//
//		// The remaining in the buffer is the payload
//////		int data = RX_FIFO_OCCUPANCY_IN;
////		if (data != 0){
////			for (int i=0; i < data; i++){
////				buffer.data[i] = rx_fifo_data;
////			}
////		}
//
//		//return data;
//
//	}
}
