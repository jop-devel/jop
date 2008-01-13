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

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class TableswitchStmt extends AbstractStatement implements SwitchStmt {

    private int lowValue;
    private int highValue;

    public TableswitchStmt(int lowValue, int highValue) {
        this.lowValue = lowValue;
        this.highValue = highValue;
    }

    public int getLowValue() {
        return lowValue;
    }

    public int getHighValue() {
        return highValue;
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
        // default-target is 0
        return highValue - lowValue + 2;
    }

    public int getValueTarget(int value) {
        if ( value <= lowValue || value >= highValue ) {
            return 0;
        }
        return value - lowValue + 1;
    }

    public int[] createMatchs() {
        int[] matchs = new int[highValue - lowValue + 1];
        for ( int i = 0; i < matchs.length; i++ ) {
            matchs[i] = lowValue + i;
        }
        return matchs;
    }

    public String getTable() {
        StringBuffer out = new StringBuffer();
        out.append("  ");
        out.append(lowValue);
        out.append(": #1\n");
        if ( highValue - lowValue > 1 ) {
            out.append("  ...\n");
        }
        out.append("  ");
        out.append(highValue);
        out.append(": #");
        out.append(highValue - lowValue + 1);
        out.append("\n");
        out.append("  default: #0");        
        return out.toString();
    }
}
