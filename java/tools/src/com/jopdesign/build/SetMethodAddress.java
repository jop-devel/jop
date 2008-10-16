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

package com.jopdesign.build;

import org.apache.bcel.classfile.*;

/**
 * Set the start address of the method.
 * @author martin
 *
 */
public class SetMethodAddress extends JOPizerVisitor {

	private int addr;

	public SetMethodAddress(JOPizer jz) {
		super(jz);
		addr = jz.codeStart;
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);


		Method[] methods = clazz.getMethods();
		
		for(int i=0; i < methods.length; i++) {
			Method m = methods[i];
			String methodId = m.getName()+m.getSignature();
	        JopMethodInfo mi = (JopMethodInfo) getCli().getMethodInfo(methodId);
	        if(JOPizer.dumpMgci){
	          // GCRT: get number of words used for this method's GC
	          addr += GCRTMethodInfo.gcLength(mi);    
	        }

	        mi.setInfo(addr);
	        addr += mi.getLength();
		}
		
		((JOPizer) ai).pointerAddr = addr;
	}


}
