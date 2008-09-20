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
import java.io.PrintWriter;
import java.io.Serializable;

import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;


/**
 * @author Flavius, Martin
 *
 */
public class MethodInfo implements Serializable{

  private static final long serialVersionUID = 1L;

	static List clinitList;
	String methodId;
	ClassInfo cli;
	Method method;
	int codeAddress;
	// struct address is ONLY useful for <clinit> methods
	// and the boot/main methods!
    // Now it's necessary for debugging too. 
	int structAddress;
	Code code;
	CodeException[] exctab;
	int mstack, margs, mreallocals;
	int len, exclen;
	int vtindex;
	
	public MethodInfo(ClassInfo jc, String mid) {
		cli = jc;
		codeAddress = 0;
		structAddress = 0;
		code = null;
		methodId = mid;
		
		
//		vindex = 0;
//		codedumped = false;
	}

	/**
	 * Set on SetClassInfo
	 * @param m
	 */
	public void setMethod(Method m) {
        method = m;
        code = m.getCode();		
	}
	/**
	 * @param m
	 */
	public void setInfo(int addr) {
        codeAddress = addr;
        
        Method m = method;
        
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
			mstack = mreallocals = len = exclen = 0;
			exctab = null;
		} else {
			mstack = m.getCode().getMaxStack();
			// the 'real' locals - means without arguments
			mreallocals = m.getCode().getMaxLocals() - margs;
//			System.err.println(" ++++++++++++ "+methodId+" --> mlocals ="+mlocals+" margs ="+margs);
			len = (m.getCode().getCode().length + 3)/4;
			exctab = m.getCode().getExceptionTable();
			exclen = exctab != null ? exctab.length : 0;

			// TODO: couldn't len=JOP...MAX_SIZE/4 be ok?
			if (len>=JOPizer.METHOD_MAX_SIZE/4 || mreallocals>31 || margs>31) {
				// we interprete clinit on JOP - no size restriction
				if (!m.getName().equals("<clinit>")) {
					System.err.println("len(max:"+(JOPizer.METHOD_MAX_SIZE/4)+")="+len+
							"mreallocals(max:31)="+mreallocals+" margs(max:31)="+margs);
					System.err.println("wrong size: "+cli.clazz.getClassName()+"."+methodId);
					System.exit(-1);					
				}
			}
//System.out.println((mstack+m.getCode().getMaxLocals())+" "+
//		m.getName()+" maxStack="+mstack+" locals="+m.getCode().getMaxLocals());

		}
	}

	public int getLength() {

		return method.isAbstract() ? 0 : len + 1 + 2*exclen;
	}

	public void dumpMethodStruct(PrintWriter out, int addr) {

		if (methodId.equals(JOPizer.clinitSig) && len>=JOPizer.METHOD_MAX_SIZE/4) {
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
		// no length on large <clinit> methods
		// get interpreted at start - see Startup.clazzinit()
		if (methodId.equals(JOPizer.clinitSig) && len>=JOPizer.METHOD_MAX_SIZE/4) {
			word1 = codeAddress << 10;
		}
		int word2 = cli.cpoolAddress << 10 |  mreallocals << 5 |  margs;
		
		if (method.isAbstract()) {
			word1 = word2 = 0;
		}
		
		out.println("\t\t"+word1+",");
		out.println("\t\t"+word2+",");

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

		word = method.isSynchronized() ? 1 : 0;
		word = word << 16 | (exclen & 0xFFFF);

 		out.println("\t"+word+",\t//\tsynchronized?, exception table length");

		for (i = 0; i < exclen; i++) {
			Integer idx = new Integer(exctab[i].getCatchType());
			int pos = cli.cpoolUsed.indexOf(idx)+1;
			
			word = exctab[i].getStartPC();
			post = "// start: "+exctab[i].getStartPC();
			word = word << 16 | exctab[i].getEndPC();
			post += "\tend: "+exctab[i].getEndPC();
			out.println("\t"+word+",\t"+post);
			
			word = exctab[i].getHandlerPC();
			post = "// target: "+exctab[i].getHandlerPC();
			word = word << 16 | pos;
			post += "\ttype: "+pos;
			out.println("\t"+word+",\t"+post);
		}
	}

  public int getCodeAddress()
  {
    return codeAddress;
  }
  
  public int getStructAddress()
  {
    return structAddress;
  }
  
  public Method getMethod()
  {
    return method;
  }

  public Code getCode()
  {
    return code;
  }
}
