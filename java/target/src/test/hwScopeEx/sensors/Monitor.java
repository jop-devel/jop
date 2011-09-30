package hwScopeEx.sensors;

import joprt.RtThread;

import com.jopdesign.sys.Memory;

public class Monitor {
	
	/*
	 * Use the simulated VHDL versions of the HW sensors, otherwhise
	 * random numbers are generated by the SW.
	 * 
	 * Status:
	 * - HW sensor A: Possible to read only one constant value (Used for testing
	 * 				  the implementation of the HW objects)
	 * - HW sensor B: Nothing 
	 */
	public static final boolean USE_HW_SENSORS = true;
	
	public static final int THRESHOLD = 400;
//	private static final int PERIOD = 1;

	static final int M_SIZE = 4096;
	static final int S_SIZE = 512;
	
	public static String state;
	public static long start;
	
	public static int tempValue;
	
	public static String[] calculationResults;

// new constants
	
	static final int TIME_BASE = 10000;
	static final int FRAME_LENGTH = 10;
	
	//Priorities for the real time threads
	static final int TS_PRIO = 14;
//	static final int PS_PRIO = 13;
//	static final int EX_PRIO = 12;
//	static final int CV_PRIO = 11;
	
	//Periods for the real time threads
	static final int TS_PERIOD = 1;
//	static final int PS_PERIOD = 1;
//	static final int EX_PERIOD = 1;
//	static final int CV_PERIOD = 1;
	
	// Sensor ID's
	static final int Asensor = 0;
	static final int Bsensor = 1;
//	static final int E_ID = 2;
//	static final int C_ID = 3;
	
	/* An array holding two objects. each object has the results of the computations
	 * of each sensor. This array will be stored in an upper scope so we can test the
	 * use of the aastore bytecode
	 */
	// static fields located in immortal memory (level = 0)
	public static Meassurements[] meassurements = new Meassurements[2];
	
	public Environment E = new Environment();
	
	public void run() {
		
		meassurements[Asensor] = new Meassurements();
		meassurements[Bsensor] = new Meassurements();
		
		final SensorProcess sensorProc = new SensorProcess(E);
		
		new RtThread(TS_PRIO, TS_PERIOD*TIME_BASE) {
			
			public void run() {
				for(;;){
					/* Nested scoped memory. It will be used to hold temporary objects
					 * such as the sensor objects and to perform temporary computations
					 * When the enterPrivate() method returns, we have the necessary
					 * information to take a decision (i.e. start some actuator)
					 */ 
					Memory m = Memory.getCurrentMemory();
					m.enterPrivateMemory(512, sensorProc);
					
					/*
					 * Put here logic to be performed after reading of sensors is complete
					 */
					print(meassurements[Asensor].average);

					waitForNextPeriod();
					
				}
			}
		};
		
		RtThread.startMission();
	}
	
	public static void main (String args[]){
		
		//Environment E = new Environment();
		Monitor myMonitor = new Monitor();
		
		myMonitor.run();
	}
	
	/*
	 * Just a helper method to avoid writing System.out... every time 
	 * we want to print something...
	 */
	public static void print(int s){
		System.out.println(s);
	}
}