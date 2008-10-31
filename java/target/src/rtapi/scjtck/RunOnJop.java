/*
  This file is part of the SCJ TCK

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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Terminal.getTerminal().writeln("TCK on JOP");
		JopSystem.startMission(new TestTermination());
	}

}
