package csp.scj;

import javax.safetycritical.JopSystem;

public class WatchDogAppSCJ {

	public static void main(String args[]){

		WatchDogSaflet SS = new WatchDogSaflet();
		SS.setup();

		System.out.println("Safelet created");

		JopSystem.startMission(SS);

	}

}
