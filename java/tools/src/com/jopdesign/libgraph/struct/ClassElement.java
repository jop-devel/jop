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
 * This interface is for fields and methods (and maybe attributes in the future) of a class.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface ClassElement extends ModifierInfo {

    ClassInfo getClassInfo();

    String getName();

    /**
     * Get the signature of a field or method (without its name).
     * @return the signature of parameters of methods and returntype of this method or field.
     */
    String getSignature();

}
