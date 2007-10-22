/**
 * 
 */
package wcet.components.graphbuilder.methodgb;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import wcet.components.graphbuilder.basicblockgb.JOPInsnNode;
import wcet.components.graphbuilder.util.IMethodBlockCache;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.hierarchy.IHierarchy;
import wcet.framework.interfaces.instruction.IJOPMethodVisitor;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.4 20.01.2007
 * 
 * Interim representation of a method invocation. Used in the first part of the
 * cotrol flow graph creaion.
 */

public class MethodBlock extends MethodNode implements IJOPMethodVisitor {
    /**
         * cache used to search for method block
         */
    private static IMethodBlockCache methodCache;

    private static IHierarchy hierarchy;

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

    private HashSet<MethodKey> specials;

    private boolean resolved = false;

    // DEBUG
    private FileWriter debug;

    /**
         * Get method blocks of method's children. Before this method is called,
         * only children keys are stored in the children map, all method blocks
         * are null.
         * 
         * @throws TaskInitException
         */
    public void resolve() throws TaskInitException {
	if (!resolved) {
	    this.constructChildren();
	    Iterator<MethodBlock> blockIterator = this.children.values()
		    .iterator();
	    while (blockIterator.hasNext()) {
		blockIterator.next().resolve();
	    }
	    this.resolved = true;
	}

    }

    public static void setMBCache(IMethodBlockCache cache) {
	MethodBlock.methodCache = cache;
    }

    public static void setHierarchy(IHierarchy hierarchy) {
	MethodBlock.hierarchy = hierarchy;
    }

    /**
         * @return method enclosing class
         */
    public String getOwner() {
	return this.owner;
    }

    /**
         * @return file name ffrom which this method's enclosing class was
         *         compiled.
         */
    public String getSourceFile() {
	return this.source;
    }

    /**
         * Get child method block.
         * 
         * @param key -
         *                key of the child method
         * @return Method block of the child identified by the key
         */
    public MethodBlock getChild(MethodKey key) {
	return children.get(key);
    }

    // /**
    // * Get the predecessor label.
    // *
    // * @param label - label of this method
    // * @return label immediately before the parameter in the bytecode
    // */
    // public Label getPreviousLabel(Label label) {
    // int labelPosition = this.labels.indexOf(label);
    // return this.labels.get(labelPosition - 1);
    // }
    //
    // /**
    // * Get the successor label.
    // *
    // * @param label - label of this method
    // * @return label immediately after the parameter in the bytecode
    // */
    // public Label getNextLabel(Label label) {
    // int labelPosition = this.labels.indexOf(label);
    // return this.labels.get(labelPosition + 1);
    // }
    //
    // /**
    // * Compare positions of two labels.
    // *
    // * @param l1 - first label
    // * @param l2 - second label
    // * @return 0 if equal, positive if l1 before l2, negative if l1 after
    // l2
    // */
    // public int compareLabelPosition(Label l1, Label l2) {
    // int idx1 = this.labels.indexOf(l1);
    // int idx2 = this.labels.indexOf(l2);
    // return idx2-idx1;
    // }

    /**
         * Constructor called from ClassNode when reading the class bytecode.
         * 
         * @param owner -
         *                method enclosing class
         * @param source
         *                -source of the owner
         * @param access -
         *                access flags of the method
         * @param name -
         *                name of the method
         * @param desc -
         *                mehod desriptor
         * @param signature -
         *                method signature (generic information)
         * @param exceptions -
         *                exception class names that can be thrown
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

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.tree.MethodNode#visitMethodInsn(int,
         *      java.lang.String, java.lang.String, java.lang.String)
         */
    @Override
    public void visitMethodInsn(final int opcode, final String owner,
	    final String name, final String desc) {
	this.children.put(new MethodKey(owner, name, desc), null);
	if (opcode == OpCodes.INVOKESPECIAL) {
	    if (this.specials == null)
		this.specials = new HashSet<MethodKey>();
	    this.specials.add(new MethodKey(owner, name, desc));
	}
	instructions.add(new MethodInsnNode(opcode, owner, name, desc));
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	super.visitLookupSwitchInsn(dflt, keys, labels);
	this.labels.add(dflt);
	this.labels.addAll(Arrays.asList(labels));
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt,
	    Label[] labels) {
	super.visitTableSwitchInsn(min, max, dflt, labels);
	this.labels.add(dflt);
	this.labels.addAll(Arrays.asList(labels));
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler,
	    String type) {
	super.visitTryCatchBlock(start, end, handler, type);
	this.labels.add(start);
	this.labels.add(end);
	this.labels.add(handler);
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.tree.MethodNode#visitJumpInsn(int,
         *      org.objectweb.asm.Label)
         */
    @Override
    public void visitJumpInsn(int opc, Label label) {
	super.visitJumpInsn(opc, label);
	this.labels.add(label);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.instruction.IJOPMethodVisitor#visitJOPInsn(int)
         */
    public void visitJOPInsn(int opCode) {
	instructions.add(new JOPInsnNode(opCode));
    }

    public boolean isJumpLabel(Label label) {
	return this.labels.contains(label);
    }

    /**
         * Resolve children method keys into corresponding method blocks. It is
         * called only for methods realy needed in the graph.
         * 
         * @throws TaskInitException
         */
    private void constructChildren() throws TaskInitException {
	Iterator<MethodKey> keyIterator = this.children.keySet().iterator();
	LinkedHashMap<MethodKey, MethodBlock> helpMap = new LinkedHashMap<MethodKey, MethodBlock>();
	// DEBUG
	if (this.debug == null) {
	    try {
		this.debug = new FileWriter("debug.txt");
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	}
	try {
	    this.debug.write("Resolving method:" + this.owner + "." + this.name
		    + "(" + this.desc + ")\n");
	    this.debug.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	while (keyIterator.hasNext()) {
	    MethodKey currKey = keyIterator.next();
	    MethodBlock currChild = methodCache.getMethodBlock(currKey);
	    // I could call resolve on the currChild right here
	    // but this way it should better use the methodBlockCache.
	    // Maybe I will try it later.
	    // System.out.println(currKey.getOwner()
	    // +":"+currKey.getName()+":"+currKey.getDecription());
	    if ((hierarchy != null)&&((this.specials==null)||(this.specials != null)
		    && (!this.specials.contains(currKey)))) {
		this.constructDoppelgaengers(currKey, helpMap);
	    } else {
		this.children.put(currKey, currChild);
	    }
	}
	this.children.putAll(helpMap);
    }

    private void constructDoppelgaengers(MethodKey key,
	    LinkedHashMap<MethodKey, MethodBlock> map) throws TaskInitException {
	HashSet<MethodKey> dgKeys = hierarchy.getAllMethodImpls(key);
	for (Iterator<MethodKey> iterator = dgKeys.iterator(); iterator
		.hasNext();) {
	    MethodKey currKey = iterator.next();
	    MethodBlock currDG = methodCache.getMethodBlock(currKey);
	    map.put(currKey, currDG);
	}
    }

    protected void replaceChild(MethodKey oldChild, MethodKey newChild) {
	if (this.children.containsKey(oldChild)
		&& (this.children.get(oldChild) == null)) {
	    this.children.remove(oldChild);
	    if (newChild != null)
		this.children.put(newChild, null);
	}
    }
}
