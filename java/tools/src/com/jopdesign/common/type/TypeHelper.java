/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.type;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.misc.AppInfoError;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.DSTORE;
import org.apache.bcel.generic.FLOAD;
import org.apache.bcel.generic.FSTORE;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class TypeHelper {

    public static int getNumSlots(Type[] types) {
        if (types == null) return 0;
        int i = 0;
        for (Type t : types) {
            i += t.getSize();
        }
        return i;
    }

    public static int getNumInvokeSlots(MethodInfo method) {
        int slots = getNumSlots(method.getArgumentTypes());
        if (!method.isStatic()) {
            slots += 1;
        }
        return slots;
    }

    public static StoreInstruction createStoreInstruction(Type type, int slot) {
        switch (type.getType()) {
            case Constants.T_BOOLEAN:
            case Constants.T_BYTE:
            case Constants.T_CHAR:
            case Constants.T_SHORT:
            case Constants.T_INT:
                return new ISTORE(slot);
            case Constants.T_FLOAT:
                return new FSTORE(slot);
            case Constants.T_LONG:
                return new LSTORE(slot);
            case Constants.T_DOUBLE:
                return new DSTORE(slot);
            case Constants.T_OBJECT:
            case Constants.T_ARRAY:
                return new ASTORE(slot);
            default:
                throw new AppInfoError("Unsupported type "+type+" for slot "+slot);
        }
    }

    public static LoadInstruction createLoadInstruction(Type type, int slot) {
        switch (type.getType()) {
            case Constants.T_BOOLEAN:
            case Constants.T_BYTE:
            case Constants.T_CHAR:
            case Constants.T_SHORT:
            case Constants.T_INT:
                return new ILOAD(slot);
            case Constants.T_FLOAT:
                return new FLOAD(slot);
            case Constants.T_LONG:
                return new LLOAD(slot);
            case Constants.T_DOUBLE:
                return new DLOAD(slot);
            case Constants.T_OBJECT:
            case Constants.T_ARRAY:
                return new ALOAD(slot);
            default:
                throw new AppInfoError("Unsupported type "+type+" for slot "+slot);
        }
    }


    /**
     * Check if we can assign something with type 'from' to something with type 'to'.
     *
     * @see ReferenceType#isAssignmentCompatibleWith(Type)
     * @param from source type.
     * @param to target type.
     * @return true if source type can be implicitly converted to target type.
     */
    public static boolean canAssign(Type from, Type to) {

        // TODO should we do size-check first??
        if (from.equals(Type.UNKNOWN) || to.equals(Type.UNKNOWN)) return true;

        if (to.equals(Type.VOID)) return true;

        if (from.getSize() != to.getSize()) return false;

        if (from instanceof BasicType) {
            if (!(to instanceof BasicType)) return false;
            if (from.getType() == to.getType()) return true;

            switch (from.getType()) {
                case Constants.T_BOOLEAN:
                    return to.getType() == Constants.T_CHAR ||
                           to.getType() == Constants.T_SHORT ||
                           to.getType() == Constants.T_INT;
                case Constants.T_CHAR:
                    return to.getType() == Constants.T_SHORT ||
                           to.getType() == Constants.T_INT;
                case Constants.T_SHORT:
                    return to.getType() == Constants.T_INT;
                default:
                    return false;
            }
        }
        if (from instanceof ReferenceType) {
            try {
                return ((ReferenceType)from).isCastableTo(to);
            } catch (ClassNotFoundException e) {
                // TODO maybe silently ignore, just return true / false?
                throw new AppInfoError("Error checking assignment from "+from+" to "+to, e);
            }
        }
        // should not happen..
        throw new AppInfoError("Unknown Type type "+from);
    }

    /**
     * Check if an array of types is assignment-compatible to another array of types.
     * If the arrays have different length, then the suffix of the longer one is compared to the shorter
     * array.
     *
     * @see #canAssign(Type, Type)
     * @param from source types
     * @param to target types
     * @return true if the types from source can be assigned to the target
     */
    public static boolean canAssign(Type[] from, Type[] to) {
        int p1 = (from.length > to.length) ? from.length - to.length : 0;
        int p2 = (from.length < to.length) ? to.length - from.length : 0;
        while (p1 < from.length) {
            if (!canAssign(from[p1++], to[p2++])) {
                return false;
            }
        }
        return true;
    }

}
