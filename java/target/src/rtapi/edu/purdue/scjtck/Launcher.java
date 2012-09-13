package edu.purdue.scjtck;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Safelet;

import edu.purdue.scjtck.tck.TestSchedule402;



public final class Launcher {


	public static void main(final String[] args) {
		
		Safelet s = new TestSchedule402();
		System.out.println("Safelet created");
		
		JopSystem.startMission(s);
		
		System.out.println("Main method finished");
	}

}