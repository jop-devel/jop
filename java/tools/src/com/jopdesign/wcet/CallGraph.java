/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) 2006, Rasmus Ulslev Pedersen

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

package com.jopdesign.wcet;

import java.io.IOException;
import java.util.*;

import com.jopdesign.build.AppInfo;
import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.JopClassInfo;
import com.jopdesign.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.util.InstructionFinder;

/**
 * That's just a test program...
 *
 * @author Martin Schoeberl
 *
 */

public class CallGraph extends AppInfo {

	static ClassInfo template = ClassInfo.getTemplate();

	public CallGraph(String[] args) throws IOException {
		super(template);
		parseOptions(args);
		load();
	}

	MethClass mainMethClass;
	HashMap methMap = new HashMap();

	List methodList = new LinkedList();
	Graph cg = new Graph();
	MethodVertex root;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CallGraph la = null;
		try {
			la = new CallGraph(args);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		la.findMain();
		if (la.mainMethClass==null) {
			System.out.println("Error: main method '"+la.mainMethodName+"' not found");
			System.exit(-1);
		}
		la.root = new MethodVertex(la.mainMethClass);
		la.traverse(la.root);

//		Vertex v1 = la.cg.addVertex("abc");
//		Vertex v2 = la.cg.addVertex("def");
//		Vertex v3 = la.cg.addVertex("XYZ");
//		la.cg.addEdge(v1, v2);
//		la.cg.addEdge(v1, v3);
		System.out.println(la.cg.printGraph());


	}

	private MethodVertex traverse(MethodVertex mv) {

		cg.addVertex(mv);
		Method method = mv.getMethod();
		System.out.println("in traverse for "+method);
		if (method.isNative()) {
			System.out.println("is native");
			return mv;
		}

		ConstantPoolGen cpoolgen = new ConstantPoolGen(method.getConstantPool());
		// Why the hell do I have construct a MethodGen just
		// to find invoke instructions???
		MethodGen mg  = new MethodGen(method, "abcdef", cpoolgen);
		InstructionList il  = mg.getInstructionList();
		InstructionFinder f = new InstructionFinder(il);

		String methodId = method.getName() + method.getSignature();
		// if(methodId.equalsIgnoreCase("f_lshl(III)J")){


		// find invokes and traverse them
		String invokeStr = "InvokeInstruction";
		for(Iterator i = f.search(invokeStr); i.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])i.next();
			InstructionHandle   first = match[0];
			InvokeInstruction ii = (InvokeInstruction)first.getInstruction();
			String id  = ii.getClassName(cpoolgen)+'.'+
				ii.getMethodName(cpoolgen)+ii.getSignature(cpoolgen);

			// TODO: add possible super class methods
			MethClass mc = (MethClass) methMap.get(id);
			System.out.println(id);
			MethodVertex mcalled = new MethodVertex(mc);
			traverse(mcalled);
			cg.addEdge(mv, mcalled);

		}


		return mv;
	}

	public void findMain() {

		Collection<? extends ClassInfo> jclSet = cliMap.values();
		for (Iterator iterator = jclSet.iterator(); iterator.hasNext();) {
			ClassInfo info = (ClassInfo) iterator.next();
			JavaClass jclazz = info.clazz;
			Method[] ma = jclazz.getMethods();
			for (int j=0; j<ma.length; ++j) {
				String id  = jclazz.getClassName()+'.'+
					ma[j].getName()+ma[j].getSignature();
				MethClass mc = new MethClass(ma[j], jclazz);
				methMap.put(mc.toString(), mc);

				if (jclazz.getClassName().equals(mainClass)) {
					if (ma[j].getName().equals(mainMethodName)) {
						mainMethClass = mc;
					}
				}
			}

		}
		System.out.println(mainMethClass);
	}
}

class MethClass {
	Method m;
	JavaClass jc;

	public MethClass(Method meth, JavaClass jclazz) {
		m = meth;
		jc = jclazz;
	}

	public String toString() {
		return jc.getClassName()+'.'+m.getName()+m.getSignature();
	}
}

class MethodVertex extends Vertex {

	MethClass mc;
	public MethodVertex(MethClass methClass) {
		super(methClass.toString());
		mc = methClass;
	}

	public Method getMethod() {
		return mc.m;
	}

	MethClass getMethodClass() {
		return mc;
	}

	public String toDotString() {
		return mc.jc.getClassName()+"\\n"+mc.m.getName()+mc.m.getSignature();
	}

}


// new DescendingVisitor(jclazz[i], new CallGraphVisitor()).visit();


//class CallGraphVisitor extends EmptyVisitor {
//
//	public void visitJavaClass(JavaClass clazz) {
//		System.out.println(clazz.getClassName());
//	}
//
//	public void visitMethod(Method m) {
//		System.out.println("\t"+m.getName());
//	}
//}
