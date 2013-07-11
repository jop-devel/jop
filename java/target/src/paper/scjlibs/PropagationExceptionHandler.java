package scjlibs;

import javax.safetycritical.ManagedMemory;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import com.jopdesign.sys.Memory;


public class PropagationExceptionHandler extends GenericPeriodicEventHandler{

	public PropagationExceptionHandler(String name, int priority) {
		super(name, priority);
		// TODO Auto-generated constructor stub
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public void handleAsyncEvent() {
		// TODO Auto-generated method stub
		System.out.println(getName());
		
		System.out.println("--------------");
		System.out.println("Size: "+Memory.getCurrentMemory().size());
		System.out.println("Bs remaining: "+Memory.getCurrentMemory().bStoreRemaining());
		System.out.println("--------------");
		
		int[] nums = new int[50];

		Thrower thrower = new Thrower();
		thrower.setNums(nums);
		
		try {
			ManagedMemory.enterPrivateMemory(1024, thrower);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		System.out.println("back");
	}

}
