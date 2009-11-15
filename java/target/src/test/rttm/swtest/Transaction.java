/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Peter Hilber (peter@hilber.name)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package rttm.swtest;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import com.jopdesign.sys.RollbackException;

import rttm.internal.Utils;

public class Transaction {
	
	protected static final boolean LOG = true; 
	
	public static boolean conflicting = false;
	
	/**
	 * @exception RollbackException Thrown by hardware if a conflict is detected 
	 * or by user program.
	 */
	protected static int atomicSection(int arg0) throws Exception, 
		RollbackException {
		boolean ignored = conflicting;
		return arg0;
	}
	
	public static int run(int arg0) throws RollbackException {
		int arg0Copy = 0xdeadbeef; // make compiler happy
		
		int result = 0xdeadbeef; // make compiler happy
			
		boolean outermostTransaction = !Utils.inTransaction[Native.rd(Const.IO_CPU_ID)];
		
		try {
			
			if (outermostTransaction) {
				// TMTODO disable interrupts?
				Native.wr(0, Const.IO_INT_ENA);
				
				Utils.inTransaction[Native.rd(Const.IO_CPU_ID)] = true;
				arg0Copy = arg0;
			}
	
			boolean retryTransaction;
	
			do {
				retryTransaction = false;
				
				if (outermostTransaction) {
					Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
				} else {
					if (LOG) {
						rttm.internal.Utils.logEnterInnerTransaction();
					}
				}
	
				try {
					result = atomicSection(arg0);
	
					if (outermostTransaction) {
						// flush write set					
						Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
					}
					
					// TMTODO we could move finally part there
				} catch (Throwable e) { // RollbackError or any other exception
					if (outermostTransaction) {
						Native.wr(0, Const.IO_ENA_HW_EXC);
						
						if (e == Utils.abortException) {
							throw Utils.abortException;
						}
						
						retryTransaction = true;
						
						if (LOG) {
							rttm.internal.Utils.logAbort();
						}
					
						// rollback
						arg0 = arg0Copy;					
					} else {
						// don't refer to exception thrown during aborted
						// transaction
						
						if (e == Utils.abortException) {
							throw Utils.abortException;
						} else {
							throw Utils.rollbackException;
						}
					}
				}
			} while (retryTransaction);
		} finally {
			if (outermostTransaction) {
				Utils.inTransaction[Native.rd(Const.IO_CPU_ID)] = false;

				// TMTODO (unconditionally) re-enable interrupts here?
				Native.wr(1, Const.IO_INT_ENA);
			}
		}
		
		return result;
	}
}
