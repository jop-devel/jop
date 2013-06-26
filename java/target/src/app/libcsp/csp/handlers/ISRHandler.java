package libcsp.csp.handlers;

import javax.safetycritical.ManagedInterruptServiceRoutine;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import libcsp.csp.interfaces.IMACProtocol;

/**
 * Interrupt service routine used to read packets from the specified protocol
 * interface.
 * 
 * @author Mikkel Todberg, Jeppe Lund Andersen, modified by Juan Rios
 * 
 */
public class ISRHandler extends ManagedInterruptServiceRoutine {

	private IMACProtocol protocolInterface;

	/**
	 * Creates an interrupt service routine
	 * 
	 * @param storage
	 *            Storage Parameters
	 * @param scopeSize
	 *            Scoped memory sized used by this handler
	 * @param protocolInterface
	 *            MAC protocol interface
	 */
	public ISRHandler(StorageParameters storage, long scopeSize,
			IMACProtocol protocolInterface) {
		super(storage, scopeSize);

		this.protocolInterface = protocolInterface;

	}

	@Override
	@SCJAllowed(Level.LEVEL_1)
	protected void handle() {
		protocolInterface.receiveFrame();
	}

}