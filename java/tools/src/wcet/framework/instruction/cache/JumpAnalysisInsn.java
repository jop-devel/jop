/**
 * 
 */
package wcet.framework.instruction.cache;

import wcet.framework.instruction.AnalysisInstruction;
import wcet.framework.interfaces.instruction.IAnalysisInstructionType;
import wcet.framework.interfaces.instruction.cache.ICacheAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
public class JumpAnalysisInsn extends AnalysisInstruction implements ICacheAnalysisInstruction{
	
	public JumpAnalysisInsn(int opc){
		super(opc);
		this.type = IAnalysisInstructionType.JUMP_INSN;
	}

	public int get8BitLength() {
	    //JSR_W and GOTO_W not used
	    return 3;
	}

}
