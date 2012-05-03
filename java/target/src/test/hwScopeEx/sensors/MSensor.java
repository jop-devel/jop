package hwScopeEx.sensors;

/* This class represent the sensor object. We add some intelligence to this 
 * object since it computes some intermediate results. 
 * 
 */

public class MSensor implements Runnable {
	
	Environment E;
	Meassurements Mess;
	Calibration Cal;
	int x;
	
	/*
	 * This is the "time step", that is, how spaced are in "time"
	 * each value of or sample vector
	 */
	final int step = 1;
	
	public MSensor(int ID, Environment env, Meassurements M, Calibration C){
		
		E = env;
		Mess = M;
		Cal = C;
	};
	
	public void run() {
		
		int[] frame = new int[Monitor.FRAME_LENGTH];
		frame = readSensor(E);
		compute(frame);
		
	};

	/*
	 * Here we perform some operations using the current scope as a
	 * scratchpad memory
	 */
	private void compute(int[] frame) {
		
		int r;
		int s;
		int t;
		int u;
		
		int sum = 0;
		int avg = 0;
		
		/*
		 * an array of 1 element just to force array references
		 */
		Calibration[] tempC = new Calibration[1];

		for (int i = 0; i < frame.length; i++){
			//sum = sum + frame[i];
			tempC[0] = Cal;
			sum = frame[i] * tempC[0].cal_param_1;
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
		
		Mess.slope =	(Monitor.FRAME_LENGTH*t - r*s)/(Monitor.FRAME_LENGTH*u - r*r);
		Mess.average = avg;
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
			frameTemp[i] = E.getMSensorData();
		}
		return frameTemp;
	}
	
}
