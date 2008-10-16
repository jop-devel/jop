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
package com.jopdesign.libgraph.demo;

import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.TransitiveHullLoader;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.bcel.BcelClassLoader;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 * A simple Hello World program for libgraph.
 * The working directory of the application should be the classpath of the application.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class HelloWorld {

    private static AppStruct appStruct;

    public static void main(String[] args) {

        // ***** Set everything up *****

        // Log4j does not need much initialization, but to simplify the setup, load config
        // file from this package instead of the default file (from the classpath root).
        URL configUrl = HelloWorld.class.getResource("log4j.properties");
        if ( configUrl != null ) {
            PropertyConfigurator.configure(configUrl);
        }

        // Create a new appstruct with default BCEL classinfo factory and simple config
        appStruct = new AppStruct(new BcelClassLoader(), new SimpleConfig() );

        // Set the classpath to the classloader. This is just a wrapper for loader.setClassPath().
        // Default is '.'
        appStruct.setClassPath(".");


        // ***** Initialization done, now do some serious class loading stuff *****
        try {

            // let the class load itself into the appstruct
            ClassInfo main = appStruct.loadClassInfo("com.jopdesign.libgraph.demo.HelloWorld");

            if ( main == null ) {
                System.out.println("Personality error: could not find myself.");
                System.exit(1);
            }

            // add the main/root class to appStruct
            appStruct.addClass(main);

            // This is a new one: Use fancy new TransitiveHullLoader to simply load all the rest
            TransitiveHullLoader thLoader = new TransitiveHullLoader(appStruct);
            thLoader.extendTransitiveHull(appStruct.getClassInfos());

            for (Iterator it = thLoader.getNewClasses().iterator(); it.hasNext();) {
                ClassInfo newClass = (ClassInfo) it.next();
                appStruct.addClass(newClass);
                System.out.println("Added class " + newClass.getClassName());
            }

            // Important: After loading is complete, all classinfos and methodinfos need to be initialized.
            // This should be done somehow during loading/transitive hull creation, someday
            appStruct.initClassInfos(false);

            System.out.println("Loaded " + appStruct.getClassInfos().size() + " classes.");

        } catch (TypeException e) {
            // This is something which is not a 'normal' error (i.e. the class was not found or
            // ignored due to config settings but is essential), but something like IO-errors.
            e.printStackTrace();
            System.exit(2);
        }


        // ***** Add a main method to SimpleConfig *****
        try {

            // get the SimpleConfig class. 'false' here means that the result should never be null
            // even if the class is ignored by configuration or something.
            ClassInfo configClass = appStruct.getClassInfo("com.jopdesign.libgraph.demo.SimpleConfig", false);

            for (Iterator it = configClass.getMethodInfos().iterator(); it.hasNext();) {
                MethodInfo methodInfo = (MethodInfo) it.next();
                System.out.println("Found method: " + methodInfo.getModifierString() + methodInfo.getFQMethodName());
            }

            MethodInfo main = configClass.getMethodInfo("main", "(Ljava/lang/String;)V");
            if ( main == null ) {

                // TODO create new method/create new class not yet implemented in classinfo
                // main = configClass.addMethod("main", new TypeInfo[] { new ArrayRefType(1, new StringType() ) },
                //                                      new BaseType(TypeInfo.TYPE_VOID), false ); // 'not abstract'
                // main.setStatic(true);
                // main.setFinal(true);
                // main.setAccessType(MethodInfo.ACC_PUBLIC);
                // ControlFlowGraph graph = main.getMethodCode().getControlFlowGraph();


                // Write new class file
                String filename = configClass.getClassName().replace(".", File.separator) + ".class";
                configClass.writeClassFile(filename);

            } else {
                System.out.println("Main method already exists.");
            }

        } catch (TypeException e) {
            // Unable to load SimpleConfig class or something nasty happened.
            e.printStackTrace();
            System.exit(2);
        } catch (IOException e) {
            // Error writing classfile
            e.printStackTrace();
            System.exit(2);
        }

    }

}
