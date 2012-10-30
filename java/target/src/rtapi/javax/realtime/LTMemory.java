/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
  This subset of javax.realtime is provided for the JSR 302
  Safety Critical Specification for Java

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * TODO: We shall just simply override the methods in the SCJ classes
 * and drop them from here. It would not be noticeable, right?
 * 
 * We don't want to implement the meat of memory management in
 * an RTSJ class.
 * 
 * A quick fix would be to make it abstract. And we will do ;-)
 * 
 * @author martin
 *
 */
@SCJAllowed
public abstract class LTMemory extends ScopedMemory {

//	protected LTMemory(int size, int bsSize) {
//		super(size, bsSize);
//		// TODO Auto-generated constructor stub
//	}
//
//	protected LTMemory(int size) {
//		this(size,0);
//	}
	
	/**
	 * We don't allow to use the constructor....
	 * @param size
	 */
//	@SCJProtected
//	public LTMemory(long size) {
////		super(size);
//	}

}
