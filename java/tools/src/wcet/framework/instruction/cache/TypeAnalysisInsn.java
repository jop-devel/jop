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
public class TypeAnalysisInsn extends AnalysisInstruction implements ICacheAnalysisInstruction{
	
	public TypeAnalysisInsn(int opc){
		super(opc);
		this.type = IAnalysisInstructionType.TYPE_INSN;
	}

	public int get8BitLength() {
	    return 3;
	}

}
