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

package com.jopdesign.common.code;

import com.jopdesign.common.code.InstructionInterpreter.Edge;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;

/**
* @author Stefan Hepp (stefan@stefant.org)
*/
public interface InstructionAnalysis<T> {

    T bottom();

    T initial(InstructionHandle entry);

    /**
     * @param exceptionHandler the exception handler entry
     * @return the initial value for the exceptionHandler.getHandlerPC() instruction.
     */
    T initial(CodeExceptionGen exceptionHandler);

    /**
     * @param tailValue the old value for the tail instruction.
     * @param edge the edge from tail to the head instruction to transfer this value for.
     * @return the new value for head.
     */
    T transfer(T tailValue, Edge edge);

    boolean compare(T transferred, T oldValue);

    T join(T transferred, T oldValue);
}
