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
package com.jopdesign.build_ok;


import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.*;
import java.io.*;
import java.util.*;
import org.apache.bcel.Constants;
import org.apache.bcel.Repository;

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
 */
public class TransitiveHull extends org.apache.bcel.classfile.EmptyVisitor {
	
	private ClassPath classpath;
	private JavaClass		_class;
	private ClassQueue	 _queue;
	private ClassSet		 _set;
	private ConstantPool _cp;

	private String[] _ignored = {
/*
		"java[.].*",
		"javax[.].*",
		"com[.]sun[.].*"
*/
	};


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
		
		this.classpath = classpath;
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

	/**
	 * Start traversal using DescendingVisitor pattern.
	 */
	public void start() {
		while(!_queue.empty()) {
			JavaClass clazz = _queue.dequeue();
			_class = clazz;
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
		// we ignore array classes
		if (class_name.startsWith("[")) {
			return;
		}

		JavaClass clazz = null;
		if (classpath==null) {
			clazz = Repository.lookupClass(class_name);
		} else {
			InputStream is;
			try {
				is = classpath.getInputStream(class_name);
				clazz = new ClassParser(is, class_name).parse();			
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
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
		ClassParser parser=null;
		JavaClass	 java_class;

		try {
			if(argv.length == 0) {
				System.err.println("transitive: No input files specified");
			} else {
				if((java_class = Repository.lookupClass(argv[0])) == null) {
					java_class = new ClassParser(argv[0]).parse();
				}
			
				TransitiveHull hull = new TransitiveHull(java_class);
			
				hull.start();
				System.out.println(Arrays.asList(hull.getClassNames()));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
