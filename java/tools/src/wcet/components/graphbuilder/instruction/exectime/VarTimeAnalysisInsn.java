/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import com.jopdesign.wcet.WCETInstruction;

import wcet.framework.instruction.cache.VarAnalysisInsn;
//import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
// OK
public class VarTimeAnalysisInsn extends VarAnalysisInsn implements
	ITimeAnalysisInstruction {

    public VarTimeAnalysisInsn(int opc) {
	super(opc);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.graphbuilder.instuctions.exectime.ITimeAnalysisInstruction#getCycles()
         */
    public int getCycles() {
	return WCETInstruction.getCycles(this.opcode, false, 0);
	/*switch (this.opcode) {
	case OpCodes.ILOAD:
	    return 2;
	case OpCodes.LLOAD:
	    return 11;
	case OpCodes.FLOAD:
	    return 2;
	case OpCodes.DLOAD:
	    return 11;
	case OpCodes.ALOAD:
	    return 2;
	case OpCodes.ISTORE:
	    return 2;
	case OpCodes.LSTORE:
	    return 11;
	case OpCodes.FSTORE:
	    return 2;
	case OpCodes.DSTORE:
	    return 11;
	case OpCodes.ASTORE:
	    return 2;
	case OpCodes.RET:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	default:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	}*/
    }
}
