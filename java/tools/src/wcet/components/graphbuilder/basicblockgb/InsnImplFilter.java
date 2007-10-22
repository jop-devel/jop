/**
 * 
 */
package wcet.components.graphbuilder.basicblockgb;

import java.io.FileInputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.instruction.IImplementationConfig;
import wcet.framework.interfaces.instruction.IJOPMethodVisitor;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.3 03.03.2007
 * 
 * Precedes java bytecodes that are implemented in java with the implementing method.
 */
public class InsnImplFilter implements ClassVisitor, IAnalyserComponent {

    /**
     * Shared datastore
     */
    private IDataStore datastore;

    /**
     * Next visitor in the chain to which delegate calls
     */
    private ClassVisitor lastVisitor;

    /**
     * configuration settings - read in from an xml file
     */
    private Properties jopInsnConfig;

    /**
     * metod visitor of this class
     */
    private InsnImplFilterVisitor myVisitor;

    public InsnImplFilter(IDataStore ds) {
	this.datastore = ds;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
         */
    public boolean getOnlyOne() {
	return false;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
         */
    public int getOrder() {
	return IGlobalComponentOrder.NOT_EXECUTED;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
         */
    public void init() throws InitException {
	//get the last class visitore from the datastore and save me there
	//instead of it.
	this.lastVisitor = (ClassVisitor) this.datastore
		.getObject(IGraphBuilderConstants.LAST_CLASS_VISITOR_KEY);
	this.datastore.storeObject(
		IGraphBuilderConstants.LAST_CLASS_VISITOR_KEY, this);
	//get the config file and read it in
	String configFile = (String) datastore
		.getObject(IGraphBuilderConstants.JOP_CONFIG_FILE_KEY);
	this.jopInsnConfig = new Properties();
	try {
	    this.jopInsnConfig.loadFromXML(new FileInputStream(configFile));
	} catch (InvalidPropertiesFormatException e) {
	    throw new InitException(e);
	} catch (Exception e) {
	    // ignored, the default configuration used;
	}
	//since the InsnImplFilterVisitor is stateless, it is enough to have only one
	this.myVisitor = new InsnImplFilterVisitor();
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
    public String call() throws Exception {
	// this component is not executed explicitly, it is called in the chain
	// from graph writer
	return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
	    String arg3, String[] arg4) {
	if (this.lastVisitor != null) {
	    this.myVisitor.mv = this.lastVisitor.visitMethod(arg0, arg1, arg2,
		    arg3, arg4);
	} else {
	    this.myVisitor.mv = null;
	}
	// InsnImplFilterVisitor is stateless, so there is no need to create for
        // every
	// visited method a new one
	return this.myVisitor;

    }

    /**
     * @author Elena Axamitova
     * @version 0.1 05.06.2007
     * 
     * Method visitor that does nearly all the work.
     */
    class InsnImplFilterVisitor implements IJOPMethodVisitor {
	// -1 not set, 0 implemented in java, 1 not implemented in java
	/**
	 * bytecode implementation not set yet
	 */
	private static final int IMPL_NOT_SET = -1;

	/**
	 * bytecode implemented in java
	 */
	private static final int IMPL_IN_JAVA = 0;

	/**
	 * bytecode not implemented in java (hw, mc, not implemented)
	 */
	private static final int NOT_IMPL_IN_JAVA = 1;

	/**
	 * next method visitor in chain
	 */
	private MethodVisitor mv;

	public InsnImplFilterVisitor() {
	}

	/**
	 * If an instruction is implemeted in java, this method 
	 * inserts a call to the corresponding method. 
	 * @param opCode - opcode of the instruction
	 */
	private void handleJOPInstruction(int opCode) {

	    int implementation = InsnImplFilterVisitor.IMPL_NOT_SET;
	    //setting in the xml config file take preference
	    if (jopInsnConfig != null) {
		String configValue = jopInsnConfig
			.getProperty(OpCodes.OPCODE_NAMES[opCode]);
		if (configValue != null) {
		    if (configValue.equals(IImplementationConfig.JAVA_STRING)) {
			implementation = InsnImplFilterVisitor.IMPL_IN_JAVA;
		    } else {
			implementation = InsnImplFilterVisitor.NOT_IMPL_IN_JAVA;
		    }
		}
	    }
	    //if nothing found, look into default settings
	    if ((implementation == InsnImplFilterVisitor.IMPL_NOT_SET)
		    && (IImplementationConfig.JOP_INSN_IMPL_DEFAULT[opCode] == IImplementationConfig.JAVA)) {
		implementation = InsnImplFilterVisitor.IMPL_IN_JAVA;
	    }
	    
	   if (implementation == InsnImplFilterVisitor.IMPL_IN_JAVA) {
	       //call the method 
		String name = "f_" + OpCodes.OPCODE_NAMES[opCode];
		// QUESTION invokestatic or invoke special (private)
		this.mv.visitMethodInsn(OpCodes.INVOKESTATIC,
			IGraphBuilderConstants.JVMCLASS_INTERNAL_NAME, name,
			IImplementationConfig.JOP_METHOD_IMPL_DESCR[opCode]);
	    }
	}
	    // in the methods bellow the method handleJOPInstruction is called first
	//and then is the call delegated to the next visitor 
	    //if it exists
	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void visitFieldInsn(int arg0, String arg1, String arg2,
		String arg3) {
	    this.handleJOPInstruction(arg0);
	    if (this.mv != null)
		this.mv.visitFieldInsn(arg0, arg1, arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitIincInsn(int, int)
	 */
	public void visitIincInsn(int arg0, int arg1) {
	    this.handleJOPInstruction(arg0);
	    if (this.mv != null)
		this.mv.visitIincInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
	 */
	public void visitInsn(int arg0) {
	    this.handleJOPInstruction(arg0);
	    if (this.mv != null)
		this.mv.visitInsn(arg0);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitIntInsn(int, int)
	 */
	public void visitIntInsn(int arg0, int arg1) {
	    this.handleJOPInstruction(arg0);
	    if (this.mv != null)
		this.mv.visitIntInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitJumpInsn(int, org.objectweb.asm.Label)
	 */
	public void visitJumpInsn(int arg0, Label arg1) {
	    this.handleJOPInstruction(arg0);
	    if (this.mv != null)
		this.mv.visitJumpInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLdcInsn(java.lang.Object)
	 */
	public void visitLdcInsn(Object cnst) {
	    if ((cnst instanceof Integer) || (cnst instanceof Float)
		    || (cnst instanceof String) || (cnst instanceof Type)) {
		this.handleJOPInstruction(OpCodes.LDC_W);
	    } else {
		this.handleJOPInstruction(OpCodes.LDC2_W);
	    }
	    if (this.mv != null)
		this.mv.visitLdcInsn(cnst);
	    // TODO LDC or LDC_W
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLookupSwitchInsn(org.objectweb.asm.Label, int[], org.objectweb.asm.Label[])
	 */
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
	    this.handleJOPInstruction(OpCodes.LOOKUPSWITCH);
	    if (this.mv != null)
		this.mv.visitLookupSwitchInsn(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void visitMethodInsn(int oc, String owner, String name,
		String desc) {
	    this.handleJOPInstruction(oc);
	    // TODO bad - infinite loop - log in JVM - WORKAROUND
	    if ((owner.endsWith("JVMHelp") && (name.equals("wr")) && (desc
		    .equals("(Ljava/lang/String;)V"))))
		return;
	    if (this.mv != null)
		if (!owner
			.equals(IGraphBuilderConstants.NATIVECLASS_INTERNAL_NAME))
		    this.mv.visitMethodInsn(oc, owner, name, desc);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMultiANewArrayInsn(java.lang.String, int)
	 */
	public void visitMultiANewArrayInsn(String arg0, int arg1) {
	    this.handleJOPInstruction(OpCodes.MULTIANEWARRAY);
	    if (this.mv != null)
		this.mv.visitMultiANewArrayInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitTableSwitchInsn(int, int, org.objectweb.asm.Label, org.objectweb.asm.Label[])
	 */
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
		Label[] arg3) {
	    this.handleJOPInstruction(arg0);
	    if (this.mv != null)
		this.mv.visitTableSwitchInsn(arg0, arg1, arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitTypeInsn(int, java.lang.String)
	 */
	public void visitTypeInsn(int arg0, String arg1) {
	    this.handleJOPInstruction(arg0);
	    if (this.mv != null)
		this.mv.visitTypeInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitVarInsn(int, int)
	 */
	public void visitVarInsn(int arg0, int arg1) {
	    this.handleJOPInstruction(arg0);
	    if (this.mv != null)
		this.mv.visitVarInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see wcet.framework.interfaces.instruction.IJOPMethodVisitor#visitJOPInsn(int)
	 */
	public void visitJOPInsn(int opCode) {
	    this.handleJOPInstruction(opCode);
	    if (this.mv != null)
		((IJOPMethodVisitor) this.mv).visitJOPInsn(opCode);
	}

	    // all the methods bellow just delegate the call to the next visitor 
	    //if it exists
	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitAnnotation(java.lang.String, boolean)
	 */
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
	    if (this.mv != null)
		return this.mv.visitAnnotation(arg0, arg1);
	    else
		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitAnnotationDefault()
	 */
	public AnnotationVisitor visitAnnotationDefault() {
	    if (this.mv != null)
		return this.mv.visitAnnotationDefault();
	    else
		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitAttribute(org.objectweb.asm.Attribute)
	 */
	public void visitAttribute(Attribute arg0) {
	    if (this.mv != null)
		this.mv.visitAttribute(arg0);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitCode()
	 */
	public void visitCode() {
	    if (this.mv != null)
		this.mv.visitCode();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitEnd()
	 */
	public void visitEnd() {
	    if (this.mv != null)
		this.mv.visitEnd();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitFrame(int, int, java.lang.Object[], int, java.lang.Object[])
	 */
	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3,
		Object[] arg4) {
	    if (this.mv != null)
		this.mv.visitFrame(arg0, arg1, arg2, arg3, arg4);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLabel(org.objectweb.asm.Label)
	 */
	public void visitLabel(Label arg0) {
	    if (this.mv != null)
		this.mv.visitLabel(arg0);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLineNumber(int, org.objectweb.asm.Label)
	 */
	public void visitLineNumber(int arg0, Label arg1) {
	    if (this.mv != null)
		this.mv.visitLineNumber(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLocalVariable(java.lang.String, java.lang.String, java.lang.String, org.objectweb.asm.Label, org.objectweb.asm.Label, int)
	 */
	public void visitLocalVariable(String arg0, String arg1, String arg2,
		Label arg3, Label arg4, int arg5) {
	    if (this.mv != null)
		this.mv.visitLocalVariable(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMaxs(int, int)
	 */
	public void visitMaxs(int arg0, int arg1) {
	    if (this.mv != null)
		this.mv.visitMaxs(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitParameterAnnotation(int, java.lang.String, boolean)
	 */
	public AnnotationVisitor visitParameterAnnotation(int arg0,
		String arg1, boolean arg2) {
	    if (this.mv != null)
		return this.mv.visitParameterAnnotation(arg0, arg1, arg2);
	    else
		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitTryCatchBlock(org.objectweb.asm.Label, org.objectweb.asm.Label, org.objectweb.asm.Label, java.lang.String)
	 */
	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
		String arg3) {
	    if (this.mv != null)
		this.mv.visitTryCatchBlock(arg0, arg1, arg2, arg3);
	}
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    public void visit(int arg0, int arg1, String arg2, String arg3,
	    String arg4, String[] arg5) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visit(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
     */
    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
	if (this.lastVisitor != null)
	    return this.lastVisitor.visitAnnotation(arg0, arg1);
	else
	    return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitAttribute(org.objectweb.asm.Attribute)
     */
    public void visitAttribute(Attribute arg0) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitAttribute(arg0);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitEnd()
     */
    public void visitEnd() {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitEnd();
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    public FieldVisitor visitField(int arg0, String arg1, String arg2,
	    String arg3, Object arg4) {
	if (this.lastVisitor != null)
	    return this.lastVisitor.visitField(arg0, arg1, arg2, arg3, arg4);
	else
	    return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
     */
    public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitInnerClass(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitOuterClass(java.lang.String, java.lang.String, java.lang.String)
     */
    public void visitOuterClass(String arg0, String arg1, String arg2) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitOuterClass(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitSource(java.lang.String, java.lang.String)
     */
    public void visitSource(String arg0, String arg1) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitSource(arg0, arg1);
    }

}
