/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package com.jopdesign.build;

import java.util.*;

import org.apache.bcel.classfile.JavaClass;
/**
 * The new version of ClassInfo
 * @author Martin Schoeberl
 *
 */
public class CInfo {

	JavaClass clazz;
	
	/**
	 * A strang kind of factory method as we are too lazy to
	 * do a real factory pattern.
	 * @param jc
	 * @return
	 */
	Map<String, CInfo> genCInfoMap(JavaClass jc[]) {
		Map<String, CInfo> map = new HashMap<String, CInfo>();
		for (int i=0; i<jc.length; ++i) {
			CInfo cli = new CInfo();
			cli.clazz = jc[i];
			map.put(cli.clazz.getClassName(), cli);
		}
		return map;
	}
	
	public String toString() {
		return clazz.getClassName();
	}
}
