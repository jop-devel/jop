package com.jopdesign.wcet.annotations;

import java.util.List;
import java.util.ArrayList;

import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.code.SymbolicMarker;

public class Parser {
	public static final int _EOF = 0;
	public static final int _ident = 1;
	public static final int _vident = 2;
	public static final int _number = 3;
	public static final int _string = 4;
	public static final int _char = 5;
	public static final int _cmpop = 6;
	public static final int maxT = 18;

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
buildLoopBound(String cmpop, LoopBoundExpr bound, SymbolicMarker marker)
{
    if(bound == null) return null;
	if(cmpop.equals("<=")) bound = bound.relaxLowerBound(0);
	if(marker == null) return LoopBound.simpleBound(bound);
	else               return LoopBound.markerBound(bound, marker);
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
		String cmpop; LoopBoundExpr expr; SymbolicMarker marker; 
		Expect(7);
		Expect(6);
		cmpop = t.val; 
		expr = Expression();
		marker = Context();
		result = buildLoopBound(cmpop, expr, marker); 
	}

	LoopBoundExpr  Expression() {
		LoopBoundExpr  expr;
		LoopBoundExpr e2; 
		expr = Expression2();
		while (la.kind == 8 || la.kind == 9) {
			if (la.kind == 8) {
				Get();
				e2 = Expression2();
				expr = expr.add(e2); 
			} else {
				Get();
				e2 = Expression2();
				expr = expr.subtract(e2); 
			}
		}
		return expr;
	}

	SymbolicMarker  Context() {
		SymbolicMarker  marker;
		marker = null; 
		if (la.kind == 16) {
			Get();
			int outerLoop = 1; 
			if (la.kind == 12) {
				Get();
				Expect(3);
				outerLoop = Integer.parseInt(t.val); 
				Expect(13);
			}
			marker = SymbolicMarker.outerLoopMarker(outerLoop); 
		} else if (la.kind == 17) {
			Get();
			String markerMethod = null; 
			if (la.kind == 12) {
				Get();
				Expect(4);
				markerMethod = t.val; 
				Expect(13);
			}
			marker = SymbolicMarker.methodMarker(markerMethod); 
		} else if (la.kind == 0) {
		} else SynErr(19);
		return marker;
	}

	LoopBoundExpr  Expression2() {
		LoopBoundExpr  expr;
		LoopBoundExpr e2; 
		expr = Expression3();
		while (la.kind == 10 || la.kind == 11) {
			if (la.kind == 10) {
				Get();
				e2 = Expression3();
				expr = expr.mul(e2); 
			} else {
				Get();
				e2 = Expression3();
				expr = expr.idiv(e2); 
			}
		}
		return expr;
	}

	LoopBoundExpr  Expression3() {
		LoopBoundExpr  expr;
		expr = null; 
		if (la.kind == 3) {
			Get();
			expr = LoopBoundExpr.numericBound(t.val, t.val); 
		} else if (la.kind == 12) {
			Get();
			expr = Expression();
			Expect(13);
		} else if (la.kind == 1) {
			Get();
			String ident = t.val; 
			expr = IdentExpression(ident);
		} else if (la.kind == 2) {
			Get();
			String ident = t.val; 
			expr = ArgExpression(ident);
		} else SynErr(20);
		return expr;
	}

	LoopBoundExpr  IdentExpression(String ident) {
		LoopBoundExpr  expr;
		expr = null; 
		if (la.kind == 12) {
			ArrayList<LoopBoundExpr> args = new ArrayList<LoopBoundExpr>(); 
			Get();
			ExpressionList(args);
			Expect(13);
			expr = LoopBoundExpr.builtInFunction(ident, args); 
		} else if (StartOf(1)) {
			ArrayList<String> members = new ArrayList<String>(); members.add(ident); 
			while (la.kind == 14) {
				Get();
				Expect(1);
				members.add(t.val); 
			}
			expr = LoopBoundExpr.constRef(members); 
		} else SynErr(21);
		return expr;
	}

	LoopBoundExpr  ArgExpression(String index) {
		LoopBoundExpr  expr;
		ArrayList<String> members = new ArrayList<String>(); 
		while (la.kind == 14) {
			Get();
			Expect(1);
			members.add(t.val); 
		}
		expr = LoopBoundExpr.argRef(index, members); 
		return expr;
	}

	void ExpressionList(List args) {
		if (StartOf(2)) {
			LoopBoundExpr expr; 
			expr = Expression();
			args.add(expr); 
			while (la.kind == 15) {
				Get();
				expr = Expression();
				args.add(expr); 
			}
		} else if (la.kind == 13) {
		} else SynErr(22);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Annotation();

		Expect(0);
	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{T,x,x,x, x,x,x,x, T,T,T,T, x,T,T,T, T,T,x,x},
		{x,T,T,T, x,x,x,x, x,x,x,x, T,x,x,x, x,x,x,x}

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
			case 2: s = "vident expected"; break;
			case 3: s = "number expected"; break;
			case 4: s = "string expected"; break;
			case 5: s = "char expected"; break;
			case 6: s = "cmpop expected"; break;
			case 7: s = "\"loop\" expected"; break;
			case 8: s = "\"+\" expected"; break;
			case 9: s = "\"-\" expected"; break;
			case 10: s = "\"*\" expected"; break;
			case 11: s = "\"/\" expected"; break;
			case 12: s = "\"(\" expected"; break;
			case 13: s = "\")\" expected"; break;
			case 14: s = "\".\" expected"; break;
			case 15: s = "\",\" expected"; break;
			case 16: s = "\"outer\" expected"; break;
			case 17: s = "\"method\" expected"; break;
			case 18: s = "??? expected"; break;
			case 19: s = "invalid Context"; break;
			case 20: s = "invalid Expression3"; break;
			case 21: s = "invalid IdentExpression"; break;
			case 22: s = "invalid ExpressionList"; break;
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

