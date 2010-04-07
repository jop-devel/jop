package com.jopdesign.wcet.annotations;

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
