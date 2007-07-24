/**
 * 
 */
package wcet.framework.instruction.cache;

import wcet.framework.instruction.AnalysisInstruction;
import wcet.framework.interfaces.instruction.IAnalysisInstructionType;
import wcet.framework.interfaces.instruction.OpCodes;
import wcet.framework.interfaces.instruction.cache.ICacheAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
public class MethodAnalysisInsn extends AnalysisInstruction implements ICacheAnalysisInstruction{
	
	public MethodAnalysisInsn(int opc){
		super(opc);
		this.type = IAnalysisInstructionType.METHOD_INSN;
	}

	public int get8BitLength() {
	    if(this.opcode==OpCodes.INVOKEINTERFACE){
		return 5;
	    }else{
		return 3;
	    }
	}

}
