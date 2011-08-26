/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.wcet.allocation;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.dfa.analyses.Interval;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.annotations.SourceAnnotations;
import com.jopdesign.wcet.jop.CacheModel;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.NoMethodCache;
import com.jopdesign.wcet.jop.ObjectCache;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AllocationWcetModel implements WCETProcessorModel {

	protected WCETTool project;

	public AllocationWcetModel(WCETTool p) {
		project = p;
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
	
	public long getExecutionTime(ExecutionContext context, InstructionHandle ih) {

		int opcode = ih.getInstruction().getOpcode();
		MethodInfo mCtx = context.getMethodInfo();
		if (opcode == Constants.NEW) {
			NEW insn = (NEW)ih.getInstruction();
			ObjectType type = insn.getLoadClassType(mCtx.getConstantPoolGen());
			return computeObjectSize(getFieldSize(getObjectFields(type.getClassName())));
		} else if (opcode == Constants.NEWARRAY || opcode == Constants.ANEWARRAY) {
			int typeSize = 1;
			if (ih.getInstruction() instanceof NEWARRAY) {
				NEWARRAY insn = (NEWARRAY)ih.getInstruction();
				if (insn.getTypecode() == Constants.T_DOUBLE
						|| insn.getTypecode() == Constants.T_LONG) {
						typeSize = 2;
				}
			}
			return computeArraySize(getArrayBound(context, ih, 0)*typeSize);
		} else if (opcode == Constants.MULTIANEWARRAY) {
			MULTIANEWARRAY insn = (MULTIANEWARRAY)ih.getInstruction();
			int dim = insn.getDimensions();
			long count = 1;
			long size = 0;
			for (int i = dim-1; i >= 0; i--) {
				long bound = getArrayBound(context, ih, i);
				size += count * computeArraySize(bound);
				count *= bound;
			}
			return size;
		} else {
			return 0;
		}
	}

	private long getArrayBound(ExecutionContext context, InstructionHandle ih, int index) {

		int srcLine = context.getMethodInfo().getCode().getLineNumberTable().getSourceLine(ih.getPosition());

		// get annotated size
		LoopBound annotated = null;
		try {
			SourceAnnotations annots = project.getAnnotations(context.getMethodInfo().getClassInfo());
			annotated = annots.annotationsForLine(srcLine);
			if (annotated == null) {
				WCETTool.logger.info("No annotated bound for array at " + context + ":" + srcLine);
			}
		} catch (Exception exc) {
			// TODO: anything else to do?
			WCETTool.logger.warn("Problem reading annotated bound for array at " + context + ":" + srcLine, exc);
		}

		// get analyzed size
		Interval analyzed = null;
		Interval [] sizes = null;
		if (project.getDfaLoopBounds() != null) {
			sizes = project.getDfaLoopBounds().getArraySizes(ih, context.getCallString());
		}
		if (sizes == null) {
			WCETTool.logger.info("No DFA available for array at " + context + ":" + srcLine);
		} else {
			analyzed = sizes[index];
			if (analyzed == null) {
				WCETTool.logger.info("No DFA bound for array at " + context + ":" + srcLine);
			}
		}

		// compute which bound to use
		if (analyzed != null && analyzed.hasUb()) {
			if (annotated != null) {
				if (annotated.getUpperBound(context) > analyzed.getUb()) {
					WCETTool.logger.warn("DFA bound smaller than annotated bound for array at " + context + ":" + srcLine);
				}
				if (annotated.getUpperBound(context) < analyzed.getUb()) {
					WCETTool.logger.warn("DFA bound larger than annotated bound for array at " + context + ":" + srcLine);
				}
				if (annotated.getUpperBound(context) == analyzed.getUb()) {
					WCETTool.logger.info("DFA bound equals annotated bound for array at " + context + ":" + srcLine);
				}
				return Math.max(annotated.getUpperBound(context), analyzed.getUb());
			} else {
				return analyzed.getUb();
			}
		} else {
			if (annotated != null) {
				return annotated.getUpperBound(context);
			} else {
				WCETTool.logger.error("Cannot determine cost of unbounded array " +
						             context.getMethodInfo().getFQMethodName() +
									 ":" + srcLine +
									 ".\nApproximating with 1024 words, but result is not safe anymore.");
				return 1024;
			}
		}
	}

	public abstract long computeObjectSize(long raw);

	public abstract long computeArraySize(long raw);

	public List<Type> getObjectFields(String className) {
		List<Type> l = new LinkedList<Type>();

		ClassInfo cli = project.getAppInfo().getClassInfo(className);

		if (cli.getSuperClassName() != null) {
			l.addAll(getObjectFields(cli.getSuperClassName()));
		}

		for (FieldInfo f : cli.getFields()) {
			if (!f.isStatic()) {
				l.add(f.getType());
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

	@Override
    public MethodCache getMethodCache() {
    	return new NoMethodCache();
	}

	@Override
	public ObjectCache getObjectCache() {
		return null;
	}

	@Override
	public List<CacheModel> getCaches() {
		return Collections.emptyList();
	}
	

}
