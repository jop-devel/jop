package i2c;

import java.io.IOException;

import oebb.Status;

import util.Timer;

import com.jopdesign.io.*;

public class HelloI2C {
	
	// Our address when functioning as a slave. Limited to 7 bits (0 to 127)
	public static final int SLAVE_ADD = 100;
	
	// The device address to whom we wish to communicate
	public static final int SLAVE = 80;
	
	public static final int COUNT = 1;
	
	// read or write request
	public static int READ = 1;
	public static int WRITE = 0;
	
	public static void main(String[] args) {
		
		
		System.out.println("Hello World");
		
		I2CFactory fact = I2CFactory.getFactory();
		I2Cport iic_p = fact.getI2Cport();
		
		iic_p.initConf(SLAVE_ADD);
		
		// Data to write to eeprom
//		int[] data = new int[17];
		
		// Array to store read data
		int[] readData = new int[16];
		
//		for (int i = 0; i < data.length; ++i ){
//			data[i] = 3*i+5;
//		}
//		
//		data[0] = 0;
		
		// Write the data
//		iic_p.write(SLAVE, data);
		
		// Perform a dummy write to set the read address of eeprom
		iic_p.write(SLAVE, 32);

		// Now read the data
		readData = iic_p.read(SLAVE, 4);
		
		//Print read data
		for(int i=0; i < readData.length; i++){
			System.out.println(readData[i] & 0x000000FF);
		}
	}
}
