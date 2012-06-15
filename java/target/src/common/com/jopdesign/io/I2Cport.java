package com.jopdesign.io;

import csp.CSPbuffer;
import csp.Conf;

public class I2Cport extends HardwareObject {
	
	// TX/RX buffer size in bytes. This size is the size of the FIFO's in
	// the hardware controller. To modify it you should modify the vhdl files.
	public static final int BUFFER_SIZE = 16;
	
	// Some constants for the registers
	public static final int BUS_BUSY = 0x00000001;
	public static final int TX_FIFO_FULL = 0x00000002;
	public static final int RX_FIFO_EMPTY = 0x00000004;
	public static final int TX_FIFO_OCCUPANCY_IN = 0x00000078;
	public static final int TX_FIFO_OCCUPANCY_OUT = 0x00000780;
	public static final int RX_FIFO_OCCUPANCY_IN = 0x00007800;
	public static final int RX_FIFO_OCCUPANCY_OUT = 0x00078000;
	public static final int TR_PROGRESS  = 0x00100000;
	public static final int TRA_PROGRESS = 0x00200000;
	public static final int DATA_VALID   = 0x02000000;
	public static final int RESET_REP_START = 0x00080000;
	
	public static final int FLUSH_FIFO = 0x00000100;
	public static final int NOT_FLUSH_FIFO = 0xFFFFFEFF;

	public static final int TX_FIFO_EMPTY = 0x00400000;
	public static final int RX_FIFO_FULL  = 0x00800000;

	// Configuration constants
	public static final int MASTER_TX = 0x00000001;
	public static final int MASTER_RX = 0x00000005;
	public static final int SLAVE = 0x00000000;

	// Count base for SCL timings.
	public static final int CNT_BASE = 28;

	// These constants define the timings of the SCL signal
	public static final int HOLD_START = 5*CNT_BASE - 1;
	public static final int T_LOW = 5*CNT_BASE - 1;
	public static final int T_HIGH = 5*CNT_BASE - 1;
	public static final int DELAY_STOP = 2*CNT_BASE - 1;
	public static final int T_SUSTO = 2*CNT_BASE - 1;
	public static final int T_WAIT = 9*CNT_BASE;
	
	// Control register
	public volatile int control;

	// Status register
	public volatile int status;
	
	// Host slave address
	public volatile int sladd;

	// Data to send
	public volatile int tx_fifo_data;

	// Data to receive
	public volatile int rx_fifo_data;
	
	// Register with the SCL timing information
	public volatile int t_const_int;
	
	// Size of the message (in bytes) to be transmitted
	public volatile int message_size;

	// Initialization
	final public void initConf(int slave_add){
		sladd = slave_add;
		int temp = (T_WAIT << 25) + (DELAY_STOP << 17) + (T_SUSTO << 9) + (T_HIGH);
		t_const_int =   temp;
	}
	
	public void flushFifo(){

		control = control | FLUSH_FIFO;
		control = control & NOT_FLUSH_FIFO;

	}
	
	// Multi-byte write
	//public void write(int slAddress, int[] data){
	public void write(int[] data){
		
		int j = BUFFER_SIZE - 1;
		
		// To write, the LSB of the address is set to zero
		// and the first position of buffer is used to store the
		// address of the slave we wish to communicate with.
		//tx_fifo_data = slAddress*2;
		tx_fifo_data = data[0];
		
		// Fill tx buffer
		if (data.length <= BUFFER_SIZE - 1){
			for (int i = 1; i < data.length; ++i){
				tx_fifo_data = data[i];
			};
			// Set I2C to master transmitter and set STRT bit
			control = 0x00000003;
			while ((status & BUS_BUSY) == 0){
				;
			}
			// Reset STRT bit to avoid starting a new transfer after
			// finishing the current one.
			control = 0x00000001;

		}else{
			for (int i = 1; i < BUFFER_SIZE ; ++i){
				tx_fifo_data = data[i];
			};
			control = 0x00000003;
			while ((status & BUS_BUSY) == 0){
				;
			}
			control = 0x00000001;

			while( j < data.length ){
				while((status & TX_FIFO_FULL) == 0){
					tx_fifo_data = data[j];
					j = j + 1;
					}
			}
			
		}
	}
	
	public void CSPwrite(int slave, CSPbuffer buffer){

		tx_fifo_data = slave;

//		for(int i = 0; i < buffer.length.length; i++){
//			tx_fifo_data = buffer.length[i];
//		}

		if (Conf.CSP_USE_CRC32) {
			debug_msg("Writing CRC to buffer...");
			for (int i = 0; i < buffer.crc32.length; i++) {
				tx_fifo_data = buffer.crc32[i];
			}
		}

		System.out.println("Writing header to buffer...");
		for(int i = 0; i < buffer.header.length; i++){
			tx_fifo_data = buffer.header[i];
		}

		if (buffer.data != null) {
			debug_msg("Writing data to buffer...");
			for (int i = 0; i < buffer.data.length; i++) {
				tx_fifo_data = buffer.data[i];
			}
		}

		// Set I2C to master transmitter and set STRT bit
		control = 0x00000003;

		while ((status & BUS_BUSY) == 0){
				;
			}

		// Reset STRT bit to avoid starting a new transfer after
		// finishing the current one.
		control = 0x00000001;

		//TODO: Check transmission was successful to free buffer.

	}


	private void debug_msg(String string) {
		// TODO Auto-generated method stub
		System.out.println(string);
	}

	/**
	 * Single byte write
	 *
	 * @param slAddress: Address of the slave target.
	 * @param data: Data to be written.
	 *
	 */
	public void write(int slAddress, int data){
		
		// To write, the LSB of the address is set to zero
		// and the first position of buffer is used to store the
		// address of the slave we wish to communicate with.
		// To write, the LSB of the address is set to zero
		tx_fifo_data = slAddress*2;
		
		// Write byte to tx buffer
		tx_fifo_data = data;
	
		// Set I2C to master transmitter and set STRT bit
		control = 0x00000003;

		while ((status & BUS_BUSY) == 0){
			;
		}
		control = 0x00000001;
		
		while ((status & BUS_BUSY) == 1){
			;
		}
	}
	
	/**
	 * Data read
	 *
	 * @param slAddress: Target slave address.
	 * @param readSize: Size in bytes of the read transaction.
	 * @return An array with the read data.
	 */
	public int[] read(int slAddress, int readSize){
		
		int[] data = new int[readSize];
		
		// A read operation starts by transmitting the slave address.
		// Add 1 to indicate a read transaction.
		tx_fifo_data = slAddress*2 + 1;
		
		control = 0x00000007 | (readSize << 3);
		
		while ((status & BUS_BUSY) == 0){
			;
		}
		control = 0x00000005 | (readSize << 3);
		while ((status & BUS_BUSY) == 1){
			;
		}
		
		// Rx buffer should be full at this point, so know we 
		// write the data to the returning array
		for (int i=0; i< readSize; i++){
			data[i] = rx_fifo_data;
		}
		
		return data;
		
	}

	public void slaveMode(){
		control = control | FLUSH_FIFO;
		control = control & NOT_FLUSH_FIFO;
		control = SLAVE;
	}

	public void masterTX(){
		control = control | FLUSH_FIFO;
		control = control & NOT_FLUSH_FIFO;
		control = MASTER_TX;
	}

	public void masterRX(){
		control = MASTER_RX;
	}


	public void readBuffer(int[] data) {

		//int[] data = new int[BUFFER_SIZE];

		for (int i=0; i< BUFFER_SIZE-1; i++){
			data[i] = rx_fifo_data;
		}

		//return data;

	}

	
	public void CSPreadBuffer(CSPbuffer buffer) {

//		for (int i=0; i < buffer.length.length; i++){
//			buffer.length[i] = rx_fifo_data;
//		}

		// If CRC is enabled, it uses the first 4 bytes
		// of the received packet
		if (Conf.CSP_USE_CRC32) {
			for (int i=0; i < buffer.crc32.length; i++){
				buffer.crc32[i] = rx_fifo_data;
			}
		}

		// Header is always 4 bytes
		for (int i=0; i < buffer.header.length; i++){
			buffer.header[i] = rx_fifo_data;
		}

//		for (int i=0; i < buffer.data.length; i++){

		// The remaining in the buffer is the payload
		int data = RX_FIFO_OCCUPANCY_IN;
		if (data != 0){
			for (int i=0; i < data; i++){
				buffer.data[i] = rx_fifo_data;
			}
		}

		//return data;

	}
}
