package com.jopdesign.wcet08.jop;

import java.util.List;
import java.util.Vector;
import com.jopdesign.tools.Instruction;
import com.jopdesign.tools.Jopa.Line;
import com.jopdesign.wcet08.jop.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.wcet08.jop.MicrocodeAnalysis.MicrocodePath;


public class MicrocodeTiming {
	public static class TimingExpression {
		int constCycles;
		int reads;
		int writes;		
		int hidden;
		public static TimingExpression constTime(int constCycles) 
			throws MicrocodeVerificationException {
			return new TimingExpression(constCycles,0,0,0);
		}
		public static TimingExpression read(int hidden) 
			throws MicrocodeVerificationException {
			return new TimingExpression(0,1,0,hidden);
		}
		public static TimingExpression write(int hidden) 
			throws MicrocodeVerificationException {
			return new TimingExpression(0,0,1,hidden);
		}
		public TimingExpression(int constCycles, int reads, int writes, int hidden) 
			throws MicrocodeVerificationException {
			this.constCycles = constCycles;
			this.reads = reads;
			this.writes = writes;
			if(hidden < 0) {
				throw new MicrocodeVerificationException("Too few wait states ?");
			}
			this.hidden = hidden;
		}
		public int eval(int r, int w) {
			int cycles = constCycles + reads * r + writes * w - hidden;
			return (cycles > 0 ? cycles : 0);
		}
		public String toString() {
			Vector<String> sb = new Vector<String>();
			if(constCycles > 0) sb.add(""+constCycles);
			sb.add(product(reads,"r"));
			sb.add(product(writes,"w"));
			StringBuffer s = concat(" + ",sb);
			if(hidden > 0) {
				return String.format("Math.max(0,%s - %d)",s,hidden);
			} else {
				return s.toString();
			}
		}
		private static String product(int count, String name) {
			if(count == 0) return "";
			else if(count == 1) return name;
			else if(count == -1) return "-"+name;
			else return count + " " + name;
		}
	}
	private Vector<TimingExpression> timing = new Vector<TimingExpression>();
	private int bcAccessHidden = -1;

	public MicrocodeTiming(MicrocodePath p) throws MicrocodeVerificationException {
		computeTiming(p);
	}
	private void computeTiming(MicrocodePath p) throws MicrocodeVerificationException {
		int start = -1;
		int constantCycles = 0;
		boolean wasWait = false;
		int accessKind = -1;
		for(Line l : p.path) {
			Instruction i = l.getInstruction();
			switch(i.opcode) {
			case Instruction.OPCODE_STMRA: 
			case Instruction.OPCODE_STMWD:
			case Instruction.OPCODE_STBCRD:
			case Instruction.OPCODE_STGF:
			case Instruction.OPCODE_STPF:
			case Instruction.OPCODE_STALD:
			case Instruction.OPCODE_STAST:
				if(accessKind >= 0) {
					throw new MicrocodeVerificationException("No wait after memory access (unsafe)");
				}
				start = constantCycles;
				accessKind = i.opcode;
				break;
			case Instruction.OPCODE_WAIT:
				if(wasWait) {
					int passed = constantCycles - start;
					if(accessKind < 0) {
						throw new MicrocodeVerificationException("Wait without memory access ?");
					}
					switch(accessKind) {
					case Instruction.OPCODE_STMRA: 
						timing.add(TimingExpression.read(passed - 2));
						break;
					case Instruction.OPCODE_STMWD:
						timing.add(TimingExpression.write(passed - 2));
						break;
					case Instruction.OPCODE_STBCRD:
						if(bcAccessHidden >= 0) {
							throw new MicrocodeVerificationException("More than one bytecode read in a microcode sequence");
						}
						bcAccessHidden = passed - 2;
						break;
					case Instruction.OPCODE_STGF:
						/* 6 + 2 r (3 hidden at least) */
						timing.add(new TimingExpression(3,2,0,passed - 3));
						break;
					case Instruction.OPCODE_STPF:
						/* 7 + r + w (3 hidden at least) */
						timing.add(new TimingExpression(4,1,1,passed - 3));
						break;
					case Instruction.OPCODE_STALD:
						timing.add(new TimingExpression(2,3,0,passed - 3));
						break;
					case Instruction.OPCODE_STAST:
						timing.add(new TimingExpression(4,2,1,passed - 4));
						break;
					default:
						throw new MicrocodeVerificationException("Unsupport kind of memory access: "+accessKind);
					}
					accessKind = start = -1;
				}
				break;
			default: break;
			}
			constantCycles++;
			if(i.opcode == Instruction.OPCODE_WAIT) wasWait = true;
			else wasWait = false;
		}
		timing.add(TimingExpression.constTime(constantCycles));
	}
	public int getCycles(int readDelay, int writeDelay, int bytecodeAccessDelay) {
		int r = 0;
		for(TimingExpression te : timing) {
			r += te.eval(readDelay, writeDelay);
		}
		if(bcAccessHidden > 0) r += Math.max(0, bytecodeAccessDelay - bcAccessHidden);
		return r;
	}
	public String toString() {
		StringBuffer s = concat(" + ",timing);
		if(bcAccessHidden > 0) {
			s.append(" + Math.max(0, bca - "+bcAccessHidden+")");
		}
		return s.toString();
	}
	private static StringBuffer concat(String sep, List<? extends Object> list) {
		boolean first = true; // HELP: Library function
		StringBuffer buf = new StringBuffer();
		for(Object s : list) {
			String str = s.toString();
			if(str.length() == 0) continue;
			if(first) { buf.append(str); first = false; }
			else      { buf.append(sep); buf.append(str); }
		}
		return buf;
	}
}
