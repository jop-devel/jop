package rttm.tests.manual;

import static com.jopdesign.sys.Const.MEM_TM_MAGIC;
import static com.jopdesign.sys.Const.TM_END_TRANSACTION;
import static com.jopdesign.sys.Const.TM_START_TRANSACTION;
import rttm.AbortException;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.RetryException;
import com.jopdesign.sys.Startup;

public class RunReferenceTransaction implements Runnable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		Runnable r = new RunReferenceTransaction();
		Startup.setRunnable(r, 0);
		
		sys.signal = 1;
		
		while (true) {
			int result = -1;
			try {
				result = Transaction.atomicMethod(1);
			} catch (RetryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AbortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(result);
		}
	}
	
	public void run() {
		while (true) {
			Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
			
			Transaction.conflicting = true;
			for (int i = 0; i < 10; i++);
			
			Native.wrMem(TM_END_TRANSACTION, MEM_TM_MAGIC);
		}
	}


}
