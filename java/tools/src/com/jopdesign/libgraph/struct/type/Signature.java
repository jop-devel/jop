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

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class Signature {

    private String name;
    private TypeInfo type;

    protected Signature(String name, TypeInfo type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Get the name of the field or method.
     * @return the name of this field or method.
     */
    public String getName() {
        return name;
    }

    public TypeInfo getType() {
        return type;
    }

    /**
     * get the full name of this signature, which can be used as
     * unique identifier.
     * @return a unique name for this signature.
     */
    public abstract String getFullName();

    public String toString() {
        return getFullName();
    }
}
