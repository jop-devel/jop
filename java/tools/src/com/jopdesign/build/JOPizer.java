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

package com.jopdesign.build;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import org.apache.bcel.classfile.Attribute;

import boxpeeking.instrument.bcel.AnnotationReader;

/**
 * @author flavius, martin
 *
 */
public class JOPizer extends AppInfo implements Serializable {

	// needed to avoid issues related to store/load of serializable content
	private static final long serialVersionUID = 1307508540685275006L;

	public final static String nativeClass = "com.jopdesign.sys.Native";
	public final static String startupClass = "com.jopdesign.sys.Startup";
	public final static String jvmClass = "com.jopdesign.sys.JVM";
	public final static String helpClass = "com.jopdesign.sys.JVMHelp";
	public final static String bootMethod = "boot()V";
	public final static String mainMethod = "main([Ljava/lang/String;)V";
	public final static String rttmClass = "rttm.internal.Utils";

	public final static String stringClass = "java.lang.String";
	public final static String objectClass = "java.lang.Object";

	public static final int PTRS = 6;

	public static final int IMPORTANT_PTRS = 12;
	public static final int GCINFO_NONREFARRY = 6; // gci is at -1 from classinfo
	public static final int CLASSINFO_NONREFARRY = GCINFO_NONREFARRY + 1;
	public static final int GCINFO_REFARRY = GCINFO_NONREFARRY+2; // gci must be at classinfo-1
	public static final int CLASSINFO_REFARRY = GCINFO_NONREFARRY + 1;
	public static final int CLINITS_OFFSET = 11;

	public static final boolean CACHE_INVAL = false;
	public static final boolean USE_RTTM = false;

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
	/** link info for the D$ analysis */
	transient PrintWriter outLinkInfo;

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

	// Added to implement the symbol manager
	public static JOPizer jz;

	public JOPizer(ClassInfo template) {
		super(template);
	}

	public static void main(String[] args) {

		dumpMgci = System.getProperty("mgci", "false").equals("true");

		if (USE_RTTM) {
			Attribute.addAttributeReader("RuntimeInvisibleAnnotations", new AnnotationReader());
		}


    // TODO: small change to implement quickly the symbol manager
//		JOPizer jz = new JOPizer();
		jz = new JOPizer(JopClassInfo.getTemplate());

		if(args.length < 3) {
			System.err.println("JOPizer arguments: [-cp classpath] -o file class [class]*");
			System.exit(-1);
		};
		jz.parseOptions(args);
		if(jz.outFile == null) {
			System.err.println("JOPizer: Missing argument: '-o file'");
			System.exit(-1);
		}
		jz.addClass(startupClass);
		jz.addClass(jvmClass);
		jz.addClass(helpClass);
		if (USE_RTTM) {
			jz.addClass(rttmClass);
		}		
		jz.excludeClass(nativeClass);

		try {
			jz.out = new PrintWriter(new FileOutputStream(jz.outFile));
			jz.outTxt = new PrintWriter(new FileOutputStream(jz.outFile+".txt"));
			jz.outLinkInfo = new PrintWriter(new FileOutputStream(jz.outFile+".link.txt"));

			jz.load(); 
			
			if (USE_RTTM) {
				jz.iterate(new ReplaceAtomicAnnotation(jz));
			}
			
			// Reduce constant pool
			// TODO: remove unused field and static field entries
			// and remove the code from resolveCPool(cp).
			jz.iterate(new FindUsedConstants(jz));
			// length of the reduced cpool is now known
	        if(dumpMgci){
	          jz.iterate(new SetGCRTMethodInfo(jz));
	        }

	        // replace the wide instructions generated
			// by Sun's javac 1.5
			jz.iterate(new ReplaceIinc(jz));

			// add monitorenter and exit for synchronized
			// methods
			jz.iterate(new InsertSynchronized(jz));

	        // dump of BCEL info to a text file
			jz.iterate(new Dump(jz, jz.outTxt));

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
			JopMethodInfo.clinitList = cliOrder.findOrder();

			// calculate addresses for static fields
			jz.iterate(new CountStaticFields(jz));
			int addrVal = 2;
			JopClassInfo.addrValueStatic = addrVal;
			int addrRef = JopClassInfo.addrValueStatic + JopClassInfo.cntValueStatic;
			JopClassInfo.addrRefStatic = addrRef;
			jz.iterate(new SetStaticAddresses(jz));
			// set back the start addresses
			JopClassInfo.addrValueStatic = addrVal;
			JopClassInfo.addrRefStatic = addrRef;

			// change methods - replace Native calls
			// TODO: also change the index into the cp for the
			// reduced version.
			jz.iterate(new ReplaceNativeAndCPIdx(jz));
			// No further access via BCEL is now possible -
			// we have 'illegal' instructions in the bytecode.

			jz.codeStart = JopClassInfo.addrRefStatic + JopClassInfo.cntRefStatic;;
			// Now we can set the method info code and the address
			// jz.pointerAddr is set
			jz.iterate(new SetMethodAddress(jz));

			// How long is the <clinit> List?
			int cntClinit = JopMethodInfo.clinitList.size();
			// How long is the string table?
			StringInfo.stringTableAddress = jz.pointerAddr+PTRS+cntClinit+1;

			// Start of class info
			jz.clinfoAddr = StringInfo.stringTableAddress + StringInfo.length;

			// Calculate class info addresses
			ClassAddress cla = new ClassAddress(jz, jz.clinfoAddr);
			jz.iterate(cla);
			// Now all sizes are known
			jz.length = cla.getAddress();

			// As all addresses are now known we can
			// resolve the constants.
			jz.iterate(new ResolveCPool(jz));

			// Finally we can write the .jop file....
			new JopWriter(jz).write();

			jz.outLinkInfo.close();

		} catch(Exception e) { e.printStackTrace();}
	}
}
