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
import com.jopdesign.common.BaseInfo;
import com.jopdesign.common.ClassInfo;

/**
 * This is a helper class to handle parsing, generating, lookups and other signature related tasks
 * for signatures including classname and/or member names.
 *
 * @see Descriptor
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class Signature {

    private String className;
    private String memberName;
    private Descriptor descriptor;

    public Signature(String signature) {
        int p1 = signature.indexOf("#");
        int p2 = signature.indexOf("(");

        if ( p1 == -1 ) {
            if ( p2 == -1 ) {
                // assume classname here
                className = signature;
            } else {
                memberName = signature.substring(0,p2);
                descriptor = new Descriptor(signature.substring(p2));
            }
        } else {
            className = signature.substring(0,p1);
            if ( p2 == -1 ) {
                memberName = signature.substring(p1+1);
            } else {
                memberName = signature.substring(p1+1, p2);
                descriptor = new Descriptor(signature.substring(p2));
            }
        }
        
    }

    public Signature(String className, String memberName, Descriptor descriptor) {
        this.className = className;
        this.memberName = memberName;
        this.descriptor = descriptor;
    }

    public static String getClassName(String signature) {
        int pos = signature.indexOf("#");
        return pos == -1 ? signature : signature.substring(0, pos);
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

    public String getMemberSignature() {
        return memberName+descriptor;
    }

    public String getMemberName() {
        return memberName;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    public BaseInfo findInfo(AppInfo appInfo) {
        if ( className == null ) {
            return null;
        }
        ClassInfo cls = appInfo.getClass(className);
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
                s.append('#');
            }
            s.append(memberName);
        }
        if ( descriptor != null && (className == null || memberName != null) ) {
            s.append(descriptor);
        }
        return s.toString();
    }
}
