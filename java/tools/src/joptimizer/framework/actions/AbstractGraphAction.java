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

import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.struct.MethodInfo;
import joptimizer.framework.JOPtimizer;
import org.apache.log4j.Logger;

/**
 * An abstract class for Graphactions, which implements a single transformation on a
 * graph of a method.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class AbstractGraphAction extends AbstractMethodAction implements GraphAction {

    public AbstractGraphAction(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    public void execute(MethodInfo methodInfo) throws ActionException {
        if ( methodInfo.getMethodCode() == null ) {
            return;
        }
        try {
            execute(methodInfo, methodInfo.getMethodCode().getGraph());
        } catch (GraphException e) {
            if ( getJopConfig().doIgnoreActionErrors() ) {
                Logger.getLogger(this.getClass()).warn("Could not get graph of {"+
                        methodInfo.getFQMethodName()+"}, skipping.", e);
            } else {
                throw new ActionException("Could not get graph for method {"+methodInfo.getFQMethodName()+"}.", e);
            }
        }
    }
}
