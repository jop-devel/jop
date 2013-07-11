package i2c;

import com.jopdesign.io.*;

public class HelloI2C_de2_70 {
	
	// Our address when functioning as a slave. Limited to 7 bits (0 to 127)
	public static final int HOST_SLAVE_ADD = 100;
	
	// The device address to whom we wish to communicate
	public static final int TARGET_SLAVE_ADD = 32;
	//public static final int TARGET_SLAVE_ADD = 80;
	
	//public static final int COUNT = 1;
	
	// read or write request
	public static int READ = 1;
	public static int WRITE = 0;
	
	public static void main(String[] args) {
		
		
		System.out.println("Hello World");
		
		I2CFactory fact = I2CFactory.getFactory();
		I2Cport iic_p = fact.getI2Cport();
		
		iic_p.initConf(HOST_SLAVE_ADD);
		
		// Array to write to video codec
		int[] data = new int[1];
		
		// Array to store read data
		int[] readData = new int[16];
		
		// Base address to read from
		data[0] = 43;
		
		// Perform a dummy write to set the read address of device
		iic_p.write(TARGET_SLAVE_ADD, data);
		
		// Wait until TX fifo is empty
		// TODO: Use empty flag from fifo
		while(((iic_p.status & iic_p.TX_FIFO_OCCUPANCY_OUT) != 0) & (iic_p.status & iic_p.TR_PROGRESS) == 1){
			;
		}
		
		iic_p.tx_fifo_data = TARGET_SLAVE_ADD*2 + 1;
		
		// Signal a repeated start
		iic_p.control = 0x00000081;
		while((iic_p.status & iic_p.RESET_REP_START)== 0){
			;
		}
		// Reset the repeated start condition
		iic_p.control = 0x00000005 | (15 << 3);
		
		
		while ((iic_p.status & iic_p.BUS_BUSY) == 1){
			;
		}
		
		// Rx buffer should be full at this point, so know we 
		// write the data to the returning array
		for (int i=0; i< 16; i++){
			readData[i] = iic_p.rx_fifo_data;
		}

		// Now read from register at address 0x00 to 
		//readData = iic_p.read(TARGET_SLAVE_ADD, 4);
		
		//Print read data
		for(int i=0; i < readData.length; i++){
			System.out.println(readData[i] & 0x000000FF);
		}
	}
}
