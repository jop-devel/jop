/**
 * 
 */
package wcet.framework.interfaces.instruction;

import org.objectweb.asm.Label;

/**
 * @author Elena Axamitova
 * @version 0.1 10.04.2007
 */
public interface IInstructionGenerator {
    public IAnalysisInstruction getFieldInsn(int opcode, String owner, String name, String desc);

    public IAnalysisInstruction getIincInsn(int var, int inc);

    public IAnalysisInstruction getInsn(int opcode);

    public IAnalysisInstruction getIntInsn(int opcode, int o);

    public IAnalysisInstruction getJumpInsn(int opcode, Label label);

    public IAnalysisInstruction getLdcInsn(Object cst);

    public IAnalysisInstruction getLookupSwitchInsn(Label dflt, int[] keys, Label[] labels);

    public IAnalysisInstruction getMethodInsn(int opcode, String owner, String name, String desc);

    public IAnalysisInstruction getMultiANewArrayInsn(String desc, int dims);

    public IAnalysisInstruction getTableSwitchInsn(int min, int max, Label dflt, Label[] labels);

    public IAnalysisInstruction getTypeInsn(int opcode, String desc);

    public IAnalysisInstruction getVarInsn(int opcode, int var);
    
    public IAnalysisInstruction getJOPInsn(int opcode);
}
