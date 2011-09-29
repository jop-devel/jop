package hwScopeEx.sensors;

/* This class represent the sensor object. We add some intelligence to this 
 * object since it computes 
 * 
 */

public class Sensor {
	
	int id;
	Environment E;
	
	final int step = 1;
	
	/*
	 * This is the "time step", that is, how spaced are in "time"
	 * each value of or sample vector
	 */
	
	
	public Sensor(int ID, Environment env){
		
		id = ID;
		E = env;
//		results = new Meassurements();
	};
	
	public void execute() {
		
		int[] frame = new int[Monitor.FRAME_LENGTH];

		// TODO:
		// Update previous measurement
		// Maybe also take state decision in here, in that way we are
		// accessing a memory located in an upper scope.
		
		frame = readSensor(E);
		compute(frame);
//		Monitor.meassurements[id] = results;
//		int j = Monitor.meassurements[id].average;
//		System.out.println(j);
		
	};
	

	/*
	 * Here we perform some operations using the current scope as a
	 * scratchpad memory
	 */
	private void compute(int[] frame) {
		
		double r;
		double s;
		double t;
		double u;
		
		int sum = 0;
		int avg = 0;

		for (int i = 0; i < frame.length; i++){
			//sum = sum + frame[i];
			sum = frame[i];
		}
		
		//avg = sum / Monitor.FRAME_LENGTH;
		avg = sum;
		
		r = s = t = u = 0;
		for (int i=0; i < Monitor.FRAME_LENGTH; i++){
			int temp = (i + 1)*step;
			r = r + temp;
			u = u + temp*temp;
			
			s = s + frame[i];
			t = t + temp*frame[i];
			
		}
		
		Meassurements m = new Meassurements();
		m.slope =	(Monitor.FRAME_LENGTH*t - r*s)/(Monitor.FRAME_LENGTH*u - r*r);
		m.average = avg;

		/*
		 * Now we save the results of the computations in the array located in an upper scope
		 */
		Monitor.meassurements[id].average = avg;
		Monitor.meassurements[id].slope = (Monitor.FRAME_LENGTH*t - r*s)/(Monitor.FRAME_LENGTH*u - r*r);;
		
	}

	/* Data from the sensor is collected when the sensor is read. We can simulate the sensor in
	 * two ways:
	 * 		
	 * 		1. As a SW code in a Java class   
	 * 		2. Using a HW object and coding the functionality of the sensor in VHDL 
	 */
	public int[] readSensor(Environment E) {
		
		int[] frameTemp = new int[Monitor.FRAME_LENGTH];

		for (int i = 0; i < frameTemp.length; i++){
			frameTemp[i] = E.getSensorData();
		}
		return frameTemp;
	}
	
}
