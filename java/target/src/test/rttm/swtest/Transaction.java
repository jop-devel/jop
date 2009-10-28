package rttm.swtest;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import com.jopdesign.sys.RollbackException;
import rttm.utils.Utils;

public abstract class Transaction {
	
	protected static final boolean LOG = true; 
	
	public static boolean conflicting = false;
	
	/**
	 * @exception RollbackException Thrown by hardware if a conflict is detected 
	 * or by user program.
	 */
	protected static int atomicSection(int arg0) throws Exception, 
		RollbackException {
		boolean ignored = conflicting;
		//for (int i = 0; i < 10; i++);
		return arg0;
	}
	
	public static int run(int arg0) throws RollbackException {
		int arg0Copy = 0xdeadbeef; // make compiler happy
		int result = 0xdeadbeef; // make compiler happy
		
		// TMTODO disable interrupts?
		
		boolean outermostTransaction = !Utils.inTransaction[Native.rd(Const.IO_CPU_ID)];
		
		if (outermostTransaction) {
			Utils.inTransaction[Native.rd(Const.IO_CPU_ID)] = true;
			arg0Copy = arg0;
		}

		boolean transactionAborted;

		do {
			transactionAborted = false;
			
			if (outermostTransaction) {
				Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
			} else {
				if (LOG) {
					System.out.println("Entered inner transaction.");
				}
			}

			try {
				result = atomicSection(arg0);

				if (outermostTransaction) {
					// flush write set					
					Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
				}
			} catch (Throwable e) { // RollbackError or any other exception
				if (!outermostTransaction) {
					// don't rethrow an exception thrown during aborted 
					// transaction
					throw Utils.RollbackException;
				} else {
					Native.wr(0, Const.IO_ENA_HW_EXC);
					transactionAborted = true;
					
					if (LOG) {
						System.out.println("Transaction aborted.");
					}
				
					// rollback
					arg0 = arg0Copy;					
				}
			} finally {
				if (outermostTransaction) {
					Utils.inTransaction[Native.rd(Const.IO_CPU_ID)] = false;
					
					// TMTODO re-enable interrupts here?
					// Utils.sysDev.cntInt = 1;
				}
			}
		} while (transactionAborted);
		
		return result;
	}
}
