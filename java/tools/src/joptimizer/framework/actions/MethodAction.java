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
package joptimizer.framework.actions;

import com.jopdesign.libgraph.struct.MethodInfo;

/**
 * An action interface for actions which can be executed on single methods.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface MethodAction extends Action {
    
    /**
     * Run this action on a single method. <br>
     *
     * @param methodInfo the method which this action should use.
     * @throws joptimizer.framework.actions.ActionException if an error occurs or this action does not implement this method.
     */
    void execute(MethodInfo methodInfo) throws ActionException;

}
