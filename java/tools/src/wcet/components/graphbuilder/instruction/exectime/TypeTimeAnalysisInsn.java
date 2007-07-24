/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import com.jopdesign.wcet.WCETInstruction;

import wcet.framework.instruction.cache.TypeAnalysisInsn;
//import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
//OK
public class TypeTimeAnalysisInsn extends TypeAnalysisInsn implements
	ITimeAnalysisInstruction {

    public TypeTimeAnalysisInsn(int opc) {
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
	case OpCodes.NEW:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.ANEWARRAY:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.CHECKCAST:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.INSTANCEOF:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	default:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	}*/
    }

}
