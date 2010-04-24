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

package rttm;

import com.jopdesign.sys.RetryException;
import rttm.AbortException;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * Transactional memory interface (apart from {@link atomic} annotation).
 * @author Peter Hilber (peter@hilber.name)
 */
public class Commands {
	/**
	 * Abort current transaction and throw {@link AbortException}.
	 * 
	 * It is the user's responsibility to ensure that no early commit has
	 * happened before calling this method.
	 */
	public static void abort() {
		// immediately abort transaction to avoid early commit 
		Native.wrMem(Const.TM_ABORTED, Const.MEM_TM_MAGIC);
		throw AbortException.instance;
	}

	/**
	 * Explicitly restart the current transaction. 
	 * 
	 * It is the user's responsibility to ensure that no early commit has
	 * happened before calling this method.
	 */
	public static void retry() {
		// immediately abort transaction to avoid early commit
		Native.wrMem(Const.TM_ABORTED, Const.MEM_TM_MAGIC);
		throw RetryException.instance;
	}
	
	/**
	 * Explicitly perform early commit.
	 * 
	 * An early commit assures that the transaction will not abort due to a 
	 * conflict and must be performed before doing I/O in a transaction.  
	 */
	public static void earlyCommit() {
		Native.wr(Const.TM_EARLY_COMMIT, Const.MEM_TM_MAGIC);
	}
}
