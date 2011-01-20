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

import com.jopdesign.common.KeyManager.CustomKey;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.BadGraphError;
import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.common.misc.HashedString;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
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
    private static final Object KEY_INVOKESITE = new HashedString("MethodInfo.InvokeSite");
    // Keys to attach custom values to InstructionHandles
    private static final Object KEY_CUSTOMVALUES = new HashedString("KeyManager.InstructionValue");
    // Keys to attach values directly to InstructionHandles, which are not handled by KeyManager
    private static final Object KEY_LINENUMBER = new HashedString("MethodCode.LineNumber");
    private static final Object KEY_SOURCEFILE = new HashedString("MethodCode.SourceFile");
    private static final Object KEY_EXCEPTIONS = new HashedString("MethodCode.Exceptions");

    private static final Object[] MANAGED_KEYS = {KEY_INVOKESITE, KEY_LINENUMBER, KEY_SOURCEFILE, KEY_EXCEPTIONS};

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_CODE+".MethodCode");

    private final MethodInfo methodInfo;
    private final MethodGen methodGen;
    private ControlFlowGraph cfg;

    // true when the instruction list has lineNrs and exceptions attached to them.
    private boolean ilTablesLoaded;
    // true when the table attributes may be out of date.
    private boolean ilTablesDirty;

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

    /**
     * Get the length of the code attribute. This needs to compile the CFG first.
     * @see Code#length
     * @return the length of the code attribute
     */
    public int getLength() {
        prepareInstructionList(false);
        return methodGen.getMethod().getCode().getLength();
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

    /**
     * Get a table of linenumber entries.
     * If you want to get the line number of an instruction, use {@link #getLineNumber(InstructionHandle)} instead.
     *
     * @return a table of linenumber entries.
     */
    public LineNumberGen[] getLineNumbers() {
        return methodGen.getLineNumbers();
    }

    /**
     * Get the linenumber table attribute.
     * If you want to get the line number of an instruction, use {@link #getLineNumber(InstructionHandle)} instead.
     *
     * @return the linenumbers attribute.
     */
    public LineNumberTable getLineNumberTable() {
        return methodGen.getLineNumberTable(getConstantPoolGen());
    }

    /**
     * Remove a line number entry. Only use this if you manage the linenumber table yourself, else use
     * {@link #removeLineNumber(InstructionHandle)}.
     *
     * @param l the entry to remove.
     */
    public void removeLineNumber(LineNumberGen l) {
        methodGen.removeLineNumber(l);
    }

    /**
     * Remove the linenumber table attribute.
     * <p>
     * TODO if rebuildTables or compile() is called, the table might get recreated. Should be prevented
     * if this is called (?)
     * </p>
     */
    public void removeLineNumbers() {
        methodGen.removeLineNumbers();
    }

    public LineNumberGen setLineNumber(InstructionHandle ih, int src_line) {
        if (ilTablesLoaded) {
            ih.addAttribute(KEY_LINENUMBER, src_line);
            ilTablesDirty = true;
            return null;
        } else {
            // TODO check if entry exists
            return methodGen.addLineNumber(ih, src_line);
        }
    }

    public void removeLineNumber(InstructionHandle ih) {
        if (ilTablesLoaded) {
            ih.removeAttribute(KEY_LINENUMBER);
            ilTablesDirty = true;
        } else {
            // TODO remove from LineNumberTable
        }
    }

    /**
     * Get the line number of the instruction. If instruction handle attributes are not used,
     * the positions of the instruction handles must be uptodate.
     *
     * @see #getSourceFileName(InstructionHandle)
     * @param ih the instruction to check.
     * @return the line number of the instruction, or 0 if unknown.
     */
    public int getLineNumber(InstructionHandle ih) {
        if (ilTablesLoaded) {
            return (Integer)ih.getAttribute(KEY_LINENUMBER);
        } else {
            return getLineNumberTable().getSourceLine(ih.getPosition());
        }
    }

    public void setSourceFileName(InstructionHandle ih, String filename) {
        ih.addAttribute(KEY_SOURCEFILE, filename);
    }

    public String getSourceFileName(InstructionHandle ih) {
        // We cannot store SourceFile info in Tables, so we always check the InstructionHandle..
        String source = (String) ih.getAttribute(KEY_SOURCEFILE);
        if (source != null) {
            return source;
        }
        return methodInfo.getClassInfo().getSourceFileName();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Code Access, Instruction Lists and CFG
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Get the instruction list of this code. If {@link #getControlFlowGraph()} has been used before, the CFG
     * will be compiled and removed first.
     *
     * @see #rebuildTables(boolean)
     * @see #compile()
     * @param loadAttributes if true, load linenumbers and exceptions attributes to the handles, if they
     *                       are not loaded already, and a successive call to {@link #compile} will update the
     *                       tables (if not preceded by {@link #rebuildTables(boolean)}).
     *                       Use {@code false} if you do not modify the list, if you do not need to keep tables
     *                       in sync or if you update them yourself.
     * @return the instruction list of this code.
     */
    public InstructionList getInstructionList(boolean loadAttributes) {

        InstructionList list = prepareInstructionList(loadAttributes);

        // If one only uses the IList to analyze code but does not modify it, we could keep an existing CFG.
        // Unfortunately, there is no 'const InstructionList' or 'UnmodifiableInstructionList', so we
        // can never be sure what the user will do with the list, so we kill the CFG to avoid inconsistencies.
        removeCFG();

        return list;
    }

    /**
     * Set a new instruction list to this code.
     * @param il the new list
     * @param hasAttributes if true, use the linenumber and exception attributes set to the handles of the new list.
     */
    public void setInstructionList(InstructionList il, boolean hasAttributes) {
        methodGen.setInstructionList(il);
        removeCFG();
        ilTablesLoaded = hasAttributes;
        ilTablesDirty = hasAttributes;
    }

    /**
     * Check if the lineNr and exception table may be outdated and need to be rebuilt from
     * the instructionhandle attributes. This gets set when {@code getInstructionList(true)}
     * is called and reset when {@link #rebuildTables(boolean)} or {@link #compile()} gets called.
     * <p>
     * Note that this returns false when the instruction list gets modified after
     * {@link #compile()} has been called and before {@code getInstructionList(true)} has
     * been called again.
     * </p>
     * <p>
     * TODO We could add an InstructionListObserver to get notified on changes, but this still
     *      requires the user to call InstructionList.update()!
     * </p>
     *
     * @see #getInstructionList(boolean)
     * @see #compile()
     * @return true if the table attributes of this code need to be rebuilt.
     */
    public boolean needsRebuildTables() {
        return ilTablesLoaded && ilTablesDirty;
    }

    /**
     * Rebuild the linenumber and exception attributes from CustomValues from the instruction handles.
     * <p>
     * This does not check {@link #needsRebuildTables()}. If no values are set to the handles, the tables
     * will be removed.
     * </p>
     * @param clearNeedsRebuild set to false if you do want to
     */
    public void rebuildTables(boolean clearNeedsRebuild) {
        rebuildTables(methodGen.getInstructionList());
        if (clearNeedsRebuild) {
            ilTablesDirty = false;
        }
    }

    /**
     * Get the InvokeSite for an instruction handle from the code of this method.
     * This does not check if the given instruction is an invoke instruction.
     *
     * @param ih an instruction handle from this code
     * @return the InvokeSite associated with this instruction or a new one.
     */
    public InvokeSite getInvokeSite(InstructionHandle ih) {
        InvokeSite is = (InvokeSite) ih.getAttribute(KEY_INVOKESITE);
        if (is == null) {
            is = new InvokeSite(ih, this.getMethodInfo());
            ih.addAttribute(KEY_INVOKESITE, is);
        }
        return is;
    }

    public void removeNOPs() {
        prepareInstructionList(true);
        methodGen.removeNOPs();
    }

    public ControlFlowGraph getControlFlowGraph() {
        if ( this.cfg == null ) {
            try {
                cfg = new ControlFlowGraph(this.getMethodInfo());
            } catch (BadGraphException e) {
                throw new BadGraphError("Unable to create CFG for " + methodInfo, e);
            }
        }

        return cfg;
    }

    public boolean hasCFG() {
        return cfg != null;
    }

    /**
     * Remove the CFG linked to this MethodCode, dismissing all changes made to it.
     */
    public void removeCFG() {
        if (cfg != null) {
            cfg.dispose();
            cfg = null;
        }
    }

    /**
     * Compile all changes, rebuild linenumber and exception tables if needed, and update maxStack and maxLocals.
     * <p>
     * Note that {@link #needsRebuildTables()} will return false after this call until
     * {@code getInstructionList(true)} is called again.
     */
    public void compile() {
        prepareInstructionList(false);
        if (needsRebuildTables()) {
            rebuildTables(true);
        }
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Get and set CustomValues
    //////////////////////////////////////////////////////////////////////////////

    public Object setCustomValue(InstructionHandle ih, CustomKey key, Object value) {
        @SuppressWarnings({"unchecked"})
        Map<CustomKey,Object> map = (Map<CustomKey, Object>) ih.getAttribute(KEY_CUSTOMVALUES);
        if (map == null) {
            map = new HashMap<CustomKey, Object>(1);
            ih.addAttribute(KEY_CUSTOMVALUES, map);
        }
        return map.put(key, value);
    }

    public Object getCustomValue(InstructionHandle ih, CustomKey key) {
        @SuppressWarnings({"unchecked"})
        Map<CustomKey,Object> map = (Map<CustomKey, Object>) ih.getAttribute(KEY_CUSTOMVALUES);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public Object setCustomValue(InstructionHandle ih, CustomKey key, CallString context, Object value) {
        // TODO implement
        throw new AppInfoError("Not yet implemented.");
    }

    public Object getCustomValue(InstructionHandle ih, CustomKey key, CallString context, boolean checkSuffixes) {
        // TODO implement
        throw new AppInfoError("Not yet implemented.");
    }

    public Object clearCustomKey(InstructionHandle ih, CustomKey key) {
        @SuppressWarnings({"unchecked"})
        Map<CustomKey,Object> map = (Map<CustomKey, Object>) ih.getAttribute(KEY_CUSTOMVALUES);
        if (map == null) {
            return null;
        }
        Object value = map.remove(key);
        if (map.size() == 0) {
            ih.removeAttribute(KEY_CUSTOMVALUES);
        }
        return value;
    }

    public void copyCustomValues(InstructionHandle from, InstructionHandle to) {
        Object value;
        for (Object key : MANAGED_KEYS) {
            value = from.getAttribute(key);
            if (value != null) to.addAttribute(key, value);
            else to.removeAttribute(key);
        }

        @SuppressWarnings({"unchecked"})
        Map<CustomKey,Object> map = (Map<CustomKey, Object>) from.getAttribute(KEY_CUSTOMVALUES);
        if (map == null) {
            to.removeAttribute(KEY_CUSTOMVALUES);
            return;
        }
        Map<CustomKey,Object> newMap = new HashMap<CustomKey, Object>(map);
        to.addAttribute(KEY_CUSTOMVALUES, newMap);
    }


    //////////////////////////////////////////////////////////////////////////////
    // Private area. For staff only..
    //////////////////////////////////////////////////////////////////////////////

    private InstructionList prepareInstructionList(boolean loadAttributes) {
        if ( cfg != null ) {
            cfg.compile();
        }

        InstructionList list = methodGen.getInstructionList();
        if ( loadAttributes && !ilTablesLoaded ) {
            loadTableAttributes(list);
            ilTablesLoaded = true;
        }
        if ( loadAttributes ) {
            ilTablesDirty = true;
        }
        return list;
    }

    private void loadTableAttributes(InstructionList il) {
        LineNumberTable lt = methodGen.getLineNumberTable(getConstantPoolGen());
        // TODO handle missing linenumbertable, missing entries
        for (InstructionHandle ih : il.getInstructionHandles()) {
            int pos = lt.getSourceLine(ih.getPosition());
            ih.addAttribute(KEY_SOURCEFILE, pos);
        }

        // TODO load exception ranges

    }

    private void rebuildTables(InstructionList il) {
        // TODO rebuild Linenumber- and exception table
        
    }

}
