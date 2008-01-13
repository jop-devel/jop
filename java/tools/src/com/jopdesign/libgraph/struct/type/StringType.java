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
package com.jopdesign.libgraph.struct.type;

import com.jopdesign.libgraph.struct.ConstantClass;

/**
 * TODO make child of ObjectRef, but where to get the classInfo for String?
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StringType extends RefTypeInfo {

    public static final String DESCRIPTOR = "Ljava/lang/String;";

    public StringType() {
        super(TYPE_STRING);
    }

    public String getDescriptor() {
        return DESCRIPTOR;
    }

    public String getTypeName() {
        return "java.lang.String";
    }

    public ConstantClass getClassConstant() {
        return new ConstantClass(getTypeName());
    }
}
