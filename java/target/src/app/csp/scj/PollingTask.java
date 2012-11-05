package csp.scj;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import com.jopdesign.io.I2Cport;

import util.Timer;

public class PollingTask extends PeriodicEventHandler{

	int dataCount;
	int[] data;
	
	public PollingTask(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize) {
		super(priority, parameters, scp, scopeSize);
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public void handleAsyncEvent() {
		
		Timer.wd();

		dataCount = (WatchDogSaflet.portB.status & 0x00078000) >> 15;
		data = new int[dataCount];
		
		if ((WatchDogSaflet.portB.status & I2Cport.DATA_VALID) != 0){
			for(int i=0; i < dataCount; i++){
				data[i] = WatchDogSaflet.portB.rx_fifo_data;
			}
		}
		
		
		Timer.wd();
		
	}

}
