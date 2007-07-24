/**
 * 
 */
package wcet.components.graphbuilder.basicblockgb;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;

import wcet.framework.interfaces.instruction.IAnalysisInstructionType;
import wcet.framework.interfaces.instruction.IJOPMethodVisitor;

/**
 * @author Elena Axamitova
 * @version 0.1 19.04.2007
 * 
 * Instruction node of jop special bytecodes.
 */
public class JOPInsnNode extends AbstractInsnNode {

    public JOPInsnNode(int opCode){
	super(opCode);
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.AbstractInsnNode#accept(org.objectweb.asm.MethodVisitor)
     */
    @Override
    public void accept(MethodVisitor mv) {
	((IJOPMethodVisitor)mv).visitJOPInsn(this.opcode);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.AbstractInsnNode#clone(java.util.Map)
     */
    @Override
    public AbstractInsnNode clone(Map arg0) {
	return new JOPInsnNode(this.opcode);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.AbstractInsnNode#getType()
     */
    @Override
    public int getType() {
	return IAnalysisInstructionType.JOP_INSN;
    }

}
