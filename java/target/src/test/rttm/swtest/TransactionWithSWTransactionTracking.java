package rttm.swtest;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import com.jopdesign.sys.RollbackException;
import rttm.utils.Utils;

public abstract class TransactionWithSWTransactionTracking {
	
	// TMTODO use Native.wr() if more efficient

	public static boolean conflicting = false;
	
	// TMTODO we might also want to catch some additional Throwables not
	// derived from Exception
	/**
	 * @exception EarlyCommitError Thrown by user program or on transaction
	 * buffer overflow.
	 * @exception RollbackError Thrown by hardware if a conflict is detected 
	 * or by user program.
	 */
	protected static int atomicSection(int arg0) throws Exception {
		boolean ignored = conflicting;
		//for (int i = 0; i < 10; i++);
		return arg0;
	}
	
	public static int run(int arg0) throws RollbackException {
		int arg0Copy = 0xdeadbeef; // make compiler happy
		int result = 0xdeadbeef; // make compiler happy
		
		boolean outermostTransaction = !Utils.inTransaction[Utils.sysDev.cpuId];
		
		if (outermostTransaction) {
			Utils.inTransaction[Utils.sysDev.cpuId] = true;
			arg0Copy = arg0;
		}

		boolean transactionAborted;

		do {
			transactionAborted = false;
			
			if (outermostTransaction) {
				Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
			} else {
				System.out.println("Should not happen!");
			}

			try {
				result = atomicSection(arg0);

				if (outermostTransaction) {
					// flush write set					
					Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
				}
			} catch (Throwable e) { // RollbackError or any other exception
				// TMTODO this is redundant and unsafe, since tm might 
				// interrupt stack manipulation in f_athrow.
				// it only works if nothing has been written so far
				// signal end of transactional code to HW
				// TMTODO what if RollbackError is thrown here?
//				try {
//					Native.wrMem(Const.TM_ABORTED, Const.MEM_TM_MAGIC);
//				} catch (Throwable e2) {
//					// if RollbackError was thrown, re-execute abort
//					Native.wrMem(Const.TM_ABORTED, Const.MEM_TM_MAGIC);
//				}
				
				if (!outermostTransaction) {
					// don't rethrow an exception thrown during aborted 
					// transaction
					throw Utils.RollbackException;
				} else {
					Utils.sysDev.enableHwExceptions = 0;
					transactionAborted = true;
					
					System.out.println("Transaction aborted.");
				
					// rollback
					arg0 = arg0Copy;					
				}
			} finally {
				if (outermostTransaction) {
					Utils.inTransaction[Utils.sysDev.cpuId] = false;
					
					// TMTODO re-enable interrupts here?
					// Utils.sysDev.cntInt = 1;
				}
			}
		} while (transactionAborted);
		
		return result;
	}
}
