/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
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

package com.jopdesign.common.processormodel;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.config.Config;
import org.apache.bcel.generic.Instruction;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO check if this really extends JOPModel or inherited methods can be implemented as stubs.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AllocationModel extends JOPModel {

    public AllocationModel(Config configData) {
        super(configData);
    }

    public String getName() {
        return "allocation";
    }

    public MethodInfo getJavaImplementation(AppInfo ai, MethodInfo ctx, Instruction instr) {
        throw new AssertionError("allocation model model does not (yet) support java implemented methods");
    }

    public List<String> getJVMClasses() {
        return new LinkedList<String>();
    }

    public boolean isImplementedInJava(Instruction i) {
        return false;
    }
}
