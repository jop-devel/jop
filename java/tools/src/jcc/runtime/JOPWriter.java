/*
 *				JOPWriter.java				1.24				00/05/05				SMI
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").		 You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package runtime;
import components.*;
import vm.*;
import jcc.Util;
import util.*;
import java.util.*;
import java.io.OutputStream;



/*
 * The CoreImageWriter for the Embedded VM
 */

public class JOPWriter implements CoreImageWriter, Const, EVMConst {
	JOPStringTable stringTable = new JOPStringTable();
	JOPNameTable nameTable;
	JOPClassTable classTable;


	/* INSTANCE DATA */
	protected  String		 outputFileName;
	protected  Exception failureMode = null; // only interesting on failure


	protected OutputStream				  xxx;
	CCodeWriter out;

	protected	 vm.VMClassFactory	 classMaker = new vm.EVMClassFactory();

	//CConstants sc;
	boolean		formatError = false;
	boolean		verbose	 = false;
	boolean		classDebug  = false;

	boolean		buildingRelocationTable = false;
	boolean		relocatableROM = false;

	protected static final String		   staticStoreName = "JOP_staticData";
	protected static final String		   masterStaticStoreName = "JOP_masterStaticData";

	protected static final String		   FORWARD_STATIC_DECLARATION = "FORWARD_STATIC_DECLARATION\n";

	// In ROMjava.c we need to make several "forward static" declarations,
	// where the forward declaration has no initializer and the later
	// declaration has an initializer. Some compilers allow you to specify
	// simply "static" in both places. Other compiler treat this as a 
	// redefinition error and require the "forward" declaration to be
	// "external". So, we will emit the identifier
	// "FORWARD_STATIC_DECLARATION", and each specific compiler will
	// have to define this to be what is appropriate for that compiler.

	protected ClassnameFilterList		   nativeTypes;

	protected boolean					   classLoading = true; // by default.

	/* for statistics only */
	int  ncodebytes;
	int  ncatchframes;
	int  nmethods;
	int  nfields;
	int  nconstants;
	int  njavastrings;

	public static final int ACC_ARRAY_CLASS		= 0x1000;
	public static final int ACC_ROM_CLASS		  = 0x2000;
	public static final int ACC_ROM_NON_INIT_CLASS = 0x4000;

//
// added for JOP
//

	Map mapCodeStart = new HashMap();
	int addrCnt = 1;	// first word is pointer to main pointer
	class ClAddr {
		int stat;		// static fields first
		int start;
		int mtab;
		int ctab;
		int itab;
	};
	Map mapClAddr = new HashMap();

	class ClVT {
		int len;
		int[] key;
		int[] ptr;
		String[] nativeName;
		EVMMethodInfo meth[];
	}
	Map mapClVT = new HashMap();

	class IT {
		int nr;
		int key;
		String nativeName;
		EVMMethodInfo meth;
	}
	// 'global' interface table
	LinkedList listIT = new LinkedList();

	private static final int SYS_BC_START = 0xd0;

	static final int CLS_HEAD = 2;
	static final int METH_STR = 2;
	static final int CONST_STR = 1;

	static int addrString = 0;	// pointer to method table of class java.lang.String

// end added for JOP

	public JOPWriter( ){ 
		nameTable = new JOPNameTable(); 
		classTable = new JOPClassTable(nameTable);
		nameTable.classTable = classTable;
	}

	public void init( boolean classDebug, ClassnameFilterList nativeTypes, 
					  boolean verbose, int maxSegmentSize ){
		this.verbose = verbose;
		this.classDebug = classDebug;
		this.nativeTypes = nativeTypes;
	}

	public boolean setAttribute( String attribute ){
		return false; 
	}


	public boolean open( String filename ){
		if ( out != null ) { 
			close();
		}
		outputFileName = filename;
		if ( filename == null){
			xxx = System.out;
			out = new CCodeWriter( xxx );
		} else {
			try {
				xxx = new java.io.FileOutputStream( filename );
				out = new CCodeWriter( xxx );
			} catch ( java.io.IOException e ){
				failureMode = e;
				return false;
			}
		}
		return true;
	}


	public void close(){
		if (out != null) { 
			out.close();
			outputFileName = null;
			out = null;
		}
	}


	public boolean writeClasses( ConstantPool consts ) {
		return writeClasses(consts, null);
	}

	public void printError( java.io.PrintStream o ){
		if ( failureMode != null ){
			failureMode.printStackTrace( o );
		} else {
			if ( out != null && out.checkError() )
				o.println(outputFileName+": Output write error");
		}
	}

	private MethodConstant runCustomCodeConstant;

	public boolean writeClasses( ConstantPool consts, 
								 ConstantPool sharedconsts ){
		ClassClass classes[] = ClassClass.getClassVector( classMaker );
		ClassClass.setTypes();
		
		runCustomCodeConstant = 
				new MethodConstant(new ClassConstant(
										new UnicodeConstant("java/lang/Class")),
								   new NameAndTypeConstant(
										new UnicodeConstant("runCustomCode"),
										new UnicodeConstant("()V")));



		
		if (verbose) { 
			System.out.println(Localizer.getString("cwriter.writing_classes"));
		}
		try {

			initialPass(classes);

			if (!buildingRelocationTable) { 
				// write out some constant pool stuff here,
				//writeProlog();

				
				/* Write out the static store */
/*
				writeStaticStore(classes);
*/

				/* Write out the UTF table */
/*
				if (relocatableROM) { 
					out.println("void *UTFSectionHeader = &UTFSectionHeader;");
				}
				nameTable.writeTable(out, "UTFStringTable");
				if (relocatableROM) { 
					out.println("void *UTFSectionTrailer = &UTFSectionTrailer;");
				}
*/
				/* Write out class definitions */
				writeAllClassDefinitions(classes); 

			} else { 
				writeRelocationFile(classes);
			}
		} catch (DataFormatException e) { 
			out.flush();
			System.out.println(e);
			e.printStackTrace(System.out);
			formatError = true;
		} catch (RuntimeException e) { 
			out.flush();
			System.out.println(e);
			e.printStackTrace(System.out);
			formatError = true;
		}
		return (!formatError) && (! out.checkError());
	}

	protected void initialPass(ClassClass classes[]) { 
		for (int i = 0; i < classes.length; i++) { 
			EVMClass cc = (EVMClass) classes[i];
			if (cc.isPrimitiveClass()) { 
				/* Do nothing */
			} else if (cc.isArrayClass()) {			 
				/* Make sure this is in the table */
				classTable.addArrayClass(cc.ci.className);   
			} else { 
				/* Make sure this is in the table */
				classTable.getClassKey(cc.ci.className);

				EVMMethodInfo m[] = cc.methods;
				FieldInfo	  f[] = cc.ci.fields;
				ConstantObject cpool[] = cc.ci.constants;
				ClassConstant  intfs[] = cc.ci.interfaces;
				
				int methodCount = m.length;
				int fieldCount =  f.length;
				int constantCount = cpool.length;
				int intfCount = intfs.length;

				for (int index = 0; index < methodCount; index++) { 
					classTable.getNameAndTypeKey(m[index].method);
				}
				for (int index = 0; index < fieldCount; index++) { 
					classTable.getNameAndTypeKey(f[index]);
					if (f[index].isStaticMember() && 
						f[index].value instanceof StringConstant) { 
						stringTable.intern( (StringConstant) f[index].value);
					}
				}
				for (int index = 1; index < constantCount;) {
					ConstantObject obj = cpool[index];
					if (obj instanceof StringConstant){
						stringTable.intern( (StringConstant) obj);
					}
					index += obj.nSlots;
				}
			}
		}
		stringTable.stringHashTable.closed = true;
		classTable.closed = true;
		nameTable.closed = true;
	}


	/*
	 * Merge the static store for all classes into one area.
	 * Sort static cells, refs first, and assign offsets.
	 * Write the resulting master data glob and arrange for
	 * it to be copied to writable at startup.
	 */
	void writeStaticStore (ClassClass classes[]) {
		int nclass = classes.length;
		int nStaticWords = 1; // 1 header word assumed
		int nRef	= 0;

		/*
		 * Count all statics.
		 * Count refs, and count number of words.
		 */
		for (int cno = 0; cno < nclass; cno++ ){
			EVMClass c = (EVMClass)(classes[cno]);
			c.orderStatics();
			nRef += c.nStaticRef;
			nStaticWords += c.nStaticWords;
			FieldInfo f[] = c.statics;
		}

		/*
		 * Assign offsets and at the same time
		 * write any initial values.
		 */
		int refOff = 1;
		int scalarOff = refOff + nRef;

		final ConstantObject staticInitialValue[] = 
			new ConstantObject[nStaticWords];

		for ( int cno = 0; cno < nclass; cno++ ){
			EVMClass c = (EVMClass)(classes[cno]);
			FieldInfo f[] = c.statics;
			if ((f == null) || (f.length == 0 )) { 
				continue; // this class has none
			}
			int nFields = f.length;
			for ( int i = 0; i < nFields; i++ ){
				FieldInfo fld = f[i];
				char toptype =  fld.type.string.charAt(0);
				if ( (toptype == 'L') || (toptype=='[')){
					fld.instanceOffset = refOff;
					staticInitialValue[refOff] = fld.value;
					refOff += 1;
				} else {
					fld.instanceOffset = scalarOff;
					staticInitialValue[scalarOff] = fld.value;
					scalarOff += fld.nSlots;
				}
			}
		}

		ConstantObject zero = new SingleValueConstant(0);
		staticInitialValue[0] = new SingleValueConstant(nRef);
		ArrayPrinter ap = 
			new ArrayPrinter() { 
				DoubleValueConstant previous;
				public void print(int index) { 
					ConstantObject value = staticInitialValue[index];
					if (value != null) { 
						out.print("ROM_STATIC_");
						if (value.nSlots == 1) { 
									writeConstant(value, false);
								} else { 
									DoubleValueConstant dval = 
										(DoubleValueConstant) value;
									String name = (value.tag == Const.CONSTANT_LONG)
											   ? "LONG" : "DOUBLE";
									out.print(name + "(");
									writeIntegerValue(dval.highVal);
									previous = dval;
								}
							} else if (previous != null) { 
								writeIntegerValue(previous.lowVal);
								out.print(")");
								previous = null;
							} else { 
								writeIntegerValue(0);
							}
						}
					};

				out.println("long "+staticStoreName+"["+nStaticWords+"];");
				out.println("struct {");
				out.println("\tlong count;");
				out.println("\tINSTANCE roots[" + nRef + "];");
				out.println("\tlong nonRoots[" + (nStaticWords - nRef - 1) + "];");
				out.println("} " + masterStaticStoreName + "= {");
				out.println("\t" + nRef + ",");
				out.println("\t{");
				writeArray(1, nRef + 1, 8, "\t\t", ap);
				out.println("\t},");
				out.println("\t{");
				writeArray(nRef + 1, nStaticWords, 4, "\t\t", ap);
				out.println("\t}");
				out.println("};\n");
			}


			protected void buildVT(EVMClass c) {

				int i, j;

				ClVT supVt = null;
				if (c.ci.superClass == null) {
					;	// now we'r Object
				} else {
					ClassInfo sci = ClassInfo.lookupClass(c.ci.superClass.name.string);
					String superName = ((EVMClass)(sci.vmClass)).getNativeName();
					if (mapClVT.get(superName)==null) {	
						buildVT((EVMClass) (sci.vmClass));	// first build super VT
					}
					supVt = (ClVT) mapClVT.get(superName);
				}

				String nativeName = c.getNativeName();
				if (mapClVT.get(nativeName)!=null) {	
					return;									// allready done!
				}

				int intfCount =  c.ci.interfaces.length;


				ClVT clvt = new ClVT();
				mapClVT.put(nativeName, clvt);

				EVMMethodInfo m[] = c.methods;
				int methodCount =  m.length;

				int maxLen = methodCount;
				if (supVt!=null) maxLen += supVt.len;
				clvt.len = 0;
				clvt.key = new int[maxLen];
				clvt.ptr = new int[maxLen];
				clvt.nativeName = new String[maxLen];
				clvt.meth = new EVMMethodInfo[maxLen];

				if (supVt!=null) {
					for (i=0; i<supVt.len; ++i) {
						clvt.key[i] = supVt.key[i];
						clvt.nativeName[i] = supVt.nativeName[i];
						clvt.meth[i] = supVt.meth[i];
					}
					clvt.len = supVt.len;
				}
//out.println("// VT: "+nativeName);

				for (i = 0; i < methodCount; i++) { 
					EVMMethodInfo meth = m[i];  
					MethodInfo mi = meth.method;
    				JOPClassTable.NameAndTypeKey ntk = classTable.getNameAndTypeKey(mi);
//out.println("//          "+prettyName(mi)+" "+ntk.nameKey+" "+ntk.typeKey);
					int key = (ntk.nameKey<<16) + ntk.typeKey;
					String methodNativeName = meth.getNativeName();
					for (j=0; j<clvt.len; ++j) {
						if (clvt.key[j] == key) {					// override method
							clvt.nativeName[j] = methodNativeName;
							clvt.meth[j] = meth;
							break;
						}
					}
					if (j==clvt.len) {								// new method
						clvt.key[clvt.len] = key;
						clvt.nativeName[clvt.len] = methodNativeName;
						clvt.meth[clvt.len] = meth;
						++clvt.len;
					}

				}
//for (i=0; i<clvt.len; i++) { 
//	out.println("//      "+clvt.nativeName[i]+" "+clvt.key[i]);
//}

			}
		//
		//	build VT (virtual method table) for all classes
		//
			protected void buildAllVT() {

				for (Enumeration e = classTable.enumerate(); e.hasMoreElements(); ) { 
					String name = ((JOPClassName)e.nextElement()).toString();
					ClassInfo ci = ClassInfo.lookupClass(name);
					if (ci != null) { 
						EVMClass cc = (EVMClass)ci.vmClass;
						if (!cc.isPrimitiveClass() && !cc.isArrayClass()) { 
							buildVT(cc);
						}
					}
				}
			}

			protected void buildInterfaceTable(Vector instanceClasses) {

				for (Enumeration e = instanceClasses.elements(); e.hasMoreElements();) {
					EVMClass cc = (EVMClass)e.nextElement();
					String nativeName = (String)e.nextElement();

					//
					//	build global interface table
					//
					if ((cc.ci.access & Const.ACC_INTERFACE) != 0) { 

						EVMMethodInfo m[] = cc.methods;
						int methodCount =  m.length;

						for (int i = 0; i < methodCount; i++) { 
							EVMMethodInfo meth = m[i];  
							MethodInfo mi = meth.method;
    						JOPClassTable.NameAndTypeKey ntk = classTable.getNameAndTypeKey(mi);
							int key = (ntk.nameKey<<16) + ntk.typeKey;
							String methodNativeName = meth.getNativeName();

							IT it = new IT();
							it.nativeName = methodNativeName;
							it.nr = listIT.size();
							it.key = key;
							it.meth = meth;
							listIT.add(it);
						}

					}

				}
			}

			protected void writeAllClassDefinitions(ClassClass classes[]) 
				throws DataFormatException
			{
				Vector instanceClasses = new Vector();
				for (Enumeration e = classTable.enumerate(); e.hasMoreElements(); ) { 
					String name = ((JOPClassName)e.nextElement()).toString();
					ClassInfo ci = ClassInfo.lookupClass(name);
					if (ci != null) { 
						EVMClass cc = (EVMClass)ci.vmClass;
						if (!cc.isPrimitiveClass() && !cc.isArrayClass()) { 
							String nativeName = cc.getNativeName();
							instanceClasses.addElement(cc);
							instanceClasses.addElement(cc.getNativeName());
						}
					}
				}
				writeAllByteCodes(instanceClasses);


		// TODO		writeAllHandlers(instanceClasses);
		// TODO		writeAllInterfaceTableDefinitions(instanceClasses);
				buildInterfaceTable(instanceClasses);
				

				buildAllVT();

		//
		//	calculate address of class struct and find main in start class (from outputFileName)
		//
				String startClass = "";
/*
				int pos = outputFileName.indexOf('.');
				if (pos==-1) {
					startClass = outputFileName;
				} else {
					startClass = outputFileName.substring(0, pos);
				}
*/
		//
		//	Ed's way is more elegant
		//
				startClass = System.getProperty("jop.startclass", startClass);
				System.out.println("Start class is " + startClass);
				if (startClass.length()==0) {
					System.out.println("No start class given");
					System.exit(-1);
				}

				addrCnt += 4;		// pointer to main(), JVM, Util classes
				int addrStringTable = addrCnt;

				addrCnt += stringTable.getTableLength();

				int addrBoot = 0;	// pointer to boot code
				int addrJVM = 0;	// pointer to method table of class JVM
				int addrJVMHelp = 0;	// pointer to method table of class JVMHelp
				int addrMain = 0;	// pointer to main struct
				ClVT oclvt = (ClVT) mapClVT.get("java_lang_Object");

				for (Enumeration e = classTable.enumerate(); e.hasMoreElements(); ) {
					String name = ((JOPClassName)e.nextElement()).toString();
					ClassInfo ci = ClassInfo.lookupClass(name);
					if (ci == null) { 
		// TODO				writeRawClassDefinition(name);
					} else { 
						EVMClass cc = (EVMClass)ci.vmClass;
						if (cc.isPrimitiveClass()) { 
							/* ignore */
						} else if (cc.isArrayClass()) { 
		// TODO					writeArrayClassDefinition(cc);
						} else { 

							ClAddr cla = new ClAddr();
							mapClAddr.put(cc.getNativeName(), cla);
							cla.stat = addrCnt;			// first static fields
// count only static fields!!							addrCnt += cc.ci.fields.length-cc.instanceSize();
							FieldInfo ff[] = cc.ci.fields;
							int nfields = ff == null ? 0 : ff.length;

							for ( int i = 0; i < nfields; i++ ){
								FieldInfo f = ff[i];
								if (f.isStaticMember()) { 
									++addrCnt;
								}
							}
							cla.start = addrCnt;
							addrCnt += CLS_HEAD;		// header
							cla.mtab = addrCnt;
							// old meth tab: addrCnt += cc.methods.length*METH_STR;
							ClVT clvt = (ClVT) mapClVT.get(cc.getNativeName());
							addrCnt += clvt.len*METH_STR;
							addrCnt += 1;		// back reference from cp-1 to class struct
							cla.ctab = addrCnt;
							addrCnt += cc.ci.constants.length*CONST_STR;
							if (cc.ci.interfaces.length>0) {
								cla.itab = addrCnt;
								addrCnt += listIT.size();
							} else {
								cla.itab = 0;
							}

							// add method count of class Object !
							if (cc.getNativeName().equals("com_jopdesign_sys_JVM")) {
								addrJVM = cla.mtab+oclvt.len*METH_STR;
							}
							if (cc.getNativeName().equals("com_jopdesign_sys_JVMHelp")) {
								addrJVMHelp = cla.mtab+oclvt.len*METH_STR;
							}
		//
		//	find boot() method
		//
							if (cc.getNativeName().equals("com_jopdesign_sys_Startup")) {
								EVMMethodInfo m[] = cc.methods;
								for (int i = 0; i < m.length; i++) { 
									EVMMethodInfo meth = m[i];  
									MethodInfo mi = meth.method;
									if (mi.name.string.indexOf("boot")!=-1) {
										String methodNativeName = meth.getNativeName();
										for (int j=0; j<clvt.len; ++j) {
											if (clvt.nativeName[j].equals(methodNativeName)) {
												addrBoot = cla.mtab+j*METH_STR;
												break;
											}
										}
									}
								}
							}

							if (cc.getNativeName().equals("java_lang_String")) {
								addrString = cla.mtab;
							}
		//
		//	find pointer to main...
		//
							if (cc.ci.className.equals(startClass)) {
								EVMMethodInfo m[] = cc.methods;
								for (int i = 0; i < m.length; i++) { 
									EVMMethodInfo meth = m[i];  
									MethodInfo mi = meth.method;
									if (mi.name.string.indexOf("main")!=-1) {
										String methodNativeName = meth.getNativeName();
										for (int j=0; j<clvt.len; ++j) {
											if (clvt.nativeName[j].equals(methodNativeName)) {
												addrMain = cla.mtab+j*METH_STR;
												break;
											}
										}
									}
								}
							}
						}
					}
				}

				out.println("//");
				out.println("//\tspecial pointer:");
				out.println("//");
				out.println("\t\t "+addrBoot+",\t// pointer to boot code");
				out.println("\t\t "+addrJVM+",\t// pointer to first non Object method struct of class JVM");
				out.println("\t\t "+addrJVMHelp+",\t// pointer to first non Object method struct of of class JVMHelp");
				out.println("\t\t "+addrMain+",\t// pointer to main method struct");

				// Print out the string table
				njavastrings = stringTable.writeStrings(this, "InternStringTable", addrString, addrStringTable);

				//
				//	now write the class definitions
				//

				for (Enumeration e = classTable.enumerate(); e.hasMoreElements(); ) { 
					String name = ((JOPClassName)e.nextElement()).toString();
					ClassInfo ci = ClassInfo.lookupClass(name);
					if (ci == null) { 
		// TODO				writeRawClassDefinition(name);
					} else { 
						EVMClass cc = (EVMClass)ci.vmClass;
						if (cc.isPrimitiveClass()) { 
							/* ignore */
						} else if (cc.isArrayClass()) { 
		// TODO					writeArrayClassDefinition(cc);
						} else { 
							writeNormalClassDefinition(cc);
						}
					}
				}

				/* Write out the Class Table 
				out.println("\014");
				classTable.writeTable(out, "ClassTable");
				if (relocatableROM) { 
					out.println("void *ClassDefinitionSectionTrailer = &ClassDefinitionSectionTrailer;");
				}
				*/
			}

			protected void writeNormalClassDefinition(EVMClass c) {
				int methodCount = c.methods.length;
				int fieldCount =  c.ci.fields.length;
				int constantCount = c.ci.constants.length;
				int intfCount =  c.ci.interfaces.length;
				// This will be clear later.  The only important thing is that it
				// becomes zero after we've output the last thing.
				int extras = methodCount + fieldCount + constantCount + intfCount;
				int access = c.ci.access + ACC_ROM_CLASS;
				if (!c.hasStaticInitializer) { 
					/* This class doesn't need to be inited */
					access += ACC_ROM_NON_INIT_CLASS;
				}

				String nativeName = c.getNativeName();
				ClAddr cla = (ClAddr) mapClAddr.get(nativeName);

				out.println();
				out.println("//\t"+cla.stat+"\t"+nativeName+" static fields");
				out.println();
				writeFields(c);

				out.println("//");
				out.println("//\t" +cla.start+"\t"+ nativeName);
				out.println("//");

				// writeBasicClassInfo(c, "instanceClassStruct", "INSTANCE_INFO", access);
				out.println("\t\t" + c.instanceSize() + ",\t\t//\tinstance size ");

				out.print("\t\t\t\t// TODO: ");
				out.println(c.hasStaticInitializer ? "CLASS_VERIFIED" : "CLASS_READY");

				out.print("\t\t\t\t// TODO: ");
				if (c.ci.superClass == null) {
					out.print("\t\t0,");
				} else {
					ClassInfo sci = ClassInfo.lookupClass(c.ci.superClass.name.string);
					String superName = ((EVMClass)(sci.vmClass)).getNativeName();
					out.print("\t\t-1,\t// " + superName);
				}
				out.println("\t// super reference");

				out.println("\t\t" + cla.itab + ",\t//\tpointer to interface table");

				out.println();
				out.println("//\t"+nativeName+" method table");
				out.println();
				writeMethods(c);

				out.println();
				out.println("\t\t" + cla.start + ",\t//\tpointer back to class struct (cp-1)");
				out.println();
				out.println("//\t"+cla.ctab+"\t"+nativeName+" constants");
				out.println();
				writeConstantPool(c);

				int length = c.ci.interfaces.length;
				if (length > 0) { 
					out.println();
					out.println("//\t"+cla.itab+"\t"+nativeName+" interface table");
					out.println();
					writeInterfaces(c);
				}
			}


			protected void writeRawClassDefinition(String className){
				String nativeName = Util.convertToClassName(className);
				JOPClassName cn = new JOPClassName(className);
				writeBasicClassInfo(nativeName, cn, 
									"instanceClassStruct", "RAW_CLASS_INFO", 0);
				out.println("\t\tNULL)");
				out.println("};");
			}


			protected void writeArrayClassDefinition(EVMClass c){
				JOPClassName cn = new JOPClassName(c.ci.className);
				int access = c.ci.access + ACC_ARRAY_CLASS + ACC_ROM_CLASS; 
				ConstantObject cp = c.ci.constants[1];
				if (cp.tag == Const.CONSTANT_CLASS) {   
					ArrayClassInfo aci = (ArrayClassInfo)c.ci;
					ClassInfo elemClass = 
						ClassInfo.lookupClass(((ClassConstant)cp).name.string);
					String elemName = ((EVMClass)(elemClass.vmClass)).getNativeName();
					writeBasicClassInfo(c, "arrayClassStruct", 
										"ARRAY_OF_OBJECT", access);
					out.println("\t\t" + elemName + "_Classblock)};\n");
				} else {
					String baseName = ((ArrayClassInfo)(c.ci)).baseName.toUpperCase();
					writeBasicClassInfo(c, "arrayClassStruct", 
										"ARRAY_OF_PRIMITIVE", access);
					
					out.println("\t\t" + baseName + ")};\n");
				}
			}
			
			protected void writeBasicClassInfo(EVMClass c,
											 String type, String macro, int access) {
				writeBasicClassInfo(c.getNativeName(), new JOPClassName(c.ci.className),
									type, macro, access);
			}

			protected void writeBasicClassInfo(String nativeName, 
											 JOPClassName cn,
											 String type, String macro, int access) {
				String fullBaseName = cn.getFullBaseName();
				String packageName = cn.getPackageName();
				int packageKey = -1;
				if (packageName != null) { 
					packageKey = classTable.getNameKey(packageName);
					nameTable.declareUString(out, packageName);
				}
				int baseKey = classTable.getNameKey(fullBaseName);	  
				nameTable.declareUString(out, fullBaseName);

				int classKey = classTable.getClassKey(cn);
				out.println("static struct " + type + " "   + nativeName + "_Classblock = {");
				out.println("\t" + macro + "( \\");
				if (packageKey == -1) {
					out.println("\t\tNULL, ");
				} else { 
					out.println("\t\t" + nameTable.getUString(packageName) 
								+ ",  /* " + packageName + " */ \\");
				}
				out.println("\t\t" + nameTable.getUString(fullBaseName) 
							+ ",  /* " + fullBaseName + " */ \\");
				JOPClassName nextName = (JOPClassName)classTable.getNext(cn);
				if (nextName == null) { 
					out.print("\t\tNULL, ");
				} else {
					EVMClass next = nextName.getEVMClass();
					String nextNativeName = 
						(next != null) ? next.getNativeName()
									   : Util.convertToClassName(nextName.toString());
					out.print("\t\t&" + nextNativeName + "_Classblock, ");
				}
				out.printHexInt(classKey);
				out.print(", ");
				out.printHexInt(access);
				out.println(", \\");
			}


			protected void writeAllInterfaceTableDefinitions(Vector instanceClasses) {
				Vector todo = new Vector();
				out.println("static struct AllInterfaces_Struct { ");
				for (Enumeration e = instanceClasses.elements(); e.hasMoreElements();) {
					EVMClass cc = (EVMClass)e.nextElement();
					String nativeName = (String)e.nextElement();
					int length = cc.ci.interfaces.length;
					if (length > 0) { 
						todo.addElement(cc);
						out.println("\tstruct {");
						out.println("\t\tunsigned short length;");
						out.println("\t\tunsigned short index[" + length + "];");
						out.println("\t} " + nativeName + ";");
					}
				}
				out.println("} AllInterfaces = { ");
				for (Enumeration e = todo.elements(); e.hasMoreElements();) {
					EVMClass cc = (EVMClass)e.nextElement();
					out.println("\t{");
					out.println("\t\t/* " + cc.ci.className + " */");
					writeInterfaces(cc);
					out.println("\t},");
				}
				out.println("};\n");
			}
			

			protected void writeAllByteCodes(Vector instanceClasses) 
					  throws DataFormatException{ 
				/* Find the values of the two special methods */
				// final MethodInfo runCustomCodeMethod = runCustomCodeConstant.find();
				Vector todo = new Vector();
				Vector natives = new Vector();
				

				for (Enumeration e = instanceClasses.elements(); e.hasMoreElements();) {
					EVMClass cc = (EVMClass)e.nextElement();
					String classNativeName = (String)e.nextElement(); // ignored
					EVMMethodInfo  m[] = cc.methods;
					int nmethod = m.length;
					for (int j = 0; j < nmethod; j++){
						EVMMethodInfo meth = m[j];
						MethodInfo mi = meth.method;
						if ((mi.access & Const.ACC_ABSTRACT) != 0) { 
							/* Do nothing */
						} else if ((mi.access & Const.ACC_NATIVE) != 0) { 
							natives.addElement(meth);
						} else { 
							String methodNativeName = meth.getNativeName();
							mapCodeStart.put(methodNativeName, new Integer(addrCnt));
							addrCnt += (mi.code.length+3)/4;
							todo.addElement(meth);
							todo.addElement(methodNativeName);
						}
					}
				}
				ncodebytes = 0;
				out.println("//");
				out.println("//	Code: as 32 bit word, first byte is highest byte");
				out.println("//\tmeans that 32 bit word is seen as high byte first, but code linear");
				out.println("//");
				out.println("\t\t"+addrCnt+",\t//\tlength of code in 32 bit words = address of 'special' pointer");
				out.println();

				for (Enumeration e = todo.elements(); e.hasMoreElements(); ) { 
					Object nextElement = e.nextElement();
					EVMMethodInfo meth = (EVMMethodInfo)nextElement;
					String methodNativeName = (String)e.nextElement();
					final MethodInfo mi = meth.method;
					ncodebytes += mi.code.length;
					out.println("\t// " + mi.parent.className + ": " + prettyName(mi));

		/*
					if (index == 0 && (mi == runCustomCodeMethod)) {
										  out.print("CUSTOMCODE");
		*/
		//
		//	check if byte code is implemeted in JOP
		//	and substitute JopSys functions with special byte code!
		//
		if (!mi.parent.className.equals("java/lang/Object")) {
					for (int pc=0; pc<mi.code.length; ) {

						int bc = mi.code[pc] & 0xff;

						if (bc == 0xb8) {			// invoke static
							int idx = ((mi.code[pc+1]&0xff)<<8) + (mi.code[pc+2]&0xff);

							ConstantObject value = mi.parent.constants[idx];

							MethodInfo clmi = ((MethodConstant)value).find();
							if ((clmi.access & Const.ACC_NATIVE) != 0) {
		/*
								System.out.println("NATIVE "+clmi.parent.className+" "+clmi.index);
		*/

								mi.code[pc] = (byte) (SYS_BC_START+clmi.index);		// change to SPECIAL byte code
								mi.code[pc+1] = 0x00; 	// nop
								mi.code[pc+2] = 0x00; 	// nop
							}


						}

		//				if (!com.jopdesign.tools.JopInstr.imp(bc)) {
		//					System.out.println(mi.parent.className);
		//					System.out.println(com.jopdesign.tools.JopInstr.name(bc)+" not implemented");
		//					System.exit(-1);
		//				}
						if (bc == 0xaa) {	// tableswitch
							pc &= 0xfffffffc;	// adjust pc for fill bytes
							pc += 8;			// point to low
							int low = mi.code[pc] << 24;
							low += mi.code[pc+1] << 16;
							low += mi.code[pc+2] << 8;
							low += mi.code[pc+3];
							pc += 4;
							int high = mi.code[pc] << 24;
							high += mi.code[pc+1] << 16;
							high += mi.code[pc+2] << 8;
							high += mi.code[pc+3];
							pc += 4 + (high-low+1)*4;
						} else if (bc == 0xab) {
							pc &= 0xfffffffc;	// adjust pc for fill bytes
							pc += 8;			// point to npairs
							int npairs = mi.code[pc] << 24;
							npairs += mi.code[pc+1] << 16;
							npairs += mi.code[pc+2] << 8;
							npairs += mi.code[pc+3];
							pc += 4;
							pc += 4 + (npairs)*4*2;
						} else {
							pc += com.jopdesign.tools.JopInstr.len(bc);
						}
					}
		}

					for (int i=0; i<mi.code.length; i+=4) {
						int val = 0;
						for (int j=0; j<4; ++j) {
							if (i+j < mi.code.length) {
								val += (mi.code[i+j] & 0x0ff)<<((3-j)*8);
							}
						}
						out.print("\t\t"+val+",\t//\t");
						for (int j=0; j<4; ++j) {
							if (i+j < mi.code.length) {
								out.print((mi.code[i+j] & 0x0ff) + " ");
							}
						}
						out.println();
					}
				}

		/*
				if (!relocatableROM) { 
					for (Enumeration e = natives.elements(); e.hasMoreElements(); ) {
						EVMMethodInfo meth = (EVMMethodInfo)e.nextElement();
						String jniName = meth.method.getNativeName(true);
						out.println("extern void " + jniName + "(void);");
					}
				}
		*/
			}


			protected void writeAllHandlers(Vector instanceClasses) { 
				Vector todo = new Vector();
				out.println("static struct AllHandlers_Struct { ");
				ExceptionEntry empty[] = new ExceptionEntry[0];
				for (Enumeration e = instanceClasses.elements(); e.hasMoreElements();) {
					EVMClass cc = (EVMClass)e.nextElement();
					String classNativeName = (String)e.nextElement(); // ignored
					EVMMethodInfo  m[] = cc.methods;
					int nmethod = m.length;
					for (int j = 0; j < nmethod; j++){
						EVMMethodInfo meth = m[j];
						MethodInfo mi = meth.method;
						if (mi.exceptionTable == null) { 
							mi.exceptionTable = empty;
						}
						int tryCatches = mi.exceptionTable.length;
						if (tryCatches > 0) { 
							String methodNativeName = meth.getNativeName();
							todo.addElement(meth);
							ncatchframes += tryCatches;
							out.println("\tstruct { /* " 
										+ mi.parent.className + ": " 
										+ prettyName(mi) + "*/");
							out.println("\t\tlong length;");
							out.println("\t\tstruct exceptionHandlerStruct handlers[" + 
											 tryCatches + "];");
							out.println("\t} " + methodNativeName + ";");

						} 
					}
				}
				out.println("} AllHandlers = {");
				for (Enumeration e = todo.elements(); e.hasMoreElements(); ) { 
					EVMMethodInfo meth = (EVMMethodInfo)e.nextElement();
					MethodInfo mi = meth.method;
					ExceptionEntry[] exceptions = mi.exceptionTable;
					out.println("\t{ /* " + mi.parent.className + ": " 
								 + prettyName(mi) + "*/");
					out.println("\t\t" + exceptions.length + ",");
					out.println("\t\t{");
					int tryCatches = exceptions.length;
					for (int k = 0; k < tryCatches; k++) { 
						ExceptionEntry ee = mi.exceptionTable[k];
						out.println("\t\t\tHANDLER_ENTRY(" + ee.startPC + ", " + 
									ee.endPC + ", " + ee.handlerPC + ", " + 
									(ee.catchType == null ? 0 : ee.catchType.index)
									+ ")" + ((k == tryCatches - 1) ? "" : ","));
					}
					out.println("\t\t}");
					out.println("\t},\n");
				}
				out.println("};");
			}

			protected void writeMethods(EVMClass c) {

// not used
				EVMMethodInfo m[] = c.methods;
				int methodCount =  m.length;
// for new tab

				ClVT clvt = (ClVT) mapClVT.get(c.getNativeName());

				for (int i = 0; i < clvt.len; i++) { 
					EVMMethodInfo meth = clvt.meth[i];  
					MethodInfo mi = meth.method;
					String type = ((mi.access & Const.ACC_ABSTRACT) != 0) ? "ABSTRACT"
								: ((mi.access & Const.ACC_NATIVE) != 0)   ? "NATIVE"
								: "";
					out.println("\t// " + type + " " + prettyName(mi));

					ClassInfo ci = mi.parent;
					EVMClass rc = (EVMClass)(ci.vmClass);	// get the 'real' class

					out.println("\t\t// " + rc.getNativeName());
					ClAddr cla = (ClAddr) mapClAddr.get(rc.getNativeName());

		/* TODO ???
					if ((mi.access & Const.ACC_ABSTRACT) != 0) { 
						// do nothing
					} else if ((mi.access & Const.ACC_NATIVE)  != 0) { 
						out.println("\t\t" + mi.getNativeName(true) + " native ???");
					} else { 
						String methodNativeName = meth.getNativeName();
						out.println("\t\t\t\t\tAllCode." 
									+ methodNativeName + "_CodeSection."
									+ methodNativeName + ", \\");
						if (mi.exceptionTable.length > 0) { 
							out.println("\t\t\t\t\t&AllHandlers." + methodNativeName
										+ ", \\");
						} else { 
							out.println("\t\t\t\t\t0, \\");
						}
					}
		*/

					out.print("\t\t\t// ");
					out.printHexInt(mi.access);
					out.println("\tTODO access");
					out.println("\t\t\t// " + mi.stack + "\tTODO ? stack");

					if ((mi.access & (Const.ACC_ABSTRACT + Const.ACC_NATIVE)) != 0) { 
						out.println("\t\t\t//\tlocals: "+mi.locals+" args size: "+mi.argsSize);
						out.println("\t\t"+(-mi.index)+",");
						out.println("\t\t0,");
					} else {
						String methodNativeName = meth.getNativeName();
						Integer addr = (Integer) mapCodeStart.get(methodNativeName);
						int len = (mi.code.length+3)/4;
						out.println("\t\t\t//\tcode start:" + addr);
						out.println("\t\t\t//\tcode length:" + len);
						out.println("\t\t\t//\tcp:" + cla.ctab);
						out.println("\t\t\t//\tlocals: "+mi.locals+" args size: "+mi.argsSize);

						int realLocals = mi.locals-mi.argsSize;
						if (len>=512/4 || realLocals>31 || mi.argsSize>31) {
							System.out.println("wrong size: "+c.getNativeName()+" "+prettyName(mi));
							System.exit(-1);
						}
						out.println("\t\t"+((addr.intValue()<<10) | len)+",");
						out.println("\t\t"+((cla.ctab<<10) | (realLocals<<5) | mi.argsSize)+",");
					}
		//			out.print(classTable.getNameAndTypeKey(mi));

				}
				nmethods += methodCount;
			}

			void writeFields(EVMClass c) {
				// Some day, I'll have to deal with > 255 fields.
				// Today is not that day. Just do the simple case.
				FieldInfo ff[] = c.ci.fields;
				int nfields = ff == null ? 0 : ff.length;

				for ( int i = 0; i < nfields; i++ ){
					FieldInfo f = ff[i];
					if (f.isStaticMember()) { 
						out.print("\t\t0,\t//\tstatic ");
					} else { 
						out.print("\t\t\t//\t");
					}
					out.println(prettyName(f)+"\tTODO: detailed info...");

		/*	TODO: detailed info:
					out.print("\t\t\t\t" + c.getNativeName() + ", ");
					// Note that access already includes ACC_DOUBLE, ACC_POINTER
					out.printHexInt(f.access);
					out.print(", " + f.instanceOffset + ", " 
							  +  classTable.getNameAndTypeKey(f) + ")");
					out.println(  (i == nfields - 1) ? "" : ","  );
		*/
				}
				this.nfields += nfields;
			}

			protected void writeConstantPool(EVMClass c) { 
				final ConstantObject cpool[] = c.ci.constants;
				int length = cpool.length;
				final int[] tags = new int[length];

				out.println("\t\t" + length + ",\t//\tconst pool length");
				out.println();

				for (int index = 1; index < length; ) { 
					ConstantObject cp = cpool[index];
					int tag = cp.tag;
					switch (tag) { 
						case Const.CONSTANT_INTEGER: case Const.CONSTANT_STRING:
						case Const.CONSTANT_LONG:   case Const.CONSTANT_DOUBLE:
							tags[index] = tag;
							break;
						default:
							tags[index] = tag | (cp.isResolved() ? 0x80:0);
							break;
					}
					out.print("\t\t");
					writeConstant(cpool[index], true);
					index += cp.nSlots;
				}
		/* TODO tags ???
				writeArray(length, 12, "\t\t\t", 
						   new ArrayPrinter() { 
							   public void print(int index) { out.print(tags[index]); }
						   });
		*/
				nconstants += length;
			}

			protected void writeInterfaces(EVMClass c) {

				ClVT clvt = (ClVT) mapClVT.get(c.getNativeName());
				ClAddr cla = (ClAddr) mapClAddr.get(c.getNativeName());

				for (int i=0; i<listIT.size(); ++i) {
					IT it = (IT) listIT.get(i);
					int j;
					for (j = 0; j < clvt.len; j++) { 
						if (clvt.key[j]==it.key) {
							break;
						}
					}
					if (j!=clvt.len) {
						out.print("\t\t"+(cla.mtab+j*METH_STR)+",");
							EVMMethodInfo meth = clvt.meth[j];  
							MethodInfo mi = meth.method;
					} else {
						out.print("\t\t"+0+",\t");
					}
					out.println("\t// "+it.meth);
				}

			}


			protected void
			writeIntegerValue( int v ){
				 // little things make gcc happy.
		/*
				 if (v==0x80000000)
					out.print( "(long)0x80000000" );
				else
		*/
					 out.print(v+",");
			}

			protected void
			writeLongValue( String tag, int highval, int lowval ){
				writeIntegerValue( highval );
				out.println();
				out.print("\t\t");
				writeIntegerValue( lowval );
			}


			protected void
			writeConstant(ConstantObject value, boolean verbose){
				switch ( value.tag ){
				case Const.CONSTANT_INTEGER:
					writeIntegerValue(((SingleValueConstant)value).value);
					out.println("\t//\tINT");
					break;
				case Const.CONSTANT_FLOAT:
					writeIntegerValue(((SingleValueConstant)value).value);
					out.println("\t//\tFLOAT");
					break;

				case Const.CONSTANT_UTF8:
		/* disable for now
					 System.out.println("Cannot write UTF entry: \"" + 
										((UnicodeConstant)value).string + "\"");
		*/
					 out.println("-1,\t//\tUNKNOWN");
					 formatError = true;
					 break;

				case Const.CONSTANT_STRING:
					out.print(stringTable.getStringAddress((StringConstant) value)+",");
					out.print("\t//\tSTRING: ");
					if (verbose) { 
						String s = ((StringConstant)value).str.string;
						if (s.length() > 20) { 
							s = s.substring(0, 20 - 3) + "...";
						} 
						out.printSafeString(s);
					}
					out.println();
					break;

				case Const.CONSTANT_LONG: {
					DoubleValueConstant dval = (DoubleValueConstant) value;
					writeLongValue( "LONG",  dval.highVal, dval.lowVal );
					out.println("\t//\tLONG");
					break;
				}

				case Const.CONSTANT_DOUBLE: {
					DoubleValueConstant dval = (DoubleValueConstant) value;
					writeLongValue( "DOUBLE",  dval.highVal, dval.lowVal );
					out.println("\t//\tDOUBLE");
					break;
				}

				case Const.CONSTANT_CLASS:
					if (value.isResolved()) {
						ClassInfo ci = ((ClassConstant)value).find();
						EVMClass c = (EVMClass)(ci.vmClass);

						String nativeName = c.getNativeName();
						ClAddr cla = (ClAddr) mapClAddr.get(nativeName);
						out.println(cla.start+",\t//\tCLASS: " + nativeName);
					} else {
		/* disable for now
		*/
						System.out.println("Unresolved class constant: "+value.toString() );
						formatError = true;
						out.println("-1,\t//\tUNKNOWN");
					}
					break;

				case Const.CONSTANT_METHOD:
					if ( value.isResolved() ){
						MethodInfo mi = ((MethodConstant)value).find();
						ClassInfo  ci = mi.parent;
						EVMClass   EVMci = (EVMClass)(ci.vmClass);
						ClAddr cla = (ClAddr) mapClAddr.get(EVMci.getNativeName());

						ClVT clvt = (ClVT) mapClVT.get(EVMci.getNativeName());

						// old mtab out.print(cla.mtab+mi.index*METH_STR);

						for (int i = 0; i < clvt.len; i++) { 
							EVMMethodInfo meth = clvt.meth[i];  
							MethodInfo m = meth.method;
							if (m == mi) {
								if (mi.isStaticMember() ||
									// <init> and privat methods are called with invokespecial
									// which mapps in jvm.asm to invokestatic
									prettyName(mi).charAt(0)=='<' || (mi.access & Const.ACC_PRIVATE)!=0) {

									out.print((cla.mtab+i*METH_STR)+",");
out.print("\t//\tstatic, special or private");
								} else {
									out.print(((i*2<<8)+mi.argsSize-1)+",");
out.print("\t//\tvirtual index: "+i+" args: "+mi.argsSize);
								}
								break;
							}
						}

						String type = ((mi.access & Const.ACC_ABSTRACT) != 0) ? "ABSTRACT"
								: ((mi.access & Const.ACC_NATIVE) != 0)   ? "NATIVE"
								: "";
						out.println("\t//\t"+EVMci.getNativeName()+": "+
							(mi.isStaticMember() ? " static " : "")+
							type+" "+prettyName(mi));

					} else {
						formatError = true;
						out.println("-1,\t//\tUNKNOWN");
/* disable for now
*/
						System.out.println("Unresolved method constant: "+value.toString() );
					}
					break;

				case Const.CONSTANT_INTERFACEMETHOD:
					if ( value.isResolved() ){
						MethodInfo mi = ((MethodConstant)value).find();

						ClassInfo  ci = mi.parent;
						EVMClass   EVMci = (EVMClass)(ci.vmClass);
						ClAddr cla = (ClAddr) mapClAddr.get(EVMci.getNativeName());

						ClVT clvt = (ClVT) mapClVT.get(EVMci.getNativeName());

						// old mtab out.print(cla.mtab+mi.index*METH_STR);

						for (int i = 0; i < listIT.size(); i++) { 
							IT it = (IT) listIT.get(i);
							EVMMethodInfo meth = it.meth;
							MethodInfo m = meth.method;
							if (m == mi) {
								if (mi.isStaticMember() ||
									// <init> and privat methods are called with invokespecial
									// which mapps in jvm.asm to invokestatic
									prettyName(mi).charAt(0)=='<' || (mi.access & Const.ACC_PRIVATE)!=0) {

									out.print((cla.mtab+i*METH_STR)+",");	// wrong!!!
out.print("\t//\tstatic, special or private Interface ????");
								} else {
									out.print(((i<<8)+mi.argsSize-1)+",");
									out.print("\t\t//\tinterface index: "+i+" args: "+mi.argsSize);
								}
								break;
							}
						}

						String type = ((mi.access & Const.ACC_ABSTRACT) != 0) ? "ABSTRACT"
								: ((mi.access & Const.ACC_NATIVE) != 0)   ? "NATIVE"
								: "";
						out.println("\t//\t"+EVMci.getNativeName()+": "+
							(mi.isStaticMember() ? " static " : "")+
							type+" "+prettyName(mi));

					} else {
						formatError = true;
						out.println("-1,\t//\tUNKNOWN");
/* disable for now
*/
						System.out.println("Unresolved interface constant: "+value.toString() );
					}
					break;

				case Const.CONSTANT_FIELD:

					if ( value.isResolved() ){
						FieldInfo fi = ((FieldConstant)value).find();
						ClassInfo ci = fi.parent;
						EVMClass c = (EVMClass)(ci.vmClass);

						String nativeName = c.getNativeName();

						int realPos = 0;
						int fieldCnt = 0;

						FieldInfo ff[] = ci.fields;
						int nfields = ff == null ? 0 : ff.length;
						for (int i = 0; i < nfields; i++ ){
							FieldInfo f = ff[i];
							if (fi==f) {
								break;
							}
							if (fi.isStaticMember() && f.isStaticMember()) { 
								++realPos;
							} else if (!fi.isStaticMember() && !f.isStaticMember()) { 
								++realPos;
							}
						}

						for (int i = 0; i < nfields; i++ ) {
							FieldInfo f = ff[i];
							if (!f.isStaticMember()) { 
								++fieldCnt;			// count only non static fields
							}
						}

						if (fi.isStaticMember()) {
							ClAddr cla = (ClAddr) mapClAddr.get(nativeName);
							realPos += cla.stat;
						} else {
							realPos += c.instanceSize() - fieldCnt;
						}
						out.print(realPos+",");
						out.print("\t//\t"+nativeName+": ");
						if (fi.isStaticMember()) {
							out.print("static ");
						}
						out.println(prettyName(fi));
					} else {
						formatError = true;
						out.println("-1,\t//\tUNKNOWN");
/* disable for now
*/
						System.out.print("Unresolved field constant: "+value.toString() );
					}
					break;

				default:
					formatError = true;
					out.println("-1,\t//\tUNKNOWN");
/* disable for now
					System.out.print("ERROR: constant " + value.tag);
*/
				}
	}

	/*
	 * These are macros to encapsulate some of the messiness
	 * of data definitions. They are, perhaps, compiler-specific.
	 */
	protected static String stdHeader[] = {
		"#define COMPILING_ROMJAVA 1",
		"",
		"#include <global.h>",
		"#include \"rom.h\"",
		"",
		"#if ROMIZING",
		""
	};

	protected void writeProlog(){
		java.util.Date date = new java.util.Date();
		out.println("/* This is a generated file.  Do not modify.");
		out.println(" * Generated on " + date);
		out.println(" */\n");
		out.println();
		out.println("#define ROM_GENERATION_DATE \"" + date + "\"");
		out.println();
		for ( int i = 0; i < stdHeader.length; i++ ){
			out.println( stdHeader[i] );
		}
	}
	
	public void 
	writeArray(int start, int length, 
			   int columns, String indent, ArrayPrinter ap) { 
		CCodeWriter out = this.out;
		int column = 0;
		out.print(indent);  
		for ( int i = start; ; i++ ){
			if (column >= columns) { 
				out.print("\n" + indent);
				column = 1;
			} else {
				column += 1;
			}
			ap.print(i);
			if (i < (length - 1)) { 
				out.print(", ");
			} else { 
				out.println(ap.finalComma() ? ", " : "");
				break;
			}
		}
	}

	public void 
	writeArray(int length, int columns, String indent, ArrayPrinter ap) { 
		writeArray(0, length, columns, indent, ap);
	}
	
	public String prettyName(MethodInfo mi) { 
		String name = mi.name.string;
		String type = mi.type.string;
		int lastParen = type.lastIndexOf(')');
		StringBuffer result = new StringBuffer();
		
		/* First, put in the result type */
		if (!name.equals("<init>") && !name.equals("<clinit>")) { 
			typeName(type, lastParen + 1, result);
			result.append(' ');
		}
		result.append(name).append('(');
		for (int index = 1; index < lastParen; ) { 
			if (index > 1) { 
				result.append(", ");
			}
			index = typeName(type, index, result);
		}
		return result.append(')').toString();
	}

	public String prettyName(FieldInfo fi) { 
		String name = fi.name.string;
		String type = fi.type.string;

		StringBuffer result = new StringBuffer();
		typeName(type, 0, result);

		return result.append(' ').append(name).toString();
	}


	private int
	typeName(String type, int index, StringBuffer result) {
		int end;
		switch(type.charAt(index)) { 
			case 'V':  result.append("void");	return index + 1;
			case 'Z':  result.append("boolean"); return index + 1;
			case 'B':  result.append("byte");	return index + 1;
			case 'S':  result.append("short");   return index + 1;
  			case 'C':  result.append("char");	return index + 1;
			case 'I':  result.append("int");	 return index + 1;
			case 'J':  result.append("long");	return index + 1;
			case 'F':  result.append("float");   return index + 1;
			case 'D':  result.append("double");  return index + 1; 

			case 'L': { 
				end = type.indexOf(';', index) + 1;
				String className = type.substring(index + 1, end - 1);
				if (className.startsWith("java/lang/")) { 
					className = className.substring(10);
				}
				result.append(className.replace('/', '.'));
				return end;
			}

			case '[': { 
				end = typeName(type, index + 1, result);
				result.append("[]");
				return end;
			}
			default:
				System.err.println("Unrecognized character");
				result.append("XXXX");
				return index + 1;
		}
	}

	public void printSpaceStats( java.io.PrintStream o ){
		ClassClass classes[] = ClassClass.getClassVector( classMaker );
		o.println(Localizer.getString("cwriter.total_classes", Integer.toString(classes.length)));
		o.println(Localizer.getString("cwriter.method_blocks", Integer.toString(nmethods)));
		o.println(Localizer.getString("cwriter.bytes_java_code", Integer.toString(ncodebytes)));
		o.println(Localizer.getString("cwriter.catch_frames", Integer.toString(ncatchframes)));
		o.println(Localizer.getString("cwriter.field_blocks", Integer.toString(nfields)));
		o.println(Localizer.getString("cwriter.constant_pool_entries", Integer.toString(nconstants)));
		o.println(Localizer.getString("cwriter.java_strings",Integer.toString(njavastrings)));
		o.println(addrCnt + " words of memory");
	}
	
	abstract public static class ArrayPrinter { 
		abstract void print(int index);
		boolean finalComma() { return false; }
	}

	public static class ArrayEqual { 
		Object array;
		ArrayEqual(byte[] x) { this.array = x; }

		public int hashCode() { 
			byte[] a = (byte[])array;
			int total = 0;
			for (int i = 0; i < a.length; i++) { 
				total = (total * 3) + a[i];
			}
			return total;
		}

		public boolean equals(Object y) { 
			if (this.getClass() != y.getClass()) { 
				return false;
			}
			byte[] a = (byte[])array;
			byte[] b = (byte[])((ArrayEqual)y).array;
			if (a.length != b.length) { 
				return false;
			}
			for (int i = 0; i < a.length; i++) { 
				if (a[i] != b[i]) { 
					return false;
				}
			}
			return true;
		}
	}


	// Here so that PALMWriter can override it.
	protected void writeRelocationFile(ClassClass classes[]) { 
	}

}
