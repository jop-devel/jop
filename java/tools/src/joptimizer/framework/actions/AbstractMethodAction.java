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

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodInfo;
import joptimizer.framework.JOPtimizer;

import java.util.Iterator;

/**
 * A simple implementation of a {@link joptimizer.framework.actions.MethodAction}
 * which executes this action on all methods of a class or on all classes.
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class AbstractMethodAction extends AbstractClassAction implements MethodAction {

    public AbstractMethodAction(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    /**
     * This default implementation executes this action for all methods of the given class.
     *
     * @param classInfo the classinfo on which this action should be executed.
     * @throws ActionException
     *
     * @see ClassAction#execute(ClassInfo)
     */
    public void execute(ClassInfo classInfo) throws ActionException {
        Iterator it = classInfo.getMethodInfos().iterator();
        while (it.hasNext()) {
            MethodInfo methodInfo = (MethodInfo) it.next();
            execute(methodInfo);
        }
    }

}
