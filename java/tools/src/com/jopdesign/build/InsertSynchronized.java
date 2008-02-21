/***************************************************************************
Add code for synchronization of methods.
Copyright (C) 2007  Martin Schoeberl

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
***************************************************************************/
package com.jopdesign.build

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
public class InsertSynchronized extends MyVisitor {

    private ConstantPoolGen cpoolgen;

    public InsertSynchronized(Loader ldr) {
	super(ldr);
    }
	
    public void visitJavaClass(JavaClass clazz) {

	super.visitJavaClass(clazz);
		
	Method[] methods = clazz.getMethods();
	cpoolgen = new ConstantPoolGen(clazz.getConstantPool());
		
	for(int i=0; i < methods.length; i++) {
	    if(!(methods[i].isAbstract() || methods[i].isNative())
	       && methods[i].isSynchronized()) {		
		Method m = synchronize(methods[i]);
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
 	    il.insert(new GET_CURRENT_CLASS());
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
		
	    if (method.isStatic()) {
		il.insert(ih, new GET_CURRENT_CLASS());
	    } else {
		il.insert(ih, new ALOAD(0));
	    }
    	    il.insert(ih, new MONITOREXIT());
	}	
	il.setPositions();

	mg.setInstructionList(il);

	Method m = mg.getMethod();
	il.dispose();
	return m;
    }

}
