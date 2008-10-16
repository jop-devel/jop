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
package com.jopdesign.libgraph.struct;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface ModifierInfo {

    public static final int ACC_PUBLIC = 1;
    public static final int ACC_PACKAGE = 2;
    public static final int ACC_PRIVATE = 3;
    public static final int ACC_PROTECTED = 4;

    boolean isPublic();

    boolean isPrivate();

    boolean isProtected();

    boolean isFinal();

    boolean isStatic();

    boolean isSynchronized();

    void setStatic(boolean val);

    void setFinal(boolean val);

    /**
     * Get the access type of this object.
     * @return one of {@link #ACC_PRIVATE}, {@link #ACC_PROTECTED}, {@link #ACC_PACKAGE} or {@link #ACC_PUBLIC}.
     */
    int getAccessType();

    /**
     * Set the access type of this object.
     * @param type one of {@link #ACC_PRIVATE}, {@link #ACC_PROTECTED}, {@link #ACC_PACKAGE} or {@link #ACC_PUBLIC}.
     */
    void setAccessType(int type);

    String getModifierString();
}
