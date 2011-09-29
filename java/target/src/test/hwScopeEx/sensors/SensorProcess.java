package hwScopeEx.sensors;

/*
 * We have this class to be able to create the Sensor objects
 * inside its runnable method which in turn is the method executed
 * when we use enterPrivateMemory(). In this way all the Sensor
 * objects (and the objects created by them) are collected after
 * enterPrivateMemory() returns  
 */
public class SensorProcess implements Runnable {
	
	Environment env;;
	
	SensorProcess(Environment E){
		
		env = E;
		
	};
	
	@Override
	public void run() {

		Sensor sensorA = new Sensor(Monitor.Asensor, env);
		Sensor sensorB = new Sensor(Monitor.Bsensor, env);
		
		sensorA.execute();
		sensorB.execute();

		
	}


}
