/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet08.frontend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Vector;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.log4j.Logger;

import com.jopdesign.build.AppInfo;
import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.Config.MissingConfigurationError;
import com.jopdesign.wcet08.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet08.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet08.graphutils.TopOrder.BadGraphException;
import com.jopdesign.wcet08.report.InvokeDot;

/**
 * AppInfo subclass for the WCET analysis.
 * Provides a TypeGraph.
 * 
 * @author Benedikt Huber, benedikt.huber@gmail.com
 */
public class WcetAppInfo  {
	private static final long serialVersionUID = 2L;
 	/* package logger */
	public static final Logger logger = Logger.getLogger(WcetAppInfo.class.getPackage().toString());
	/**
	 * Raised when we cannot find / fail to load a referenced method.
	 */
	public class MethodNotFoundException extends Exception {
		private static final long serialVersionUID = 1L;
		public MethodNotFoundException(String message) {
			super(message);			
		}
	}	

	public static final String JVM_CLASS = "com.jopdesign.sys.JVM";

	private TypeGraph typeGraph;
	private AppInfo ai;
	private Map<MethodInfo, ControlFlowGraph> cfgs;
	private List<ControlFlowGraph> cfgsByIndex;
	private Map<InstructionHandle, ContextMap<String, String>> receiverAnalysis = null;

	public WcetAppInfo(com.jopdesign.build.AppInfo ai) {
		this.ai = ai; 
		this.typeGraph = new TypeGraph(this);
	}
	
	/**
	 * @return A mapping from the name of a loaded class to {@link ClassInfo}.
	 */
	public Map<String, ? extends ClassInfo> getCliMap() {
		return ai.cliMap;
	}
	/**
	 * @return The typegraph of all loaded classes
	 */
	public TypeGraph getTypeGraph() {
		return typeGraph;
	}
	/**
	 * @param className Name of the class to lookup
	 * @return the class info, or null if the class could'nt be found
	 */
	public ClassInfo getClassInfo(String className) {
		return getCliMap().get(className);
	}

	/**
	 * Find the given method
	 * @param className The fully qualified name of the class the method is located in
	 * @param methodName The name of the method to be searched. 
	 * 				     Note that the signature is optional if the method name is unique.
	 * @return The method searched for, or null if it couldn't be found
	 * @throws MethodNotFoundException if the method couldn't be found or is ambigous
	 */
	public MethodInfo searchMethod(String className, String methodName) throws MethodNotFoundException {
		ClassInfo cli = getCliMap().get(className);
		if(cli == null) throw new MethodNotFoundException("The class "+className+" couldn't be found");
		return searchMethod(cli,methodName);
	}
	private MethodInfo searchMethod(ClassInfo cli, String methodName) throws MethodNotFoundException {
		MethodInfo mi = null;
		if(methodName.indexOf("(") > 0) {
			mi = cli.getMethodInfo(methodName);
		} else {
			for(MethodInfo candidate : cli.getMethods()) {
				if(methodName.equals(candidate.getMethod().getName())) {
					if(mi == null) {
						mi = candidate;
					} else {
						throw new MethodNotFoundException("The method name "+methodName+" is ambigous."+
														  "Both "+mi.methodId+" and "+candidate.methodId+" match");
					}
				}
			}			
		}
		if(mi == null) {
			throw new MethodNotFoundException("The method "+cli.toString()+"."+methodName+" could not be found");
		}
		return mi;
	}
	/**
	 * Return the receiver name and method name of a 
	 * method referenced by the given invoke instruction
	 * @param invokerCi the classinfo of the method which contains the {@link InvokeInstruciton}
	 * @param instr the invoke instruction
	 * @return A pair of class info and method name
	 */
	public MethodRef getReferenced(ClassInfo invokerCi, InvokeInstruction instr) {
		ConstantPoolGen cpg = new ConstantPoolGen(invokerCi.clazz.getConstantPool());
		String classname = instr.getClassName(cpg );
		String methodname = instr.getMethodName(cpg) + instr.getSignature(cpg);
		ClassInfo refCi = getClassInfo(classname);
		if(refCi == null) throw new AssertionError("Failed class lookup (invoke target): "+classname);
		return new MethodRef(refCi,methodname);
	}
	public MethodRef getReferenced(MethodInfo method, InvokeInstruction instr) {
		return getReferenced(method.getCli(),instr);
	}

	public MethodInfo findStaticImplementation(MethodRef ref) {
		ClassInfo receiver = ref.getReceiver();
		String methodId  = ref.getMethodId();
		MethodInfo staticImpl = ref.getReceiver().getMethodInfo(methodId);
		if(staticImpl == null) {
			ClassInfo superRec = receiver;
			while(staticImpl == null && superRec != null) {
				staticImpl = superRec.getMethodInfo(methodId);
				if(superRec.clazz.getSuperClass() == null) superRec = null;
				else superRec = superRec.superClass;
			}
		}		
		return staticImpl;
	}

	/**
	 * Find possible implementations of the given method in the given class
	 * <p>
	 * For all candidates, check whether they implement the method.
	 * All subclasses of the receiver class are candidates. If the method isn't implemented
	 * in the receiver, the lowest superclass implementing the method is a candidate too.
	 * </p>
	 * @param receiver The class info of the receiver
	 * @param methodname The method name
	 * @return list of method infos that might be invoked
	 */
	public List<MethodInfo> findImplementations(MethodRef methodRef) {
		Vector<MethodInfo> impls = new Vector<MethodInfo>(3);
		tryAddImpl(impls,findStaticImplementation(methodRef));
		for(ClassInfo subty : this.typeGraph.getStrictSubtypes(methodRef.getReceiver())) {
			MethodInfo subtyImpl = subty.getMethodInfo(methodRef.getMethodId());
			tryAddImpl(impls,subtyImpl);
		}
		return impls;
	}
	
	/**
	 * Variant operating on an instruction handle and therefore capable of 
	 * using DFA analysis results.
	 * @param invInstr
	 * @return
	 */
	public List<MethodInfo> findImplementations(MethodInfo invokerM, InstructionHandle ih) {
		MethodRef ref = this.getReferenced(invokerM, (InvokeInstruction) ih.getInstruction());
		List<MethodInfo> staticImpls = findImplementations(ref);
		// TODO: rather slow, for debugging purposes
		if(this.receiverAnalysis != null) {
			ContextMap<String, String> receivers = receiverAnalysis.get(ih);
			List<MethodInfo> dynImpls = new Vector<MethodInfo>();
			Set<String> dynReceivers = receivers.keySet();
			for(MethodInfo impl : staticImpls) {
				if(dynReceivers.contains(impl.getFQMethodName())) {
					dynReceivers.remove(impl.getFQMethodName());
					dynImpls.add(impl);
				} else {
					logger.info("Static but not dynamic receiver: "+impl);
				}
			}
			if(! dynReceivers.isEmpty()) {
				throw new AssertionError("Bad receiver analysis ? Dynamic but not static receivers: "+dynReceivers);
			}
			staticImpls = dynImpls;
		}
		return staticImpls;
	}

	/* helper to avoid code dupl */
	private void tryAddImpl(List<MethodInfo> ms, MethodInfo m) {
		if(m != null) {
			if(! m.getMethod().isAbstract() && ! m.getMethod().isInterface()) {
				ms.add(m);
			}
		}		
	}

	/**
	 * check whether we need to deal with the given statement in a special way,
	 * because it is translated to a JOP specific microcode sequence
     *
	 * @param instr the instruction to check
	 * @return true, if this is translated to a JOP specific bytecode
	 */
	public boolean isSpecialInvoke(ClassInfo ci, Instruction i) {		
		if(! (i instanceof INVOKESTATIC)) return false;
		ConstantPoolGen cpg = new ConstantPoolGen(ci.clazz.getConstantPool());
		String classname = ((INVOKESTATIC) i).getClassName(cpg);
		return (classname.equals("com.jopdesign.sys.Native"));		
	}
	public boolean isSpecialInvoke(MethodInfo methodInfo, Instruction i) {
		return isSpecialInvoke(methodInfo.getCli(),i);
	}

	/**
	 * Get the (actual) opcode of a statement, as executed on JOP
	 * @param instr the BCEL instructions
	 * @return
	 */
	public int getJOpCode(ClassInfo ci, Instruction instr) {
		if(isSpecialInvoke(ci,instr)) {
			ConstantPoolGen cpg = new ConstantPoolGen(ci.clazz.getConstantPool());
			String methodName = ((INVOKESTATIC) instr).getMethodName(cpg);			
			return JopInstr.getNative(methodName);
		} else {
			return instr.getOpcode();
		}
	}
	/** Get the reference to the method in {@link com.jopdesign.sys.JVM} implementing the
	 *  given instruction
	 * @param ii the instruction
	 * @return the reference to the java implementation of the bytecode, or null if this is
	 * a native bytecode
	 */
	public MethodInfo getJavaImpl(ClassInfo ci, Instruction instr) {
		if(WCETInstruction.isInJava(getJOpCode(ci,instr))) {
			ClassInfo receiver = ai.cliMap.get(JVM_CLASS);
			String methodName = "f_"+instr.getName();
			try {
				return searchMethod(receiver,methodName);
			} catch (MethodNotFoundException e) {
				throw new AssertionError("Failed to find java implementation for: "+instr);
			}
		} else {
			return null;
		}
	}

	/*
	 * DEMO
	 * ~~~~
	 */

	public static String USAGE = 
		"Usage: java [-Dconfig=file://<config.props>] "+ 
		WcetAppInfo.class.getCanonicalName()+
		" [-outdir outdir] [-cp classpath] package.rootclass.rootmethod";

	/* small demo using the class loader */	
	public static void main(String[] argv) {

		try {
			String[] argvrest = Config.load(System.getProperty("config"), argv);
			Config config = Config.instance();
			config.setProjectName("typegraph");			
			if(argvrest.length == 1) {
				String target = argvrest[0];
				if(target.indexOf('(') > 0) target = target.substring(0,target.indexOf('('));
				config.setProperty(Config.APP_CLASS_NAME.getKey(), target.substring(0,target.lastIndexOf('.')));
				config.setProperty(Config.TARGET_METHOD.getKey(),argvrest[0]);
			}
			config.checkPresent(Config.REPORTDIR_PROPERTY);
			config.initializeReport();
		} catch(MissingConfigurationError e) {
			System.err.println(e);
			System.err.println(USAGE);
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		AppInfo ai = null;
		try {
			Config config = Config.instance();
			System.out.println("Classloader Demo: "+config.getAppClassName());
			String rootClass = config.getAppClassName();
			String rootPkg = rootClass.substring(0,rootClass.lastIndexOf("."));

			ai = Project.loadApp();
			WcetAppInfo wcetAi = new WcetAppInfo(ai);
			ClassInfo ci = wcetAi.getClassInfo(config.getAppClassName());
			System.out.println("Source file: "+ci.clazz.getSourceFileName());
			System.out.println("Root class: "+ci.clazz.toString());
			{ 
				System.out.println("Writing type graph to "+config.getOutFile("typegraph.png"));
				File dotFile = config.getOutFile("typegraph.dot");
				FileWriter dotWriter = new FileWriter(dotFile);
				wcetAi.getTypeGraph().exportDOT(dotWriter,rootPkg);			
				dotWriter.close();			
				InvokeDot.invokeDot(dotFile, config.getOutFile("typegraph.png"));
			}
			CallGraph cg = CallGraph.buildCallGraph(wcetAi, config.getMeasuredClass(), config.getMeasuredMethod());			
			{
				System.out.println("Writing call graph to "+config.getOutFile("callgraph.png"));
				File dotFile = config.getOutFile("callgraph.dot");
				FileWriter dotWriter = new FileWriter(dotFile);
				cg.exportDOT(dotWriter);			
				dotWriter.close();			
				InvokeDot.invokeDot(dotFile, config.getOutFile("callgraph.png"));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MethodNotFoundException e) {
			e.printStackTrace();
		}
	}

	public AppInfo getAppInfo() {
		return this.ai;
	}

	public void analyseFlowGraphs(Project p, List<MethodInfo> methodInfos) throws Exception {
		cfgsByIndex = new Vector<ControlFlowGraph>();
		cfgs = new Hashtable<MethodInfo, ControlFlowGraph>();
		for(int i = 0; i < methodInfos.size(); i++) {
			MethodInfo method = methodInfos.get(i);
			SortedMap<Integer,LoopBound> wcaMap = p.getAnnotations(method.getCli());
			assert(wcaMap != null);
			ControlFlowGraph fg;
			try {
				fg = new ControlFlowGraph(i,this,method);
			}  catch(BadGraphException e) {
				logger.error("Bad flow graph: "+e);
				throw e;
			}
			cfgsByIndex.add(fg);
			cfgs.put(method,fg);
		}
		for(ControlFlowGraph fg: cfgsByIndex) {
			try {
				fg.loadAnnotations(p);
				fg.resolveVirtualInvokes();
//				fg.insertSplitNodes();
			} catch (BadAnnotationException e) {
				logger.error("Bad annotation: "+e);
				throw e;
			}
		}
	}
	public boolean hasFlowGraph(MethodInfo mi) {
		return(cfgs.containsKey(mi));
	}
	public ControlFlowGraph getFlowGraph(int id) {
		return cfgsByIndex.get(id);
	}
	public ControlFlowGraph getFlowGraph(MethodInfo m) {
		if(cfgs.get(m) == null) {
			throw new AssertionError("No FlowGraph for "+m.getFQMethodName());
		}
		return cfgs.get(m);
	}

	public void setReceivers(
			Map<InstructionHandle, ContextMap<String, String>> receiverResults) {
		this.receiverAnalysis = receiverResults;
	}

}

