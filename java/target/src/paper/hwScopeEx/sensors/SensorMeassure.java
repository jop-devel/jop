package hwScopeEx.sensors;

import com.jopdesign.sys.Memory;

/*
 * We have this class to be able to create the Sensor objects
 * inside its runnable method which in turn is the method executed
 * when we use enterPrivateMemory(). In this way all the Sensor
 * objects (and the objects created by them) are collected after
 * enterPrivateMemory() returns  
 */
public class SensorMeassure implements Runnable {
	
	Environment env;
	Meassurements[] Mes;
	Calibration[] Cal;
	int idx = 0;
	MSensor[] mSensor;
	
	SensorMeassure(Environment E, Meassurements[] M, Calibration[] C){		
		
		env = E;
		Mes = M;
		Cal = C;

		init();
		
	};
	
	public void init(){
		
		mSensor = new MSensor[Monitor.NO_OF_SENSORS];

		for(int i=0; i<Monitor.NO_OF_SENSORS;i++){
			mSensor[i] = new MSensor(i, env, Mes[i], Cal[i]);
		}
	}
	
	public void setIdx(int i){
		
		idx = i;
		
	}
	
	@Override
	public void run() {
		
//		init();

//		MSensor[] mSensor = new MSensor[Monitor.NO_OF_SENSORS];
//		
//		for(int i=0; i<Monitor.NO_OF_SENSORS;i++){
//			mSensor[i] = new MSensor(i, env, Mes[i], Cal[i]);
////			mSensor[i] = new MSensor(i, env,i);
//		}
		
//		Memory meme = Memory.getCurrentMemory();
		
		
		//for(int i=0; i<Monitor.NO_OF_SENSORS;i++){
			//meme.enterPrivateMemory(1024,mSensor[i]);
			mSensor[idx].run();
		//}
		
	}
}
