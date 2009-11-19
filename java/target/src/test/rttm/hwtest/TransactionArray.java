package rttm.hwtest;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

import static com.jopdesign.sys.Const.*;

public class TransactionArray implements Runnable {
	
	enum cmd {
			end_transaction,
			start_transaction,
			aborted,
			early_commit }
	
	static volatile int[] vals = new int[10];
	
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
		
		while (true)
		{
			for (int i = 0; i < vals.length; i++)
			{
				System.out.print(vals[i]);
			}
			
			System.out.println();
		}
	}

	public void run() {
		Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
		
		for (int i = 0; i < vals.length; i++)
		{
			vals[i] = i;
		}
		
		Native.wrMem(TM_END_TRANSACTION, MEM_TM_MAGIC);
		
		Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
		
		for (int i = 0; i < vals.length; i++)
		{
			vals[i] = 0;
		}
		
		while (true);
	}

}
