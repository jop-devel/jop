/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.misc;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public enum Ternary {
    TRUE, FALSE, UNKNOWN;

    public static Ternary and(Ternary a, Ternary b) {
        if ( a == FALSE || b == FALSE ) return FALSE;
        if ( a == UNKNOWN || b == UNKNOWN ) return UNKNOWN;
        return TRUE;
    }

    public static Ternary or(Ternary a, Ternary b) {
        if ( a == TRUE || b == TRUE ) return TRUE;
        if ( a == UNKNOWN || b == UNKNOWN ) return UNKNOWN;
        return FALSE;
    }

    public static Ternary not(Ternary a) {
        if ( a == TRUE ) return FALSE;
        if ( a == FALSE ) return TRUE;
        return UNKNOWN;
    }

    public Ternary and(Ternary b) {
        return and(this, b);
    }

    public Ternary or(Ternary b) {
        return or(this, b);
    }

    public Ternary not() {
        return not(this);
    }

    public String toString() {
        switch (this) {
            case TRUE: return "true";
            case FALSE: return "false";
            case UNKNOWN: return "unknown";
        }
        return "invalid";
    }

    public static Ternary valueOf(boolean val) {
        return val ? TRUE : FALSE;
    }
}
