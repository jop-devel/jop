/**
 * 
 */
package wcet.components.graphbuilder.methodgb;

import java.util.Iterator;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.util.IFileList;
import wcet.components.graphbuilder.util.IMethodBlockCache;
import wcet.components.graphbuilder.util.MethodBlockCache;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.3 12.01.2007
 * 
 * Caches method blocks of previously encountred classes. If the required method
 * block not found in cache, it will be created and stored. The cache cache uses
 * least recently accessed strategy when removing the oldest entry.
 */
public class MBCacheVisitor implements ClassVisitor, IMethodBlockCache {
    private MethodBlockCache methodBlockCache;

    /**
         * Construct a new cache that uses the provided file list
         * 
         * @param fl -
         *                file list to get class input streams from
         */

    public MBCacheVisitor(IFileList fl) {
	this.methodBlockCache = new MethodBlockCache(fl);
    }

    public void setFilter(ClassVisitor filter) {
	this.methodBlockCache.setFilter(filter);
    }

    /**
         * Get MethodBlock for the method key. If not in cache, read the class,
         * store all method blocks of the class in cache.
         * 
         * @param key
         * @return
         * @throws TaskInitException
         */
    public MethodBlock getMethodBlock(MethodKey key) throws TaskInitException {
	MethodBlock methBlock;
	if (key.getOwner().startsWith(
		IGraphBuilderConstants.JOP_SYSTEM_PACKAGE_NAME)) {
	    try {
		methBlock = this.getJOPMethodBlock(key);
	    } catch (Exception e) {
		throw new TaskInitException(e);
	    }
	} else {
	    methBlock = this.methodBlockCache.getMethodBlock(key);
	}
	return methBlock;
    }

    private MethodBlock getJOPMethodBlock(MethodKey key)
	    throws TaskInitException {
	MethodBlock result;
	// MEMO bad - infinite loop - wr(String) in JVMHelp -charAt in String-
	// WORKAROUND
	if (key.getOwner().endsWith("WorkAroundString")) {
	    MethodKey tempKey = new MethodKey("java/lang/String",
		    key.getName(), key.getDecription());
	    result = this.methodBlockCache.getMethodBlock(tempKey);
	    Iterator insnIterator = result.instructions.iterator();
	    while (insnIterator.hasNext()) {
		AbstractInsnNode insnNode = (AbstractInsnNode) insnIterator
			.next();
		if (insnNode.getOpcode() == OpCodes.NEW)
		    insnNode = new FieldInsnNode(OpCodes.GETFIELD,
			    "java/lang/String", "value", "[C");
		if ((insnNode.getType() == AbstractInsnNode.METHOD_INSN)) {
		    MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
		    if (((methodInsnNode.owner.equals("com/jopdesign/sys/JVM"))
			    && (methodInsnNode.name.equals("f_new")) && (methodInsnNode.desc
			    .equals("(I)I")))
			    || ((methodInsnNode.owner
				    .equals("java/lang/StringIndexOutOfBoundsException"))
				    && (methodInsnNode.name.equals("<init>")) && (methodInsnNode.desc
				    .equals("(I)V")))
			    || ((methodInsnNode.owner
				    .equals("com/jopdesign/sys/JVM"))
				    && (methodInsnNode.name.equals("f_athrow")) && (methodInsnNode.desc
				    .equals("(Ljava/lang/Throwable;)Ljava/lang/Throwable;")))) {
			FieldInsnNode tempInsnNode = new FieldInsnNode(OpCodes.GETFIELD,
				"java/lang/String", "value", "[C");
			result.instructions.insertBefore(methodInsnNode, tempInsnNode);
			result.instructions.remove(methodInsnNode);
		    }
		}
	    }
	    result.replaceChild(new MethodKey("com/jopdesign/sys/JVM", "f_new",
		    "(I)I"), null);
	    result.replaceChild(
		    new MethodKey("com/jopdesign/sys/JVM", "f_athrow",
			    "(Ljava/lang/Throwable;)Ljava/lang/Throwable;"),
		    null);
	    result.replaceChild(new MethodKey(
		    "java/lang/StringIndexOutOfBoundsException", "<init>",
		    "(I)V"), null);

	} else {
	    result = this.methodBlockCache.getMethodBlock(key);
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
		MethodKey oldKey = new MethodKey("java/lang/String", "charAt",
			"(I)C");
		MethodKey newKey = new MethodKey(
			IGraphBuilderConstants.JOP_SYSTEM_PACKAGE_NAME
				+ "WorkAroundString", "charAt", "(I)C");
		result.replaceChild(oldKey, newKey);
	    } else if (key.getOwner().endsWith("com/jopdesign/sys/Startup")
		    && key.getName().equals("exit")
		    && (key.getDecription().equals("()V"))) {
		Iterator insnIterator = result.instructions.iterator();
		while (insnIterator.hasNext()) {
		    AbstractInsnNode insnNode = (AbstractInsnNode) insnIterator
			    .next();
		    if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {
			MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
			if ((methodInsnNode.owner
				.endsWith("com/jopdesign/sys/JVM"))
				&& (methodInsnNode.name.equals("f_athrow"))
				&& (methodInsnNode.desc
					.equals("(Ljava/lang/Throwable;)Ljava/lang/Throwable;"))) {
			    FieldInsnNode tempInsnNode = new FieldInsnNode(OpCodes.GETFIELD,
					"java/lang/String", "value", "[C");
				result.instructions.insertBefore(methodInsnNode, tempInsnNode);
				result.instructions.remove(methodInsnNode);
			}
		    }
		}
		result.replaceChild(new MethodKey("com/jopdesign/sys/JVM",
			"f_athrow",
			"(Ljava/lang/Throwable;)Ljava/lang/Throwable;"), null);
	    }
	}
	// result.resolve();
	return result;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String,
         *      java.lang.String, java.lang.String, java.lang.String[])
         */
    public void visit(int version, int access, String name, String signature,
	    String superName, String[] interfaces) {
	this.methodBlockCache.getCurrClassNode().visit(version, access, name,
		signature, superName, interfaces);

    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String,
         *      boolean)
         */
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
	return this.methodBlockCache.getCurrClassNode().visitAnnotation(desc,
		visible);
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitAttribute(org.objectweb.asm.Attribute)
         */
    public void visitAttribute(Attribute attr) {
	this.methodBlockCache.getCurrClassNode().visitAttribute(attr);

    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitEnd()
         */
    public void visitEnd() {
	this.methodBlockCache.getCurrClassNode().visitEnd();
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitField(int, java.lang.String,
         *      java.lang.String, java.lang.String, java.lang.Object)
         */
    public FieldVisitor visitField(int access, String name, String desc,
	    String signature, Object value) {
	return this.methodBlockCache.getCurrClassNode().visitField(access,
		name, desc, signature, value);
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String,
         *      java.lang.String, java.lang.String, int)
         */
    public void visitInnerClass(String name, String outerName,
	    String innerName, int access) {
	this.methodBlockCache.getCurrClassNode().visitInnerClass(name,
		outerName, innerName, access);
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitMethod(int,
         *      java.lang.String, java.lang.String, java.lang.String,
         *      java.lang.String[])
         */
    public MethodVisitor visitMethod(int access, String name, String desc,
	    String signature, String[] exceptions) {
	return this.methodBlockCache.getCurrClassNode().visitMethod(access,
		name, desc, signature, exceptions);
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitOuterClass(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
    public void visitOuterClass(String owner, String name, String desc) {
	this.methodBlockCache.getCurrClassNode().visitOuterClass(owner, name,
		desc);
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitSource(java.lang.String,
         *      java.lang.String)
         */
    public void visitSource(String source, String debug) {
	this.methodBlockCache.getCurrClassNode().visitSource(source, debug);
    }
}
