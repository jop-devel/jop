/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen

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
import org.apache.bcel.classfile.*;

/*
 * It calls the methods and the stack walker. Adapted from SetMethodInfo. 
 * @author rup, ms
 */
public class SetGCRTMethodInfo extends JOPizerVisitor {
  OldAppInfo jz;
	public SetGCRTMethodInfo(OldAppInfo jz) {
		super(jz);
		this.jz = jz; 
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);

		Method[] methods = clazz.getMethods();
		
		for(int i=0; i < methods.length; i++) {
			Method m = methods[i];
			String methodId = m.getName()+m.getSignature();
	    OldMethodInfo mi = getCli().getMethodInfo(methodId);
	    // GCRT: Walk the method
	    GCRTMethodInfo.stackWalker(mi);
		}
	}
}
