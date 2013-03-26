package scjlibs;

import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import scjlibs.util.HashMap;
import scjlibs.util.Iterator;
import scjlibs.util.Map;
import scjlibs.util.Set;
import scjlibs.util.Map.Entry;
import scjlibs.util.Vector;

public class PEH_2 extends GenericPeriodicEventHandler {

	Vector<GenericEntry> vector;

	public PEH_2(String name, int priority, Vector<GenericEntry> vector) {
		super(name, priority);
		this.vector = vector;
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public void handleAsyncEvent() {

		System.out.println(getName());

		// Get an iterator
		Iterator<GenericEntry> i = vector.iterator();

		// Display elements
		while (i.hasNext()) {
			GenericEntry ge = (GenericEntry) i.next();
			ImmortalEntry.term.writeln(ge.getName());
		}

	}

}
