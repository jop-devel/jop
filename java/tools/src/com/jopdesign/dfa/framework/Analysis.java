/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Wolfgang Puffitsch
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

package com.jopdesign.dfa.framework;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.dfa.DFATool;
import org.apache.bcel.generic.InstructionHandle;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface Analysis<K, V> {

    /**
     * @return Unique ID for this analysis instance
     */
    String getId();

    ContextMap<K, V> bottom();

    ContextMap<K, V> initial(InstructionHandle stmt);

    /**
     * Initialize the analysis
     *
     * @param entry   The entry method (main)
     * @param context The initial context
     */
    void initialize(MethodInfo entry, Context context);

    ContextMap<K, V> transfer(InstructionHandle stmt,
                              FlowEdge edge,
                              ContextMap<K, V> input,
                              Interpreter<K, V> interpreter,
                              Map<InstructionHandle, ContextMap<K, V>> state);

    /**
     * {@code compare(s1, s2)} returns {@code true} if and only if both s1 and s2 have the same context
     * and s1 \subseteq s2 (s1 `join` s2 = s2)
     *
     * @param s1
     * @param s2
     * @return
     */
    boolean compare(ContextMap<K, V> s1, ContextMap<K, V> s2);

    ContextMap<K, V> join(ContextMap<K, V> s1, ContextMap<K, V> s2);

    Map getResult();

    void printResult(DFATool program);

    /**
     * serialize the analysis results to the given file.
     * precondition: {@link #getResult()} returns non-null value.
     *
     * @param cacheFile the file to serialize to
     * @throws IOException
     */
    void serializeResult(File cacheFile) throws IOException;

    /**
     * deserialize the analysis results from the given file.
     * precondition: {@link #getResult()} returns non-null value.
     *
     * @param cacheFile the file to serialize to
     * @throws IOException
     */
    Map deSerializeResult(AppInfo appInfo, File cacheFile) throws
            IOException, ClassNotFoundException, MethodNotFoundException;

    /**
     * Copy results to a new instruction handle.
     * @param newContainer
     * @param newHandles keys are old handles, values are corresponding new handles
     */
    void copyResults(MethodInfo newContainer, Map<InstructionHandle, InstructionHandle> newHandles);
}
