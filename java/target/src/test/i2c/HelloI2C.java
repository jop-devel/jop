package i2c;

import com.jopdesign.io.*;

public class HelloI2C {
	
	// Our address when functioning as a slave. Limited to 7 bits (0 to 127)
	public static final int MASTER_ADD = 50;
	
	// The device address to whom we wish to communicate
	public static final int SLAVE_ADD = 55;
	
	public static final int COUNT = 1;
	
	// read or write request
	public static int READ = 1;
	public static int WRITE = 0;
	
	static I2Cport i2cMaster;
	static I2Cport i2cSlave;
	
	public static void main(String[] args) {
		
		HelloI2C app = new HelloI2C();
		
		System.out.println();
		System.out.println("Hello i2c!");
		System.out.println();
		
		I2CFactory fact = I2CFactory.getFactory();
		i2cMaster = fact.getI2CportA();
		i2cSlave = fact.getI2CportB();
		
		i2cMaster.initialize(MASTER_ADD, true);
		i2cSlave.initialize(SLAVE_ADD, false);
		
//		app.writeTest();
//		app.readTest();
		
//		int[] data = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
//		i2cMaster.writeBuffer(data);
		
//		app.simEEPROM();
		app.videoCodecTest();
		
	}
	
	public void writeTest(){
		
		int[] data = {1,2,3,4,5}; 
		i2cMaster.write(SLAVE_ADD, data);
		
	}
	

	public void readTest(){
		
		int[] data = new int[6];
		
		// Write something into the slave tx buffer
		for(int i=0; i<data.length;i++){
			i2cSlave.tx_fifo_data = i;
		}
		
		i2cMaster.read(SLAVE_ADD, data.length);
		
	}

	// NOTE: Works only in simulation with ModelSim, using VHDL model
	// of 24CXX EEPROM
	public void simEEPROM(){
		
		int EEPROM_ADDR = 80;
		int BYTE_ADDR = 16;
		int PAGE_SIZE = 16;
		
		
		// Data to write to eeprom
		int[] writeData = new int[PAGE_SIZE + 1];
		
		// Array to store read data
		int[] readData = new int[PAGE_SIZE];
		
		for (int i = 0; i < writeData.length; ++i){
			writeData[i] = 3*i+5;
		}
		
		// Set byte address
		writeData[0] = BYTE_ADDR;
		
		// Write the data
		i2cMaster.write(EEPROM_ADDR, writeData);
		
		// Poll busy bit to detect transmission is finished
		while((i2cMaster.status & I2Cport.BUS_BUSY) == I2Cport.BUS_BUSY){
			;
		}

		// Perform a dummy write to set the read address of eeprom
		i2cMaster.flushTXBuff();
		i2cMaster.write(EEPROM_ADDR, BYTE_ADDR);

		// Poll busy bit to detect transmission is finished
		while((i2cMaster.status & I2Cport.BUS_BUSY) == I2Cport.BUS_BUSY){
			;
		}

		// Now read the data
		i2cMaster.read(EEPROM_ADDR, PAGE_SIZE);

		// Poll the data ready flag
		while((i2cMaster.status & I2Cport.DATA_RDY) == 0){
			;
		}

		// Read data buffer
		i2cMaster.readBuffer(readData);

		//Print read data
//		for(int i=0; i < readData.length; i++){
//			System.out.println(readData[i] & 0x000000FF);
//		}
		
		
	}

	// NOTE: To use on Altera DE2-70 board
	public void videoCodecTest(){
		
		int ADV_ADDR = 32;
		int SUB_ADDR = 13;
		int READ_SIZE = 16;
		
		// Array to store read data
		int[] readData = new int[READ_SIZE];
		
		i2cMaster.writeRead(ADV_ADDR, SUB_ADDR, READ_SIZE);
		
		// Poll the data ready flag
		while((i2cMaster.status & I2Cport.DATA_RDY) == 0){
			;
		}

		int data_in_buffer = (i2cMaster.rx_occu & I2Cport.OCCU_RD) >>> 16;
		
		System.out.println("RX buffer occupancy: "+data_in_buffer+" bytes");
		System.out.println();
		
		// Read data buffer
		i2cMaster.readBuffer(readData);
		
		//Print read data
		for(int i=0; i < READ_SIZE; i++){
			System.out.println("["+(SUB_ADDR+i)+"]: " + (readData[i]));
		}
		
		System.out.println();
		i2cMaster.dumpRegisters();
		System.out.println();
		
		// **************** Perform another operation ************************
		
		SUB_ADDR = SUB_ADDR+17;
		
		i2cMaster.writeRead(ADV_ADDR, SUB_ADDR, READ_SIZE);
		
		// Poll the data ready flag
		while((i2cMaster.status & I2Cport.DATA_RDY) == 0){
			;
		}

		data_in_buffer = (i2cMaster.rx_occu & I2Cport.OCCU_RD) >>> 16;
		
		System.out.println("RX buffer occupancy: "+data_in_buffer+" bytes");
		System.out.println();
		
		// Read data buffer
		i2cMaster.readBuffer(readData);
		
		//Print read data
		for(int i=0; i < READ_SIZE; i++){
			System.out.println("["+(SUB_ADDR+i)+"]: " + (readData[i]));
		}
		
		System.out.println();
		i2cMaster.dumpRegisters();
		
	}
	
	// NOTE: To use on Xilinx ML-507 board
	public void xilEEPROM(){
		
		// Comming soon....
		
	}
	

}
