package com.jopdesign.io;

public class I2Cport extends HardwareObject {
	
	// TX/RX buffer size in bytes
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
	public static final int RESET_REP_START = 0x00080000;

	
	public static final int CNT_BASE = 28;
		
	public static final int HOLD_START = 5*CNT_BASE - 1;
	public static final int T_LOW = 5*CNT_BASE - 1;
	public static final int T_HIGH = 5*CNT_BASE - 1;
	public static final int DELAY_STOP = 2*CNT_BASE - 1;
	public static final int T_SUSTO = 2*CNT_BASE - 1;
	public static final int T_WAIT = 9*CNT_BASE;
	
	
	public volatile int control;
	public volatile int status;
	
	// I2C's slave address
	public volatile int sladd;
	// I2C's data to send
	public volatile int tx_fifo_data;
	// I2C's data to receive
	public volatile int rx_fifo_data;
	
	public volatile int t_const_int;
	
	public volatile int message_size;
	
	
	// Multi-byte write
	public void write(int slAddress, int[] data){
		
		int j = BUFFER_SIZE - 1;
		
		// To write, the LSB of the address is set to zero
		// and the first position of buffer is used to store the
		// address of the slave we wish to communicate with.
		tx_fifo_data = slAddress*2;
		
		// Fill tx buffer
		if (data.length <= BUFFER_SIZE - 1){
			for (int i = 0; i < data.length; ++i){
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
			for (int i = 0; i < BUFFER_SIZE - 1; ++i){
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
	
	// Single byte write
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
	
	// Data read
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
	 
	final public void initConf(int slave_add){
		sladd = slave_add;
		int temp = (T_WAIT << 25) + (DELAY_STOP << 17) + (T_SUSTO << 9) + (T_HIGH);
		t_const_int =   temp;
		//System.out.println(temp);
	}

	
}
