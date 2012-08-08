package test.level0;

import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.CyclicSchedule;

public class CustomCyclicSchedule {
	
	
	static CyclicSchedule generate(PeriodicEventHandler[] peh){
		
		CyclicSchedule.Frame frames[] = new CyclicSchedule.Frame[2];
		
		PeriodicEventHandler frame0_handlers[] = new PeriodicEventHandler[3]; 
		PeriodicEventHandler frame1_handlers[] = new PeriodicEventHandler[2];
		
		RelativeTime frame0_length = new RelativeTime(500,0);
		RelativeTime frame1_length = new RelativeTime(500,0);
		
		frame0_handlers[0] = peh[0];
		frame0_handlers[1] = peh[1];
		frame0_handlers[2] = peh[2];
		
		frame1_handlers[0] = peh[0];
		frame1_handlers[1] = peh[1];
		
		frames[0] = new CyclicSchedule.Frame(frame0_length, frame0_handlers);
		frames[1] = new CyclicSchedule.Frame(frame1_length, frame1_handlers);
		
		return new CyclicSchedule(frames);
		
	}

}
