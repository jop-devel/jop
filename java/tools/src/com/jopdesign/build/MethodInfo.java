/*
 * Created on 05.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.jopdesign.build;

import java.util.*;
import java.io.PrintWriter;

import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;


/**
 * @author Flavius, Martin
 *
 */
public class MethodInfo {

	static List clinitList = new LinkedList();
	// counts the number of methods with one or more local object references
	public static int cntMgci = 0;
	private static int tmpCntMgci = 0;
	int mgci;	
	String methodId;
	ClassInfo cli;
	Method method;
	int codeAddress;
	// struct address is ONLY useful for <clinit> methods
	// and the boot/main methods!
	int structAddress;
	Code code;
	int mstack, margs, mreallocals, len;
	int vtindex;
	
	public MethodInfo(ClassInfo jc, String mid) {
		cli = jc;
		codeAddress = 0;
		structAddress = 0;
		code = null;
		methodId = mid;
		
		
//		vindex = 0;
//		codedumped = false;
		if (mid.equals(JOPizer.clinitSig)) {
			clinitList.add(this);
		}
	}

	/**
	 * @param m
	 */
	public void setMethod(Method m, int addr) {
        method = m;
        code = m.getCode();
        codeAddress = addr;
        
        margs = 0;
        Type at[] = m.getArgumentTypes();
        for (int i=0; i<at.length; ++i) {
        	margs += at[i].getSize();
        }
//		FIXME! invokespecial adds an extra objref!!! inits, private, and superclass calls
//		for now only handle inits
		if(!m.isStatic()) {
			margs++;
		}
		if (m.isAbstract()) {
			mstack = mreallocals = len = 0;
		} else {
			mstack = m.getCode().getMaxStack();
			// the 'real' locals - means without arguments
			mreallocals = m.getCode().getMaxLocals() - margs;
//			System.err.println(" ++++++++++++ "+methodId+" --> mlocals ="+mlocals+" margs ="+margs);
			len = (m.getCode().getCode().length + 3)/4;

			if (len>=JOPizer.METHOD_MAX_SIZE/4 || mreallocals>31 || margs>31) {
				// we interprete clinit on JOP - no size restriction
				if (!m.getName().equals("<clinit>")) {
					System.err.println("wrong size: "+cli.clazz.getClassName()+"."+methodId);
					System.exit(-1);					
				}
			}
//System.out.println((mstack+m.getCode().getMaxLocals())+" "+
//		m.getName()+" maxStack="+mstack+" locals="+m.getCode().getMaxLocals());
			
			
			//rup: Collect info on the local variables
			LocalVariableTable lvt = m.getLocalVariableTable();
			// method GC info
			mgci = 0; 
			if (lvt != null) {
				LocalVariable[] lv = lvt.getLocalVariableTable();
				for (int i = 0; i < lv.length; i++) {
//					System.out.println("Local var:" + lv[i].getIndex()
//							+ " Signature:" + lv[i].getSignature());
					String sig = lv[i].getSignature();
					if (sig.charAt(0) == 'L' || sig.charAt(0) == '[') {
						int ref = (1 << i);
						mgci |= ref;
//						System.out.println("GCI, sig(0):" + sig.charAt(0)
//								+ ", ref:" + ref + ", mgci:" + mgci);
					}
				}
				// increment the static counter (used by JOPizer) if a reference
				// is part of the args or locals
				if (mgci > 0)
					cntMgci++;
			} else {
//				System.out.println("LocalVariableTable:null,margs:" + margs
//						+ ",mreallocals:" + mreallocals);
				if (margs + mreallocals > 0) {
					System.out
							.println("GC problem: GC needs LocalVariableTable to mark references");
					System.out
							.println("Recompile target source with javac -g option");
					// System.exit(0); //TODO: Why does test test Clock not give
					// LocalVariableTables
				}
			}
		}
	}

	public int getLength() {

		return len;
	}

	public void dumpMethodStruct(PrintWriter out, int addr) {
		
		if (methodId.equals(JOPizer.clinitSig)) {
			out.println("\t// no size for <clinit> - we iterpret it and allow larger methods!");
		}
		// java_lang_String
		// 0x01 TODO access
		// 2	TODO ? stack
		//  code start:1736
		//  code length:4
		//  cp:3647
		//  locals: 1 args size: 1
		
		String abstr = "";
		if (method.isAbstract()) {
			abstr = "abstract ";
		}

		out.println("\t//\t"+addr+": "+abstr+cli.clazz.getClassName()+"."+methodId);
		out.println("\t\t//\tcode start: " + codeAddress);
		out.println("\t\t//\tcode length: " + len);
		out.println("\t\t//\tcp: " + cli.cpoolAddress);
		out.println("\t\t//\tlocals: "+(mreallocals+margs)+" args size: "+margs);

		int word1 = codeAddress << 10 | len;
		// we allow only large <clinit> methods
		if (methodId.equals(JOPizer.clinitSig)) {
			word1 = codeAddress << 10;
		}
		int word2 = cli.cpoolAddress << 10 |  mreallocals << 5 |  margs;
		
		if (method.isAbstract()) {
			word1 = word2 = 0;
		}
		
		out.println("\t\t"+word1+",");
		out.println("\t\t"+word2+",");

	}
	public void dumpMethodGcis(PrintWriter out) {
		out.println("\t//\tgarbage info word for method "
				+ cli.clazz.getClassName() + "." + methodId);
		out.println("\t//\targs size:" + margs + " locals:" + mreallocals);

		if (mgci > 0) {
			tmpCntMgci++;
			out.println("\t//\ttmpCntMgci:" + tmpCntMgci);
			// make mgci to bit string
			StringBuffer sb = new StringBuffer();
			int mask = 0x01;
			for (int i = 31; i >= 0; i--) {
				int res = (mgci >>> i) & mask;
				if ((i + 1) % 8 == 0 && i < 31)
					sb.append("_");
				sb.append(res);
			}

			// out.println("\t\t"+codeAddress+",//\tkey");structAddress
			out.println("\t\t" + structAddress + ",//\tkey");
			out.println("\t\t" + mgci + ",//\tmgci:" + sb.toString());
		}
	}
		
	public void dumpByteCode(PrintWriter out) {

		out.println("//\t"+codeAddress+": "+methodId);
		if (code==null) {
			out.println("//\tabstract");
			return;
		}
		byte bc[] = code.getCode();
		String post = "// ";
		int i,word,j;
		for(j=0,i=3,word=0;j<bc.length;j++) {
			word = word << 8 | (bc[j] & 0xFF);
			post += (bc[j] & 0xFF)+" ";
			if(i == 0) {
				out.println("\t"+word+",\t"+post);
				post = "// ";
				i = 3;
				word = 0;
			} else 
				i--;
		}
		if(i!=3) {
			word = word << (i+1)*8;
			out.println("\t"+word+",\t"+post);
		}
	}


}
