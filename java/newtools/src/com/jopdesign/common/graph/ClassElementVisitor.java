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

package com.jopdesign.common.graph;

import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.type.ConstantClassInfo;
import com.jopdesign.common.type.ConstantDoubleInfo;
import com.jopdesign.common.type.ConstantFieldInfo;
import com.jopdesign.common.type.ConstantFloatInfo;
import com.jopdesign.common.type.ConstantIntegerInfo;
import com.jopdesign.common.type.ConstantLongInfo;
import com.jopdesign.common.type.ConstantMethodInfo;
import com.jopdesign.common.type.ConstantNameAndTypeInfo;
import com.jopdesign.common.type.ConstantStringInfo;

/**
 * This interface is used to visit a single element of a classInfo (including the class itself).
 * To visit all elements of a class, use a {@link DescendingClassTraverser}.
 *
 * TODO this interface currently serves primarily as a note to myself, is not yet complete or used!
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface ClassElementVisitor extends ClassVisitor {

    void start();

    void stop();

    boolean visitMethod(MethodInfo methodInfo);

    boolean visitField(FieldInfo fieldInfo);

    boolean visitConstantClass(ConstantClassInfo constant);

    boolean visitConstantDouble(ConstantDoubleInfo constant);

    boolean visitConstantField(ConstantFieldInfo constant);

    boolean visitConstantFloat(ConstantFloatInfo constant);

    boolean visitConstantInteger(ConstantIntegerInfo constant);

    boolean visitConstantLong(ConstantLongInfo constant);

    boolean visitConstantMethod(ConstantMethodInfo constant);

    boolean visitConstantNameAndType(ConstantNameAndTypeInfo constant);

    boolean visitConstantString(ConstantStringInfo constant);

    
}
