/*
 * Created on 03.06.2005
 *
 */
package com.jopdesign.build;

import java.util.*;
import java.io.*;

import org.apache.bcel.util.ClassPath;
import org.apache.bcel.classfile.*;

import com.sun.corba.se.ActivationIDL.Repository;

/**
 * @author flavius, martin
 *
 */
public class JOPizer {
	
	public final static String nativeClass = "com.jopdesign.sys.Native";
	public final static String startupClass = "com.jopdesign.sys.Startup";
	public final static String jvmClass = "com.jopdesign.sys.JVM";
	public final static String helpClass = "com.jopdesign.sys.JVMHelp";
	public final static String bootMethod = "boot()V";
	public final static String mainMethod = "main([Ljava/lang/String;)V";
	
	public static String clinitSig = "<clinit>()V";

	
	public final static String stringClass = "java.lang.String";
	public final static String objectClass = "java.lang.Object";

public static final int PTRS = 4;
	public static final int IMPORTANT_PTRS = 12;
	public static final int GCINFO_NONREFARRY = 6; // gci is at -1 from classinfo
	public static final int CLASSINFO_NONREFARRY = GCINFO_NONREFARRY + 1;
	public static final int GCINFO_REFARRY = GCINFO_NONREFARRY+2; // gci must be at classinfo-1
	public static final int CLASSINFO_REFARRY = GCINFO_NONREFARRY + 1;
	public static final int CLINITS_OFFSET = 11;

	// TODO add all changes???
	public static final int METHOD_MAX_SIZE = 1024;

	public static boolean useHandle = true;
	public static boolean useGC = false;

	PrintWriter out;
	
	org.apache.bcel.util.ClassPath classpath; // = ClassPath.SYSTEM_CLASS_PATH;
	/**
	 * Loaded classes, type is ClassInfo
	 */
	List clazzes = new LinkedList();
	/**
	 * Mapping from JavaClass to the ClassInfo
	 */
//	HashMap clazz2cli = new HashMap();
	/**
	 * 
	 * @param jc a JavaClass object
	 * @return the ClassInfo object
	 */
/* not used - info is in ClassInfo
	public ClassInfo getClassInfoX(JavaClass jc) {
		
//System.err.println(jc.getClassName());
//System.err.println(clazz2cli);
		return (ClassInfo) clazz2cli.get(jc);
	}
*/
	/**
	 * The class that contains the main method.
	 */
	static String mainClass;
	
	/**
	 * Start of bytecode
	 */
	int codeStart;
	/**
	 * Address of the important pointers
	 */
	int pointerAddr;
	/**
	 * Address of class info structures
	 */
	int clinfoAddr;
	/**
	 * Address of first free memory
	 */
	int heapStart;
	
	public JOPizer() {
		
		classpath = new org.apache.bcel.util.ClassPath(".");
	}
	
	/**
	 * Load all classes and the super classes from the argument list.
	 * @throws IOException
	 */
	private void load(Set clsArgs) throws IOException {
		
		JavaClass[] jcl = new JavaClass[clsArgs.size()];
		
		Iterator i = clsArgs.iterator();
		for (int nr=0; i.hasNext(); ++nr) {
			String clname = (String) i.next();
			InputStream is = classpath.getInputStream(clname);
			jcl[nr] = new ClassParser(is, clname).parse();
			System.out.println(jcl[nr].getClassName());

		}
		TransitiveHull hull = new TransitiveHull(classpath, jcl);
		hull.start();
		System.out.println(Arrays.asList(hull.getClassNames()));
		JavaClass[] jc = hull.getClasses();
		// clazzes contains now the closure of the application
		for (int j=0; j<jc.length; ++j) {
			// The class Native is NOT used in a JOP application
			if (!jc[j].getClassName().equals(nativeClass)) {
				ClassInfo cli = new ClassInfo(jc[j]);
				clazzes.add(cli);
//				clazz2cli.put(jc[j], cli);
			}
		}	
	}
	
	private void iterate(Visitor v) {

		Iterator it = clazzes.iterator();
//System.err.println("------------------");
		while (it.hasNext()) {
			JavaClass clz = ((ClassInfo) it.next()).clazz;
//System.err.println("it:"+clz.getClassName());
			new DescendingVisitor(clz, v).visit();
		}	
	}
	
	/**
	 * @param clazz
	 */
/*	private void addSuper(JavaClass clazz) {
		for (JavaClass scl=clazz.getSuperClass(); scl!=null; scl=scl.getSuperClass()) {
			if (!clazzes.contains(scl)) {
				clazzes.add(scl);
				System.out.println(scl.getClassName());					
			}
		}
	}
*/
	public static void main(String[] args) {
		String outFile = "/tmp/test.jop";
		
		JOPizer jz = new JOPizer();
		HashSet clsArgs = new HashSet();

		clsArgs.add(startupClass);
		clsArgs.add(jvmClass);
		clsArgs.add(helpClass);
		
		try {
			if(args.length == 0) {
				System.err.println("JOPizer arguments: [-cp classpath] [-o file] class [class]*");
			} else {
				for(int	i=0; i < args.length; i++) {
					if(args[i].equals("-cp")) {
						i++;
						jz.classpath = new org.apache.bcel.util.ClassPath(args[i]);
						continue;
					}
					if(args[i].equals("-o")) {
						i++;
						outFile = args[i];
						continue;
					}
					
					clsArgs.add(args[i]);
					
					// The last class contains the main method
					// We also allow / notation as used with JCC
					mainClass = args[i].replace('/', '.');
//					AppInfo.mainClass = args[i];
				}
				
				System.out.println("CLASSPATH="+jz.classpath+"\tmain class="+mainClass);
				
				jz.out = new PrintWriter(new FileOutputStream(outFile));

				jz.load(clsArgs);
// System.out.println(jz.clazz2cli);
				// add the super and method info to ClassInfo
				// collect String constants
				jz.iterate(new SetClassInfo(jz));
				
				// Reduce constant pool
				jz.iterate(new FindUsedConstants(jz));
				// length of the reduced cpool is now known

				// jz.iterate(new Dump(jz));
				
				// change methods - replace Native calls
				// TODO: also change the index into the cp for the
				// reduced version.
				jz.iterate(new ReplaceNativeAndCPIdx(jz));
				// No further access via BCEL is now possible -
				// we have 'illegal' instructions in the bytecode.
	

				jz.codeStart = 1;
				// Now we can set the method info code and the address
				// jz.pointerAddr is set
				jz.iterate(new SetMethodInfo(jz));
				
				// Build the virtual tables
				BuildVT vt = new BuildVT(jz);
				jz.iterate(vt);
				
				// How long is the <clinit> List?
				int cntClinit = MethodInfo.clinitList.size();
				// How long is the string table?
				StringInfo.stringTableAddress = jz.pointerAddr+PTRS+cntClinit+1;
				
				// Start of class info
				jz.clinfoAddr = StringInfo.stringTableAddress + StringInfo.length;
				// Calculate class info addresses
				ClassAddress cla = new ClassAddress(jz, jz.clinfoAddr);
				jz.iterate(cla);
				// Now all sizes are known
				jz.heapStart = cla.getAddress();
				
				// As all addresses are now known we can
				// resolve the constants.
				jz.iterate(new ResolveCPool(jz));
				
				// Finally we can write the .jop file....
				new JopWriter(jz).write();
				
			}
		} catch(Exception e) { e.printStackTrace();}
	}
}
