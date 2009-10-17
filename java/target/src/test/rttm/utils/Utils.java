package rttm.utils;

import com.jopdesign.sys.RollbackException;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

public class Utils {

	/**
	 * TMTODO
	 */
	public static RollbackException RollbackException = new RollbackException();
	
	public static SysDevice sysDev = IOFactory.getFactory().getSysDevice();
	
	// TMTODO either hijack a register or use cpu id for array
	public static boolean[] inTransaction = new boolean[Utils.sysDev.nrCpu];
}
