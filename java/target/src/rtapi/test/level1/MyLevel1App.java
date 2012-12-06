package test.level1;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Safelet;

public class MyLevel1App {
	
	public static void main(String args[]){
		
		Safelet s = new Level1Safelet();
		
		System.out.println("Safelet created");
		
		JopSystem.startMission(s);
		
		System.out.println("Main method finished");
	}


}
