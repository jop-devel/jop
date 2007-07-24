/**
 * 
 */
package wcet.framework.interfaces.instruction;

import org.objectweb.asm.MethodVisitor;

/**
 * @author Elena Axamitova
 * @version 0.1 19.04.2007
 */
public interface IJOPMethodVisitor extends MethodVisitor {
    //TODO more parameters???
    public void visitJOPInsn(int opCode);
}
