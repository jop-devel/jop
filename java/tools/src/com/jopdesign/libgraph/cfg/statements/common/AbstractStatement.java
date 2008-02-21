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
package com.jopdesign.libgraph.cfg.statements.common;

import com.jopdesign.libgraph.cfg.statements.Statement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class AbstractStatement implements Statement {

    private int lineNr;
    private Map props;

    protected AbstractStatement() {
        lineNr = -1;
        props = null;
    }

    public void setLineNumber(int nr) {
        lineNr = nr;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getLineNumber() {
        return lineNr;
    }

    public Object setProperty(Object key, Object value) {
        if ( props == null ) {
            props = new HashMap(1);
        }
        return props.put(key, value);
    }

    public Object getProperty(Object key) {
        if ( props == null ) {
            return null;
        }
        return props.get(key);
    }

    public Object removeProperty(Object key) {
        if ( props == null ) {
            return null;
        }
        return props.remove(key);
    }

    public boolean containsProperty(Object key) {
        return props != null && props.containsKey(key);
    }
}
