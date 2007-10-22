/**
 * 
 */
package wcet.components.graphbuilder.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.basicblockgb.JOPInsnNode;
import wcet.components.graphbuilder.methodgb.MethodKey;
import wcet.components.graphbuilder.util.IFileList;
import wcet.components.graphbuilder.util.MethodBlockCache;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.4 20.01.2007
 * 
 * Interim representation of a method invocation. Used in the
 * first part of the cotrol flow graph creaion.
 */

// TODO find all possible implementations of a method - in resolve() if possible
// TODO handle inner classes - enclosed in a method (?method parameter access)
public class MethodBlock extends MethodNode {
    /**
     * cache used to search for method block
     */
    private static MethodBlockCache methodCache;

    /**
     * enclosing class of the method
     */
    private String owner;

    
    /**
     * source file from which this method's class was compiled
     */
    private String source;

    /**
     * all labels in the method sorted by their order in the method bytecode
     */
    private ArrayList<Label> labels;

    /**
     * method keys and method blocks of methods called from this method
     */
    private LinkedHashMap<MethodKey, MethodBlock> children;

    /**
     * Get the method block of the root method.
     * 
     * @param key - key of the root block
     * @param fileList - file list to use
     * @return method block of the root method
     * @throws TaskInitException
     */
    public static MethodBlock getRootBlock(MethodKey key, IFileList fileList)
	    throws TaskInitException {
	methodCache = new MethodBlockCache(fileList);
	return methodCache.getMethodBlock(key);
    }

    /**
     * Get method blocks of method's children. Before this method is called,
     * only children keys are stored in the children map, all method blocks are
     * null.
     * 
     * @throws TaskInitException
     */
    public void resolve() throws TaskInitException {
	this.constructChildren();
	Iterator<MethodBlock> blockIterator = this.children.values().iterator();
	while (blockIterator.hasNext()) {
	    blockIterator.next().resolve();
	}
    }

    /**
     * @return method enclosing class
     */
    public String getOwner() {
	return this.owner;
    }

    /**
     * @return file name ffrom which this method's enclosing class was compiled.
     */
    public String getSourceFile() {
	return this.source;
    }

    /**
     * Get child method block.
     * 
     * @param key - key of the child method
     * @return Method block of the child identified by the key
     */
    public MethodBlock getChild(MethodKey key) {
	return children.get(key);
    }

    /**
     * Get the predecessor label.
     * 
     * @param label - label of this method
     * @return label immediately before the parameter in the bytecode
     */
    public Label getPreviousLabel(Label label) {
	int labelPosition = this.labels.indexOf(label);
	return this.labels.get(labelPosition - 1);
    }

    /**
     * Get the successor label.
     * 
     * @param label - label of this method
     * @return label immediately after the parameter in the bytecode
     */
    public Label getNextLabel(Label label) {
	int labelPosition = this.labels.indexOf(label);
	return this.labels.get(labelPosition + 1);
    }

    /**
     * Compare positions of two labels.
     * 
     * @param l1 - first label
     * @param l2 - second label
     * @return 0 if equal, positive if l1 before l2, negative if l1 after l2
     */
    public int compareLabelPosition(Label l1, Label l2) {
	int idx1 = this.labels.indexOf(l1);
	int idx2 = this.labels.indexOf(l2);
	return idx2-idx1;
    }

    /**
     * Constructor called from ClassNode when reading the class bytecode.
     * 
     * @param owner - method enclosing class
     * @param source -source of the owner
     * @param access - access flags of the method
     * @param name - name of the method
     * @param desc - mehod desriptor
     * @param signature - method signature (generic information)
     * @param exceptions - exception class names that can be thrown
     */
    public MethodBlock(String owner, String source, final int access,
	    final String name, final String desc, final String signature,
	    final String[] exceptions) {

	super(access, name, desc, signature, exceptions);
	this.owner = owner;
	this.source = source;
	this.children = new LinkedHashMap<MethodKey, MethodBlock>();
	this.labels = new ArrayList<Label>();
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.MethodNode#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void visitMethodInsn(final int opcode, final String owner,
	    final String name, final String desc) {
	if (!owner.equals(IGraphBuilderConstants.NATIVECLASS_INTERNAL_NAME)) {
	    this.children.put(new MethodKey(owner, name, desc), null);
	    instructions.add(new MethodInsnNode(opcode, owner, name, desc));
	} else {
	    int opCode = this.jopNativeMethodToOpcode(name, desc);
	    if(opCode != -1)
		instructions.add(new JOPInsnNode(opCode));
	}
    }

    /**
     * Resolve children method keys into corresponding method blocks.
     * It is called only for methods realy needed in the graph.
     * 
     * @throws TaskInitException
     */
    private void constructChildren() throws TaskInitException {
	Iterator<MethodKey> keyIterator = this.children.keySet().iterator();
	while (keyIterator.hasNext()) {
	    MethodKey currKey = keyIterator.next();
	    MethodBlock currChild = methodCache.getMethodBlock(currKey);
	    // I could call resolve on the currChild right here
	    // but this way it should better use the methodBlockCache.
	    // Maybe I will try it later.
	    // System.out.println(currKey.getOwner()
                // +":"+currKey.getName()+":"+currKey.getDecription());
	    this.children.put(currKey, currChild);
	}
    }

    /**
     * Get the opcode of a corresponding bytecode for a Native method call.
     * 
     * @param name - Native method name
     * @param desc - Native method desriptor
     * @return - jop system bytecode opcode
     */
    private int jopNativeMethodToOpcode(String name, String desc) {
	int result;
	String lowerCaseName = name.toLowerCase();
	List<String> opCodesList = Arrays.asList(OpCodes.OPCODE_NAMES);
	if (name.equalsIgnoreCase("int2extMem")) {
	    lowerCaseName = "int2ext";
	} else if (name.equalsIgnoreCase("ext2intMem")) {
	    lowerCaseName = "ext2int";
	} else if (name.equalsIgnoreCase("rdIntMem")) {
	    lowerCaseName = "rdint";
	} else if (name.equalsIgnoreCase("wrIntMem")) {
	    lowerCaseName = "wrint";
	}
	result = opCodesList.indexOf("jopsys_" + lowerCaseName);
	if (result != -1) {
	    return result;
	} else {
	    // TODO implement other mappings
	    return OpCodes.JOPSYS_NOP;
	}
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.MethodNode#visitLabel(org.objectweb.asm.Label)
     */
    @Override
    public void visitLabel(Label label) {
	this.labels.add(label);
	super.visitLabel(label);
    }
}
