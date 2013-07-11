package test.scj;

import javax.safetycritical.JopSystem;

public class MyAppSCJ {
	
	public static void main(String args[]){
		
		MainSaflet SS = new MainSaflet();
		
		
		System.out.println("safelet created");
		
		JopSystem.startMission(SS);
		
	}

}
