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
package com.jopdesign.wcet.jop;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.WCETTool;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.ObjectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class ConstantCache {

    private WCETTool project;

    private Map<MethodInfo, Set<Integer>> cpoolAddressMap =
            new HashMap<MethodInfo, Set<Integer>>();
    private Map<MethodInfo, Set<Integer>> staticAddressMap =
            new HashMap<MethodInfo, Set<Integer>>();

    private void addAddress(Map<MethodInfo, Set<Integer>> map, MethodInfo mi, int address) {
        if (!map.containsKey(mi)) {
            map.put(mi, new TreeSet<Integer>());
        }
        map.get(mi).add(address);
    }

    public ConstantCache(WCETTool project) {
        this.project = project;
    }

    public ConstantCache build() {
        List<MethodInfo> methods = project.getCallGraph().getReachableImplementations(project.getTargetMethod());
        for (int i = methods.size() - 1; i >= 0; i--) {
            MethodInfo mi = methods.get(i);
            ControlFlowGraph cfg = project.getFlowGraph(mi);
            for (CFGNode n : cfg.vertexSet()) {
                BasicBlock bb = n.getBasicBlock();
                if (bb == null) continue;
                for (InstructionHandle ii : bb.getInstructions()) {
                    extractConstantAddresses(cfg, ii);
                }
            }
        }
        return this;
    }
    /* Prototyping ... */

    /**
     * PROTOTYPE status: Find the set of non-heap addresses that might be accessed by the instruction handle
     *
     * @param cfg
     * @param ih
     * @return
     */
    private void extractConstantAddresses(ControlFlowGraph cfg, InstructionHandle ih) {
        Instruction ii = ih.getInstruction();
        switch (ii.getOpcode()) {
            case Constants.PUTSTATIC:
            case Constants.GETSTATIC:
                addStaticFieldAddress(cfg, (FieldInstruction) ii);
                break;
            case Constants.LDC:
            case Constants.LDC_W:
            case Constants.LDC2_W:
                addConstantPoolAddress(cfg, (CPInstruction) ii);
                break;
            default:
                if (ii instanceof InvokeInstruction) {
                    addConstantPoolAddress(cfg, (InvokeInstruction) ii);
                }
                break;
        }
    }

    private void addStaticFieldAddress(ControlFlowGraph cfg, FieldInstruction fii) {
        AppInfo appInfo = cfg.getAppInfo();
        ConstantPoolGen cpg = cfg.getMethodInfo().getConstantPoolGen();
        String fieldName = fii.getFieldName(cpg) + fii.getSignature(cpg);
        Integer address = project.getLinkerInfo().getStaticFieldAddress(
                ((ObjectType) fii.getReferenceType(cpg)).getClassName(), fieldName);
        addAddress(staticAddressMap, cfg.getMethodInfo(), address);
    }

    private void addConstantPoolAddress(ControlFlowGraph cfg, CPInstruction ii) {
        AppInfo appInfo = cfg.getAppInfo();
        LinkerInfo linker = project.getLinkerInfo();
        Integer address = linker.getLinkInfo(cfg.getMethodInfo().getClassInfo()).getConstAddress(ii.getIndex());
        addAddress(cpoolAddressMap, cfg.getMethodInfo(), address);
    }

    public void dumpStats() {
        System.out.println("Static addresses");
        Set<Integer> allStatics = new TreeSet<Integer>();
        for (Entry<MethodInfo, Set<Integer>> addressEntry : staticAddressMap.entrySet()) {
            System.out.print("  " + addressEntry.getKey().getFQMethodName());
            for (int address : addressEntry.getValue()) {
                System.out.print(String.format(" 0x%x", address));
                allStatics.add(address);
            }
            System.out.println();
        }
        System.out.println("  Total: " + allStatics.size());
        System.out.println("Constant Pool addresses");
        Set<Integer> allConsts = new TreeSet<Integer>();
        for (Entry<MethodInfo, Set<Integer>> addressEntry : cpoolAddressMap.entrySet()) {
            System.out.print("  " + addressEntry.getKey().getFQMethodName());
            for (int address : addressEntry.getValue()) {
                System.out.print(String.format(" 0x%x", address));
                allConsts.add(address);
            }
            System.out.println();
        }
        System.out.println("  Total: " + allConsts.size());
    }
}
