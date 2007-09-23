/**
 * 
 */
package wcet.components.graphbuilder.methodgb;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.util.IFileList;
import wcet.components.graphbuilder.util.MethodBlockCache;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.instruction.IImplementationConfig;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 05.04.2007
 * 
 * MethodMap of jop system classes.
 */
public class JOPSystemMethodCache {
    private IFileList systemMethodsList;

    private MethodBlockCache jopSystemMethodCache;

    protected JOPSystemMethodCache(IFileList systemFl){
	this.systemMethodsList = systemFl;
	this.jopSystemMethodCache = new MethodBlockCache(this.systemMethodsList);
    }

    protected MethodBlock getJOPMethodBlock(MethodKey key)
	    throws TaskInitException {
	MethodBlock result;
	// MEMO bad - infinite loop - wr(String) in JVMHelp -charAt in String-
        // WORKAROUND
	if (key.getOwner().endsWith("WorkAroundString")) {
	    MethodKey tempKey = new MethodKey("java/lang/String",
		    key.getName(), key.getDecription());
	    result = this.jopSystemMethodCache.getMethodBlock(tempKey);
	    Iterator insnIterator = result.instructions.iterator();
	    while (insnIterator.hasNext()) {
		AbstractInsnNode insnNode = (AbstractInsnNode) insnIterator
			.next();
		if (insnNode.getOpcode() == OpCodes.NEW)
		    insnNode = new FieldInsnNode(OpCodes.GETFIELD,
			    "java/lang/String", "value", "[C");
		if (insnNode.getOpcode() == OpCodes.INVOKESPECIAL)
		    insnNode = new FieldInsnNode(OpCodes.GETFIELD,
			    "java/lang/String", "value", "[C");
	    }
	} else {
	    result = this.jopSystemMethodCache.getMethodBlock(key);
	    if ((key.getOwner().endsWith("JVMHelp")
		    && (key.getName().equals("wr")) && (key.getDecription()
		    .equals("(Ljava/lang/String;)V")))) {
		Iterator insnIterator = result.instructions.iterator();
		while (insnIterator.hasNext()) {
		    AbstractInsnNode insnNode = (AbstractInsnNode) insnIterator
			    .next();
		    if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {
			MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
			if ((methodInsnNode.owner.endsWith("String"))
				&& (methodInsnNode.name.equals("charAt"))
				&& (methodInsnNode.desc.equals("(I)C"))) {
			    methodInsnNode.owner = IGraphBuilderConstants.JOP_SYSTEM_PACKAGE_NAME
				    + "WorkAroundString";
			}
		    }
		}
	    }
	}
	result.resolve();
	return result;
    }

    public static void main(String[] args) {
	for (int i = 0; i < IImplementationConfig.JOP_INSN_IMPL_DEFAULT.length; i++) {
	    System.out.println("OpCode: " + i + " name:"
		    + OpCodes.OPCODE_NAMES[i] + " desc:"
		    + IImplementationConfig.JOP_METHOD_IMPL_DESCR[i] + ".");
	}
    }

}
