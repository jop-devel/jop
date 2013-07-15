/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Wolfgang Puffitsch
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
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.misc.MissingClassError;
import com.jopdesign.common.type.FieldRef;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.AnalysisResultSerialization;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.dfa.framework.Interpreter;
import com.jopdesign.dfa.framework.MethodHelper;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallStringReceiverTypes implements Analysis<CallString, Set<TypeMapping>> {

    private final int callStringLength;

    public static final String NAME = "CallStringReceiverTypes";
    public static final Logger logger = Logger.getLogger(DFATool.LOG_DFA_ANALYSES+"."+NAME);

    public CallStringReceiverTypes(int callStringLength) {
        this.callStringLength = callStringLength;
    }

	public String getId() {
		return NAME + "-" + callStringLength;
	}

    private Map<String, ContextMap<CallString, Set<TypeMapping>>> threads = new LinkedHashMap<String, ContextMap<CallString, Set<TypeMapping>>>();
    private Map<InstructionHandle, ContextMap<CallString, Set<String>>> targets = new LinkedHashMap<InstructionHandle, ContextMap<CallString, Set<String>>>();

    public ContextMap<CallString, Set<TypeMapping>> bottom() {
        return null;
    }

    public ContextMap<CallString, Set<TypeMapping>> initial(InstructionHandle stmt) {
        ContextMap<CallString, Set<TypeMapping>> init =
                new ContextMap<CallString, Set<TypeMapping>>(new Context(), new LinkedHashMap<CallString, Set<TypeMapping>>());

        Set<TypeMapping> s = new LinkedHashSet<TypeMapping>();
        s.add(new TypeMapping("com.jopdesign.io.IOFactory.sp", "com.jopdesign.io.SerialPort"));
        s.add(new TypeMapping("com.jopdesign.io.IOFactory.sys", "com.jopdesign.io.SysDevice"));

        init.put(CallString.EMPTY, s);

        return init;
    }

    public void initialize(MethodInfo mi, Context context) {
        String sig = mi.getFQMethodName();
        threads.put(sig, new ContextMap<CallString, Set<TypeMapping>>(context, new LinkedHashMap<CallString, Set<TypeMapping>>()));
    }

    public ContextMap<CallString, Set<TypeMapping>> join(ContextMap<CallString, Set<TypeMapping>> s1, ContextMap<CallString, Set<TypeMapping>> s2) {

        if (s1 == null) {
            return new ContextMap<CallString, Set<TypeMapping>>(s2);
        }

        if (s2 == null) {
            return new ContextMap<CallString, Set<TypeMapping>>(s1);
        }

        ContextMap<CallString, Set<TypeMapping>> result = new ContextMap<CallString, Set<TypeMapping>>(new Context(s2.getContext()), new LinkedHashMap<CallString, Set<TypeMapping>>());
        result.putAll(s1);
        result.putAll(s2);

        Set<TypeMapping> a = s1.get(s2.getContext().callString);
        Set<TypeMapping> b = s2.get(s2.getContext().callString);

        Set<TypeMapping> merged = new LinkedHashSet<TypeMapping>();
        if (a != null) {
            merged.addAll(a);
        }

        if (b == null) {
        	logger.error("RT join: failed to find DF information for "+
        			     s2.getContext().callString.toStringVerbose(false));
        }

        merged.addAll(b);
        result.put(s2.getContext().callString, merged);

        if (result.getContext().stackPtr < 0) {
            result.getContext().stackPtr = s2.getContext().stackPtr;
        }
        if (result.getContext().syncLevel < 0) {
            result.getContext().syncLevel = s2.getContext().syncLevel;
        }
        result.getContext().threaded = Context.isThreaded();

        return result;
    }

    public boolean compare(ContextMap<CallString, Set<TypeMapping>> s1, ContextMap<CallString, Set<TypeMapping>> s2) {

        if (s1 == null || s2 == null) {
            return false;
        }

        if (!s1.getContext().equals(s2.getContext())) {
            return false;
        } else {

            Set<TypeMapping> a = s1.get(s1.getContext().callString);
            Set<TypeMapping> b = s2.get(s1.getContext().callString);

            if (a == null || b == null) {
                return false;
            }

            if (!b.containsAll(a)) {
                return false;
            }
        }

        return true;
    }

    public ContextMap<CallString, Set<TypeMapping>> transfer(
            InstructionHandle stmt, FlowEdge edge,
            ContextMap<CallString, Set<TypeMapping>> input,
            Interpreter<CallString, Set<TypeMapping>> interpreter,
            Map<InstructionHandle, ContextMap<CallString, Set<TypeMapping>>> state) {

        Context context = new Context(input.getContext());
        Set<TypeMapping> in = input.get(context.callString);
        ContextMap<CallString, Set<TypeMapping>> retval = new ContextMap<CallString, Set<TypeMapping>>(context, new LinkedHashMap<CallString, Set<TypeMapping>>());

        Set<TypeMapping> result = new LinkedHashSet<TypeMapping>();
        retval.put(context.callString, result);

        Instruction instruction = stmt.getInstruction();

// 		if (context.method.startsWith("ttpa.protocol.MultiPartner")
// 			|| context.method.startsWith("java.util.LinkedHashMap.put")
// 			// || context.method.startsWith("java.util.Stack")
// 			) {
// 			System.out.println(context.method+": "+stmt+" / "+context.callString.asList());
// 			System.out.print(stmt.getInstruction()+":\t{ ");
// 			System.out.print(context.callString.asList()+": "+input.get(context.callString));
// 			System.out.println(" }");
// 		}

        switch (instruction.getOpcode()) {

            case Constants.NOP:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.ACONST_NULL:
            case Constants.ICONST_M1:
            case Constants.ICONST_0:
            case Constants.ICONST_1:
            case Constants.ICONST_2:
            case Constants.ICONST_3:
            case Constants.ICONST_4:
            case Constants.ICONST_5:
            case Constants.BIPUSH:
            case Constants.SIPUSH:
            case Constants.FCONST_0:
            case Constants.FCONST_1:
            case Constants.FCONST_2:
            case Constants.LCONST_0:
            case Constants.LCONST_1:
            case Constants.DCONST_0:
            case Constants.DCONST_1:
            case Constants.LDC2_W:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.LDC:
            case Constants.LDC_W: {
                LDC instr = (LDC) instruction;
                result = new LinkedHashSet<TypeMapping>(in);
                retval.put(context.callString, result);
                Type type = instr.getType(context.constPool());
                if (type.equals(Type.STRING)) {
                    result.add(new TypeMapping(context.stackPtr, type.toString()));
                    String value = type.toString() + ".value";
                    String name = "char[]";
                    name += "@" + context.method() + ":" + stmt.getPosition();
                    result.add(new TypeMapping(value, name));
                }
            }
            break;

            case Constants.DUP: {
                for (TypeMapping m : in) {
                    result.add(m);
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(context.stackPtr, m.type));
                    }
                }
            }
            break;
            case Constants.DUP_X1: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 2) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(context.stackPtr - 2, m.type));
                        result.add(new TypeMapping(context.stackPtr, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 2) {
                        result.add(new TypeMapping(context.stackPtr - 1, m.type));
                    }
                }
            }
            break;
            case Constants.DUP_X2: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 3) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(context.stackPtr - 3, m.type));
                        result.add(new TypeMapping(context.stackPtr, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 2) {
                        result.add(new TypeMapping(context.stackPtr - 1, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 3) {
                        result.add(new TypeMapping(context.stackPtr - 2, m.type));
                    }
                }
            }
            break;
            case Constants.DUP2: {
                for (TypeMapping m : in) {
                    result.add(m);
                    if (m.stackLoc == context.stackPtr - 2) {
                        result.add(new TypeMapping(context.stackPtr, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(context.stackPtr + 1, m.type));
                    }
                }
            }
            break;

            case Constants.DUP2_X1: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 3) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 2) {
                        result.add(new TypeMapping(context.stackPtr - 3, m.type));
                        result.add(new TypeMapping(context.stackPtr, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(context.stackPtr - 2, m.type));
                        result.add(new TypeMapping(context.stackPtr + 1, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 3) {
                        result.add(new TypeMapping(context.stackPtr - 1, m.type));
                    }
                }
            }
            break;

            case Constants.DUP2_X2: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 3) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 2) {
                        result.add(new TypeMapping(context.stackPtr - 4, m.type));
                        result.add(new TypeMapping(context.stackPtr, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(context.stackPtr - 3, m.type));
                        result.add(new TypeMapping(context.stackPtr + 1, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 4) {
                        result.add(new TypeMapping(context.stackPtr - 2, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 3) {
                        result.add(new TypeMapping(context.stackPtr - 1, m.type));
                    }
                }
            }
            break;

            case Constants.SWAP: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 2) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 2) {
                        result.add(new TypeMapping(context.stackPtr - 1, m.type));
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(context.stackPtr - 2, m.type));
                    }
                }
            }
            break;

            case Constants.POP:
                filterSet(in, result, context.stackPtr - 1);
                break;
            case Constants.POP2:
                filterSet(in, result, context.stackPtr - 2);
                break;

            case Constants.GETFIELD: {

                GETFIELD instr = (GETFIELD) instruction;
                List<String> receivers = new LinkedList<String>();

                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 1) {
                        result.add(m);
                    } else if (m.stackLoc == context.stackPtr - 1) {
                        receivers.add(m.type);
                    }
                }

//			if (receivers.isEmpty()
//				&& instr.getFieldType(context.constPool) != Type.INT) {
//				System.out.println("GETFIELD not found: "+context.method+"@"+stmt+": "+instr.getFieldName(context.constPool));
////				System.exit(1);
//			}

                DFATool p = interpreter.getDFATool();

                String fieldName = instr.getFieldName(context.constPool());
                for (String receiver : receivers) {
                    //String heapLoc = receiver + "." + instr.getFieldName(context.constPool);
                    //String namedLoc = receiver.split("@")[0] + "." + instr.getFieldName(context.constPool);
                    String className = receiver.split("@")[0];
                    ClassInfo classInfo = p.classForField(className, fieldName);
                    if (classInfo != null) {
                        String heapLoc = classInfo.getClassName() + "." + fieldName;
                        recordReceiver(stmt, context, heapLoc);
                        for (TypeMapping m : in) {
                            if (heapLoc.equals(m.heapLoc)) {
                                result.add(new TypeMapping(context.stackPtr - 1, m.type));
                            }
                        }
                    }
                }
            }
            break;

            case Constants.PUTFIELD: {

                PUTFIELD instr = (PUTFIELD) instruction;
                List<String> receivers = new LinkedList<String>();

                int fieldSize = instr.getFieldType(context.constPool()).getSize();

                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 1 - fieldSize) {
                        result.add(m);
                    } else if (m.stackLoc == context.stackPtr - 1 - fieldSize) {
                        receivers.add(m.type);
                    }
                }

//			if (receivers.isEmpty()) {
//				System.out.println("PUTFIELD not found: "+context.method+"@"+stmt+": "+instr.getFieldName(context.constPool));
////				System.exit(-1);
//			}

                DFATool p = interpreter.getDFATool();

                String fieldName = instr.getFieldName(context.constPool());
                for (String receiver : receivers) {
                    //String heapLoc = receiver + "." + instr.getFieldName(context.constPool);
                    //String namedLoc = receiver.split("@")[0] + "." + instr.getFieldName(context.constPool);
                    String className = receiver.split("@")[0];
                    ClassInfo classInfo = p.classForField(className, fieldName);
                    if (classInfo != null) {
                        String heapLoc = classInfo.getClassName() + "." + fieldName;
                        recordReceiver(stmt, context, heapLoc);
                        for (TypeMapping m : in) {
                            if (!heapLoc.equals(m.heapLoc) || context.threaded) {
                                result.add(m);
                            }
                            if (m.stackLoc == context.stackPtr - 1) {
                                result.add(new TypeMapping(heapLoc, m.type));
                            }
                        }
                    }
                }

                if (instr.getFieldType(context.constPool()) instanceof ReferenceType) {
                    doInvokeStatic("com.jopdesign.sys.JVM.f_putfield_ref(III)V", stmt, context, input, interpreter, state, retval);
                }
            }
            break;

            case Constants.GETSTATIC: {

                GETSTATIC instr = (GETSTATIC) instruction;

                DFATool p = interpreter.getDFATool();
                FieldRef ref = context.getMethodInfo().getCode().getFieldRef(instr);

                String fieldName = ref.getName();
                String className = ref.getClassName();
                ClassInfo classInfo = p.classForField(className, fieldName);

                if (classInfo != null) {
                    String heapLoc = classInfo.getClassName() + "." + fieldName;
                    recordReceiver(stmt, context, heapLoc);
                    for (TypeMapping m : in) {
                        if (m.stackLoc < context.stackPtr) {
                            result.add(m);
                        }
                        if (heapLoc.equals(m.heapLoc)) {
                            result.add(new TypeMapping(context.stackPtr, m.type));
                        }
                    }
                } else {
//				System.out.println("GETSTATIC not found: "+heapLoc);
//				System.exit(1);
                }

            }
            break;

            case Constants.PUTSTATIC: {

                PUTSTATIC instr = (PUTSTATIC) instruction;

                DFATool p = interpreter.getDFATool();
                FieldRef ref = context.getMethodInfo().getCode().getFieldRef(instr);

                String fieldName = ref.getName();
                String className = ref.getClassName();
                ClassInfo classInfo = p.classForField(className, fieldName);

                if (classInfo != null) {
                    String heapLoc = classInfo.getClassName() + "." + fieldName;
                    recordReceiver(stmt, context, heapLoc);
                    for (TypeMapping m : in) {
                        if (m.stackLoc >= 0 && m.stackLoc < context.stackPtr - 1) {
                            result.add(m);
                        } else if (m.stackLoc == -1 && (!heapLoc.equals(m.heapLoc) || context.threaded)) {
                            result.add(m);
                        }
                        if (m.stackLoc == context.stackPtr - 1) {
                            result.add(new TypeMapping(heapLoc, m.type));
                        }
                    }
                } else {
//				System.out.println("PUTSTATIC not found: "+heapLoc);
//				System.exit(1);
                }

                if (instr.getFieldType(context.constPool()) instanceof ReferenceType) {
                    doInvokeStatic("com.jopdesign.sys.JVM.f_putstatic_ref(II)V", stmt, context, input, interpreter, state, retval);
                }
            }
            break;

            case Constants.ARRAYLENGTH:
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 1) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        recordReceiver(stmt, context, m.type);
                    }
                }
                break;

            case Constants.IASTORE:
            case Constants.BASTORE:
            case Constants.CASTORE:
            case Constants.SASTORE:
            case Constants.FASTORE: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 3) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 3) {
                        recordReceiver(stmt, context, m.type);
                    }
                }
            }
            break;

            case Constants.LASTORE:
            case Constants.DASTORE: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 4) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 4) {
                        recordReceiver(stmt, context, m.type);
                    }
                }
            }
            break;

            case Constants.AASTORE: {

                List<String> receivers = new LinkedList<String>();

                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 3) {
                        result.add(m);
                    } else if (m.stackLoc == context.stackPtr - 3) {
                        receivers.add(m.type);
                        recordReceiver(stmt, context, m.type);
                    }
                }

//			if (receivers.isEmpty()) {
//				System.out.println("AASTORE not found: "+context.method+"@"+stmt);
////				System.exit(-1);
//			}

                for (String healLoc : receivers) {
                    for (TypeMapping m : in) {
                        if (m.stackLoc == context.stackPtr - 1) {
                            result.add(new TypeMapping(healLoc, m.type));
                        }
                    }
                }

                doInvokeStatic("com.jopdesign.sys.JVM.f_aastore(III)V", stmt, context, input, interpreter, state, retval);
            }
            break;

            case Constants.IALOAD:
            case Constants.BALOAD:
            case Constants.CALOAD:
            case Constants.SALOAD:
            case Constants.FALOAD: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 2) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 2) {
                        recordReceiver(stmt, context, m.type);
                    }
                }
            }
            break;

            case Constants.LALOAD:
            case Constants.DALOAD: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 2) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 2) {
                        recordReceiver(stmt, context, m.type);
                    }
                }
            }
            break;

            case Constants.AALOAD: {

                List<String> receivers = new LinkedList<String>();

                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 2) {
                        result.add(m);
                    } else if (m.stackLoc == context.stackPtr - 2) {
                        receivers.add(m.type);
                        recordReceiver(stmt, context, m.type);
                    }
                }

//			if (receivers.isEmpty()) {
//					System.out.println("AALOAD not found: "+context.method+"@"+stmt);
////					System.exit(1);
//			}

                for (String heapLoc : receivers) {
                    for (TypeMapping m : in) {
                        if (heapLoc.equals(m.heapLoc)) {
                            result.add(new TypeMapping(context.stackPtr - 2, m.type));
                        }
                    }
                }
            }
            break;

            case Constants.NEW: {
                NEW instr = (NEW) instruction;
                filterSet(in, result, context.stackPtr);
                String name = instr.getType(context.constPool()).toString();
                name += "@" + context.method() + ":" + stmt.getPosition();
                result.add(new TypeMapping(context.stackPtr, name));
                doInvokeStatic("com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(I)I", stmt, context, input, interpreter, state, retval);
            }
            break;

            case Constants.ANEWARRAY: {
                ANEWARRAY instr = (ANEWARRAY) instruction;
                filterSet(in, result, context.stackPtr - 1);
                String name = instr.getType(context.constPool()).toString() + "[]";
                name += "@" + context.method() + ":" + stmt.getPosition();
                result.add(new TypeMapping(context.stackPtr - 1, name));
                doInvokeStatic("com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(II)I", stmt, context, input, interpreter, state, retval);
            }
            break;

            case Constants.NEWARRAY: {
                NEWARRAY instr = (NEWARRAY) instruction;
                filterSet(in, result, context.stackPtr - 1);
                String name = instr.getType().toString();
                name += "@" + context.method() + ":" + stmt.getPosition();
                result.add(new TypeMapping(context.stackPtr - 1, name));
                doInvokeStatic("com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(II)I", stmt, context, input, interpreter, state, retval);
            }
            break;

            case Constants.MULTIANEWARRAY: {
                MULTIANEWARRAY instr = (MULTIANEWARRAY) instruction;
                int dim = instr.getDimensions();

                filterSet(in, result, context.stackPtr - dim);

                String type = instr.getType(context.constPool()).toString();
                type = type.substring(0, type.indexOf("["));

                for (int i = 1; i <= dim; i++) {
                    String type1 = type;
                    String type2 = type;
                    for (int k = 0; k < i; k++) {
                        type1 += "[]";
                    }
                    for (int k = 0; k < i - 1; k++) {
                        type2 += "[]";
                    }
                    type1 += "@" + context.method() + ":" + stmt.getPosition();
                    type2 += "@" + context.method() + ":" + stmt.getPosition();
                    result.add(new TypeMapping(type1, type2));
                }

                String name = instr.getType(context.constPool()).toString();
                name += "@" + context.method() + ":" + stmt.getPosition();
                result.add(new TypeMapping(context.stackPtr - dim, name));

                doInvokeStatic("com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "()I", stmt, context, input, interpreter, state, retval);
            }
            break;

            case Constants.ILOAD_0:
            case Constants.ILOAD_1:
            case Constants.ILOAD_2:
            case Constants.ILOAD_3:
            case Constants.ILOAD:
            case Constants.FLOAD_0:
            case Constants.FLOAD_1:
            case Constants.FLOAD_2:
            case Constants.FLOAD_3:
            case Constants.FLOAD:
            case Constants.LLOAD_0:
            case Constants.LLOAD_1:
            case Constants.LLOAD_2:
            case Constants.LLOAD_3:
            case Constants.LLOAD:
            case Constants.DLOAD_0:
            case Constants.DLOAD_1:
            case Constants.DLOAD_2:
            case Constants.DLOAD_3:
            case Constants.DLOAD:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.ALOAD_0:
            case Constants.ALOAD_1:
            case Constants.ALOAD_2:
            case Constants.ALOAD_3:
            case Constants.ALOAD: {

                LoadInstruction instr = (LoadInstruction) instruction;

                int index = instr.getIndex();

                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr) {
                        result.add(m);
                    }
                    if (m.stackLoc == index) {
                        result.add(new TypeMapping(context.stackPtr, m.type));
                    }
                }
            }
            break;

            case Constants.ISTORE_0:
            case Constants.ISTORE_1:
            case Constants.ISTORE_2:
            case Constants.ISTORE_3:
            case Constants.ISTORE:
            case Constants.FSTORE_0:
            case Constants.FSTORE_1:
            case Constants.FSTORE_2:
            case Constants.FSTORE_3:
            case Constants.FSTORE:
            case Constants.LSTORE_0:
            case Constants.LSTORE_1:
            case Constants.LSTORE_2:
            case Constants.LSTORE_3:
            case Constants.LSTORE:
            case Constants.DSTORE_0:
            case Constants.DSTORE_1:
            case Constants.DSTORE_2:
            case Constants.DSTORE_3:
            case Constants.DSTORE:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.ASTORE_0:
            case Constants.ASTORE_1:
            case Constants.ASTORE_2:
            case Constants.ASTORE_3:
            case Constants.ASTORE: {

                StoreInstruction instr = (StoreInstruction) instruction;

                int index = instr.getIndex();

                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 1
                            && m.stackLoc != index) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(index, m.type));
                    }
                }
            }
            break;

            case Constants.FCMPL:
            case Constants.FCMPG:
            case Constants.LCMP:
            case Constants.DCMPL:
            case Constants.DCMPG:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.IFEQ:
            case Constants.IFNE:
            case Constants.IFLT:
            case Constants.IFGE:
            case Constants.IFGT:
            case Constants.IFLE:
            case Constants.IFNULL:
            case Constants.IFNONNULL:
                filterSet(in, result, context.stackPtr - 1);
                break;

            case Constants.IF_ICMPEQ:
            case Constants.IF_ICMPNE:
            case Constants.IF_ICMPLT:
            case Constants.IF_ICMPGE:
            case Constants.IF_ICMPGT:
            case Constants.IF_ICMPLE:
            case Constants.IF_ACMPEQ:
            case Constants.IF_ACMPNE:
                filterSet(in, result, context.stackPtr - 2);
                break;

            case Constants.TABLESWITCH:
            case Constants.LOOKUPSWITCH:
                filterSet(in, result, context.stackPtr - 1);
                break;

            case Constants.GOTO:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.IADD:
            case Constants.ISUB:
            case Constants.IMUL:
            case Constants.IDIV:
            case Constants.IREM:
            case Constants.ISHL:
            case Constants.ISHR:
            case Constants.IUSHR:
            case Constants.IAND:
            case Constants.IOR:
            case Constants.IXOR:
            case Constants.FADD:
            case Constants.FSUB:
            case Constants.FMUL:
            case Constants.FDIV:
            case Constants.FREM:
            case Constants.DADD:
            case Constants.DSUB:
            case Constants.DMUL:
            case Constants.DDIV:
            case Constants.DREM:
            case Constants.IINC:
            case Constants.INEG:
            case Constants.FNEG:
            case Constants.LNEG:
            case Constants.DNEG:
            case Constants.LADD:
            case Constants.LAND:
            case Constants.LOR:
            case Constants.LXOR:
            case Constants.LSUB:
            case Constants.LSHL:
            case Constants.LSHR:
            case Constants.LUSHR:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.LMUL:
            case Constants.LDIV:
            case Constants.LREM:
                result = new LinkedHashSet<TypeMapping>(in);
                retval.put(context.callString, result);
                doInvokeStatic("com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(JJ)J", stmt, context, input, interpreter, state, retval);
                break;

            case Constants.I2B:
            case Constants.I2C:
            case Constants.I2S:
            case Constants.I2F:
            case Constants.F2I:
            case Constants.I2L:
            case Constants.I2D:
            case Constants.F2L:
            case Constants.F2D:
            case Constants.L2I:
            case Constants.D2I:
            case Constants.L2F:
            case Constants.D2F:
            case Constants.L2D:
            case Constants.D2L:
                result = in;
                retval.put(context.callString, result);
                break;

            case Constants.INSTANCEOF:
                filterSet(in, result, context.stackPtr - 1);
                break;

            case Constants.CHECKCAST: {

                DFATool p = interpreter.getDFATool();
                CHECKCAST instr = (CHECKCAST) instruction;

                for (TypeMapping m : in) {
                    if (m.stackLoc < context.stackPtr - 1) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        // check whether this class can possibly be cast
                        String constClassName = instr.getType(context.constPool()).toString();
                        ClassInfo staticClass = p.getAppInfo().getClassInfo(constClassName);
                        ClassInfo dynamicClass = p.getAppInfo().getClassInfo(m.type.split("@")[0]);
//					System.out.println("CHECKCAST: "+context.callString.asList()+"/"+context.method+": "+stmt+": "+constClassName+" vs "+m.type);
                        if (dynamicClass.isSubclassOf(staticClass)) {
                            result.add(m);
//							System.out.println("yay!");
                        }
                    }
                }
            }
            break;

            case Constants.MONITORENTER:
                filterSet(in, result, context.stackPtr - 1);
                context.syncLevel++;
                break;

            case Constants.MONITOREXIT:
                filterSet(in, result, context.stackPtr - 1);
                context.syncLevel--;
                if (context.syncLevel < 0) {
                    System.err.println("Synchronization level mismatch.");
                    System.exit(-1);
                }
                break;

            case Constants.INVOKEVIRTUAL:
            case Constants.INVOKEINTERFACE: {

                InvokeInstruction instr = (InvokeInstruction) instruction;
                int argSize = MethodHelper.getArgSize(instr, context.constPool());

                DFATool p = interpreter.getDFATool();
                MethodRef ref = context.getMethodInfo().getCode().getInvokeSite(stmt).getInvokeeRef();
                ClassInfo constClass = ref.getClassInfo();

                // find possible receiver types
                List<String> receivers = new LinkedList<String>();
                for (TypeMapping m : in) {
                    if (m.stackLoc == context.stackPtr - argSize) {
                        String clName = m.type.split("@")[0];
                        ClassInfo dynamicClass;
                        // check whether this class can possibly be a receiver
                        try {
                            dynamicClass = p.getAppInfo().getClassInfo(clName);
                        } catch (MissingClassError ex) {
                            logger.error("TRANSFER/" + instr + ": " + ex);
                            continue;
                        }

                        if ((instr instanceof INVOKEVIRTUAL
                                && dynamicClass.isSubclassOf(constClass))
                                || (instr instanceof INVOKEINTERFACE
                                && dynamicClass.isImplementationOf(constClass))) {
                            receivers.add(clName);
                        } else {
                            logger.debug(context.method() + ": class " + constClass + " is not a superclass of " + clName);
                        }
                    }
                }

                /* Get the actual set of invoked methods */
                //ArrayList<MethodInfo> invokeCandidates = new ArrayList<MethodInfo>();
                String signature = ref.getMethodSignature();
                for (String receiver : receivers) {
                    // find receiving method
                    MethodInfo impl = p.getAppInfo().getMethodInfoInherited(receiver, signature);
                    if (impl == null) {
                        throw new AppInfoError("Could not find implementation for: " + receiver + "#" + signature);
                    }
                    doInvokeVirtual(receiver, impl, stmt, context, input, interpreter, state, retval);
                }

                // add relevant information to result
                filterSet(in, result, context.stackPtr - argSize);
            }
            break;

            case Constants.INVOKESTATIC:
            case Constants.INVOKESPECIAL: {

                InvokeInstruction instr = (InvokeInstruction) instruction;
                int argSize = MethodHelper.getArgSize(instr, context.constPool());

                MethodRef ref = context.getMethodInfo().getCode().getInvokeSite(stmt).getInvokeeRef();
                MethodInfo impl = ref.getMethodInfo();
                if (impl == null) {
                    throw new AppInfoError("Cannot find implementation of " + ref);
                } else if (impl.isPrivate() && !impl.isStatic()) {
                    doInvokeVirtual(ref.getClassName(), impl, stmt, context, input, interpreter, state, retval);
                } else {
                    doInvokeStatic(impl.getMemberID().toString(), stmt, context, input, interpreter, state, retval);
                }

                // add relevant information to result
                filterSet(in, result, context.stackPtr - argSize);
            }
            break;

            case Constants.RETURN:
            case Constants.IRETURN:
            case Constants.FRETURN:
            case Constants.LRETURN:
            case Constants.DRETURN:
                filterSet(in, result, 0);
                break;

            case Constants.ARETURN: {
                for (TypeMapping m : in) {
                    if (m.stackLoc < 0) {
                        result.add(m);
                    }
                    if (m.stackLoc == context.stackPtr - 1) {
                        result.add(new TypeMapping(0, m.type));
                    }
                }
            }
            break;

            default:
                System.out.println("Unknown opcode: " + instruction.toString(context.constPool().getConstantPool()));
                System.exit(-1);
        }

//		System.out.print(instruction+":\t{ ");
//		for (Iterator k = result.iterator(); k.hasNext(); ) {
//			ReceiverTypes.TypeMapping m = (ReceiverTypes.TypeMapping) k.next();
//			if (m.stackLoc >= 0) {
//				System.out.print("<stack[" + m.stackLoc + "], " + m.type +">, ");
//			} else {
//				System.out.print("<" + m.heapLoc + ", " + m.type +">, ");						
//			}
//		}
//		System.out.println("}");				

//		System.out.println("AFTER "+context.method+": "+stmt+" / "+context.callString.asList());
//		System.out.print(stmt.getInstruction()+":\t{ ");
//		System.out.print(context.callString.asList()+": "+retval.get(context.callString));
//		System.out.println(" }");

        context.stackPtr += instruction.produceStack(context.constPool()) - instruction.consumeStack(context.constPool());

        return retval;
    }

    private void filterSet(Set<TypeMapping> input, Set<TypeMapping> result, int bound) {
        for (TypeMapping m : input) {
            if (m.stackLoc < bound) {
                result.add(m);
            }
        }
    }

    private void filterReturnSet(Set<TypeMapping> input, Set<TypeMapping> result, int varPtr) {
        if (input != null) {
            for (TypeMapping m : input) {
                if (m.stackLoc < 0) {
                    result.add(m);
                }
                if (m.stackLoc >= 0) {
                    TypeMapping t = new TypeMapping(m.stackLoc + varPtr, m.type);
                    result.add(t);
                }
            }
        }
    }

    private void doInvokeVirtual(String receiver, MethodInfo method,
                                 InstructionHandle stmt, Context context,
                                 ContextMap<CallString, Set<TypeMapping>> input,
                                 Interpreter<CallString, Set<TypeMapping>> interpreter,
                                 Map<InstructionHandle, ContextMap<CallString, Set<TypeMapping>>> state,
                                 ContextMap<CallString, Set<TypeMapping>> result) {

        String methodImplName = method.getClassName() + "." + method.getMethodSignature();

        recordReceiver(stmt, context, methodImplName);

//		LineNumberTable lines = p.getMethods().get(context.method).getLineNumberTable(context.constPool);
//		int sourceLine = lines.getSourceLine(stmt.getPosition());
//		String invokeId = context.method+"\t"+":"+sourceLine+"."+stmt.getPosition();

        // set up new context
        int varPtr = context.stackPtr - MethodHelper.getArgSize(method);
        Context c = new Context(context);
        c.stackPtr = method.getCode().getMaxLocals();
        if (method.isSynchronized()) {
            c.syncLevel = context.syncLevel + 1;
        }
        c.setMethodInfo(method);
        c.callString = c.callString.push(context.getMethodInfo(), stmt, callStringLength);

        boolean threaded = false;

        DFATool p = interpreter.getDFATool();
        if (p.getAppInfo().getClassInfo(receiver).isSubclassOf(p.getAppInfo().getClassInfo("joprt.RtThread")) &&
                "run()V".equals(method.getMethodSignature())) {
            c.createThread();
            threaded = true;
        }

        // carry only minimal information with call
        Set<TypeMapping> in = input.get(context.callString);
        Set<TypeMapping> out = new LinkedHashSet<TypeMapping>();

        ContextMap<CallString, Set<TypeMapping>> tmpresult = new ContextMap<CallString, Set<TypeMapping>>(c, new LinkedHashMap<CallString, Set<TypeMapping>>());
        tmpresult.put(c.callString, out);

        for (TypeMapping m : in) {
            if (m.stackLoc < 0) {
                out.add(m);
            }
            if (m.stackLoc > varPtr) {
                out.add(new TypeMapping(m.stackLoc - varPtr, m.type));
            }
            if (m.stackLoc == varPtr) {
                // add "this"
                ClassInfo staticClass = p.getAppInfo().getClassInfo(receiver);
                ClassInfo dynamicClass = null;
                try {
                    dynamicClass = p.getAppInfo().getClassInfo(m.type.split("@")[0]);
                } catch (MissingClassError ex) {
                    logger.error("doInvokeVirtual - trying to improve type of " + staticClass + ": " + ex);
                    // TODO: maybe we should throw an exception/error instead?
                }
                if (dynamicClass != null && dynamicClass.isSubclassOf(staticClass)) {
                    out.add(new TypeMapping(0, m.type));
                }
            }
        }

        InstructionHandle entry = p.getEntryHandle(method);
        state.put(entry, join(state.get(entry), tmpresult));

        // interpret method
        Map<InstructionHandle, ContextMap<CallString, Set<TypeMapping>>> r = interpreter.interpret(c, entry, state, false);

        // pull out relevant information from call
        InstructionHandle exit = p.getExitHandle(method);
        if (r.get(exit) != null) {
            Set<TypeMapping> returned = r.get(exit).get(c.callString);
            if (returned != null) {
                filterReturnSet(returned, result.get(context.callString), varPtr);
            }
        }

        // update all threads
        if (threaded) {
            threads.put(methodImplName, new ContextMap<CallString, Set<TypeMapping>>(c, result));
            updateThreads(result, interpreter, state);
        }
    }

    private void doInvokeStatic(String methodName,
                                InstructionHandle stmt,
                                Context context,
                                ContextMap<CallString, Set<TypeMapping>> input,
                                Interpreter<CallString, Set<TypeMapping>> interpreter,
                                Map<InstructionHandle, ContextMap<CallString, Set<TypeMapping>>> state,
                                ContextMap<CallString, Set<TypeMapping>> result) {

        DFATool p = interpreter.getDFATool();
        MethodInfo method = p.getMethod(methodName);
        if (method == null) {
            throw new AppInfoError("DFA: cannot find static method " + methodName);
        }
        //noinspection AssignmentToMethodParameter
        methodName = method.getClassName() + "." + method.getMethodSignature();

        recordReceiver(stmt, context, methodName);

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
            c.callString = c.callString.push(context.getMethodInfo(), stmt, callStringLength);

            // carry only minimal information with call
            Set<TypeMapping> in = input.get(context.callString);
            Set<TypeMapping> out = new LinkedHashSet<TypeMapping>();

            ContextMap<CallString, Set<TypeMapping>> tmpresult = new ContextMap<CallString, Set<TypeMapping>>(c, new LinkedHashMap<CallString, Set<TypeMapping>>());
            tmpresult.put(c.callString, out);

            for (TypeMapping m : in) {
                if (m.stackLoc < 0) {
                    out.add(m);
                }
                if (m.stackLoc >= varPtr) {
                    out.add(new TypeMapping(m.stackLoc - varPtr, m.type));
                }
            }

            InstructionHandle entry = p.getEntryHandle(method);
            state.put(entry, join(state.get(entry), tmpresult));

            // interpret method
            Map<InstructionHandle, ContextMap<CallString, Set<TypeMapping>>> r = interpreter.interpret(c, entry, state, false);

            // pull out relevant information from call
            InstructionHandle exit = p.getExitHandle(method);
            if (r.get(exit) != null) {
                Set<TypeMapping> returned = r.get(exit).get(c.callString);
                if (returned != null) {
                    filterReturnSet(returned, result.get(context.callString), varPtr);
                }
            }

        }
    }

    private void updateThreads(Map<CallString, Set<TypeMapping>> input,
                               Interpreter<CallString, Set<TypeMapping>> interpreter,
                               Map<InstructionHandle, ContextMap<CallString, Set<TypeMapping>>> state) {

        DFATool p = interpreter.getDFATool();

        boolean modified = true;
        while (modified) {
            modified = false;

            Map<String, ContextMap<CallString, Set<TypeMapping>>> tmpThreads = new LinkedHashMap<String, ContextMap<CallString, Set<TypeMapping>>>();

            for (String methodName : threads.keySet()) {

                MethodInfo method = p.getMethod(methodName);
                InstructionHandle entry = p.getEntryHandle(method);
                Context c = state.get(entry).getContext();

                int varPtr = c.stackPtr - MethodHelper.getArgSize(method);

                // prepare input information
                ContextMap<CallString, Set<TypeMapping>> threadInput = new ContextMap<CallString, Set<TypeMapping>>(c, new LinkedHashMap<CallString, Set<TypeMapping>>());
                for (CallString cs : input.keySet()) {
                    Set<TypeMapping> s = input.get(cs);
                    Set<TypeMapping> o = new LinkedHashSet<TypeMapping>();
                    filterSet(s, o, 0);
                    threadInput.put(cs, o);
                }
                state.put(entry, join(threadInput, state.get(entry)));
                // save information
                ContextMap<CallString, Set<TypeMapping>> savedResult = threads.get(methodName);

                // interpret thread
                Map<InstructionHandle, ContextMap<CallString, Set<TypeMapping>>> r = interpreter.interpret(c, entry, state, false);

                // pull out relevant information from thread
                InstructionHandle exit = p.getExitHandle(method);
                ContextMap<CallString, Set<TypeMapping>> threadResult;
                if (r.get(exit) != null) {
                    threadResult = new ContextMap<CallString, Set<TypeMapping>>(c, new LinkedHashMap<CallString, Set<TypeMapping>>());
                    threadResult.put(c.callString, new LinkedHashSet<TypeMapping>());
                    Set<TypeMapping> returned = r.get(exit).get(c.callString);
                    filterReturnSet(returned, threadResult.get(c.callString), varPtr);
                } else {
                    threadResult = new ContextMap<CallString, Set<TypeMapping>>(c, new LinkedHashMap<CallString, Set<TypeMapping>>());
                    threadResult.put(c.callString, new LinkedHashSet<TypeMapping>());
                }

                if (!threadResult.equals(savedResult)) {
                    modified = true;
                }

                tmpThreads.put(methodName, threadResult);
            }

            threads = tmpThreads;
        }
    }

    @SuppressWarnings({"LiteralAsArgToStringEquals"})
    private Map<CallString, Set<TypeMapping>> handleNative(MethodInfo method, Context context,
                                                           Map<CallString, Set<TypeMapping>> input, Map<CallString, Set<TypeMapping>> result) {

        String methodId = method.getMemberID().toString();

        Set<TypeMapping> in = input.get(context.callString);
        Set<TypeMapping> out = new LinkedHashSet<TypeMapping>();

        if (methodId.equals("com.jopdesign.sys.Native#rd(I)I")
                || methodId.equals("com.jopdesign.sys.Native#rdMem(I)I")
                || methodId.equals("com.jopdesign.sys.Native#rdIntMem(I)I")
                || methodId.equals("com.jopdesign.sys.Native#getStatic(I)I")
                || methodId.equals("com.jopdesign.sys.Native#toInt(F)I")) {
            filterSet(in, out, context.stackPtr - 1);
        } else if (methodId.equals("com.jopdesign.sys.Native#wr(II)V")
                || methodId.equals("com.jopdesign.sys.Native#wrMem(II)V")
                || methodId.equals("com.jopdesign.sys.Native#wrIntMem(II)V")
                || methodId.equals("com.jopdesign.sys.Native#putStatic(II)V")) {
            filterSet(in, out, context.stackPtr - 2);
        } else if (methodId.equals("com.jopdesign.sys.Native#getSP()I")) {
            filterSet(in, out, context.stackPtr);
        } else if (methodId.equals("com.jopdesign.sys.Native#toInt(Ljava/lang/Object;)I")
                || methodId.equals("com.jopdesign.sys.Native#toObject(I)Ljava/lang/Object;")) {
            filterSet(in, out, context.stackPtr - 1);
        } else if (methodId.equals("com.jopdesign.sys.Native#toIntArray(I)[I")) {
            filterSet(in, out, context.stackPtr - 1);
            String name = "int[]@" + context.method() + ":0";
            TypeMapping map = new TypeMapping(context.stackPtr - 1, name);
            out.add(map);
        } else if (methodId.equals("com.jopdesign.sys.Native#makeLong(II)J")) {
            filterSet(in, out, context.stackPtr - 2);
        } else if (methodId.equals("com.jopdesign.sys.Native#memCopy(III)V")) {
            filterSet(in, out, context.stackPtr - 3);
        } else if (methodId.equals("com.jopdesign.sys.Native#getField(II)I")
                || methodId.equals("com.jopdesign.sys.Native#arrayLoad(II)I")
                || methodId.equals("com.jopdesign.sys.Native#toLong(D)J")
                || methodId.equals("com.jopdesign.sys.Native#toDouble(J)D")) {
            filterSet(in, out, context.stackPtr - 2);
        } else if (methodId.equals("com.jopdesign.sys.Native#putField(III)V")
                || methodId.equals("com.jopdesign.sys.Native#arrayStore(III)V")) {
            filterSet(in, out, context.stackPtr - 3);
        } else if (methodId.equals("com.jopdesign.sys.Native#condMove(IIZ)I")
                || methodId.equals("com.jopdesign.sys.Native#condMoveRef(Ljava/lang/Object;Ljava/lang/Object;Z)Ljava/lang/Object;")) {
            filterSet(in, out, context.stackPtr - 3);
        } else if (methodId.equals("com.jopdesign.sys.Native#invalidate()V")) {
            filterSet(in, out, context.stackPtr);
        } else if (methodId.equals("com.jopdesign.sys.Native#arrayLength(I)I")
                || methodId.equals("com.jopdesign.sys.Native#invoke(I)V")) {
            filterSet(in, out, context.stackPtr - 1);
        } else if (methodId.equals("com.jopdesign.sys.Native#invoke(II)V")) {
            filterSet(in, out, context.stackPtr - 2);
        } else {
            AppInfoError ex = new AppInfoError("Unknown native method: " + methodId);
            logger.error(ex);
            throw ex;
        }

        result.put(context.callString, out);

        return result;
    }

    private void recordReceiver(InstructionHandle stmt, Context context, String target) {
        if (targets.get(stmt) == null) {
            targets.put(stmt, new ContextMap<CallString, Set<String>>(context, new LinkedHashMap<CallString, Set<String>>()));
        }
        if (targets.get(stmt).get(context.callString) == null) {
            targets.get(stmt).put(context.callString, new LinkedHashSet<String>());
        }
        targets.get(stmt).get(context.callString).add(target);
    }

    public Map<InstructionHandle, ContextMap<CallString, Set<String>>> getResult() {
        return targets;
    }

    public void printResult(DFATool program) {
    	AnalysisResultSerialization.fromContextMapResult(getResult()).dump(System.out);
    }

    @Override
	public void serializeResult(File cacheFile) throws IOException {
    	AnalysisResultSerialization.fromContextMapResult(getResult()).serialize(cacheFile);
	}

	public Map<InstructionHandle, ContextMap<CallString, Set<String>>>
	    deSerializeResult(AppInfo appInfo, File cacheFile)
	    throws IOException,ClassNotFoundException, MethodNotFoundException {

		AnalysisResultSerialization<Set<String>> serialization =
    		AnalysisResultSerialization.fromSerialization(cacheFile);
		this.targets = serialization.toContextMapResult(appInfo, null);
    	this.threads = null;
    	return targets;
	}

    @Override
    public void copyResults(MethodInfo newContainer, Map<InstructionHandle, InstructionHandle> newHandles) {
        for (Map.Entry<InstructionHandle,InstructionHandle> entry : newHandles.entrySet()) {
            InstructionHandle oldHandle = entry.getKey();
            InstructionHandle newHandle = entry.getValue();
            if (newHandle == null) continue;

            ContextMap<CallString, Set<String>> value = targets.get(oldHandle);
            // TODO support updating the callstrings too
            // TODO this does NOT update stackPtr,.. in the new context!
            if (value != null) targets.put(newHandle, value.copy(newContainer));
        }
    }
}
