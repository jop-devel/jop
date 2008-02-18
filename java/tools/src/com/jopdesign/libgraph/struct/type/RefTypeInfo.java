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
 * Base type for all reference types.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class RefTypeInfo extends AbstractType {

    private boolean virtual;

    public RefTypeInfo(byte type) {
        super(type);
        virtual = true;
    }

    protected RefTypeInfo(byte type, boolean virtual) {
        super(type);
        this.virtual = virtual;
    }

    public String getMachineTypeName() {
        return "reference";
    }

    public byte getLength() {
        return 1;
    }

    public abstract ConstantClass getClassConstant();

    /**
     * Check if this is a reference to a virtual class which may be overloaded at runtime or
     * if this is a concrete implementation.
     *
     * @return true if this type may refer to any subclass.
     */
    public boolean isVirtual() {
        return virtual;
    }

}
