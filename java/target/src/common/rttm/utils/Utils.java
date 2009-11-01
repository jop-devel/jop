package rttm.utils;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.RollbackException;

public class Utils {

	/**
	 * TMTODO
	 */
	public static RollbackException RollbackException = new RollbackException();
	
	// TMTODO either hijack a register or use cpu id for array
	public static boolean[] inTransaction = new boolean[Native.rd(Const.IO_CPUCNT)];
	
	public static void logAbort() {
		System.out.println("Transaction aborted.");
	}
	
	public static void logEnterInnerTransaction() {
		System.out.println("Entered inner transaction.");
	}
}
