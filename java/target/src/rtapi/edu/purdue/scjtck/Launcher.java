package edu.purdue.scjtck;

import javax.safetycritical.JopSystem;

import edu.purdue.scjtck.tck.TestSchedule402;



public final class Launcher {


	public static void main(final String[] args) {
		
		TestSchedule402 s = new TestSchedule402();
		s.initializeApplication();
		System.out.println("Safelet created");
		
		JopSystem.startMission(s);
		
		System.out.println("Main method finished");
	}

}