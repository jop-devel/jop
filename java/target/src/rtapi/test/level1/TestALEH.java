package test.level1;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.AperiodicLongEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.Terminal;

public class TestALEH extends AperiodicLongEventHandler{

	public TestALEH(PriorityParameters priority, AperiodicParameters release,
			StorageParameters storage, long scopeSize, String name) {
		super(priority, release, storage, scopeSize, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleAsyncEvent(long data) {

		Terminal.getTerminal().writeln(""+data);
//		System.out.println(data);
		
	}

}
