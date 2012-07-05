package scopeuse.ex1;

import javax.safetycritical.JopSystem;

public class LoopAppSCJ {
	
	public static void main(String args[]){
		
		LoopSafelet SS = new LoopSafelet();
		SS.setup();
		
		System.out.println("Safelet created");
		
		JopSystem.startMission(SS);
		
	}

}
