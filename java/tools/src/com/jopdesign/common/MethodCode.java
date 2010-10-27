/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common;

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.BadGraphException;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MethodCode {

    /**
     * A key used to attach an {@link InvokeSite} object to an InstructionHandle.
     */
    public static final Object KEY_IH_INVOKESITE = "MethodInfo.InvokeSite";

    // Keys to attach custom values to InstructionHandles
    private static final Object KEY_INSTRUCTION_VALUE = "KeyManager.InstructionValue";
    private static final Object KEY_BLOCK_VALUE = "KeyManager.BlockValue";

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_CODE+".MethodCode");

    private final MethodInfo methodInfo;
    private final MethodGen methodGen;
    private ControlFlowGraph cfg;
    

    /**
     * Only to be used by MethodInfo.
     *
     * @param methodInfo reference to the method containing the code.
     */
    MethodCode(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
        methodGen = methodInfo.getInternalMethodGen();
    }

    public AppInfo getAppInfo() {
        return methodInfo.getAppInfo();
    }

    public ClassInfo getClassInfo() {
        return methodInfo.getClassInfo();
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public ConstantPoolGen getConstantPoolGen() {
        return methodInfo.getConstantPoolGen();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Various wrappers to BCEL methods
    //////////////////////////////////////////////////////////////////////////////

    public int getMaxStack() {
        return methodGen.getMaxStack();
    }

    public int getMaxLocals() {
        return methodGen.getMaxLocals();
    }

    public LocalVariableGen addLocalVariable(String name, Type type, int slot, InstructionHandle start, InstructionHandle end) {
        return methodGen.addLocalVariable(name, type, slot, start, end);
    }

    public LocalVariableGen addLocalVariable(String name, Type type, InstructionHandle start, InstructionHandle end) {
        return methodGen.addLocalVariable(name, type, start, end);
    }

    public void removeLocalVariable(LocalVariableGen l) {
        methodGen.removeLocalVariable(l);
    }

    public void removeLocalVariables() {
        methodGen.removeLocalVariables();
    }

    public LocalVariableGen[] getLocalVariables() {
        return methodGen.getLocalVariables();
    }

    public CodeExceptionGen[] getExceptionHandlers() {
        return methodGen.getExceptionHandlers();
    }

    public CodeExceptionGen addExceptionHandler(InstructionHandle start_pc, InstructionHandle end_pc, InstructionHandle handler_pc, ObjectType catch_type) {
        return methodGen.addExceptionHandler(start_pc, end_pc, handler_pc, catch_type);
    }

    public void removeExceptionHandler(CodeExceptionGen c) {
        methodGen.removeExceptionHandler(c);
    }

    public void removeExceptionHandlers() {
        methodGen.removeExceptionHandlers();
    }

    public void addException(String class_name) {
        methodGen.addException(class_name);
    }

    public void removeException(String c) {
        methodGen.removeException(c);
    }

    public void removeExceptions() {
        methodGen.removeExceptions();
    }

    public String[] getExceptions() {
        return methodGen.getExceptions();
    }

    public Attribute[] getAttributes() {
        return methodGen.getCodeAttributes();
    }

    public void addAttribute(Attribute a) {
        methodGen.addCodeAttribute(a);
    }

    public void removeAttribute(Attribute a) {
        methodGen.removeCodeAttribute(a);
    }

    public void removeAttributes() {
        methodGen.removeCodeAttributes();
    }


    //////////////////////////////////////////////////////////////////////////////
    // Line number handling
    //////////////////////////////////////////////////////////////////////////////


    public LineNumberGen[] getLineNumbers() {
        return methodGen.getLineNumbers();
    }

    public LineNumberTable getLineNumberTable() {
        return methodGen.getLineNumberTable(getConstantPoolGen());
    }

    public LineNumberGen addLineNumber(InstructionHandle ih, int src_line) {
        return methodGen.addLineNumber(ih, src_line);
    }

    public void removeLineNumber(LineNumberGen l) {
        methodGen.removeLineNumber(l);
    }

    public void removeLineNumbers() {
        methodGen.removeLineNumbers();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Code Access, Instruction Lists and CFG
    //////////////////////////////////////////////////////////////////////////////

    public InstructionList getInstructionList() {
        compileCFG();
        // If one only uses the IList to analyze code but does not modify it, we could keep an existing CFG.
        // Unfortunately, there is no 'const InstructionList' or 'UnmodifiableInstructionList', so we
        // can never be sure what the user will do with the list..
        cfg = null;
        return methodGen.getInstructionList();
    }

    public void setInstructionList(InstructionList il) {
        methodGen.setInstructionList(il);
        cfg = null;
    }

    public InvokeSite getInvokeSite(InstructionHandle ih) {
        InvokeSite is = (InvokeSite) ih.getAttribute(KEY_IH_INVOKESITE);
        if (is == null) {
            is = new InvokeSite(ih, this.getMethodInfo());
            ih.addAttribute(KEY_IH_INVOKESITE, is);
        }
        return is;
    }

    public void removeNOPs() {
        compileCFG();
        cfg = null;
        methodGen.removeNOPs();
    }

    public ControlFlowGraph getControlFlowGraph() {
        if ( this.cfg == null ) {
            try {
                cfg = new ControlFlowGraph(this.getMethodInfo());
            } catch (BadGraphException e) {
                // TODO handle this!
                e.printStackTrace();
            }
        }

        return cfg;
    }

    public boolean hasCFG() {
        return cfg != null;
    }

    public void compileCFG() {
        if ( cfg != null ) {
            cfg.compile();
        }
    }

    /**
     * Compile all changes and update maxStack and maxLocals.
     */
    public void compile() {
        compileCFG();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Get and set CustomValues
    //////////////////////////////////////////////////////////////////////////////

    public Object setCustomValue(InstructionHandle ih, KeyManager.CustomKey key, Object value) {
        return setCustomValue(ih, key, value, KEY_INSTRUCTION_VALUE);
    }

    public Object setCustomBlockValue(InstructionHandle ih, KeyManager.CustomKey key, Object value) {
        return setCustomValue(ih, key, value, KEY_BLOCK_VALUE);
    }

    public Object getCustomValue(InstructionHandle ih, KeyManager.CustomKey key) {
        return getCustomValue(ih, key, KEY_INSTRUCTION_VALUE);
    }

    public Object getCustomBlockValue(InstructionHandle ih, KeyManager.CustomKey key) {
        return getCustomValue(ih, key, KEY_BLOCK_VALUE);
    }

    public Object setCustomValue(InstructionHandle ih, KeyManager.CustomKey key, CallString context, Object value) {
        return setCustomValue(ih, key, context, value, KEY_INSTRUCTION_VALUE);
    }

    public Object setCustomBlockValue(InstructionHandle ih, KeyManager.CustomKey key, CallString context, Object value) {
        return setCustomValue(ih, key, context, value, KEY_BLOCK_VALUE);
    }

    public Object getCustomValue(InstructionHandle ih, KeyManager.CustomKey key, CallString context, boolean checkSuffixes) {
        return getCustomValue(ih, key, context, checkSuffixes, KEY_INSTRUCTION_VALUE);
    }

    public Object getCustomBlockValue(InstructionHandle ih, KeyManager.CustomKey key, CallString context, boolean checkSuffixes) {
        return getCustomValue(ih, key, context, checkSuffixes, KEY_BLOCK_VALUE);
    }

    public Object clearCustomKey(InstructionHandle ih, KeyManager.CustomKey key) {
        return clearCustomKey(ih, key, KEY_INSTRUCTION_VALUE);
    }

    public Object clearCustomBlockKey(InstructionHandle ih, KeyManager.CustomKey key) {
        return clearCustomKey(ih, key, KEY_BLOCK_VALUE);
    }

    public void copyCustomValues(InstructionHandle from, InstructionHandle to) {
        // TODO copy all instruction- and block-values
    }


    //////////////////////////////////////////////////////////////////////////////
    // Private area. For staff only..
    //////////////////////////////////////////////////////////////////////////////

    private Object setCustomValue(InstructionHandle ih, KeyManager.CustomKey key, Object value, Object ihKey) {
        @SuppressWarnings({"unchecked"})
        Map<KeyManager.CustomKey,Object> map = (Map<KeyManager.CustomKey, Object>) ih.getAttribute(ihKey);
        if (map == null) {
            map = new HashMap<KeyManager.CustomKey, Object>(1);
            ih.addAttribute(ihKey, map);
        }
        return map.put(key, value);
    }

    private Object getCustomValue(InstructionHandle ih, KeyManager.CustomKey key, Object ihKey) {
        @SuppressWarnings({"unchecked"})
        Map<KeyManager.CustomKey,Object> map = (Map<KeyManager.CustomKey, Object>) ih.getAttribute(ihKey);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    private Object setCustomValue(InstructionHandle ih, KeyManager.CustomKey key, CallString context, Object value, Object ihKey) {
        // TODO implement
        return null;
    }

    private Object getCustomValue(InstructionHandle ih, KeyManager.CustomKey key, CallString context, boolean checkSuffixes, Object ihKey) {
        // TODO implement
        return null;
    }

    private Object clearCustomKey(InstructionHandle ih, KeyManager.CustomKey key, Object ihKey) {
        @SuppressWarnings({"unchecked"})
        Map<KeyManager.CustomKey,Object> map = (Map<KeyManager.CustomKey, Object>) ih.getAttribute(ihKey);
        if (map == null) {
            return null;
        }
        Object value = map.remove(key);
        if (map.size() == 0) {
            ih.removeAttribute(ihKey);
        }
        return value;
    }


}
