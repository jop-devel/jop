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
import com.jopdesign.dfa.framework.ClassInfo;
import com.jopdesign.dfa.framework.AppInfo;

public class Main {
	
	public static void main(String[] args) {
		
		AppInfo program = new AppInfo(new ClassInfo());
		
		// basic initializations
		program.parseOptions(args);
		
		// load the program, ready to be analyzed
		try {
			program.load();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		
		// get receivers for this program
		ReceiverTypes rt = new ReceiverTypes();
		program.setReceivers(program.runAnalysis(rt));
//		rt.printResult(program);
		
		// run loop bounds analysis
		LoopBounds lb = new LoopBounds();
		program.runAnalysis(lb);
		lb.printResult(program);				
	}

}
