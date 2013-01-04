package csp.scj.watchdog;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Mission;
import javax.safetycritical.Safelet;

public class WatchDogAppSCJ {

	public static void main(String args[]){

		Safelet<Mission> safelet = new WatchDogSaflet();

		System.out.println("Safelet created");

		JopSystem.startMission(safelet);

	}

}
