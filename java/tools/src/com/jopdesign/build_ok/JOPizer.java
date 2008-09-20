/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2004,2005, Flavius Gruian
  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.build_ok;

import java.util.*;
import java.io.*;

import org.apache.bcel.util.ClassPath;
import org.apache.bcel.classfile.*;

/**
 * @author flavius, martin
 *
 */
public class JOPizer implements Serializable{

  // needed to avoid issues related to store/load of serializable content
  private static final long serialVersionUID = 1307508540685275006L;
	
	public final static String nativeClass = "com.jopdesign.sys.Native";
	public final static String startupClass = "com.jopdesign.sys.Startup";
	public final static String jvmClass = "com.jopdesign.sys.JVM";
	public final static String helpClass = "com.jopdesign.sys.JVMHelp";
	public final static String bootMethod = "boot()V";
	public final static String mainMethod = "main([Ljava/lang/String;)V";
	
	public static String clinitSig = "<clinit>()V";

	
	public final static String stringClass = "java.lang.String";
	public final static String objectClass = "java.lang.Object";

	public static final int PTRS = 6;

	public static final int IMPORTANT_PTRS = 12;
	public static final int GCINFO_NONREFARRY = 6; // gci is at -1 from classinfo
	public static final int CLASSINFO_NONREFARRY = GCINFO_NONREFARRY + 1;
	public static final int GCINFO_REFARRY = GCINFO_NONREFARRY+2; // gci must be at classinfo-1
	public static final int CLASSINFO_REFARRY = GCINFO_NONREFARRY + 1;
	public static final int CLINITS_OFFSET = 11;

	// TODO add all changes???
	/**
	 * maximum method size (for minimum cache) in bytes
	 */
	public static final int METHOD_MAX_SIZE = 1024;

	public static boolean dumpMgci = false;

	/** .jop output file */
	transient PrintWriter out;
	/** text file for additional information */
	transient PrintWriter outTxt;

	org.apache.bcel.util.ClassPath classpath;
	/**
	 * Loaded classes, type is ClassInfo
	 */
	public List clazzes = new LinkedList();
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
	 * Length of the generated application in words.
	 * It is written as the first word in .jop
	 * Equal to the address of the first free memory = heap start.
	 */
	int length;
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

  // TODO: added to implement the symbol manager
	public static JOPizer jz;

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

		dumpMgci = System.getProperty("mgci", "false").equals("true");

    // TODO: small change to implement quickly the symbol manager
//		JOPizer jz = new JOPizer();
    jz = new JOPizer();
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
				jz.outTxt = new PrintWriter(new FileOutputStream(outFile+".txt"));

				jz.load(clsArgs);
// System.out.println(jz.clazz2cli);
				// add the super and method info to ClassInfo
				// collect String constants
				jz.iterate(new SetClassInfo(jz));
				
				// Reduce constant pool
				jz.iterate(new FindUsedConstants(jz));
				// length of the reduced cpool is now known
		        if(dumpMgci){
		          jz.iterate(new SetGCRTMethodInfo(jz));
		        }

		        // replace the wide instrucitons generated
				// by Sun's javac 1.5
				jz.iterate(new ReplaceIinc(jz));
				
				// add monitorenter and exit for synchronized
				// methods
				jz.iterate(new InsertSynchronized(jz));
				
		        
		        // dump of BCEL info to a text file
				jz.iterate(new Dump(jz));

				// BuildVT was after SetMethodInfo
				// we need it for replace of field offsets
				// is this ok?
				// TODO: split VT and field info...
				// Build the virtual tables
				BuildVT vt = new BuildVT(jz);
				jz.iterate(vt);
				
				// find all <clinit> methods and their dependency,
				// resolve the depenency and generate the list
				ClinitOrder cliOrder = new  ClinitOrder(jz);
				jz.iterate(cliOrder);
				MethodInfo.clinitList = cliOrder.findOrder();
				
				// change methods - replace Native calls
				// TODO: also change the index into the cp for the
				// reduced version.
				jz.iterate(new ReplaceNativeAndCPIdx(jz));
				// No further access via BCEL is now possible -
				// we have 'illegal' instructions in the bytecode.
	

				jz.codeStart = 2;
				// Now we can set the method info code and the address
				// jz.pointerAddr is set
				jz.iterate(new SetMethodInfo(jz));
				
				// How long is the <clinit> List?
				int cntClinit = MethodInfo.clinitList.size();
				// How long is the string table?
				StringInfo.stringTableAddress = jz.pointerAddr+PTRS+cntClinit+1;
				
				// calculate addresses for static fields
				jz.iterate(new CountStaticFields(jz));
				int addrVal = StringInfo.stringTableAddress + StringInfo.length;
				ClassInfo.addrValueStatic = addrVal; 
				int addrRef = ClassInfo.addrValueStatic + ClassInfo.cntValueStatic;
				ClassInfo.addrRefStatic = addrRef;
				
				// Start of class info
				jz.clinfoAddr = ClassInfo.addrRefStatic + ClassInfo.cntRefStatic;
				
				// Calculate class info addresses
				ClassAddress cla = new ClassAddress(jz, jz.clinfoAddr);
				jz.iterate(cla);
				// Now all sizes are known
				jz.length = cla.getAddress();

				// set back the start addresses
				ClassInfo.addrValueStatic = addrVal; 
				ClassInfo.addrRefStatic = addrRef;

				// As all addresses are now known we can
				// resolve the constants.
				jz.iterate(new ResolveCPool(jz));
				
				// Finally we can write the .jop file....
				new JopWriter(jz).write();

			}
		} catch(Exception e) { e.printStackTrace();}
	}
}
