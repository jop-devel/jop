package com.jopdesign.dfa.framework;

import java.io.IOException;
import java.util.Iterator;
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
import org.apache.bcel.util.ClassPath;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.build.ClinitOrder;

public class AppInfo extends com.jopdesign.build.AppInfo {

	private static final long serialVersionUID = 1L;

	private List<InstructionHandle> statements;
	private Flow flow;
	private Map<InstructionHandle, ContextMap<String, String>> receivers;
	
	public AppInfo(ClassPath classpath, String mainClass) {
		
		super(new ClassInfo());
		this.classpath = classpath;
		this.mainClass = mainClass;
		this.statements = new LinkedList<InstructionHandle>();
		this.flow = new Flow();
		this.receivers = null;
		addClass(mainClass);
		
		try {
			load();
		} catch (IOException exc) {
			exc.printStackTrace();
			System.exit(-1);
		}
		
		List<String> clinits = new LinkedList<String>();
		
		ClinitOrder c = new ClinitOrder(this);
		iterate(c);

		List<ClassInfo> order = c.findOrder();
		for (Iterator<ClassInfo> i = order.iterator(); i.hasNext(); ) {
			JavaClass jc = i.next().clazz;
			clinits.add(jc.getClassName()+".<clinit>()V");
		}

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
			String clinitSig = i.next();					
			String className = clinitSig.substring(0, clinitSig.lastIndexOf("."));
			idx = prologueCP.addMethodref(className, "<clinit>", "()V");
			instr = new INVOKESPECIAL(idx); 
			prologue.append(instr);
		}

		// add main method
		instr = new ACONST_NULL();
		prologue.append(instr);
		idx = prologueCP.addMethodref(mainClass, "main", "([Ljava/lang/String;)V");
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

		MethodGen method = new MethodGen(Constants.ACC_PRIVATE, Type.VOID, Type.NO_ARGS, null, "java.lang.Object.<prologue>", "", prologue, prologueCP);
		MethodInfo mi = new MethodInfo(cliMap.get("java.lang.Object"), "<prologue>");
		mi.setMethodGen(method);
		cliMap.get("java.lang.Object").getMethodInfoMap().put("<prologue>", mi);
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

	public void setReceivers(Map<InstructionHandle, ContextMap<String, String>> receivers) {
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
