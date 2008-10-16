/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007,2008, Martin Schoeberl (martin@jopdesign.com)

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

package com.jopdesign.build;

import java.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;

/**
 * 
 * Insert bytecodes for synchronizing methods.
 * 
 * @author Martin Schoeberl, Wolfgang Puffitsch
 * 
 */
public class InsertSynchronized extends JOPizerVisitor {

	private ConstantPoolGen cpoolgen;

	public InsertSynchronized(AppInfo jz) {
		super(jz);
	}

	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);

		Method[] methods = clazz.getMethods();
		cpoolgen = new ConstantPoolGen(clazz.getConstantPool());

		for(int i=0; i < methods.length; i++) {
			if(!(methods[i].isAbstract() || methods[i].isNative())
					&& methods[i].isSynchronized()) {		
				Method m = synchronize(methods[i]);
		        // why does this work without the following line?
		        // mi.setMethod(m);
				if (m!=null) {
					methods[i] = m;
				}
			}
		}
	}

	private Method synchronize(Method method) {

		MethodGen mg  = new MethodGen(method, clazz.getClassName(), cpoolgen);
		InstructionList il  = mg.getInstructionList();
		InstructionFinder f;

		// prepend monitorenter (reversed order of opcodes)
		il.insert(new MONITORENTER());
		if (method.isStatic()) {
			// il.insert(new GET_CURRENT_CLASS());
			throw new Error("synchronized on static methods not yet supported");
		} else {
			il.insert(new ALOAD(0));
		}
		il.setPositions();

		f = new InstructionFinder(il);
		// find return instructions and insert monitorexit
		String retInstr = "ReturnInstruction";

		for(Iterator iterator = f.search(retInstr); iterator.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])iterator.next();
			InstructionHandle   ih = match[0];
			InstructionHandle   newh; // handle for inserted sequence

			if (method.isStatic()) {
				// il.insert(ih, new GET_CURRENT_CLASS());
				throw new Error("synchronized on static methods not yet supported");
			} else {
				newh = il.insert(ih, new ALOAD(0));
			}
			il.insert(ih, new MONITOREXIT());

			// correct jumps
			InstructionTargeter[] it = ih.getTargeters();
			for (int i = 0; it != null && i < it.length; i++) {
				it[i].updateTarget(ih, newh);
			}
		}	
		il.setPositions();

		mg.setInstructionList(il);

		Method m = mg.getMethod();
		il.dispose();
		return m;
	}

}
