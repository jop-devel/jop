package test.level1;

import javax.safetycritical.JopSystem;

public class MyLevel1App {
	
	public static void main(String args[]){
		
		Level1Safelet s = new Level1Safelet();
		
		System.out.println("Safelet created");
		
		JopSystem.startMission(s);
		
		System.out.println("Main method finished");
	}


}
