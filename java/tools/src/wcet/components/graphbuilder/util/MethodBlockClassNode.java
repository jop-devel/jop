/**
 * 
 */
package wcet.components.graphbuilder.util;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import wcet.components.graphbuilder.methodgb.MethodBlock;

/**
 * @author Elena Axamitova
 * @version 0.1 21.07.2007
 */
public class MethodBlockClassNode extends ClassNode {
    
    @SuppressWarnings("unchecked")
    @Override
    public MethodVisitor visitMethod(final int access,
	    final String name, final String desc,
	    final String signature, final String[] exceptions) {
	MethodBlock mn = new MethodBlock(this.name, this.sourceFile,
		access, name, desc, signature, exceptions);
	methods.add(mn);
	return mn;
    }
    
    
}
