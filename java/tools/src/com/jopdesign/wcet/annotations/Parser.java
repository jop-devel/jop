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
import com.jopdesign.common.code.SymbolicMarker;

public class Parser {
	public static final int _EOF = 0;
	public static final int _ident = 1;
	public static final int _number = 2;
	public static final int _string = 3;
	public static final int _char = 4;
	public static final int _cmpop = 5;
	public static final int maxT = 11;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	LoopBound result;
public LoopBound getResult()
{
	return result;
}
LoopBound
buildLoopBound(String cmpOp, Long bound, SymbolicMarker marker)
{
	long lb = 0,
	     ub = bound;
	if(cmpOp.equals("=")) lb = ub;
	if(marker == null) return new LoopBound(lb,ub);
	else               return LoopBound.markerBound(lb,ub,marker);
}
/*--------------------------------------------------------------------*/



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void Annotation() {
		Long expr; SymbolicMarker marker; 
		Expect(6);
		Expect(5);
		String compareOperator = t.val; 
		expr = Expression();
		marker = Context();
		result = buildLoopBound(compareOperator, expr, marker); 
	}

	Long  Expression() {
		Long  expr;
		Expect(2);
		expr = Long.parseLong(t.val); 
		return expr;
	}

	SymbolicMarker Context() {
		SymbolicMarker  marker;
		marker = null; 
		if (la.kind == 7) {
			Get();
			int outerLoop = 1; 
			if (la.kind == 8) {
				Get();
				Expect(2);
				outerLoop = Integer.parseInt(t.val); 
				Expect(9);
			}
			marker = SymbolicMarker.outerLoopMarker(outerLoop); 
		} else if (la.kind == 10) {
			Get();
			String markerMethod = null; 
			if (la.kind == 8) {
				Get();
				Expect(3);
				markerMethod = t.val; 
				Expect(9);
			}
			marker = SymbolicMarker.methodMarker(markerMethod); 
		} else if (la.kind == 0) {
		} else SynErr(12);
		return marker;
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Annotation();

		Expect(0);
	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "ident expected"; break;
			case 2: s = "number expected"; break;
			case 3: s = "string expected"; break;
			case 4: s = "char expected"; break;
			case 5: s = "cmpop expected"; break;
			case 6: s = "\"loop\" expected"; break;
			case 7: s = "\"outer\" expected"; break;
			case 8: s = "\"(\" expected"; break;
			case 9: s = "\")\" expected"; break;
			case 10: s = "\"method\" expected"; break;
			case 11: s = "??? expected"; break;
			case 12: s = "invalid Context"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}

