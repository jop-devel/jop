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

/**
 * This is a (immutable) class to handle parsing, generating, lookups and other signature related tasks
 * for signatures including classname and/or member names.
 *
 * @see Descriptor
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class Signature {

    // alternative member separator
    public static final char ALT_MEMBER_SEPARATOR = '#';

    private final String className;
    private final String memberName;
    private final Descriptor descriptor;

    public static String getClassName(String signature) {
        int pos = signature.indexOf(ALT_MEMBER_SEPARATOR);
        // uses alternative separator, easy
        if (pos != -1) return signature.substring(0, pos);

        pos = signature.indexOf('(');
        if ( pos != -1 ) {
            // has a descriptor, is a method signature, strip last member part
            pos = signature.lastIndexOf('.', pos);
            return pos != -1 ? signature.substring(0, pos) : "";
        }

        // field or class name, cannot decide, assume it is a field
        pos = signature.lastIndexOf('.');
        return pos != -1 ? signature.substring(0, pos) : "";
    }

    public static String getSignature(String className, String memberName) {
        return className + "." +  memberName;
    }

    public static String getSignature(String className, String memberName, String descriptor) {
        return className + "." +  memberName + descriptor;
    }

    public static String getMemberSignature(String memberName, String descriptor) {
        return memberName + descriptor;
    }

    /**
     * Parse a signature, with or without classname, with or without descriptor.
     * If the signature is ambiguous, first check if a class by that name exists, and
     * if not assume that the signature refers to a class member.
     *
     * @param signature the signature to parse.
     * @return a new signature object.
     */
    public static Signature parse(String signature) {
        return parse(signature, false, true);
    }

    /**
     * Parse a signature, with or without classname, with or without descriptor.
     *
     * @param signature the signature to parse.
     * @param isClassMember If true, always assume that the last simple member
     *                      name is a class member, if the signature is ambiguous, else assume it is a class name.
     * @return a new signature object.
     */
    public static Signature parse(String signature, boolean isClassMember) {
        return parse(signature, isClassMember, false);
    }

    /**
     * Parse a signature, with or without classname, with or without descriptor.
     *
     * @param signature the signature to parse.
     * @param isClassMember If true, always assume that the last simple member
     *                      name is a class member, if the signature is ambiguous.
     * @param checkExists If true and the signature is ambiguous, first check if a class by that name exists, and
     *                    if not assume that the signature refers to a class member. Only has an effect if
     *                    {@code isClassMember} is {@code false}.
     * @return a new signature object.
     */
    public static Signature parse(String signature, boolean isClassMember, boolean checkExists) {
        int p1 = signature.indexOf(ALT_MEMBER_SEPARATOR);
        int p2 = signature.indexOf("(");

        String className = null;
        String memberName = null;
        Descriptor descriptor = null;

        if ( p1 == -1 ) {
            if ( p2 == -1 ) {
                // no descriptor, either not alternative syntax or no classname
                if ( isClassMember || (checkExists && !AppInfo.getSingleton().classExists(signature)) ) {
                    // is a class member with or without class name
                    p1 = signature.lastIndexOf('.');
                    className  = p1 != -1 ? signature.substring(0, p1) : null;
                    memberName = p1 != -1 ? signature.substring(p1+1) : signature;
                } else {
                    // assume signature is classname only
                    className = signature;
                }
            } else {
                // we have a descriptor, this is a method signature of some sort
                p1 = signature.lastIndexOf('.', p2);
                if (p1 != -1) {
                    className = signature.substring(0, p1);
                    memberName = signature.substring(p1+1,p2);
                } else {
                    memberName = signature.substring(0,p2);
                }
                descriptor = Descriptor.parse(signature.substring(p2));
            }
        } else {
            // alternative style with classname, easy to parse
            className = signature.substring(0,p1);
            if ( p2 == -1 ) {
                memberName = signature.substring(p1+1);
            } else {
                memberName = signature.substring(p1+1, p2);
                descriptor = Descriptor.parse(signature.substring(p2));
            }
        }

        return new Signature(className, memberName, descriptor);
    }

    public Signature(String className) {
        this.className = className;
        this.memberName = null;
        this.descriptor = null;
    }

    public Signature(String className, String memberName, String descriptor) {
        this.className = className;
        this.memberName = memberName;
        this.descriptor = Descriptor.parse(descriptor);
    }

    public Signature(String className, String memberName, Descriptor descriptor) {
        this.className = className;
        this.memberName = memberName;
        this.descriptor = descriptor;
    }

    public Signature(String memberName, String descriptor) {
        this.className = null;
        this.memberName = memberName;
        this.descriptor = Descriptor.parse(descriptor);
    }

    public Signature(String memberName, Descriptor descriptor) {
        this.className = null;
        this.memberName = memberName;
        this.descriptor = descriptor;
    }

    public boolean hasMemberName() {
        return memberName != null && !"".equals(memberName);
    }

    public boolean hasMemberSignature() {
        return memberName != null && descriptor != null;
    }

    public boolean hasClassName() {
        return className != null;
    }

    public boolean isMethodSignature() {
        return hasMemberSignature() && descriptor.isMethod();
    }

    public String getClassName() {
        return className;
    }

    /**
     * @return the member name and descriptor, if set.
     */
    public String getMemberSignature() {
        return (memberName!=null ? memberName : "") + (descriptor!=null ? descriptor : "");
    }

    public String getMemberName() {
        return memberName;
    }

    public Descriptor getMemberDescriptor() {
        return descriptor;
    }

    public MemberInfo findInfo(AppInfo appInfo) {
        if ( className == null ) {
            return null;
        }
        ClassInfo cls = appInfo.getClassInfo(className);
        if ( cls == null || !hasMemberSignature() ) {
            return cls;
        }

        if ( isMethodSignature() ) {
            return cls.getMethodInfo(getMemberSignature());
        } else {
            return cls.getFieldInfo(memberName);
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        if ( className != null ) {
            s.append(className);
        }
        if (memberName != null) {
            if ( className != null ) {
                s.append(ALT_MEMBER_SEPARATOR);
            }
            s.append(memberName);
        }
        if ( descriptor != null && (className == null || memberName != null) ) {
            s.append(descriptor);
        }
        return s.toString();
    }

}
