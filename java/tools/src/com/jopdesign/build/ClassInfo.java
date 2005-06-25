/*
 * Created on 05.06.2005
 *
 */
package com.jopdesign.build;

import java.io.PrintWriter;
import java.util.*;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;


/**
 * @author Flavius, Martin
 *
 * Class struct:
 * 
 * -n: class variables
 *  0: instance size (class reference)
 *  1: GC info field (one bit per field)
 *  2: pointer to interface table
 * 3+: method table, two words per entry
 *   : class reference (pointer back to class info)
 *   : constant pool (cp)
 *   : optional interface table
 * 
 * Flavius type class struct:
 * 
 * A Class struct image should look as follows:
 * (the byte code for used Methods is output and at known locations)
 * 
 * a. GCInfo: USE length + PACKED 2w into 1w ? (not big anyway)
 * b. static fields
 * c. -2. addrOf(staticfields)
 * c. -1. addrOf(GCInfo)
 * c.  0. instance size (class struct ptr)
 * c.  1. addrOf(interfaces)
 * d. method table, 2w per entry
 *	- uses f or other classes' f
 * e. addrOf(class struct)
 * f. constant pool
 * 	  - length
 *	- uses entries in b. and d.
 */

public class ClassInfo {

	static final int CLS_HEAD = 3;
	static final int METH_STR = 2;
	static final int CONST_STR = 1;

	static class IT {
		int nr;
		String key;
//		String nativeName;
		MethodInfo meth;
	}
	
	// 'global' interface table
	static LinkedList listIT = new LinkedList();
	
	static int nrObjMethods;
	static int bootAddress;
	static int jvmAddress;
	static int jvmHelpAddress;
	static int mainAddress;
	
	// 'global' mapping of class names to ClassInfo 
	static HashMap mapClassNames = new HashMap();


	// virtual method table
	class ClVT {
		int len;
		// Method name plus signature is the key
		String[] key;
// do I need this?
//		int[] ptr;
// do I need this?
// This was a app.-wide unique id.
//		String[] nativeName;
		MethodInfo mi[];
	}
	
	/**
	 * Field table
	 * @author Martin
	 *
	 */
	class ClFT {
		int len;
		// fieldname and signature
		String[] key;
		// index in the object
		int[] idx;
		int[] size;
		boolean[] isStatic;
		boolean[] isReference;
	}

	static int cntValueStatic = 0;
	static int cntRefStatic = 0;
	static int addrValueStatic = 0;
	static int addrRefStatic = 0;
	
	
	public JavaClass clazz;
	public ClassInfo superClass;

	private HashMap usedMethods = new HashMap();
	// Methods in a list
	private List list = new LinkedList();

	public ClVT clvt;
	
	public ClFT clft;
	private int instSize;
	private int instGCinfo;

	public List cpoolUsed;
	public int cpoolArry[];
	public String cpoolComments[];
	
	public int staticValueVarAddress;
	public int staticRefVarAddress;
	public int classRefAddress;
	public int methodsAddress;
	public int cpoolAddress;
	public int iftableAddress;


	public ClassInfo(JavaClass clazz) {
		this.clazz = clazz;
		methodsAddress = 0;
		cpoolAddress = 0;
		instSize = 0;
		instGCinfo = 0;
		cpoolUsed = new LinkedList();

		mapClassNames.put(clazz.getClassName(), this);
		
		if (clazz.getClassName().equals(JOPizer.stringClass)) {
			StringInfo.cli = this;
		}
		if (clazz.getClassName().equals(JOPizer.objectClass)) {
			nrObjMethods = clazz.getMethods().length;
		}

	}

	public boolean isMethodPresent(String amth) {
		return usedMethods.containsKey(amth);
	}
	public void addMethodOnce(String mid) {
		if(!isMethodPresent(mid)) {
			MethodInfo mi = new MethodInfo(this, mid);
			usedMethods.put(mid, mi);
			list.add(mi);
			//System.err.println(className+" has a new method: "+amth);
		}
	}
	public MethodInfo getMethodInfo(String amth) {
		return (MethodInfo)usedMethods.get(amth);
	}
	
	public MethodInfo getVTMethodInfo(String mid) {
		for (int i=0; i<clvt.len; ++i) {
			if (clvt.key[i].equals(mid)) {
				return clvt.mi[i];
			}
		}
		return null;
	}
	
	public List getMethods() {
		return list;
	}
	
	public ClVT getClVT() {
		
		if (clvt==null) {
			clvt = new ClVT();
		}
		return clvt;
	}
	public ClFT getClFT() {
		
		if (clft==null) {
			clft = new ClFT();
		}
		return clft;
	}
	
	public void setInstanceSize(int size) {
		instSize = size;
	}
	/**
	 * Get an IT object.
	 * @return
	 */
	public static IT getITObject() {
		return new IT();
	}
	
	void cntStaticFields() {
		
		int i;
		for (i=0; i<clft.len; ++i) {
			if (clft.isStatic[i]) {
				if (clft.isReference[i]) {
					cntRefStatic += clft.size[i];
				} else {
					cntValueStatic += clft.size[i];
				}
			}
		}
	}
	
	/**
	 * Calculate the size of the class info table,
	 * adjust the addresses and return the next available
	 * address.
	 * Calculate GC info for the instance.
	 * @param addr
	 * @return
	 */
	public int setAddress(int addr) {
		
		int i;
		instGCinfo = 0;
		// first are the class variables - the static fields
		staticRefVarAddress = addrRefStatic;
		staticValueVarAddress = addrValueStatic;
		for (i=0; i<clft.len; ++i) {
			if (clft.isStatic[i]) {
				// resolve the address
				// idx is now the static address
				if (clft.isReference[i]) {
					clft.idx[i] = addrRefStatic;			
					addrRefStatic += clft.size[i];
				} else {
					clft.idx[i] = addrValueStatic;			
					addrValueStatic += clft.size[i];
				}
			} else {
				// generate GC info for the instance
				if (clft.isReference[i]) {
					instGCinfo |= (1<<clft.idx[i]);
				}
			}
		}
		classRefAddress = addr;
		// class head contains the instance size and
		// a pointer to the inteface table
		// class references point to the instance size
		addr += CLS_HEAD;
		// start of the method table, objects contain a pointer
		// to the start of this table (at ref-1)
		methodsAddress = addr;
		for (i=0; i<clvt.len; ++i) {
			MethodInfo m = clvt.mi[i];
			m.vtindex = i;
			m.structAddress = addr;
			if (clazz.getClassName().equals(JOPizer.startupClass)) {
				if (m.methodId.equals(JOPizer.bootMethod)) {
					bootAddress = addr;
				}
			}
			if (clazz.getClassName().equals(JOPizer.mainClass)) {
				if (m.methodId.equals(JOPizer.mainMethod)) {
					mainAddress = addr;
				}
			}
			addr += METH_STR;
		}
		// back reference from cp-1 to class struct
		addr += 1;
		// constant pool
		cpoolAddress = addr;
//System.out.println(clazz.getClassName()+" cplen="+clazz.getConstantPool().getLength());
		// the final size of the cp plus the length field
		addr += cpoolUsed.size()+1;
		// the optional interface table
		iftableAddress = 0;
		if (clazz.getInterfaceNames().length>0) {
			iftableAddress = addr;
			addr += listIT.size();
		}

		// add method count of class Object !
		if (clazz.getClassName().equals(JOPizer.jvmClass)) {
			jvmAddress = methodsAddress+nrObjMethods*METH_STR;
		}
		if (clazz.getClassName().equals(JOPizer.helpClass)) {
			jvmHelpAddress = methodsAddress+nrObjMethods*METH_STR;
		}
		return addr;
	}

	public void addUsedConst(int idx, int len) {
		
		Integer ii = new Integer(idx);
		
		if (cpoolUsed.contains(ii)) return;
		cpoolUsed.add(ii);
		if (len>1) {
			// add a dummy entry for a long or double constant
			cpoolUsed.add(null);
		}
	}

	/**
	 * @param cp
	 */
	public void resolveCPool(ConstantPool cp) {
		
		Constant[] ca = cp.getConstantPool();
		cpoolArry = new int[cpoolUsed.size()];
		cpoolComments = new String[ca.length];

// System.out.println(clazz.getClassName()+" cpool "+cpoolUsed);

		
		for (int i=0; i<ca.length; ++i) {
			Constant co = ca[i];
			Integer idx = new Integer(i);
			// pos is the new position in the reduced constant pool
			// idx is the position in the 'original' unresolved cpool
			int pos = cpoolUsed.indexOf(idx);
			if (pos!=-1) {
				boolean isInterface = false;
//				System.out.println("cpool@"+pos+" = orig_cp@"+i+" "+co);
				switch(co.getTag()) {
					case Constants.CONSTANT_Integer:
						cpoolArry[pos] = ((ConstantInteger) co).getBytes();
						cpoolComments[pos] = "Integer";
						break;
					case Constants.CONSTANT_Long:
						long lval = ((ConstantLong) co).getBytes();
						// store LOW, HIGH words in this order
						int loW = (new Long(0xFFFFFFFF & lval)).intValue();
						int hiW = (new Long(lval >>> 32)).intValue();
						cpoolArry[pos] = hiW;
						cpoolArry[pos+1] = loW;
						cpoolComments[pos] = "Long: "+lval;
						cpoolComments[pos+1] = "";
						break;
					case Constants.CONSTANT_Float:
						float fval = ((ConstantFloat) co).getBytes();
						cpoolArry[pos] = Float.floatToRawIntBits(fval);
						cpoolComments[pos] = "Float: "+fval;
						break;
					case Constants.CONSTANT_String:
						String str = ((ConstantString) co).getBytes(cp);
						StringInfo si = StringInfo.getStringInfo(str);
						cpoolArry[pos] = StringInfo.stringTableAddress+si.getAddress();
						cpoolComments[pos] = "String: "+si.getSaveString();
						break;
					case Constants.CONSTANT_Class:
						String clname = ((ConstantClass) co).getBytes(cp).replace('/','.');
						ClassInfo clinfo = (ClassInfo) mapClassNames.get(clname);
						if (clinfo==null) {
							cpoolComments[pos] = "Problem with class: "+clname;
							continue;
						}
						cpoolArry[pos] = clinfo.classRefAddress;
						cpoolComments[pos] = "Class: "+clname;
						break;
					case Constants.CONSTANT_InterfaceMethodref:
						isInterface = true;
					case Constants.CONSTANT_Methodref:
						// find the class for this method
						int mclidx;
						if (isInterface) {
							mclidx = ((ConstantInterfaceMethodref) co).getClassIndex();
						} else {
							mclidx = ((ConstantMethodref) co).getClassIndex();
						}
						ConstantClass mcl = (ConstantClass) cp.getConstant(mclidx);
						// the method has "/" instead of ".", fix that
						// now get the signature too...
						String mclname = mcl.getBytes(cp).replace('/','.');
						int sigidx;
						if (isInterface) {
							sigidx = ((ConstantInterfaceMethodref) co).getNameAndTypeIndex();
						} else {
							sigidx = ((ConstantMethodref) co).getNameAndTypeIndex();
						}
						ConstantNameAndType signt = (ConstantNameAndType) cp.getConstant(sigidx);
						String sigstr = signt.getName(cp)+signt.getSignature(cp);
						// now find the address of the method struct!
						ClassInfo clinf = (ClassInfo) mapClassNames.get(mclname);
						if (clinf==null) {
							// probably a reference to Native - a class that
							// is NOT present in the application.
							// we could avoid this by not adding method refs to
							// Native in our reduced cpool.
							cpoolArry[pos] = 0;
							cpoolComments[pos] = "static "+mclname+"."+sigstr;
							break;
						}
						MethodInfo minf = clinf.getVTMethodInfo(sigstr);
						if (minf==null) {
							System.out.println("Error: Method "+sigstr+" not found.");
							System.out.println("Invoked by "+clazz.getClassName());
							System.exit(1);
						}
						if(minf.method.isStatic() ||	
							// <init> and privat methods are called with invokespecial
							// which mapps in jvm.asm to invokestatic
								minf.method.isPrivate() ||
								sigstr.charAt(0)=='<'
							) {
							// for static methods a direct pointer to the
							// method struct
							cpoolArry[pos] = minf.structAddress;
							cpoolComments[pos] = "static, special or private "+clinf.clazz.getClassName()+
								"."+minf.methodId;
						} else {
							// as Flavius correctly comments:
							// TODO: CHANGE THIS TO A MORE CONSISTENT FORMAT...
							// extract the objref! for some reason the microcode needs -1 here...weird

							// that's for simple virtual methods
							int vpos = minf.vtindex;
							String comment = "virtual";
							if (isInterface) {
								comment = "interface";
								for (int j=0; j<listIT.size(); ++j) {
									IT it = (IT) listIT.get(j);
									if (it.key.equals(minf.methodId)) {
										vpos = j;
										break;
									}
								}
								// offest in interface table
								// index plus number of arguments (without this!)
								cpoolArry[pos] = (vpos<<8) + (minf.margs-1);
							} else {
								// offest in method table
								// (index*2) plus number of arguments (without this!)
								cpoolArry[pos] = (vpos*METH_STR<<8) + (minf.margs-1);
								
							}
							cpoolComments[pos] = comment+" index: "+vpos+
								" args: "+minf.margs+" "+clinf.clazz.getClassName()+
								"."+minf.methodId;						}
						break;
					case Constants.CONSTANT_Fieldref:
						int fidx = ((ConstantFieldref) co).getClassIndex();
						ConstantClass fcl = (ConstantClass) cp.getConstant(fidx);
						String fclname = fcl.getBytes(cp).replace('/','.');
						// got the class name
						sigidx = ((ConstantFieldref) co).getNameAndTypeIndex();
						signt = (ConstantNameAndType) cp.getConstant(sigidx);
						sigstr = signt.getName(cp)+signt.getSignature(cp);
						clinf = (ClassInfo) mapClassNames.get(fclname);
						int j;
						String comment = "";
						for (j=0; j<clinf.clft.len; ++j) {
							if (clinf.clft.key[j].equals(sigstr)) {
								break;
							}
						}
						if (j==clinf.clft.len) {
							System.out.println("Error: field "+fclname+"."+sigstr+" not found!");
							break;
						}
						if (clinf.clft.isStatic[j]) {
							comment = "static ";						
						}
						// for static fields a direct pointer to the
						// static field
						cpoolArry[pos] = clinf.clft.idx[j];
						cpoolComments[pos] = comment+clinf.clazz.getClassName()+
							"."+sigstr;				
						break;
					default:
						System.out.println("TODO: cpool@"+pos+" = orig_cp@"+i+" "+co);
						cpoolComments[pos] = "Problem with: "+co;
				}	 	
			}

		}
		
	}
/* from JCC JOPWriter
 * 
 * 
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
*/
	
	
	
	
	
	public void dumpStaticFields(PrintWriter out, boolean ref) {
		
		int i, addr;
		if (ref) {
			addr = staticRefVarAddress;
		} else {
			addr = staticValueVarAddress;
		}
		out.println("//");
		out.println("//\t"+addr+": "+clazz.getClassName()+
				" static fields");
		out.println("//");
		for (i=0; i<clft.len; ++i) {
			if (clft.isStatic[i]) {
				if (clft.isReference[i]==ref) {
					if (clft.size[i]==1) {
						out.print("\t\t0,");
					} else {
						out.print("\t\t0, 0,");
					}
					out.println("\t//\t"+clft.idx[i]+": "+clft.key[i]);				
				}
			}
		}
	}

	public void dump(PrintWriter out) {
			
			int i;
		
		out.println("//");
		out.println("//\t"+classRefAddress+": "+clazz.getClassName());
		out.println("//");
		out.println("\t\t"+instSize+",\t//\tinstance size");
		for (i=0; i<clft.len; ++i) {
			if (!clft.isStatic[i]) {
				out.println("\t\t\t\t//\t"+clft.idx[i]+" "+clft.key[i]);				
			}
		}
		if (instSize>31) {
			System.err.println("Error: Object of "+clazz.getClassName()+" to big!");
			System.exit(-1);
		}
		out.println("\t\t"+instGCinfo+",\t//\tinstance GC info");

		String supname = "null";
		if (superClass!=null) {
			supname = superClass.clazz.getClassName();
		}
		out.println("\t\t\t//\tTODO: pointer to super class - "+supname);
		out.println("\t\t"+iftableAddress+",\t//\tpointer to interface table");
		
		out.println("//");
		out.println("//\t"+methodsAddress+": "+clazz.getClassName()+
				" method table");
		out.println("//");

		int addr = methodsAddress;
		for(i=0; i < clvt.len; i++) {
			clvt.mi[i].dumpMethodStruct(out, addr);
			addr += METH_STR;
		}
		
		out.println();
		out.println("\t\t"+classRefAddress+",\t//\tpointer back to class struct (cp-1)");
		out.println();
	 		
		out.println("//");
		out.println("//\t"+cpoolAddress+": "+clazz.getClassName()+" constants");
		out.println("//");

		// constant pool length includes the length field
		// same is true for the index in the bytecodes:
		// The lowest constant has indes 1.
		out.println("\t\t"+(cpoolArry.length+1)+",\t//\tconst pool length");
		out.println();
		for (i=0; i<cpoolArry.length; ++i) {
			out.println("\t\t"+cpoolArry[i]+",\t//\t"+cpoolComments[i]);
		}
		
		if (iftableAddress!=0) {
			out.println("//");
			out.println("//\t"+iftableAddress+": "+clazz.getClassName()+
					" interface table");
			out.println("//");
			out.println("//\tTODO: is it enough to use methodId as key???");
			out.println("//");
			for (i=0; i<listIT.size(); ++i) {
				IT it = (IT) listIT.get(i);
				int j;
				for (j = 0; j < clvt.len; j++) { 
					if (clvt.key[j].equals(it.key)) {
						break;
					}
				}
				if (j!=clvt.len) {
					out.print("\t\t"+(methodsAddress+j*METH_STR)+",");
				} else {
					out.print("\t\t"+0+",\t");
				}
				out.println("\t//\t"+it.meth.methodId);
			}
		}

	}

	/**
	 * @param className
	 * @return
	 */
	public static ClassInfo getClassInfo(String className) {
		return (ClassInfo) mapClassNames.get(className);
	}
	
}
