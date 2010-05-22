/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, 2010 Peter Hilber (peter@hilber.name)

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

package rttm.tests.manual;

import static com.jopdesign.sys.Const.*;
import com.jopdesign.sys.Native;

import com.jopdesign.sys.RetryException;

import rttm.AbortException;
import rttm.Commands;
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

	protected static int originalMethodBody(int arg0) throws RetryException, 
		AbortException, Throwable {
		boolean ignored = conflicting;
		return arg0;
	}

	/**
	 * @exception RetryException Thrown by hardware if a conflict is 
	 * detected or by user program using {@link Commands#retry()} .
	 * Is not user-visible, i.e. not propagated outside of not nested 
	 * transaction.
	 * 
	 * @exception AbortExcepton Thrown by user program to abort transaction, 
	 * using {@link Commands#abort()}. 
	 * Is user-visible, i.e. propagated outside of not nested transaction.
	 */
	public static int atomicMethod(int arg0) throws RetryException, 
		AbortException, Throwable {
		int arg0Copy = 0xdeadbeef; // make compiler happy
		boolean isNotNestedTransaction = 
			!Utils.inTransaction[Native.rdMem(IO_CPU_ID)];

		if (isNotNestedTransaction) {
			arg0Copy = arg0; // save method arguments		
			Native.wrMem(0, IO_INT_ENA); // disable interrupts
			Utils.inTransaction[Native.rdMem(IO_CPU_ID)] = true;
		}

		while (true) {
			if (isNotNestedTransaction) {
				Native.wrMem(TM_START_TRANSACTION, MEM_TM_MAGIC);
			}

			try {
				// Not really a method invocation
				// The original method body is inserted here, return 
				// statements in it are redirected to the next statement
				int result = originalMethodBody(arg0);

				if (isNotNestedTransaction) {

					// try commit					
					Native.wrMem(TM_END_TRANSACTION, MEM_TM_MAGIC);
					// no exceptions happen after here

					Utils.inTransaction[Native.rdMem(IO_CPU_ID)] = false;
					Native.wrMem(1, IO_INT_ENA); // re-enable interrupts
				}
				return result;
			} catch (Throwable e) {
				// exception handling issues ABORTED HW command
				// e is RetryException, AbortException or other exception

				if (isNotNestedTransaction) {
					// reference comparison is enough for singleton
					if (e == RetryException.instance) {
						// restore method arguments
						arg0 = arg0Copy;						
					} else {
						// transaction was manually aborted or a bug triggered
						Utils.inTransaction[Native.rdMem(IO_CPU_ID)] = false;
						Native.wrMem(1, IO_INT_ENA); // re-enable interrupts
						throw e;
					}
				} else { // nested transaction: propagate exception
					throw e;
				}
			}
		}
	}
}
