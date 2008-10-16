/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.struct.bcel;

import com.jopdesign.libgraph.struct.AppClassLoader;
import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ClassInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.util.ClassPath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelClassLoader implements AppClassLoader {

    private ClassPath classPath;

    public BcelClassLoader() {
        classPath = new ClassPath(".");
    }

    public String getClassPath() {
        return classPath.toString();
    }

    public void setClassPath(String path) {
        classPath = new ClassPath(path);
    }

    public ClassInfo loadClassInfo(AppStruct appStruct, String className) throws IOException {

        JavaClass jc = createJavaClass(className);
        if ( jc == null ) {
            return null;
        }

        return new BcelClassInfo(appStruct, jc);
    }

    public ClassInfo createClassInfo(AppStruct appStruct, String className, String superClassName, boolean isInterface) {

        String filename = className.replace(".", File.separator) + ".class";
        
        int af = Constants.ACC_PUBLIC;
        if ( isInterface ) {
            af |= Constants.ACC_INTERFACE;
        }

        JavaClass jc = new ClassGen(className, superClassName, filename, af, new String[0]).getJavaClass();
        return new BcelClassInfo(appStruct, jc);
    }

    /**
     * create a Bcel JavaClass from a classname using the configured classpath.
     *
     * @param className the name of the class to load.
     * @return the bcel class.
     * @throws IOException if reading the class fails.
     */
    public JavaClass createJavaClass(String className) throws IOException {
        InputStream is = classPath.getInputStream(className);
        JavaClass javaClass = new ClassParser(is, className).parse();
        is.close();
        return javaClass;
    }

}
