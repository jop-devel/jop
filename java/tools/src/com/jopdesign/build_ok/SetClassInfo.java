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

package com.jopdesign.build_ok;


import org.apache.bcel.classfile.*;

/**
 * @author martin
 *
 */
public class SetClassInfo extends MyVisitor {

	private ConstantPool cpool;
	
	public SetClassInfo(JOPizer jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
// System.err.println("visit "+clazz.getClassName()+" getSuper");
		cli.superClass = ClassInfo.getClassInfo(clazz.getSuperclassName());
		if (clazz.getClassName().equals("java.lang.Object")) {
			// Object has no super class
			cli.superClass = null;
		} else {
			// add this ClassInfo as a known sub class to the super class
			cli.superClass.addSubClass(ClassInfo.getClassInfo(clazz.getClassName()));
		}
		// this one searches in the application CLASSPATH!!!
		/*
		JavaClass suClazz = clazz.getSuperClass();
		if (suClazz!=null) {
			cli.superClass = ClassInfo.getClassInfo(suClazz.getClassName());
		} else {
			cli.superClass = null;
		}
		*/
// System.err.println("after visit getSuper");
		cpool = clazz.getConstantPool();

		if (clazz.isInterface()) {
			cli.interfaceID = ++cli.interfaceCnt;
			cli.interfaceList.add(cli.interfaceID-1, clazz.getClassName());
		}
	}

	public void visitMethod(Method method) {
		
		String methodId = method.getName()+method.getSignature();
        cli.addMethodOnce(methodId);
        MethodInfo mi = cli.getMethodInfo(methodId);
        mi.setMethod(method);
        if(JOPizer.dumpMgci){
          // GCRT
          new GCRTMethodInfo(mi,method);
        }

	}

	public void visitConstantString(ConstantString S) {
		// identifying constant strings
		StringInfo.addString(S.getBytes(cpool));
	}

}
