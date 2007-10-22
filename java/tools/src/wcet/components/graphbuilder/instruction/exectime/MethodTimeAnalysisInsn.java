/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import com.jopdesign.wcet.WCETInstruction;

import wcet.framework.instruction.cache.MethodAnalysisInsn;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
// OK
public class MethodTimeAnalysisInsn extends MethodAnalysisInsn implements
	ITimeAnalysisInstruction {

    public MethodTimeAnalysisInsn(int opc) {
	super(opc);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.graphbuilder.instuctions.exectime.ITimeAnalysisInstruction#getCycles()
         */
    public int getCycles() {
	// the cycles needed to test if the method is in the cache and
	// the load of the method in the case of a cache miss will be suplied by
	// the invoke block following this instruction in the cfg;
	//int retValue = 0;
	int retValue = WCETInstruction.getCycles(this.opcode, false, 0);
	int b = WCETInstruction.calculateB(false, 0);
	switch (this.opcode) {
	case OpCodes.INVOKEVIRTUAL:
	   // retValue = 100 + 2 * ITimeAnalysisInstruction.rws;
	   // break;
	case OpCodes.INVOKESPECIAL:
	case OpCodes.INVOKESTATIC:
	    //retValue = 74 + ITimeAnalysisInstruction.rws;
	    //break;
	case OpCodes.INVOKEINTERFACE:
	    //retValue = 114 + 4 * ITimeAnalysisInstruction.rws;
	    //break;
	    if(b>this.getHiddenCycles())
		retValue -= b - this.getHiddenCycles();
	    break;
	default:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	}
	/*if (ITimeAnalysisInstruction.rws > 3) {
	    retValue += ITimeAnalysisInstruction.rws - 3;
	}
	if (ITimeAnalysisInstruction.rws > 2) {
	    retValue += ITimeAnalysisInstruction.rws - 2;
	}*/
	return retValue;
    }

    public int getHiddenCycles() {
	switch (this.opcode) {
	case OpCodes.INVOKEVIRTUAL:
	case OpCodes.INVOKESPECIAL:
	case OpCodes.INVOKESTATIC:
	case OpCodes.INVOKEINTERFACE:
	    return 37;
	default:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	}
    }
}
