/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.jcopter.analysis;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.type.ConstantDoubleInfo;
import com.jopdesign.common.type.ConstantFieldInfo;
import com.jopdesign.common.type.ConstantFloatInfo;
import com.jopdesign.common.type.ConstantIntegerInfo;
import com.jopdesign.common.type.ConstantLongInfo;
import com.jopdesign.common.type.TypeHelper;
import com.jopdesign.common.type.ValueInfo;
import com.jopdesign.common.type.ValueTable;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;

/**
 * This is a helper class to analyse value mappings of stack and local slots.
 *
 * TODO currently just a bunch of code, needs cleanup and more generic implementation. Maybe use DFA code?
 *      - make DFA to construct FlowGraph on the fly (in Interpreter, use "GraphProvider" instead of DFATool)
 *      - add bottom/top/.. elements to type system
 *      - implement as dfa analysis, no callstrings, no following of invokes, track copies
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ValueMapAnalysis {
    private final MethodInfo methodInfo;
    private final ConstantPoolGen cpg;

    private ValueTable values;

    public ValueMapAnalysis(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
        this.cpg = methodInfo.getConstantPoolGen();

        values = new ValueTable();
    }

    public void loadParameters() {
        values.clear();
        if (!methodInfo.isStatic()) {
            values.addLocalValue(new ValueInfo(methodInfo));
        }
        int i = 0;
        for (Type type : methodInfo.getArgumentTypes()) {
            values.addLocalValue(new ValueInfo(type, i++));
        }
    }

    public void transfer(Instruction instruction) {

        switch (instruction.getOpcode()) {
            case Constants.NOP:
                break;

            case Constants.ACONST_NULL:
                values.push(new ValueInfo(Type.NULL));
                break;

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
                int value = instr.getValue().intValue();
                values.push(new ValueInfo(instr.getType(cpg), new ConstantIntegerInfo(value)));
                break;
            }
            case Constants.LCONST_0:
            case Constants.LCONST_1: {
                ConstantPushInstruction instr = (ConstantPushInstruction) instruction;
                long value = instr.getValue().longValue();
                values.push(new ValueInfo(instr.getType(cpg), new ConstantLongInfo(value)));
                break;
            }

            case Constants.FCONST_0:
            case Constants.FCONST_1:
            case Constants.FCONST_2: {
                ConstantPushInstruction instr = (ConstantPushInstruction) instruction;
                float value = instr.getValue().floatValue();
                values.push(new ValueInfo(instr.getType(cpg), new ConstantFloatInfo(value)));
                break;
            }
            case Constants.DCONST_0:
            case Constants.DCONST_1: {
                ConstantPushInstruction instr = (ConstantPushInstruction) instruction;
                double value = instr.getValue().doubleValue();
                values.push(new ValueInfo(instr.getType(cpg), new ConstantDoubleInfo(value)));
                break;
            }

            case Constants.LDC:
            case Constants.LDC_W:
            case Constants.LDC2_W: {
                CPInstruction instr = (CPInstruction) instruction;
                values.push(new ValueInfo(methodInfo.getClassInfo().getConstantInfo(instr.getIndex())));
                break;
            }

            case Constants.ISTORE_0:
            case Constants.ISTORE_1:
            case Constants.ISTORE_2:
            case Constants.ISTORE_3:
            case Constants.ISTORE:
            case Constants.ASTORE_0:
            case Constants.ASTORE_1:
            case Constants.ASTORE_2:
            case Constants.ASTORE_3:
            case Constants.ASTORE:
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
            case Constants.FSTORE_0:
            case Constants.FSTORE_1:
            case Constants.FSTORE_2:
            case Constants.FSTORE_3:
            case Constants.FSTORE: {
                StoreInstruction instr = (StoreInstruction) instruction;
                int index = instr.getIndex();
                values.setLocalValue(index, values.popValue());
                break;
            }
            
            case Constants.ILOAD_0:
            case Constants.ILOAD_1:
            case Constants.ILOAD_2:
            case Constants.ILOAD_3:
            case Constants.ILOAD:
            case Constants.LLOAD_0:
            case Constants.LLOAD_1:
            case Constants.LLOAD_2:
            case Constants.LLOAD_3:
            case Constants.LLOAD:
            case Constants.FLOAD_0:
            case Constants.FLOAD_1:
            case Constants.FLOAD_2:
            case Constants.FLOAD_3:
            case Constants.FLOAD:
            case Constants.DLOAD_0:
            case Constants.DLOAD_1:
            case Constants.DLOAD_2:
            case Constants.DLOAD_3:
            case Constants.DLOAD:
            case Constants.ALOAD_0:
            case Constants.ALOAD_1:
            case Constants.ALOAD_2:
            case Constants.ALOAD_3:
            case Constants.ALOAD: {
                LoadInstruction instr = (LoadInstruction) instruction;
                int index = instr.getIndex();
                values.push(values.getLocalValue(index));
                break;
            }

            case Constants.DUP:
                values.push(values.top());
                break;
            case Constants.DUP_X1:
                values.insert(2, values.top());
                break;
            case Constants.DUP_X2:
                values.insert(3, values.top());
            case Constants.DUP2:
                values.push(values.top(1), false);
                values.push(values.top(1), false);
                break;
            case Constants.DUP2_X1:
                values.insert(3, values.top(1));
                values.insert(3, values.top(0));
                break;
            case Constants.DUP2_X2:
                values.insert(4, values.top(1));
                values.insert(4, values.top(0));
                break;
            case Constants.POP:
                values.pop();
                break;
            case Constants.POP2:
                values.pop();
                values.pop();
                break;

            case Constants.SWAP:
                values.insert(1, values.pop());
                break;

            case Constants.IASTORE:
            case Constants.LASTORE:
            case Constants.FASTORE:
            case Constants.DASTORE:
            case Constants.CASTORE:
            case Constants.SASTORE:
            case Constants.BASTORE:
            case Constants.AASTORE:
                values.popValue();
                values.pop();
                values.pop();
                break;

            case Constants.IALOAD:
            case Constants.LALOAD:
            case Constants.FALOAD:
            case Constants.DALOAD:
            case Constants.CALOAD:
            case Constants.SALOAD:
            case Constants.BALOAD:
            case Constants.AALOAD: {
                values.pop();
                values.pop();
                Type t = ((ArrayInstruction)instruction).getType(cpg);
                values.push(new ValueInfo(t));
                break;
            }

            case Constants.IINC: {
                int i = ((IINC)instruction).getIndex();
                ValueInfo old = values.getLocalValue(i);
                if (old.isConstantValue() && old.getConstantValue() instanceof ConstantIntegerInfo) {
                    ConstantIntegerInfo value = (ConstantIntegerInfo) old.getConstantValue();
                    int newval = value.getValue() + ((IINC)instruction).getIncrement();
                    values.setLocalValue(i, new ValueInfo(new ConstantIntegerInfo(newval)));
                } else {
                    values.setLocalValue(i, new ValueInfo(Type.INT));
                }
                break;
            }

            case Constants.IADD:
            case Constants.ISUB:
            case Constants.IMUL:
            case Constants.IDIV:
            case Constants.IREM:
            case Constants.IAND:
            case Constants.IOR:
            case Constants.IXOR:
            case Constants.ISHL:
            case Constants.ISHR:
            case Constants.IUSHR:
                values.pop();
            case Constants.INEG:
                values.pop();
                values.push(new ValueInfo(Type.INT));
                break;
            case Constants.FADD:
            case Constants.FSUB:
            case Constants.FMUL:
            case Constants.FDIV:
            case Constants.FREM:
                values.pop();
            case Constants.FNEG:
                values.pop();
                values.push(new ValueInfo(Type.FLOAT));
                break;
            case Constants.LADD:
            case Constants.LSUB:
            case Constants.LMUL:
            case Constants.LDIV:
            case Constants.LREM:
            case Constants.LAND:
            case Constants.LOR:
            case Constants.LXOR:
                values.pop();
                values.pop();
            case Constants.LNEG:
                values.pop();
                values.pop();
                values.push(new ValueInfo(Type.LONG));
                break;
            case Constants.DADD:
            case Constants.DSUB:
            case Constants.DMUL:
            case Constants.DDIV:
            case Constants.DREM:
                values.pop();
                values.pop();
            case Constants.DNEG:
                values.pop();
                values.pop();
                values.push(new ValueInfo(Type.DOUBLE));
                break;
            case Constants.LSHL:
            case Constants.LSHR:
            case Constants.LUSHR:
                values.pop();
                values.pop();
                values.pop();
                values.push(new ValueInfo(Type.LONG));
                break;

            case Constants.I2B:
            case Constants.I2C:
            case Constants.I2S:
            case Constants.I2L:
            case Constants.I2F:
            case Constants.I2D:
            case Constants.L2I:
            case Constants.L2F:
            case Constants.L2D:
            case Constants.F2I:
            case Constants.F2L:
            case Constants.F2D:
            case Constants.D2I:
            case Constants.D2L:
            case Constants.D2F: {
                values.popValue();
                values.push(new ValueInfo( ((ConversionInstruction)instruction).getType(cpg) ));
                break;
            }

            case Constants.LCMP:
            case Constants.DCMPL:
            case Constants.DCMPG:
                values.pop();
                values.pop();
            case Constants.FCMPL:
            case Constants.FCMPG:
                values.pop();
                values.pop();
                values.push( new ValueInfo(Type.INT) );
                break;

            case Constants.IF_ICMPEQ:
            case Constants.IF_ICMPNE:
            case Constants.IF_ICMPLT:
            case Constants.IF_ICMPGE:
            case Constants.IF_ICMPGT:
            case Constants.IF_ICMPLE:
            case Constants.IF_ACMPEQ:
            case Constants.IF_ACMPNE:
                values.pop();
            case Constants.IFEQ:
            case Constants.IFNE:
            case Constants.IFLT:
            case Constants.IFGE:
            case Constants.IFLE:
            case Constants.IFGT:
            case Constants.IFNULL:
            case Constants.IFNONNULL:
                values.pop();
                break;

            case Constants.GOTO:
            case Constants.RET:
                break;
            case Constants.JSR:
                // This is of type 'returnAddress'
                values.push( new ValueInfo(Type.INT));
                break;

            case Constants.ARETURN:
            case Constants.IRETURN:
            case Constants.LRETURN:
            case Constants.FRETURN:
            case Constants.DRETURN:
            case Constants.RETURN:
                values.clearStack();
                break;

            case Constants.LOOKUPSWITCH:
            case Constants.TABLESWITCH:
                values.pop();
                break;

            case Constants.GETFIELD:
                values.pop();
            case Constants.GETSTATIC: {
                FieldInstruction f = (FieldInstruction)instruction;
                ConstantFieldInfo field = (ConstantFieldInfo) methodInfo.getClassInfo().getConstantInfo(f.getIndex());
                values.push(new ValueInfo(f.getFieldType(cpg), field.getValue()));
                break;
            }
            case Constants.PUTFIELD:
                values.pop();
            case Constants.PUTSTATIC: {
                FieldInstruction f = (FieldInstruction)instruction;
                values.pop(f.getFieldType(cpg).getSize());
                break;
            }

            case Constants.INVOKEVIRTUAL:
            case Constants.INVOKEINTERFACE:
            case Constants.INVOKESPECIAL:
                values.pop();
            case Constants.INVOKESTATIC: {
                InvokeInstruction invoke = (InvokeInstruction)instruction;
                values.pop(TypeHelper.getNumSlots(invoke.getArgumentTypes(cpg)));
                values.push(new ValueInfo(invoke.getReturnType(cpg)));
                break;
            }

            case Constants.MONITORENTER:
            case Constants.MONITOREXIT:
                values.pop();
                break;

            case Constants.ATHROW:
                ValueInfo ref = values.pop();
                values.clearStack();
                values.push(ref);
                break;
            case Constants.CHECKCAST:
                break;
            case Constants.INSTANCEOF:
                values.pop();
                values.push( new ValueInfo(Type.INT) );
                break;

            case Constants.NEW:
                values.push(new ValueInfo( ((NEW)instruction).getType(cpg) ));
                break;
            case Constants.NEWARRAY: {
                Type t = ((NEWARRAY)instruction).getType();
                values.push(new ValueInfo( new ArrayType(t, 1)));
                break;
            }
            case Constants.ANEWARRAY: {
                Type t = ((ANEWARRAY)instruction).getType(cpg);
                values.push(new ValueInfo( new ArrayType(t, 1)));
                break;
            }
            case Constants.MULTIANEWARRAY: {
                MULTIANEWARRAY instr = (MULTIANEWARRAY) instruction;
                values.pop(instr.getDimensions());
                values.push(new ValueInfo( new ArrayType(instr.getType(cpg), instr.getDimensions()) ));
                break;
            }

            case Constants.ARRAYLENGTH:
                values.pop();
                values.push(new ValueInfo(Type.INT));
                break;

            default:
                throw new AppInfoError("Instruction not supported: "+instruction);
        }

    }

    public ValueTable getValueTable() {
        return values;
    }

}
