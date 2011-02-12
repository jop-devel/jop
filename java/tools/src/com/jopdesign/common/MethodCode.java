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
import com.jopdesign.common.bcel.StackMapTable;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.InvokeNode;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.BadGraphError;
import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.common.misc.HashedString;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.processormodel.ProcessorModel;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final Object[] MANAGED_KEYS = {KEY_INVOKESITE, KEY_LINENUMBER, KEY_SOURCEFILE};

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

    /**
     * Get the length of the code attribute. This needs to compile the CFG first.
     * @see Code#length
     * @return the length of the code attribute
     */
    public int getLength() {
        prepareInstructionList();
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
     * Remove a line number entry.
     * @param l the entry to remove.
     */
    public void removeLineNumber(LineNumberGen l) {
        methodGen.removeLineNumber(l);
    }

    /**
     * Remove all line numbers.
     */
    public void removeLineNumbers() {
        methodGen.removeLineNumbers();
    }

    public LineNumberGen setLineNumber(InstructionHandle ih, int src_line) {
        LineNumberGen lg = getLineNumberEntry(ih, false);
        if (lg != null) {
            lg.setSourceLine(src_line);
            return lg;
        }
        return methodGen.addLineNumber(ih, src_line);
    }

    public LineNumberGen getLineNumberEntry(InstructionHandle ih, boolean checkPrevious) {
        InstructionHandle prev = ih.getPrev();
        while (prev != null) {
            InstructionTargeter[] targeter = ih.getTargeters();
            if (targeter != null) {
                for (InstructionTargeter t : targeter) {
                    if (t instanceof LineNumberGen) {
                        // found a linenumber attached to this
                        return (LineNumberGen) t;
                    }
                }
            }
            // no match found
            if (checkPrevious) {
                prev = prev.getPrev();
            } else {
                break;
            }
        }
        return null;
    }

    /**
     * Get the line number of the instruction. If instruction handle attributes are not used,
     * the positions of the instruction handles must be uptodate.
     *
     * @see #getSourceFileName(InstructionHandle)
     * @param ih the instruction to check.
     * @return the line number of the instruction, or -1 if unknown.
     */
    public int getLineNumber(InstructionHandle ih) {
        return getLineNumberTable().getSourceLine(ih.getPosition());
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
    // Exception handling
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Add an exception which can be thrown by this method
     * @param class_name classname of the exception
     */
    public void addException(String class_name) {
        methodGen.addException(class_name);
    }

    /**
     * Remove an exception which is no longer thrown by this method.
     * @param c classname of the exception to remove
     */
    public void removeException(String c) {
        methodGen.removeException(c);
    }

    /**
     * This method does not throw any exceptions anymore, remove all exceptions which are thrown from this method.
     */
    public void removeExceptions() {
        methodGen.removeExceptions();
    }

    /**
     * @return a list of classnames of all exceptions which this method can throw
     */
    public String[] getExceptions() {
        return methodGen.getExceptions();
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

    public boolean isExceptionRangeStart(InstructionHandle ih) {
        return !getStartingExceptionRanges(ih).isEmpty();
    }

    public boolean isExceptionRangeEnd(InstructionHandle ih) {
        return !getEndingExceptionRanges(ih).isEmpty();
    }

    public boolean isExceptionRangeTarget(InstructionHandle ih) {
        return !getTargetingExceptionRanges(ih).isEmpty();
    }

    /**
     * Get a list of all exception ranges which start at this instruction.
     * @param ih the instruction to check
     * @return a list of all exception ranges with this instruction as start instruction, or an empty list.
     */
    public List<CodeExceptionGen> getStartingExceptionRanges(InstructionHandle ih) {
        List<CodeExceptionGen> list = new ArrayList<CodeExceptionGen>();
        InstructionTargeter[] targeters = ih.getTargeters();
        if (targeters == null) return list;
        for (InstructionTargeter t : targeters) {
            if (t instanceof CodeExceptionGen) {
                CodeExceptionGen ceg = (CodeExceptionGen) t;
                if (ceg.getStartPC().equals(ih)) {
                    list.add(ceg);
                }
            }
        }
        return list;
    }

    public List<CodeExceptionGen> getEndingExceptionRanges(InstructionHandle ih) {
        List<CodeExceptionGen> list = new ArrayList<CodeExceptionGen>();
        InstructionTargeter[] targeters = ih.getTargeters();
        if (targeters == null) return list;
        for (InstructionTargeter t : targeters) {
            if (t instanceof CodeExceptionGen) {
                CodeExceptionGen ceg = (CodeExceptionGen) t;
                if (ceg.getEndPC().equals(ih)) {
                    list.add(ceg);
                }
            }
        }
        return list;
    }

    public List<CodeExceptionGen> getTargetingExceptionRanges(InstructionHandle ih) {
        List<CodeExceptionGen> list = new ArrayList<CodeExceptionGen>();
        InstructionTargeter[] targeters = ih.getTargeters();
        if (targeters == null) return list;
        for (InstructionTargeter t : targeters) {
            if (t instanceof CodeExceptionGen) {
                CodeExceptionGen ceg = (CodeExceptionGen) t;
                if (ceg.getHandlerPC().equals(ih)) {
                    list.add(ceg);
                }
            }
        }
        return list;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Code Access, Instruction Lists and CFG
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Get the instruction list of this code. If {@link #getControlFlowGraph(boolean)} has been used before, the CFG
     * will be compiled and removed first.
     *
     * @see #compile()
     * @return the instruction list of this code.
     */
    public InstructionList getInstructionList() {

        InstructionList list = prepareInstructionList();

        // If one only uses the IList to analyze code but does not modify it, we could keep an existing CFG.
        // Unfortunately, there is no 'const InstructionList' or 'UnmodifiableInstructionList', so we
        // can never be sure what the user will do with the list, so we kill the CFG to avoid inconsistencies.
        removeCFG();

        return list;
    }

    /**
     * Set a new instruction list to this code.
     * <p>
     * IMPORTANT: If you use this method, make sure to update all targeters using
     * {@link #retarget(InstructionHandle, InstructionHandle)} first, else the various tables will link
     * to invalid handlers.
     * </p>
     * @see #getInstructionList()
     * @param il the new list
     */
    public void setInstructionList(InstructionList il) {
        methodGen.getInstructionList().dispose();
        methodGen.setInstructionList(il);
        removeCFG();
    }

    /**
     * Retarget all targeters (jumps, branches, exception ranges, linenumbers,..) of a handle to a new handle.
     * @param oldHandle the old target
     * @param newHandle the new target
     */
    public void retarget(InstructionHandle oldHandle, InstructionHandle newHandle) {
        InstructionTargeter[] it = oldHandle.getTargeters();
        if (it == null) return;
        for (InstructionTargeter targeter : it) {
            targeter.updateTarget(oldHandle, newHandle);
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

    /**
     * Get a list of invoke sites in this method code. This also returns invoke sites for special
     * instructions implemented in java.
     *
     * @return a list of all invoke sites in this code.
     */
    public Set<InvokeSite> getInvokeSites() {
        Set<InvokeSite> invokes = new HashSet<InvokeSite>();
        if (hasCFG()) {
            for (CFGNode node : cfg.getGraph().vertexSet()) {
                if (node instanceof InvokeNode) {
                    invokes.add( ((InvokeNode)node).getInvokeSite() );
                }
            }
        } else {
            for (InstructionHandle ih : methodGen.getInstructionList().getInstructionHandles()) {
                if (ih.getInstruction() instanceof InvokeInstruction) {
                    invokes.add( getInvokeSite(ih) );
                } else if (getAppInfo().getProcessorModel().isImplementedInJava(ih.getInstruction())) {
                    invokes.add( getInvokeSite(ih) );
                }
            }
        }
        return invokes;
    }

    public void removeNOPs() {
        prepareInstructionList();
        methodGen.removeNOPs();
    }

    /**
     * Remove attributes related to debugging (e.g. variable name mappings, stack maps, ..)
     * except the linenumber table.
     */
    public void removeDebugAttributes() {
        removeLocalVariables();
        for (Attribute a : getAttributes()) {
            if (a instanceof StackMapTable ||
                a instanceof StackMap)
            {
                removeAttribute(a);
            }
        }
    }

    /**
     * Get the control flow graph associated with this method code or create a new one.
     * @param clean if true, compile and recreate the graph if {@link ControlFlowGraph#isClean()} returns false.
     * @return the CFG for this method.
     */
    public ControlFlowGraph getControlFlowGraph(boolean clean) {

        // clean/isClean could be made more general (like the CallGraphConfiguration)

        if ( cfg != null && clean && !cfg.isClean()) {
            cfg.compile();
            cfg = null;
        }
        if ( this.cfg == null ) {
            try {
                cfg = new ControlFlowGraph(this.getMethodInfo());
                for (AppEventHandler ah : AppInfo.getSingleton().getEventHandlers()) {
                    ah.onCreateControlFlowGraph(cfg, clean);
                }
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
     * Compile all changes, and update maxStack and maxLocals.
     */
    public void compile() {
        prepareInstructionList();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
    }

    public int getNumberOfBytes(InstructionList il) {
        int sum = 0;
        ProcessorModel pm = getAppInfo().getProcessorModel();

        for (InstructionHandle ih : il.getInstructionHandles()) {
            sum += pm.getNumberOfBytes(methodInfo, ih.getInstruction());
        }

        return sum;
    }

    /**
     * Get the length of the implementation
     *
     * @return the length in bytes
     */
    public int getNumberOfBytes() {
        if (hasCFG()) {
            return cfg.getNumberOfBytes();
        } else {
            return getNumberOfBytes(methodGen.getInstructionList());
        }
    }

    public int getNumberOfWords() {
        return MiscUtils.bytesToWords(getNumberOfBytes());
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

    private InstructionList prepareInstructionList() {
        if ( cfg != null ) {
            cfg.compile();
        }
        return methodGen.getInstructionList();
    }
}
