/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import wcet.framework.instruction.cache.FieldAnalysisInsn;

import com.jopdesign.wcet.WCETInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
//OK
public class FieldTimeAnalysisInsn extends FieldAnalysisInsn
		implements ITimeAnalysisInstruction {
	
	public FieldTimeAnalysisInsn(int opc){
		super(opc);
	}
	/* (non-Javadoc)
	 * @see wcet.components.graphbuilder.instuctions.exectime.ITimeAnalysisInstruction#getCycles()
	 */
	public int getCycles() {
	    return WCETInstruction.getCycles(this.opcode, false, 0);
		/*switch(this.opcode){
		case OpCodes.PUTSTATIC:
		   // WCETInstruction.getCycles();
		    return 13+ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
		case OpCodes.GETSTATIC:
		    return 12+2*ITimeAnalysisInstruction.rws;
		case OpCodes.PUTFIELD:
		    return 20+ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
		case OpCodes.GETFIELD:
		    return 17+2*ITimeAnalysisInstruction.rws;
		 default:
		     return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
		}*/
	}

}
