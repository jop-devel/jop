package test.scj;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class Voltage extends PeriodicEventHandler{

	public Voltage(PriorityParameters priority, PeriodicParameters parameters,
			StorageParameters scp, long privSize) {
		super(priority, parameters, scp, privSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleAsyncEvent() {
		// TODO Auto-generated method stub
		
		// Add here code to read voltage
		System.out.println("Volts");
		
	}
	
	

}
