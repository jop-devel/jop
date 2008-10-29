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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.util.ClassPath;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.HashedString;
import com.jopdesign.dfa.analyses.Location;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.analyses.Pair;
import com.jopdesign.dfa.analyses.ReceiverTypes;
import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.Interpreter;
import com.jopdesign.dfa.framework.AppInfo;

public class Main {
	
	public static void main(String[] args) {
		
		AppInfo program;

		if (args[0].equals("-help")) {
			System.out.println("Usage: Main [-cp CLASSPATH] CLASS");
			System.exit(0);
		}

		if (args[0].equals("-cp")) {
			program = new AppInfo(new ClassPath(args[1]), args[2]);
		} else {
			program = new AppInfo(ClassPath.SYSTEM_CLASS_PATH, args[0]);
		}

		{
			Analysis<ReceiverTypes.TypeMapping, ReceiverTypes.TypeMapping> analysis = new ReceiverTypes();
			Interpreter<ReceiverTypes.TypeMapping, ReceiverTypes.TypeMapping> interpreter = new Interpreter<ReceiverTypes.TypeMapping, ReceiverTypes.TypeMapping>(analysis, program);
			
			Map<InstructionHandle, ContextMap<ReceiverTypes.TypeMapping, ReceiverTypes.TypeMapping>> result = new HashMap<InstructionHandle, ContextMap<ReceiverTypes.TypeMapping, ReceiverTypes.TypeMapping>>();
			try {
				MethodInfo prologue = program.getMethod("java.lang.Object.<prologue>");
				
				// build initial context
				Context context = new Context();
				context.stackPtr = 0;
				context.syncLevel = 0;
				context.constPool = new ConstantPoolGen(prologue.getMethod().getConstantPool());
				context.method = prologue.methodId;

				// do magic initialization
				analysis.initialize(args[2]+".main([Ljava/lang/String;)V", context);
								
				// analyze
				result = interpreter.interpret(context, prologue.getMethodGen().getInstructionList().getStart(), result, true);
								
			} catch (Throwable thr) {
				thr.printStackTrace();
			}

			program.setReceivers(analysis.getResult());
	
//			for (Iterator i = analysis.getResult().keySet().iterator(); i.hasNext(); ) {
//				InstructionHandle instr = (InstructionHandle)i.next();
//
//				ContextMap<String, String> r = (ContextMap<String, String>)analysis.getResult().get(instr);
//				Context c = r.getContext();
//
//				LineNumberTable lines = program.getMethods().get(c.method).getLineNumberTable(c.constPool);
//				int sourceLine = lines.getSourceLine(instr.getPosition());			
//
//				System.out.println(c.method+":"+/*sourceLine*/instr.getPosition());
//				for (Iterator<String> k = r.keySet().iterator(); k.hasNext(); ) {
//					String target = k.next();
//					System.out.println("\t"+target);
//				}
//			}			
		}
		
		{
			Analysis<List<HashedString>, Map<Location, LoopBounds.ValueMapping>> analysis = new LoopBounds();
			Interpreter<List<HashedString>, Map<Location, LoopBounds.ValueMapping>> interpreter = new Interpreter<List<HashedString>, Map<Location, LoopBounds.ValueMapping>>(analysis, program);

			Map<InstructionHandle, ContextMap<List<HashedString>, Map<Location, LoopBounds.ValueMapping>>> result = new HashMap<InstructionHandle, ContextMap<List<HashedString>, Map<Location, LoopBounds.ValueMapping>>>();
			try {
				MethodInfo prologue = program.getMethod("java.lang.Object.<prologue>");

				Context context = new Context();
				context.stackPtr = 0;
				context.syncLevel = 0;
				context.constPool = new ConstantPoolGen(prologue.getMethod().getConstantPool());
				context.method = prologue.methodId;

				analysis.initialize(args[2]+".main([Ljava/lang/String;)V", context);

				result = interpreter.interpret(context, prologue.getMethodGen().getInstructionList().getStart(), result, true);
			} catch (Throwable thr) {
				thr.printStackTrace();
			}

//			System.out.println("##################");
			
			for (Iterator i = analysis.getResult().keySet().iterator(); i.hasNext(); ) {
				InstructionHandle instr = (InstructionHandle)i.next();

				ContextMap<List<HashedString>, Pair<LoopBounds.ValueMapping>> r = (ContextMap<List<HashedString>, Pair<LoopBounds.ValueMapping>>)analysis.getResult().get(instr);
				Context c = r.getContext();

				LineNumberTable lines = program.getMethod(c.method).getMethod().getLineNumberTable();
				int sourceLine = lines.getSourceLine(instr.getPosition());			

				for (Iterator<List<HashedString>> k = r.keySet().iterator(); k.hasNext(); ) {
					List<HashedString> callString = k.next();
					Pair<LoopBounds.ValueMapping> bounds = r.get(callString);

					LoopBounds.ValueMapping first = bounds.getFirst();
					LoopBounds.ValueMapping second = bounds.getSecond();
					
					System.out.println(c.method+":"+sourceLine+":\t"+callString+": ");
					
					System.out.print("\t\ttrue:\t");
					System.out.println(first);
					System.out.print("\t\tfalse:\t");
					System.out.println(second);
					System.out.print("\t\tbound:\t");

					// basic checks
					if (//first == null ||
							first.increment == null
							// || second == null
							|| second.increment == null) {
						System.out.println("no valid increment");
						continue;
					}
					// check for boundedness
					if (!first.assigned.hasLb()
							|| !first.assigned.hasUb()
							|| !second.assigned.hasLb()
							|| !second.assigned.hasUb()) {
						System.out.println("unbounded");
						continue;
					}
					// monotone increments?
					if (first.increment.getLb()*first.increment.getUb() <= 0
							|| second.increment.getLb()*second.increment.getUb() <= 0) {
						System.out.println("invalid increments");
						continue;
					}
										
					int firstRange = first.assigned.getUb() - first.assigned.getLb() + 1;
					int secondRange = second.assigned.getUb() - second.assigned.getLb() + 1;
					
					int firstBound;
					if (first.assigned.getUb() < first.assigned.getLb()) {
						firstBound = 0;
					} else {
						firstBound = firstRange / Math.min(Math.abs(first.increment.getUb()), Math.abs(first.increment.getLb()));
					}
					int secondBound;
					if (second.assigned.getUb() < second.assigned.getLb()) {
						secondBound = 0;
					} else {
						secondBound = secondRange / Math.min(Math.abs(second.increment.getUb()), Math.abs(second.increment.getLb()));
					}
						
					System.out.println(Math.max(firstBound, secondBound));						
				}
			}

//			for (Iterator<String> i = program.getMethods().keySet().iterator(); i.hasNext(); ) {
//
//				MethodGen method = program.getMethods().get(i.next());
//				System.out.println(method.getClassName()+"."+method.getName()+method.getSignature()+":");
//				InstructionList list = method.getInstructionList();
//				if (list != null) {
//					for (Iterator j = list.iterator(); j.hasNext(); ) {
//						InstructionHandle statement = (InstructionHandle)j.next(); 
//						ContextMap<List<HashedString>, Map<Location, LoopBounds.ValueMapping>> s = result.get(statement);
//						if (s != null) {
//							System.out.print(statement.getPosition()+": ("+s.getContext().threaded+")");
//							System.out.print(statement.getInstruction().toString(method.getConstantPool().getConstantPool()));
//							System.out.println(":\t|"+s.size()+"| { ");
//							for (Iterator<List<HashedString>> k = s.keySet().iterator(); k.hasNext(); ) {
//								List<HashedString> l = k.next();
//								System.out.println(l+": "+s.get(l));
//							}
//							System.out.println("}");				
//						}
//					}
//				} else {
//					System.out.println("abstract");
//				}
//				System.out.println();
//
//			}
			
		}
				
	}

}
