/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.wcet.annotations;

import com.jopdesign.common.code.LoopBound;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Purpose:
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class SourceAnnotations {
	private TreeMap<Integer, LoopBound> loopBounds = new TreeMap<Integer,LoopBound>();

	/**
	 * @param lineNr     source code line number
	 * @param annotatedBound the annotations at this line number
	 */
	public void addLoopBound(int lineNr, LoopBound annotatedBound) {
		LoopBound oldBounds = this.loopBounds.get(lineNr);
		if(oldBounds == null) {
			this.loopBounds.put(lineNr, annotatedBound);
		} else {
			oldBounds.addBound(annotatedBound);
		}
	}

	/**
	 * @param firstLine
	 * @param lastLine
	 * @return the loop bounds for the line range
	 */
	public Collection<LoopBound> annotationsForLineRange(int firstLine, int lastLine) {
		return loopBounds.subMap(firstLine, lastLine).values();
	}

	/**
	 * @param srcLine
	 * @return
	 */
	public LoopBound annotationsForLine(int srcLine) {
		return loopBounds.get(new Integer(srcLine));
	}
	
}
