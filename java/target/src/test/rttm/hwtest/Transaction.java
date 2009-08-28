package rttm.hwtest;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

import joprt.RtThread;

public class Transaction implements Runnable {
	
	static final int TM_MAGIC = 0x0FFFFF;
	enum cmd {
			end_transaction,
			start_transaction,
			aborted,
			early_commit }
	
	static volatile boolean flag = false;
	
	int id;
	
	public Transaction(int i) {
		id = i;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		Runnable r = new Transaction(0);
		Startup.setRunnable(r, 0);
		
		sys.signal = 1;
		
		System.out.println("Waiting for flag.");
		
		while (true)
		{
			RtThread.busyWait(100000);
			System.out.println(flag);
		}
		
	}

	public void run() {
		Native.wrMem(1, TM_MAGIC);
		
		flag = true;
		
		Native.wrMem(0, TM_MAGIC);
		
		RtThread.busyWait(1000000);
		flag = false;
		RtThread.busyWait(1000000);
		
		Native.wrMem(1, TM_MAGIC);
		
		flag = true;
		
		Native.wrMem(0, TM_MAGIC);
		
		Native.wrMem(1, TM_MAGIC);
		
		flag = false;
		
		while (true);
	}

}
