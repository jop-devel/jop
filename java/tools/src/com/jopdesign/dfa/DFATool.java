/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
 * Copyright (C) 2008, Wolfgang Puffitsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.dfa;

import com.jopdesign.common.AppEventHandler;
import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.EmptyTool;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MemberInfo.AccessType;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.HashedString;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.tools.ClinitOrder;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.Signature;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.analyses.ValueMapping;
import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.Flow;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.dfa.framework.Interpreter;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.UnconditionalBranch;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Tool for dataflow analysis */
public class DFATool extends EmptyTool<AppEventHandler> {

    private static final String prologueName = "<prologue>";
    private static final String prologueSig = "()V";

    private AppInfo appInfo;

    private List<InstructionHandle> statements;
    private Flow flow;
    private Map<InstructionHandle, ContextMap<CallString, Set<String>>> receivers;

    private LoopBounds loopBounds;

    public DFATool() {
        super("head");
        this.appInfo = AppInfo.getSingleton();
        this.statements = new LinkedList<InstructionHandle>();
        this.flow = new Flow();
        this.receivers = null;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    @Override
    public void registerOptions(OptionGroup options) {
    }

    @Override
    public void onSetupAppInfo(AppSetup setup, AppInfo appInfo) throws BadConfigurationException {
        load();
    }

    public void load() {

        // find ordering for class initializers
        ClinitOrder c = new ClinitOrder();
        appInfo.iterate(c);

        List<ClassInfo> order = c.findOrder();

        MethodInfo mainClass = appInfo.getMainMethod();

        // create prologue
        buildPrologue(mainClass, statements, flow, order);
        
        // Now we need to process all classes (for DFA's internal flow graph)
        for(ClassInfo ci : appInfo.getClassInfos()) {
        	for(MethodInfo mi : ci.getMethods()) {
        		if(mi.hasCode()) {
        			loadMethod(mi);
        		}
        	}
        }
    }

    private void loadMethod(MethodInfo method) {

    	MethodCode mcode = method.getCode();
    	InstructionList exit = new InstructionList(new NOP());
    	this.getStatements().add(exit.getStart());
    	for (Iterator<?> l = mcode.getInstructionList().iterator(); l.hasNext();) {
    		InstructionHandle handle = (InstructionHandle) l.next();
    		this.getStatements().add(handle);

    		Instruction instr = handle.getInstruction();
    		if (instr instanceof BranchInstruction) {
    			if (instr instanceof Select) {
    				Select s = (Select) instr;
    				InstructionHandle[] target = s.getTargets();
    				for (int j = 0; j < target.length; j++) {
    					this.getFlow().addEdge(new FlowEdge(handle, target[j],
    							FlowEdge.TRUE_EDGE));
    				}
    				this.getFlow().addEdge(new FlowEdge(handle, s.getTarget(),
    						FlowEdge.FALSE_EDGE));
    			} else {
    				BranchInstruction b = (BranchInstruction) instr;
    				this.getFlow().addEdge(new FlowEdge(handle, b.getTarget(),
    						FlowEdge.TRUE_EDGE));
    			}
    		}
    		if (handle.getNext() != null
    				&& !(instr instanceof UnconditionalBranch
    						|| instr instanceof Select || instr instanceof ReturnInstruction)) {
    			if (instr instanceof BranchInstruction) {
    				this.getFlow().addEdge(new FlowEdge(handle, handle.getNext(),
    						FlowEdge.FALSE_EDGE));
    			} else {
    				this.getFlow().addEdge(new FlowEdge(handle, handle.getNext(),
    						FlowEdge.NORMAL_EDGE));
    			}
    		}
    		if (instr instanceof ReturnInstruction) {
    			this.getFlow().addEdge(new FlowEdge(handle, exit.getStart(),
    					FlowEdge.NORMAL_EDGE));
    		}
    	}

    	// We do not really want to modify the REAL instruction list and append exit
    	// FIXME: is this necessary???

    	// InstructionList list = method.getInstructionList();
    	// list.append(exit);
    	// list.setPositions();
    }

    private void buildPrologue(MethodInfo mainMethod, List<InstructionHandle> statements, Flow flow, List<ClassInfo> clinits) {

        // we use a prologue sequence for startup
        InstructionList prologue = new InstructionList();
        ConstantPoolGen prologueCP = mainMethod.getConstantPoolGen();

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

        // Not in prologue anymore
        //        idx = prologueCP.addMethodref("java.lang.System", "<init>", "()V");
        //        instr = new INVOKESTATIC(idx);
        //        prologue.append(instr);

        // add class initializers
        for (ClassInfo clinit : clinits) {
            Signature cSig = appInfo.getClinitSignature(clinit.getClassName());
            idx = prologueCP.addMethodref(cSig.getClassName(), cSig.getMemberName(),
                    cSig.getMemberDescriptor().toString());
            instr = new INVOKESTATIC(idx);
            prologue.append(instr);
        }

        // add main method
        instr = new ACONST_NULL();
        prologue.append(instr);
        idx = prologueCP.addMethodref(mainMethod.getClassName(), mainMethod.getShortName(),
                mainMethod.getDescriptor().toString());
        instr = new INVOKESTATIC(idx);
        prologue.append(instr);

//		// invoke startMission() to ensure analysis of threads
//		idx = prologueCP.addMethodref("joprt.RtThread", "startMission", "()V");
//		instr = new INVOKESTATIC(idx);
//		prologue.append(instr);

        instr = new NOP();
        prologue.append(instr);

        prologue.setPositions(true);

// 		System.out.println(prologue);

        // add prologue to program structure
        for (Iterator l = prologue.iterator(); l.hasNext();) {
            InstructionHandle handle = (InstructionHandle) l.next();
            statements.add(handle);
            if (handle.getInstruction() instanceof GOTO) {
                GOTO g = (GOTO) handle.getInstruction();
                flow.addEdge(new FlowEdge(handle, g.getTarget(), FlowEdge.NORMAL_EDGE));
            } else if (handle.getNext() != null) {
                flow.addEdge(new FlowEdge(handle, handle.getNext(), FlowEdge.NORMAL_EDGE));
            }
        }

        Signature pSig = new Signature(prologueName, Descriptor.parse(prologueSig));
        MethodInfo mi = mainMethod.getClassInfo().createMethod(pSig, null, prologue);

        mi.setAccessType(AccessType.ACC_PRIVATE);
    }

    @SuppressWarnings({"unchecked"})
    public Map runAnalysis(Analysis analysis) {

        Interpreter interpreter = new Interpreter(analysis, this);

        try {
            MethodInfo main = appInfo.getMainMethod();
            MethodInfo prologue = main.getClassInfo().getMethodInfo(prologueName + prologueSig);

            Context context = new Context();
            context.stackPtr = 0;
            context.syncLevel = 0;
            context.constPool = prologue.getConstantPoolGen();
            context.method = prologue.getMethodRef();

            analysis.initialize(main, context);

            InstructionHandle entry = prologue.getCode().getInstructionList().getStart();
            interpreter.interpret(context, entry, new HashMap(), true);
        } catch (Throwable thr) {
            thr.printStackTrace();
        }

        return analysis.getResult();
    }

    public <K, V>
    Map runLocalAnalysis(Analysis<K, V> analysis, MethodInfo start) {

        Interpreter<K, V> interpreter = new Interpreter<K, V>(analysis, this);

        try {
            if (start == null) throw new AssertionError("No such method: " + start);
            Context context = new Context();
            context.stackPtr = start.getCode().getMaxLocals();
            context.constPool = start.getClassInfo().getConstantPoolGen();
            context.method = start.getMethodRef();

            analysis.initialize(start, context);
            InstructionHandle entry = start.getCode().getInstructionList().getStart();
            interpreter.interpret(context, entry, new HashMap<InstructionHandle, ContextMap<K, V>>(), true);
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

    public Set<String> getReceivers(InstructionHandle stmt, CallString cs) {
        ContextMap<CallString, Set<String>> map = receivers.get(stmt);
        if (map == null) {
            return null;
        }
        Set<String> retval = new HashSet<String>();
        for (CallString c : map.keySet()) {
            if (c.hasSuffix(cs)) {
                retval.addAll(map.get(c));
            }
        }
        return retval;
    }

    public void setReceivers(Map<InstructionHandle, ContextMap<CallString, Set<String>>> receivers) {
        this.receivers = receivers;
    }

    public LoopBounds getLoopBounds() {
        return loopBounds;
    }

    public void setLoopBounds(LoopBounds lb) {
        this.loopBounds = lb;
    }

    /**
     * Helper method to find the actually invoked method given the
     * dynamic type and the method signature
     *
     * @param recvStr the dynamic type of the receiver
     * @param sigstr the signature (without class) of the method.
     * @return the actually invoked method, or {@code null} if not found
     */
    public MethodInfo getMethod(String recvStr, String sigStr) {
    	ClassInfo receiver = appInfo.getClassInfo(recvStr);
    	Signature methodSig = Signature.parse(sigStr, true);
    	return receiver.getMethodInfoInherited(methodSig, true);
    }

	/**
     * Helper method to find the actually invoked method given the
     * dynamic type and the method signature
     * 
	 * @param signature FQ signature of the method
	 * @return the invoked method, or {@code null} if not found
	 */
	public MethodInfo getMethod(String signature) {
		Signature sig = Signature.parse(signature);
    	ClassInfo receiver = appInfo.getClassInfo(sig.getClassName());
    	return receiver.getMethodInfoInherited(sig, true);
	}
	
    public ClassInfo classForField(String fieldName) {
        Signature s = Signature.parse(fieldName, true);
        if(! getAppInfo().hasClassInfo(s.getClassName())) {
        	Logger.getLogger(this.getClass()).debug("Unknown class as potential receiver for putfield operation " + s.toString(true));
        	return null;
        }

        ClassInfo cls = getAppInfo().getClassInfo(s.getClassName());
        if (cls == null) {
            return null;
        }
        // TODO maybe we *do* want to check access here...
        FieldInfo field = cls.getFieldInfoInherited(s.getMemberName(), false);
        return field != null ? field.getClassInfo() : null;
    }

    @SuppressWarnings("unchecked")
    public String dumpDFA(MethodInfo method) {
        if (getLoopBounds() == null) return "n/a";
        if (method.isAbstract()) {
            return "n/a";
        }

        Map<InstructionHandle, ContextMap<List<HashedString>, Pair<ValueMapping, ValueMapping>>> results = getLoopBounds().getResult();
        if (results == null) return "n/a";
        StringBuilder s = new StringBuilder();

        ControlFlowGraph cfg = method.getCode().getControlFlowGraph(false);
        for (CFGNode n : cfg.getGraph().vertexSet()) {
            if (n.getBasicBlock() == null) continue;
            ContextMap<List<HashedString>, Pair<ValueMapping, ValueMapping>> r = results.get(n.getBasicBlock().getLastInstruction());
            if (r != null) {
                s.append(n);
                s.append(" :: ");
                s.append(r);
                s.append("\n");
            }
        }
        return s.toString();
    }

    public boolean containsField(String fieldName) {
        return classForField(fieldName) != null;
    }



}
