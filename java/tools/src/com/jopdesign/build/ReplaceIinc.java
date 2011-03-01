/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)

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
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.SIPUSH;
import org.apache.bcel.util.InstructionFinder;

import java.util.Iterator;

/**
 * @author Martin
 *         <p/>
 *         replaces IINC by ILOAD, push the constant, IADD, and ISTORE
 *         <p/>
 *         avoids issues with the Java 1.5 compiler (produces WIDE IINC) and
 *         generates faster code on JOP.
 */
public class ReplaceIinc implements ClassVisitor {

    public ReplaceIinc() {
    }

    @Override
    public boolean visitClass(ClassInfo classInfo) {

        for (MethodInfo method : classInfo.getMethods()) {
            if (!(method.isAbstract() || method.isNative())) {
                replace(method);
            }
        }
        return true;
    }

    @Override
    public void finishClass(ClassInfo classInfo) {
    }

    private void replace(MethodInfo method) {

        MethodCode mc = method.getCode();
        InstructionList il = mc.getInstructionList();
        InstructionFinder f = new InstructionFinder(il);

        for (Iterator i = f.search("IINC"); i.hasNext();) {
            InstructionHandle[] match = (InstructionHandle[]) i.next();
            InstructionHandle ih = match[0];
            IINC ii = (IINC) ih.getInstruction();
            int idx = ii.getIndex();
            int inc = ii.getIncrement();
//  	    IINC rep = new IINC(idx, inc);
            ih.setInstruction(new ILOAD(idx));
            if (inc >= -1 && inc <= 5) {
                ih = il.append(ih, new ICONST(inc));
            } else if (inc >= -128 && inc < 127) {
                ih = il.append(ih, new BIPUSH((byte) inc));
            } else if (inc >= -32768 && inc < 32767) {
                ih = il.append(ih, new SIPUSH((short) inc));
            } else {
                System.out.println("IINC constant too big");
                System.exit(-1);
            }
            ih = il.append(ih, new IADD());
            ih = il.append(ih, new ISTORE(idx));
        }

        method.compile();
    }
}
