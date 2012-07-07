package scopeuse.ex1;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

import com.jopdesign.sys.GC;
import com.jopdesign.sys.Memory;
import com.jopdesign.sys.Native;

public class LoopHandler extends PeriodicEventHandler{
	
	Mission mission;

	public LoopHandler(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize) {
		
		super(priority, parameters, scp, scopeSize);
		
	}

	@Override
	public void handleAsyncEvent() {
		System.out.println("***************** Handler *****************");
		System.out.println("");
		
		Worker w = new Worker();
		
		for(int i = 0; i< Data.N_BLOCKS; i++){
			ManagedMemory.enterPrivateMemory(256, w);
		}
		
		// Just to see that Data was modified
		System.out.println(Data.data[5]);
	}
}
