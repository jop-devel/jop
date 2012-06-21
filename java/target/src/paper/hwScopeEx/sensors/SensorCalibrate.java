package hwScopeEx.sensors;

import com.jopdesign.sys.Memory;

/*
 * We have this class to be able to create the Sensor objects
 * inside its runnable method which in turn is the method executed
 * when we use enterPrivateMemory(). In this way all the Sensor
 * objects (and the objects created by them) are collected after
 * enterPrivateMemory() returns  
 */
public class SensorCalibrate implements Runnable {
	
	Environment env;
	Calibration[] Cal;	
	
	SensorCalibrate(Environment E, Calibration[] CC){
		env = E;
		Cal = CC;
		
	};
	
	@Override
	public void run() {
		
		//double t1 = System.currentTimeMillis();
		
		/*
		 * Array of calibration data (i.e. calibration objects)
		 */
//		Calibration[] calArray = new Calibration[Monitor.NO_OF_SENSORS];
//		for(int i=0; i<Monitor.NO_OF_SENSORS;i++){
//			calArray[i] = new Calibration();
//		}
		
		/*
		 * Array of calibration sensors
		 */
		CSensor[] cSensor = new CSensor[Monitor.NO_OF_SENSORS];

		for(int i=0; i<Monitor.NO_OF_SENSORS;i++){
			cSensor[i] = new CSensor(i, env);
		}
		
		
		
		/*
		 * Array references in the same scope
		 */
		for(int i=0; i<Monitor.NO_OF_SENSORS;i++){
			Cal[i].cal_param_1 = cSensor[i].calibrate().cal_param_1;
		}
		
		
//		double t2 = System.currentTimeMillis();
//		double time = t2 - t1;
//		System.out.println("Calibration time: "+ time);
	
	}
}
