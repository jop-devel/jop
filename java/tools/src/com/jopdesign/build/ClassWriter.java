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

import org.apache.bcel.classfile.JavaClass;

import java.io.File;
import java.io.IOException;

/**
 * @author Martin Schoeberl
 * @deprecated
 */
public class ClassWriter extends AppVisitor {

	String dir;
	public ClassWriter(OldAppInfo ai, String outDir) {
		super(ai);
		dir = outDir;
	}

	@Override
	public void visitJavaClass(JavaClass clazz) {
		super.visitJavaClass(clazz);

		String filename = dir+File.separator+
			clazz.getClassName().replace(".", File.separator)+".class";
	    try {
			cli.writeClassFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
