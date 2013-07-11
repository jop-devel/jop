package scjlibs;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Safelet;

public class Launcher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Safelet<GenericMission> safelet = new GenericSafelet();
		JopSystem.startMission(safelet);
		

	}

}
