/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import com.jopdesign.wcet.WCETInstruction;

import wcet.framework.instruction.cache.LdcAnalysisInsn;
//import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
//OK
public class LdcTimeAnalysisInsn extends LdcAnalysisInsn implements
	ITimeAnalysisInstruction {

    public LdcTimeAnalysisInsn(int opc) {
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
	case OpCodes.LDC:
	    return 7 + ITimeAnalysisInstruction.rws;
	case OpCodes.LDC_W:
	    return 8 + ITimeAnalysisInstruction.rws;
	case OpCodes.LDC2_W:
	    //return 17 + 2 * ITimeAnalysisInstruction.rws;
	    int retValue = 17;
	    if (ITimeAnalysisInstruction.rws>2){
		retValue += ITimeAnalysisInstruction.rws-2;
	    }
	    if (ITimeAnalysisInstruction.rws>1){
		retValue += ITimeAnalysisInstruction.rws-1;
	    }
	    return retValue;
	default:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	}*/
    }
}
