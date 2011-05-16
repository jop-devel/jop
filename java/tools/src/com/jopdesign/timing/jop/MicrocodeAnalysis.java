/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber (benedikt.huber@gmail.com)

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

package com.jopdesign.timing.jop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.Map.Entry;

import com.jopdesign.tools.Instruction;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.tools.Jopa;
import com.jopdesign.tools.Jopa.Line;

/**
 * Parse microcode file, compute timings
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class MicrocodeAnalysis {
	public static int JOPSYS_NOIM = 254;

	public static class MicrocodeVerificationException extends Exception {
		private static final long serialVersionUID = 1L;
		public MicrocodeVerificationException(String msg) { super(msg); }
	}

	private static void ifail(String msg) throws AssertionError {
		throw new AssertionError("Interpreter failed: "+msg);
	}

	/** Limit for the length of a microcode path during simulation */
	public final int PATH_SIZE_LIMIT = 1000;
	/** Number of branch delay slots */
	public final int BRANCH_DELAY_SLOTS = 2;

	/** Jumps to this label are assumed to happen on a null pointer exception and
	 *  are treated in a special way
	 */
	public static final String NULL_PTR_CHECK_LABEL = "null_pointer";

	/** Jumps to this label are assumed to happen on an array index out of bounds
	 *  exception and are treated in a special way.
	 */
	public static final String ARRAY_BOUND_CHECK_LABEL = "array_bound";

	public static final String VP_SAVE_LABEL = "invoke_vpsave";

	public static String[] INFEASIBLE_BRANCHES =
		{ NULL_PTR_CHECK_LABEL, ARRAY_BOUND_CHECK_LABEL, "! "+VP_SAVE_LABEL };


	/** Symbolic values */
	private static abstract class MachineValue {
		public abstract boolean isConcrete();
		public abstract Integer evaluate();
		public static MachineValue number(int num) { return new NumberValue(num); }
		public static MachineValue symbol(String sym) { return new SymbolicValue(sym); }
		public static MachineValue alu(int opcode, MachineValue e1, MachineValue e2) {
			if(e1.isConcrete() && e2.isConcrete()) {
				int v1 = e1.evaluate();
				int v2 = e2.evaluate();
				int c = -1;
				switch(opcode) {
				case MicrocodeConstants.AND: c = v1 & v2;break;
				case MicrocodeConstants.OR:  c = v1 | v2;break;
				case MicrocodeConstants.XOR: c = v1 ^ v2;break;
				case MicrocodeConstants.ADD: c = v1 + v2;break;
				case MicrocodeConstants.SUB: c = v1 - v2;break;
				case MicrocodeConstants.USHR: c = v2 >>> v1; break;
				case MicrocodeConstants.SHL:  c = v2 << v1; break;
				case MicrocodeConstants.SHR:  c = v2 >> v1; break;
				default: ifail("case statement [arithmethic]: default");
				}
				return number(c);
			} else {
				return expr(opcode,e1,e2);
			}

		}
		public static MachineValue expr(int opcode, Vector<MachineValue> args) {
			return new Expression(opcode,args);
		}
		public static MachineValue expr(int opcode, MachineValue e1, MachineValue e2) {
			Vector<MachineValue> args = new Vector<MachineValue>();
			args.add(e1);args.add(e2);
			return expr(opcode,args);
		}
	}
	private static class NumberValue extends MachineValue {
		int val;
		public NumberValue(int num)          { this.val = num; }
		@Override public Integer evaluate()      { return val; }
		@Override public boolean isConcrete() { return true; }
		@Override public String toString()    { return ""+val; }
	}
	private static class SymbolicValue extends MachineValue {
		String symbol;
		public SymbolicValue(String sym)      { this.symbol = sym; }
		@Override public Integer evaluate()      { return null; }
		@Override public boolean isConcrete() { return false; }
		@Override public String toString()    { return symbol; }
	}
	private static class Expression extends MachineValue {
		int funSym;
		List<MachineValue> args;
		public Expression(int sym, List<MachineValue> args) {
			this.funSym = sym;
			this.args = args;
		}
		@Override public Integer evaluate()      { return null; }
		@Override public boolean isConcrete() { return false; }
		@Override public String toString()    {
			StringBuilder sb = new StringBuilder();
			sb.append(Instruction.get(funSym));
			sb.append('(');
			sb.append(MicropathTiming.concat(", ", args));
			sb.append(')');
			return sb.toString();
		}
	}

	/** machine state for microcode interpretation */
	private class MachineState {
		private int ic          = -1; /* instruction counter */
		private int addr;             /* microcode address */
		private int jmpAddr     = -1; /* jmp target */
		private int branchDelay = -1; /* number of pending delay slots */

		private int mulStart    = -1; /* time multiplier started */
		private MachineValue mulResult = null; /* multiplication result */

		private int stackInputCount;  /* number of stack items consumed from the initial stack */
		private int symbolGenCount = 0; /* number of generated symbols */
		private String genSymbol(String prefix) { return prefix+"_"+(++symbolGenCount);}

		private Stack<MachineValue> stack = new Stack<MachineValue>();
		private HashMap<String,MachineValue> localVars = new HashMap<String,MachineValue>();
		private HashMap<MachineValue,Boolean> constraints = new HashMap<MachineValue, Boolean>();

		public MachineValue getLocalVar(String name) {
			if(! localVars.containsKey(name)) {
				localVars.put(name, MachineValue.symbol("local_"+name));
			}
			return localVars.get(name);
		}

		public MachineState(int addr) {
			this.addr = addr;
			this.stackInputCount = 0;
		    stack.push(MachineValue.symbol("ts_0"));
		}

		@SuppressWarnings("unchecked")
		public MachineState clone() {
			MachineState cloned = new MachineState(addr);
			cloned.ic = ic;
			cloned.jmpAddr = jmpAddr;
			cloned.branchDelay = branchDelay;
			cloned.mulStart = mulStart;
			cloned.mulResult = mulResult;
			cloned.stackInputCount = stackInputCount;
			cloned.symbolGenCount = symbolGenCount;
			cloned.stack = (Stack<MachineValue>) stack.clone();
			cloned.localVars = new HashMap<String,MachineValue>(localVars);
			cloned.constraints = new HashMap<MachineValue, Boolean>(constraints);
			return cloned;
		}

		public int stackUsage() {
			return stack.size() - stackInputCount;
		}

		public MachineValue stackPop() {
			MachineValue v = stack.pop();
			if(stack.empty()) {
				stackInputCount++;
			    stack.push(MachineValue.symbol("ts_"+stackInputCount));
			}
			return v;
		}

		public void stackPush(MachineValue v) {
			stack.push(v);
		}

		public void stackPushNewSymbol(String prefix) {
			stack.push(MachineValue.symbol(genSymbol(prefix)));
		}

		public void arithmeticOp(int opcode) {
			MachineValue v1 = stackPop();
			MachineValue v2 = stackPop();
			stackPush(MachineValue.alu(opcode,v1,v2));
		}

		public void initBranch(int targetPC) {
			if(branchDelay >= 0) { /* branch initialized; fail */
				ifail("initBranch: branching already initialized");
			}
			this.branchDelay = BRANCH_DELAY_SLOTS;
			this.jmpAddr = targetPC;
		}

		public Line fetchNext() {
			if(branchDelay == 0) {
				addr = jmpAddr;
				jmpAddr = branchDelay = -1;
			} else if(branchDelay > 0) {
				branchDelay--;
			}
			ic++;
			return instrs.get(addr++);
		}

		public void initMul() {
			MachineValue a = stackPop();
			MachineValue b = stackPop();
			stackPush(b);
			mulStart = getIC();
			if(a.isConcrete() && b.isConcrete()) {
				mulResult = MachineValue.number(a.evaluate() * b.evaluate());
			} else {
				mulResult = MachineValue.expr(MicrocodeConstants.STMUL, a, b);
			}
		}

		public int readMul() {
			if(mulStart < 0) ifail("multiplier: load before store");
			stackPush(mulResult);
			int delay = getIC() - mulStart;
			mulStart = -1; mulResult = null;
			return delay;
		}

		public int getIC() {
			return ic;
		}

		public void addConstraint(MachineValue val, boolean isEqualZero) throws MicrocodeVerificationException {
			if(this.constraints.containsKey(val)) {
				boolean wasEqualZero = constraints.get(val);
				if(wasEqualZero != isEqualZero) {
					throw new MicrocodeVerificationException("inconsistent path constraints on "+val);
				}
			}
			this.constraints.put(val, isEqualZero);
		}

		public String dumpConstraints() {
			Vector<String> cts = new Vector<String>();
			for(Entry<MachineValue, Boolean> x : this.constraints.entrySet()) {
				boolean isEqualZero = x.getValue();
				if(isEqualZero) {
					cts.add(""+x.getKey()+" = 0");
				} else {
					cts.add(""+x.getKey()+" /= 0");
				}
			}
			return MicropathTiming.concat(", ", cts).toString();
		}
	}

	/**
	 * mini-interpreter to compute microcode paths
	 */
	public class MicroInterpreter {
		private class Continuation {
			MicrocodePath p;
			MachineState st;
			Continuation(MicrocodePath p, MachineState st) {
				this.p = p;
				this.st = st;
			}
			public Continuation clone() {
				return new Continuation(p.clone(), st.clone());
			}
		}
		private Continuation current;
		private Stack<Continuation> continuations = new Stack<Continuation>();

		private void pushBranchFalse(boolean branchOnZero, MachineValue val) throws MicrocodeVerificationException {
			Continuation stateCopy = current.clone();
			stateCopy.st.addConstraint(val, ! branchOnZero);
			continuations.push(stateCopy);
		}
		public MicroInterpreter() {
		}
		public Vector<MicrocodePath> interpret(String name,int startAddress) throws MicrocodeVerificationException {
			Vector<MicrocodePath> thePaths = new Vector<MicrocodePath>();
			current = new Continuation(new MicrocodePath(name),new MachineState(startAddress));
			do {
				try {
					while(interpret()) { }
				} catch(AssertionError ex) {
					throw new MicrocodeVerificationException(ex.getMessage());
				}
//				System.out.println(" Final state: ");
//				System.out.println("   Path:  "+current.p);
//				System.out.println("   Stack: "+current.st.stack);
//				System.out.println("   Constraints: "+current.st.dumpConstraints());
				thePaths.add(current.p);
				current = null;
				if(! continuations.empty()) current = continuations.pop();
			} while(current != null);
			return thePaths;
		}
		private boolean interpret() throws MicrocodeVerificationException {
			Line microLine = current.st.fetchNext();
			Instruction microInstr = microLine.getInstruction();
			/* eliminate the error-prone getIntVal/getSymVal/getSpecial stuff */
			int constant ; // constant arguments
			String label = microLine.getSymVal();  // labels for jumps
			String localVar = microLine.getSymVal(); // local variable names
			if(microLine.getSymVal() != null) {
				constant = (Integer) jopa.getSymMap().get(microLine.getSymVal());
			} else {
				constant = microLine.getIntVal();
			}
			current.p.addInstr(microLine, current.st.stack.peek().evaluate());
			if(current.p.getPath().size() > PATH_SIZE_LIMIT) {
				ifail("<loop>: path exceeded maximal size of "+PATH_SIZE_LIMIT);
			}

			switch(microInstr.opcode) {
			case MicrocodeConstants.POP:
				current.st.stackPop();break;
			case MicrocodeConstants.AND:
			case MicrocodeConstants.OR:
			case MicrocodeConstants.XOR:
			case MicrocodeConstants.ADD:
			case MicrocodeConstants.SUB:
				current.st.arithmeticOp(microInstr.opcode);
				break;

			//	extension 'address' selects function 4 bits
			// multiplication
			case MicrocodeConstants.STMUL: /* init multiplication */
				current.st.initMul();
				break;
			// memory read/write
			case MicrocodeConstants.STMWA:
			case MicrocodeConstants.STMRA:
			case MicrocodeConstants.STMWD:
			case MicrocodeConstants.STMRAC:
			case MicrocodeConstants.STMRAF:
			case MicrocodeConstants.STMWDF:
				current.st.stackPop();break;
			// array instructions
			case MicrocodeConstants.STALD:
			case MicrocodeConstants.STAST:
				current.st.stackPop();break;
			// getfield/putfield
			case MicrocodeConstants.STGF:
			case MicrocodeConstants.STPF:
				current.st.stackPop();break;
			// magic copying
			case MicrocodeConstants.STCP:
				current.st.stackPop();break;
			// bytecode read
			case MicrocodeConstants.STBCRD:
				current.st.stackPop();break;
			// TODO: why isn't the pop characteristic from
			// instruction info used?
			case MicrocodeConstants.STIDX:
			case MicrocodeConstants.STPS:
				current.st.stackPop();break;
			//	st (vp)	3 bits
			case MicrocodeConstants.ST0:
			case MicrocodeConstants.ST1:
			case MicrocodeConstants.ST2:
			case MicrocodeConstants.ST3:
			case MicrocodeConstants.ST:
			case MicrocodeConstants.STMI:
				current.st.stackPop();break;

			case MicrocodeConstants.STVP:
			case MicrocodeConstants.STJPC:
			case MicrocodeConstants.STAR:
			case MicrocodeConstants.STSP:
				current.st.stackPop();break;

			//shift
			case MicrocodeConstants.USHR:
			case MicrocodeConstants.SHL:
			case MicrocodeConstants.SHR:
				current.st.arithmeticOp(microInstr.opcode);
				break;

			//5 bits
			case MicrocodeConstants.STM:
				MachineValue local = current.st.stackPop();
				current.st.localVars.put(localVar,local);
				break;
			/* microcode branches */
			case MicrocodeConstants.BNZ:
			case MicrocodeConstants.BZ:
				mcBranch(microInstr, label);
				break;

			// -----------------------------------------------------------------------------------
			//	'no sp change' instructions
			// -----------------------------------------------------------------------------------
	        /* microcode jump */
			case MicrocodeConstants.JMP:
				jump(label);
				break;
			case MicrocodeConstants.NOP:
				break;
			case MicrocodeConstants.WAIT:
				current.p.setHasWait();
				break;
			// bytecode branch
			case MicrocodeConstants.JBR:
				break;
			// start getstatic
			case MicrocodeConstants.STGS:
				break;

			// -----------------------------------------------------------------------------------
			//	'push' instructions
			// -----------------------------------------------------------------------------------

			//5 bits
		    // stack.push(local[n])
			case MicrocodeConstants.LDM:
				current.st.stackPush(current.st.getLocalVar(localVar));
				break;

			case MicrocodeConstants.LDI:
				current.st.stackPush(MachineValue.number(constant));
				break;

			//	extension 'address' selects function 4 bits
			case MicrocodeConstants.LDMRD:
				current.st.stackPushNewSymbol("ldmrd");break;
			/* multiplier */
			case MicrocodeConstants.LDMUL:
				int delay = current.st.readMul();
				current.p.setNeedsMultiplier(delay);
				break;
			case MicrocodeConstants.LDBCSTART:
				current.st.stackPushNewSymbol("ldbcstart");
				break;

			//ld (vp)	3 bits
			case MicrocodeConstants.LD0:
			case MicrocodeConstants.LD1:
			case MicrocodeConstants.LD2:
			case MicrocodeConstants.LD3:
			case MicrocodeConstants.LD:
			case MicrocodeConstants.LDMI:
				current.st.stackPushNewSymbol("LDx");break;
			//2 bits
			case MicrocodeConstants.LDSP:
			case MicrocodeConstants.LDVP:
			case MicrocodeConstants.LDJPC:
				current.st.stackPushNewSymbol("LDsp");break;

			//ld opd 2 bits
			case MicrocodeConstants.LD_OPD_8U:
			case MicrocodeConstants.LD_OPD_8S:
			case MicrocodeConstants.LD_OPD_16U:
			case MicrocodeConstants.LD_OPD_16S:
				current.st.stackPushNewSymbol("LDopd");break;

			case MicrocodeConstants.DUP:
				MachineValue v = current.st.stackPop();
				current.st.stackPush(v);current.st.stackPush(v);
				break;
			}
			return ! (microLine.hasNxtFlag());
		}
		/* jump */
		private void jump(String symVal) {
			Integer targetPC = (Integer) symMap.get(symVal);
			if(targetPC == null) ifail("Label not defined: "+symVal);
			current.st.initBranch(targetPC);
		}
		/* non-deterministic jump  */
		private void jumpMaybe(Instruction branchInstr, MachineValue val, String symVal) throws MicrocodeVerificationException {
			boolean branchOnZero = branchInstr.opcode == MicrocodeConstants.BZ;
			pushBranchFalse(branchOnZero, val);
			current.st.addConstraint(val, branchOnZero);
			jump(symVal);
		}
		private void mcBranch(Instruction microInstr, String symVal) throws MicrocodeVerificationException {
			/* branch when zero/not zero.
			 * Attention: the TOS from the last cycle isn't
			 * yet available. To avoid accidental errors, will require that
			 * the last instruction was a NOP */
			MachineValue val = current.st.stackPop();
			if(symVal.equals(NULL_PTR_CHECK_LABEL)) {
				current.p.setNullPtrCheck();
			} else if(symVal.equals(ARRAY_BOUND_CHECK_LABEL)) {
				current.p.setArrayBoundCheck();
			} else if(symVal.equals(VP_SAVE_LABEL)) {
				jump(symVal); // ALWAYS taken
			} else {
				current.p.checkStableTOS();
				if(! val.isConcrete()) {
					jumpMaybe(microInstr, val, symVal); // Non-deterministic branch
				} else {
					boolean doBranch = (microInstr.opcode == MicrocodeConstants.BZ) ?
							           (val.evaluate() == 0) :
						               (val.evaluate() != 0);
					if(doBranch) jump(symVal);
				}
			}
		}
	}
	private File asmFile;

	private Jopa jopa;
	//private Vector<Line> lines;
	private Map<String, Integer> symMap;
	private Map<Integer,Integer> jInstrs;
	private List<Line> instrs;
	public static final File DEFAULT_ASM_FILE = new File("asm", new File("generated","jvmgen.asm").getPath());

	public MicrocodeAnalysis(String jvmAsm) throws IOException {
		asmFile = new File(jvmAsm);
		parse(false);
	}

	private File preprocess() throws IOException {
		/* create temporary file for preprocessing */
		File asmfilePath = new File(asmFile.getParent());
		File tmpFile = File.createTempFile("jvm", ".asm");
		tmpFile.deleteOnExit();
		String fn =  asmFile.getName();

		String[] cmd = {"gcc","-o",tmpFile.getPath(),"-x","c","-E","-C","-P",fn};
		Process p = Runtime.getRuntime().exec(cmd,null,asmfilePath);
		int exitCode;
		try {
			exitCode = p.waitFor();
		} catch (InterruptedException e) {
			throw new IOException("Preprocess failed: "+e);
		}
		if(exitCode != 0) {
			System.err.println(Arrays.toString(cmd));
			BufferedReader eis = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String l = null;
			while((l = eis.readLine()) != null) {
				System.err.println(l);
			}
			throw new IOException("Preprocessor failed with exit code "+exitCode);
		}
		return tmpFile;
	}

	private void parse(boolean preprocess) throws IOException {
		if(! asmFile.exists()) {
			throw new IOException("Assembler file "+asmFile+" not found. You may need to run e.g.: 'make gen_mem -e ASM_SRC=jvm JVM_TYPE=USB'");
		}
		File inFile;
		if(preprocess) {
			inFile = preprocess();
		} else {
			inFile = asmFile;
		}
		jopa = new Jopa(inFile.getName(),inFile.getParent(),inFile.getParent());
		jopa.pass1();
		this.symMap = jopa.getSymMap();
		this.jInstrs = jopa.getJavaInstructions();
		this.instrs = jopa.getInstructions();
	}

	public Integer getStartAddress(int opcode) {
		if(opcode == JOPSYS_NOIM) return this.jInstrs.get(JOPSYS_NOIM);  // sys no-im
		String name = JopInstr.name(opcode);
		int jopinstr = JopInstr.get(name);
		return this.jInstrs.get(jopinstr);
	}

	Vector<MicrocodePath> getMicrocodePaths(String opName, int addr)
		throws MicrocodeVerificationException{
		MicroInterpreter microMachine = new MicroInterpreter();
		return microMachine.interpret(opName,addr);
	}


	public static void main(String[] argv) {
//		MicrocodeAnalysis mt = null;
//		try {
//			mt = new MicrocodeAnalysis(asmFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
		// Detailed analysis ... (TODO)
	}
}
