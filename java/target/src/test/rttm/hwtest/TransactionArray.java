package rttm.hwtest;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

import joprt.RtThread;

public class TransactionArray implements Runnable {
	
	static final int TM_MAGIC = 0x0FFFFF;
	enum cmd {
			end_transaction,
			start_transaction,
			aborted,
			early_commit }
	
	static volatile int[] vals = { 0, 0, 0, 0 };
	
	int id;
	
	public TransactionArray(int i) {
		id = i;		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		Runnable r = new TransactionArray(0);
		Startup.setRunnable(r, 0);
		
		sys.signal = 1;
		
		System.out.println("Waiting for flag.");
		
		while (true)
		{
		
				System.out.print(vals[0]);
				System.out.print(vals[1]);
				System.out.print(vals[2]);
				System.out.println(vals[3]);
		}
		
	}

	public void run() {
		Native.wrMem(1, TM_MAGIC);
		
		vals[0] = 1;
		vals[1] = 2;
		
		//Native.wrMem(0, TM_MAGIC);
		
		while (true);
	}

}
