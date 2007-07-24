/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import wcet.framework.instruction.cache.IntAnalysisInsn;

import com.jopdesign.wcet.WCETInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
public class IntTimeAnalysisInsn extends IntAnalysisInsn implements
	ITimeAnalysisInstruction {
    private int operand;

    public IntTimeAnalysisInsn(int opc, int op) {
	super(opc);
	this.operand = op;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.graphbuilder.instuctions.exectime.ITimeAnalysisInstruction#getCycles()
         */
    public int getCycles() {
	 return WCETInstruction.getCycles(this.opcode, false, 0);
	 /*switch (this.opcode) {
	case OpCodes.BIPUSH:
	    return 2;
	case OpCodes.SIPUSH:
	    return 3;
	case OpCodes.NEWARRAY:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	default:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	}*/
    }

    public int getOperand() {
	return this.operand;
    }
}
