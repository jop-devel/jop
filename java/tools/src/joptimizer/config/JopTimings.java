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

import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackIfCmp;
import com.jopdesign.libgraph.cfg.statements.stack.StackIfZero;
import com.jopdesign.libgraph.cfg.statements.stack.StackInvoke;
import com.jopdesign.libgraph.cfg.statements.stack.StackReturn;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;

/**
 * Timings for Jop architecture.
 *
 * TODO maybe make generic by simulating microcode/javacode, or more configs..
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class JopTimings implements ArchTiming {

    private ArchConfig config;

    public JopTimings(ArchConfig config) {
        this.config = config;
    }

    public int getCycles(StackStatement stmt) {
        int cycles = 0;
        int r = getReadCycles();

        // TODO implement other statements, what do do about java-impl of statements? simulate? WCET?
        
        // some simple instructions:
        switch ( stmt.getOpcode() ) {
            case   0: return 1;     // NOP
            case 167: return 4;     // GOTO
        }

        if ( stmt instanceof StackIfCmp ) {
            return 4;
        }
        if ( stmt instanceof StackIfZero ) {
            return 4;
        }
        if ( stmt instanceof StackInvoke ) {
            StackInvoke invoke = (StackInvoke) stmt;
            switch (invoke.getInvokeType()) {
                case InvokeStmt.TYPE_VIRTUAL:
                    cycles = 100 + 2*r + ( r > 3 ? r - 3 : 0 ) + ( r > 2 ? r - 2 : 0 );
                    break;
                case InvokeStmt.TYPE_SPECIAL:
                case InvokeStmt.TYPE_STATIC:
                    cycles = 74 + r + ( r > 3 ? r - 3 : 0 ) + ( r > 2 ? r - 2 : 0 );
                    break;
                case InvokeStmt.TYPE_INTERFACE:
                    cycles = 114 + 4*r + ( r > 3 ? r - 3 : 0 ) + ( r > 2 ? r - 2 : 0 );
                    break;
            }
        } else if ( stmt instanceof StackReturn ) {
            StackReturn ret = (StackReturn) stmt;

            int length = ret.getType() != null ? ret.getType().getLength() : 0;
            cycles = 21 + 2*length + ( r > 3 ? r - 3 : 0 );
        }

        return cycles;
    }

    public int getInvokeCycles(StackInvoke stmt, int methodSize, boolean cacheMiss) {

        int cycles = getCycles(stmt);

        int load = getCacheLoadCycles(methodSize, cacheMiss);
        cycles += load > 37 ? load - 37 : 0;

        return cycles;
    }

    public int getReturnCycles(StackReturn stmt, int methodSize, boolean cacheMiss) {
        int cycles = getCycles(stmt);

        int load = getCacheLoadCycles(methodSize, cacheMiss);
        int length = stmt.getType() != null ? stmt.getType().getLength() : 0;
        cycles += load > 9 + length ? load - 9 - length : 0;

        return cycles;        
    }

    public int getCacheMemCycles(int bytes) {
        int rws = getReadCycles();
        int cw = rws > 1 ? rws - 1 : 0;

        return ((bytes + 3)/4) * (2 + cw);
    }

    private int getCacheLoadCycles(int methodSize, boolean cacheMiss) {
        int load;
        if ( cacheMiss ) {
            load = 6 + getCacheMemCycles(methodSize + 4);
        } else {
            load = 4;
        }
        return load;
    }

    private int getReadCycles() {
        int ram_cnt = config.getRamReadCycles();
        return ram_cnt > 1 ? ram_cnt - 1 : 0;
    }

    private int getWriteCycles() {
        int ram_cnt = config.getRamWriteCycles();
        return ram_cnt > 1 ? ram_cnt - 1 : 0;
    }

}
