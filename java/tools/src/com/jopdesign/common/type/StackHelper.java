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

package com.jopdesign.common.type;

import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.MiscUtils;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.TypedInstruction;

import java.util.Arrays;

/**
 * Just a container of various helper methods to work with types
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class StackHelper {

    public static Type[] consumeStack(ConstantPoolGen cpg, Instruction instruction) {

        // TODO any better (BCEL) way to do this?

        switch (instruction.getOpcode()) {
            case Constants.NOP:
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
            case Constants.LCONST_0:
            case Constants.LCONST_1:
            case Constants.FCONST_0:
            case Constants.FCONST_1:
            case Constants.FCONST_2:
            case Constants.DCONST_0:
            case Constants.DCONST_1:
            case Constants.LDC:
            case Constants.LDC_W:
            case Constants.LDC2_W:
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
            case Constants.ALOAD:
                return Type.NO_ARGS;

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
                return new Type[] { instr.getType(cpg) };
            }

            case Constants.DUP:
                return new Type[] {Type.UNKNOWN};
            case Constants.DUP_X1:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP_X2:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP2:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP2_X1:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP2_X2:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN};
            case Constants.POP:
                return new Type[] {Type.UNKNOWN};
            case Constants.POP2:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN};
            case Constants.SWAP:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN};

            case Constants.IASTORE:
            case Constants.FASTORE:
            case Constants.LASTORE:
            case Constants.DASTORE:
            case Constants.CASTORE:
            case Constants.SASTORE:
            case Constants.BASTORE:
            case Constants.AASTORE: {
                Type t = ((ArrayInstruction)instruction).getType(cpg);
                return new Type[] { new ArrayType(t, 1), Type.INT, t };
            }

            case Constants.IALOAD:
            case Constants.LALOAD:
            case Constants.FALOAD:
            case Constants.DALOAD:
            case Constants.CALOAD:
            case Constants.SALOAD:
            case Constants.BALOAD:
            case Constants.AALOAD: {
                Type t = ((ArrayInstruction)instruction).getType(cpg);
                return new Type[] { new ArrayType(t, 1), Type.INT };
            }

            case Constants.IINC:
                return Type.NO_ARGS;

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
                return new Type[] {Type.INT, Type.INT};
            case Constants.INEG:
                return new Type[] {Type.INT};
            case Constants.LADD:
            case Constants.LSUB:
            case Constants.LMUL:
            case Constants.LDIV:
            case Constants.LREM:
            case Constants.LAND:
            case Constants.LOR:
            case Constants.LXOR:
                return new Type[] {Type.LONG, Type.LONG};
            case Constants.LSHL:
            case Constants.LSHR:
            case Constants.LUSHR:
                return new Type[] {Type.LONG, Type.INT};
            case Constants.LNEG:
                return new Type[] {Type.LONG};
            case Constants.FADD:
            case Constants.FSUB:
            case Constants.FMUL:
            case Constants.FDIV:
            case Constants.FREM:
                return new Type[] {Type.FLOAT, Type.FLOAT};
            case Constants.FNEG:
                return new Type[] {Type.FLOAT};
            case Constants.DADD:
            case Constants.DSUB:
            case Constants.DMUL:
            case Constants.DDIV:
            case Constants.DREM:
                return new Type[] {Type.DOUBLE, Type.DOUBLE};
            case Constants.DNEG:
                return new Type[] {Type.DOUBLE};

            case Constants.I2B:
            case Constants.I2C:
            case Constants.I2S:
            case Constants.I2L:
            case Constants.I2F:
            case Constants.I2D:
                return new Type[] {Type.INT};
            case Constants.L2I:
            case Constants.L2F:
            case Constants.L2D:
                return new Type[] {Type.LONG};
            case Constants.F2I:
            case Constants.F2L:
            case Constants.F2D:
                return new Type[] {Type.FLOAT};
            case Constants.D2I:
            case Constants.D2L:
            case Constants.D2F:
                return new Type[] {Type.FLOAT};

            case Constants.LCMP:
            case Constants.FCMPL:
            case Constants.FCMPG:
            case Constants.DCMPL:
            case Constants.DCMPG: {
                Type t = ((TypedInstruction)instruction).getType(cpg);
                return new Type[] {t, t};
            }

            case Constants.IFNULL:
            case Constants.IFNONNULL:
                return new Type[] {Type.OBJECT};
            case Constants.IFEQ:
            case Constants.IFNE:
            case Constants.IFLT:
            case Constants.IFGE:
            case Constants.IFLE:
            case Constants.IFGT:
                return new Type[] {Type.INT};
            case Constants.IF_ICMPEQ:
            case Constants.IF_ICMPNE:
            case Constants.IF_ICMPLT:
            case Constants.IF_ICMPGE:
            case Constants.IF_ICMPGT:
            case Constants.IF_ICMPLE:
                return new Type[] {Type.INT, Type.INT};
            case Constants.IF_ACMPEQ:
            case Constants.IF_ACMPNE:
                return new Type[] {Type.OBJECT, Type.OBJECT};

            case Constants.GOTO:
            case Constants.JSR:
            case Constants.RET:
                return Type.NO_ARGS;

            case Constants.RETURN:
                return Type.NO_ARGS;
            case Constants.ARETURN:
            case Constants.IRETURN:
            case Constants.LRETURN:
            case Constants.FRETURN:
            case Constants.DRETURN: {
                Type t = ((ReturnInstruction)instruction).getType();
                return new Type[] {t};
            }

            case Constants.LOOKUPSWITCH:
            case Constants.TABLESWITCH:
                return new Type[] {Type.INT};

            case Constants.PUTFIELD: {
                PUTFIELD p = (PUTFIELD)instruction;
                return new Type[] {p.getReferenceType(cpg), p.getFieldType(cpg)};
            }
            case Constants.PUTSTATIC: {
                PUTSTATIC p = (PUTSTATIC)instruction;
                return new Type[] {p.getFieldType(cpg)};
            }
            case Constants.GETFIELD: {
                GETFIELD g = (GETFIELD)instruction;
                return new Type[] {g.getReferenceType(cpg)};
            }
            case Constants.GETSTATIC:
                return Type.NO_ARGS;

            case Constants.INVOKESTATIC: {
                InvokeInstruction invoke = (InvokeInstruction)instruction;
                return invoke.getArgumentTypes(cpg); 
            }
            case Constants.INVOKEVIRTUAL:
            case Constants.INVOKEINTERFACE:
            case Constants.INVOKESPECIAL: {
                InvokeInstruction invoke = (InvokeInstruction)instruction;
                return MiscUtils.concat(invoke.getReferenceType(cpg), invoke.getArgumentTypes(cpg));
            }

            case Constants.MONITORENTER:
            case Constants.MONITOREXIT:
                return new Type[] {Type.OBJECT};

            case Constants.ATHROW:
                return new Type[] {Type.OBJECT};
                
            case Constants.CHECKCAST:
            case Constants.INSTANCEOF:
                return new Type[] {Type.OBJECT};

            case Constants.NEW:
                return Type.NO_ARGS;

            case Constants.NEWARRAY:
            case Constants.ANEWARRAY:
                return new Type[] {Type.INT};
            case Constants.MULTIANEWARRAY: {
                Type[] t = new Type[((MULTIANEWARRAY)instruction).getDimensions()];
                Arrays.fill(t, Type.INT);
                return t;
            }
            case Constants.ARRAYLENGTH:
                return new Type[] {new ArrayType(Type.UNKNOWN, 1)};

            default:
                throw new AppInfoError("Instruction not supported: "+instruction);
        }

    }

    public static Type[] produceStack(ConstantPoolGen cpg, Instruction instruction) {

        switch (instruction.getOpcode()) {
            case Constants.NOP:
                return Type.NO_ARGS;

            case Constants.ACONST_NULL:
                return new Type[] {Type.NULL};

            case Constants.ICONST_M1:
            case Constants.ICONST_0:
            case Constants.ICONST_1:
            case Constants.ICONST_2:
            case Constants.ICONST_3:
            case Constants.ICONST_4:
            case Constants.ICONST_5:
            case Constants.BIPUSH:
            case Constants.SIPUSH:
            case Constants.LCONST_0:
            case Constants.LCONST_1:
            case Constants.FCONST_0:
            case Constants.FCONST_1:
            case Constants.FCONST_2:
            case Constants.DCONST_0:
            case Constants.DCONST_1: {
                ConstantPushInstruction instr = (ConstantPushInstruction)instruction;
                return new Type[] {instr.getType(cpg)};
            }

            case Constants.LDC:
            case Constants.LDC_W:
            case Constants.LDC2_W: {
                CPInstruction instr = (CPInstruction) instruction;
                return new Type[] {instr.getType(cpg)};
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
            case Constants.FSTORE:
                return Type.NO_ARGS;

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
                return new Type[] {instr.getType(cpg)};
            }

            case Constants.DUP:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP_X1:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP_X2:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP2:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP2_X1:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN};
            case Constants.DUP2_X2:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN,
                                   Type.UNKNOWN, Type.UNKNOWN, Type.UNKNOWN};

            case Constants.POP:
            case Constants.POP2:
                return Type.NO_ARGS;

            case Constants.SWAP:
                return new Type[] {Type.UNKNOWN, Type.UNKNOWN};

            case Constants.IASTORE:
            case Constants.LASTORE:
            case Constants.FASTORE:
            case Constants.DASTORE:
            case Constants.CASTORE:
            case Constants.SASTORE:
            case Constants.BASTORE:
            case Constants.AASTORE:
                return Type.NO_ARGS;

            case Constants.IALOAD:
            case Constants.LALOAD:
            case Constants.FALOAD:
            case Constants.DALOAD:
            case Constants.CALOAD:
            case Constants.SALOAD:
            case Constants.BALOAD:
            case Constants.AALOAD:
                return new Type[] { ((ArrayInstruction)instruction).getType(cpg) };

            case Constants.IINC:
                return Type.NO_ARGS;

            case Constants.IADD:
            case Constants.ISUB:
            case Constants.IMUL:
            case Constants.IDIV:
            case Constants.INEG:
            case Constants.IREM:
            case Constants.LADD:
            case Constants.LSUB:
            case Constants.LMUL:
            case Constants.LDIV:
            case Constants.LNEG:
            case Constants.LREM:
            case Constants.FADD:
            case Constants.FSUB:
            case Constants.FMUL:
            case Constants.FDIV:
            case Constants.FNEG:
            case Constants.FREM:
            case Constants.DADD:
            case Constants.DSUB:
            case Constants.DMUL:
            case Constants.DDIV:
            case Constants.DNEG:
            case Constants.DREM:
            case Constants.ISHL:
            case Constants.ISHR:
            case Constants.IUSHR:
            case Constants.LSHL:
            case Constants.LSHR:
            case Constants.LUSHR:
            case Constants.IAND:
            case Constants.IOR:
            case Constants.IXOR:
            case Constants.LAND:
            case Constants.LOR:
            case Constants.LXOR:
                return new Type[] { ((ArithmeticInstruction)instruction).getType(cpg) };

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
            case Constants.D2F:
                return new Type[] { ((ConversionInstruction)instruction).getType(cpg) };

            case Constants.LCMP:
            case Constants.FCMPL:
            case Constants.FCMPG:
            case Constants.DCMPL:
            case Constants.DCMPG:
                return new Type[] {Type.INT};

            case Constants.IFNULL:
            case Constants.IFNONNULL:
            case Constants.IFEQ:
            case Constants.IFNE:
            case Constants.IFLT:
            case Constants.IFGE:
            case Constants.IFLE:
            case Constants.IFGT:
            case Constants.IF_ICMPEQ:
            case Constants.IF_ICMPNE:
            case Constants.IF_ICMPLT:
            case Constants.IF_ICMPGE:
            case Constants.IF_ICMPGT:
            case Constants.IF_ICMPLE:
            case Constants.IF_ACMPEQ:
            case Constants.IF_ACMPNE:
            case Constants.GOTO:
            case Constants.ARETURN:
            case Constants.RETURN:
            case Constants.IRETURN:
            case Constants.LRETURN:
            case Constants.FRETURN:
            case Constants.DRETURN:
            case Constants.LOOKUPSWITCH:
            case Constants.TABLESWITCH:
                return Type.NO_ARGS;

            case Constants.PUTFIELD:
            case Constants.PUTSTATIC:
                return Type.NO_ARGS;

            case Constants.GETFIELD:
            case Constants.GETSTATIC: {
                FieldInstruction instr = (FieldInstruction)instruction;
                return new Type[] {instr.getFieldType(cpg)};
            }

            case Constants.INVOKEVIRTUAL:
            case Constants.INVOKEINTERFACE:
            case Constants.INVOKESTATIC:
            case Constants.INVOKESPECIAL: {
                InvokeInstruction invoke = (InvokeInstruction)instruction;
                Type t = invoke.getReturnType(cpg);
                if (Type.VOID.equals(t)) {
                    return Type.NO_ARGS;
                } else {
                    return new Type[] {t};
                }
            }

            case Constants.MONITORENTER:
            case Constants.MONITOREXIT:
            case Constants.ATHROW:
                return Type.NO_ARGS;

            case Constants.CHECKCAST:
                return new Type[] { ((CHECKCAST)instruction).getType(cpg) };
            case Constants.INSTANCEOF:
                return new Type[] { Type.INT };

            case Constants.NEW:
                return new Type[] { ((NEW)instruction).getType(cpg) };
            case Constants.NEWARRAY: {
                Type t = ((NEWARRAY)instruction).getType();
                return new Type[] { new ArrayType(t, 1) };
            }
            case Constants.ANEWARRAY: {
                Type t = ((ANEWARRAY)instruction).getType(cpg);
                return new Type[] { new ArrayType(t, 1) };
            }
            case Constants.MULTIANEWARRAY: {
                MULTIANEWARRAY instr = (MULTIANEWARRAY) instruction;
                return new Type[] { new ArrayType(instr.getType(cpg), instr.getDimensions()) };
            }

            case Constants.ARRAYLENGTH:
                return new Type[] {Type.INT};

            default:
                throw new AppInfoError("Instruction not supported: "+instruction);
        }
    }

}
