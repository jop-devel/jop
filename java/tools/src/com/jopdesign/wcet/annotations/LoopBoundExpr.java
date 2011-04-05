package com.jopdesign.wcet.annotations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.jopdesign.common.graphutils.Pair;

@SuppressWarnings("unchecked")
public abstract class LoopBoundExpr {
	public static enum ExprType { LITERAL, CONST_REF, ARG_REF, UN_OP, BIN_OP };

	public static enum BinOp    { MIN, MAX, INTERSECT, UNION, ADD, SUB, MUL };

	public static class LInteger extends Number {
		private static final long serialVersionUID = 1L;
		private BigInteger repr;
		public LInteger(long val) { 
			repr = new BigInteger(""+val);
		}		
		public LInteger(String val) {
			if(val == null) throw new AssertionError("LInteger((String)null)");
			repr = new BigInteger(val);
		}
		private LInteger(BigInteger repr) { 
			this.repr = repr;
		}
		@Override public double doubleValue() { 
			if(isInfinite()) return Double.POSITIVE_INFINITY;
			else             return repr.doubleValue(); 
		}
		@Override public float floatValue() { return (float)repr.doubleValue(); }
		@Override public int intValue() { 
			if(isInfinite()) throw new ArithmeticException("cannot convert infinity to int");
			return repr.intValue(); 
		}
		@Override public long longValue() { 
			if(isInfinite()) throw new ArithmeticException("cannot convert infinity to long");
			return repr.longValue(); 
		}

		public boolean isInfinite() { 
			return repr == null;
		}
		public boolean isNegative() {
			if(isInfinite()) return false;
			return repr.signum() < 0;
		}
		public LInteger add(LInteger other) {
			if(this.isInfinite()) return INFINITY;
			if(other.isInfinite()) return INFINITY;
			return new LInteger(repr.add(other.repr));
		}
		public LInteger subtract(LInteger other) {
			if(other.isInfinite()) throw new ArithmeticException("x - infinity is undefined in the loop bound domain");
			if(this.isInfinite()) return INFINITY;
			return new LInteger(repr.subtract(other.repr));
		}
		public LInteger multiply(LInteger other) {
			if(this.isInfinite() || other.isInfinite()) return INFINITY;
			return new LInteger(repr.multiply(other.repr));
		}
		public LInteger min(LInteger other) {
			if(other.isInfinite()) return this;
			if(this.isInfinite()) return other;
			return new LInteger(repr.min(other.repr));
		}
		public LInteger max(LInteger other) {
			if(this.isInfinite() || other.isInfinite()) return INFINITY;
			return new LInteger(repr.max(other.repr));
		}
		public int compareTo(LInteger other) {
			if(this.isInfinite()) {
				if(other.isInfinite()) return 0;
				else return 1;
			} else if(other.isInfinite()) {
				return -1;
			} else {
				return this.repr.compareTo(other.repr);
			}
		}
		@Override public String toString() {
			if(isInfinite()) return "inf";
			return repr.toString(10);
		}
	}

	public static final LInteger ZERO     = new LInteger(0);

	public static final LInteger INFINITY = new LInteger((BigInteger)null);

	public static final LoopBoundExpr ANY = new IntervalExpr(ZERO, INFINITY);

	protected ExprType type;

	/** Calculate the value of the given expression in the given context */
	public abstract Pair<LInteger, LInteger> evaluate();

	/** Value of a constant expression, or ANY if the expression is not constant */
	public abstract Pair<LInteger, LInteger> constValue();

	/** Constant loop upper bound, or null if no (independent) constant upper bound is known */
	public Long upperBound() {
		Pair<LInteger, LInteger> cv = constValue();
		if(cv == null) return null;
		return cv.second().longValue();
	}

	/** Constant loop lower bound */
	public Long lowerBound() {
		Pair<LInteger, LInteger> cv = constValue();
		if(cv == null) return 0L;
		return cv.first().longValue();
	}

	public LoopBoundExpr(ExprType ty) {
		this.type = ty;
	}

	public static IntervalExpr numericBound(long lb, long ub) {
		return new IntervalExpr(new LInteger(lb), new LInteger(ub));
	}
	public static IntervalExpr numericBound(String lb, String ub) {
		return new IntervalExpr(new LInteger(lb), new LInteger(ub));
	}

	public static IntervalExpr numUpperBound(long ub) {
		return new IntervalExpr(ZERO, new LInteger(ub));
	}

	public static IntervalExpr numUpperBound(String ub) {
		return new IntervalExpr(ZERO, new LInteger(ub));
	}

	static LoopBoundExpr builtInFunction(String ident, List<LoopBoundExpr> args) {
		throw new AssertionError("Unimplemented: LoopBoundExpr#BuiltInFunction");
	}
	
	static LoopBoundExpr memberRef(ArrayList<String> memberIDs) {
		throw new AssertionError("Unimplemented: LoopBoundExpr#BuiltInFunction");
	}

	static LoopBoundExpr argRef(String arg, ArrayList<String> memberIDs) {
		throw new AssertionError("Unimplemented: LoopBoundExpr#argRef");
	}

	public LoopBoundExpr relaxLowerBound(long lb) {
		return union(numericBound(lb,lb));
	}

	public LoopBoundExpr add(LoopBoundExpr other) {
		return new BinOpExpr(BinOp.ADD, this, other) {
			protected Pair evalInterval(LInteger lb1, LInteger lb2, LInteger ub1, LInteger ub2) {
				return new Pair(lb1.add(lb2),
								ub1.add(ub2));
			}			
		};
	}

	public LoopBoundExpr subtract(LoopBoundExpr other) {
		return new BinOpExpr(BinOp.SUB, this, other) {
			protected Pair evalInterval(LInteger lb1, LInteger lb2, LInteger ub1, LInteger ub2) {
				return new Pair(lb1.subtract(ub2),
								ub1.subtract(lb2));
			}			
		};
	}

	public LoopBoundExpr mul(LoopBoundExpr other) {
		return new BinOpExpr(BinOp.MUL, this, other) {
			protected Pair evalInterval(LInteger lb1, LInteger lb2, LInteger ub1, LInteger ub2) {
				if(lb1.isNegative()) throw new AssertionError("Multiplication with negative number");
				if(lb2.isNegative()) throw new AssertionError("Multiplication with negative number");
				return new Pair(lb1.multiply(lb2),
								ub1.multiply(ub2));
			}			
		};
	}
	
	public LoopBoundExpr intersect(LoopBoundExpr other) {
		if(other == null) return this;
		return new BinOpExpr(BinOp.INTERSECT, this, other) {
			protected Pair evalInterval(LInteger lb1, LInteger lb2, LInteger ub1, LInteger ub2) {
				if(ub1.compareTo(lb1) < 0 && ub2.compareTo(lb2) < 0) {
					throw new AssertionError("Empty Interval-Intersection (probably a bug)");
				}
				return new Pair(lb1.max(lb2),
								ub1.min(ub2));
			}			
		};
	}

	public LoopBoundExpr union(LoopBoundExpr other) {
		return new BinOpExpr(BinOp.UNION, this, other) {
			protected Pair evalInterval(LInteger lb1, LInteger lb2, LInteger ub1, LInteger ub2) {
				return new Pair(lb1.min(lb2),
								ub1.max(ub2));
			}			
		};
	}

	public static class IntervalExpr extends LoopBoundExpr {
		private LInteger lb, ub;
		private IntervalExpr(LInteger lb, LInteger ub) {
			super(ExprType.LITERAL);
			this.lb = lb;
			this.ub = ub;
		}

		/* Calculate the value of the given expression in the given context */
		public Pair<LInteger, LInteger> evaluate() {
			return constValue();
		}
		/* Value of a constant expression, or null if the expression is not constant */
		public Pair<LInteger, LInteger> constValue() {
			return new Pair<LInteger, LInteger>(lb,ub);
		}
		@Override public String toString() {
			if(lb.compareTo(ub) == 0) return ub.toString();
			return "["+lb.toString()+","+ub.toString()+"]";			
		}
	}


	public abstract static class BinOpExpr extends LoopBoundExpr {
		private BinOp op;
		private LoopBoundExpr m1,m2;
		private BinOpExpr(BinOp op, LoopBoundExpr m1, LoopBoundExpr m2) {
			super(ExprType.BIN_OP);
			this.op = op;
			this.m1 = m1;
			this.m2 = m2;
		}
		@Override
		public Pair<LInteger, LInteger> evaluate() {
			return constValue();
		}
		/* Value of a constant expression, or null if the expression is not constant */
		@Override
		public Pair<LInteger, LInteger> constValue() {			
			Pair<LInteger, LInteger> n1 = m1.constValue();
			Pair<LInteger, LInteger> n2 = m2.constValue();
			if(n1 == null) n1 = ANY.constValue();
			if(n2 == null) n2 = ANY.constValue();
			return evalInterval(n1,n2);
		}
		public Pair<LInteger, LInteger> evalInterval(Pair<LInteger, LInteger> n1, 
				Pair<LInteger, LInteger> n2) {
			LInteger lb1 = n1.first(), lb2 = n2.first();
			LInteger ub1 = n1.second(), ub2 = n2.second();
			Pair<LInteger,LInteger> r = evalInterval(lb1,lb2,ub1,ub2);
			if(r.first().compareTo(r.second()) > 0) throw new AssertionError("Interval Arithmetic: lb > ub ?");
			return r;
		}

		protected abstract Pair<LInteger, LInteger> evalInterval(
				LInteger lb1, LInteger lb2, LInteger ub1, LInteger ub2);

		@Override public String toString() {
			return this.op+"("+m1.toString()+", "+m2.toString()+")";
		}
	}

}
//			case MAX:
//				/* [a,b] `max` [c,d] = [a `max` c, b `max` d] */
//				lb = lb1.max(lb2);
//				ub = ub1.max(ub2);
//				break;
//			case MIN:
//				/* [a,b] `min` [c,d] = [a `min` c, b `min` d] */
//				lb = lb1.min(lb2);
//				ub = ub1.min(ub2);
//				break;
