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

package com.jopdesign.jcopter.inline;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.optimizer.AbstractOptimizer;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.log4j.Logger;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class SimpleInliner extends AbstractOptimizer {

    private static final Logger logger = Logger.getLogger(JCopter.LOG_INLINE+".SimpleInliner");

    private final InlineHelper helper;

    private int inlineCounter;

    public SimpleInliner(JCopter jcopter, InlineConfig inlineConfig) {
        super(jcopter);
        helper = new InlineHelper(jcopter, inlineConfig);
    }

    @Override
    public void initialize() {
        inlineCounter = 0;
    }

    @Override
    public void optimizeMethod(MethodInfo method) {
        ConstantPoolGen cpg = method.getConstantPoolGen();
        InstructionList il = method.getCode().getInstructionList();

        for (InvokeSite invoke : method.getCode().getInvokeSites()) {

            CallString cs = CallString.EMPTY;
            InvokeSite is = invoke;

            while (is != null) {
                cs = cs.push(is);

                // Preliminary checks
                MethodInfo invokee = helper.devirtualize(cs);

                if (checkInvoke(cs, invokee)) {
                    is = performSimpleInline(cs, invokee);
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void printStatistics() {
        logger.info("Inlined "+inlineCounter+" invoke sites.");
    }

    private boolean checkInvoke(CallString cs, MethodInfo invokee) {

        if (invokee.getCode().getExceptionHandlers().length > 0) {
            // We do not support inlining code with exception handles (as this code would be too large anyway..)
            return false;
        }

        // ignore methods which are most certainly too large (allow for param loading, invoke and return)
        int estimate = invokee.getArgumentTypes().length * 2 + 6;
        if (invokee.getCode().getNumberOfBytes(false) > estimate) {
            return false;
        }

        if (!helper.canInline(cs, invokee)) {
            return false;
        }

        // TODO we should check if the stack is empty and if so inline anyway?
        if (helper.needsEmptyStack(cs.first(), invokee)) {
            return false;
        }

        // Other checks are done on the fly when trying to inline

        return true;
    }

    /**
     * Try to inline a simple getter, wrapper or stub method.
     * <p>
     * Note that if the inlined code is again an invoke, the InvokeSite does not change because
     * the InstructionHandle of the invoker's invoke is kept.</p>
     *
     * @param cs the callstring from the invoker to the invoke to inline (if recursive).
     * @param invokee the method to inline.
     * @return if inlining has been performed and the inlined code is again an invoke, return the invokesite from the invokee.
     */
    private InvokeSite performSimpleInline(CallString cs, MethodInfo invokee) {

        InvokeSite invokeSite = cs.first();
        MethodInfo invoker = invokeSite.getInvoker();

        // if we do not need an NP check, we can also inline code which does not throw an exception in the same way
        boolean needsNPcheck = helper.needsNullpointerCheck(invokeSite, invokee, false);

        // TODO check the code if it is possible to inline without increasing the code size


        // Do the actual inlining
        helper.prepareInlining(invoker, invokee);


        // TODO update callgraph (?) If we update the callgraph, the callstrings become invalid!
        // -> update callgraph only after we finished inlining of a toplevel invokesite;
        //    collect all invokesites to collapse into toplevel invokesite;
        //    replace old invokesite with invokesites from inlined code, add edges to not inlined methods
        //  - callstring in checks: not affected by callgraph
        //    callstrings in devirtualize: must match the way the callgraph is updated.


        return null;
    }

}

