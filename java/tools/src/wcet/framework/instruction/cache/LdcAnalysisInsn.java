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
public class LdcAnalysisInsn extends AnalysisInstruction implements ICacheAnalysisInstruction{
	
	public LdcAnalysisInsn(int opc){
		super(opc);
		this.type = IAnalysisInstructionType.LDC_INSN;
	}

	public int get8BitLength() {
	    if(this.opcode==OpCodes.LDC){
		return 2;
	    }else{
		return 3;//LDC_W or LDC2_W
	    }
	}

}
