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

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

/**
 * Info for methods. Shall be extended fo application specific
 * type and use the funny factory contructor.
 * @author Martin Schoeberl
 * @deprecated
 */
public class OldMethodInfo {

	private OldClassInfo cli;
	private Method method;
	private MethodGen methodGen;
	private ConstantPoolGen constGen;
	public String methodId;

	public OldMethodInfo(OldClassInfo classInfo, String mid) {
		cli = classInfo;
		methodId = mid;
	}

	/**
	 * Set on ClassInfo visitor and visitors that manipulate the method.
	 * 
	 * @param m
	 */
	public void setMethod(Method m) {
		method = m;
		methodGen = null;
	}
	
	public void setMethodGen(MethodGen m) {
		methodGen = m;
		updateMethodFromGen();
	}
	
	public void updateMethodFromGen() {
		methodGen.setMaxStack();
		methodGen.setMaxLocals();
		method = methodGen.getMethod();		
	}
	
	public Method getMethod() {
		return method;
	}
	
	public MethodGen getMethodGen() {
		return methodGen;
	}
	
	public ConstantPoolGen getConstantPoolGen() {
		return methodGen.getConstantPool();
	}

	public Code getCode() {
		return method.getCode();
	}
	
	public OldClassInfo getCli() {
		return cli;
	}

	public String getFQMethodName() {
		return cli.clazz.getClassName() + "." + methodId;
	}
	
	@Override public String toString() {
		return super.toString()+"\""+getFQMethodName() +"\"";
	}
	@Override public boolean equals(Object other) {
		if(this == other) return true;
		return(this.getFQMethodName().equals(((OldMethodInfo) other).getFQMethodName()));
	}
	@Override public int hashCode() {
		return(this.getFQMethodName().hashCode());
	}
}
