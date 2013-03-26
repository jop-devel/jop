package scjlibs;

import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

public class PropagateExceptionMission extends GenericMission {

	@Override
	@SCJAllowed(Level.SUPPORT)
	protected void initialize() {
		// TODO Auto-generated method stub

		PropagationExceptionHandler peh = new PropagationExceptionHandler(
				"Propagate Exception Handler", 10);
		peh.register();
	}

}
