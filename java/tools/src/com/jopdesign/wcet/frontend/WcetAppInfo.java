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
package com.jopdesign.wcet.frontend;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Vector;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.log4j.Logger;

import com.jopdesign.build.AppInfo;
import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.Project.AnalysisError;
import com.jopdesign.wcet.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet.graphutils.TopOrder.BadGraphException;

/**
 * AppInfo subclass for the WCET analysis.
 * Provides a TypeGraph.
 *
 * @author Benedikt Huber, benedikt.huber@gmail.com
 */
public class WcetAppInfo  {
	private static final long serialVersionUID = 3L;
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
	private TypeGraph typeGraph;
	private AppInfo ai;
	private Map<MethodInfo, ControlFlowGraph> cfgs;
	private List<ControlFlowGraph> cfgsByIndex;
	private Map<InstructionHandle, ContextMap<String, String>> receiverAnalysis = null;
	private ProcessorModel processor;
	private Project project;

	public WcetAppInfo(Project p, com.jopdesign.build.AppInfo ai, ProcessorModel processor) {
		this.project = p;
		this.ai = ai;
		this.processor = processor;
		this.typeGraph = new TypeGraph(this);
		cfgsByIndex = new Vector<ControlFlowGraph>();
		cfgs = new Hashtable<MethodInfo, ControlFlowGraph>();
	}

	/**
	 * @return A mapping from the name of a loaded class to {@link ClassInfo}.
	 */
	public Map<String, ? extends ClassInfo> getCliMap() {
		return ai.cliMap;
	}

	public Project getProject() {
		return project;
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
	 * @throws MethodNotFoundException if the method couldn't be found or is ambiguous
	 */
	public MethodInfo searchMethod(String className, String methodName) throws MethodNotFoundException {
		ClassInfo cli = getCliMap().get(className);
		if(cli == null) throw new MethodNotFoundException("The class "+className+" couldn't be found");
		return searchMethod(cli,methodName);
	}

	public MethodInfo searchMethod(ClassInfo cli, String methodName) throws MethodNotFoundException {
		MethodInfo mi = null;
		if(methodName.indexOf("(") > 0) {
			mi = cli.getMethodInfo(methodName);
			if(mi == null) {
				throw new MethodNotFoundException("The fully qualified method '"+methodName+"' could not be found in "+
						cli.getMethodInfoMap().keySet());
			}
		} else {
			for(MethodInfo candidate : cli.getMethods()) {
				if(methodName.equals(candidate.getMethod().getName())) {
					if(mi == null) {
						mi = candidate;
					} else {
						throw new MethodNotFoundException("The method name "+methodName+" is ambiguous."+
														  "Both "+mi.methodId+" and "+candidate.methodId+" match");
					}
				}
			}
			if(mi == null) {
				Vector<String> candidates = new Vector<String>();
				for(MethodInfo candidate : cli.getMethods()) {
					candidates.add(candidate.getMethod().getName());
				}
				throw new MethodNotFoundException("The method "+methodName+"could not be found in "+cli.toString()+". "+
												  "Candidates: "+candidates);
			}
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
		ClassInfo refCi;
		ConstantPoolGen cpg = new ConstantPoolGen(invokerCi.clazz.getConstantPool());
		String classname = instr.getClassName(cpg );
		String methodname = instr.getMethodName(cpg) + instr.getSignature(cpg);
		refCi = getClassInfo(classname);
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
				try {
					if(superRec.clazz.getSuperClass() == null) superRec = null;
					else superRec = superRec.superClass;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new Error();
				}
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
		staticImpls = dfaReceivers(ih, staticImpls);
		return staticImpls;
	}
	// TODO: rather slow, for debugging purposes
	private List<MethodInfo> dfaReceivers(InstructionHandle ih, List<MethodInfo> staticImpls) {
		if(this.receiverAnalysis != null && receiverAnalysis.containsKey(ih)) {
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
			return dynImpls;
		} else {
			return staticImpls;
		}
	}
	/* helper to avoid code dupl */
	private void tryAddImpl(List<MethodInfo> ms, MethodInfo m) {
		if(m != null) {
			if(! m.getMethod().isAbstract() && ! m.getMethod().isInterface()) {
				ms.add(m);
			}
		}
	}

	public AppInfo getAppInfo() {
		return this.ai;
	}
	public ControlFlowGraph getFlowGraph(int id) {
		return cfgsByIndex.get(id);
	}
	public ControlFlowGraph getFlowGraph(MethodInfo m) {
		if(cfgs.get(m) == null) {
			try {
				loadFlowGraph(m);
			} catch (BadAnnotationException e) {
				throw new AnalysisError("Bad Flow Fact Annotation: "+e.getMessage(),e);
			} catch (IOException e) {
				throw new AnalysisError("IO Exception",e);
			} catch (BadGraphException e) {
				throw new AnalysisError("Bad Flow Graph: "+e.getMessage(),e);
			}
		}
		return cfgs.get(m);
	}
	private ControlFlowGraph loadFlowGraph(MethodInfo method) throws BadAnnotationException, IOException, BadGraphException {
		SortedMap<Integer,LoopBound> wcaMap = project.getAnnotations(method.getCli());
		assert(wcaMap != null);
		if(method.getCode() == null) {
			throw new BadGraphException("No implementation of "+method.getFQMethodName()+" available for the target processor");
		}
		ControlFlowGraph fg;
		try {
			fg = new ControlFlowGraph(cfgsByIndex.size(),project,method);
			fg.loadAnnotations(project);
			fg.resolveVirtualInvokes();
//			fg.insertSplitNodes();
//			fg.insertSummaryNodes();
			fg.insertReturnNodes();
			fg.insertContinueLoopNodes();
			cfgsByIndex.add(fg);
			cfgs.put(method,fg);
			return fg;
		}  catch(BadGraphException e) {
			logger.error("Bad flow graph: "+e);
			throw e;
		}
	}
	public void setReceivers(
			Map<InstructionHandle, ContextMap<String, String>> receiverResults) {
		this.receiverAnalysis = receiverResults;
	}

	public ProcessorModel getProcessorModel() {
		return this.processor;
	}

	public MethodInfo getJavaImplementation(MethodInfo ctx, Instruction lastInstr) {
		return this.processor.getJavaImplementation(this, ctx, lastInstr);
	}

}

