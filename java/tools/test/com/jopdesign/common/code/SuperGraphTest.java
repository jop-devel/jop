/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.common.code;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.TestFramework;
import com.jopdesign.common.misc.BadGraphException;

/**
 * Purpose: Tests for the (redesigned) SuperGraph datastructure
 * 
 * Test files:
 *   - java/tools/test/test/cg1.zip (from java/target/test/wcet/devel/CallGraph1.java)
 *   -
 *   
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class SuperGraphTest implements CFGProvider {
	private AppInfo appInfo;

	public static void check(boolean test) {
		System.out.println(test ? "OK" : "FAIL");
	}

	public static void main(String[] args) {
		TestFramework testFramework = new TestFramework();
		AppSetup setup =  testFramework.setupAppSetup("java/tools/test/test/cg1.zip", null);
		AppInfo appInfo = testFramework.setupAppInfo("wcet.devel.CallGraph1.run", true);
		
		SuperGraphTest testInst = new SuperGraphTest();
		testInst.appInfo = appInfo;

		MethodInfo mainMethod = appInfo.getMainMethod();		
		SuperGraph sg0 = new SuperGraph(testInst, testInst.getFlowGraph(mainMethod), 0);

		try {
			testInst.getFlowGraph(mainMethod).exportDOT(new File("/tmp/cg1-run.dot"));
			sg0.exportDOT(new File("/tmp/cg1-super0.dot"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		SuperGraph sg2 = new SuperGraph(testInst, testInst.getFlowGraph(mainMethod), 2);

		try {
			FileWriter fw = new FileWriter("/tmp/cg1-super2.dot");
			sg2.exportDOT(new File("/tmp/cg1-super2.dot"));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ControlFlowGraph getFlowGraph(MethodInfo method) {
		ControlFlowGraph cfg = appInfo.getFlowGraph(method);
		try {
			cfg.resolveVirtualInvokes();
			cfg.insertReturnNodes();
		} catch (BadGraphException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return cfg;
	}
}
