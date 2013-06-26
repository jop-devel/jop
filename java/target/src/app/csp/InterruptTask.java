package csp;

import javax.safetycritical.ManagedInterruptServiceRoutine;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

public class InterruptTask extends ManagedInterruptServiceRoutine{

	public InterruptTask() {
		
		super(new StorageParameters(512, null), 256);
	}

	@Override
	@SCJAllowed(Level.LEVEL_1)
	protected void handle() {

		Services.receivePacket(ImmortalEntry.i2c_b);
		
	}

}
