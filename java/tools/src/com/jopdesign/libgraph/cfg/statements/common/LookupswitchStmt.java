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

import com.jopdesign.libgraph.cfg.statements.SwitchStmt;

import java.util.Arrays;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class LookupswitchStmt extends AbstractStatement implements SwitchStmt {

    private int[] matchs;

    public LookupswitchStmt(int[] matchs) {
        this.matchs = matchs;
    }

    public int[] getMatchs() {
        return matchs;
    }

    public boolean canThrowException() {
        return false;
    }

    public boolean isAlwaysTaken() {
        return true;
    }

    /**
     * The first target (0) is the default target,
     * the other targets correspond to the matching values.
     * @return the number of targets.
     *
     * @see com.jopdesign.libgraph.cfg.statements.BranchStmt#getTargetCount()
     */
    public int getTargetCount() {
        // default target is target 0
        return matchs.length + 1;
    }

    public String getTable() {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < matchs.length; i++) {
            out.append("  ");
            out.append(matchs[i]);
            out.append(": #");
            out.append(i+1);
            out.append("\n");
        }
        out.append("  default: #0");
        return out.toString();
    }

    public int getValueTarget(int value) {
        int pos = Arrays.binarySearch(matchs, value);
        if ( pos < 0 || pos >= matchs.length || matchs[pos] != value ) {
            return 0;
        }
        return pos + 1;
    }
}
