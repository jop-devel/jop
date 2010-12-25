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

import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;

/**
 * @author Flavius, Martin
 * @deprecated code needs to be moved to other class, data should be attached to methodinfo using CustomKeys
 */
public class JopMethodInfo extends OldMethodInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	static List clinitList;
	int codeAddress;
	// struct address is ONLY useful for <clinit> methods
	// and the boot/main methods!
	// Now it's necessary for debugging too.
	int structAddress;
	CodeException[] exctab;
	int mstack, margs, mreallocals;
	int len, exclen;
	int vtindex;

	/**
	 * Constructor is only used by the ClassInfo visitor
	 *
	 * @param jc
	 * @param mid
	 */
	protected JopMethodInfo(OldClassInfo jc, String mid) {
		super(jc, mid);
		codeAddress = 0;
		structAddress = 0;
	}

	/**
	 * Return the correct type of cli
	 */
	public JopClassInfo getCli() {
		return (JopClassInfo) super.getCli();
	}

	/**
	 * Extract linking information for this method
	 * @param addr the bytecode start address
	 */
	public void setInfo(int addr) {
		codeAddress = addr;

		Method m = getMethod();

		margs = 0;
		Type at[] = m.getArgumentTypes();
		for (int i = 0; i < at.length; ++i) {
			margs += at[i].getSize();
		}
		// FIXME! invokespecial adds an extra objref!!! inits, private, and
		// superclass calls
		// for now only handle inits
		if (!m.isStatic()) {
			margs++;
		}
		if (m.isAbstract()) {
			mstack = mreallocals = len = exclen = 0;
			exctab = null;
		} else {
			mstack = m.getCode().getMaxStack();
			// the 'real' locals - means without arguments
			mreallocals = m.getCode().getMaxLocals() - margs;
			// System.err.println(" ++++++++++++ "+methodId+" --> mlocals
			// ="+mlocals+" margs ="+margs);
			len = (m.getCode().getCode().length + 3) / 4;
			exctab = m.getCode().getExceptionTable();
			exclen = exctab != null ? exctab.length : 0;

			// TODO: couldn't len=JOP...MAX_SIZE/4 be ok?
			if (len >= JOPizer.METHOD_MAX_SIZE / 4 || mreallocals > 31
					|| margs > 31) {
				// we interprete clinit on JOP - no size restriction
				if (!m.getName().equals("<clinit>")) {
					System.err.println("len(max:"
							+ (JOPizer.METHOD_MAX_SIZE / 4) + ")=" + len
							+ "mreallocals(max:31)=" + mreallocals
							+ " margs(max:31)=" + margs);
					System.err.println("wrong size: "
							+ getCli().clazz.getClassName() + "." + methodId);
					throw new Error();
				}
			}
			// System.out.println((mstack+m.getCode().getMaxLocals())+" "+
			// m.getName()+" maxStack="+mstack+"
			// locals="+m.getCode().getMaxLocals());

		}
	}

	public int getLength() {

		return getMethod().isAbstract() ? 0 : len + 1 + 2 * exclen;
	}

	public void dumpMethodStruct(PrintWriter out, int addr) {

		if (methodId.equals(OldAppInfo.clinitSig)
				&& len >= JOPizer.METHOD_MAX_SIZE / 4) {
			out.println("\t// no size for <clinit> - we interpret it and allow larger methods!");
		}
		// java_lang_String
		// 0x01 TODO access
		// 2 TODO ? stack
		// code start:1736
		// code length:4
		// cp:3647
		// locals: 1 args size: 1

		String abstr = "";
		if (getMethod().isAbstract()) {
			abstr = "abstract ";
		}

		out.println("\t//\t" + addr + ": " + abstr
				+ getCli().clazz.getClassName() + "." + methodId);
		out.println("\t\t//\tcode start: " + codeAddress);
		out.println("\t\t//\tcode length: " + len);
		out.println("\t\t//\tcp: " + getCli().cpoolAddress);
		out.println("\t\t//\tlocals: " + (mreallocals + margs) + " args size: "
				+ margs);

		int word1 = codeAddress << 10 | len;
		// no length on large <clinit> methods
		// get interpreted at start - see Startup.clazzinit()
		if (methodId.equals(OldAppInfo.clinitSig)
				&& len >= JOPizer.METHOD_MAX_SIZE / 4) {
			word1 = codeAddress << 10;
		}
		int word2 = getCli().cpoolAddress << 10 | mreallocals << 5 | margs;

		if (getMethod().isAbstract()) {
			word1 = word2 = 0;
		}

		out.println("\t\t" + word1 + ",");
		out.println("\t\t" + word2 + ",");

	}

	public void dumpByteCode(PrintWriter out, PrintWriter outLinkInfo) {
		// link info: dump bytecode address
		outLinkInfo.println("bytecode "+getFQMethodName()+" "+codeAddress);

		out.println("//\t" + codeAddress + ": " + methodId);
		if (getCode() == null) {
			out.println("//\tabstract");
			return;
		}
		byte bc[] = getCode().getCode();
		String post = "// ";
		int i, word, j;
		for (j = 0, i = 3, word = 0; j < bc.length; j++) {
			word = word << 8 | (bc[j] & 0xFF);
			post += (bc[j] & 0xFF) + " ";
			if (i == 0) {
				out.println("\t" + word + ",\t" + post);
				post = "// ";
				i = 3;
				word = 0;
			} else
				i--;
		}
		if (i != 3) {
			word = word << (i + 1) * 8;
			out.println("\t" + word + ",\t" + post);
		}

		word = getMethod().isSynchronized() ? 1 : 0;
		word = word << 16 | (exclen & 0xFFFF);

		out.println("\t" + word
				+ ",\t//\tsynchronized?, exception table length");

		for (i = 0; i < exclen; i++) {
			Integer idx = new Integer(exctab[i].getCatchType());
			int pos = getCli().cpoolUsed.indexOf(idx) + 1;

			word = exctab[i].getStartPC();
			post = "// start: " + exctab[i].getStartPC();
			word = word << 16 | exctab[i].getEndPC();
			post += "\tend: " + exctab[i].getEndPC();
			out.println("\t" + word + ",\t" + post);

			word = exctab[i].getHandlerPC();
			post = "// target: " + exctab[i].getHandlerPC();
			word = word << 16 | pos;
			post += "\ttype: " + pos;
			out.println("\t" + word + ",\t" + post);
		}
	}

	public int getCodeAddress() {
		return codeAddress;
	}

	public int getStructAddress() {
		return structAddress;
	}
}
