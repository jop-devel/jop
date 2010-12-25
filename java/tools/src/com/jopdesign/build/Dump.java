/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

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

/*
 */
package com.jopdesign.build;


import java.io.PrintWriter;

import org.apache.bcel.classfile.*;

/**
 * @author martin
 *
 * Just dump all methods to the debug text file
 */
public class Dump extends AppVisitor {

	PrintWriter outTxt;
	public Dump(OldAppInfo ai, PrintWriter pw) {
		super(ai);
		outTxt = pw;
	}
	
	public void visitMethod(Method method) {

		outTxt.println(clazz.getClassName()+":"+method.getName()+method.getSignature());
		outTxt.println(method.getCode());
	}
}
