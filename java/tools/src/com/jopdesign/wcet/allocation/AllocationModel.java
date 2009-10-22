package com.jopdesign.wcet.allocation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.Interval;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.HashedString;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.NoMethodCache;

public class AllocationModel implements ProcessorModel {

	public static final String JOP_NATIVE = "com.jopdesign.sys.Native";
	private final MethodCache NO_METHOD_CACHE;
	private Map<InstructionHandle, ContextMap<List<HashedString>, Interval[]>> sizes;
	protected Project project;
	
	public AllocationModel(Project p) {
		project = p;
		NO_METHOD_CACHE = new NoMethodCache(p);		
	}

	public long basicBlockWCET(BasicBlock bb) {
		int size = 0;
		MethodInfo ctx = bb.getMethodInfo();
		for(InstructionHandle ih : bb.getInstructions()) {
			size += getExecutionTime(ctx, ih);
		}
		return size;
	}

	public long getInvokeReturnMissCost(ControlFlowGraph invokerFlowGraph, ControlFlowGraph receiverFlowGraph) {
		return 0;
	}

	public MethodInfo getJavaImplementation(WcetAppInfo ai, MethodInfo ctx, Instruction instr) {
		throw new AssertionError("allocation model model does not (yet) support java implemented methods");
	}

	public List<String> getJVMClasses() {
		return new Vector<String>();
	}

	public long getMethodCacheMissPenalty(int numberOfWords, boolean loadOnInvoke) {
		return 0;
	}

	public MethodCache getMethodCache() {
		return NO_METHOD_CACHE;
	}


	public int getNativeOpCode(MethodInfo context, Instruction instr) {
		if(isSpecialInvoke(context,instr)) {
			INVOKESTATIC isi = (INVOKESTATIC) instr;
			String methodName = isi.getMethodName(context.getConstantPoolGen());
			return JopInstr.getNative(methodName);
		} else {
			return instr.getOpcode();
		}
	}

	public int getNumberOfBytes(MethodInfo context, Instruction instruction) {
		int opCode = getNativeOpCode(context, instruction);
		// FIXME jamuth specific instructions ?
		if(opCode >= 0) return JopInstr.len(opCode);
		else throw new AssertionError("Invalid opcode: "+context+" : "+instruction);
	}

	public boolean hasMethodCache() {
		return false;
	}

	public boolean isImplementedInJava(Instruction i) {
		return false;
	}

	public boolean isSpecialInvoke(MethodInfo context, Instruction i) {
		if(! (i instanceof INVOKESTATIC)) return false;
		INVOKESTATIC isi = (INVOKESTATIC) i;
		ReferenceType refTy = isi.getReferenceType(context.getConstantPoolGen());
		if(refTy instanceof ObjectType){
			ObjectType objTy = (ObjectType) refTy;
			String className = objTy.getClassName();
			return (className.equals(JOP_NATIVE));
		} else {
			return false;
		}
	}

	public int getExecutionTime(MethodInfo context, InstructionHandle ih) {
	
		int opcode = ih.getInstruction().getOpcode();
	
		if (opcode == Constants.NEW) {
			NEW insn = (NEW)ih.getInstruction();
			ObjectType type = insn.getLoadClassType(context.getConstantPoolGen());
			return computeObjectSize(getFieldSize(getObjectFields(type.getClassName())));			
		} else if (opcode == Constants.NEWARRAY || opcode == Constants.ANEWARRAY) {	
			return computeArraySize(getArrayBound(context, ih, 0));
		} else if (opcode == Constants.MULTIANEWARRAY) {
			MULTIANEWARRAY insn = (MULTIANEWARRAY)ih.getInstruction();
			int dim = insn.getDimensions();
			int count = 1;
			int size = 0;
			for (int i = dim-1; i >= 0; i--) {
				int bound = getArrayBound(context, ih, i);
				size += count * computeArraySize(bound);
				count *= bound;
			}
			return size;
		} else {
			return 0;
		}
	}

	private int getArrayBound(MethodInfo context, InstructionHandle ih, int index) {
		int srcLine = context.getMethod().getLineNumberTable().getSourceLine(ih.getPosition());
		
		// get annotated size
		LoopBound annotated = null;
		try {
			Map<Integer, LoopBound> annots = project.getAnnotations(context.getCli());
			annotated = annots.get(new Integer(srcLine));
			if (annotated == null) {
				Project.logger.info("No annotated bound for array at " + context + ":" + srcLine);
			}
		} catch (Exception exc) {
			// TODO: anything else to do?
			Project.logger.warn("Problem reading annotated bound for array at " + context + ":" + srcLine);				
		}
		
		// get analyzed size
		Interval analyzed = null;
		if (sizes == null && project.getDfaLoopBounds() != null) {
			sizes = project.getDfaLoopBounds().getArraySizes();
		}
		if (sizes == null) {
			Project.logger.info("No DFA available for array at " + context + ":" + srcLine);
		} else {
			// TODO: do we have proper callstrings?
			List<HashedString> callString = new LinkedList<HashedString>();			
			ContextMap<List<HashedString>, Interval[]> t = sizes.get(ih);
			if (t == null) {
				Project.logger.info("No DFA bound for array at " + context + ":" + srcLine);					
			} else {
				analyzed = t.get(callString)[index];
				if (analyzed == null) {
					Project.logger.info("No DFA bound for array at " + context + ":" + srcLine);
				}
			}
		}
					
		// compute which bound to use
		if (analyzed != null && analyzed.hasUb()) {
			if (annotated != null) {
				if (annotated.getUpperBound() > analyzed.getUb()) {
					Project.logger.warn("DFA bound smaller than annotated bound for array at " + context + ":" + srcLine);
				}
				if (annotated.getUpperBound() < analyzed.getUb()) {
					Project.logger.warn("DFA bound larger than annotated bound for array at " + context + ":" + srcLine);
				}
				if (annotated.getUpperBound() == analyzed.getUb()) {
					Project.logger.info("DFA bound equals annotated bound for array at " + context + ":" + srcLine);
				}
				return Math.max(annotated.getUpperBound(), analyzed.getUb());
			} else {				
				return analyzed.getUb();
			}
		} else {
			if (annotated != null) {
				return annotated.getUpperBound();
			} else {
				Project.logger.error("Cannot determine cost of unbounded array " + context + ":" + srcLine +
									 ".\nApproximating with 4096 words, but result is not safe anymore.");
				return 4096;
			}
		}
	}

	public int computeObjectSize(int raw) {
		return 1;
	}

	public int computeArraySize(int raw) {
		return 1;
	}
	
	public List<Type> getObjectFields(String className) {
		List<Type> l = new LinkedList<Type>();
	
		ClassInfo cli = project.getWcetAppInfo().getClassInfo(className);
	
		if (cli.superClass != null) {
			l.addAll(getObjectFields(cli.superClass.toString()));
		}
	
		Field [] f = cli.clazz.getFields();		
		for (int i = 0; i < f.length; i++) {
			l.add(f[i].getType());
		}		
	
		return l;
	}

	public int getFieldSize(List<Type> fields) {
		int size = 0;
		for (Type t : fields) {
			size += t.getSize();
		}
		return size;
	}

}
