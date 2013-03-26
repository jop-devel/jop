package scjlibs;

import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import scjlibs.util.Enumeration;
import scjlibs.util.ObjectPool;
import scjlibs.util.PoolObject;
import scjlibs.util.Vector;

public class PEH_1 extends GenericPeriodicEventHandler {

//	Vector<GenericEntry> vector;
	ObjectPool<GenericEntry> pool;

	public PEH_1(String name, int priority, ObjectPool<GenericEntry> pool){//Vector<GenericEntry> vector) {
		super(name, priority);
//		this.vector = vector;
		this.pool = pool;
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public void handleAsyncEvent() {
		// TODO Auto-generated method stub

		ImmortalEntry.term.writeln(getName());

		System.out.println(pool.usedObjects());
		
		GenericEntry obj = pool.getPoolObject();
		
		StringBuffer f = new StringBuffer("Juan");
		obj.setName(f.toString());
		
		System.out.println(obj.getName());
		System.out.println(pool.usedObjects());
		
		pool.releasePoolObject(obj);
		
		System.out.println(pool.usedObjects());
		
		// Problem with lazy initialization: Get a set representation of the
		// entries, this is a field that uses lazy initialization. The HashMap
		// is in MissionMemory so all PEH handlers should be able to use it.
		// With the initialization below, any other PEH will fail when trying to
		// use it as the Set element is created in this handler allocation
		// context. Moreover, in the HashMap, this field is now different
		// from null preventing it from being re-initialized. Thus we are left
		// with a dangling pointer.
		//
		// hm.entrySet();
		//
		// ManagedMemory.executeInAreaOf(hm, new Runnable() {
		//
		// @Override
		// public void run() {
		// hm.entrySet();
		//
		// }
		// });

//		System.out.println("-----------------------------");
//		System.out.println(hm.get("Entry_0").getName());
//		System.out.println("-----------------------------");

//		Set<Entry<String, GenericEntry>> set = hm.entrySet();

		// Get an iterator
//		Iterator<Entry<String, GenericEntry>> i = set.iterator();

		// Display elements
//		while (i.hasNext()) {
//			Map.Entry<String, GenericEntry> me = (Map.Entry<String, GenericEntry>) i
//					.next();
//			System.out.print(me.getKey() + ": ");
//			System.out.println(me.getValue().getName());
//		}
		
//		ImmortalEntry.memStats();
		
//		ManagedMemory.executeInAreaOf(vector, new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				vector.add(new GenericEntry("Juan"));
//			}
//		});
//		
//		
//		Enumeration<GenericEntry> e = vector.elements();
//		System.out.println(e.nextElement().getName());

		Mission.getCurrentMission().requestTermination();

	}

}
