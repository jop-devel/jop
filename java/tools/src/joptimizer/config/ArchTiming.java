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
package joptimizer.config;

import com.jopdesign.libgraph.cfg.statements.stack.StackInvoke;
import com.jopdesign.libgraph.cfg.statements.stack.StackReturn;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;

/**
 * Interface for timing calculation implementations (architecture/configuration-dependent).
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface ArchTiming {

    /**
     * Get number of cycles needed for this statement. For invokes and returns this does not
     * include the time to initialize the method cache.
     *
     * @param stmt any stackstatement.
     * @return the number of cycles needed for the statement, 0 if the statement has no Opcode.
     */
    int getCycles(StackStatement stmt);
    
    int getInvokeCycles(StackInvoke stmt, int methodSize, boolean cacheMiss);

    int getReturnCycles(StackReturn stmt, int methodSize, boolean cacheMiss);
    
    /**
     * Get the cycles needed to load a given number of bytes into the method cache.
     * This does not include any initialization or additional data, this is handled by
     * {@link #getInvokeCycles(com.jopdesign.libgraph.cfg.statements.stack.StackInvoke, int, boolean)}.
     *
     * @param bytes the number of bytes to load.
     * @return the cycles needed to load the bytes into the cache.
     */
    int getCacheMemCycles(int bytes);
}
