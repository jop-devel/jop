/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007,2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.build;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.common.misc.JavaClassFormatError;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.util.InstructionFinder;

import java.util.Iterator;

/**
 * Insert bytecodes for synchronizing methods.
 *
 * @author Martin Schoeberl, Wolfgang Puffitsch
 */
public class InsertSynchronized implements ClassVisitor {

    public InsertSynchronized() {
    }

    @Override
    public boolean visitClass(ClassInfo classInfo) {

        for (MethodInfo method : classInfo.getMethods()) {
            if (!(method.isAbstract() || method.isNative())
                    && method.isSynchronized()) {
                synchronize(method);
                // TODO prevent code inserted more than once if this is executed both in the optimizer and in JOPizer
                // method.setSynchronized(false);
            }
        }
        return true;
    }

    @Override
    public void finishClass(ClassInfo classInfo) {
    }

    private void synchronize(MethodInfo method) {

        MethodCode mc = method.getCode();
        InstructionList il = mc.getInstructionList();
        InstructionFinder f;

        // prepend monitorenter (reversed order of opcodes)
        il.insert(new MONITORENTER());
        if (method.isStatic()) {
            // il.insert(new GET_CURRENT_CLASS());
            throw new JavaClassFormatError("synchronized on static methods not yet supported");
        } else {
            il.insert(new ALOAD(0));
        }
        il.setPositions();

        f = new InstructionFinder(il);
        // find return instructions and insert monitorexit
        String retInstr = "ReturnInstruction";

        for (Iterator iterator = f.search(retInstr); iterator.hasNext();) {
            InstructionHandle[] match = (InstructionHandle[]) iterator.next();
            InstructionHandle ih = match[0];
            InstructionHandle newh; // handle for inserted sequence

            if (method.isStatic()) {
                // il.insert(ih, new GET_CURRENT_CLASS());
                throw new JavaClassFormatError("synchronized on static methods not yet supported");
            } else {
                // TODO this could become a bug if JCopter ever reassigns local variable slots, then
                // we could not be sure that slot 0 holds the this reference anymore.. To be on the safe side
                // we should check if there is an xSTORE_0 somewhere in the code
                newh = il.insert(ih, new ALOAD(0));
            }
            il.insert(ih, new MONITOREXIT());

            // correct jumps
            method.getCode().retarget(ih, newh);
        }
        il.setPositions();

        method.compile();
    }

}
