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
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ValueInfo {

    /**
     * CONTINUED is for the higher slot or 64bit values (don't have a better name for now..)
     */
    public enum ValueFlag { LIVE, CONTINUED, UNUSED }

    public static final ValueInfo UNUSED = new ValueInfo(ValueFlag.UNUSED);
    public static final ValueInfo CONTINUED = new ValueInfo(ValueFlag.CONTINUED);

    private final ValueFlag flag;
    private Type type;

    private MethodInfo thisRef;
    private int paramNr = -1;
    private ConstantInfo constantValue;

    // TODO support tracking of copies, support removal of copy-reference on write ?

    private ValueInfo(ValueFlag flag) {
        this.flag = flag;
    }

    public ValueInfo(Type type) {
        this.type = type;
        this.flag = ValueFlag.LIVE;
    }

    public ValueInfo(Type type, ConstantInfo constantValue) {
        this.type = type;
        this.flag = ValueFlag.LIVE;
        this.constantValue = constantValue;
    }

    public ValueInfo(ConstantInfo constantValue) {
        this.type = constantValue.getType();
        this.flag = ValueFlag.LIVE;
        this.constantValue = constantValue;
    }

    public ValueInfo(Type type, int paramNr) {
        this.type = type;
        this.flag = ValueFlag.LIVE;
        this.paramNr = paramNr;
    }

    public ValueInfo(Type type, FieldRef staticField) {
        this.type = type;
        this.flag = ValueFlag.LIVE;
        this.constantValue = new ConstantFieldInfo(staticField);
    }

    public ValueInfo(MethodInfo thisRef) {
        this.type = thisRef.getType();
        this.flag = ValueFlag.LIVE;
        this.thisRef = thisRef;
    }

    public ValueFlag getFlag() {
        return flag;
    }

    public boolean isThisReference() {
        return thisRef != null;
    }

    public boolean isNullReference() {
        return constantValue == null && Type.NULL.equals(type);
    }

    public boolean isParamReference() {
        return paramNr != -1;
    }

    public boolean isConstantValue() {
        return (constantValue != null && !(constantValue instanceof ConstantFieldInfo))
               || Type.NULL.equals(type);
    }

    public boolean isUnused() {
        return flag == ValueFlag.UNUSED;
    }

    public boolean isStaticFieldReference() {
        return constantValue != null && constantValue instanceof ConstantFieldInfo;
    }

    public boolean isContinued() {
        return flag == ValueFlag.CONTINUED;
    }

    public Type getType() {
        return type;
    }

    public boolean usesTwoSlots() {
        return type != null && type.getSize() == 2;
    }

    public TypeInfo getTypeInfo() {
        return TypeInfo.getTypeInfo(type);
    }

    public MethodInfo getThisRef() {
        return thisRef;
    }

    public FieldRef getStaticFieldRef() {
        return isStaticFieldReference() ? ((ConstantFieldInfo)constantValue).getValue() : null;
    }

    /**
     * @return the number of the parameter this value represents, or -1 if this value
     *         is not a parameter (or if it is a 'this' reference).
     */
    public int getParamNr() {
        return paramNr;
    }

    public ConstantInfo getConstantValue() {
        return constantValue;
    }
}
