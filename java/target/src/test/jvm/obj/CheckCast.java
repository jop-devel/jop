/*
 * Created on 30.07.2005
 *
 */
package jvm.obj;

import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class CheckCast extends TestCase implements Runnable {
	
	public String getName() {
		return "CheckCast";
	}
	
	
	public boolean test() {
		
		boolean ok = true;
		
		Object o = new CheckCast();
		CheckCast c;
		c = (CheckCast) o;
		
		// Issue: JOP does not check interfaces on checkcast!
		Runnable r = (Runnable) o;
		
		return ok;
	}


	public void run() {
		// just dummy to use an interface
	}

}
