/*
  This file is part of the TCK for JSR 302: Safety Critical JavaTM Technology
  	see <http://jcp.org/en/jsr/detail?id=302>

  Copyright (C) 2008, The Open Group
  Author: Martin Schoeberl (martin@jopdesign.com)

  License TBD.
*/


/**
 * 
 */
package scjtck;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Terminal;

/**
 * @author Martin Schoeberl
 *
 */
public class RunOnJop {
	
//	TestCase tc[] = {
//			new TestTermination(),
//	};
	
//	static TestCase tc = new TestTermination();
	static TestCase tc = new TestPeriodicParameters();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Terminal.getTerminal().writeln("SCJ TCK on JOP");
		JopSystem.startMission(tc);
	}

}
