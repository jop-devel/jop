package com.jopdesign.wcet08.jop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import com.jopdesign.timing.WCETInstruction;
import com.jopdesign.tools.Instruction;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.tools.Jopa;
import com.jopdesign.tools.Jopa.Line;

/**
 * Parse microcode file, compute timings 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class MicrocodeAnalysis {
	private final int PATH_SIZE_LIMIT = 10000;
	private final int BRANCH_DELAY_SLOTS = 2;

	public static final String NULL_PTR_CHECK_LABEL = "null_pointer";
	public static final String ARRAY_BOUND_CHECK_LABEL = "array_bound";
	public static final String VP_SAVE_LABEL = "invoke_vpsave";

	public static class MicrocodeVerificationException extends Exception {
		private static final long serialVersionUID = 1L;
		public MicrocodeVerificationException(String msg) { super(msg); }
	}

	public class MicrocodePath {
		Vector<Line> path;
		boolean nullPtrCheck = false, arrayBoundCheck = false, hasWait = false;
		int minMultiplierDelay = Integer.MAX_VALUE;

		public MicrocodePath() {
			this.path = new Vector<Line>();
		}
		public MicrocodePath clone() {
			MicrocodePath cloned = new MicrocodePath();
			cloned.path = new Vector<Line>(path); 
			cloned.nullPtrCheck = nullPtrCheck;
			cloned.arrayBoundCheck = arrayBoundCheck;
			cloned.hasWait = hasWait;
			cloned.minMultiplierDelay = minMultiplierDelay;
			return cloned;
		}
		public void addInstr(Line microInstr) {
			this.path.add(microInstr);
		}
		public boolean lastNotModifiedTOS() {
			/* Unknown last instr: maybe modified TOS */
			if (path.size() == 0) return false;
			/* Some statements do not use the stack */
			if(path.lastElement().getInstruction().noStackUse()) return true;
			/* If we have a DUP first, and the next statement is a consumer,
			 * it doesn't modify the TOS */
			if(path.size() >= 2 && 
			   path.get(path.size() - 2).getInstruction().opcode == Instruction.OPCODE_DUP &&
			   path.lastElement().getInstruction().isStackConsumer()) {
				return true;
			}
			/* Unknown: Maybe TOS was modified */
			return false;
		}
		public String toString() {
			Vector<String> names = new Vector<String>();
			for(Line l : path) {
				Instruction i = l.getInstruction();
				String name = i.name;
				if(i.hasOpd) name += " "+l.getIntVal();
				names.add(name);
			}
			String s = names.toString();
			if(hasWait) s+= "[wait]";
			if(nullPtrCheck) s+= "[check-null-ptr]";
			if(arrayBoundCheck) s+= "[check-array-bound]";
			if(minMultiplierDelay != Integer.MAX_VALUE) s+= "[multiplier/delay "+minMultiplierDelay+"]";
			return s;
		}
		public void setNullPtrCheck() {
			this.nullPtrCheck = true;
		}
		public void setArrayBoundCheck() {
			this.arrayBoundCheck = true;
		}
		public void setNeedsMultiplier(int delay) {
			this.minMultiplierDelay = Math.min(minMultiplierDelay, delay);
		}
		public void setHasWait() {
			this.hasWait = true;
		}
	}
	
	private static void noimpl(String msg) { throw new AssertionError("Not implemented: "+msg); }
	private static void ifail(String msg) throws AssertionError { throw new AssertionError("Interpreter failed: "+msg); }

	/** machine state for microcode interpretation */
	private class MachineState {
		private int ic          = -1; /* instruction counter */
		private int addr;             /* microcode address */
		private int jmpAddr     = -1; /* jmp target */
		private int branchDelay = -1; /* number of pending delay slots */

		private int mulStart    = -1; /* time multiplier started */
		private Integer mulResult = -1; /* multiplication result */

		private int stackInputCount;  /* number of stack items consumed from the initial stack */

		private Stack<Integer> stack = new Stack<Integer>();
		private Map<Integer,Integer> localVars = new HashMap<Integer,Integer>();
		public MachineState(int addr) {
			this.addr = addr;
			this.stackInputCount = 0;
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
			cloned.stack = (Stack<Integer>) stack.clone();
			cloned.localVars = new HashMap<Integer,Integer>(localVars);
			return cloned;
		}
		public int stackUsage() {
			return stack.size() - stackInputCount;
		}
		public Integer stackPop() {
			if(stack.empty()) {
				stackInputCount++;
				return null;
			} else {
				return stack.pop();
			}
		}
		public void stackPush(Integer i) {
			stack.push(i);
		}
		public void stackPushUnknown() {
			stack.push(null);
		}
		public void arithmeticOp(int opcode) {
			Integer v1 = stackPop();
			Integer v2 = stackPop();
			if(v1 != null && v2 != null) {
				Integer c = null;
				switch(opcode) {
				case Instruction.OPCODE_AND: c = v1 & v2;break;
				case Instruction.OPCODE_OR:  c = v1 | v2;break;
				case Instruction.OPCODE_XOR: c = v1 ^ v2;break;
				case Instruction.OPCODE_ADD: c = v1 + v2;break;
				case Instruction.OPCODE_SUB: c = v1 - v2;break;
				case Instruction.OPCODE_USHR: c = v2 >>> v1; break;
				case Instruction.OPCODE_SHL:  c = v2 << v1; break;
				case Instruction.OPCODE_SHR:  c = v2 >> v1; break;
				default: ifail("case statement [arithmethic]: default");
				}
				stackPush(c);
			} else {
				stackPushUnknown();
			}
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
			Integer a = stackPop();
			Integer b = stackPop();
			stackPush(b);
			mulStart = getIC();
			if(a != null && b != null) {
				mulResult = a * b;
			} else {
				mulResult = null;
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
		private void pushCont() {
			continuations.push(current.clone());
		}
		public MicroInterpreter() {
		}
		public Vector<MicrocodePath> interpret(int startAddress) throws Exception {
			Vector<MicrocodePath> thePaths = new Vector<MicrocodePath>();
			current = new Continuation(new MicrocodePath(),new MachineState(startAddress));
			do {
				try {
					while(interpret()) { }
				} catch(AssertionError ex) {
					throw new Exception("Interpreter failed: "+ex.getMessage());
				}
				thePaths.add(current.p);
				current = null;
				if(! continuations.empty()) current = continuations.pop();
			} while(current != null);
			return thePaths;
		}
		private boolean interpret() {
			Line microLine = current.st.fetchNext();
			Instruction microInstr = microLine.getInstruction();
			String symVal = microLine.getSymVal();			
			switch(microInstr.opcode) {
			case Instruction.OPCODE_POP: 
				current.st.stackPop();break;
			case Instruction.OPCODE_AND:
			case Instruction.OPCODE_OR:
			case Instruction.OPCODE_XOR:
			case Instruction.OPCODE_ADD:
			case Instruction.OPCODE_SUB:
				current.st.arithmeticOp(microInstr.opcode);
				break;

			//	extension 'address' selects function 4 bits
			// multiplication
			case Instruction.OPCODE_STMUL: /* init multiplication */
				current.st.initMul();
				break;
			// memory read/write
			case Instruction.OPCODE_STMWA:
			case Instruction.OPCODE_STMRA:
			case Instruction.OPCODE_STMWD:
				current.st.stackPop();break;
			// array instructions
			case Instruction.OPCODE_STALD:
			case Instruction.OPCODE_STAST:
				current.st.stackPop();break;
			// getfield/putfield
			case Instruction.OPCODE_STGF:
			case Instruction.OPCODE_STPF:
				current.st.stackPop();break;
			// magic copying
			case Instruction.OPCODE_STCP:
				current.st.stackPop();break;
			// bytecode read
			case Instruction.OPCODE_STBCRD:
				current.st.stackPop();break;
			//	st (vp)	3 bits
			case Instruction.OPCODE_ST0:
			case Instruction.OPCODE_ST1:
			case Instruction.OPCODE_ST2:
			case Instruction.OPCODE_ST3:
			case Instruction.OPCODE_ST:
			case Instruction.OPCODE_STMI:
				current.st.stackPop();break;

			case Instruction.OPCODE_STVP:
			case Instruction.OPCODE_STJPC:
			case Instruction.OPCODE_STAR:
			case Instruction.OPCODE_STSP:
				current.st.stackPop();break;

			//shift
			case Instruction.OPCODE_USHR:
			case Instruction.OPCODE_SHL:
			case Instruction.OPCODE_SHR:
				current.st.arithmeticOp(microInstr.opcode);
				break;

			//5 bits
			case Instruction.OPCODE_STM:
				current.st.stackPop();break;
			/* microcode branches */
			case Instruction.OPCODE_BNZ: 
			case Instruction.OPCODE_BZ:
				mcBranch(microInstr, symVal);
				break;

			// -----------------------------------------------------------------------------------
			//	'no sp change' instructions
			// -----------------------------------------------------------------------------------
			case Instruction.OPCODE_NOP: 
				break;
			case Instruction.OPCODE_WAIT:
				current.p.setHasWait();
				break;
			// bytecode branch
			case Instruction.OPCODE_JBR:
				break;

			// -----------------------------------------------------------------------------------
			//	'push' instructions
			// -----------------------------------------------------------------------------------

			//5 bits
			case Instruction.OPCODE_LDM: 
				current.st.stackPushUnknown();
				break;

			case Instruction.OPCODE_LDI:
				current.st.stackPush(microLine.getIntVal());
				break;

			//	extension 'address' selects function 4 bits
			case Instruction.OPCODE_LDMRD: 
				current.st.stackPushUnknown();break;
			/* multiplier */
			case Instruction.OPCODE_LDMUL:
				int delay = current.st.readMul();
				current.p.setNeedsMultiplier(delay);
				break;
			case Instruction.OPCODE_LDBCSTART:
				current.st.stackPushUnknown();break;

			//ld (vp)	3 bits
			case Instruction.OPCODE_LD0:
			case Instruction.OPCODE_LD1:
			case Instruction.OPCODE_LD2:
			case Instruction.OPCODE_LD3:
			case Instruction.OPCODE_LD:
			case Instruction.OPCODE_LDMI:
				current.st.stackPushUnknown();break;
			//2 bits
			case Instruction.OPCODE_LDSP:
			case Instruction.OPCODE_LDVP:
			case Instruction.OPCODE_LDJPC:
				current.st.stackPushUnknown();break;

			//ld opd 2 bits
			case Instruction.OPCODE_LD_OPD_8U:
			case Instruction.OPCODE_LD_OPD_8S:
			case Instruction.OPCODE_LD_OPD_16U:
			case Instruction.OPCODE_LD_OPD_16S:
				current.st.stackPushUnknown();break;

			case Instruction.OPCODE_DUP:
				Integer v = current.st.stackPop();
				current.st.stackPush(v);current.st.stackPush(v);
				break;
			}
			current.p.addInstr(microLine);
			if(current.p.path.size() > PATH_SIZE_LIMIT) ifail("<loop>: path exceeded maxial size");
			return ! (microLine.hasNxtFlag());
		}
		/* jump */
		private void jump(String symVal) {
			Integer targetPC = (Integer) symMap.get(symVal);
			if(targetPC == null) ifail("Label not defined: "+symVal);
			current.st.initBranch(targetPC);
		}
		/* non-deterministic jump  */		
		private void jumpMaybe(String symVal) {
			pushCont();
			jump(symVal);
		}
		private void mcBranch(Instruction microInstr, String symVal) {
			/* branch when zero/not zero. 
			 * Attention: the TOS from the last cycle isn't
			 * yet available. To avoid accidental errors, will require that
			 * the last instruction was a NOP */
			Integer val = current.st.stackPop();
			if(symVal.equals(NULL_PTR_CHECK_LABEL)) {
				current.p.setNullPtrCheck();					
			} else if(symVal.equals(ARRAY_BOUND_CHECK_LABEL)) {
				current.p.setArrayBoundCheck();
			} else if(symVal.equals(VP_SAVE_LABEL)) {
				jump(symVal); // ALWAYS taken
			} else {
				if(! current.p.lastNotModifiedTOS()) {
					ifail("Microcode branch to " + symVal + " preceeded by operation "+
						 "modifying TOS: "+current.p.path);
				}
				if(val == null) {
					jumpMaybe(symVal); // Non-deterministic branch
				} else {
					boolean doBranch = (microInstr.opcode == Instruction.OPCODE_BZ) ?
							           (val.intValue() == 0) :
						               (val.intValue() != 0);
					if(doBranch) jump(symVal);
				}
			}			
		}
	}
	private File asmFile;

	private Jopa jopa;
	private Vector<Line> lines;
	private Map<Object, Object> symMap;
	private Map<Integer,Integer> jInstrs;
	private List<Line> instrs;
	private List<String> vars;

	public MicrocodeAnalysis(String jvmAsm) {
		asmFile = new File(jvmAsm);
	}
	private File preprocess() throws IOException, InterruptedException {
		/* create temporary file for preprocessing */
		File asmfilePath = new File(asmFile.getParent());
		File tmpFile = File.createTempFile("jvm", ".asm");
		tmpFile.deleteOnExit();
		String fn =  asmFile.getName();

		String[] cmd = {"gcc","-o",tmpFile.getPath(),"-x","c","-E","-C","-P",fn};
		Process p = Runtime.getRuntime().exec(cmd,null,asmfilePath);
		int exitCode = p.waitFor();
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
	@SuppressWarnings("unchecked")
	public void parse(boolean preprocess) throws IOException, InterruptedException {
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
		this.vars   = jopa.getVarList();
	}
	public Integer getStartAddress(int opcode) throws Exception {
		String name = WCETInstruction.OPCODE_NAMES[opcode];
		if(name.equals(WCETInstruction.ILLEGAL_OPCODE)) {
			throw new Exception("Illegal opcode: "+opcode);
		}
		int jopinstr = JopInstr.get(name);
		return this.jInstrs.get(jopinstr);		
	}
	public static void main(String[] argv) {
//		String asmFile= 
//			"/Users/benedikt/Documents/programming/community/jop/cvs_head/opencores_jop/asm/src/jvm.asm";
		String asmFile =
			"/Users/benedikt/Documents/programming/community/jop/cvs_head/opencores_jop/asm/generated/jvmser.asm";
		MicrocodeAnalysis mt = new MicrocodeAnalysis(asmFile);
		try {
			mt.parse(false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		mt.dumpMicrocodeSequences();
	}
	private void dumpMicrocodeSequences() {
		Vector<String> notImpl = new Vector<String>();
		Vector<Throwable> exs = new Vector<Throwable>();
		for(int i = 0; i < 256; i++) {
			if(WCETInstruction.OPCODE_NAMES[i].equals(WCETInstruction.ILLEGAL_OPCODE)) {
				continue;
			}
			try {
				Integer addr = getStartAddress(i);
				if(addr != null) {
					String s = 
						MessageFormat.format("Opcode: {0}, Name: {1}: ",i,WCETInstruction.OPCODE_NAMES[i]);
					s += MessageFormat.format("Start address: {0}", addr);
					System.out.println(s);
					int j = addr;
					MicroInterpreter microMachine = new MicroInterpreter();
					Vector<MicrocodePath> paths = microMachine.interpret(addr);
					int maxCycles = 0;
					int bcLoad = WCETInstruction.calculateB(false, 64);
					for(MicrocodePath p : paths) {
						System.out.println(" "+p);
						MicrocodeTiming mt = new MicrocodeTiming(p);
						System.out.println(" Cycles: "+mt);
						maxCycles = Math.max(maxCycles, mt.getCycles(WCETInstruction.r,WCETInstruction.w,bcLoad));
					}
					int check  = WCETInstruction.getCycles(i, true, 64);
					System.out.println(MessageFormat.format(" Max Cycles[r={0}/w={1}/bc={2}] = {3}",
							WCETInstruction.r, WCETInstruction.w, bcLoad, maxCycles));
					if(maxCycles != check) {
						throw new Exception("Found mismatch jvm.asm vs. WCETInstruction: "+
							WCETInstruction.OPCODE_NAMES[i]+": "+maxCycles+" vs. "+check);
					}
				} else {
					notImpl.add(WCETInstruction.OPCODE_NAMES[i]);
				}
			} catch(Exception e) {
				exs.add(e);
			} catch(AssertionError e) {
				exs.add(e);
			}
			System.out.flush();
		}
		System.err.println("Not implemented: "+notImpl);
		for(Throwable t : exs) {
			System.err.println("[ERROR] "+t.toString());
		}
	}
}
