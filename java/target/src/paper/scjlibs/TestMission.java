package scjlibs;

import javax.safetycritical.ManagedMemory;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import com.jopdesign.sys.Memory;

import scjlibs.util.HashMap;
import scjlibs.util.ObjectPool;
import scjlibs.util.Set;
import scjlibs.util.Map.Entry;
import scjlibs.util.Vector;

public class TestMission extends GenericMission {

	Set<Entry<String, GenericEntry>> set;
	private Vector<GenericEntry> vector;

	@Override
	@SCJAllowed(Level.SUPPORT)
	protected void initialize() {
		
		// Create a vector structure in mission memory
		vector = new Vector<GenericEntry>();
		
		// Put elements to the vector
		for (int i = 0; i < 5; i++) {
//			ImmortalEntry.log.addEvent("Start put");
			vector.add(new GenericEntry("Entry_" + i));
//			ImmortalEntry.log.addEvent("End put");
//			ImmortalEntry.log.addEvent(Memory.getCurrentMemory().memoryConsumed());
		}
		
		
		GenericEntryFactory factory = new GenericEntryFactory();
		ObjectPool<GenericEntry> pool = new ObjectPool<GenericEntry>(factory);
		System.out.println("good");
		
//		ImmortalEntry.log.addEvent(Memory.getCurrentMemory().memoryConsumed());
//		GenericEntry ge = new GenericEntry("");
//		GenericEntry ge2 = new GenericEntry("");
//		ImmortalEntry.log.addEvent(Memory.getCurrentMemory().memoryConsumed());
		
		
		PEH_1 peh_1 = new PEH_1("PEH_1", Constants.MIN_PRIO + 1, pool); //vector); 
		peh_1.register();

		PEH_2 peh_2 = new PEH_2("PEH_2", Constants.MIN_PRIO, vector);
		peh_2.register();
		
		
		
		ImmortalEntry.memStats();

	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	protected void cleanUp() {
		
		super.cleanUp();
		
		if(Constants.ENABLE_LOG){
			dumpLog();
		}
		
	}

	private void dumpLog() {

		// ImmortalEntry.dumpLog.selector = 0;
		// for (int i = 0; i < ImmortalEntry.eventsLogged; i++) {
		// ImmortalEntry.dumpLog.logEntry = i;
		// ManagedMemory.enterPrivateMemory(1500, ImmortalEntry.dumpLog);
		// }

		ImmortalEntry.dumpLog.selector = 0;
		
		for(int i = 0; i < ImmortalEntry.log.totalLoggedEvents; i++){
			ImmortalEntry.dumpLog.logEntry = i;
			ManagedMemory.enterPrivateMemory(1500, ImmortalEntry.dumpLog);
		}
		
//		ImmortalEntry.dumpLog.selector = 1;
//		
//		for(int i = 0; i < ImmortalEntry.log.totalLoggedEvents-1; i=i+2){
//			ImmortalEntry.dumpLog.logEntry = i;
//			ManagedMemory.enterPrivateMemory(1500, ImmortalEntry.dumpLog);
//		}


	}

}
