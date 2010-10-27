/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package com.jopdesign.build;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.apache.bcel.classfile.*;


import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

/**
 * The new version of ClassInfo
 * @author Martin Schoeberl
 *
 */
public class ClassInfo implements Serializable {
	
	/**
	 * Is invoked on cli map creation for additional information setting.
	 * 
	 * @author Martin Schoeberl
	 */
	protected class CliVisitor extends EmptyVisitor implements Serializable {
		
		private static final long serialVersionUID = 1L;

		protected Map<String, ClassInfo> map;
		protected ClassInfo cli;
	
		public CliVisitor(Map<String, ClassInfo> map) {
			this.map = map;
		}
		public void visitJavaClass(JavaClass clazz) {

			cli = map.get(clazz.getClassName());
			cli.superClass = map.get(clazz.getSuperclassName());
			if (clazz.getClassName().equals("java.lang.Object")) {
				// Object has no super class
				cli.superClass = null;
			} else {
				// add this ClassInfo as a known sub class to the super class
				cli.superClass.subClasses.add(map.get(clazz.getClassName()));
			}
		}
		
		public void visitMethod(Method method) {
			
			ClassInfo cli = (ClassInfo) this.cli;
			
			String methodId = method.getName()+method.getSignature();
	        if(!cli.methods.containsKey(methodId)) {
				MethodInfo mi1 = cli.newMethodInfo(methodId);
				cli.methods.put(methodId, mi1);
				cli.list.add(mi1);
			}
	        
	        MethodInfo mi = cli.getMethodInfo(methodId);
	        mi.setMethod(method);
	        
	        /* NOTE: don't set MethodGen here. You have to explicitly ask for it later,
	         * as changing bytecode will invalidate the MethodGen
	         */
		}

	}

	private static final long serialVersionUID = 1L;

	/**
	 * The BCEL class representation.
	 */
	public JavaClass clazz;

	/**
	 * Reference to the super class.
	 */
	public ClassInfo superClass;
	
	/**
	 * Set of sub classes.
	 */
	private Set<ClassInfo> subClasses = new HashSet<ClassInfo>();
	/**
	 * Back link to the application info
	 */
	public AppInfo appInfo;

	/**
	 * Map of method signatures to a MethodInfo.
	 */
	protected Map<String, MethodInfo> methods = new HashMap<String, MethodInfo>();

	/**
	 * Methods in a list ordered in the visit order.
	 */
	protected List<MethodInfo> list = new LinkedList<MethodInfo>();
	
	protected ClassInfo(JavaClass jc, AppInfo ai) {
		clazz = jc;
		appInfo = ai;
	}
	
	/**
	 * A dummy instance for the dispatch of newClassInfo() that
	 * creates the real ClassInfo sub type
	 */
	public static ClassInfo getTemplate() {
		return new ClassInfo(null, null);
	}

	/**
	 * Create ClassInfos and the map from class names to ClassInfo
	 * @param jc
	 * @return
	 */
	Map<String, ? extends ClassInfo> genClassInfoMap(JavaClass jc[], AppInfo ai) {
		Map<String, ClassInfo> map = new HashMap<String, ClassInfo>();
		for (int i=0; i<jc.length; ++i) {
			ClassInfo cli = newClassInfo(jc[i], ai);
			map.put(cli.clazz.getClassName(), cli);
		}
		// second iteration over all class infos for additional information setting
		CliVisitor v = newCliVisitor(map);
		Iterator<? extends ClassInfo> it = map.values().iterator();
		while (it.hasNext()) {
			JavaClass clz = it.next().clazz;
			new DescendingVisitor(clz, v).visit();
		}
		return map;
	}
	
	/**
	 * A funny version of a factory method to create ClassInfo
	 * types. Has to be overwritten by each sub-type.
	 * Wolfgang and Martin in SF, Saint Mary's Square
	 */
	public ClassInfo newClassInfo(JavaClass jc,AppInfo ai) {
		return new ClassInfo(jc, ai);
	}
	
	/**
	 * Another funny factory method.
	 * @param map
	 * @return
	 */
	public CliVisitor newCliVisitor(Map<String, ClassInfo> map) {
		return new CliVisitor(map);
	}
	
	/**
	 * And another funny factory.
	 * @param mid
	 * @return
	 */
	public MethodInfo newMethodInfo(String mid) {
		return new MethodInfo(this, mid);
	}
	
	public String toString() {
		return clazz.getClassName();
	}

	public MethodInfo getMethodInfo(String amth) {
		return methods.get(amth);
	}

	public Map<String, MethodInfo> getMethodInfoMap() {
		return methods;
	}

	/**
	 * Return the methods as list in the order they have been visited.
	 * @return
	 */
	public List<MethodInfo> getMethods() {
		return list;
	}

	/**
	 * Write the BCEL clazz to a class file. 'Stolen' from Stefan's libgraph.
	 * @param filename
	 * @throws IOException
	 */
	public void writeClassFile(String filename) throws IOException {
        File file = new File(filename);
        String parent = file.getParent();

        if(parent != null) {
            File dir = new File(parent);
            dir.mkdirs();
        }

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        // TODO: what happens if we have changed the class (gen)?
        // updateClass();
        clazz.dump(new DataOutputStream(out));		
	}

}
