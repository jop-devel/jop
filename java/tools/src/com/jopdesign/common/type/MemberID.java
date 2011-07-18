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

package com.jopdesign.common.type;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MemberInfo;
import org.apache.bcel.util.ClassPath;

import java.io.IOException;

/**
 * This is a (immutable) class to handle parsing, generating, lookups and other signature related tasks
 * for signatures including classname and/or member names.
 *
 * @see Descriptor
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MemberID {

    // alternative member separator
    public static final char ALT_MEMBER_SEPARATOR = '#';

    private final String className;
    private final String memberName;
    private final Descriptor descriptor;

    private String stringRep = null;

    /**
     * Parse a member ID, with or without classname, with or without descriptor.
     *
     * @param memberID the member ID to parse.
     * @param isClassMember If the ID is ambiguous, if true always assume that the last simple member
     *                      name is a method or field, else assume it is a class name.
     * @return the classname part of the ID.
     */
    public static String getClassName(String memberID, boolean isClassMember) {
        int pos = memberID.indexOf(ALT_MEMBER_SEPARATOR);
        // uses alternative separator, easy
        if (pos != -1) return memberID.substring(0, pos);

        pos = memberID.indexOf('(');
        if ( pos != -1 ) {
            // has a descriptor, is a method ID, strip last member part
            pos = memberID.lastIndexOf('.', pos);
            return pos != -1 ? memberID.substring(0, pos) : "";
        }

        if (isClassMember) {
            // field or class name, cannot decide, assume it is a field
            pos = memberID.lastIndexOf('.');
            return pos != -1 ? memberID.substring(0, pos) : "";
        } else {
            // assume it is a class name
            return memberID;
        }
    }

    public static String getMemberID(String className, String memberName) {
        return className + "." +  memberName;
    }

    public static String getMemberID(String className, String memberName, String descriptor) {
        return className + "." +  memberName + descriptor;
    }

    public static String getMethodSignature(String memberName, String descriptor) {
        return memberName + descriptor;
    }

    /**
     * Parse a member ID, with or without classname, with or without descriptor.
     * If the ID is ambiguous, first check if a class by that name exists, and
     * if not assume that the signature refers to a class member.
     *
     * @param memberID the member ID to parse.
     * @return a new MemberID object.
     */
    public static MemberID parse(String memberID) {
        return parse(memberID, false, AppInfo.getSingleton().getClassPath());
    }

    /**
     * Parse a member ID, with or without classname, with or without descriptor.
     *
     * @param memberID the memberID to parse.
     * @param isClassMember If the ID is ambiguous, if true always assume that the last simple member
     *                      name is a method or field, else assume it is a class name.
     * @return a new MemberID object.
     */
    public static MemberID parse(String memberID, boolean isClassMember) {
        return parse(memberID, isClassMember, null);
    }

    /**
     * Parse a member ID.
     *
     * @param memberID the member ID to parse.
     * @param classPath if the ID is ambiguous, first check if a class by that name exists in AppInfo or
     *                  in this classPath.
     * @return a new MemberID object.
     */
    public static MemberID parse(String memberID, ClassPath classPath) {
        return parse(memberID, false, classPath);
    }

    /**
     * Parse a member ID, with or without classname, with or without descriptor.
     *
     * @param memberID the member ID to parse.
     * @param isClassMember If true, always assume that the last simple member
     *                      name is a class member, if the ID is ambiguous.
     * @param classPath If not null and the ID is ambiguous, first check if a class by that name exists, and
     *                  if not assume that the ID refers to a class member. Only has an effect if
     *                  {@code isClassMember} is {@code false}.
     * @return a new MemberID object.
     */
    private static MemberID parse(String memberID, boolean isClassMember, ClassPath classPath) {
        int p1 = memberID.indexOf(ALT_MEMBER_SEPARATOR);
        int p2 = memberID.indexOf("(");

        String className = null;
        String memberName = null;
        Descriptor descriptor = null;

        if ( p1 == -1 ) {
            if ( p2 == -1 ) {
                // TODO we might want to handle array signatures too here
                // no descriptor, either not alternative syntax or no classname
                if ( isClassMember || (classPath != null && !classExists(memberID, classPath)) ) {
                    // is a class member with or without class name
                    p1 = memberID.lastIndexOf('.');
                    className  = p1 != -1 ? memberID.substring(0, p1) : null;
                    memberName = p1 != -1 ? memberID.substring(p1+1) : memberID;
                } else {
                    // assume signature is classname only
                    className = memberID;
                }
            } else {
                // we have a descriptor, this is a method signature of some sort
                p1 = memberID.lastIndexOf('.', p2);
                if (p1 != -1) {
                    className = memberID.substring(0, p1);
                    memberName = memberID.substring(p1+1,p2);
                } else {
                    memberName = memberID.substring(0,p2);
                }
                descriptor = Descriptor.parse(memberID.substring(p2));
            }
        } else {
            // alternative style with classname, easy to parse
            className = memberID.substring(0,p1);
            if ( p2 == -1 ) {
                memberName = memberID.substring(p1+1);
            } else {
                memberName = memberID.substring(p1+1, p2);
                descriptor = Descriptor.parse(memberID.substring(p2));
            }
        }

        return new MemberID(className, memberName, descriptor);
    }

    private static boolean classExists(String className, ClassPath classPath) {
        if (AppInfo.getSingleton().hasClassInfo(className)) return true;
        try {
            classPath.getClassFile(className);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }


    /**
     * Name of a Java class, field or method. Consists of the class name, the member's name and a
     * description of the member (i.e., its type).
     * It is permissible to leave out either the class name or the member name.
     *
     * @param className name of the Java class. May be {@code null}, if class name is unknown.
     *                  Separator must be '.' instead of '/'.
     * @param memberName name of the class member. If {@code null}, the instance represents a class name.
     * @param descriptor type of the class member. Must be {@code null}, if the member's name is not given
     */
    public MemberID(String className, String memberName, Descriptor descriptor) {
        this.className = className;
        this.memberName = memberName;
        this.descriptor = descriptor;
    }

    public MemberID(String className) {
        this.className = className;
        this.memberName = null;
        this.descriptor = null;
    }

    public MemberID(String className, String memberName, String descriptor) {
        this.className = className;
        this.memberName = memberName;
        this.descriptor = Descriptor.parse(descriptor);
    }

    public MemberID(String memberName, String descriptor) {
        this.className = null;
        this.memberName = memberName;
        this.descriptor = Descriptor.parse(descriptor);
    }

    public MemberID(String memberName, Descriptor descriptor) {
        this.className = null;
        this.memberName = memberName;
        this.descriptor = descriptor;
    }

    public boolean isArrayClass() {
        return className != null && className.startsWith("[");
    }

    public boolean hasClassName() {
        return className != null;
    }

    public boolean hasMemberName() {
        return memberName != null && !"".equals(memberName);
    }

    public boolean hasDescriptor() {
        return descriptor != null;
    }

    public boolean hasMethodSignature() {
        return memberName != null && descriptor != null && descriptor.isMethod();
    }

    public String getClassName() {
        return className;
    }

    /**
     * @return the member name and descriptor if set and if it is a method descriptor.
     */
    public String getMethodSignature() {
        return (memberName!=null ? memberName : "") + (descriptor!=null && descriptor.isMethod() ? descriptor : "");
    }

    public String getMemberName() {
        return memberName;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    public MemberInfo findMemberInfo(AppInfo appInfo) {
        if ( className == null ) {
            return null;
        }
        ClassInfo cls = appInfo.getClassInfo(className);
        if ( cls == null || !hasMemberName() ) {
            return cls;
        }

        if ( hasMethodSignature() ) {
            return cls.getMethodInfo(getMethodSignature());
        } else {
            return cls.getFieldInfo(memberName);
        }
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (memberName != null ? memberName.hashCode() : 0);
        result = 31 * result + (descriptor != null ? descriptor.hashCode() : 0);
        return result;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemberID memberID = (MemberID) o;

        if (className != null ? !className.equals(memberID.className) : memberID.className != null) return false;
        if (memberName != null ? !memberName.equals(memberID.memberName) : memberID.memberName != null) return false;
        if (descriptor != null ? !descriptor.equals(memberID.descriptor) : memberID.descriptor != null) return false;

        return true;
    }

    /**
     * Get a string representation of this member, using the '#' separator for
     * class members. This only includes the descriptor for methods, so that
     * the ID of a field does not include its type.
     *
     * @see #toString(boolean)
     * @return a unique representation of this member ID.
     */
    @Override
    public String toString() {
        if (stringRep == null) {
            stringRep = toString(true);
        }
    	return stringRep;
    }
    
    /**
     * String representation of the member ID. Will include the
     * class name, if present. The descriptor is only appended for methods or if it
     * is the only component of this ID.
     *
     * <p>TODO: Maybe this method should be more general, allowing to specify whether
     * the signature should include the class name? </p>
     * 
     * @param altMemberSep Whether to use '#' to separate class name and member signature
     * @return the ID string
     */
    public String toString(boolean altMemberSep) {
        StringBuffer s = new StringBuffer();
        if ( className != null ) {
            s.append(className);
        }
        if (memberName != null) {
            if ( className != null ) {            	
                s.append(altMemberSep ? ALT_MEMBER_SEPARATOR : '.');
            }
            s.append(memberName);
        }
        if ( descriptor != null && ((className == null && memberName == null) ||
                                    (descriptor.isMethod() && memberName != null) ) )
        {
            s.append(descriptor);
        }
        return s.toString();
    }

}
