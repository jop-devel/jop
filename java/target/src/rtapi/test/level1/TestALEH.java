package test.level1;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.AperiodicLongEventHandler;
import javax.safetycritical.StorageParameters;

public class TestALEH extends AperiodicLongEventHandler{

	public TestALEH(PriorityParameters priority, AperiodicParameters release_info,
			StorageParameters scp, String s) {
		super(priority, release_info, scp, s);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleAsyncEvent(long data) {

		System.out.println(data);
		
	}

}
