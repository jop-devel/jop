package wcet.framework.instruction.cache;

import wcet.framework.instruction.AnalysisInstruction;
import wcet.framework.interfaces.instruction.IAnalysisInstructionType;

public class JOPAnalysisInsn extends AnalysisInstruction{
	
	public JOPAnalysisInsn(int opc){
		super(opc);
		this.type = IAnalysisInstructionType.INT_INSN;
	}

	//TODO check 
	//jop native instructions are public static native - so 
	//it is probalbly invokestatic - length 3
	public int get8BitLength() {
	    return 3;
	}


}

