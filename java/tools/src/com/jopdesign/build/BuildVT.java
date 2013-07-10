/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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
 * Created on 04.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.jopdesign.build;

import java.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

/**
 * 
 * Build virtual method tables and the one and only interface table.
 * 
 * @author martin
 *
 */
public class BuildVT extends JOPizerVisitor {

	Map mapClVT = new HashMap();
	Map mapClFT = new HashMap();

	
	public BuildVT(OldAppInfo jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		// don't get confused in the build process
		setCli(null);
		// TODO: if (!array class) ...
		buildVT(clazz);
		if (clazz.isInterface()) {
			buildIT(clazz);
		}
	}

	
	/**
	 * Called recursive to build the VTs top down.
	 * 
	 * @param clazz
	 */
	private void buildVT(JavaClass clazz) {

		int i, j;

// System.err.println("invoke buildVT on class: "+clazz);
		JopClassInfo cli;
		cli = (JopClassInfo) ai.cliMap.get(clazz.getClassName());
// System.err.println("build VT on class: "+cli.clazz);

/*
 * TODO: we have TWO mappings of clazzes: in JOPWriter AND in ClassInfo
 * However, they are not consistent!
 * The following results in a null pointer for java.lang.Object:
		cli = jz.getClassInfo(clazz);
System.err.println("build VT on class: "+cli.clazz);
*/

		JopClassInfo.ClVT supVt = null;
		JopClassInfo.ClFT supFt = null;
		if (clazz.getClassName().equals(clazz.getSuperclassName())) {
			;	// now we'r Object
		} else {
			OldClassInfo clisup = (OldClassInfo) ai.cliMap.get(clazz.getSuperclassName());

//					JavaClass superClazz = clazz.getSuperClass();
//			 System.err.println("super: "+superClazz);
			JavaClass superClazz = clisup.clazz;
			String superName = superClazz.getClassName();
// System.err.println("super name: "+superName);
			if (mapClVT.get(superName)==null) {	
// System.err.println("rec. invoke buildVT with: "+superClazz);
				buildVT(superClazz);	// first build super VT
			}
			supVt = (JopClassInfo.ClVT) mapClVT.get(superName);
			supFt = (JopClassInfo.ClFT) mapClFT.get(superName);
		}
// System.err.println("build VT on: "+clazz.getClassName());
		String clazzName = clazz.getClassName();
		if (mapClVT.get(clazzName)!=null) {	
			return;									// allready done!
		}

// this also tries to load from application CLASSPATH
//		int intfCount =  clazz.getInterfaces().length;

// System.err.println(cli);
		JopClassInfo.ClVT clvt = cli.getClVT();
		JopClassInfo.ClFT clft = cli.getClFT();
		mapClVT.put(clazzName, clvt);
		mapClFT.put(clazzName, clft);

		Method m[] = clazz.getMethods();
		int methodCount =  m.length;

		int maxLen = methodCount;
		if (supVt!=null) maxLen += supVt.len;
		try {
			for (JavaClass cl : clazz.getInterfaces()) {
				maxLen += cl.getMethods().length;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Error();
		}

		clvt.len = 0;
		clvt.key = new String[maxLen];
//		clvt.ptr = new int[maxLen];
		clvt.mi = new JopMethodInfo[maxLen];
		
		Field f[] = clazz.getFields();
		maxLen = f.length;
//		if (supFt!=null) maxLen += supFt.len;
		clft.len = 0;
		clft.key = new String[maxLen];
		clft.idx = new int[maxLen];
		clft.size = new int[maxLen];
		clft.isStatic = new boolean[maxLen];
		clft.isReference = new boolean[maxLen];
		
		

// System.out.println("// VT: "+clazzName);

		if (supVt!=null) {
			for (i=0; i<supVt.len; ++i) {
				clvt.key[i] = supVt.key[i];
// System.out.println("//super: "+clvt.key[i]);
				clvt.mi[i] = supVt.mi[i];
			}
			clvt.len = supVt.len;
		}
/*
		if (supFt!=null) {
			for (i=0; i<supFt.len; ++i) {
				// copy only the non static fields
				if (!supFt.isStatic[i]) {
					clft.key[clft.len] = supFt.key[i];
					clft.idx[clft.len] = supFt.idx[i];
					clft.size[clft.len] = supFt.size[i];
					clft.isStatic[clft.len] = supFt.isStatic[i];
					clft.isReference[clft.len] = supFt.isReference[i];
					++clft.len;
				}
			}
		}
*/

		for (i = 0; i < methodCount; i++) { 
			Method meth = m[i];
			String methodId = meth.getName()+meth.getSignature();
			JopMethodInfo mi = (JopMethodInfo) cli.getMethodInfo(methodId);
			
			for (j=0; j<clvt.len; ++j) {
				if (clvt.key[j].equals(methodId)) {					// override method
//System.out.println("override "+methodId);
					clvt.mi[j] = mi;
					break;
				}
			}
			if (j==clvt.len) {								// new method
				clvt.key[clvt.len] = methodId;
//System.out.println("new "+methodId);
				clvt.mi[clvt.len] = mi;
				++clvt.len;
			}
		}

		// add interface methods that are not actually implemented here
		try {
			for (i = 0; i < clazz.getInterfaces().length; i++) {
				OldClassInfo icli = ai.cliMap.get(clazz.getInterfaces()[i].getClassName());
				for (String methodId : icli.methods.keySet()) {
					for (j=0; j<clvt.len; ++j) {
						if (clvt.key[j].equals(methodId)) { // ok, we have the method
							break;
						}
					}
					if (j==clvt.len) { // method not found
						clvt.key[clvt.len] = methodId;
						clvt.mi[clvt.len] = (JopMethodInfo)icli.methods.get(methodId);
						++clvt.len;
					}				
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new Error();
		}

//System.out.println("The VT of "+clazzName);
//for (i=0; i<clvt.len; i++) { 
//	System.out.println("//\t"+clvt.meth[i].cli.clazz.getClassName()+"."+clvt.key[i]);
//}

		// this is strange!!!
		// BTW we copied only the non static fields....
		int nextFieldIndex = 0;
		int nextStaticIndex = 0;
/*
		for (j=0; j<clft.len; ++j) {
			int size = clft.size[j];
			if (clft.isStatic[j]) {
				nextStaticIndex += size;
			} else {
				nextFieldIndex += size;				
			}
		}
*/
		if (supFt!=null) {
			nextFieldIndex = supFt.instSize;
			for (i=0; i<supFt.len; ++i) {
				if (supFt.isStatic[i]) {
					if (supFt.idx[i]>=nextStaticIndex) {
						nextStaticIndex = supFt.idx[i]+1;
					}
/*
				} else {
					if (supFt.idx[i]>=nextFieldIndex) {
						nextFieldIndex = supFt.idx[i]+1;
					}
*/
				}
			}
		}

				
		for (i=0; i<f.length; ++i) {
			Field field = f[i];
			int size = field.getType().getSize();
	
			String fieldId = field.getName()+field.getSignature();
			
			clft.key[clft.len] = fieldId;
			clft.size[clft.len] = size;
			clft.isReference[clft.len] = field.getType() instanceof ReferenceType; 
			if (field.isStatic()) {
				clft.idx[clft.len] = nextStaticIndex;
				clft.isStatic[clft.len] = true;
				nextStaticIndex += size;
			} else {
				clft.idx[clft.len] = nextFieldIndex;
				clft.isStatic[clft.len] = false;
				nextFieldIndex += size;				
			}
			clft.len++;
		}
		// This is redundant!
		clft.instSize = nextFieldIndex;
		cli.setInstanceSize(nextFieldIndex);

	}

	private void buildIT(JavaClass clazz) {

		int i, j;

		OldClassInfo cli = (OldClassInfo) ai.cliMap.get(clazz.getClassName());

		Method m[] = clazz.getMethods();
		int methodCount =  m.length;

		//
		//	build global interface table
		//

		for (i = 0; i < methodCount; i++) { 
			Method meth = m[i];
			String methodId = meth.getName()+meth.getSignature();
			JopMethodInfo mi = (JopMethodInfo) cli.getMethodInfo(methodId);

			JopClassInfo.IT it = JopClassInfo.getITObject();
			it.nr = JopClassInfo.listIT.size();
			it.key = methodId;
// System.out.println("Add to IT: "+it.nr+" "+it.key);
			it.meth = mi;
			JopClassInfo.listIT.add(it);

		}
		
	}

}
