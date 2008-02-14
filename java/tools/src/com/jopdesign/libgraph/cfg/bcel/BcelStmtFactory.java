/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.cfg.bcel;

import com.jopdesign.libgraph.cfg.statements.CmpStmt;
import com.jopdesign.libgraph.cfg.statements.IdentityStmt;
import com.jopdesign.libgraph.cfg.statements.common.BinopStmt;
import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.cfg.statements.stack.*;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.ConstantPoolInfo;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.ObjectRefType;
import com.jopdesign.libgraph.struct.type.RefTypeInfo;
import com.jopdesign.libgraph.struct.type.TypeHelper;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;

/**
 * Implementation of the mapping of JVM bytecodes to graph-stackcode-statements. 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelStmtFactory {

    private AppStruct appStruct;
    private ConstantPoolInfo cp;

    public BcelStmtFactory(AppStruct appStruct, ConstantPoolInfo cp) {
        this.appStruct = appStruct;
        this.cp = cp;
    }

    /**
     * create one or more stackstatements from an instruction.
     * @param instruction the instruction to parse.
     * @param stack the current types on the stack.
     * @param tos the index in the array for the top of the stack.
     * @return a list of stackstatements, or null if the next instruction must be read to complete the stackstatement.
     * @throws com.jopdesign.libgraph.struct.TypeException if instruction not known or NYI.
     */
    public StackStatement[] getStackStatements(Instruction instruction, VariableTable varTable,
                                               TypeInfo[] stack, int tos) throws TypeException
    {
        ConstantValue val;
        ConstantClass clazz;

        switch ( instruction.getOpcode() ) {
            case 0x00: return new StackStatement[] {
                    new StackNop()
                };
            case 0x01: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_NULL)
                };
            case 0x02: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_I_MINUS_ONE)
                };
            case 0x03: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_I_ZERO)
                };
            case 0x04: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_I_ONE)
                };
            case 0x05: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_I_TWO)
                };
            case 0x06: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_I_THREE)
                };
            case 0x07: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_I_FOUR)
                };
            case 0x08: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_I_FIVE)
                };
            case 0x09: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_L_ZERO)
                };
            case 0x0a: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_L_ONE)
                };
            case 0x0b: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_F_ZERO)
                };
            case 0x0c: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_F_ONE)
                };
            case 0x0d: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_F_TWO)
                };
            case 0x0e: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_D_ZERO)
                };
            case 0x0f: return new StackStatement[] {
                    new StackPush(ConstantValue.CONST_D_ONE)
                };
            case 0x10: return new StackStatement[] {
                    new StackPush(new ConstantValue(TypeInfo.CONST_BYTE,
                            ((BIPUSH)instruction).getValue().byteValue() ))
                };
            case 0x11: return new StackStatement[] {
                    new StackPush(new ConstantValue(TypeInfo.CONST_SHORT,
                            ((SIPUSH)instruction).getValue().shortValue() ))
                };
            case 0x12:  // ldc
                val = cp.getConstant(((LDC)instruction).getIndex());
                return new StackStatement[] {
                    new StackPush(val)
                };
            case 0x13:
                val = cp.getConstant(((LDC_W)instruction).getIndex());
                return new StackStatement[] {
                    new StackPush(val)
                };
            case 0x14:
                val = cp.getConstant( ((LDC2_W)instruction).getIndex());
                return new StackStatement[] {
                    new StackPush(val)
                };
            case 0x15: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(((ILOAD)instruction).getIndex()))
                };
            case 0x16: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(((LLOAD)instruction).getIndex()))
                };
            case 0x17: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(((FLOAD)instruction).getIndex()))
                };
            case 0x18: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(((DLOAD)instruction).getIndex()))
                };
            case 0x19: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_OBJECTREF, varTable.getDefaultLocalVariable(((ALOAD)instruction).getIndex()))
                };
            case 0x1a: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(0))
                };
            case 0x1b: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(1))
                };
            case 0x1c: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(2))
                };
            case 0x1d: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(3))
                };
            case 0x1e: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(0))
                };
            case 0x1f: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(1))
                };
            case 0x20: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(2))
                };
            case 0x21: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(3))
                };
            case 0x22: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(0))
                };
            case 0x23: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(1))
                };
            case 0x24: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(2))
                };
            case 0x25: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(3))
                };
            case 0x26: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(0))
                };
            case 0x27: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(1))
                };
            case 0x28: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(2))
                };
            case 0x29: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(3))
                };
            case 0x2a: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_OBJECTREF, varTable.getDefaultLocalVariable(0))
                };
            case 0x2b: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_OBJECTREF, varTable.getDefaultLocalVariable(1))
                };
            case 0x2c: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_OBJECTREF, varTable.getDefaultLocalVariable(2))
                };
            case 0x2d: return new StackStatement[] {
                    new StackLoad(TypeInfo.CONST_OBJECTREF, varTable.getDefaultLocalVariable(3))
                };
            case 0x2e: return new StackStatement[] {
                    new StackArrayLoad(TypeInfo.CONST_INT)
                };
            case 0x2f: return new StackStatement[] {
                    new StackArrayLoad(TypeInfo.CONST_LONG)
                };
            case 0x30: return new StackStatement[] {
                    new StackArrayLoad(TypeInfo.CONST_FLOAT)
                };
            case 0x31: return new StackStatement[] {
                    new StackArrayLoad(TypeInfo.CONST_DOUBLE)
                };
            case 0x32: return new StackStatement[] {
                    new StackArrayLoad(TypeInfo.CONST_OBJECTREF)
                };
            case 0x33: return new StackStatement[] {
                    // NOTICE this can also be of type BOOLEAN!
                    new StackArrayLoad(TypeInfo.CONST_BYTE)
                };
            case 0x34: return new StackStatement[] {
                    new StackArrayLoad(TypeInfo.CONST_CHAR)
                };
            case 0x35: return new StackStatement[] {
                    new StackArrayLoad(TypeInfo.CONST_SHORT)
                };
            case 0x36: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(((ISTORE)instruction).getIndex()))
                };
            case 0x37: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(((LSTORE)instruction).getIndex()))
                };
            case 0x38: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(((FSTORE)instruction).getIndex()))
                };
            case 0x39: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(((DSTORE)instruction).getIndex()))
                };
            case 0x3a:
                if ( stack[tos].getMachineType() != TypeInfo.TYPE_REFERENCE ) {
                    throw new TypeException("Expected reference type on stack for store instruction");
                }
                return new StackStatement[] {
                    new StackStore(stack[tos], varTable.getDefaultLocalVariable(((ASTORE)instruction).getIndex()))
                };
            case 0x3b: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(0))
                };
            case 0x3c: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(1))
                };
            case 0x3d: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(2))
                };
            case 0x3e: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_INT, varTable.getDefaultLocalVariable(3))
                };
            case 0x3f: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(0))
                };
            case 0x40: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(1))
                };
            case 0x41: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(2))
                };
            case 0x42: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_LONG, varTable.getDefaultLocalVariable(3))
                };
            case 0x43: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(0))
                };
            case 0x44: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(1))
                };
            case 0x45: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(2))
                };
            case 0x46: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_FLOAT, varTable.getDefaultLocalVariable(3))
                };
            case 0x47: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(0))
                };
            case 0x48: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(1))
                };
            case 0x49: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(2))
                };
            case 0x4a: return new StackStatement[] {
                    new StackStore(TypeInfo.CONST_DOUBLE, varTable.getDefaultLocalVariable(3))
                };
            case 0x4b:
                if ( stack[tos].getMachineType() != TypeInfo.TYPE_REFERENCE ) {
                    throw new TypeException("Expected reference type on stack for store instruction");
                }
                return new StackStatement[] {
                    new StackStore(stack[tos], varTable.getDefaultLocalVariable(0))
                };
            case 0x4c:
                if ( stack[tos].getMachineType() != TypeInfo.TYPE_REFERENCE ) {
                    throw new TypeException("Expected reference type on stack for store instruction");
                }
                return new StackStatement[] {
                    new StackStore(stack[tos], varTable.getDefaultLocalVariable(1))
                };
            case 0x4d:
                if ( stack[tos].getMachineType() != TypeInfo.TYPE_REFERENCE ) {
                    throw new TypeException("Expected reference type on stack for store instruction");
                }
                return new StackStatement[] {
                    new StackStore(stack[tos], varTable.getDefaultLocalVariable(2))
                };
            case 0x4e:
                if ( stack[tos].getMachineType() != TypeInfo.TYPE_REFERENCE ) {
                    throw new TypeException("Expected reference type on stack for store instruction");
                }
                return new StackStatement[] {
                    new StackStore(stack[tos], varTable.getDefaultLocalVariable(3))
                };
            case 0x4f: return new StackStatement[] {
                    new StackArrayStore(TypeInfo.CONST_INT)
                };
            case 0x50: return new StackStatement[] {
                    new StackArrayStore(TypeInfo.CONST_LONG)
                };
            case 0x51: return new StackStatement[] {
                    new StackArrayStore(TypeInfo.CONST_FLOAT)
                };
            case 0x52: return new StackStatement[] {
                    new StackArrayStore(TypeInfo.CONST_DOUBLE)
                };
            case 0x53: return new StackStatement[] {
                    new StackArrayStore(TypeInfo.CONST_OBJECTREF)
                };
            case 0x54: return new StackStatement[] {
                    new StackArrayStore(TypeInfo.CONST_BYTE)
                };
            case 0x55: return new StackStatement[] {
                    new StackArrayStore(TypeInfo.CONST_CHAR)
                };
            case 0x56: return new StackStatement[] {
                    new StackArrayStore(TypeInfo.CONST_SHORT)
                };
            case 0x57: return new StackStatement[] {
                    new StackPop(stack[tos])
                };
            case 0x58: return new StackStatement[] {
                    // POP2: pop two values of length 1 or one value of length 2
                    new StackPop(stack[tos], stack[tos].getLength() == 1 ? 2 : 1)
                };
            case 0x59: return new StackStatement[] {
                    new StackDup(stack[tos])
                };
            case 0x5a: return new StackStatement[] {
                    new StackDup(stack[tos], stack[tos-1])
                };
            case 0x5b: return new StackStatement[] {
                    // DUP_x2: top is category 1, insert down 2 slots
                    new StackDup(new TypeInfo[] {stack[tos]},
                        stack[tos-1].getLength() == 2 ? 
                              new TypeInfo[] {stack[tos-1]} :
                              new TypeInfo[] {stack[tos-2],stack[tos-1]}
                    )
                };
            case 0x5c: return new StackStatement[] {
                    // DUP2: duplicate two top slots
                    new StackDup( stack[tos].getLength() == 2 ?
                        new TypeInfo[] {stack[tos]} :
                        new TypeInfo[] {stack[tos-1],stack[tos]}
                    )
                };
            case 0x5d: return new StackStatement[] {
                    // DUP2_X1: dup two top slots one slot down
                    new StackDup( stack[tos].getLength() == 2 ?
                        new TypeInfo[] {stack[tos]} :
                        new TypeInfo[] {stack[tos-1],stack[tos]}
                        , new TypeInfo[] {stack[tos-2]} )

                };
            case 0x5e: return new StackStatement[] {
                    // DUP2_X2: dup two top slots two slots down
                    new StackDup( stack[tos].getLength() == 2 ?
                        new TypeInfo[] {stack[tos]} :
                        new TypeInfo[] {stack[tos-1],stack[tos]}
                        , stack[tos-2].getLength() == 2 ?
                          new TypeInfo[] {stack[tos-2]} :
                          new TypeInfo[] {stack[tos-3],stack[tos-2]}
                    )
                };
            case 0x5f: return new StackStatement[] {
                    new StackSwap(stack[tos-1], stack[tos])
            };
            case 0x60: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_ADD)
                };
            case 0x61: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_ADD)
                };
            case 0x62: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_FLOAT, BinopStmt.OP_ADD)
                };
            case 0x63: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_DOUBLE, BinopStmt.OP_ADD)
                };
            case 0x64: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_SUB)
                };
            case 0x65: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_SUB)
                };
            case 0x66: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_FLOAT, BinopStmt.OP_SUB)
                };
            case 0x67: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_DOUBLE, BinopStmt.OP_SUB)
                };
            case 0x68: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_MUL)
                };
            case 0x69: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_MUL)
                };
            case 0x6a: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_FLOAT, BinopStmt.OP_MUL)
                };
            case 0x6b: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_DOUBLE, BinopStmt.OP_MUL)
                };
            case 0x6c: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_DIV)
                };
            case 0x6d: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_DIV)
                };
            case 0x6e: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_FLOAT, BinopStmt.OP_DIV)
                };
            case 0x6f: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_DOUBLE, BinopStmt.OP_DIV)
                };
            case 0x70: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_REMINDER)
                };
            case 0x71: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_REMINDER)
                };
            case 0x72: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_FLOAT, BinopStmt.OP_REMINDER)
                };
            case 0x73: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_DOUBLE, BinopStmt.OP_REMINDER)
                };
            case 0x74: return new StackStatement[] {
                    new StackNegate(TypeInfo.CONST_INT)
                };
            case 0x75: return new StackStatement[] {
                    new StackNegate(TypeInfo.CONST_LONG)
                };
            case 0x76: return new StackStatement[] {
                    new StackNegate(TypeInfo.CONST_FLOAT)
                };
            case 0x77: return new StackStatement[] {
                    new StackNegate(TypeInfo.CONST_DOUBLE)
                };
            case 0x78: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_SHIFT_LEFT)
                };
            case 0x79: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_SHIFT_LEFT)
                };
            case 0x7a: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_SHIFT_RIGHT)
                };
            case 0x7b: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_SHIFT_RIGHT)
                };
            case 0x7c: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_LOGIC_SHIFT_RIGHT)
                };
            case 0x7d: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_LOGIC_SHIFT_RIGHT)
                };
            case 0x7e: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_AND)
                };
            case 0x7f: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_AND)
                };
            case 0x80: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_OR)
                };
            case 0x81: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_OR)
                };
            case 0x82: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_INT, BinopStmt.OP_XOR)
                };
            case 0x83: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_XOR)
                };
            case 0x84: return new StackStatement[] {
                    new StackIInc(varTable.getDefaultLocalVariable(((IINC)instruction).getIndex()),
                            ((IINC)instruction).getIncrement())
                };
            case 0x85: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_INT, TypeInfo.CONST_LONG)
                };
            case 0x86: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_INT, TypeInfo.CONST_FLOAT)
                };
            case 0x87: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_INT, TypeInfo.CONST_DOUBLE)
                };
            case 0x88: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_LONG, TypeInfo.CONST_INT)
                };
            case 0x89: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_LONG, TypeInfo.CONST_FLOAT)
                };
            case 0x8a: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_LONG, TypeInfo.CONST_DOUBLE)
                };
            case 0x8b: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_FLOAT, TypeInfo.CONST_INT)
                };
            case 0x8c: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_FLOAT, TypeInfo.CONST_LONG)
                };
            case 0x8d: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_FLOAT, TypeInfo.CONST_DOUBLE)
                };
            case 0x8e: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_DOUBLE, TypeInfo.CONST_INT)
                };
            case 0x8f: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_DOUBLE, TypeInfo.CONST_LONG)
                };
            case 0x90: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_DOUBLE, TypeInfo.CONST_FLOAT)
                };
            case 0x91: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_INT, TypeInfo.CONST_BYTE)
                };
            case 0x92: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_INT, TypeInfo.CONST_CHAR)
                };
            case 0x93: return new StackStatement[] {
                    new StackConvert(TypeInfo.CONST_INT, TypeInfo.CONST_SHORT)
                };
            case 0x94: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_LONG, BinopStmt.OP_CMP)
                };
            case 0x95: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_FLOAT, BinopStmt.OP_CMPL)
                };
            case 0x96: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_DOUBLE, BinopStmt.OP_CMPG)
                };
            case 0x97: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_FLOAT, BinopStmt.OP_CMPL)
                };
            case 0x98: return new StackStatement[] {
                    new StackBinop(TypeInfo.CONST_DOUBLE, BinopStmt.OP_CMPG)
                };
            case 0x99: return new StackStatement[] {
                    new StackIfZero(TypeInfo.CONST_INT, CmpStmt.OP_EQUAL)
            };
            case 0x9a: return new StackStatement[] {
                    new StackIfZero(TypeInfo.CONST_INT, CmpStmt.OP_NOTEQUAL)
                };
            case 0x9b: return new StackStatement[] {
                    new StackIfZero(TypeInfo.CONST_INT, CmpStmt.OP_LESS)
                };
            case 0x9c: return new StackStatement[] {
                    new StackIfZero(TypeInfo.CONST_INT, CmpStmt.OP_GREATER_OR_EQUAL)
                };
            case 0x9d: return new StackStatement[] {
                    new StackIfZero(TypeInfo.CONST_INT, CmpStmt.OP_GREATER)
                };
            case 0x9e: return new StackStatement[] {
                    new StackIfZero(TypeInfo.CONST_INT, CmpStmt.OP_LESS_OR_EQUAL)
                };
            case 0x9f: return new StackStatement[] {
                    new StackIfCmp(TypeInfo.CONST_INT, CmpStmt.OP_EQUAL)
                };
            case 0xa0: return new StackStatement[] {
                    new StackIfCmp(TypeInfo.CONST_INT, CmpStmt.OP_NOTEQUAL)
                };
            case 0xa1: return new StackStatement[] {
                    new StackIfCmp(TypeInfo.CONST_INT, CmpStmt.OP_LESS)
                };
            case 0xa2: return new StackStatement[] {
                    new StackIfCmp(TypeInfo.CONST_INT, CmpStmt.OP_GREATER_OR_EQUAL)
                };
            case 0xa3: return new StackStatement[] {
                    new StackIfCmp(TypeInfo.CONST_INT, CmpStmt.OP_GREATER)
                };
            case 0xa4: return new StackStatement[] {
                    new StackIfCmp(TypeInfo.CONST_INT, CmpStmt.OP_LESS_OR_EQUAL)
                };
            case 0xa5: return new StackStatement[] {
                    new StackIfCmp(TypeInfo.CONST_OBJECTREF, CmpStmt.OP_EQUAL)
                };
            case 0xa6: return new StackStatement[] {
                    new StackIfCmp(TypeInfo.CONST_OBJECTREF, CmpStmt.OP_NOTEQUAL)
                };
            case 0xa7: return new StackStatement[] {
                    new StackGoto()
                };
            case 0xa8: return new StackStatement[] {
                    new StackJSR()
                };
            case 0xa9: return new StackStatement[] {
                    new StackJSRReturn(varTable.getDefaultLocalVariable(((RET)instruction).getIndex() ))
                };
            case 0xaa:
                int[] matchs = ((TABLESWITCH)instruction).getMatchs();
                return new StackStatement[] {
                    new StackTableswitch(matchs[0], matchs[matchs.length-1])
                };
            case 0xab: return new StackStatement[] {
                    new StackLookupswitch(((LOOKUPSWITCH)instruction).getMatchs())
                };
            case 0xac: return new StackStatement[] {
                    new StackReturn(TypeInfo.CONST_INT)
                };
            case 0xad: return new StackStatement[] {
                    new StackReturn(TypeInfo.CONST_LONG)
                };
            case 0xae: return new StackStatement[] {
                    new StackReturn(TypeInfo.CONST_FLOAT)
                };
            case 0xaf: return new StackStatement[] {
                    new StackReturn(TypeInfo.CONST_DOUBLE)
                };
            case 0xb0: return new StackStatement[] {
                    new StackReturn(TypeInfo.CONST_OBJECTREF)
                };
            case 0xb1: return new StackStatement[] {
                    new StackReturn()
                };
            case 0xb2: return new StackStatement[] {
                    new StackGetField(cp.getFieldReference(((GETSTATIC)instruction).getIndex()))
                };
            case 0xb3: return new StackStatement[] {
                    new StackPutField(cp.getFieldReference(((PUTSTATIC)instruction).getIndex()))
                };
            case 0xb4: return new StackStatement[] {
                    new StackGetField(cp.getFieldReference(((GETFIELD)instruction).getIndex()))
                };
            case 0xb5: return new StackStatement[] {
                    new StackPutField(cp.getFieldReference(((PUTFIELD)instruction).getIndex()))
                };
            case 0xb6: return new StackStatement[] {
                    new StackInvoke(cp.getMethodReference(((INVOKEVIRTUAL)instruction).getIndex()), InvokeStmt.TYPE_VIRTUAL)
                };
            case 0xb7: return new StackStatement[] {
                    new StackInvoke(cp.getMethodReference(((INVOKESPECIAL)instruction).getIndex()), InvokeStmt.TYPE_SPECIAL)
                };
            case 0xb8: return new StackStatement[] {
                    new StackInvoke(cp.getMethodReference(((INVOKESTATIC)instruction).getIndex()), InvokeStmt.TYPE_STATIC)
                };
            case 0xb9: return new StackStatement[] {
                    new StackInvoke(cp.getMethodReference(((INVOKEINTERFACE)instruction).getIndex()), InvokeStmt.TYPE_INTERFACE)
                };
            case 0xbb: return new StackStatement[] {
                    new StackNew(cp.getClassReference(((NEW)instruction).getIndex()))
                };
            case 0xbc: return new StackStatement[] {
                    new StackNewArray(TypeHelper.parseType( appStruct,
                            BasicType.getType( ((NEWARRAY)instruction).getTypecode() ) ))
                };
            case 0xbd:
                clazz = cp.getClassReference( ((ANEWARRAY)instruction).getIndex() );
                return new StackStatement[] {
                    new StackNewArray(new ObjectRefType(clazz))
                };
            case 0xbe: return new StackStatement[] {
                    new StackArrayLength()
                };
            case 0xbf: return new StackStatement[] {
                    new StackThrow()
                };
            case 0xc0: return new StackStatement[] {
                    new StackCheckcast(cp.getClassReference(((CHECKCAST)instruction).getIndex()))
                };
            case 0xc1: return new StackStatement[] {
                    new StackInstanceof(cp.getClassReference(((INSTANCEOF)instruction).getIndex()))
                };
            case 0xc2: return new StackStatement[] {
                    new StackEntermonitor()
                };
            case 0xc3: return new StackStatement[] {
                    new StackExitmonitor()
                };
            case 0xc5:
                clazz = cp.getClassReference( ((MULTIANEWARRAY)instruction).getIndex() );
                return new StackStatement[] {
                    new StackNewMultiArray(clazz, ((MULTIANEWARRAY)instruction).getDimensions())
                };
            case 0xc6: return new StackStatement[] {
                    new StackIfZero(TypeInfo.CONST_OBJECTREF, CmpStmt.OP_EQUAL)
                };
            case 0xc7: return new StackStatement[] {
                    new StackIfZero(TypeInfo.CONST_OBJECTREF, CmpStmt.OP_NOTEQUAL)
                };
            case 0xc8: return new StackStatement[] {
                    new StackGoto()
                };
            case 0xc9: throw new TypeException("JSR is currently not supported."); 
                //return new StackStatement[] {
                    // TODO resolve basicblock
                //    new StackJSR(null)
                //};
            case 0xca: return new StackStatement[] {
                    new StackBreakpoint()
                };
            default: throw new TypeException("Invalid or unsupported opcode {"+instruction.getOpcode()+"}.");
        }
    }

    public Instruction[] getInstructions(StackStatement stmt, VariableTable varTable, InstructionHandle[] targets) throws TypeException {

        if ( stmt instanceof StackArrayLength ) {
            return new Instruction[] { new ARRAYLENGTH() };
        }
        if ( stmt instanceof StackArrayLoad ) {
            Instruction is;
            if ( ((StackArrayLoad)stmt).getArrayType().getMachineType() == TypeInfo.TYPE_REFERENCE ) {
                is = new AALOAD();
            } else {
                switch ( ((StackArrayLoad)stmt).getArrayType().getType() ) {
                    case TypeInfo.TYPE_BOOL: is = new BALOAD(); break;
                    case TypeInfo.TYPE_BYTE: is = new BALOAD(); break;
                    case TypeInfo.TYPE_CHAR: is = new CALOAD(); break;
                    case TypeInfo.TYPE_SHORT: is = new SALOAD(); break;
                    case TypeInfo.TYPE_INT: is = new IALOAD(); break;
                    case TypeInfo.TYPE_LONG: is = new LALOAD(); break;
                    case TypeInfo.TYPE_FLOAT: is = new FALOAD(); break;
                    case TypeInfo.TYPE_DOUBLE: is = new DALOAD(); break;
                    default: throw new TypeException("Invalid type for arrayload {"+
                            ((StackArrayLoad)stmt).getArrayType().getTypeName()+"}");
                }
            }

            return new Instruction[] { is };
        }
        if ( stmt instanceof StackArrayStore ) {
            Instruction is;
            if ( ((StackArrayStore)stmt).getArrayType().getMachineType() == TypeInfo.TYPE_REFERENCE ) {
                is = new AASTORE();
            } else {
                switch ( ((StackArrayStore)stmt).getArrayType().getType() ) {
                    case TypeInfo.TYPE_BOOL: is = new BASTORE(); break;
                    case TypeInfo.TYPE_BYTE: is = new BASTORE(); break;
                    case TypeInfo.TYPE_CHAR: is = new CASTORE(); break;
                    case TypeInfo.TYPE_SHORT: is = new SASTORE(); break;
                    case TypeInfo.TYPE_INT: is = new IASTORE(); break;
                    case TypeInfo.TYPE_LONG: is = new LASTORE(); break;
                    case TypeInfo.TYPE_FLOAT: is = new FASTORE(); break;
                    case TypeInfo.TYPE_DOUBLE: is = new DASTORE(); break;
                    default: throw new TypeException("Invalid type for arrayload {"+
                            ((StackArrayStore)stmt).getArrayType().getTypeName()+"}");
                }
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackBinop ) {
            StackBinop binop = (StackBinop) stmt;
            Instruction is = null;
            switch ( binop.getOperand() ) {
                case StackBinop.OP_ADD:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new IADD(); break;
                        case TypeInfo.TYPE_LONG: is = new LADD(); break;
                        case TypeInfo.TYPE_FLOAT: is = new FADD(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new DADD(); break;
                    }
                    break;
                case StackBinop.OP_AND:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new IAND(); break;
                        case TypeInfo.TYPE_LONG: is = new LAND(); break;
                    }
                    break;
                case StackBinop.OP_CMP: 
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_LONG: is = new LCMP(); break;
                    }
                    break;
                case StackBinop.OP_CMPG:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_FLOAT: is = new FCMPG(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new DCMPG(); break;
                    }
                    break;
                case StackBinop.OP_CMPL:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_FLOAT: is = new FCMPL(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new DCMPL(); break;
                    }
                    break;
                case StackBinop.OP_DIV:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new IDIV(); break;
                        case TypeInfo.TYPE_LONG: is = new LDIV(); break;
                        case TypeInfo.TYPE_FLOAT: is = new FDIV(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new DDIV(); break;
                    }
                    break;
                case StackBinop.OP_LOGIC_SHIFT_RIGHT:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new IUSHR(); break;
                        case TypeInfo.TYPE_LONG: is = new LUSHR(); break;
                    }
                    break;
                case StackBinop.OP_MUL:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new IMUL(); break;
                        case TypeInfo.TYPE_LONG: is = new LMUL(); break;
                        case TypeInfo.TYPE_FLOAT: is = new FMUL(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new DMUL(); break;
                    }
                    break;
                case StackBinop.OP_OR:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new IOR(); break;
                        case TypeInfo.TYPE_LONG: is = new LOR(); break;
                    }
                    break;
                case StackBinop.OP_REMINDER:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new IREM(); break;
                        case TypeInfo.TYPE_LONG: is = new LREM(); break;
                        case TypeInfo.TYPE_FLOAT: is = new FREM(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new DREM(); break;
                    }
                    break;
                case StackBinop.OP_SHIFT_LEFT:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new ISHL(); break;
                        case TypeInfo.TYPE_LONG: is = new LSHL(); break;
                    }
                    break;
                case StackBinop.OP_SHIFT_RIGHT:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new ISHR(); break;
                        case TypeInfo.TYPE_LONG: is = new LSHR(); break;
                    }
                    break;
                case StackBinop.OP_SUB:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new ISUB(); break;
                        case TypeInfo.TYPE_LONG: is = new LSUB(); break;
                        case TypeInfo.TYPE_FLOAT: is = new FSUB(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new DSUB(); break;
                    }
                    break;
                case StackBinop.OP_XOR:
                    switch ( binop.getType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new IXOR(); break;
                        case TypeInfo.TYPE_LONG: is = new LXOR(); break;
                    }
                    break;

            }
            if ( is == null ) {
                throw new TypeException("Invalid operand or type.");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackBreakpoint ) {
            Instruction is = new BREAKPOINT();
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackCheckcast ) {
            StackCheckcast cast = (StackCheckcast) stmt;
            Instruction is = new CHECKCAST(cp.addConstant(cast.getClassConstant()));
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackConvert ) {
            StackConvert convert = (StackConvert) stmt;
            Instruction is = null;
            switch ( convert.getFromType().getMachineType() ) {
                case TypeInfo.TYPE_INT:
                    switch ( convert.getToType().getType() ) {
                        case TypeInfo.TYPE_LONG: is = new I2L(); break;
                        case TypeInfo.TYPE_FLOAT: is = new I2F(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new I2D(); break;
                        case TypeInfo.TYPE_SHORT: is = new I2S(); break;
                        case TypeInfo.TYPE_BYTE: is = new I2B(); break;
                        case TypeInfo.TYPE_BOOL: is = new I2B(); break;
                        case TypeInfo.TYPE_CHAR: is = new I2C(); break;
                    }
                    break;
                case TypeInfo.TYPE_LONG:
                    switch ( convert.getToType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new L2I(); break;
                        case TypeInfo.TYPE_FLOAT: is = new L2F(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new L2D(); break;
                    }
                    break;
                case TypeInfo.TYPE_FLOAT:
                    switch ( convert.getToType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new F2I(); break;
                        case TypeInfo.TYPE_LONG: is = new F2L(); break;
                        case TypeInfo.TYPE_DOUBLE: is = new F2D(); break;
                    }
                    break;
                case TypeInfo.TYPE_DOUBLE:
                    switch ( convert.getToType().getMachineType() ) {
                        case TypeInfo.TYPE_INT: is = new D2I(); break;
                        case TypeInfo.TYPE_LONG: is = new D2L(); break;
                        case TypeInfo.TYPE_FLOAT: is = new D2F(); break;
                    }
                    break;
            }
            if ( is == null ) {
                throw new TypeException("Invalid conversion type.");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackDup ) {
            StackDup dup = (StackDup) stmt;
            Instruction is = null;
            switch ( dup.getTypeLength() ) {
                case 1:
                    switch ( dup.getDownLength() ) {
                        case 0: is = new DUP(); break;
                        case 1: is = new DUP_X1(); break;
                        case 2: is = new DUP_X2(); break;
                    }
                    break;
                case 2:
                    switch ( dup.getDownLength() ) {
                        case 0: is = new DUP2(); break;
                        case 1: is = new DUP2_X1(); break;
                        case 2: is = new DUP2_X2(); break;
                    }
                    break;
            }
            if ( is == null ) {
                throw new TypeException("Invalid type {"+dup.getTypeLength()+"} or down {"+
                        dup.getDownLength()+"} length.");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackEntermonitor ) {
            Instruction is = new MONITORENTER();
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackExitmonitor ) {
            Instruction is = new MONITOREXIT();
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackGetField ) {
            StackGetField getfield = (StackGetField) stmt;
            Instruction is;
            if ( getfield.isStatic() ) {
                is = new GETSTATIC(cp.addConstant(getfield.getConstantField()));
            } else {
                is = new GETFIELD(cp.addConstant(getfield.getConstantField()));
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackGoto ) {
            if ( targets.length < 1 ) {
                throw new TypeException("No target for goto defined.");
            }
            Instruction is = new GOTO(targets[0]);
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackIfCmp ) {
            if ( targets.length < 1 ) {
                throw new TypeException("No target for ifcmp defined.");
            }
            StackIfCmp cmp = (StackIfCmp) stmt;
            Instruction is = null;
            switch ( cmp.getType().getMachineType() ) {
                case TypeInfo.TYPE_INT:
                    switch ( cmp.getOperand() ) {
                        case StackIfCmp.OP_EQUAL: is = new IF_ICMPEQ(targets[0]); break;
                        case StackIfCmp.OP_NOTEQUAL: is = new IF_ICMPNE(targets[0]); break;
                        case StackIfCmp.OP_GREATER: is = new IF_ICMPGT(targets[0]); break;
                        case StackIfCmp.OP_GREATER_OR_EQUAL: is = new IF_ICMPGE(targets[0]); break;
                        case StackIfCmp.OP_LESS: is = new IF_ICMPLT(targets[0]); break;
                        case StackIfCmp.OP_LESS_OR_EQUAL: is = new IF_ICMPLE(targets[0]); break;
                    }
                    break;
                case TypeInfo.TYPE_REFERENCE:
                    switch ( cmp.getOperand() ) {
                        case StackIfCmp.OP_EQUAL: is = new IF_ACMPEQ(targets[0]); break;
                        case StackIfCmp.OP_NOTEQUAL: is = new IF_ACMPNE(targets[0]); break;
                    }
                    break;
            }
            if ( is == null ) {
                throw new TypeException("Invalid operand or type for ifcmp");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackIfZero ) {
            if ( targets.length < 1 ) {
                throw new TypeException("No target for ifzero defined.");
            }
            StackIfZero cmp = (StackIfZero) stmt;
            Instruction is = null;
            switch ( cmp.getType().getMachineType() ) {
                case TypeInfo.TYPE_INT:
                    switch ( cmp.getOperand() ) {
                        case StackIfCmp.OP_EQUAL: is = new IFEQ(targets[0]); break;
                        case StackIfCmp.OP_NOTEQUAL: is = new IFNE(targets[0]); break;
                        case StackIfCmp.OP_GREATER: is = new IFGT(targets[0]); break;
                        case StackIfCmp.OP_GREATER_OR_EQUAL: is = new IFGE(targets[0]); break;
                        case StackIfCmp.OP_LESS: is = new IFLT(targets[0]); break;
                        case StackIfCmp.OP_LESS_OR_EQUAL: is = new IFLE(targets[0]); break;
                    }
                    break;
                case TypeInfo.TYPE_REFERENCE:
                    switch ( cmp.getOperand() ) {
                        case StackIfCmp.OP_EQUAL: is = new IFNULL(targets[0]); break;
                        case StackIfCmp.OP_NOTEQUAL: is = new IFNONNULL(targets[0]); break;
                    }
                    break;
            }
            if ( is == null ) {
                throw new TypeException("Invalid operand or type for if");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackIInc ) {
            StackIInc inc = (StackIInc) stmt;
            Instruction is = new IINC( varTable.getIndex(inc.getIncVariable()),inc.getIncrement());
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackInstanceof ) {
            Instruction is = new INSTANCEOF(cp.addConstant(((StackInstanceof)stmt).getClassConstant()));
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackInvoke ) {
            StackInvoke invoke = (StackInvoke) stmt;
            int cls = cp.addConstant(invoke.getMethodConstant());
            Instruction is = null;
            switch ( invoke.getInvokeType() ) {
                case StackInvoke.TYPE_INTERFACE:
                    int[] args = invoke.getParamSlots();
                    int nargs = 0;
                    for (int i = 0; i < args.length; i++) {
                        nargs += args[i];
                    }                    
                    is = new INVOKEINTERFACE(cls, nargs);
                    break;
                case StackInvoke.TYPE_SPECIAL: is = new INVOKESPECIAL(cls); break;
                case StackInvoke.TYPE_STATIC: is = new INVOKESTATIC(cls); break;
                case StackInvoke.TYPE_VIRTUAL: is = new INVOKEVIRTUAL(cls); break;
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackJSR ) {
            Instruction is = new JSR(null);
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackJSRReturn ) {
            Instruction is = new RET( varTable.getIndex(((StackJSRReturn)stmt).getRetAddressVar()) );
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackLoad ) {
            StackLoad load = (StackLoad) stmt;
            int var = varTable.getIndex(load.getVariable());
            Instruction is = null;
            switch ( load.getType().getMachineType() ) {
                case TypeInfo.TYPE_INT: is = new ILOAD(var); break;
                case TypeInfo.TYPE_LONG: is = new LLOAD(var); break;
                case TypeInfo.TYPE_FLOAT: is = new FLOAD(var); break;
                case TypeInfo.TYPE_DOUBLE: is = new DLOAD(var); break;
                case TypeInfo.TYPE_REFERENCE: is = new ALOAD(var); break;
            }
            if ( is == null ) {
                throw new TypeException("Invalid type for load.");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackLookupswitch ) {
            StackLookupswitch ls = (StackLookupswitch) stmt;
            int[] matchs = ls.getMatchs();
            if ( targets.length < matchs.length + 1 ) {
                throw new TypeException("Too few targets defined for lookupswitch");
            }
            InstructionHandle[] mTargets = new InstructionHandle[matchs.length];
            System.arraycopy(targets, 1, mTargets, 0, mTargets.length);
            Instruction is = new LOOKUPSWITCH(matchs, mTargets, targets[0]);
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackNegate ) {
            Instruction is = null;
            switch ( ((StackNegate)stmt).getType().getMachineType() ) {
                case TypeInfo.TYPE_INT: is = new INEG(); break;
                case TypeInfo.TYPE_LONG: is = new LNEG(); break;
                case TypeInfo.TYPE_FLOAT: is = new FNEG(); break;
                case TypeInfo.TYPE_DOUBLE: is = new DNEG(); break;
            }
            if ( is == null ) {
                throw new TypeException("Invalid type for negate.");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackNew ) {
            Instruction is = new NEW(cp.addConstant( ((StackNew)stmt).getObjectClass() ));
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackNewArray ) {
            StackNewArray newArray = (StackNewArray) stmt;
            Instruction is;
            if ( newArray.getArrayType().getMachineType() == TypeInfo.TYPE_REFERENCE ) {
                is = new ANEWARRAY( cp.addConstant( ((RefTypeInfo)newArray.getArrayType()).getClassConstant() ) );
            } else {
                is = new NEWARRAY( getBcelType( newArray.getArrayType() ).getType() );
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackNewMultiArray ) {
            StackNewMultiArray ma = (StackNewMultiArray) stmt;
            Instruction is = new MULTIANEWARRAY( cp.addConstant(ma.getArrayClass()), ma.getDimensions() );
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackNop ) {
            Instruction is = new NOP();
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackPop ) {
            Instruction is = null;
            switch ( ((StackPop)stmt).getPopCount() ) {
                case 1: is = new POP(); break;
                case 2: is = new POP2(); break;
            }
            if ( is == null ) {
                throw new TypeException("Invalid pop count {" + ((StackPop)stmt).getPopCount() + "}.");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackPush ) {
            StackPush push = (StackPush) stmt;
            ConstantValue value = push.getValue();
            Instruction is = null;

            switch ( value.getType().getMachineType() ) {
                case TypeInfo.TYPE_INT:
                    int iValue = value.getIntValue();
                    if ( iValue >= -1 && iValue <= 5 ) {
                        is = InstructionConstants.INSTRUCTIONS[Constants.ICONST_0 + iValue];
                    } else if ( iValue >= -128 && iValue <= 127 ) {
                        is = new BIPUSH((byte)iValue);
                    } else if ( iValue >= -32768 && iValue <= 32767 ) {
                        is = new SIPUSH((short)iValue);
                    } else {
                        is = new LDC( cp.addConstant(value) );
                    }
                    break;
                case TypeInfo.TYPE_LONG:
                    long lValue = value.getLongValue();
                    if ( lValue == 0 ) {
                        is = InstructionConstants.LCONST_0;
                    } else if ( lValue == 1 ) {
                        is = InstructionConstants.LCONST_1;
                    } else {
                        is = new LDC2_W( cp.addConstant(value) );
                    }
                    break;
                case TypeInfo.TYPE_FLOAT:
                    float fValue = value.getFloatValue();
                    if ( fValue == 0.0f ) {
                        is = InstructionConstants.FCONST_0;
                    } else if ( fValue == 1.0f ) {
                        is = InstructionConstants.FCONST_1;
                    } else if ( fValue == 2.0f ) {
                        is = InstructionConstants.FCONST_2;
                    } else {
                        is = new LDC( cp.addConstant(value) );
                    }
                    break;
                case TypeInfo.TYPE_DOUBLE:
                    double dValue = value.getDoubleValue();
                    if ( dValue == 0.0 ) {
                        is = InstructionConstants.DCONST_0;
                    } else if ( dValue == 1.0 ) {
                        is = InstructionConstants.DCONST_1;
                    } else {
                        is = new LDC2_W( cp.addConstant(value) );
                    }
                    break;
                case TypeInfo.TYPE_REFERENCE:
                    String txt = value.getTxtValue();
                    if ( txt == null ) {
                        is = InstructionConstants.ACONST_NULL;
                    } else {
                        is = new LDC( cp.addConstant(value) );
                    }
                    break;
            }
            if ( is == null ) {
                throw new TypeException("Unknown type {"+push.getType().getTypeName()+"} of pushed value.");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackPutField ) {
            StackPutField putfield = (StackPutField) stmt;
            Instruction is;
            if ( putfield.isStatic() ) {
                is = new PUTSTATIC(cp.addConstant(putfield.getConstantField()));
            } else {
                is = new PUTFIELD(cp.addConstant(putfield.getConstantField()));
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackReturn ) {
            StackReturn ret = (StackReturn) stmt;
            Instruction is = null;
            if ( ret.getType() == null ) {
                is = new RETURN();
            } else {
                switch ( ret.getType().getMachineType() ) {
                    case TypeInfo.TYPE_INT: is = new IRETURN(); break;
                    case TypeInfo.TYPE_LONG: is = new LRETURN(); break;
                    case TypeInfo.TYPE_FLOAT: is = new FRETURN(); break;
                    case TypeInfo.TYPE_DOUBLE: is = new DRETURN(); break;
                    case TypeInfo.TYPE_REFERENCE: is = new ARETURN(); break;
                }
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackStore ) {
            StackStore store = (StackStore) stmt;
            int var = varTable.getIndex(store.getVariable());
            Instruction is = null;
            switch ( store.getType().getMachineType() ) {
                case TypeInfo.TYPE_INT: is = new ISTORE(var); break;
                case TypeInfo.TYPE_LONG: is = new LSTORE(var); break;
                case TypeInfo.TYPE_FLOAT: is = new FSTORE(var); break;
                case TypeInfo.TYPE_DOUBLE: is = new DSTORE(var); break;
                case TypeInfo.TYPE_REFERENCE: is = new ASTORE(var); break;
            }
            if ( is == null ) {
                throw new TypeException("Invalid type for store.");
            }
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackSwap ) {
            Instruction is = new SWAP();
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackTableswitch ) {
            int[] matchs = ((StackTableswitch)stmt).createMatchs();
            if ( targets.length < matchs.length + 1 ) {
                throw new TypeException("Too few targets defined for tableswitch");
            }
            InstructionHandle[] mTargets = new InstructionHandle[matchs.length];
            System.arraycopy(targets, 1, mTargets, 0, mTargets.length);
            Instruction is = new TABLESWITCH(matchs, mTargets, targets[0]);
            return new Instruction[] { is };
        }
        if ( stmt instanceof StackThrow ) {
            Instruction is = new ATHROW();
            return new Instruction[] { is };
        }
        if ( stmt instanceof IdentityStmt) {
            return new Instruction[0];
        }

        return new Instruction[0];
    }

    public static Type getBcelType(TypeInfo type) {
        switch (type.getType()) {
            case TypeInfo.TYPE_BOOL: return Type.BOOLEAN;
            case TypeInfo.TYPE_BYTE: return Type.BYTE;
            case TypeInfo.TYPE_CHAR: return Type.CHAR;
            case TypeInfo.TYPE_SHORT: return Type.SHORT;
            case TypeInfo.TYPE_INT: return Type.INT;
            case TypeInfo.TYPE_LONG: return Type.LONG;
            case TypeInfo.TYPE_FLOAT: return Type.FLOAT;
            case TypeInfo.TYPE_DOUBLE: return Type.DOUBLE;
        }
        if ( type.getMachineType() == TypeInfo.TYPE_REFERENCE ) {
            return Type.OBJECT;
        }
        return null;
    }
}
