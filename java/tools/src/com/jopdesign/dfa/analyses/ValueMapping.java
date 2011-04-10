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
package com.jopdesign.dfa.analyses;

import java.io.Serializable;

public class ValueMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int WIDEN_LIMIT = 2;
    private static final int ASSIGN_LIMIT = 4;
    private static final int CONSTRAINT_LIMIT = 64;

    public Interval assigned;
    public Interval constrained;
    public Interval increment;
    public Location source;
    public int cnt;
    public int defscope;
    public boolean softinc;

    public static int scope = 0;
    public static int scopeCnt = 0;

    public ValueMapping() {
        assigned = new Interval();
        constrained = new Interval();
        increment = null;
        source = null;
        cnt = 0;
        defscope = scope;
        softinc = false;
    }

    public ValueMapping(int val) {
        assigned = new Interval(val, val);
        constrained = new Interval(val, val);
        increment = null;
        source = null;
        cnt = 0;
        defscope = scope;
        softinc = false;
    }

    public ValueMapping(ValueMapping val, boolean full) {
        assigned = new Interval(val.assigned);
        constrained = new Interval(val.constrained);

        if (full) {
            if (val.increment != null) {
                increment = new Interval(val.increment);
            } else {
                increment = null;
            }
            source = val.source;
            cnt = val.cnt;
            defscope = val.defscope;
            softinc = val.softinc;
        } else {
            increment = null;
            source = null;
            cnt = 0;
            defscope = scope;
            softinc = false;
        }
    }

    public void join(ValueMapping val) {

        //System.out.print("join: "+this+", "+val+" = ");
        if (val != null) {
            final Interval old = new Interval(assigned);

            // merge assigned values
            if (cnt > ASSIGN_LIMIT) {
                assigned = new Interval();
            } else {
                assigned.join(val.assigned);
            }
            // merge constraints
            if (cnt > CONSTRAINT_LIMIT) {
                constrained = new Interval();
            } else {
                constrained.join(val.constrained);
            }
            // apply new constraints
            assigned.constrain(constrained);
            if (cnt > WIDEN_LIMIT) {
                // widen if possible
                assigned.widen(constrained);
            }

            // merge increments
            if (increment == null) {
                increment = val.increment;
                softinc = val.softinc;
            } else if (val.increment != null) {
                increment.join(val.increment);
                if (softinc || val.softinc) {
                    increment.join(new Interval(0, 0));
                    softinc = true;
                }
            }

            if (!old.equals(assigned)) {
                cnt++;
            }

            defscope = Math.max(defscope, val.defscope);
        }

        //System.out.println(this);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValueMapping)) {
            return false;
        }

        ValueMapping m = (ValueMapping) o;

        boolean inceq;
        if (increment == null && m.increment == null) {
            inceq = true;
        } else if (increment == null || m.increment == null) {
            inceq = false;
        } else {
            inceq = increment.equals(m.increment);
        }

        @SuppressWarnings({"UnnecessaryLocalVariable"})
        boolean retval = inceq
                && assigned.equals(m.assigned)
                && constrained.equals(m.constrained)
                //&& defscope == m.defscope
                && softinc == m.softinc;

        //System.out.println("equ: "+this+" vs "+m+" -> "+retval+ " / "+(this == m));

        return retval;
    }

    public boolean compare(Object o) {
        if (this == o) {
            return true;
        }

        ValueMapping m = (ValueMapping) o;

        boolean inceq;
        if (increment == null) {
            inceq = true;
        } else if (m.increment == null) {
            inceq = false;
        } else {
            inceq = increment.compare(m.increment);
        }

        @SuppressWarnings({"UnnecessaryLocalVariable"})
        boolean retval = inceq
                && assigned.compare(m.assigned)
                && constrained.compare(m.constrained)
                //&& defscope == m.defscope
                && softinc == m.softinc;

        //System.out.println("cmp: "+this+" vs "+m+" -> "+retval+ " / "+(this == m));

        return retval;
    }

    public int hashCode() {
        return 1 + assigned.hashCode() + 31 * constrained.hashCode() + 31 * 31 * increment.hashCode();
    }

    public String toString() {
        return "<" + assigned + ", " + constrained + ", =" + source + ", #" + cnt + ", +" + increment + ", $" + defscope + ", !" + softinc + ">";
    }

    public static int computeBound(ValueMapping first, ValueMapping second) {
        // basic checks
        if (//first == null ||
                first.increment == null
                        // || second == null
                        || second.increment == null) {
            // System.out.println("no valid increment");
            return -1;
        }
        // check for boundedness
        if (!first.assigned.hasLb()
                || !first.assigned.hasUb()
                || !second.assigned.hasLb()
                || !second.assigned.hasUb()) {
            // System.out.println("unbounded");
            return -1;
        }
        // monotone increments?
        if (first.increment.getLb() * first.increment.getUb() <= 0
                || second.increment.getLb() * second.increment.getUb() <= 0) {
            // System.out.println("invalid increments");
            return -1;
        }

        int firstRange = first.assigned.getUb() - first.assigned.getLb() + 1;
        int secondRange = second.assigned.getUb() - second.assigned.getLb() + 1;

        int firstBound;
        if (first.assigned.getUb() < first.assigned.getLb()) {
            firstBound = 0; //return -1;
        } else {
            firstBound = (int) Math.ceil((double) firstRange / Math.min(Math.abs(first.increment.getUb()), Math.abs(first.increment.getLb())));
        }
        int secondBound;
        if (second.assigned.getUb() < second.assigned.getLb()) {
            secondBound = 0; //return -1;
        } else {
            secondBound = (int) Math.ceil((double) secondRange / Math.min(Math.abs(second.increment.getUb()), Math.abs(second.increment.getLb())));
        }

        return Math.max(firstBound, secondBound);
    }
}