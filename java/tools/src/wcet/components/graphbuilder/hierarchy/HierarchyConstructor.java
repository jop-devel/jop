/**
 * 
 */
package wcet.components.graphbuilder.hierarchy;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.util.IFileList;
import wcet.framework.exceptions.InitException;
import wcet.framework.hierarchy.Hierarchy;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.hierarchy.IHierarchyConstructor;

/**
 * @author Elena Axamitova
 * @version 0.1 07.09.2007
 */
public class HierarchyConstructor implements IAnalyserComponent {

    private IFileList fileList;

    private IDataStore dataStore;

    private IHierarchyConstructor hierarchy;

    private ClassVisitor hierarchyConstrVisitor;

    public HierarchyConstructor(IDataStore dataStore) {
	this.dataStore = dataStore;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
         */
    public boolean getOnlyOne() {
	return true;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
         */
    public int getOrder() {
	return IGraphBuilderConstants.HIERARCHY_CONSTRUCTOR;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
         */
    public void init() throws InitException {
	this.hierarchy = new Hierarchy();
	this.hierarchyConstrVisitor = new HierarchyClassVisitor();
	this.fileList = (IFileList) this.dataStore
		.getObject(IGraphBuilderConstants.FILE_LIST_KEY);
	if (this.fileList != null) {
	    HashSet<String> fileNames = this.fileList.getAllFiles();
	    for (Iterator<String> iterator = fileNames.iterator(); iterator
		    .hasNext();) {
		String fileName = iterator.next();
		try {
		    ClassReader classReader = new ClassReader(this.fileList
			    .getFileInputStream(fileName));
		    classReader.accept(this.hierarchyConstrVisitor,
			    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
		} catch (IOException e) {
		    throw new InitException(e);
		}
	    }
	}
	this.dataStore.storeObject(IGraphBuilderConstants.HIERARCHY_KEY,
		this.hierarchy);
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
    public String call() throws Exception {
	// nothing to do
	return null;
    }

    private class HierarchyClassVisitor implements ClassVisitor {
	String currItemName;

	public HierarchyClassVisitor() {
	    this.currItemName = null;
	}

	public void visit(int version, int access, String name,
		String signature, String superName, String[] interfaces) {
	    if ((superName != null) && ((access & Opcodes.ACC_INTERFACE) == 0)) {
		hierarchy.addSubClass(superName, name);
	    }
	    if (interfaces != null) {
		for (int i = 0; i < interfaces.length; i++) {
		    if ((access & Opcodes.ACC_INTERFACE) != 0) {
			hierarchy.addSubInterface(interfaces[i], name);
		    } else {
			hierarchy.addImplClass(interfaces[i], name);
		    }
		}
	    }
	    this.currItemName = name;
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions) {
	    if ((access & Opcodes.ACC_ABSTRACT) == 0) {
		hierarchy
			.addMethod(new MethodKey(this.currItemName, name, desc));
	    }
	    return null;
	}

	/*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitEnd()
         */
	public void visitEnd() {
	    this.currItemName = null;
	}

	// empty methods

	/*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String,
         *      boolean)
         */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
	    return null;
	}

	/*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitAttribute(org.objectweb.asm.Attribute)
         */
	public void visitAttribute(Attribute attr) {
	}

	/*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitField(int, java.lang.String,
         *      java.lang.String, java.lang.String, java.lang.Object)
         */
	public FieldVisitor visitField(int access, String name, String desc,
		String signature, Object value) {
	    return null;
	}

	/*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String,
         *      java.lang.String, java.lang.String, int)
         */
	public void visitInnerClass(String name, String outerName,
		String innerName, int access) {
	}

	/*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitOuterClass(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
	public void visitOuterClass(String owner, String name, String desc) {
	}

	/*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitSource(java.lang.String,
         *      java.lang.String)
         */
	public void visitSource(String source, String debug) {
	}
    }
}
