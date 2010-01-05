/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

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

package com.jopdesign.dfa;

import java.io.IOException;

import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.analyses.ReceiverTypes;
import com.jopdesign.dfa.analyses.SymbolicPointsTo;
import com.jopdesign.dfa.framework.DFAClassInfo;
import com.jopdesign.dfa.framework.DFAAppInfo;

public class Main {

//  wcet.StartLift
	private static final String POINTS_TO_TARGET = "jbe.lift.LiftControl.loop(Ljbe/lift/TalIo;)V";
//	private static final String POINTS_TO_TARGET = "jbe.lift.LiftControl.waitForMotorStart(Ljbe/lift/TalIo;)V";
//	wcet.devel.ObjectCache
//	private static final String POINTS_TO_TARGET = "wcet.devel.ObjectCache.test1()I";
//	private static final String POINTS_TO_TARGET = "wcet.devel.ObjectCache.test2()I";
//	private static final String POINTS_TO_TARGET = 
//		"wcet.devel.ObjectCache$Obj1.test2(Lwcet/devel/ObjectCache$Obj1;Lwcet/devel/ObjectCache$Obj2;)I";

	public static void main(String[] args) {
		
		DFAAppInfo program = new DFAAppInfo(new DFAClassInfo());
		
		// basic initializations
		program.parseOptions(args);
		// load the program, ready to be analyzed
		try {
			program.load();
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		}

		System.out.println("Starting analysis...");
		long startRtTime = System.currentTimeMillis();
		
		// get receivers for this program
		ReceiverTypes rt = new ReceiverTypes();
		program.setReceivers(program.runAnalysis(rt));

		long rtTime = System.currentTimeMillis();
		
		rt.printResult(program);

		System.out.println("Time for ReceiverTypes: "+(rtTime - startRtTime));

		long startLbTime = System.currentTimeMillis();
		
		// run loop bounds analysis
		LoopBounds lb = new LoopBounds();
		program.runAnalysis(lb);

		long lbTime = System.currentTimeMillis();
		
		lb.printResult(program);				
		lb.printSizeResult(program);
		
		System.out.println("Time for LoopBounds: "+(lbTime - startLbTime));

		// run SymbolicPointsTo Analysis (local)
		SymbolicPointsTo pointsTo = new SymbolicPointsTo(8);
		program.runLocalAnalysis(pointsTo, POINTS_TO_TARGET);
		pointsTo.printResult(program);
	}

}
