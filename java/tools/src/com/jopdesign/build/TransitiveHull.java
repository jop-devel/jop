/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) Markus Dahm
  Copyright (C) 2005,2006, Martin Schoeberl (martin@jopdesign.com)

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


import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.ClassQueue;
import org.apache.bcel.util.ClassSet;
import org.apache.bcel.util.SyntheticRepository;

import java.util.Arrays;

/**
 * Find all classes referenced by given start class and all classes
 * referenced by tjose and so on. In other words: Compute the tranitive
 * hull of classes used by a given class. This is done by checking all
 * ConstantClass entries and all method and field signatures.<br> This
 * may be useful in order to put all class files of an application
 * into a single JAR file.
 * <p>
 * It fails however in the presence of reflection code.
 *
 * @author	<A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @deprecated
 */
public class TransitiveHull extends org.apache.bcel.classfile.EmptyVisitor {
	
	private ClassQueue	 _queue;
	private ClassSet		 _set;
	private ConstantPool _cp;

	private String[] excluded = {};


	public TransitiveHull(JavaClass clazz) {
		_queue = new ClassQueue();
		_queue.enqueue(clazz);
		_set = new ClassSet();
		_set.add(clazz);
	}
	
	public TransitiveHull(JavaClass[] clazz) {
		
		_queue = new ClassQueue();
		_set = new ClassSet();
		for (int i=0; i<clazz.length; ++i) {
			_queue.enqueue(clazz[i]);
			_set.add(clazz[i]);		
		}
	}

	public TransitiveHull(ClassPath classpath, JavaClass[] clazz) {
		
		Repository.setRepository(SyntheticRepository.getInstance(classpath));
		_queue = new ClassQueue();
		_set = new ClassSet();
		for (int i=0; i<clazz.length; ++i) {
			_queue.enqueue(clazz[i]);
			_set.add(clazz[i]);		
		}
	}

	public JavaClass[] getClasses() {
		return _set.toArray();
	}

	public String[] getClassNames() {
		return _set.getClassNames();
	}
	
	public void setExcluded(String[] names) {
		excluded = names;
	}

	/**
	 * Start traversal using DescendingVisitor pattern.
	 */
	public void start() {
		while(!_queue.empty()) {
			JavaClass clazz = _queue.dequeue();
			_cp = clazz.getConstantPool();

			new org.apache.bcel.classfile.DescendingVisitor(clazz, this).visit();
		}
	}

	private void add(String class_name) {
		class_name = class_name.replace('/', '.');

		// not used for JOPizer and matches not part
		// of JDK 1.3
//		for(int i = 0; i < _ignored.length; i++) {
//			if(class_name.matches(_ignored[i])) {
//				return; // Ihh
//			}
//		}
		for (int i=0; i<excluded.length; ++i) {
			if (excluded[i].equals(class_name)) {
				return;
			}
		}
		// we ignore array classes
		if (class_name.startsWith("[")) {
			return;
		}

		JavaClass clazz;
		try {
			clazz = Repository.lookupClass(class_name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new Error();
		}

		if (clazz == null) {
			System.out.println("lookupClass("+class_name+") failed in TransitiveHull");
			System.exit(1);
		}

		if(clazz != null && _set.add(clazz)) {
			_queue.enqueue(clazz);
		}
	}

	public void visitConstantClass(ConstantClass cc) {
		String class_name = (String)cc.getConstantValue(_cp);
		add(class_name);
	}

	private void visitRef(ConstantCP ccp, boolean method) {
		String class_name = ccp.getClass(_cp);
		add(class_name);

		ConstantNameAndType cnat = (ConstantNameAndType)_cp.
			getConstant(ccp.getNameAndTypeIndex(), Constants.CONSTANT_NameAndType);

		String signature = cnat.getSignature(_cp);

		if(method) {
			Type type = Type.getReturnType(signature);

			if(type instanceof ObjectType) {
				add(((ObjectType)type).getClassName());
			}

			Type[] types = Type.getArgumentTypes(signature);

			for(int i = 0; i < types.length; i++) {
				type = types[i];
				if(type instanceof ObjectType) {
					add(((ObjectType)type).getClassName());
				}
			}
		} else {
			Type type = Type.getType(signature);
			if(type instanceof ObjectType) {
				add(((ObjectType)type).getClassName());
			}
		}
	}

	public void visitConstantMethodref(ConstantMethodref cmr) {
		visitRef(cmr, true);
	}

	public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref cimr) {
		visitRef(cimr, true);
	}

	public void visitConstantFieldref(ConstantFieldref cfr) {
		visitRef(cfr, false);
	}

	public static void main(String[] argv) {
		JavaClass java_class;

		try {
			if(argv.length == 0) {
				System.err.println("transitive: No input files specified");
			} else {
				java_class = Repository.lookupClass(argv[0]);
				TransitiveHull hull = new TransitiveHull(java_class);
				hull.start();
				System.out.println(Arrays.asList(hull.getClassNames()));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
