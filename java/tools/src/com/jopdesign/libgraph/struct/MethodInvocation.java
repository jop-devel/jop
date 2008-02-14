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
package com.jopdesign.libgraph.struct;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Contains information about a method call.
 * TODO store additional infos like lineNr,.. ?
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class MethodInvocation {
    
    private MethodInfo invoker;
    private ClassInfo invokedClass;
    private MethodInfo invokedMethod;
    private boolean special;
    private int instructionIdx;

    public MethodInvocation(MethodInfo invoker, ClassInfo invokedClass, MethodInfo invokedMethod, boolean special) {
        this.invoker = invoker;
        this.invokedClass = invokedClass;
        this.invokedMethod = invokedMethod;
        this.special = special;
        instructionIdx = -1;
    }

    public MethodInfo getInvoker() {
        return invoker;
    }

    /**
     * get the class of which the method is invoked.
     * This may not be the same as getInvokedMethod().getClassInfo(), as the invoked
     * method may be inherited.
     * @return the class which is invoked.
     */
    public ClassInfo getInvokedClass() {
        return invokedClass;
    }

    public MethodInfo getInvokedMethod() {
        return invokedMethod;
    }

    public boolean isSpecial() {
        return special;
    }

    /**
     * return index of invoke instruction, or -1 if not known.
     * @return index of instruction, or -1 if not set.
     */
    public int getInstructionIndex() {
        return instructionIdx;
    }


    /**
     * set the index of the invoke instruction in the method instruction list.
     * @param instructionIdx indes of the invoke instruction.
     */
    public void setInstructionIndex(int instructionIdx) {
        this.instructionIdx = instructionIdx;
    }

    public Set findImplementations() {

        Set impls;

        if ( special ) {
            impls = resolveSpecialInvoke();
        } else if ( invokedMethod.isInterface() ) {
            impls = resolveVirtualInvoke();
        } else if ( invokedMethod.isStatic() || invokedMethod.isFinal() || !invokedMethod.isOverwritten() ) {
            impls = Collections.singleton(invokedMethod);
        } else {
            impls = resolveVirtualInvoke();
        }
        
        return impls;
    }

    private Set resolveSpecialInvoke() {
        // TODO implement correct method resolution
        return Collections.singleton(invokedMethod);
    }

    private Set resolveVirtualInvoke() {

        Set impls = new HashSet();
        Set visited = new HashSet();
        List queue = new LinkedList();

        impls.add(invokedMethod);
        queue.addAll(invokedClass.getSubClasses());

        // Go down all classes/interfaces, collect all implementations
        while ( !queue.isEmpty() ) {
            ClassInfo cls = (ClassInfo) queue.remove(0);

            // needed for interface hierarchies
            if ( visited.contains(cls.getClassName()) ) {
                continue;
            }
            visited.add(cls.getClassName());

            MethodInfo method = cls.getMethodInfo(invokedMethod.getName(), invokedMethod.getSignature());
            if ( method != null ) {
                impls.add(method);
            }

            queue.addAll(cls.getSubClasses());
        }

        return impls;
    }

    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("Invocation: ");
        out.append(invoker.getFQMethodName());
        out.append(" [");
        out.append(instructionIdx);
        out.append("] -> ");
        out.append(invokedMethod.getFQMethodName());
        return out.toString();
    }

}
