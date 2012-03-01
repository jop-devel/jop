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

package com.jopdesign.common.processormodel;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import org.apache.bcel.generic.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class JVMModel implements ProcessorModel {
    @Override
    public String getName() {
        return "JVM";
    }

    @Override
    public boolean isSpecialInvoke(MethodInfo ctx, Instruction i) {
        return false;
    }

    @Override
    public boolean isImplementedInJava(MethodInfo ctx, Instruction i) {
        return false;
    }

    @Override
    public MethodInfo getJavaImplementation(AppInfo ai, MethodInfo ctx, Instruction instr) {
        return null;
    }

    @Override
    public int getNativeOpCode(MethodInfo ctx, Instruction instr) {
        return instr.getOpcode();
    }

    @Override
    public int getNumberOfBytes(MethodInfo context, Instruction instruction) {
        return 0;
    }

    @Override
    public List<String> getJVMClasses() {
        return new ArrayList<String>(0);
    }

    @Override
    public List<String> getNativeClasses() {
        return new ArrayList<String>(0);
    }

    @Override
    public List<String> getJVMRoots() {
        return new ArrayList<String>(0);
    }

    @Override
    public boolean keepJVMClasses() {
        return false;
    }

    @Override
    public int getMaxMethodSize() {
        return 65535;
    }

    @Override
    public int getMaxStackSize() {
        return 65535;
    }

    @Override
    public int getMaxLocals() {
        return 65535;
    }
}
