package ravenscar;
//import javax.realtime.*;
import joprt.*;

public class Initializer // extends javax.realtime.RealtimeThread
{
  public Initializer()
  {
  	/*
    super( new javax.realtime.PriorityParameters( 
       ((javax.realtime.PriorityScheduler)javax.realtime.Scheduler.getDefaultScheduler()).
         getMaxPriority()), null, null, javax.realtime.ImmortalMemory.instance(), null, null);
    */
    
  }
 
 	public void run() {
 	}
 	
 	public void start() {
 		run();
 		RtThread.startMission();
 		// perhaps do the WD thing
 		for (;;)
 			;
 	}
}
