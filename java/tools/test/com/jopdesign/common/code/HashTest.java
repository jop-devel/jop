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

package com.jopdesign.common.code;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.TestFramework;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class HashTest {

    public static void check(boolean test) {
        System.out.println(test ? "OK" : "FAIL");
    }

    public static void main(String[] args) {
        TestFramework test = new TestFramework();
        AppSetup setup =  test.setupAppSetup();
        AppInfo appInfo = test.setupAppInfo("common.code.HashTest", false);

        ClassInfo testClass = appInfo.loadClass("common.TestFramework");
        MethodInfo mainMethod = appInfo.getMainMethod();

        MethodCode code = mainMethod.getCode();

        InstructionHandle[] ih = code.getInstructionList().getInstructionHandles();

        InvokeSite i1 = code.getInvokeSite(ih[1]);
        InvokeSite i2 = code.getInvokeSite(ih[2]);
        InvokeSite i3 = code.getInvokeSite(ih[3]);

        InvokeSite i11 = code.getInvokeSite(ih[1]);
        check(i1 == i11);

        CallString c1 = new CallString(i1);
        CallString c2 = new CallString(i2);
        CallString c11 = new CallString(i1);
        check(c1.equals(c11));
        check(!c1.equals(c2));

        ExecutionContext e1 = new ExecutionContext(mainMethod, c1);
        ExecutionContext e2 = new ExecutionContext(mainMethod, c2);
        ExecutionContext e11 = new ExecutionContext(mainMethod, c11);
        check(e1.equals(e11));
        check(!e1.equals(e2));

        // TODO put stuff into maps, check contains() and get()


        // modify instruction list, check if everything still works
        InstructionList il = code.getInstructionList();
        il.insert(new ILOAD(0));
        il.insert(ih[2], new ILOAD(1));

        ih = il.getInstructionHandles();

        InvokeSite i12 = code.getInvokeSite(ih[2]);
        InvokeSite i22 = code.getInvokeSite(ih[4]);
        check(i12 == i1);
        check(i22 == i2);

        check(e1.equals(e11));
        check(!e1.equals(e2));

        il.setPositions();

        check(c1.equals(c11));
        check(!c1.equals(c2));
        check(e1.equals(e11));
        check(!e1.equals(e2));



    }
}
