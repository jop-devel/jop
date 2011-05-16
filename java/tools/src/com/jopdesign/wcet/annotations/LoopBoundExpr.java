package com.jopdesign.wcet.annotations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.log4j.Logger;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.graphutils.Pair;

/**
 * <p>Loop Bound Expressions</p>
 * We currently support the following loop bound expressions:
 * <ul>
 * <li/>{@code expr := IntervalExpr lb ub} with {@code lb,ub := LInteger } <br/>
 *      Evaluates to [lb,ub], which corresponds to the set of integers     <br/>
 *      { x | lb &lt;= x &lt; ub }  
 * <li/>{@code expr := ConstRefExpr field} with {@code field := MemberID } <br/>
 *      Evaluates to the singleton set [c,c], if c is the constant value of
 *      field found in the classfile
 * <li/>{@code expr := PrimOpExpr BitLength subexpr} <br/>
 *      If subexpr evaluates to [lb,ub], it evaluates to the set 
 *      {@code [bitlength(lb), bitlength(ub)]} <br/>
 *      The bitlength is the number of bits needed to represent a number
 *      in a standard 2-complements encoding, or equivalently <br/>
 *      {@code floor(log(x) / log(2)) + 1}
 * <li/>{@code expr := PrimOpExpr BinOp expr1 expr2} <br/>
 *      Standard operations in the interval domain: addition, subtraction,
 *      multiplication, integer division, intersection and union <br/>
 *      We currently do not support maximum or minimum, as in most situations,
 *      what you really mean is {@code union} (The maximum of the
 *      upper bound and the minimum of the lower bound) or {@code intersection}
 *      (The minum of the lower bound and the maximum of the upper bound)
 * </ul>
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
@SuppressWarnings("unchecked")
public abstract class LoopBoundExpr {
	public static enum ExprType { LITERAL, CONST_REF, ARG_REF, PRIM_OP };

	public static enum PrimOp    { BIT_LENGTH, /* bit scan reverse (index of most-significant 1 bit) */
		                           INTERSECT, UNION, 
		                           ADD, SUB, MUL, IDIV };

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
		public LInteger divide(LInteger other) {
			if(other.isInfinite()) throw new ArithmeticException("division by infinity");
			if(other.repr.intValue() == 0) throw new ArithmeticException("division by zero");
			if(this.isInfinite()) return INFINITY;
			return new LInteger(repr.divide(other.repr));
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
		public LInteger bitLength() {
			if(isInfinite()) return this;
			return new LInteger(repr.bitLength());
		}
	}

	public static final LInteger ZERO     = new LInteger(0);
	public static final LInteger INFINITY = new LInteger((BigInteger)null);

    public static final LoopBoundExpr ANY = new IntervalExpr(ZERO, INFINITY);
    public static final Pair<LInteger,LInteger> ANY_VALUE = ANY.constValue(null);

    protected ExprType type;

	/** Value of an expression, or ANY if no constant interval for the expression 
	 *  can be calculated.
	 *  @param ctx If the loop bound refers to the source code (e.g., constant fields)
	 *  or results from the value analysis (e.g., the interval of an argument),
	 *  the context is needed to calculate a constant bound. Is allowed to be null
	 *  if no context is available */
	public abstract Pair<LInteger, LInteger> constValue(ExecutionContext ctx);

	
	/** Constant loop upper bound, or null if no (independent) constant upper bound is known */
	public Long upperBound(ExecutionContext ctx) {
		Pair<LInteger, LInteger> cv = constValue(ctx);
		if(cv.second().isInfinite()) return null;
		return cv.second().longValue();
	}

	/** Constant loop lower bound */
	public Long lowerBound(ExecutionContext ctx) {
		Pair<LInteger, LInteger> cv = constValue(ctx);
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

	private static final Map<String,PrimOp> ASSOCIATIVE_BIN_OPS = buildAssociativeBinopMap();
	private static Map<String,PrimOp> buildAssociativeBinopMap() {
		HashMap<String, PrimOp> map = new HashMap<String, PrimOp>();
		map.put("sum", PrimOp.ADD);
		map.put("product", PrimOp.MUL);
		map.put("union", PrimOp.UNION);
		map.put("intersection", PrimOp.INTERSECT);
		return map;
	}

	static LoopBoundExpr builtInFunction(String ident, List<LoopBoundExpr> args) {
		if(ident.equals("bitlength")) {
			return new PrimOpExpr(PrimOp.BIT_LENGTH, args, 1) {
				@Override protected Pair evaluatePrimOp(List<Pair<LInteger, LInteger>> args) {
					/* Minimum number of bits in minimal two-bit complement representation */
					/* For positive numbers, this is bitwidth(x) - clz(x) */
					Pair<LInteger, LInteger> n = args.get(0);
					if(n.first().compareTo(ZERO) < 0) {
						throw new ArithmeticException("bit-length of negative number");
					}
					return new Pair<LInteger,LInteger>(n.first().bitLength(),
							                           n.second().bitLength());
				}
				
			};
		} else if(ASSOCIATIVE_BIN_OPS.containsKey(ident)) { 
			/* associative bin-op */
			Iterator<LoopBoundExpr> argIter = args.iterator();
			if(! argIter.hasNext()) {
				throw new ArithmeticException("LoopBound expression: Empty "+ident+" (probably not intended)");
			}
			LoopBoundExpr r = argIter.next();
			while(argIter.hasNext()) {
				r = r.binOp(ASSOCIATIVE_BIN_OPS.get(ident), argIter.next());
			}
			return r;
		}
		throw new ArithmeticException("Unsupported prim-op: "+ident);
	}
	
	static LoopBoundExpr constRef(ArrayList<String> memberIDs) {
		return new ConstRefExpr(memberIDs);
	}

	static LoopBoundExpr argRef(String arg, ArrayList<String> memberIDs) {
		throw new AssertionError("Unimplemented: LoopBoundExpr#argRef");
	}

	public LoopBoundExpr relaxLowerBound(long lb) {
		return union(numericBound(lb,lb));
	}

	public LoopBoundExpr add(LoopBoundExpr other) {
		return new PrimOpExpr(PrimOp.ADD, this, other) {
			protected Pair evaluatePrimOp(List<Pair<LInteger,LInteger>> args) {
				Pair<LInteger, LInteger> n1 = args.get(0);
				Pair<LInteger, LInteger> n2 = args.get(1);
					return new Pair(n1.first().add(n2.first()),
							        n1.second().add(n2.second()));
			}			
		};
	}

	public LoopBoundExpr subtract(LoopBoundExpr other) {
		return new PrimOpExpr(PrimOp.SUB, this, other) {
			protected Pair evaluatePrimOp(List<Pair<LInteger,LInteger>> args) {
				Pair<LInteger, LInteger> n1 = args.get(0);
				Pair<LInteger, LInteger> n2 = args.get(1);
					return new Pair(n1.first().subtract(n2.second()),
							        n1.second().subtract(n2.first()));
			}			
		};
	}

	public LoopBoundExpr mul(LoopBoundExpr other) {
		return new PrimOpExpr(PrimOp.MUL, this, other) {
			protected Pair evaluatePrimOp(List<Pair<LInteger,LInteger>> args) {
				Pair<LInteger, LInteger> n1 = args.get(0);
				Pair<LInteger, LInteger> n2 = args.get(1);
				if(n1.first().isNegative()) throw new AssertionError("Multiplication with negative number");
				if(n2.first().isNegative()) throw new AssertionError("Multiplication with negative number");
				return new Pair(n1.first().multiply(n2.first()),
							    n1.second().multiply(n2.second()));
			}			
		};
	}

	public LoopBoundExpr idiv(LoopBoundExpr other) {
		return new PrimOpExpr(PrimOp.MUL, this, other) {
			protected Pair evaluatePrimOp(List<Pair<LInteger,LInteger>> args) {
				Pair<LInteger, LInteger> n1 = args.get(0);
				Pair<LInteger, LInteger> n2 = args.get(1);
				if(n1.first().isNegative()) throw new AssertionError("Integer Division with negative number");
				if(n2.first().isNegative()) throw new AssertionError("Integer Division with negative number");
				return new Pair(n1.first().divide(n2.second()),
							    n1.second().divide(n2.first()));
			}			
		};
	}

	public LoopBoundExpr intersect(LoopBoundExpr other) {
		if(other == null) return this;
		return new PrimOpExpr(PrimOp.INTERSECT, this, other) {
			protected Pair evaluatePrimOp(List<Pair<LInteger,LInteger>> args) {
				Pair<LInteger, LInteger> n1 = args.get(0);
				Pair<LInteger, LInteger> n2 = args.get(1);
				if(n2.second().compareTo(n1.first()) < 0 && n2.second().compareTo(n2.first()) < 0) {
					throw new AssertionError("Empty Interval-Intersection (probably a bug)");
				}
				return new Pair(n1.first().max(n2.first()),
						n1.second().min(n2.second()));
			}			
		};
	}

	public LoopBoundExpr union(LoopBoundExpr other) {
		return new PrimOpExpr(PrimOp.UNION, this, other) {
			protected Pair evaluatePrimOp(List<Pair<LInteger,LInteger>> args) {
				Pair<LInteger, LInteger> n1 = args.get(0);
				Pair<LInteger, LInteger> n2 = args.get(1);
				return new Pair(n1.first().min(n2.first()),
						n1.second().max(n2.second()));
			}			
		};
	}

	/* generic binop */
	public LoopBoundExpr binOp(PrimOp op, LoopBoundExpr other) {
		switch(op) {
		case ADD: return add(other);
		case SUB: return subtract(other);
		case MUL: return mul(other);
		case IDIV: return idiv(other);
		case UNION: return union(other);
		case INTERSECT: return intersect(other);
		default: throw new ArithmeticException("Not a binary op: "+op);
		}
	}


	public static class IntervalExpr extends LoopBoundExpr {
		private LInteger lb, ub;
		private IntervalExpr(LInteger lb, LInteger ub) {
			super(ExprType.LITERAL);
			this.lb = lb;
			this.ub = ub;
		}

		/* Value of a constant expression, or null if the expression is not constant */
		public Pair<LInteger, LInteger> constValue(ExecutionContext _) {
			return new Pair<LInteger, LInteger>(lb,ub);
		}
		@Override public String toString() {
			if(lb.compareTo(ub) == 0) return ub.toString();
			return "["+lb.toString()+","+ub.toString()+"]";			
		}
	}


	public abstract static class PrimOpExpr extends LoopBoundExpr {
		private PrimOp op;
		private List<LoopBoundExpr> args;
		private PrimOpExpr(PrimOp op) {
			super(ExprType.PRIM_OP);		
			this.op = op;
			this.args = new ArrayList<LoopBoundExpr>();
		}
		private PrimOpExpr(PrimOp binOp, LoopBoundExpr m1, LoopBoundExpr m2) {
			this(binOp);
			args.add(m1);
			args.add(m2);
		}
		private PrimOpExpr(PrimOp op, List<LoopBoundExpr> args, int expectedArgs) {
			super(ExprType.PRIM_OP);
			if(args.size() != expectedArgs) {
				throw new ArithmeticException("Bad number of operands for "+op);
			}
			this.op = op;
			this.args = new ArrayList<LoopBoundExpr>(args);
		}

		/* Value of a constant expression, or null if the expression is not constant */
		@Override
		public Pair<LInteger, LInteger> constValue(ExecutionContext ctx) {	
			List<Pair<LInteger,LInteger>> constArgs =
				new ArrayList<Pair<LInteger,LInteger>>();
			for(LoopBoundExpr a : args) {
				Pair<LInteger, LInteger> n = a.constValue(ctx);
				if(n == null) n = ANY_VALUE;
				constArgs.add(n);
			}
			return evalInterval(constArgs);
		}
		public Pair<LInteger, LInteger> evalInterval(List<Pair<LInteger, LInteger>> args) {			
			Pair<LInteger,LInteger> r = evaluatePrimOp(args);
			if(r.first().compareTo(r.second()) > 0) throw new AssertionError("Interval Arithmetic: lb > ub ?");
			return r;
		}

		protected abstract Pair<LInteger, LInteger> evaluatePrimOp(List<Pair<LInteger,LInteger>> args);

		@Override public String toString() {
			StringBuffer sb = new StringBuffer(""+op+"(");
			boolean first = true;
			for(LoopBoundExpr a : args) {
				if(!first) sb.append(", "); 
				sb.append(a.toString());
				first = false;
			}
			sb.append(")");
			return sb.toString();
		}
	}
	
	public static class ConstRefExpr extends LoopBoundExpr {
		private String className;
		private String fieldName;
		
		public ConstRefExpr(ArrayList<String> memberIdentParts) {
			super(ExprType.CONST_REF);
			int i;
			StringBuffer className = new StringBuffer();
			for(i = 0; i < memberIdentParts.size() - 1; i++) {
				if(i!=0) className.append(".");
				className.append(memberIdentParts.get(i));
			}
			this.className = className.toString();
			this.fieldName = memberIdentParts.get(memberIdentParts.size()-1);
		}

		@Override
		public Pair<LInteger, LInteger> constValue(ExecutionContext ctx) {			
			ClassInfo ci;
			if(className.equals("")) {
				ci = ctx.getMethodInfo().getClassInfo();				
			} else {
				ci = ctx.getMethodInfo().getAppInfo().getClassInfo(className);
			}
			FieldInfo fieldInfo = ci.getFieldInfo(fieldName);
			if(fieldInfo == null) {
				Logger.getLogger(this.getClass()).error("Loop Bound Expression: Cannot find information on constant value "+toString());
				return ANY_VALUE;
			}
			ConstantValue cv = fieldInfo.getField().getConstantValue();
			if(cv == null) {
				Logger.getLogger(this.getClass()).error("Loop Bound Expression: Cannot find information on constant value "+toString());
				return ANY_VALUE;
			}
			Long value = getLongConstant(cv);
			if(value == null) {
				Logger.getLogger(this.getClass()).error("Loop Bound Expression: Bad constant value (not an integer): "+toString());
				return ANY_VALUE;				
			}
			return new Pair<LInteger,LInteger>(new LInteger(value), new LInteger(value));
		}
		
		/** Get constant value as long. Adapted from BCEL's ConstantValue#toString() */
		private Long getLongConstant(ConstantValue cv) {
			ConstantPool cp = cv.getConstantPool();
			int cpix = cv.getConstantValueIndex();
	        Constant c = cp.getConstant(cpix);
	        switch (c.getTag()) {
	        	case Constants.CONSTANT_Integer:
	        		return new Long(((ConstantInteger) c).getBytes());
	            case Constants.CONSTANT_Long:
	            	return ((ConstantLong) c).getBytes();
	            default:
	            	return null;
	        }
		}

		@Override public String toString() {
			return (className.length() > 0 ? (this.className+".") : "")+this.fieldName;
		}
	}

}
//			case MAX:
//				/* [a,b] `max` [c,d] = [a `max` c, b `max` d] */
//				lb = n1.first().max(n2.first());
//				ub = ub1.max(n2.second());
//				break;
//			case MIN:
//				/* [a,b] `min` [c,d] = [a `min` c, b `min` d] */
//				lb = n1.first().min(n2.first());
//				ub = ub1.min(n2.second());
//				break;
