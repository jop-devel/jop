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

package com.jopdesign.common;

import com.jopdesign.common.bcel.AnnotationAttribute;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.generic.ConstantPoolGen;

import java.util.Arrays;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class MemberInfo {

    public static final int ACC_PUBLIC = 1;
    public static final int ACC_PACKAGE = 2;
    public static final int ACC_PRIVATE = 3;
    public static final int ACC_PROTECTED = 4;

    private final AccessFlags accessFlags;

    private Object[] customValues;

    public MemberInfo(AccessFlags flags) {
        accessFlags = flags;
        customValues = new Object[getAppInfo().getRegisteredKeyCount()];
    }

    /**
     * Just a convenience method to get the AppInfo instance.
     * @return the AppInfo singleton.
     */
    public AppInfo getAppInfo() {
        return AppInfo.getSingleton();
    }

    public abstract ClassInfo getClassInfo();

    public abstract Signature getSignature();

    public abstract String getSimpleName();

    public boolean isPublic() {
        return accessFlags.isPublic();
    }

    public boolean isPrivate() {
        return accessFlags.isPrivate();
    }

    public boolean isProtected() {
        return accessFlags.isProtected();
    }

    public boolean isFinal() {
        return accessFlags.isFinal();
    }

    public boolean isStatic() {
        return accessFlags.isStatic();
    }

    public void setStatic(boolean val) {
        accessFlags.isStatic(val);
    }

    public void setFinal(boolean val) {
        accessFlags.isFinal(val);
    }

    /**
     * Get the access type of this object.
     * @return one of {@link #ACC_PRIVATE}, {@link #ACC_PROTECTED}, {@link #ACC_PACKAGE} or {@link #ACC_PUBLIC}.
     */
    public int getAccessType() {
        if ( isPublic() ) {
            return ACC_PUBLIC;
        }
        if ( isPrivate() ) {
            return ACC_PRIVATE;
        }
        if ( isProtected() ) {
            return ACC_PROTECTED;
        }
        return ACC_PACKAGE;
    }

    /**
     * Set the access type of this object.
     * @param type one of {@link #ACC_PRIVATE}, {@link #ACC_PROTECTED}, {@link #ACC_PACKAGE} or {@link #ACC_PUBLIC}.
     */
    public void setAccessType(int type) {
        int af = accessFlags.getAccessFlags() & ~(Constants.ACC_PRIVATE|Constants.ACC_PROTECTED|Constants.ACC_PUBLIC);
        switch (type) {
            case ACC_PRIVATE: af |= Constants.ACC_PRIVATE; break;
            case ACC_PROTECTED: af |= Constants.ACC_PROTECTED; break;
            case ACC_PUBLIC: af |= Constants.ACC_PUBLIC; break;
        }
        accessFlags.setAccessFlags(af);
    }

    public String getModifierString() {
        StringBuffer out = new StringBuffer();

        if ( isPrivate() ) {
            out.append("private ");
        }
        if ( isProtected() ) {
            out.append("protected ");
        }
        if ( isPublic() ) {
            out.append("public ");
        }
        if ( accessFlags.isSynchronized() ) {
            out.append("synchronized ");
        }
        if ( isStatic() ) {
            out.append("static ");
        }
        if ( isFinal() ) {
            out.append("final ");
        }
        if ( accessFlags.isAbstract() ) {
            out.append("abstract ");
        }
        return out.toString();
    }

    public Object removeCustomValue(AppInfo.CustomKey key) {
        return setCustomValue(key, null);
    }

    /**
     * Sets a new custom info value for a key.
     * Setting null as value has the same effect as removing the key.
     *
     * @param key The key to set the new value for
     * @param customValue the new value to set, or null to unset the value.
     * @return the old value, or null if not set previously.
     */
    public Object setCustomValue(AppInfo.CustomKey key, Object customValue) {
        // We could use generics here, and even use customValue.class as key, but
        // 1) using class as key makes it impossible to attach the same CustomValue class
        //    with different values multiple times,
        // 2) using generics like 'public <T extends CustomClassInfo> T getCustomValue() .. ' does
        //    not work since Java removes the generics type-info at compile-time, its not possible
        //    to access T.class or do 'instanceof T' or even 'try { return (T) value; } catch (Exception e) ..',
        //    therefore a possible type conflict must always(!) be handled at the callsite, so we may as well make
        //    the cast explicit at the callsite.

        if ( key == null ) {
            return null;
        }

        int id = key.getId();

        if ( id >= customValues.length ) {
            customValues = Arrays.copyOf(customValues, getAppInfo().getRegisteredKeyCount());
        }

        Object oldVal = customValues[id];
        customValues[id] = customValue;
        
        return oldVal;
    }

    public Object getCustomValue(AppInfo.CustomKey key) {
        if ( key == null || key.getId() >= customValues.length ) {return null;}
        return customValues[key.getId()];
    }

    public void setSynthetic(boolean flag) {
        // from version 49 on, ACC_SYNTHETIC is supported
        Synthetic s = findSynthetic();
        if ( getClassInfo().getMajor() < 49 ) {
            if ( flag ) {
                if ( s == null ) {
                    ConstantPoolGen cpg = getClassInfo().getConstantPoolGen();
                    int index = cpg.addUtf8("Synthetic");
                    addAttribute(new Synthetic(index, 0, new byte[0], cpg.getConstantPool()));
                }
            } else {
                if ( s != null ) {
                    removeAttribute(s);
                }
            }
        } else {
            accessFlags.isSynthetic(flag);
            if ( !flag && s != null ) {
                removeAttribute(s);
            }
        }
    }

    public boolean isSynthetic() {
        if (accessFlags.isSynthetic()) {
            return true;
        }
        Synthetic s = findSynthetic();
        return s != null;
    }

    public void setDeprecated(boolean flag) {
        if (flag) {
            if (findDeprecated() == null) {
                ConstantPoolGen cpg = getClassInfo().getConstantPoolGen();
                int index = cpg.addUtf8("Deprecated"); 
                addAttribute(new org.apache.bcel.classfile.Deprecated(index, 0, new byte[0], cpg.getConstantPool()));
            }
        } else {
            org.apache.bcel.classfile.Deprecated d = findDeprecated();
            if ( d != null ) {
                removeAttribute(d);
            }
        }
    }

    public boolean isDeprecated() {
        return findDeprecated() != null;
    }

    public AnnotationAttribute getAnnotation(boolean visible) {
        for (Attribute a : getAttributes()) {
            if ( a instanceof AnnotationAttribute ) {
                if ( ((AnnotationAttribute)a).isVisible() == visible ) {
                    return (AnnotationAttribute) a;
                }
            }
        }
        return null;
    }

    public abstract Attribute[] getAttributes();

    public abstract void addAttribute(Attribute a);

    public abstract void removeAttribute(Attribute a);



    private Synthetic findSynthetic() {
        for (Attribute a : getAttributes()) {
            if ( a instanceof Synthetic ) {
                return (Synthetic) a;
            }
        }
        return null;
    }

    private org.apache.bcel.classfile.Deprecated findDeprecated() {
        for (Attribute a : getAttributes()) {
            if ( a instanceof org.apache.bcel.classfile.Deprecated ) {
                return (org.apache.bcel.classfile.Deprecated) a;
            }
        }
        return null;
    }
}
