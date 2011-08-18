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

/*
 * Created on 05.06.2005
 *
 */
package com.jopdesign.build;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;

/**
 * Class Struct
 * <ul>
 * <li/> 0: instance size (class reference)
 * <li/> 1: pointer to static primitiv fields (if any)
 * <li/> 2: GC info field (one bit per field)
 * <li/> 3: pointer to super class
 * <li/> 4: pointer to interface table
 * <li/> 5+: method table, two words per entry,
 *           class reference (pointer back to class info),
 *           constant pool (cp),
 *           optional interface table
 * </ul>
 * <p>class variables are collected in one area for easier GC access of the
 * reference types</p>
 *
 * @author Flavius, Martin
 */

public class JopClassInfo extends OldClassInfo implements Serializable {

    public class JopCliVisitor extends CliVisitor implements Serializable {

        private static final long serialVersionUID = 1L;

        private ConstantPool cpool;

        public JopCliVisitor(Map<String, OldClassInfo> map) {
            super(map);
        }

        public void visitJavaClass(JavaClass clazz) {

            super.visitJavaClass(clazz);
            JopClassInfo cli = (JopClassInfo) this.cli;

            cpool = clazz.getConstantPool();

            if (clazz.isInterface()) {
                cli.interfaceID = ++cli.interfaceCnt;
                cli.interfaceList.add(cli.interfaceID - 1, clazz.getClassName());
            }
        }

        public void visitMethod(Method method) {

            super.visitMethod(method);
            // now get the MethodInfo back from the ClassInfo for
            // additional work.
            String methodId = method.getName() + method.getSignature();
            OldMethodInfo mi = getITMethodInfo(methodId);
            if (JOPizer.dumpMgci) {
                // GCRT
                new GCRTMethodInfo(mi, method);
            }
        }

        public void visitConstantString(ConstantString S) {
            // identifying constant strings
            StringInfo.addString(S.getBytes(cpool));
        }
    }

    private static final long serialVersionUID = 1L;

    static class IT implements Serializable {
        private static final long serialVersionUID = 1L;

        int nr;
        String key;
        JopMethodInfo meth;
    }

    // 'global' interface table
    static LinkedList listIT = new LinkedList();

    // list of all interfaces
    static ArrayList<String> interfaceList = new ArrayList<String>();

    static int nrObjMethods;
    static int bootAddress;
    static int jvmAddress;
    static int jvmHelpAddress;
    static int mainAddress;
    static int interfaceCnt;

    // virtual method table
    class ClVT implements Serializable {
        private static final long serialVersionUID = 1L;

        int len;
        // Method name plus signature is the key
        String[] key;
        JopMethodInfo mi[];
    }

    /**
     * Field table
     *
     * @author Martin
     *
     */
    class ClFT implements Serializable {
        private static final long serialVersionUID = 1L;
        int len;
        int instSize;
        // fieldname and signature
        String[] key;
        // index in the object
        int[] idx;
        int[] size;
        boolean[] isStatic;
        boolean[] isReference;

        public String toString() {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < len; ++i) {
                sb.append(key[i] + " ");
            }

            return sb.toString();
        }
    }

    static int cntValueStatic = 0;
    static int cntRefStatic = 0;
    static int addrValueStatic = 0;
    static int addrRefStatic = 0;

    public int interfaceID;

    public ClVT clvt;

    public ClFT clft;
    private int instSize;
    private int instGCinfo;

    public List<Integer> cpoolUsed;
    public int cpoolArry[];
    public String cpoolComments[];

    public int staticValueVarAddress;
    public int staticRefVarAddress;
    public int classRefAddress;
    public int methodsAddress;
    public int cpoolAddress;
    public int iftableAddress;

    /** Mapping from original constant pool indices to constant pool indices used in the binary */

    private Map<Integer, Integer> cpoolMap = new TreeMap<Integer,Integer>();

    @Override
    public OldClassInfo newClassInfo(JavaClass jc, OldAppInfo ai) {
        return new JopClassInfo(jc, ai);
    }

    @Override
    public CliVisitor newCliVisitor(Map<String, OldClassInfo> map) {
        return new JopCliVisitor(map);
    }

    @Override
    public OldMethodInfo newMethodInfo(String mid) {
        return new JopMethodInfo(this, mid);
    }

    /**
     * A template of the cli type for the factory.
     *
     * @return
     */
    public static OldClassInfo getTemplate() {
        return new JopClassInfo(null, null);
    }

    /**
     * Constructor is only used by following two factory methods: getTemplate
     * for the dispatch of the creation with newClassInfo
     *
     * @param clazz
     * @param ai
     */
    private JopClassInfo(JavaClass clazz, OldAppInfo ai) {
        super(clazz, ai);
        methodsAddress = 0;
        cpoolAddress = 0;
        instSize = 0;
        instGCinfo = 0;
        cpoolUsed = new LinkedList<Integer>();

        // the template class info is created with a null pointer
        if (clazz != null) {
            if (clazz.getClassName().equals(JOPizer.stringClass)) {
                StringInfo.cli = this;
            }
            if (clazz.getClassName().equals(JOPizer.objectClass)) {
                nrObjMethods = clazz.getMethods().length;
            }
        }
    }

    public JopMethodInfo getVTMethodInfo(String mid) {
        for (int i = 0; i < clvt.len; ++i) {
            if (clvt.key[i].equals(mid)) {
                return clvt.mi[i];
            }
        }
        return null;
    }

    public JopMethodInfo getITMethodInfo(String mid) {
        for (int j = 0; j < listIT.size(); ++j) {
            IT it = (IT) listIT.get(j);
            if (it.key.equals(mid)) {
                return it.meth;
            }
        }
        return null;
    }

    public ClVT getClVT() {

        if (clvt == null) {
            clvt = new ClVT();
        }
        return clvt;
    }

    public ClFT getClFT() {

        if (clft == null) {
            clft = new ClFT();
        }
        return clft;
    }

    public void setInstanceSize(int size) {
        instSize = size;
    }

    /**
     * Get an IT object.
     *
     * @return
     */
    public static IT getITObject() {
        return new IT();
    }

    void cntStaticFields() {

        int i;
        for (i = 0; i < clft.len; ++i) {
            if (clft.isStatic[i]) {
                if (clft.isReference[i]) {
                    cntRefStatic += clft.size[i];
                } else {
                    cntValueStatic += clft.size[i];
                }
            }
        }
    }

    public void setStaticAddresses() {

        // the class variables (the static fields) are in a special area
        staticRefVarAddress = addrRefStatic;
        staticValueVarAddress = addrValueStatic;
        for (int i = 0; i < clft.len; ++i) {
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
            }
        }

    }
    /**
     * Calculate the size of the class info table, adjust the addresses and
     * return the next available address. Calculate GC info for the instance.
     *
     * @param addr
     * @return
     */
    public int setAddress(OldAppInfo ai, int addr) {

        int i;
        instGCinfo = getGCInfo();
        classRefAddress = addr;
        // class head contains the instance size and
        // a pointer to the interface table
        // class references point to the instance size
        addr += ClassStructConstants.CLS_HEAD;
        // start of the method table, objects contain a pointer
        // to the start of this table (at ref-1)
        if (!clazz.isInterface()) {
            methodsAddress = addr;
            for (i = 0; i < clvt.len; ++i) {
                JopMethodInfo m = clvt.mi[i];
                m.vtindex = i;
                m.structAddress = addr;
                if (clazz.getClassName().equals(JOPizer.startupClass)) {
                    if (m.methodId.equals(JOPizer.bootMethod)) {
                        bootAddress = addr;
                    }
                }
                if (clazz.getClassName().equals(ai.mainClass)) {
                    if (m.methodId.equals(JOPizer.mainMethod)) {
                        mainAddress = addr;
                    }
                }
                addr += ClassStructConstants.METH_STR;
            }
        }
        // back reference from cp-1 to class struct
        addr += 1;
        // constant pool
        cpoolAddress = addr;

        // System.out.println(clazz.getClassName()+"
        // cplen="+clazz.getConstantPool().getLength());
        // the final size of the cp plus the length field
        addr += cpoolUsed.size() + 1;

        // the optional interface table
        iftableAddress = 0;

        boolean needsInterfaceTable = false;
        for (i = 0; i < listIT.size(); i++) {
            IT it = (IT) listIT.get(i);
            boolean matchMethod = methods.containsKey(it.meth.methodId);

            boolean matchInterface = implementsInterface(it.meth.getCli().clazz.getClassName());
            if (matchMethod && matchInterface) {
                needsInterfaceTable = true;
            }
        }

        if (superClass != null) {
            String[] interfaceNames = clazz.getInterfaceNames();
            for (i = 0; i < interfaceNames.length; i++) {
                if (!((JopClassInfo) superClass).implementsInterface(interfaceNames[i])) {
                    needsInterfaceTable = true;
                }
            }
        } else {
            needsInterfaceTable = clazz.getInterfaceNames().length > 0;
        }

        if (needsInterfaceTable) {
            addr += (interfaceCnt + 31) / 32;
            iftableAddress = addr;
            if (!clazz.isInterface()) {
                addr += listIT.size();
            }
        }

        // add method count of class Object !
        if (clazz.getClassName().equals(JOPizer.jvmClass)) {
            jvmAddress = methodsAddress + nrObjMethods
                    * ClassStructConstants.METH_STR;
        }
        if (clazz.getClassName().equals(JOPizer.helpClass)) {
            jvmHelpAddress = methodsAddress + nrObjMethods
                    * ClassStructConstants.METH_STR;
        }
        return addr;
    }

    private boolean implementsInterface(String ifname) {
        OldClassInfo cli = this;
        do {
            String[] interfaces = cli.clazz.getInterfaceNames();
            for (int i = 0; i < interfaces.length; i++) {
                if (ifname.equals(interfaces[i])) {
                    return true;
                } else {
                    // an interface may have a super-interface
                    JopClassInfo superCli = (JopClassInfo) appInfo.cliMap.get(interfaces[i]);
                    boolean match = superCli.implementsInterface(ifname);
                    if (match) {
                        return true;
                    }
                }
            }
            cli = (OldClassInfo) cli.superClass;
        } while (cli != null);
        return false;
    }

    /**
     * generate GC info for the instance
     */
    private int getGCInfo() {

        int gcInfo = 0;
        for (JopClassInfo clinf = this; clinf != null; clinf = (JopClassInfo) clinf.superClass) {
            ClFT ft = clinf.clft;
            for (int i = 0; i < ft.len; ++i) {
                if (!ft.isStatic[i] & ft.isReference[i]) {
                    gcInfo |= (1 << ft.idx[i]);
                }
            }
        }

        return gcInfo;
    }

    public void addUsedConst(int idx, int len) {

        Integer ii = new Integer(idx);

        if (cpoolUsed.contains(ii))
            return;
        cpoolUsed.add(ii);
        if (len > 1) {
            // add a dummy entry for a long or double constant
            cpoolUsed.add(null);
        }
        cpoolMap.put(idx,cpoolUsed.indexOf(ii));
    }

    /**
     * @param cp
     */
    public void resolveCPool(ConstantPool cp) {

        Constant[] ca = cp.getConstantPool();
        cpoolArry = new int[cpoolUsed.size()];
        cpoolComments = new String[ca.length];

        // System.out.println(clazz.getClassName()+" cpool "+cpoolUsed);

        for (int i = 0; i < ca.length; ++i) {
            Constant co = ca[i];
            Integer idx = new Integer(i);
            // pos is the new position in the reduced constant pool
            // idx is the position in the 'original' unresolved cpool
            int pos = cpoolUsed.indexOf(idx);
            if (pos != -1) {
                boolean isInterface = false;
                // System.out.println("cpool@"+pos+" = orig_cp@"+i+" "+co);
                switch (co.getTag()) {
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
                    cpoolArry[pos + 1] = loW;
                    cpoolComments[pos] = "Long: " + lval;
                    cpoolComments[pos + 1] = "";
                    break;
                case Constants.CONSTANT_Float:
                    float fval = ((ConstantFloat) co).getBytes();
                    cpoolArry[pos] = Float.floatToRawIntBits(fval);
                    cpoolComments[pos] = "Float: " + fval;
                    break;
                case Constants.CONSTANT_Double:
                    double dval = ((ConstantDouble) co).getBytes();
                    long d_lval = Double.doubleToRawLongBits(dval);
                    // store LOW, HIGH words in this order
                    int d_loW = (new Long(0xFFFFFFFF & d_lval)).intValue();
                    int d_hiW = (new Long(d_lval >>> 32)).intValue();
                    cpoolArry[pos] = d_hiW;
                    cpoolArry[pos + 1] = d_loW;
                    cpoolComments[pos] = "Double: " + dval;
                    cpoolComments[pos + 1] = "";
                    break;
                case Constants.CONSTANT_String:
                    String str = ((ConstantString) co).getBytes(cp);
                    StringInfo si = StringInfo.getStringInfo(str);
                    cpoolArry[pos] = StringInfo.stringTableAddress
                            + si.getAddress();
                    cpoolComments[pos] = "String: " + si.getSaveString();
                    break;
                case Constants.CONSTANT_Class:
                    String clname = ((ConstantClass) co).getBytes(cp).replace(
                            '/', '.');
                    JopClassInfo clinfo = (JopClassInfo) appInfo.cliMap
                            .get(clname);
                    if (clinfo == null) {
                        cpoolComments[pos] = "Problem with class: " + clname;
                        String type = clname.substring(clname.length()-2);
                        if (type.charAt(0)=='[') {
                        	switch (type.charAt(1)) {
                        	case 'Z':
                        		cpoolArry[pos] = 4;
                        		break;
                        	case 'C':
                        		cpoolArry[pos] = 5;
                        		break;
                        	case 'F':
                        		cpoolArry[pos] = 6;
                        		break;
                        	case 'D':
                        		cpoolArry[pos] = 7;
                        		break;
                        	case 'B':
                        		cpoolArry[pos] = 8;
                        		break;
                        	case 'S':
                        		cpoolArry[pos] = 9;
                        		break;
                        	case 'I':
                        		cpoolArry[pos] = 10;
                        		break;
                        	case 'J':
                        		cpoolArry[pos] = 11;
                        		break;
                        	default:
                        		; // all other types are missing...
                        	}
                        }
                        // System.out.println(cpoolComments[pos]+" "+type+" "+cpoolArry[pos]);
                        continue;
                    }
                    cpoolArry[pos] = clinfo.classRefAddress;
                    cpoolComments[pos] = "Class: " + clname;
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
                    String mclname = mcl.getBytes(cp).replace('/', '.');
                    int sigidx;
                    if (isInterface) {
                        sigidx = ((ConstantInterfaceMethodref) co).getNameAndTypeIndex();
                    } else {
                        sigidx = ((ConstantMethodref) co).getNameAndTypeIndex();
                    }
                    ConstantNameAndType signt = (ConstantNameAndType) cp.getConstant(sigidx);
                    String sigstr = signt.getName(cp) + signt.getSignature(cp);
                    // now find the address of the method struct!
                    JopClassInfo clinf = (JopClassInfo) appInfo.cliMap.get(mclname);
                    if (clinf == null) {
                        // probably a reference to Native - a class that
                        // is NOT present in the application.
                        // we could avoid this by not adding method refs to
                        // Native in our reduced cpool.
                        cpoolArry[pos] = 0;
                        cpoolComments[pos] = "static " + mclname + "." + sigstr;
                        break;
                    }
                    JopMethodInfo minf;
                    if (isInterface) {
                        minf = clinf.getITMethodInfo(sigstr);
                    } else {
                        minf = clinf.getVTMethodInfo(sigstr);
                    }
                    if (minf == null) {
                        System.out.println("Error: Method "
                                + clinf.clazz.getClassName() + '.' + sigstr
                                + " not found.");
                        System.out.println("Invoked by " + clazz.getClassName());
                        for (int xxx = 0; xxx < clinf.clvt.len; ++xxx) {
                            System.out.println(clinf.clvt.key[xxx]);
                        }
                        System.exit(1);
                    }
                    if (minf.getMethod().isStatic() ||
                    // <init> and privat methods are called with invokespecial
                            // which mapps in jvm.asm to invokestatic
                            minf.getMethod().isPrivate() || sigstr.charAt(0) == '<') {
                        // for static methods a direct pointer to the
                        // method struct
                        cpoolArry[pos] = minf.structAddress;
                        cpoolComments[pos] = "static, special or private "
                                + clinf.clazz.getClassName() + "."
                                + minf.methodId;
                    } else {
                        // as Flavius correctly comments:
                        // TODO: CHANGE THIS TO A MORE CONSISTENT FORMAT...
                        // extract the objref! for some reason the microcode
                        // needs -1 here...weird

                        // that's for simple virtual methods
                        int vpos = minf.vtindex;
                        String comment = "virtual";

                        // TODO: is kind of redundant search as we've already
                        // searched the IT table with getVTMethodInfo()
                        // TODO: do we handle different interfaces with same
                        // method id correct? (see buildIT)
                        if (isInterface) {
                            comment = "interface";
                            for (int j = 0; j < listIT.size(); ++j) {
                                IT it = (IT) listIT.get(j);
                                if (it.key.equals(minf.methodId)) {
                                    vpos = j;
                                    break;
                                }
                            }
                            // offest in interface table
                            // index plus number of arguments (without this!)
                            cpoolArry[pos] = (vpos << 8) + (minf.margs - 1);
                        } else {
                            // offest in method table
                            // (index*2) plus number of arguments (without
                            // this!)
                            cpoolArry[pos] = (vpos
                                    * ClassStructConstants.METH_STR << 8)
                                    + (minf.margs - 1);

                        }
                        cpoolComments[pos] = comment + " index: " + vpos
                                + " args: " + minf.margs + " "
                                + clinf.clazz.getClassName() + "."
                                + minf.methodId;
                    }
                    break;
                case Constants.CONSTANT_Fieldref:
                    throw new Error("Fieldref should not be used anymore");
//					int fidx = ((ConstantFieldref) co).getClassIndex();
//					ConstantClass fcl = (ConstantClass) cp.getConstant(fidx);
//					String fclname = fcl.getBytes(cp).replace('/', '.');
//					// got the class name
//					sigidx = ((ConstantFieldref) co).getNameAndTypeIndex();
//					signt = (ConstantNameAndType) cp.getConstant(sigidx);
//					sigstr = signt.getName(cp) + signt.getSignature(cp);
//					clinf = (JopClassInfo) appInfo.cliMap.get(fclname);
//					int j;
//					String comment = "";
//					boolean found = false;
//					while (!found) {
//						for (j = 0; j < clinf.clft.len; ++j) {
//							if (clinf.clft.key[j].equals(sigstr)) {
//								found = true;
//								if (clinf.clft.isStatic[j]) {
//									comment = "static ";
//								}
//								// for static fields a direct pointer to the
//								// static field
//								cpoolArry[pos] = clinf.clft.idx[j];
//								cpoolComments[pos] = comment
//										+ clinf.clazz.getClassName() + "."
//										+ sigstr;
//								break;
//							}
//						}
//						if (!found) {
//							clinf = (JopClassInfo) clinf.superClass;
//							if (clinf == null) {
//								System.out.println("Error: field " + fclname
//										+ "." + sigstr + " not found!");
//								break;
//							}
//						}
//					}
//					break;
                default:
                    System.out.println("TODO: cpool@" + pos + " = orig_cp@" + i
                            + " " + co);
                    cpoolComments[pos] = "Problem with: " + co;
                }
            }

        }
    }

    public void dumpStaticFields(PrintWriter out, PrintWriter outLinkInfo, boolean ref) {

        int i, addr;
        if (ref) {
            addr = staticRefVarAddress;
        } else {
            addr = staticValueVarAddress;
        }
        out.println("//");
        out.println("//\t" + addr + ": " + clazz.getClassName() + " static "
                + (ref ? "reference " : "") + "fields");
        out.println("//");

        for (i = 0; i < clft.len; ++i) {
            if (clft.isStatic[i]) {
                if (clft.isReference[i] == ref) {
                    outLinkInfo.println("static "+clazz.getClassName()+"."+clft.key[i]+" "+clft.idx[i]);
                    if (clft.size[i] == 1) {
                        out.print("\t\t0,");
                    } else {
                        out.print("\t\t0, 0,");
                    }
                    out.println("\t//\t" + clft.idx[i] + ": " + clft.key[i]);
                }
            }
        }
    }

    public void dump(PrintWriter out, PrintWriter outLinkInfo) {

        int i;


        out.println("//");
        out.println("//\t" + classRefAddress + ": " + clazz.getClassName()+" class info");
        out.println("//");
        out.println("\t\t" + instSize + ",\t//\tinstance size");

        // link info: class addresses
        outLinkInfo.println("class "+clazz.getClassName()+" "+methodsAddress+" "+cpoolAddress);
        outLinkInfo.println(" -instSize "+instSize);
        
        for (i = 0; i < clft.len; ++i) {
            if (!clft.isStatic[i]) {
                out.println("\t\t\t\t//\t" + clft.idx[i] + " " + clft.key[i]);
                /* link info: field offset */
                outLinkInfo.println(" -field " + clft.key[i] + " " +clft.idx[i]);
            }
        }

        out.println("\t\t" + staticValueVarAddress
                + ",\t//\tpointer to static primitive fields");
        if (instSize > 31) {
            System.err.println("Error: Object of " + clazz.getClassName()
                    + " to big! Size=" + instSize);
            System.exit(-1);
        }
        out.println("\t\t" + instGCinfo + ",\t//\tinstance GC info");

        String supname = "null";
        int superAddr = 0;
        if (superClass != null) {
            supname = superClass.clazz.getClassName();
            superAddr = ((JopClassInfo) appInfo.cliMap.get(supname)).classRefAddress;
        }
        if (!clazz.isInterface()) {
            out.println("\t\t" + superAddr + ",\t//\tpointer to super class - "
                    + supname);
            // link info: super address
            outLinkInfo.println(" -super "+superAddr);
        } else {
            out.println("\t\t" + (-interfaceID) + ",\t//\tinterface ID");
        }

        boolean useSuperInterfaceTable = false;
        if ((iftableAddress == 0) && (superClass != null)) {
            iftableAddress = ((JopClassInfo) appInfo.cliMap.get(supname)).iftableAddress;
            useSuperInterfaceTable = true;
        }
        out.println("\t\t" + iftableAddress
                + ",\t//\tpointer to interface table");

        if (!clazz.isInterface()) {
            out.println("//");
            out.println("//\t" + methodsAddress + ": " + clazz.getClassName()
                    + " method table");
            out.println("//");

            int addr = methodsAddress;
            for (i = 0; i < clvt.len; i++) {
                clvt.mi[i].dumpMethodStruct(out, addr);
                outLinkInfo.println(" -mtab "+clvt.mi[i].getFQMethodName()+" "+addr);
                addr += ClassStructConstants.METH_STR;
            }
        } else {
            out.println("//");
            out.println("//\tno method table for interfaces");
            out.println("//");
        }

        out.println();
        out.println("\t\t" + classRefAddress
                + ",\t//\tpointer back to class struct (cp-1)");
        out.println();

        out.println("//");
        out.println("//\t" + cpoolAddress + ": " + clazz.getClassName()
                + " constants");
        out.println("//");

        // link info: constant pool mapping
        for(Entry<Integer, Integer> entry : cpoolMap.entrySet()) {
            outLinkInfo.println(" -constmap "+entry.getKey()+" "+entry.getValue());
        }

        // constant pool length includes the length field
        // same is true for the index in the bytecodes:
        // The lowest constant has index 1.
        out.println("\t\t" + (cpoolArry.length + 1)
                + ",\t//\tconst pool length");
        out.println();
        for (i = 0; i < cpoolArry.length; ++i) {
            out.println("\t\t" + cpoolArry[i] + ",\t//\t" + cpoolComments[i]);
        }

        if (iftableAddress != 0 && !useSuperInterfaceTable) {

            out.println("//");
            out.println("//\t" + (iftableAddress - (interfaceCnt + 31) / 32)
                    + ": " + clazz.getClassName() + " implements table");
            out.println("//");
            for (i = (interfaceCnt + 31) / 32 - 1; i >= 0; i--) {
                String comment = "";
                int word = 0;
                int j;
                for (j = 31; j >= 0; j--) {
                    word <<= 1;
                    if ((i * 32 + j) < interfaceCnt) {
                        if (implementsInterface(interfaceList.get(i * 32 + j))) {
                            word |= 1;
                            comment += interfaceList.get(i * 32 + j) + ", ";
                        }
                        ;
                    }
                }
                out.println("\t\t" + word + ",\t//\t" + comment);
            }

            out.println("//");
            out.println("//\t" + iftableAddress + ": " + clazz.getClassName()
                    + " interface table");
            out.println("//");
            if (!clazz.isInterface()) {
                out.println("//\tTODO: is it enough to use methodId as key???");
                out.println("//");
                for (i = 0; i < listIT.size(); ++i) {
                    IT it = (IT) listIT.get(i);
                    int j;
                    for (j = 0; j < clvt.len; j++) {
                        if (clvt.key[j].equals(it.key)) {
                            break;
                        }
                    }
                    if (j != clvt.len) {
                        out.print("\t\t"
                                + (methodsAddress + j
                                        * ClassStructConstants.METH_STR) + ",");
                    } else {
                        out.print("\t\t" + 0 + ",\t");
                    }
                    out.println("\t//\t" + it.meth.methodId);
                }
            }
        }

    }

    /**
     * @param className
     * @return
     */
    //	public static JopClassInfo getClassInfo(String className) {
    //		return (JopClassInfo) appInfo.cliMap.get(className);
    //	}

}
