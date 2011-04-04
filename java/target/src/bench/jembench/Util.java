/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl  (martin@jopdesign.com)
                      Thomas B. Preußer <thomas.preusser@tu-dresden.de>

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
package jembench;

/* TODO: We should consider replacing this static container by an
 *       overridable abstract interface. This could be specialized
 *       more easily without hampering with actual jbe code:
 *
 *  new Executor(new jbe.Params() {
 *    public int  availableProcessors() { return  1; }
 *    public long currentTimeMillis()   { return  System.currentTimeMillis(); }
 *  });
 *
 *  Implementations for common settings could be provided.
 */

/**
 * Container for utility functions.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public final class Util {

  /** Disallow instantiation of this static container class. */
  private Util() {}
	
	/**
	 * To be compatible with CLDC we return 1 as default.
	 * Remove the comment to run CMP benchmarks on a Java
	 * system where availableProcessors() is supported.
	 * @return
	 */
	public static int getNrOfCores() {
	  // return  1;
	  return  Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Provide a wrapper for the time measurement. Can use
	 * a different (higher resolution) clock when available.
	 * @return
	 */
	public static int getTimeMillis() {
		return (int) System.currentTimeMillis();
	}
}
