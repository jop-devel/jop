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
import joptimizer.framework.JOPtimizer;

import java.util.Iterator;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class AbstractClassAction extends AbstractAction implements ClassAction {

    public AbstractClassAction(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    /**
     * This default implementation calls {@link #execute(com.jopdesign.libgraph.struct.ClassInfo)}
     * for all classes.
     *
     * @throws ActionException
     *
     * @see Action#execute()
     */
    public void execute() throws ActionException {
        Iterator it = getJoptimizer().getAppStruct().getClassInfos().iterator();

        while (it.hasNext()) {
            ClassInfo classInfo = (ClassInfo) it.next();
            execute(classInfo);
        }
    }

}
