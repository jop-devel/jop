/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import com.jopdesign.wcet.WCETInstruction;

import wcet.framework.instruction.cache.JOPAnalysisInsn;
//import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 19.04.2007
 */
public class JOPTimeAnalysisInsn extends JOPAnalysisInsn
implements ITimeAnalysisInstruction {
	
	public JOPTimeAnalysisInsn(int opc){
		super(opc);
	}
	/* (non-Javadoc)
	 * @see wcet.components.graphbuilder.instuctions.exectime.ITimeAnalysisInstruction#getCycles()
	 */
	public int getCycles() {
//	  TODO where do I get the 'n'from
	    return WCETInstruction.getCycles(this.opcode, false, 0);
	    /*switch(this.opcode){
		case OpCodes.JOPSYS_RD:
		    return 4+ITimeAnalysisInstruction.rws;
		case OpCodes.JOPSYS_WR:
		    return 5+ITimeAnalysisInstruction.rws;
		case OpCodes.JOPSYS_RDMEM:
		    return 4+ITimeAnalysisInstruction.rws;
		case OpCodes.JOPSYS_WRMEM:
		    return 5+ITimeAnalysisInstruction.rws;
		case OpCodes.JOPSYS_RDINT:
		    return 3;
		case OpCodes.JOPSYS_WRINT:
		    return 3;
		case OpCodes.JOPSYS_GETSP:
		    return 3;
		case OpCodes.JOPSYS_SETSP:
		    return 4;
		case OpCodes.JOPSYS_GETVP:
		    return 1;
		case OpCodes.JOPSYS_SETVP:
		    return 2;
		case OpCodes.JOPSYS_INT2EXT:
		    return 14+ITimeAnalysisInstruction.rws+ n * (23+ITimeAnalysisInstruction.wws);//TODO where do I get the 'n'from
		case OpCodes.JOPSYS_EXT2INT:
		    return 14+ITimeAnalysisInstruction.rws+ n * (23+ITimeAnalysisInstruction.wws);//TODO where do I get the 'n'from
		case OpCodes.JOPSYS_NOP:
		    return 1;
		    //TODO get cycles
		case OpCodes.JOPSYS_INVOKE:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.JOPSYS_COND_MOVE:
		    return 5;
		case OpCodes.GETSTATIC_REF:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.PUTSTATIC_REF:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.GETFIELD_REF:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.PUTFIELD_REF:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.GETSTATIC_LONG:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.PUTSTATIC_LONG:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.GETFIELD_LONG:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.PUTFIELD_LONG:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		case OpCodes.SYS_INT:
		    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
		case OpCodes.SYS_EXC:
		    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
		case OpCodes.SYS_NOIMP:
		    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
		case OpCodes.SYS_INIT:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		default:
		    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		}*/
	    
	}
}

