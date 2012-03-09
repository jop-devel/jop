package test.scj;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class Temperature extends PeriodicEventHandler {
	
	public Temperature(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long privSize) {
		super(priority, parameters, scp, privSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleAsyncEvent() {
		// TODO Auto-generated method stub
		
		// Add here the code to read the temperature sensor
		System.out.println("Temp");
	}
	
}
