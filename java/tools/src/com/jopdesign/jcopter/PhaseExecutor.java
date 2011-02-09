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

package com.jopdesign.jcopter;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.tools.ConstantPoolRebuilder;
import com.jopdesign.common.tools.UsedCodeFinder;

/**
 * This is just a helper class to execute various optimizations and analyses.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class PhaseExecutor {

    private final JCopter jcopter;
    private final AppInfo appInfo;

    public PhaseExecutor(JCopter jcopter) {
        this.jcopter = jcopter;
        appInfo = AppInfo.getSingleton();
    }

    public void registerOptions(OptionGroup options) {

    }

    /**
     * Reduce the callgraph stored with AppInfo.
     * {@link AppInfo#buildCallGraph(boolean)} must have been called first.
     */
    public void reduceCallGraph() {
        // TODO perform callgraph thinning analysis
    }

    /**
     * Mark all InvokeSites which are safe to inline, or store info
     * about what needs to be done in order to inline them.
     * To get better results, reduce the callgraph first as much as possible.
     */
    public void markInlineCandidates() {
        // TODO call invoke candidate finder
    }

    /**
     * Inline all methods which do not increase the code size.
     * {@link #markInlineCandidates()} must have been run first.
     */
    public void performSimpleInline() {
    }

    /**
     * Inline all InvokeSites which are marked for inlining by an inline strategy.
     */
    public void performInline() {
    }

    /**
     * Run some simple optimizations to cleanup the bytecode without increasing its size.
     */
    public void cleanupMethodCode() {
        // TODO optimize load/store
        // TODO perform some simple peephole optimizations
        // (more complex optimizations (dead-code elimination, constant-folding,..) should
        //  go into another method..)
    }

    /**
     * Find and remove unused classes, methods and fields
     */
    public void removeUnusedMembers() {
        UsedCodeFinder ucf = new UsedCodeFinder();
        ucf.resetMarks();
        ucf.markUsedMembers();
        ucf.removeUnusedMembers();
    }

    /**
     * Rebuild all constant pools.
     */
    public void cleanupConstantPool() {
        appInfo.iterate(new ConstantPoolRebuilder());
    }
}
