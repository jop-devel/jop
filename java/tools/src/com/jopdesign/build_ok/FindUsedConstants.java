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
 * Created on 04.06.2005
 *
 */
package com.jopdesign.build_ok;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.util.InstructionFinder;

import com.jopdesign.tools.JopInstr;

/**
 * Create a usefull constant pool mapping and change the index in the
 * instructions.
 * 
 * @author Flavius, Martin
 *
 *
 */
public class FindUsedConstants extends MyVisitor {

	private ConstantPoolGen cpool;
	private ConstantPoolGen cpoolNew;
	/**
	 * mapping for a reduced constant pool.
	 * Will get filled by FindUsedConstants.
	 */
	private HashMap cpoolMap;
	
	public FindUsedConstants(JOPizer jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		
		cpool = new ConstantPoolGen(clazz.getConstantPool());
		cpoolMap = new HashMap();
		cpoolNew = new ConstantPoolGen();

		Method[] methods = clazz.getMethods();
		
		for(int i=0; i < methods.length; i++) {
			if(!(methods[i].isAbstract() || methods[i].isNative())) {
				// methods[i] = find(methods[i]);
				find(methods[i]);
			}
		}
//		clazz.setConstantPool(cpoolNew.getConstantPool());
//System.out.println(clazz.getConstantPool());
		
	}


	private void find(Method method) {
		
		boolean replace = false;
		
		
		MethodGen mg  = new MethodGen(method, clazz.getClassName(), cpool);
		InstructionList il  = mg.getInstructionList();
		InstructionFinder f = new InstructionFinder(il);
		
		// find instructions that access the constant pool
		// collect all indices to constants in ClassInfo
		String cpInstr = "CPInstruction";		
		for(Iterator it = f.search(cpInstr); it.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])it.next();
			InstructionHandle   first = match[0];
			
			CPInstruction ii = (CPInstruction)first.getInstruction();
			int idx = ii.getIndex();
			Constant co = cpool.getConstant(idx);
			int len = 1;
			switch(co.getTag()) {
				case Constants.CONSTANT_Long:
				case Constants.CONSTANT_Double:
					len = 2;
			}
// System.out.println(co+" "+idx+" "+len);	
			cli.addUsedConst(idx, len);
			
			// also modify the index!
//			Constant cnst = cpool.getConstant(ii.getIndex());
//			int newIndex = addConstant(cnst);
//System.out.println(ii+" -> "+newIndex);
//			ii.setIndex(newIndex);			
			
		}
		
		il.dispose();

		CodeExceptionGen[] et = mg.getExceptionHandlers();
		for (int i = 0; i < et.length; i++) {
			ObjectType ctype = et[i].getCatchType();
			if (ctype != null) {
				cli.addUsedConst(cpool.lookupClass(ctype.getClassName()), 1);
			}
		}
	}
	
	
}
