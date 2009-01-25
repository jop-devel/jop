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
package joptimizer.tests;

import com.jopdesign.libgraph.demo.SimpleConfig;
import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.bcel.BcelClassLoader;
import joptimizer.tests.stack.StackTestGen;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 * Generate some test cases.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class TestGenerator {

    private static AppStruct appStruct;
    private static String outputPath = ".";

    public static void main(String[] args) {

        setup();

        // generate test classes
        StackTestGen stacktest = new StackTestGen(appStruct);
        stacktest.generate();

        writeClasses();
    }

    private static void setup() {

        URL configUrl = TestGenerator.class.getResource("log4j.properties");
        if ( configUrl != null ) {
            PropertyConfigurator.configure(configUrl);
        }

        appStruct = new AppStruct(new BcelClassLoader(), new SimpleConfig() );
        appStruct.setClassPath(".");
    }

    private static void writeClasses() {

        try {
            for (Iterator it = appStruct.getClassInfos().iterator(); it.hasNext();) {
                ClassInfo classInfo = (ClassInfo) it.next();

                String filename = outputPath + File.separator +
                        classInfo.getClassName().replace(".", File.separator) + ".class";
                classInfo.writeClassFile(filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }

    }

}
