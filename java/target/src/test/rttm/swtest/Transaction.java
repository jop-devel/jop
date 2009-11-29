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

import rttm.AbortException;
import rttm.Operations;
import rttm.internal.Utils;

/**
 * This class serves both as a conceptual reference of an atomic method and
 * as the base for the generation of an atomic method in 
 * com.jopdesign.build.ReplaceAtomicAnnotation 
 * (using org.apache.bcel.util.BCELifier to reverse engineer the compiled 
 * class).
 * 
 * @author Peter Hilber (peter@hilber.name)
 */
public class Transaction {

	public static boolean conflicting = false;

	protected static int atomicSection(int arg0) throws Exception, 
	RollbackException, AbortException {
		boolean ignored = conflicting;
		return arg0;
	}

	/**
	 * @exception RollbackException Thrown by hardware if a conflict is 
	 * detected or by user program using {@link Operations#rollback()} .
	 * Is not user-visible, i.e. not propagated outside of outermost 
	 * transaction.
	 * 
	 * @exception AbortExcepton Thrown by user program to abort transaction, 
	 * using {@link Operations#abort()}. 
	 * Is user-visible, i.e. propagated outside of outermost transaction.
	 */
	public static int run(int arg0) throws RollbackException, AbortException {
		int arg0Copy = 0xdeadbeef; // make compiler happy
		boolean outermostTransaction = !Utils.inTransaction[Native.rd(Const.IO_CPU_ID)];

		if (outermostTransaction) {
			arg0Copy = arg0;

			// TMTODO disable interrupts?
			Native.wr(0, Const.IO_INT_ENA);

			Utils.inTransaction[Native.rd(Const.IO_CPU_ID)] = true;
		}

		while (true) {
			if (outermostTransaction) {
				Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
			}

			try {
				int result = atomicSection(arg0);

				if (outermostTransaction) {

					// flush write set					
					Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);

					// no exceptions happen after this statement

					Utils.inTransaction[Native.rd(Const.IO_CPU_ID)] = false;

					// TMTODO (unconditionally) re-enable interrupts here?
					Native.wr(1, Const.IO_INT_ENA);
				}
				return result;
			} catch (Throwable e) { 
				// RollbackException, AbortException or any other exception

				if (outermostTransaction) {
					Native.wr(0, Const.IO_ENA_HW_EXC);

					// reference comparison is enough
					if (e == Utils.abortException) {
						throw Utils.abortException;
					} else {
						// rollback
						arg0 = arg0Copy;
					}
				} else { // inner transaction
					if (e == Utils.abortException) {
						throw Utils.abortException;
					} else {
						throw Utils.rollbackException;
					}
				}
			}
		}
	}
}
