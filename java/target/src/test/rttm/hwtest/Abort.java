package rttm.hwtest;

import static com.jopdesign.sys.Const.*;

import com.jopdesign.sys.Native;

public class Abort {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
			
			Native.wrMem(TM_ABORT, MEM_TM_MAGIC);
		} catch (Throwable t) {
			Native.wrMem(TM_ABORTED, MEM_TM_MAGIC);
			System.out.println("Aborted.");
		}
	}

}
