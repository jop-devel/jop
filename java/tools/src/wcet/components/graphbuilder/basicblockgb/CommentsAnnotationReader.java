/**
 * 
 */
package wcet.components.graphbuilder.basicblockgb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.util.FileList;
import wcet.framework.exceptions.InitException;
import wcet.framework.exceptions.TaskException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.instruction.IJOPMethodVisitor;

/**
 * @author Elena Axamitova
 * @version 0.1 08.03.2007
 * 
 * Reads wca loop annotations from source file comments and simulates 
 * java annotations for the next visitor in chain.
 */
// TODO implemented, since there is no jop jdk for java 1.5 and
// annotations do not work
// QUESTION move all this to MethodBlock (read in constructChildren())
public class CommentsAnnotationReader implements IAnalyserComponent,
	ClassVisitor {
   private static final String[] LOOP_WCA_ANNOTATIONS = { "WCA loop" };

    private static final int CAPACITY = 20;

    private IDataStore dataStore;

    private ClassVisitor lastVisitor;

    private String currSource;

    private String currMethodName;

    private String currMethodDesc;

    private LinkedHashMap<String, int[][]> annotationCache;

    private FileList fileList;

    private String errorMessages;

    private ArrayList<String> lines;

    public CommentsAnnotationReader(IDataStore ds) {
	this.dataStore = ds;
	this.annotationCache = new LinkedHashMap<String, int[][]>(
		CommentsAnnotationReader.CAPACITY, 0.75f, true) {
	    protected boolean removeEldestEntry(
		    Map.Entry<String, int[][]> eldest) {
		return this.size() > CommentsAnnotationReader.CAPACITY;
	    }
	};
	this.errorMessages = "";
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
	this.lastVisitor = (ClassVisitor) this.dataStore
		.getObject(IGraphBuilderConstants.LAST_BB_CLASS_VISITOR_KEY);
	String path = this.dataStore.getSourcepath();
	String jopPath = (String)this.dataStore.getObject(IGraphBuilderConstants.JOP_HOME_KEY);
	if (!jopPath.endsWith("/")) jopPath+="/";
	path += File.pathSeparator + jopPath+IGraphBuilderConstants.JOP_JDK_BASE_REL_SOURCEPATH
	+File.pathSeparator + jopPath+IGraphBuilderConstants.JOP_JDK_11_REL_SOURCEPATH+File.pathSeparator + jopPath+IGraphBuilderConstants.JOP_SYSTEM_REL_SOURCEPATH;
	this.fileList = new FileList(path, ".java");
	this.fileList.findAllFiles();
	this.dataStore.storeObject(
		IGraphBuilderConstants.LAST_BB_CLASS_VISITOR_KEY, this);
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
    public String call() throws Exception {
	// this component is not executed explicitly, it is called in the chain
	// from graph writer
	if (!this.errorMessages.equals(""))
	    throw new TaskException(this.errorMessages);
	return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitSource(java.lang.String, java.lang.String)
     */
    public void visitSource(String source, String debug) {
	this.currSource = source;
	this.lines = new ArrayList<String>();
	BufferedReader fileBufReader = null;
	try {
	    String currSourceName = currSource.substring(0, currSource
		    .indexOf(".java"));
	    fileBufReader = new BufferedReader(new FileReader(fileList
		    .getFilePath(currSourceName)));
	} catch (FileNotFoundException e) {
	    errorMessages += "AnnoReader: FileNotFound :" + currSource + ".\n";
	}
	String currLineText = "";
	do {
	    this.lines.add(currLineText);
	    try {
		currLineText = fileBufReader.readLine();
	    } catch (IOException e) {
		this.errorMessages += "Unable to read source file: " + source
			+ ".";
	    }
	} while (currLineText != null);

	try {
	    fileBufReader.close();
	} catch (IOException e) {
	    // ignore;
	}
	if (this.lastVisitor != null)
	    this.lastVisitor.visitSource(source, debug);
    }

    public MethodVisitor visitMethod(int access, String name, String desc,
	    String signature, String[] exceptions) {
	this.currMethodName = name;
	this.currMethodDesc = desc;
	MethodVisitor lastMethVisitor;
	if (this.lastVisitor != null) {
	    lastMethVisitor = this.lastVisitor.visitMethod(access, name, desc,
		    signature, exceptions);
	} else {
	    lastMethVisitor = null;
	}
	return new WCETSourceAnnotationVisitor(lastMethVisitor);
    }

    private String getCurrentKey() {
	return this.currSource + "$" + this.currMethodName + "§"
		+ this.currMethodDesc + "§";
    }

    class WCETSourceAnnotationVisitor extends MethodAdapter implements
	    IJOPMethodVisitor {

	private static final String ANNOTATION_DESCRIPTOR = "Lwcet/framework/interfaces/annotations/AnalyserAnnotation;";

	private ArrayList<Integer> lineNumbersOfMethodList;

	private ArrayList<Integer>[] annotations;

	@SuppressWarnings("unchecked")
	public WCETSourceAnnotationVisitor(MethodVisitor mv) {
	    super(mv);
	    this.lineNumbersOfMethodList = new ArrayList<Integer>();
	    this.annotations = new ArrayList[LOOP_WCA_ANNOTATIONS.length];
	    for (int i = 0; i < LOOP_WCA_ANNOTATIONS.length; i++) {
		this.annotations[i] = new ArrayList<Integer>();
	    }
	}

	@Override
	public void visitLineNumber(int line, Label start) {
	    if (!this.lineNumbersOfMethodList.contains(line)) {
		this.lineNumbersOfMethodList.add(line);
	    }
	    if (this.mv != null)
		this.mv.visitLineNumber(line, start);
	}

	public void visitJOPInsn(int opCode) {
	    if (this.mv != null) {
		((IJOPMethodVisitor) this.mv).visitJOPInsn(opCode);
	    }
	}

	@Override
	public void visitEnd() {
	    if (this.lineNumbersOfMethodList.size() > 0) {
		// find where the method begins and ends in the source file
		Collections.sort(this.lineNumbersOfMethodList);
		int firstLine = this.lineNumbersOfMethodList.get(0);
		int lastLine = this.lineNumbersOfMethodList
			.get(this.lineNumbersOfMethodList.size() - 1);
		// is it in the cache?
		int[][] result = annotationCache.get(getCurrentKey()
			+ firstLine);

		if (result == null) {// if not, read annotations from source
		    // file
		    result = this.readSourceFile(firstLine, lastLine);
		}
		// visit all annotations
		AnnotationVisitor annoVisitor = this.mv.visitAnnotation(
			ANNOTATION_DESCRIPTOR, false);
		for (int i = 0; i < LOOP_WCA_ANNOTATIONS.length; i++) {
		    if (result[i] != null) {
			annoVisitor.visit("type", LOOP_WCA_ANNOTATIONS[i]);
			annoVisitor.visit("value", result[i]);
		    }
		}
	    }

	    if (this.mv != null)
		this.mv.visitEnd();
	}

	@SuppressWarnings("unchecked")
	private int[][] readSourceFile(int firstLine, int lastLine) {
	    int[][] result = new int[LOOP_WCA_ANNOTATIONS.length][];
	    annotations = new ArrayList[LOOP_WCA_ANNOTATIONS.length];

	    int currLineNr = firstLine - 1;
	    String currLineText;
	    while (currLineNr++ < lastLine) {
		currLineText = lines.get(currLineNr);
		if (this.lineNumbersOfMethodList.contains(currLineNr)) {
		    for (int i = 0; i < LOOP_WCA_ANNOTATIONS.length; i++) {
			int idx = currLineText.indexOf("@"
				+ LOOP_WCA_ANNOTATIONS[i]);
			if (idx != -1) {
			    if (annotations[i] == null) {
				annotations[i] = new ArrayList<Integer>();
			    }
			    String rest = currLineText.substring(idx
				    + LOOP_WCA_ANNOTATIONS[i].length() + 1);
			    StringTokenizer strTok = new StringTokenizer(rest,
				    "= \t\n\r\f");
			    int value = Integer.decode(strTok.nextToken())
				    .intValue();
			    annotations[i].add(value);
			}
		    }
		}
	    }
	    for (int i = 0; i < LOOP_WCA_ANNOTATIONS.length; i++) {
		result[i] = this.annoIntArray(i);
	    }
	    return result;
	}

	private int[] annoIntArray(int index) {
	    if (this.annotations[index] != null) {
		int[] result = new int[this.annotations[index].size()];
		Iterator<Integer> iterator = this.annotations[index].iterator();
		for (int i = 0; i < result.length; i++) {
		    result[i] = iterator.next().intValue();
		}
		return result;
	    } else {
		return null;
	    }
	}
    }

    // bellow delegator methods only
    public void visit(int arg0, int arg1, String arg2, String arg3,
	    String arg4, String[] arg5) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visit(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
	if (this.lastVisitor != null)
	    return this.lastVisitor.visitAnnotation(arg0, arg1);
	else
	    return null;
    }

    public void visitAttribute(Attribute arg0) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitAttribute(arg0);
    }

    public void visitEnd() {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitEnd();
    }

    public FieldVisitor visitField(int arg0, String arg1, String arg2,
	    String arg3, Object arg4) {
	if (this.lastVisitor != null)
	    return this.visitField(arg0, arg1, arg2, arg3, arg4);
	else
	    return null;
    }

    public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitInnerClass(arg0, arg1, arg2, arg3);
    }

    public void visitOuterClass(String arg0, String arg1, String arg2) {
	if (this.lastVisitor != null)
	    this.lastVisitor.visitOuterClass(arg0, arg1, arg2);
    }
}
