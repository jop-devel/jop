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

package com.jopdesign.jcopter.optimizer;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.type.ConstantMethodInfo;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.jcopter.JCopter;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.log4j.Logger;

/**
 * A small optimizer to change super method invocations so that they point to the actually invoked
 * method, making invokespecial essentially behave the same way as invokestatic.
 * <p>
 * This does not not change the callgraph.
 * </p>
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class RelinkInvokesuper implements ClassVisitor {

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".RelinkInvokesuper");

    @Override
    public boolean visitClass(ClassInfo classInfo) {
        // We do not extend AbstractOptimizer, so this can be used easier in other tools
        for (MethodInfo method : classInfo.getMethods()) {
            if (method.hasCode()) {
                visitCode(method.getCode());
            }
        }
        return true;
    }

    private void visitCode(MethodCode code) {
        for (InvokeSite is : code.getInvokeSites()) {
            if (is.isInvokeSuper()) {
                relinkInvokeSuper(is);
            }
        }
    }

    private void relinkInvokeSuper(InvokeSite is) {
        // this already resolves to the correct method..
        MethodRef invokee = is.getInvokeeRef();
        MethodInfo target = invokee.getMethodInfo();
        if (target == null) {
            // .. or not (class or method excluded or unknown)
            logger.warn("Cannot try to relink invokespecial to unknown super method "+invokee);
            return;
        }
        // now simply relink instruction (no need to check if it changes)
        int index = is.getInvoker().getClassInfo().addConstantInfo(new ConstantMethodInfo(invokee));
        if (!(is.getInvokeInstruction() instanceof INVOKESPECIAL)) {
            throw new JavaClassFormatError("Invokesuper is not an invokespecial instruction!");
        }
        is.getInvokeInstruction().setIndex(index);
    }

    @Override
    public void finishClass(ClassInfo classInfo) {
    }
}
