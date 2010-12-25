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

import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The new version of ClassInfo
 * @author Martin Schoeberl
 * @deprecated
 */
public class OldClassInfo implements Serializable {
	
	/**
	 * Is invoked on cli map creation for additional information setting.
	 * 
	 * @author Martin Schoeberl
	 */
	protected class CliVisitor extends EmptyVisitor implements Serializable {
		
		private static final long serialVersionUID = 1L;

		protected Map<String, OldClassInfo> map;
		protected OldClassInfo cli;
	
		public CliVisitor(Map<String, OldClassInfo> map) {
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
			
			OldClassInfo cli = (OldClassInfo) this.cli;
			
			String methodId = method.getName()+method.getSignature();
	        if(!cli.methods.containsKey(methodId)) {
				OldMethodInfo mi1 = cli.newMethodInfo(methodId);
				cli.methods.put(methodId, mi1);
				cli.list.add(mi1);
			}
	        
	        OldMethodInfo mi = cli.getMethodInfo(methodId);
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
	public OldClassInfo superClass;
	
	/**
	 * Set of sub classes.
	 */
	private Set<OldClassInfo> subClasses = new HashSet<OldClassInfo>();
	/**
	 * Back link to the application info
	 */
	public OldAppInfo appInfo;

	/**
	 * Map of method signatures to a MethodInfo.
	 */
	protected Map<String, OldMethodInfo> methods = new HashMap<String, OldMethodInfo>();

	/**
	 * Methods in a list ordered in the visit order.
	 */
	protected List<OldMethodInfo> list = new LinkedList<OldMethodInfo>();
	
	protected OldClassInfo(JavaClass jc, OldAppInfo ai) {
		clazz = jc;
		appInfo = ai;
	}
	
	/**
	 * A dummy instance for the dispatch of newClassInfo() that
	 * creates the real ClassInfo sub type
	 */
	public static OldClassInfo getTemplate() {
		return new OldClassInfo(null, null);
	}

	/**
	 * Create ClassInfos and the map from class names to ClassInfo
	 * @param jc
	 * @return
	 */
	Map<String, ? extends OldClassInfo> genClassInfoMap(JavaClass jc[], OldAppInfo ai) {
		Map<String, OldClassInfo> map = new HashMap<String, OldClassInfo>();
		for (int i=0; i<jc.length; ++i) {
			OldClassInfo cli = newClassInfo(jc[i], ai);
			map.put(cli.clazz.getClassName(), cli);
		}
		// second iteration over all class infos for additional information setting
		CliVisitor v = newCliVisitor(map);
		Iterator<? extends OldClassInfo> it = map.values().iterator();
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
	public OldClassInfo newClassInfo(JavaClass jc,OldAppInfo ai) {
		return new OldClassInfo(jc, ai);
	}
	
	/**
	 * Another funny factory method.
	 * @param map
	 * @return
	 */
	public CliVisitor newCliVisitor(Map<String, OldClassInfo> map) {
		return new CliVisitor(map);
	}
	
	/**
	 * And another funny factory.
	 * @param mid
	 * @return
	 */
	public OldMethodInfo newMethodInfo(String mid) {
		return new OldMethodInfo(this, mid);
	}
	
	public String toString() {
		return clazz.getClassName();
	}

	public OldMethodInfo getMethodInfo(String amth) {
		return methods.get(amth);
	}

	public Map<String, OldMethodInfo> getMethodInfoMap() {
		return methods;
	}

	/**
	 * Return the methods as list in the order they have been visited.
	 * @return
	 */
	public List<OldMethodInfo> getMethods() {
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
