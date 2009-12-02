package rttm.hwtest;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.RetryException;
import com.jopdesign.sys.Startup;
import joprt.RtThread;

import static com.jopdesign.sys.Const.*;

public class Conflict implements Runnable {
	
	static volatile boolean notCommitted = false;
	static volatile boolean conflicting;
	
	int id;
	
	public Conflict(int i) {
		id = i;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		Runnable r = new Conflict(0);
		Startup.setRunnable(r, 0);
		
		sys.signal = 1;
		
		try {
			Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
			
			notCommitted = true;
			boolean ignored = conflicting;						
			
			while (true);
		} catch (RetryException e){
			System.out.println("Rollback exception");			
		}
		
		System.out.print("notCommitted set to ");
		System.out.println(notCommitted);
	}

	public void run() {
		
		// make sure it will be a conflict
		RtThread.busyWait(100);
		
		Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
		
		conflicting = true;
		
		Native.wrMem(TM_END_TRANSACTION, MEM_TM_MAGIC);
	}

}
