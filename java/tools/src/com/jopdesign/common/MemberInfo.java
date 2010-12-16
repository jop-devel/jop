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
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.type.MethodRef;
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
        customValues = null;
    }

    /**
     * Just a convenience method to get the AppInfo instance.
     * @return the AppInfo singleton.
     */
    public AppInfo getAppInfo() {
        return AppInfo.getSingleton();
    }

    public abstract ClassInfo getClassInfo();

    /**
     * Get the signature object which identifies this member.
     * @return a fully qualified signature of this member.
     */
    public abstract Signature getSignature();

    public abstract ConstantPoolGen getConstantPoolGen();

    /**
     * Get only the last part of the name (i.e. the class name without package or the field/method name
     * without class prefix and descriptor).
     *
     * @return the short name of this member without prefix or descriptor.
     */
    public abstract String getShortName();

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

    public int getAccessFlags() {
        return accessFlags.getAccessFlags();
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

    public Object removeCustomValue(KeyManager.CustomKey key) {
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
    public Object setCustomValue(KeyManager.CustomKey key, Object customValue) {
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

        if ( customValues == null ) {
            customValues = new Object[getAppInfo().getKeyManager().getNumStructKeys()];
        } else if ( id >= customValues.length ) {
            customValues = Arrays.copyOf(customValues, getAppInfo().getKeyManager().getNumStructKeys());
        }

        Object oldVal = customValues[id];
        customValues[id] = customValue;
        
        return oldVal;
    }

    public Object setCustomValue(KeyManager.CustomKey key, CallString context, Object customValue) {
        return null;
    }

    public Object getCustomValue(KeyManager.CustomKey key) {
        if ( customValues == null || key == null || key.getId() >= customValues.length ) {return null;}
        return customValues[key.getId()];
    }

    public Object getCustomValue(KeyManager.CustomKey key, CallString context, boolean checkSuffixes) {
        return null;
    }

    public void copyCustomValuesFrom(MemberInfo from) {
        // TODO implement
    }


    public void setSynthetic(boolean flag) {
        // from major version 49 on, ACC_SYNTHETIC is supported
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

    /**
     * Check if this class or class member can access the given class.
     * Note that only methods are able to access local classes.
     *
     * @param classInfo the class to access.
     * @return true if this member is able to access the class.
     */
    public boolean canAccess(ClassInfo classInfo) {

        ClassInfo thisClass = getClassInfo();

        if (!classInfo.isNestedClass()) {
            // Toplevel classes can only be public or package visible
            switch (classInfo.getAccessType()) {
                case ACC_PUBLIC: return true;
                case ACC_PACKAGE: return thisClass.hasSamePackage(classInfo);
                default:
                    throw new JavaClassFormatError("Invalid access type "+classInfo.getAccessType()
                            +" of toplevel class "+thisClass.getClassName());
            }
        }

        // this is where the fun begins .. check nested class access

        // check if we inherit from an enclosing class
        ClassInfo superClass = classInfo.getInnerClassesInfo().getEnclosingSuperClassOf(thisClass, false);
        ClassInfo enclosing = classInfo;

        while (enclosing != null) {

            if ( enclosing.isLocalInnerClass() ) {
                // we can only access (the nested member classes of) a local class if we are the
                // direct enclosing method of the local class
                MethodRef methodRef = enclosing.getEnclosingMethodRef();
                return methodRef != null && this.equals(methodRef.getMethodInfo());
            }

            switch (enclosing.getAccessType()) {
                case ACC_PUBLIC: break;
                case ACC_PROTECTED:
                    // if we inherit from an enclosing class, we can access protected nested classes
                    if ( superClass != null ) { break; }
                    // else we have only package access
                case ACC_PACKAGE:
                    if (!thisClass.hasSamePackage(enclosing)) {
                        return false;
                    }
                    break;
                case ACC_PRIVATE:
                    // Can only access private nested classes if this class encloses it
                    if ( superClass == null || !superClass.equals(this) ) {
                        return false;
                    }
                    break;
                default:
                    throw new JavaClassFormatError("Invalid access type "+classInfo.getAccessType()
                            +" of class "+thisClass.getClassName());
            }

            enclosing = enclosing.getEnclosingClassInfo();

            if ( superClass != null && superClass.equals(enclosing) ) {
                // we inherit from this enclosing class, and have been able to access
                // all 'nested enclosing classes of the class to test, so we are done!
                return true;
            }
        }
        if ( superClass != null ) {
            // now this is funny .. we somehow missed the superClass. Should never happen
            throw new JavaClassFormatError("Reached toplevel class of "+classInfo.getClassName()
                    +" but we never encountered the expected enclosing class " +superClass.getClassName());
        }
        // successfully tested every enclosing class
        return true;
    }

    /**
     * Check if this class or class member has access to the given class member.
     * Note that only methods are able to access local classes.
     *
     * @param memberInfo the member to access
     * @return true if this class can access the method or field.
     */
    public boolean canAccess(ClassMemberInfo memberInfo) {
        return canAccess(memberInfo.getClassInfo(), memberInfo.getAccessType());
    }

    /**
     * Check if a member of another class with the given accessType can be accessed by this class or this
     * class member.
     * Note that only methods are able to access local classes.
     *
     * @param cls the class containing the member to check.
     * @param accessType the accessType of the member to check, as returned by {@link MemberInfo#getAccessType()}.
     * @return true if this class is allowed to access members of the given accessType of the given class.
     */
    public boolean canAccess(ClassInfo cls, int accessType) {
        // first, check if we can access the class itself
        if ( !canAccess(cls) ) {
            return false;
        }

        // now check if we can access the member
        switch (accessType) {
            case ACC_PUBLIC:
                return true;
            case ACC_PROTECTED:
                if ( getClassInfo().isInstanceOf(cls) ) {
                    return true;
                }
                // fallthrough
            case ACC_PACKAGE:
                return getClassInfo().hasSamePackage(cls);
            case ACC_PRIVATE:
                return this.equals(cls) || cls.getInnerClassesInfo().isNestedClassOf(getClassInfo(), true);
        }
        return false;
    }



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
