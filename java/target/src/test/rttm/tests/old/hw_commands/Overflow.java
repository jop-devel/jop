package rttm.tests.old.hw_commands;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;
import joprt.RtThread;

import static com.jopdesign.sys.Const.*;

public class Overflow implements Runnable {
	
	static final int scale = 10000;
	
	int id;
	
	static volatile int[] vals = new int[100];
	
	public Overflow(int i) {
		id = i;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		Runnable r = new Overflow(0);
		Startup.setRunnable(r, 0);
		
		sys.signal = 1;
		
		RtThread.busyWait(10 * scale);
		
		for (int i = 0; i < vals.length; i++) {
			System.out.println(vals[i]);
		}
		
	}

	public void run() {
		Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
		
		for (int i = 0; i < vals.length; i++) {
			vals[i] = i;
		}
		
		Native.wrMem(TM_END_TRANSACTION, MEM_TM_MAGIC);
		
		while (true);
	}

}
