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

import org.apache.bcel.generic.Type;

import javax.crypto.spec.IvParameterSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ValueTable {

    private final List<ValueInfo> locals;
    private final List<ValueInfo> stack;

    public ValueTable() {
        locals = new ArrayList<ValueInfo>(4);
        stack = new ArrayList<ValueInfo>(4);
    }

    public List<ValueInfo> getLocals() {
        return locals;
    }

    public List<ValueInfo> getStack() {
        return stack;
    }

    public ValueInfo setLocalValue(int index, ValueInfo value) {
        for (int i = locals.size(); i <= index; i++) {
            locals.add(ValueInfo.UNUSED);
        }
        ValueInfo old = locals.set(index, value);
        if (value.usesTwoSlots()) {
            if (locals.size() > index+1) {
                locals.set(index+1, ValueInfo.CONTINUED);
            } else {
                locals.add(ValueInfo.CONTINUED);
            }
        }
        return old;
    }

    public ValueInfo getLocalValue(int index) {
        if (index >= locals.size()) return ValueInfo.UNUSED;
        return locals.get(index);
    }

    public int addLocalValue(ValueInfo value) {
        int pos = locals.size();
        locals.add(value);
        if (value.usesTwoSlots()) {
            locals.add(ValueInfo.CONTINUED);
        }
        return pos;
    }

    public void push(ValueInfo value) {
        push(value, true);
    }

    public void push(ValueInfo value, boolean addContinueMarker) {
        if (Type.VOID.equals(value.getType()) && !value.isThisReference()) return;

        stack.add(value);
        if (addContinueMarker && value.usesTwoSlots()) {
            stack.add(ValueInfo.CONTINUED);
        }
    }

    /**
     * @param down number of slots down from the top slot
     * @return the value on the stack counted down from the top
     */
    public ValueInfo top(int down) {
        if (stack.size()-down-1<0) return ValueInfo.UNUSED;
        return stack.get(stack.size()-down-1);
    }

    /**
     * @return the top stack slot value.
     */
    public ValueInfo top() {
        if (stack.isEmpty()) return ValueInfo.UNUSED;
        return stack.get(stack.size()-1);
    }

    /**
     * @return remove the top slot from the stack and return it.
     */
    public ValueInfo pop() {
        if (stack.isEmpty()) return ValueInfo.UNUSED;
        return stack.remove(stack.size()-1);
    }

    /**
     * remove the top slot from the stack, or if there is a 64bit value on top, remove two slots.
     * @return the removed value.
     */
    public ValueInfo popValue() {
        ValueInfo top = pop();
        return top.isContinued() ? pop() : top; 
    }

    /**
     * remove the top n slots from the stack.
     * @param num number of slots to remove
     */
    public void pop(int num) {
        for (int i = 0; i < num; i++) {
            pop();
        }
    }

    /**
     * @return the top slot from the stack, or if there is a 64bit value on top, the second slot down.
     */
    public ValueInfo topValue() {
        ValueInfo top = top();
        return top.isContinued() ? top(1) : top;
    }

    public ValueInfo getStackEntry(int index) {
        if (stack.size() <= index) return ValueInfo.UNUSED;
        return stack.get(index);
    }

    public int getLocalsSize() {
        return locals.size();
    }

    public int getStackSize() {
        return stack.size();
    }

    public void clear() {
        locals.clear();
        stack.clear();
    }

    /**
     * Insert a value into the stack. This does not create an additional slot if the value is of category 2.
     * @param down number of slots down from the top to insert the new value into.
     * @param value the new value to insert.
     */
    public void insert(int down, ValueInfo value) {
        stack.add(stack.size()-down, value);
    }

    public void clearStack() {
        stack.clear();
    }

}
