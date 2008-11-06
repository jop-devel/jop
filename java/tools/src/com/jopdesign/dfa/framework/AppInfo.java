/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

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

package com.jopdesign.dfa.framework;

import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.Type;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.build.ClinitOrder;

public class AppInfo extends com.jopdesign.build.AppInfo {

	private static final long serialVersionUID = 1L;

	private static final String mainName = "main";
	private static final String mainSig = "([Ljava/lang/String;)V";

	private static final String clinitName = "<clinit>";
	private static final String clinitSig = "()V";

	private static final String prologueName = "<prologue>";
	private static final String prologueSig = "()V";

	private List<InstructionHandle> statements;
	private Flow flow;
	private Map<InstructionHandle, ContextMap<String, String>> receivers;
	
	public AppInfo(ClassInfo cliTemplate) {		
		super(cliTemplate);
		this.statements = new LinkedList<InstructionHandle>();
		this.flow = new Flow();
		this.receivers = null;
	}

	public void load() throws IOException {
		
		// do the usual stuff
		super.load();
		
		// find oredering for class initializers
		List<String> clinits = new LinkedList<String>();
		
		ClinitOrder c = new ClinitOrder(this);
		iterate(c);

		List order = c.findOrder();
		for (Iterator i = order.iterator(); i.hasNext(); ) {
			JavaClass jc = ((ClassInfo)i.next()).clazz;
			clinits.add(jc.getClassName()+"."+clinitName+clinitSig);
		}

		// create prologue
		buildPrologue(mainClass, statements, flow, clinits);	
	}
	
	private void buildPrologue(String mainClass, List<InstructionHandle> statements, Flow flow, List<String> clinits) {

		// we use a prologue sequence for startup
		InstructionList prologue = new InstructionList();
		ConstantPoolGen prologueCP = new ConstantPoolGen();

		Instruction instr;
		int idx;

		// add magic initializers to prologue sequence
		instr = new ICONST(0);
		prologue.append(instr);
		instr = new ICONST(0);
		prologue.append(instr);
		idx = prologueCP.addMethodref("com.jopdesign.sys.GC", "init", "(II)V");
		instr = new INVOKESTATIC(idx);
		prologue.append(instr);
		idx = prologueCP.addMethodref("java.lang.System", "init", "()V");
		instr = new INVOKESTATIC(idx);
		prologue.append(instr);

		// add class initializers
		for (Iterator<String> i = clinits.iterator(); i.hasNext(); ) {	
			String clinit = i.next();					
			String className = clinit.substring(0, clinit.lastIndexOf("."));
			idx = prologueCP.addMethodref(className, clinitName, clinitSig);
			instr = new INVOKESPECIAL(idx); 
			prologue.append(instr);
		}

		// add main method
		instr = new ACONST_NULL();
		prologue.append(instr);
		idx = prologueCP.addMethodref(mainClass, mainName, mainSig);
		instr = new INVOKESTATIC(idx);
		prologue.append(instr);
		instr = new NOP();
		prologue.append(instr);

		prologue.setPositions(true);

//			System.out.println(prologue);

		// add prologue to program structure
		for (Iterator l = prologue.iterator(); l.hasNext(); ) {
			InstructionHandle handle = (InstructionHandle)l.next();
			statements.add(handle);
			if (handle.getNext() != null) {
				flow.addEdge(new FlowEdge(handle, handle.getNext(), FlowEdge.NORMAL_EDGE));
			}
		}

		MethodGen mg = new MethodGen(Constants.ACC_PRIVATE, Type.VOID, Type.NO_ARGS, null, mainClass+"."+prologueName+prologueSig, "", prologue, prologueCP);
		MethodInfo mi = new MethodInfo(cliMap.get(mainClass), prologueName+prologueSig);
		mi.setMethodGen(mg);
		cliMap.get(mainClass).getMethodInfoMap().put(prologueName+prologueSig, mi);
	}
		
	public Map runAnalysis(Analysis analysis) {

		Interpreter interpreter = new Interpreter(analysis, this);

		try {
			MethodInfo prologue = getMethod(mainClass+"."+prologueName+prologueSig);

			Context context = new Context();
			context.stackPtr = 0;
			context.syncLevel = 0;
			context.constPool = new ConstantPoolGen(prologue.getMethod().getConstantPool());
			context.method = prologue.methodId;

			analysis.initialize(mainClass+"."+mainName+mainSig, context);

			InstructionHandle entry = prologue.getMethodGen().getInstructionList().getStart();
			interpreter.interpret(context, entry, new HashMap(), true);
		} catch (Throwable thr) {
			thr.printStackTrace();
		}
		
		return analysis.getResult();
	}
	
	public List<InstructionHandle> getStatements() {
		return statements;
	}

	public Flow getFlow() {
		return flow;
	}

	public Map<InstructionHandle, ContextMap<String, String>> getReceivers() {
		return receivers;
	}

	public void setReceivers(Map receivers) {
		this.receivers = receivers;
	}
	
	public MethodInfo getMethod(String methodName) {
		String className = methodName.substring(0, methodName.lastIndexOf("."));
		String signature = methodName.substring(methodName.lastIndexOf(".")+1, methodName.length());
		ClassInfo cli = (ClassInfo)cliMap.get(className);
		//System.out.println(cli.toString()+": "+cli.getMethods().keySet());
		//System.out.println(signature+": "+cli.getMethods().get(signature)+" "+cli.getMethods().containsKey(signature));
		return cli.getMethodInfo(signature);
	}
	
	public boolean containsField(String fieldName) {
		String className = fieldName.substring(0, fieldName.lastIndexOf("."));
		String signature = fieldName.substring(fieldName.lastIndexOf(".")+1, fieldName.length());
		ClassInfo cli = (ClassInfo)cliMap.get(className);
		//System.out.println("contains: "+cli+" vs "+fieldName);
		return cli.getFields().contains(signature);
	}
	
}
