/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)

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

package com.jopdesign.build_ok;

import java.util.Iterator;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.verifier.statics.DOUBLE_Upper;
import org.apache.bcel.verifier.statics.LONG_Upper;

import com.jopdesign.tools.JopInstr;

/**
 * @author Martin
 *
 * replaces IINC by ILOAD, push the constant, IADD, and ISTORE
 * 
 * avoids issues with the Java 1.5 compiler (produces WIDE IINC) and
 * generates faster code on JOP.
 * 
 */
public class ReplaceIinc extends MyVisitor {

	// Why do we use a ConstantPoolGen and a ConstantPool?
	private ConstantPoolGen cpoolgen;
	private ConstantPool cp;
	
	public ReplaceIinc(JOPizer jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		
		Method[] methods = clazz.getMethods();
		cp = clazz.getConstantPool();
		cpoolgen = new ConstantPoolGen(cp);
		
		for(int i=0; i < methods.length; i++) {
			if(!(methods[i].isAbstract() || methods[i].isNative())) {
				Method m = replace(methods[i]);
		        MethodInfo mi = cli.getMethodInfo(m.getName()+m.getSignature());
		        // set new method also in MethodInfo
		        mi.setMethod(m);
				if (m!=null) {
					methods[i] = m;
				}
			}
		}
	}


	private Method replace(Method method) {
		
		MethodGen mg  = new MethodGen(method, clazz.getClassName(), cpoolgen);
		InstructionList il  = mg.getInstructionList();
		InstructionFinder f = new InstructionFinder(il);
    
		for(Iterator i = f.search("IINC"); i.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])i.next();
			InstructionHandle   ih = match[0];
			IINC ii = (IINC) ih.getInstruction();
			int idx = ii.getIndex();
			int inc = ii.getIncrement();
//			IINC rep = new IINC(idx, inc);
			ih.setInstruction(new ILOAD(idx));
			if (inc>=-1 && inc<=5) {
				ih = il.append(ih, new ICONST(inc));				
			} else if (inc>=-128 && inc<127){
				ih = il.append(ih, new BIPUSH((byte) inc));								
			} else if (inc>=-32768 && inc<32767){
				ih = il.append(ih, new SIPUSH((short) inc));								
			} else {
				System.out.println("IINC constant too big");
				System.exit(-1);
			}
			ih = il.append(ih, new IADD());
			ih = il.append(ih, new ISTORE(idx));
		}
		

		Method m = mg.getMethod();
		il.dispose();
		return m;

	}

}
