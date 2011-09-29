package hwScopeEx.sensors;

import java.util.Random;

import com.jopdesign.io.HWSensorA;
import com.jopdesign.io.HwScopeEnvironmentFactory;

public class Environment {
	
	int sensAval;
	
	HwScopeEnvironmentFactory hwScopeEnvFactory;
	HWSensorA sensA;
	
	Random random;
	
	public Environment() {
		init();
	}

	public void init(){

		if (Monitor.USE_HW_SENSORS){
			hwScopeEnvFactory = HwScopeEnvironmentFactory.getEnvironmentFactory();
			sensA = hwScopeEnvFactory.getSensA();	

		}else {
			random = new Random();
		}
	}

	public int getSensorData(){
		
		if (Monitor.USE_HW_SENSORS){
			sensAval = sensA.hwSensorA;

			return sensAval;
		}else{
			return random.nextInt(500);
		}
	}
}


	//	public static int[] temperature = {2, 4 , 6, 8, 10, 12, 14, 16, 18, 20};
//	private static int index = -1;
	
	 
//	public int getTemp(){
//		
//		if (index < temperature.length-1){
//			index++;
//			}
//		else{
//			index = 0;
//			}
		
//		return temperature[index];
//		
//	}


