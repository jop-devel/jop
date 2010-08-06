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

package com.jopdesign.common.tools;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.MethodGen;

/**
 * This class rebuilds the constantpool of a ClassInfo, and should only be used by ClassInfo itself.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantPoolRebuilder {

    private final ConstantPoolGen oldPool;
    private final ConstantPoolGen newPool;

    public ConstantPoolRebuilder(ConstantPoolGen oldPool, ConstantPoolGen newPool) {
        this.oldPool = oldPool;
        this.newPool = newPool;
    }

    public void updateClassGen(ClassGen classGen) {

        classGen.setConstantPool(newPool);

        classGen.setClassName(classGen.getClassName());

        classGen.setSuperclassName(classGen.getSuperclassName());

        // calling getInterfaces has the side-effect of adding all interface names to the CP!
        classGen.getInterfaces();
    }

    public void updateMethodGen(MethodGen methodGen) {

        methodGen.setConstantPool(newPool);


    }

    public void updateFieldGen(FieldGen fieldGen) {

        fieldGen.setConstantPool(newPool);

        

    }


}
