package rttm.hwtest;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;
import joprt.RtThread;

import static com.jopdesign.sys.Const.*;

public class Transaction implements Runnable {
	
	static volatile boolean flag = false;
	static final int scale = 10000;
	
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
		
		while (true)
		{
			RtThread.busyWait(10 * scale);
			System.out.println(flag);
		}
		
	}

	public void run() {
		Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
		
		flag = true;
		
		Native.wrMem(TM_END_TRANSACTION, MEM_TM_MAGIC);
		
		RtThread.busyWait(100 * scale);
		flag = false;
		RtThread.busyWait(100 * scale);
		
		Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
		
		flag = true;
		
		Native.wrMem(TM_END_TRANSACTION, MEM_TM_MAGIC);
		
		Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
		
		flag = false;
		
		while (true);
	}

}
