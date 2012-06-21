package hwScopeEx.sensors;

import java.util.Random;

import com.jopdesign.io.HWSensorM;
import com.jopdesign.io.HWSensorC;
import com.jopdesign.io.HwScopeEnvironmentFactory;

public class Environment {
	
	int sensMval;
	int sensCval;
	
	HwScopeEnvironmentFactory hwScopeEnvFactory;
	HWSensorM sensM;
	HWSensorC sensC;
	
	Random random;
	
	public Environment() {
		init();
	}

	public void init(){

		if (Monitor.USE_HW_SENSORS){
			hwScopeEnvFactory = HwScopeEnvironmentFactory.getEnvironmentFactory();
			sensM = hwScopeEnvFactory.getSensM();
			sensC = hwScopeEnvFactory.getSensC();

		}else {
			random = new Random();
		}
	}

	public int getMSensorData(){
		
		if (Monitor.USE_HW_SENSORS){
			sensMval = sensM.hwSensorM;

			return sensMval;
		}else{
			return random.nextInt(500);
		}
	}
	
	public int getCSensorData(){
		
		if (Monitor.USE_HW_SENSORS){
			sensCval = sensC.hwSensorC;

			return sensCval;
		}else{
			return random.nextInt(500);
		}
	}
}