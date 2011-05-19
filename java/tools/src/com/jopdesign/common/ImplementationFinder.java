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

package com.jopdesign.common;

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.type.MethodRef;

import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface ImplementationFinder {

    /**
     * Find all methods which might get invoked for a given invokesite.
     * <p>
     * This should always return either all known implementations or none, never only a subset of implementations (like
     * only non-native implementations) to avoid incorrect devirtualization.
     * </p>
     *
     * @param callString the callstring to the the invocation, including the given invokesite. Must not be empty.
     * @return a list of possible implementations for the invocation including native methods, or an empty set if resolution fails or is not safe.
     */
    Set<MethodInfo> findImplementations(CallString callString);

}
