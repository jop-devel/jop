package com.jopdesign.wcet.allocation;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

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
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.HashedString;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.ExecutionContext;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.NoMethodCache;

public class AllocationModel implements ProcessorModel {

	public static final String JOP_NATIVE = "com.jopdesign.sys.Native";
	private final MethodCache NO_METHOD_CACHE;
	private Map<InstructionHandle, ContextMap<CallString, Interval[]>> sizes;
	protected Project project;

	public AllocationModel(Project p) {
		project = p;
		NO_METHOD_CACHE = new NoMethodCache(p);
	}
	
	public String getName() {
		return "allocation";
	}

	public long basicBlockWCET(ExecutionContext ctx, BasicBlock bb) {
		int size = 0;
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

	public int getExecutionTime(ExecutionContext context, InstructionHandle ih) {

		int opcode = ih.getInstruction().getOpcode();
		MethodInfo mCtx = context.getMethodInfo();
		if (opcode == Constants.NEW) {
			NEW insn = (NEW)ih.getInstruction();
			ObjectType type = insn.getLoadClassType(mCtx.getConstantPoolGen());
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

	private int getArrayBound(ExecutionContext context, InstructionHandle ih, int index) {
		int srcLine = context.getMethodInfo().getMethod().getLineNumberTable().getSourceLine(ih.getPosition());

		// get annotated size
		LoopBound annotated = null;
		try {
			Map<Integer, LoopBound> annots = project.getAnnotations(context.getMethodInfo().getCli());
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
			ContextMap<CallString, Interval[]> t = sizes.get(ih);
			CallString callString = context.getCallString();
			if (t == null) {
				Project.logger.info("No DFA bound for array at " + context + ":" + srcLine);
			} else {
				Interval[] analysisResults = getEntryBySuffix(t, callString);
				if(analysisResults == null) {
					Project.logger.error("No DFA results matching callstring " + context.getCallString());
				} else {
					analyzed = analysisResults[index];
					if (analyzed == null) {
						Project.logger.info("No DFA bound for array at " + context + ":" + srcLine);
					}
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

	/** TODO: This operation suggests a tree like structure of the ContextMap.
	 * Maybe this would be an interesting implementation option ?
	 *  FIXME: This has a horrible complexity ( O(n*|callstring|) ) at the moment.
	 * @param t
	 * @param callStringSuffix a suffix of the call string
	 * @return the join of all intervals matching the suffix
	 */
	private Interval[] getEntryBySuffix(ContextMap<CallString, Interval[]> t, CallString cs) {
		Vector<Interval[]> intervals = new Vector<Interval[]>();
		for(Entry<CallString, Interval[]> e : t.entrySet()) {
			CallString callstring = e.getKey();
			if(isSuffix(callstring.asList(),cs.asList())) {
				//System.out.println("Matches "+cs+": "+callstring);
				intervals.add(e.getValue());
			}
		}
		if(intervals.size() == 0) return null;
		Interval[] head = intervals.get(0);
		if(intervals.size() == 1) return head;

		Interval[] merged = new Interval[head.length];
		for(int i = 0; i < head.length; i++) {
			merged[i] = new Interval(head[i]);
			for(int j = 1; j < intervals.size(); j++)
			{
				merged[i].constrain(intervals.get(j)[i]);
			}
		}
		return merged;
	}

	private<T> boolean isSuffix(List<T> list,
							    List<T> cSuffix) {
		int lN = list.size();
		int sN = cSuffix.size();
		if(lN < sN) return false;
		List<T> listSuffix = list.subList(lN - sN, lN);
		return cSuffix.equals(listSuffix);
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
			if (!f[i].isStatic()) {
				l.add(f[i].getType());
			}
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
