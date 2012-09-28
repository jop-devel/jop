package csp;

import java.util.Vector;

public class Logger {
	
	
	public static Vector timeStamp = new Vector(); 
	public static Vector logEvent = new Vector();
	
	
	public static void addEvent(String message){
		
		timeStamp.addElement(System.currentTimeMillis());
		logEvent.addElement(message);
		
	}

}
