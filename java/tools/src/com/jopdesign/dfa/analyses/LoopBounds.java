/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
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

package com.jopdesign.dfa.analyses;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.CallString.CallStringSerialization;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.AnalysisResultSerialization;
import com.jopdesign.dfa.framework.AnalysisResultSerialization.ResultFormatter;
import com.jopdesign.dfa.framework.AnalysisResultSerialization.Serializer;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.dfa.framework.FlowEdge.SerializedFlowEdge;
import com.jopdesign.dfa.framework.Interpreter;
import com.jopdesign.dfa.framework.MethodHelper;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoopBounds implements Analysis<CallString, Map<Location, ValueMapping>> {

    private final int callStringLength;

    public static final String NAME = "LoopBounds";
    private static final Logger logger = Logger.getLogger(DFATool.LOG_DFA_ANALYSES + "." + NAME);

    // used to print messages only once if trace is not enabled
    private Set<String> noReceiverWarnings = new LinkedHashSet<String>();
    private Set<String> unknownReceiverWarnings = new LinkedHashSet<String>();

    public LoopBounds(int callStringLength) {
        this.callStringLength = callStringLength;
    }

	public String getId() {
		return NAME + "-" + callStringLength;
	}

    public ContextMap<CallString, Map<Location, ValueMapping>> bottom() {
        return null;
    }

    public ContextMap<CallString, Map<Location, ValueMapping>> initial(InstructionHandle stmt) {
        ContextMap<CallString, Map<Location, ValueMapping>> retval = new ContextMap<CallString, Map<Location, ValueMapping>>(new Context(), new LinkedHashMap<CallString, Map<Location, ValueMapping>>());

        CallString l = CallString.EMPTY;
        Map<Location, ValueMapping> init = new LinkedHashMap<Location, ValueMapping>();

        ValueMapping value;

        value = new ValueMapping();
        value.assigned.setLb(0);
        value.assigned.setUb(16);
        init.put(new Location("com.jopdesign.io.SysDevice.nrCpu"), value);

        retval.put(l, init);
        return retval;
    }

    private Map<InstructionHandle, ContextMap<CallString, Pair<ValueMapping, ValueMapping>>> bounds = new LinkedHashMap<InstructionHandle, ContextMap<CallString, Pair<ValueMapping, ValueMapping>>>();
    private Map<InstructionHandle, ContextMap<CallString, Integer>> scopes = new LinkedHashMap<InstructionHandle, ContextMap<CallString, Integer>>();
    private Map<InstructionHandle, ContextMap<CallString, Interval[]>> sizes = new LinkedHashMap<InstructionHandle, ContextMap<CallString, Interval[]>>();
    private Map<InstructionHandle, ContextMap<CallString, Set<FlowEdge>>> infeasibles = new LinkedHashMap<InstructionHandle, ContextMap<CallString, Set<FlowEdge>>>();


    private Map<InstructionHandle, ContextMap<CallString, Interval>> arrayIndices =
            new LinkedHashMap<InstructionHandle, ContextMap<CallString, Interval>>();

    public void initialize(MethodInfo sig, Context context) {
    }

    public ContextMap<CallString, Map<Location, ValueMapping>> join(
            ContextMap<CallString, Map<Location, ValueMapping>> s1,
            ContextMap<CallString, Map<Location, ValueMapping>> s2) {

        if (s1 == null) {
            return new ContextMap<CallString, Map<Location, ValueMapping>>(s2);
        }

        if (s2 == null) {
            return new ContextMap<CallString, Map<Location, ValueMapping>>(s1);
        }

        ContextMap<CallString, Map<Location, ValueMapping>> result = new ContextMap<CallString, Map<Location, ValueMapping>>(new Context(s1.getContext()), new LinkedHashMap<CallString, Map<Location, ValueMapping>>());
        result.putAll(s1);
        result.putAll(s2);

        Map<Location, ValueMapping> a = s1.get(s2.getContext().callString);
        Map<Location, ValueMapping> b = s2.get(s2.getContext().callString);

        if (a != null || b != null) {
            if (a == null) {
                a = new LinkedHashMap<Location, ValueMapping>();
            }
            if (b == null) {
                b = new LinkedHashMap<Location, ValueMapping>();
            }

            Map<Location, ValueMapping> merged = new LinkedHashMap<Location, ValueMapping>(a);

            for (Location l : b.keySet()) {
                ValueMapping x = a.get(l);
                ValueMapping y = b.get(l);
                if (x != null) {
                    if (!x.equals(y)) {
                        ValueMapping r = new ValueMapping(x, true);
                        r.join(y);
                        merged.put(l, r);
                    } else {
                        merged.put(l, x);
                    }
                } else {
                    merged.put(l, y);
                }
            }

            result.put(s2.getContext().callString, merged);
        }

        if (result.getContext().stackPtr < 0) {
            result.getContext().stackPtr = s2.getContext().stackPtr;
        }
        if (result.getContext().syncLevel < 0) {
            result.getContext().syncLevel = s2.getContext().syncLevel;
        }
        result.getContext().threaded = Context.isThreaded();

        return result;
    }

    public boolean compare(ContextMap<CallString, Map<Location, ValueMapping>> s1, ContextMap<CallString, Map<Location, ValueMapping>> s2) {

        if (s1 == null || s2 == null) {
            return false;
        }

        if (!s1.getContext().equals(s2.getContext())) {
            return false;
        } else {

            Map<Location, ValueMapping> a = s1.get(s1.getContext().callString);
            Map<Location, ValueMapping> b = s2.get(s1.getContext().callString);

            if (a == null && b == null) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }

            for (Location l : a.keySet()) {
                if (!b.containsKey(l) || !a.get(l).compare(b.get(l))) {
                    return false;
                }
            }

            return true;
        }
    }

    public ContextMap<CallString, Map<Location, ValueMapping>> transfer(
            InstructionHandle stmt, FlowEdge edge,
            ContextMap<CallString, Map<Location, ValueMapping>> input,
            Interpreter<CallString, Map<Location, ValueMapping>> interpreter,
            Map<InstructionHandle, ContextMap<CallString, Map<Location, ValueMapping>>> state) {

        Context context = new Context(input.getContext());
        Map<Location, ValueMapping> in = (Map<Location, ValueMapping>) input.get(context.callString);

        ContextMap<CallString, Map<Location, ValueMapping>> retval = new ContextMap<CallString, Map<Location, ValueMapping>>(context, new LinkedHashMap<CallString, Map<Location, ValueMapping>>());

        Instruction instruction = stmt.getInstruction();

//		if (in == null) {
//			System.out.print(";; ");
//		} else if (in.isEmpty()) {
//			System.out.print("// ");
//		}
//		System.out.println(context.callString.asList()+"/"+context.method+": "+stmt);
//		if (in != null) {
//			System.out.println(in.get(new Location("java.io.JOPReader.lines")));
//		}

        // shortcut for infeasible paths
        if (in == null) {
            context.stackPtr += instruction.produceStack(context.constPool()) - instruction.consumeStack(context.constPool());
            return retval;
        }

        Map<Location, ValueMapping> result = new LinkedHashMap<Location, ValueMapping>();
        retval.put(context.callString, result);

//		System.out.println(context.method+": "+stmt);
//		System.out.println("###"+context.stackPtr+" + "+instruction.produceStack(context.constPool)+" - "+instruction.consumeStack(context.constPool));		
//		System.out.println(stmt+" "+(edge.getType() == FlowEdge.TRUE_EDGE ? "TRUE" : (edge.getType() == FlowEdge.FALSE_EDGE) ? "FALSE" : "NORMAL")+" "+edge);
//		System.out.println(context.callString+"/"+context.method);
//		System.out.print(stmt.getInstruction()+":\t{ ");
//		System.out.print(input.get(context.callString));
//		System.out.println("}");

// 		for (int i = 0; i < stmt.getTargeters().length; i++) {
// 			InstructionTargeter targeter = stmt.getTargeters()[i];
// 			if (targeter instanceof BranchInstruction) {
// 				checkScope(context, stmt);
// 				break;
// 			}
// 		}

        switch (instruction.getOpcode()) {

            case Constants.ICONST_M1:
            case Constants.ICONST_0:
            case Constants.ICONST_1:
            case Constants.ICONST_2:
            case Constants.ICONST_3:
            case Constants.ICONST_4:
            case Constants.ICONST_5:
            case Constants.BIPUSH:
            case Constants.SIPUSH: {
                ConstantPushInstruction instr = (ConstantPushInstruction) instruction;
                result = new LinkedHashMap<Location, ValueMapping>(in);
                retval.put(context.callString, result);
                int value = instr.getValue().intValue();
                result.put(new Location(context.stackPtr), new ValueMapping(value));
            }
            break;

            case Constants.ACONST_NULL:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.LDC:
            case Constants.LDC_W: {
                LDC instr = (LDC) instruction;
                result = new LinkedHashMap<Location, ValueMapping>(in);
                retval.put(context.callString, result);
                Type type = instr.getType(context.constPool());
                if (type.equals(Type.INT)) {
                    Integer value = (Integer) instr.getValue(context.constPool());
                    result.put(new Location(context.stackPtr), new ValueMapping(value));
                } else if (type.equals(Type.STRING)) {
                    String value = (String) instr.getValue(context.constPool());
                    String name = "char[]";
                    name += "@" + context.method() + ":" + stmt.getPosition();
                    result.put(new Location(name + ".length"), new ValueMapping(value.length()));
//				System.out.println(name+": \""+value+"\"");				
                }
            }
            break;

            case Constants.LDC2_W:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.ISTORE_0:
            case Constants.ISTORE_1:
            case Constants.ISTORE_2:
            case Constants.ISTORE_3:
            case Constants.ISTORE: {
                StoreInstruction instr = (StoreInstruction) instruction;
                int index = instr.getIndex();
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 1 && l.stackLoc != index) {
                        result.put(l, in.get(l));
                    }
                    if (l.stackLoc == context.stackPtr - 1) {
                        ValueMapping v = new ValueMapping(in.get(l), true);
                        if (in.get(l).source == null
                                || in.get(l).source.stackLoc != index) {
                            v.defscope = ValueMapping.scope;
                        }
                        result.put(new Location(index), v);
                    }
                }
            }
            break;

            case Constants.ASTORE_0:
            case Constants.ASTORE_1:
            case Constants.ASTORE_2:
            case Constants.ASTORE_3:
            case Constants.ASTORE:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.LSTORE_0:
            case Constants.LSTORE_1:
            case Constants.LSTORE_2:
            case Constants.LSTORE_3:
            case Constants.LSTORE:
                // drop top entries to avoid clobbering
                filterSet(in, result, context.stackPtr - 2);
                break;

            case Constants.ILOAD_0:
            case Constants.ILOAD_1:
            case Constants.ILOAD_2:
            case Constants.ILOAD_3:
            case Constants.ILOAD: {
                LoadInstruction instr = (LoadInstruction) instruction;
                filterSet(in, result, context.stackPtr);
                int index = instr.getIndex();
                for (Location l : in.keySet()) {
                    if (l.stackLoc == index) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.source = l;
                        result.put(new Location(context.stackPtr), m);
                    }
                }
            }
            break;

            case Constants.ALOAD_0:
            case Constants.ALOAD_1:
            case Constants.ALOAD_2:
            case Constants.ALOAD_3:
            case Constants.ALOAD:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.ARRAYLENGTH: {
                filterSet(in, result, context.stackPtr - 1);

                DFATool p = interpreter.getDFATool();
                Set<String> receivers = p.getReceivers(stmt, context.callString);
                Location location = new Location(context.stackPtr - 1);
                boolean valid = false;
                if (receivers == null) {
                    System.out.println("no receivers at: " + context.callString.toStringList() + context.method() + stmt);
                } else {
                    for (String arrayName : receivers) {
                        ValueMapping m = in.get(new Location(arrayName + ".length"));
                        if (m != null) {
                            ValueMapping value = new ValueMapping(m, false);
                            value.join(result.get(location));
                            result.put(location, value);
                            valid = true;
                        }
                    }
                }
                if (!valid) {
                    result.put(new Location(context.stackPtr - 1), new ValueMapping());
                }
            }
            break;

            case Constants.PUTFIELD: {
                PUTFIELD instr = (PUTFIELD) instruction;
                int fieldSize = instr.getFieldType(context.constPool()).getSize();

                for (Location l : in.keySet()) {
                    if (l.stackLoc >= 0 && l.stackLoc < context.stackPtr - 1 - fieldSize) {
                        result.put(l, in.get(l));
                    }
                }

//			System.out.println(context.stackPtr+","+fieldSize+": "+result);

                DFATool p = interpreter.getDFATool();
                Set<String> receivers = p.getReceivers(stmt, context.callString);
                if (receivers == null) {
                    warnNoReceiver(context, stmt);
                } else {
                    for (String fieldName : receivers) {

                        String f = fieldName.substring(fieldName.lastIndexOf("."), fieldName.length());
                        String strippedName;
                        if (fieldName.indexOf("@") >= 0) {
                            strippedName = fieldName.split("@")[0] + f;
                        } else {
                            strippedName = fieldName;
                        }

//					System.out.println(fieldName+" vs "+strippedName);

                        if (p.containsField(strippedName)) {
                            for (Location l : in.keySet()) {
                                if (l.stackLoc < 0 && !receivers.contains(l.heapLoc)) {
                                    result.put(l, in.get(l));
                                }
                                if (l.stackLoc == context.stackPtr - 1) {
                                    result.put(new Location(fieldName), new ValueMapping(in.get(l), false));
                                }
                            }
                        }
                    }
                }
            }
            break;

            case Constants.GETFIELD: {
                GETFIELD instr = (GETFIELD) instruction;

                filterSet(in, result, context.stackPtr - 1);

                DFATool p = interpreter.getDFATool();
                Set<String> receivers = p.getReceivers(stmt, context.callString);

                Location location = new Location(context.stackPtr - 1);
                boolean valid = false;
                if (receivers == null) {
                    warnNoReceiver(context, stmt);
                } else {
                    for (String fieldName : receivers) {
                        String f = fieldName.substring(fieldName.lastIndexOf("."), fieldName.length());
                        String strippedName;
                        if (fieldName.indexOf("@") >= 0) {
                            strippedName = fieldName.split("@")[0] + f;
                        } else {
                            strippedName = fieldName;
                        }

                        //					System.out.println(fieldName+" vs "+strippedName);

                        if (p.containsField(strippedName)) {
                            for (Location l : in.keySet()) {
                                if (l.heapLoc.equals(fieldName)) {
                                    ValueMapping value = new ValueMapping(in.get(l), false);
                                    value.join(result.get(location));
                                    result.put(location, value);
                                    valid = true;
                                }
                            }
                        }
                    }
                }
                if (!valid && !(instr.getFieldType(context.constPool()) instanceof ReferenceType)) {
                    result.put(new Location(context.stackPtr - 1), new ValueMapping());
                }
            }
            break;

            case Constants.PUTSTATIC: {
                PUTSTATIC instr = (PUTSTATIC) instruction;
                int fieldSize = instr.getFieldType(context.constPool()).getSize();

                for (Location l : in.keySet()) {
                    if (l.stackLoc >= 0 && l.stackLoc < context.stackPtr - fieldSize) {
                        result.put(l, in.get(l));
                    }
                }

                DFATool p = interpreter.getDFATool();
                Set<String> receivers = p.getReceivers(stmt, context.callString);
                for (String fieldName : receivers) {
                    if (p.containsField(fieldName)) {
                        for (Location l : in.keySet()) {
                            if (l.stackLoc < 0 && !receivers.contains(l.heapLoc)) {
                                result.put(l, in.get(l));
                            }
                            if (l.stackLoc == context.stackPtr - 1) {
                                result.put(new Location(fieldName), new ValueMapping(in.get(l), false));
                            }
                        }
                    }
                }
            }
            break;

            case Constants.GETSTATIC: {
                GETSTATIC instr = (GETSTATIC) instruction;

                result = new LinkedHashMap<Location, ValueMapping>(in);
                retval.put(context.callString, result);

                DFATool p = interpreter.getDFATool();
                Set<String> receivers = p.getReceivers(stmt, context.callString);
                Location location = new Location(context.stackPtr);
                boolean valid = false;
                for (String fieldName : receivers) {
                    if (p.containsField(fieldName)) {
                        for (Location l : in.keySet()) {
                            if (l.heapLoc.equals(fieldName)) {
                                ValueMapping value = new ValueMapping(in.get(l), false);
                                value.join(result.get(location));
                                result.put(location, value);
                                valid = true;
                            }
                        }
                    }
                }
                if (!valid && !(instr.getFieldType(context.constPool()) instanceof ReferenceType)) {
                    result.put(new Location(context.stackPtr), new ValueMapping());
                }
            }
            break;

            case Constants.IASTORE:
            case Constants.CASTORE:
            case Constants.SASTORE:
            case Constants.BASTORE: {

                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 3) {
                        result.put(l, in.get(l));
                    }
                }

                DFATool p = interpreter.getDFATool();
                Set<String> receivers = p.getReceivers(stmt, context.callString);
                if (receivers == null) {
                    warnNoReceiver(context, stmt);
                    break;
                }
                for (String name : receivers) {
                    for (Location l : in.keySet()) {
                        if (l.stackLoc == context.stackPtr - 1) {
                            Location loc = new Location(name);
                            ValueMapping val = new ValueMapping(in.get(l), false);
                            val.join(in.get(loc));
                            result.put(loc, val);
                        }
                    }
                }
            }
            break;

            case Constants.AASTORE: {
                filterSet(in, result, context.stackPtr - 3);
            }
            break;

            case Constants.IALOAD:
            case Constants.CALOAD:
            case Constants.SALOAD:
            case Constants.BALOAD: {

                filterSet(in, result, context.stackPtr - 2);

                DFATool p = interpreter.getDFATool();
                Location location = new Location(context.stackPtr - 2);
                boolean valid = false;
                Set<String> receivers = p.getReceivers(stmt, context.callString);
                for (String name : receivers) {
                    for (Location l : in.keySet()) {
                        if (l.heapLoc.equals(name)) {
                            ValueMapping value = new ValueMapping(in.get(l), false);
                            value.join(result.get(location));
                            result.put(location, value);
                            valid = true;
                        }
                    }
                }
                if (!valid) {
                    result.put(new Location(context.stackPtr - 2), new ValueMapping(0));
                }
            }
            break;

            case Constants.AALOAD: {
                ValueMapping v = in.get(new Location(context.stackPtr - 1));
                if (v == null) {
                    logger.warn("no value at: " + context.callString.toStringList() + context.method() + stmt);
                } else {
                    recordArrayIndex(stmt, context, v.assigned);
                }
                filterSet(in, result, context.stackPtr - 2);
            }
            break;

            case Constants.DUP: {
                for (Location l : in.keySet()) {
                    result.put(l, in.get(l));
                    if (l.stackLoc == context.stackPtr - 1) {
                        result.put(new Location(context.stackPtr), new ValueMapping(in.get(l), true));
                    }
                }
            }
            break;
            case Constants.DUP_X1: {
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 2) {
                        result.put(l, in.get(l));
                    }
                    if (l.stackLoc == context.stackPtr - 1) {
                        result.put(new Location(context.stackPtr - 2), new ValueMapping(in.get(l), true));
                        result.put(new Location(context.stackPtr), new ValueMapping(in.get(l), true));
                    }
                    if (l.stackLoc == context.stackPtr - 2) {
                        result.put(new Location(context.stackPtr - 1), new ValueMapping(in.get(l), true));
                    }
                }
            }
            break;

            case Constants.DUP2: {
                for (Location l : in.keySet()) {
                    result.put(l, in.get(l));
                    if (l.stackLoc == context.stackPtr - 2) {
                        result.put(new Location(context.stackPtr), new ValueMapping(in.get(l), true));
                    }
                    if (l.stackLoc == context.stackPtr - 1) {
                        result.put(new Location(context.stackPtr + 1), new ValueMapping(in.get(l), true));
                    }
                }
            }
            break;

            case Constants.POP: {
                filterSet(in, result, context.stackPtr - 1);
            }
            break;

            case Constants.POP2: {
                filterSet(in, result, context.stackPtr - 2);
            }
            break;

            case Constants.IINC: {
                IINC instr = (IINC) instruction;
                int index = instr.getIndex();
                int increment = instr.getIncrement();
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr) {
                        result.put(l, in.get(l));
                    }
                    if (l.stackLoc == index) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.assigned.add(increment);
                        m.constrained.add(increment);
                        Interval operand = new Interval(increment, increment);

                        if (m.increment != null && !m.softinc) {
                            m.increment.join(operand);
                        } else if (m.increment != null && m.softinc) {
                            if ((m.increment.getLb() < 0 && operand.getUb() > 0)
                                    || (m.increment.getUb() > 0 && operand.getLb() < 0)) {
                                m.increment.join(operand);
                            } else {
                                m.increment = operand;
                            }
                            m.softinc = false;
                        } else {
                            m.increment = operand;
                            m.softinc = false;
                        }
                        result.put(l, m);
                    }
                }
            }
            break;

            case Constants.IADD: {
                Interval operand = new Interval();
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        operand = in.get(l).assigned;
                    }
                }
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 2) {
                        result.put(l, in.get(l));
                    } else if (l.stackLoc == context.stackPtr - 2) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.assigned.add(operand);
                        m.constrained.add(operand);
                        if (m.increment != null && !m.softinc) {
                            m.increment.join(operand);
                        } else if (m.increment != null && m.softinc) {
                            if ((m.increment.getLb() < 0 && operand.getUb() > 0)
                                    || (m.increment.getUb() > 0 && operand.getLb() < 0)) {
                                m.increment.join(operand);
                            } else {
                                m.increment = operand;
                            }
                            m.softinc = false;
                        } else {
                            m.increment = operand;
                            m.softinc = false;
                        }
                        result.put(l, m);
                    }
                }

            }
            break;

            case Constants.ISUB: {
                Interval operand = new Interval();
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        operand = in.get(l).assigned;
                    }
                }
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 2) {
                        result.put(l, in.get(l));
                    } else if (l.stackLoc == context.stackPtr - 2) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.assigned.sub(operand);
                        m.constrained.sub(operand);
                        operand.neg(); // decrement rather than increment
                        if (m.increment != null && !m.softinc) {
                            m.increment.join(operand);
                        } else if (m.increment != null && m.softinc) {
                            if ((m.increment.getLb() < 0 && operand.getUb() > 0)
                                    || (m.increment.getUb() > 0 && operand.getLb() < 0)) {
                                m.increment.join(operand);
                            } else {
                                m.increment = operand;
                            }
                            m.softinc = false;
                        } else {
                            m.increment = operand;
                            m.softinc = false;
                        }
                        result.put(l, m);
                    }
                }

            }
            break;

            case Constants.INEG: {
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 1) {
                        result.put(l, in.get(l));
                    } else if (l.stackLoc == context.stackPtr - 1) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.assigned.neg();
                        m.constrained.neg();
                        m.increment = new Interval();
                        result.put(l, m);
                    }
                }
            }
            break;

            case Constants.IUSHR: {
                Interval operand = new Interval();
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        operand = in.get(l).assigned;
                    }
                }
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 2) {
                        result.put(l, in.get(l));
                    } else if (l.stackLoc == context.stackPtr - 2) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.assigned.ushr(operand);
                        m.constrained.ushr(operand);
                        m.increment = new Interval();
                        result.put(l, m);
                    }
                }
            }
            break;

            case Constants.ISHR: {
                Interval operand = new Interval();
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        operand = in.get(l).assigned;
                    }
                }
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 2) {
                        result.put(l, in.get(l));
                    } else if (l.stackLoc == context.stackPtr - 2) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.assigned.shr(operand);
                        m.constrained.shr(operand);
                        m.increment = new Interval();
                        result.put(l, m);
                    }
                }
            }
            break;

            case Constants.IMUL: {
                Interval operand = new Interval();
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        operand = in.get(l).assigned;
                    }
                }
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 2) {
                        result.put(l, in.get(l));
                    } else if (l.stackLoc == context.stackPtr - 2) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.assigned.mul(operand);
                        m.constrained.mul(operand);
                        m.increment = new Interval();
                        result.put(l, m);
                    }
                }
            }
            break;

            case Constants.IDIV: {
                Interval operand = new Interval();
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        operand = in.get(l).assigned;
                    }
                }
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 2) {
                        result.put(l, in.get(l));
                    } else if (l.stackLoc == context.stackPtr - 2) {
                        ValueMapping m = new ValueMapping(in.get(l), true);
                        m.assigned.div(operand);
                        m.constrained.div(operand);
                        m.increment = new Interval();
                        result.put(l, m);
                    }
                }
            }
            break;

            case Constants.IAND:
            case Constants.IOR:
            case Constants.IXOR:
            case Constants.IREM:
            case Constants.ISHL: {
                // TODO: we could be more clever for some operations
                for (Location l : in.keySet()) {
                    if (l.stackLoc < context.stackPtr - 2) {
                        result.put(l, in.get(l));
                    } else if (l.stackLoc == context.stackPtr - 2) {
                        ValueMapping m = new ValueMapping();
                        result.put(l, m);
                    }
                }
            }
            break;

            case Constants.I2B:
            case Constants.I2C:
            case Constants.I2S:
                // TODO: is this really correct?
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.MONITORENTER:
                result = in;
                retval.put(context.callString, result);
                context.syncLevel++;
                break;

            case Constants.MONITOREXIT:
                result = in;
                retval.put(context.callString, result);
                context.syncLevel--;
                if (context.syncLevel < 0) {
                    System.err.println("Synchronization level mismatch.");
                    System.exit(-1);
                }
                break;

            case Constants.CHECKCAST:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.INSTANCEOF: {
                filterSet(in, result, context.stackPtr - 1);
                ValueMapping bool = new ValueMapping();
                bool.assigned.setLb(0);
                bool.assigned.setUb(1);
                result.put(new Location(context.stackPtr - 1), bool);
            }
            break;

            case Constants.NEW: {
                result = in;
                retval.put(context.callString, result);
            }
            break;

            case Constants.NEWARRAY: {
                NEWARRAY instr = (NEWARRAY) instruction;

                String name = instr.getType().toString();
                name += "@" + context.method() + ":" + stmt.getPosition();

                filterSet(in, result, context.stackPtr - 1);

                boolean valid = false;
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        result.put(new Location(name + ".length"), in.get(l));
                        recordSize(stmt, context, in.get(l).assigned);
                        valid = true;
                    }
                }
                if (!valid) {
                    ValueMapping v = new ValueMapping();
                    result.put(new Location(name + ".length"), v);
                    recordSize(stmt, context, v.assigned);
                }
            }
            break;

            case Constants.ANEWARRAY: {
                ANEWARRAY instr = (ANEWARRAY) instruction;

                String name = instr.getType(context.constPool()).toString() + "[]";
                name += "@" + context.method() + ":" + stmt.getPosition();
                //System.out.println("NEW ARRAY: "+name);

                filterSet(in, result, context.stackPtr - 1);
                boolean valid = false;
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        result.put(new Location(name + ".length"), in.get(l));
                        recordSize(stmt, context, in.get(l).assigned);
                        valid = true;
                    }
                }
                if (!valid) {
                    ValueMapping v = new ValueMapping();
                    result.put(new Location(name + ".length"), v);
                    recordSize(stmt, context, v.assigned);
                }
            }
            break;

            case Constants.MULTIANEWARRAY: {
                MULTIANEWARRAY instr = (MULTIANEWARRAY) instruction;
                int dim = instr.getDimensions();

                filterSet(in, result, context.stackPtr - dim);

                String type = instr.getType(context.constPool()).toString();
                type = type.substring(0, type.indexOf("["));

                Interval[] size = new Interval[dim];

                for (int i = 1; i <= dim; i++) {
                    String name = type;
                    for (int k = 0; k < i; k++) {
                        name += "[]";
                    }
                    name += "@" + context.method() + ":" + stmt.getPosition();

                    boolean valid = false;
                    for (Location l : in.keySet()) {
                        if (l.stackLoc == context.stackPtr - i) {
                            result.put(new Location(name + ".length"), in.get(l));
                            if (size[i - 1] != null) {
                                size[i - 1].join(in.get(l).assigned);
                            } else {
                                size[i - 1] = in.get(l).assigned;
                            }
                            valid = true;
                        }
                    }
                    if (!valid) {
                        ValueMapping v = new ValueMapping();
                        result.put(new Location(name + ".length"), v);
                        size[i - 1] = v.assigned;
                    }
                }

                recordSize(stmt, context, size);
            }
            break;

            case Constants.GOTO:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.IFNULL:
            case Constants.IFNONNULL: {
                checkScope(context, stmt);
                filterSet(in, result, context.stackPtr - 1);
            }
            break;

            case Constants.IF_ACMPEQ:
            case Constants.IF_ACMPNE: {
                checkScope(context, stmt);
                filterSet(in, result, context.stackPtr - 2);
            }
            break;

            case Constants.IFEQ:
            case Constants.IFNE:
            case Constants.IFLT:
            case Constants.IFGE:
            case Constants.IFLE:
            case Constants.IFGT:
                checkScope(context, stmt);
                result = doIf(stmt, edge, context, in, result);
                retval.put(context.callString, result);
                break;

            case Constants.IF_ICMPEQ:
            case Constants.IF_ICMPNE:
            case Constants.IF_ICMPLT:
            case Constants.IF_ICMPGE:
            case Constants.IF_ICMPGT:
            case Constants.IF_ICMPLE:
                checkScope(context, stmt);
                result = doIfIcmp(stmt, edge, context, in, result);
                retval.put(context.callString, result);
                break;

            case Constants.LOOKUPSWITCH:
            case Constants.TABLESWITCH:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.INVOKEVIRTUAL:
            case Constants.INVOKEINTERFACE:
            case Constants.INVOKESTATIC:
            case Constants.INVOKESPECIAL: {
                DFATool p = interpreter.getDFATool();
                Set<String> receivers = p.getReceivers(stmt, context.callString);
                if (receivers == null) {
                    warnUnknownReceivers(context, stmt);
                    result = in;
                    break;
                }
                for (String methodName : receivers) {
                    doInvoke(methodName, stmt, context, input, interpreter, state, retval);
                }
            }
            break;

            case Constants.ARETURN:
            case Constants.RETURN: {
                filterSet(in, result, 0);
            }
            break;

            case Constants.IRETURN: {
                filterSet(in, result, 0);
                for (Location l : in.keySet()) {
                    if (l.stackLoc == context.stackPtr - 1) {
                        result.put(new Location(0), new ValueMapping(in.get(l), false));
                    }
                }
            }
            break;

            default:
//			System.out.println("unknown instruction: "+stmt);
                result = in;
                retval.put(context.callString, result);
                break;
        }

//		System.out.println(stmt);
//		System.out.print(stmt.getInstruction()+":\t{ ");
//		if (retval != null) {
//			for (Iterator<Map<Location, ValueMapping>> k = retval.values().iterator(); k.hasNext(); ) {
//				Map<Location, ValueMapping> m = k.next();
//				System.out.print(m+", ");
//			}
//		}
//		System.out.println("}");

        context.stackPtr += instruction.produceStack(context.constPool()) - instruction.consumeStack(context.constPool());
        return retval;
    }

    private void warnUnknownReceivers(Context context, InstructionHandle stmt) {
        Instruction instruction = stmt.getInstruction();
        String loc = context.method() + ": invoke " + instruction.toString(context.constPool().getConstantPool()) +
                "(" + stmt.toString(true) + ")";
        if (logger.isTraceEnabled()) {
            logger.trace(loc + " unknown receivers");
        }
        if (unknownReceiverWarnings.add(loc)) {
            logger.warn(loc + " unknown receivers");
        }
    }

    private void warnNoReceiver(Context context, InstructionHandle stmt) {
        String loc = context.callString.toStringList() + context.method() + stmt;
        if (logger.isTraceEnabled()) {
            logger.trace("no receivers at: " + loc);
        }
        if (noReceiverWarnings.add(loc)) {
            logger.warn("no receivers at: " + loc);
        }
    }

    private void recordArrayIndex(InstructionHandle stmt, Context context, Interval assigned) {
        ContextMap<CallString, Interval> indexMap = arrayIndices.get(stmt);
        if (indexMap == null) {
            indexMap = new ContextMap<CallString, Interval>(context, new LinkedHashMap<CallString, Interval>());
            arrayIndices.put(stmt, indexMap);
        }
        indexMap.put(context.callString, assigned);
    }

    private void filterSet(Map<Location, ValueMapping> in, Map<Location, ValueMapping> result, int bound) {
        for (Location l : in.keySet()) {
            if (l.stackLoc < bound) {
                result.put(l, in.get(l));
            }
        }
    }

    private void checkScope(Context context, InstructionHandle stmt) {
        if (scopes.get(stmt) == null) {
            scopes.put(stmt, new ContextMap<CallString, Integer>(context, new LinkedHashMap<CallString, Integer>()));
        }
        if (scopes.get(stmt).get(context.callString) == null) {
            ValueMapping.scope = ++ValueMapping.scopeCnt;
            scopes.get(stmt).put(context.callString, ValueMapping.scope);
        }
    }

    private Map<Location, ValueMapping> doIf(InstructionHandle stmt, FlowEdge edge, Context context,
                                             Map<Location, ValueMapping> in, Map<Location, ValueMapping> result) {

        // copy input values
        for (Location l : in.keySet()) {
            if (l.stackLoc < context.stackPtr - 1) {
                result.put(l, in.get(l));
            }
        }
        // apply constraint
        loop:
        for (Location l : in.keySet()) {
            if (l.stackLoc == context.stackPtr - 1) {

                ValueMapping m = new ValueMapping(in.get(l), true);

                switch (stmt.getInstruction().getOpcode()) {
                    case Constants.IFEQ:
                        if (edge.getType() == FlowEdge.FALSE_EDGE) {
                            // != 0 cannot be expressed as interval
                            // but we can check infeasibility
                            if (m.assigned.getUb() == 0 && m.assigned.getLb() == 0) {
                                recordInfeasible(stmt, context, edge);
                                result = null;
                                break loop;
                            }
                        } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                            m.constrained.setLb(0);
                            m.constrained.setUb(0);
                        }
                        break;
                    case Constants.IFNE:
                        if (edge.getType() == FlowEdge.FALSE_EDGE) {
                            m.constrained.setLb(0);
                            m.constrained.setUb(0);
                        } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                            // != 0 cannot be expressed as interval
                            // but we can check infeasibility
                            if (m.assigned.getUb() == 0 && m.assigned.getLb() == 0) {
                                recordInfeasible(stmt, context, edge);
                                result = null;
                                break loop;
                            }
                        }
                        break;
                    case Constants.IFLT:
                        if (edge.getType() == FlowEdge.FALSE_EDGE) {
                            m.constrained.setLb(0);
                        } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                            m.constrained.setUb(-1);
                        }
                        break;
                    case Constants.IFGE:
                        if (edge.getType() == FlowEdge.FALSE_EDGE) {
                            m.constrained.setUb(-1);
                        } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                            m.constrained.setLb(0);
                        }
                        break;
                    case Constants.IFLE:
                        if (edge.getType() == FlowEdge.FALSE_EDGE) {
                            m.constrained.setLb(1);
                        } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                            m.constrained.setUb(0);
                        }
                        break;
                    case Constants.IFGT:
                        if (edge.getType() == FlowEdge.FALSE_EDGE) {
                            m.constrained.setUb(0);
                        } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                            m.constrained.setLb(1);
                        }
                        break;

                }

                if (m.assigned.getLb() > m.constrained.getUb()
                        || m.assigned.getUb() < m.constrained.getLb()) {
                    recordInfeasible(stmt, context, edge);
                    result = null;
                    break;
                }

                removeInfeasible(stmt, context, edge);

                m.assigned.constrain(m.constrained);

                recordBound(stmt, context, edge, new ValueMapping(m, true));

                m.softinc = true;

                // TODO: is this really correct for all cases?
                if (in.get(l).source != null) {
                    result.put(in.get(l).source, m);
                }
            }
        }

        return result;
    }

    private Map<Location, ValueMapping> doIfIcmp(InstructionHandle stmt, FlowEdge edge, Context context,
                                                 Map<Location, ValueMapping> in, Map<Location, ValueMapping> result) {
        // search for constraining value
        Interval constraint = null;
        for (Location l : in.keySet()) {
            if (l.stackLoc == context.stackPtr - 1) {
                constraint = in.get(l).assigned;
            }
        }
        // copy input values
        for (Location l : in.keySet()) {
            if (l.stackLoc < context.stackPtr - 2) {
                result.put(l, in.get(l));
            }
        }
        // apply constraint
        for (Location l : in.keySet()) {
            if (l.stackLoc == context.stackPtr - 2 && in.get(l).source != null) {

                ValueMapping m = new ValueMapping(in.get(l), true);

                if (constraint != null) {
                    switch (stmt.getInstruction().getOpcode()) {
                        case Constants.IF_ICMPEQ:
                            if (edge.getType() == FlowEdge.FALSE_EDGE) {
                                // != Interval not expressable as Interval
                                // TODO: mark paths infeasible if appropriate
                            } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                                if (constraint.hasLb()) {
                                    m.constrained.setLb(constraint.getLb());
                                }
                                if (constraint.hasUb()) {
                                    m.constrained.setUb(constraint.getUb());
                                }
                            }
                            break;
                        case Constants.IF_ICMPNE:
                            if (edge.getType() == FlowEdge.FALSE_EDGE) {
                                if (constraint.hasLb()) {
                                    m.constrained.setLb(constraint.getLb());
                                }
                                if (constraint.hasUb()) {
                                    m.constrained.setUb(constraint.getUb());
                                }
                            } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                                // != Interval not expressable as Interval
                                // TODO: mark paths infeasible if appropriate
                            }
                            break;
                        case Constants.IF_ICMPLT:
                            if (edge.getType() == FlowEdge.FALSE_EDGE) {
                                if (constraint.hasLb()) {
                                    m.constrained.setLb(constraint.getLb());
                                }
                            } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                                if (constraint.hasUb()) {
                                    m.constrained.setUb(constraint.getUb() - 1);
                                }
                            }
                            break;
                        case Constants.IF_ICMPGE:
                            if (edge.getType() == FlowEdge.FALSE_EDGE) {
                                if (constraint.hasUb()) {
                                    m.constrained.setUb(constraint.getUb() - 1);
                                }
                            } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                                if (constraint.hasLb()) {
                                    m.constrained.setLb(constraint.getLb());
                                }
                            }
                            break;
                        case Constants.IF_ICMPGT:
                            if (edge.getType() == FlowEdge.FALSE_EDGE) {
                                if (constraint.hasUb()) {
                                    m.constrained.setUb(constraint.getUb());
                                }
                            } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                                if (constraint.hasLb()) {
                                    m.constrained.setLb(constraint.getLb() + 1);
                                }
                            }
                            break;
                        case Constants.IF_ICMPLE:
                            if (edge.getType() == FlowEdge.FALSE_EDGE) {
                                if (constraint.hasLb()) {
                                    m.constrained.setLb(constraint.getLb() - 1);
                                }
                            } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
                                if (constraint.hasUb()) {
                                    m.constrained.setUb(constraint.getUb());
                                }
                            }
                            break;
                    }
                }

                if (m.assigned.getLb() > m.constrained.getUb()
                        || m.assigned.getUb() < m.constrained.getLb()) {
                    recordInfeasible(stmt, context, edge);
                    result = null;
                    break;
                }

                removeInfeasible(stmt, context, edge);

                m.assigned.constrain(m.constrained);

                recordBound(stmt, context, edge, new ValueMapping(m, true));

                m.softinc = true;

                // TODO: is this really correct for all cases?
                result.put(in.get(l).source, m);
            }
        }

        return result;
    }

    private void doInvoke(String methodName,
                          InstructionHandle stmt,
                          Context context,
                          Map<CallString, Map<Location, ValueMapping>> input,
                          Interpreter<CallString, Map<Location, ValueMapping>> interpreter,
                          Map<InstructionHandle, ContextMap<CallString, Map<Location, ValueMapping>>> state,
                          Map<CallString, Map<Location, ValueMapping>> result) {

        DFATool p = interpreter.getDFATool();
        MethodInfo method = p.getMethod(methodName);
        //methodName = method.getMemberID().toString();

//		System.out.println(context.callString.asList()+"/"+context.method+": "+stmt+" invokes method: "+methodName);				

        if (method.isNative()) {

            handleNative(method, context, input, result);

        } else {

            // set up new context
            int varPtr = context.stackPtr - MethodHelper.getArgSize(method);
            Context c = new Context(context);
            c.stackPtr = method.getCode().getMaxLocals();
            if (method.isSynchronized()) {
                c.syncLevel = context.syncLevel + 1;
            }
            c.setMethodInfo(method);
            c.callString = c.callString.push(method, stmt, callStringLength);

            // carry only minimal information with call
            Map<Location, ValueMapping> in = input.get(context.callString);
            Map<Location, ValueMapping> out = new LinkedHashMap<Location, ValueMapping>();
            for (Location l : in.keySet()) {
                if (l.stackLoc < 0) {
                    out.put(l, in.get(l));
                }
                if (l.stackLoc >= varPtr) {
                    out.put(new Location(l.stackLoc - varPtr), new ValueMapping(in.get(l), false));
                }
            }

            ContextMap<CallString, Map<Location, ValueMapping>> tmpresult = new ContextMap<CallString, Map<Location, ValueMapping>>(c, new LinkedHashMap<CallString, Map<Location, ValueMapping>>());
            tmpresult.put(c.callString, out);

            InstructionHandle entry = p.getEntryHandle(method);
            state.put(entry, join(state.get(entry), tmpresult));

            // interpret method
            Map<InstructionHandle, ContextMap<CallString, Map<Location, ValueMapping>>> r = interpreter.interpret(c, entry, state, false);

            //System.out.println(">>>>>>>>");

            // pull out relevant information from call
            InstructionHandle exit = p.getExitHandle(method);
            if (r.get(exit) != null) {
                Map<Location, ValueMapping> returned = r.get(exit).get(c.callString);
                if (returned != null) {
                    for (Location l : returned.keySet()) {
                        if (l.stackLoc < 0) {
                            ValueMapping m = new ValueMapping(returned.get(l), true);
                            m.join(result.get(context.callString).get(l));
                            result.get(context.callString).put(l, m);
                        }
                        if (l.stackLoc >= 0) {
                            ValueMapping m = new ValueMapping(returned.get(l), false);
                            Location loc = new Location(l.stackLoc + varPtr);
                            m.join(result.get(context.callString).get(loc));
                            result.get(context.callString).put(loc, m);
                        }
                    }
                }
            }

            // add relevant information to result
            for (Location l : in.keySet()) {
                if (l.stackLoc >= 0 && l.stackLoc < context.stackPtr - MethodHelper.getArgSize(method)) {
                    result.get(context.callString).put(l, new ValueMapping(in.get(l), true));
                }
            }
        }
    }

    @SuppressWarnings({"LiteralAsArgToStringEquals"})
    private Map<CallString, Map<Location, ValueMapping>> handleNative(MethodInfo method, Context context,
                                                                      Map<CallString, Map<Location, ValueMapping>> input,
                                                                      Map<CallString, Map<Location, ValueMapping>> result) {

        String methodId = method.getMemberID().toString();

        Map<Location, ValueMapping> in = input.get(context.callString);
        Map<Location, ValueMapping> out = new LinkedHashMap<Location, ValueMapping>();

        if (methodId.equals("com.jopdesign.sys.Native#rd(I)I")
                || methodId.equals("com.jopdesign.sys.Native#rdMem(I)I")
                || methodId.equals("com.jopdesign.sys.Native#rdIntMem(I)I")
                || methodId.equals("com.jopdesign.sys.Native#getStatic(I)I")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr - 1) {
                    out.put(l, in.get(l));
                }
            }
            out.put(new Location(context.stackPtr - 1), new ValueMapping());
        } else if (methodId.equals("com.jopdesign.sys.Native#wr(II)V")
                || methodId.equals("com.jopdesign.sys.Native#wrMem(II)V")
                || methodId.equals("com.jopdesign.sys.Native#wrIntMem(II)V")
                || methodId.equals("com.jopdesign.sys.Native#putStatic(II)V")
                || methodId.equals("com.jopdesign.sys.Native#toLong(D)J")
                || methodId.equals("com.jopdesign.sys.Native#toDouble(J)D")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr - 2) {
                    out.put(l, in.get(l));
                }
            }
        } else if (methodId.equals("com.jopdesign.sys.Native#toInt(Ljava/lang/Object;)I")
                || methodId.equals("com.jopdesign.sys.Native#toInt(F)I")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr - 1) {
                    out.put(l, in.get(l));
                }
            }
            out.put(new Location(context.stackPtr - 1), new ValueMapping());
        } else if (methodId.equals("com.jopdesign.sys.Native#toObject(I)Ljava/lang/Object;")
                || methodId.equals("com.jopdesign.sys.Native#toIntArray(I)[I")
                || methodId.equals("com.jopdesign.sys.Native#toFloat(I)F")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr - 1) {
                    out.put(l, in.get(l));
                }
            }
        } else if (methodId.equals("com.jopdesign.sys.Native#getSP()I")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr) {
                    out.put(l, in.get(l));
                }
            }
            out.put(new Location(context.stackPtr), new ValueMapping());
        } else if (methodId.equals("com.jopdesign.sys.Native#toInt(Ljava/lang/Object;)I")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr - 1) {
                    out.put(l, in.get(l));
                }
            }
            out.put(new Location(context.stackPtr - 1), new ValueMapping());
        } else if (methodId.equals("com.jopdesign.sys.Native#condMove(IIZ)I")
                || methodId.equals("com.jopdesign.sys.Native#condMoveRef(Ljava/lang/Object;Ljava/lang/Object;Z)Ljava/lang/Object;")
                || methodId.equals("com.jopdesign.sys.Native#memCopy(III)V")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr - 3) {
                    out.put(l, in.get(l));
                }
            }
        } else if (methodId.equals("com.jopdesign.sys.Native#invalidate()V")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr) {
                    out.put(l, in.get(l));
                }
            }
        } else if (methodId.equals("com.jopdesign.sys.Native#arrayLength(I)I")
                || methodId.equals("com.jopdesign.sys.Native#invoke(I)V")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr - 1) {
                    out.put(l, in.get(l));
                }
            }
        } else if (methodId.equals("com.jopdesign.sys.Native#invoke(II)V")) {
            for (Location l : in.keySet()) {
                if (l.stackLoc < context.stackPtr - 2) {
                    out.put(l, in.get(l));
                }
            }
        } else {
        	AppInfoError ex = new AppInfoError("Unknown native method: " + methodId);
        	logger.error(ex);
        	throw ex;
        }

        result.put(context.callString, out);

        return result;
    }

    private void recordBound(InstructionHandle stmt, Context context, FlowEdge edge, ValueMapping bound) {
        ContextMap<CallString, Pair<ValueMapping, ValueMapping>> map = bounds.get(stmt);
        if (map == null) {
            map = new ContextMap<CallString, Pair<ValueMapping, ValueMapping>>(context, new LinkedHashMap<CallString, Pair<ValueMapping, ValueMapping>>());
            bounds.put(stmt, map);
        }
        Pair<ValueMapping, ValueMapping> b = map.get(context.callString);
        if (b == null) {
            b = new Pair<ValueMapping, ValueMapping>();
            map.put(context.callString, b);
        }
//		System.out.println("CONDITION BOUND: "+bound);
//		System.out.println("\tin "+context.callString+"/"+context.method);
        if (edge.getType() == FlowEdge.FALSE_EDGE) {
            map.put(context.callString, new Pair<ValueMapping, ValueMapping>(b.first(), bound));
        } else if (edge.getType() == FlowEdge.TRUE_EDGE) {
            map.put(context.callString, new Pair<ValueMapping, ValueMapping>(bound, b.second()));
        }
    }

    public Map<InstructionHandle, ContextMap<CallString, Pair<ValueMapping, ValueMapping>>> getResult() {
        return bounds;
    }

    public int getBound(InstructionHandle instr) {
        return getBound(instr, CallString.EMPTY);
    }

    public int getBound(InstructionHandle instr, CallString csSuffix) {

        ContextMap<CallString, Pair<ValueMapping, ValueMapping>> r = bounds.get(instr);
        if (r == null) {
            // no bound at this point
            return -1;
        }

        // merge bound for all contexts
        int maxValue = -1;
        for (CallString callString : r.keySet()) {
            if (!callString.hasSuffix(csSuffix)) continue;

            Pair<ValueMapping, ValueMapping> bounds = r.get(callString);

            ValueMapping first = bounds.first();
            ValueMapping second = bounds.second();

            if (first == null || second == null) {
                return -1;
            }

            InstructionHandle target = ((BranchInstruction) instr.getInstruction()).getTarget();

            if (scopes.get(target) != null) {
                if (scopes.get(target).get(callString) <= first.defscope
                        || scopes.get(target).get(callString) <= second.defscope) {
                    return -1;
                }
            }

            if (scopes.get(instr).get(callString) <= first.defscope
                    || scopes.get(instr).get(callString) <= second.defscope) {
                return -1;
            }

//			if (first.softinc || second.softinc) {
//				return -1;
//			}

            int val = ValueMapping.computeBound(first, second);
            if (val < 0) {
                // no bound for some context
                return -1;
            } else {
                // compute the maximum
                maxValue = Math.max(maxValue, val);
            }
        }

        return maxValue;
    }


    private void recordSize(InstructionHandle stmt, Context context, Interval size) {
        ContextMap<CallString, Interval[]> sizeMap;
        sizeMap = sizes.get(stmt);
        if (sizeMap == null) {
            sizeMap = new ContextMap<CallString, Interval[]>(context, new LinkedHashMap<CallString, Interval[]>());
        }
        Interval[] v = new Interval[1];
        v[0] = size;
        sizeMap.put(context.callString, v);
        sizes.put(stmt, sizeMap);
    }

    private void recordSize(InstructionHandle stmt, Context context, Interval[] size) {
        ContextMap<CallString, Interval[]> sizeMap;
        sizeMap = sizes.get(stmt);
        if (sizeMap == null) {
            sizeMap = new ContextMap<CallString, Interval[]>(context, new LinkedHashMap<CallString, Interval[]>());
        }
        sizeMap.put(context.callString, size);
        sizes.put(stmt, sizeMap);
    }

    public void printSizeResult(DFATool program) {

        for (InstructionHandle instr : sizes.keySet()) {
            ContextMap<CallString, Interval[]> r = sizes.get(instr);
            Context c = r.getContext();

            LineNumberTable lines = c.getMethodInfo().getCode().getLineNumberTable();
            int sourceLine = lines.getSourceLine(instr.getPosition());

            for (CallString callString : r.keySet()) {
                Interval[] bounds = r.get(callString);

                System.out.println(c.method() + ":" + sourceLine + ":\t" + callString.toStringList() + ": ");
                System.out.println(Arrays.asList(bounds));
            }
        }
    }

    private void recordInfeasible(InstructionHandle stmt, Context context, FlowEdge edge) {
        ContextMap<CallString, Set<FlowEdge>> infMap;
        infMap = infeasibles.get(stmt);
        if (infMap == null) {
            infMap = new ContextMap<CallString, Set<FlowEdge>>(context, new LinkedHashMap<CallString, Set<FlowEdge>>());
            infeasibles.put(stmt, infMap);
        }
        Set<FlowEdge> flowSet = infMap.get(context.callString);
        if (flowSet == null) {
            flowSet = new LinkedHashSet<FlowEdge>();
            infMap.put(context.callString, flowSet);
        }
        flowSet.add(edge);
    }

    private void removeInfeasible(InstructionHandle stmt, Context context, FlowEdge edge) {
        ContextMap<CallString, Set<FlowEdge>> infMap;
        infMap = infeasibles.get(stmt);
        if (infMap == null) {
            return;
        }
        Set<FlowEdge> flowSet = infMap.get(context.callString);
        if (flowSet == null) {
            return;
        }
        flowSet.remove(edge);
    }

    public void printInfeasibles(DFATool program) {
        System.out.println(infeasibles);
    }

    public Set<FlowEdge> getInfeasibleEdges(InstructionHandle stmt, CallString cs) {
        ContextMap<CallString, Set<FlowEdge>> map = infeasibles.get(stmt);
        Set<FlowEdge> retval = new LinkedHashSet<FlowEdge>();
        if (map == null) { // no infeasibility information here
            return retval;
        }
        for (CallString c : map.keySet()) {
            if (c.hasSuffix(cs)) {
                Set<FlowEdge> f = map.get(c);
                retval.addAll(f);
            }
        }
        return retval;
    }

    public Interval[] getArraySizes(InstructionHandle stmt, CallString cs) {
        ContextMap<CallString, Interval[]> map = sizes.get(stmt);
        Interval[] retval = null;
        if (map == null) {
            return retval;
        }
        for (CallString c : map.keySet()) {
            if (c.hasSuffix(cs)) {
                if (retval == null) {
                    Interval[] v = map.get(c);
                    // JDK1.5 compat
                    retval = new Interval[v.length];
                    System.arraycopy(v, 0, retval, 0, v.length);
                    // JDK1.6 only
                    //retval = Arrays.copyOf(v, v.length);
                } else {
                    Interval[] v = map.get(c);
                    for (int k = 0; k < v.length; k++) {
                        retval[k].join(v[k]);
                    }
                }
            }
        }
        return retval;
    }

    public Interval getArrayIndices(InstructionHandle stmt, CallString cs) {
        ContextMap<CallString, Interval> map = arrayIndices.get(stmt);
        Interval retval = null;
        if (map == null) {
            return retval;
        }
        for (CallString c : map.keySet()) {
            if (c.hasSuffix(cs)) {
                if (retval == null) {
                    retval = new Interval(map.get(c));
                } else {
                    retval.join(map.get(c));
                }
            }
        }
        return retval;
    }
    
    private class LoopBoundsResultFormatter implements ResultFormatter<Pair<ValueMapping,ValueMapping>> {

    	private AppInfo appInfo;

    	public LoopBoundsResultFormatter(AppInfo appInfo) {
    		
    		this.appInfo = appInfo;
    	}
		
    	@Override
		public String format(String method,
				CallStringSerialization css, Integer position,
				Pair<ValueMapping, ValueMapping> bounds) {
			
			StringBuffer sb = new StringBuffer(); 
			ValueMapping first = bounds.first();
			ValueMapping second = bounds.second();
				
			sb.append("[true:  ");
			sb.append(first);
			sb.append(", false: ");
			sb.append(second);
			sb.append(", bound: ");

			InstructionHandle instr;
			CallString cs;
			try {
				instr = appInfo.getMethodInfo(MemberID.parse(method)).
										  getCode().getInstructionList(false, false).findHandle(position);
				cs = css.getCallString(appInfo);
				int val = getBound(instr, cs);
				if (val >= 0) {
					sb.append(val);
				} else {
					sb.append("invalid");
				}
			} catch (MethodNotFoundException e) {
				e.printStackTrace();
			}
			return sb.toString();
		}
    	
    }
		

    public void printResult(DFATool program) {
    	AnalysisResultSerialization<Pair<ValueMapping,ValueMapping>> serializedResult =
    		AnalysisResultSerialization.fromContextMapResult(getResult());
    	serializedResult.dump(System.out,
    			new LoopBoundsResultFormatter(program.getAppInfo()));
    	System.out.println("=== Array Indices ===");
    	AnalysisResultSerialization.fromContextMapResult(arrayIndices).dump(System.out);
    	System.out.println("=== Infeasibles ===");
    	AnalysisResultSerialization.fromContextMapResult(infeasibles, FLOW_EDGE_SET_CONVERTER).dump(System.out);
    	System.out.println("=== Sizes ===");
    	AnalysisResultSerialization.fromContextMapResult(sizes).dump(System.out);
    }

    public static final Serializer<Set<FlowEdge>,List<SerializedFlowEdge>> FLOW_EDGE_SET_CONVERTER = 
    	new Serializer<Set<FlowEdge>, List<SerializedFlowEdge>>() {


		@Override
		public Set<FlowEdge> fromSerializedRepresentation(
				List<SerializedFlowEdge> serializedEdges, AppInfo appInfo) 
				throws IOException, MethodNotFoundException {

			Set<FlowEdge> edges = new LinkedHashSet<FlowEdge>();
			for(SerializedFlowEdge sfe : serializedEdges) {
				edges.add(sfe.toFlowEdge(appInfo));
			}
			return edges;
		}

		@Override
		public List<SerializedFlowEdge> serializedRepresentation(
				Set<FlowEdge> edges) {
			List<SerializedFlowEdge> serializedEdges = new ArrayList<SerializedFlowEdge>();
			for(FlowEdge fe : edges) {
                            if (!SerializedFlowEdge.exists(fe)) continue;
		            serializedEdges.add(new FlowEdge.SerializedFlowEdge(fe));
			}
			return serializedEdges;
		}
    	
    };
    
    /* SERIALIZE (in order): bounds, arrayIndices, infeasibles, scopes, sizes */
    @Override
	public void serializeResult(File cacheFile) throws IOException {

    	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile));
    	AnalysisResultSerialization.fromContextMapResult(bounds).serialize(oos);
    	AnalysisResultSerialization.fromContextMapResult(arrayIndices).serialize(oos);
    	AnalysisResultSerialization.fromContextMapResult(infeasibles, FLOW_EDGE_SET_CONVERTER).serialize(oos);
    	AnalysisResultSerialization.fromContextMapResult(scopes).serialize(oos);
    	AnalysisResultSerialization.fromContextMapResult(sizes).serialize(oos);
    	oos.close();
	}

    /* DESERIALIZE (in order): bounds, arrayIndices, infeasibles, scopes, sizes */
	public Map deSerializeResult(AppInfo appInfo, File cacheFile) throws IOException,
			ClassNotFoundException, MethodNotFoundException {
		
    	ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile));
		bounds = AnalysisResultSerialization.deserializeContextMap(appInfo, ois, null);
		arrayIndices = AnalysisResultSerialization.deserializeContextMap(appInfo, ois, null);
		infeasibles = AnalysisResultSerialization.deserializeContextMap(appInfo, ois, FLOW_EDGE_SET_CONVERTER);
		scopes = AnalysisResultSerialization.deserializeContextMap(appInfo, ois, null);
		sizes = AnalysisResultSerialization.deserializeContextMap(appInfo, ois, null);
    	
		ois.close();
		
    	return this.getResult();
	}

    @Override
    public void copyResults(MethodInfo newContainer, Map<InstructionHandle, InstructionHandle> newHandles) {
        for (Map.Entry<InstructionHandle,InstructionHandle> entry : newHandles.entrySet()) {
            InstructionHandle oldHandle = entry.getKey();
            InstructionHandle newHandle = entry.getValue();
            if (newHandle == null) continue;

            // TODO support updating the callstrings too
            // TODO this does NOT update stackPtr,.. in the new context!

            ContextMap<CallString, Pair<ValueMapping, ValueMapping>> value = bounds.get(oldHandle);
            if (value != null) bounds.put(newHandle, value.copy(newContainer));

            ContextMap<CallString, Interval> value1 = arrayIndices.get(oldHandle);
            if (value1 != null) arrayIndices.put(newHandle, value1.copy(newContainer));

            ContextMap<CallString, Integer> value2 = scopes.get(oldHandle);
            if (value2 != null) scopes.put(newHandle, value2.copy(newContainer));

            ContextMap<CallString, Interval[]> value3 = sizes.get(oldHandle);
            if (value3 != null) sizes.put(newHandle, value3.copy(newContainer));

            ContextMap<CallString,Set<FlowEdge>> old = infeasibles.get(oldHandle);
            if (old != null) {
                Map<CallString,Set<FlowEdge>> map = new LinkedHashMap<CallString, Set<FlowEdge>>(old.size());
                for (CallString cs : old.keySet()) {
                    Set<FlowEdge> newSet = new LinkedHashSet<FlowEdge>();
                    for (FlowEdge edge : old.get(cs)) {
                        InstructionHandle newHead = newHandles.get(edge.getHead());
                        InstructionHandle newTail = newHandles.get(edge.getTail());
                        if (newHead != null && newTail != null) {
                            Context ctx = new Context(edge.getContext());
                            ctx.setMethodInfo(newContainer);
                            newSet.add(new FlowEdge(newTail, newHead, edge.getType(), ctx));
                        }
                    }
                    map.put(cs,newSet);
                }

                Context c = old.getContext();
                c.setMethodInfo(newContainer);
                ContextMap<CallString,Set<FlowEdge>> edges =
                        new ContextMap<CallString, Set<FlowEdge>>(c, map);
                infeasibles.put(newHandle, edges);
            }
        }
    }
}
