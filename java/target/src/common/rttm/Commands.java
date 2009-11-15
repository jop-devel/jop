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

import rttm.utils.Utils;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * User-visible transactional memory interface (apart from {@link Atomic}
 * annotation).
 * @author Peter Hilber (peter@hilber.name)
 */
public class Commands {
	/**
	 * TMTODO
	 */
	public static void abort() {
		throw Utils.abortException;
	}
	
	/**
	 * TMTODO
	 */
	public static void earlyCommit() {
		Native.wr(Const.TM_EARLY_COMMIT, Const.MEM_TM_MAGIC);
	}
}
