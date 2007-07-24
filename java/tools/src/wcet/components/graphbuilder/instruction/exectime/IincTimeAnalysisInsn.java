/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import com.jopdesign.wcet.WCETInstruction;

import wcet.framework.instruction.cache.IincAnalysisInsn;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
//OK
public class IincTimeAnalysisInsn extends IincAnalysisInsn
		implements ITimeAnalysisInstruction {
	
	public IincTimeAnalysisInsn(int opc){
		super(opc);
	}
	
	/* (non-Javadoc)
	 * @see wcet.components.graphbuilder.instuctions.exectime.ITimeAnalysisInstruction#getCycles()
	 */
	public int getCycles() {
	    return WCETInstruction.getCycles(this.opcode, false, 0);
		//return 8;
	}

}
