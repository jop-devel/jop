/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.wcet.annotations;

import com.jopdesign.common.code.LoopBound;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ParserTest {
	public static void main(String[] argv) {
		parseTest("loop=1");
		parseTest("loop  <=  1");
		parseTest("loop = 45 outer");
		parseTest("loop <= 273 outer(2)");
		parseTest("loop <= 45 method");
		parseTest("loop <= 45 method(\"rtlib.InsertionSort.sort(IV;)Z\")");
	}

	private static void parseTest(String string) {
		System.out.println("Parsing: '"+string+"'");
		InputStream is = new ByteArrayInputStream(string.getBytes());
		Scanner scanner = new Scanner(is);
		Parser parser = new Parser(scanner);
		try {
			parser.Parse();
			LoopBound loopBound = parser.getResult();
			System.out.println(loopBound);
		} catch(Error e) {
			e.printStackTrace();
		}
	}
	
}
