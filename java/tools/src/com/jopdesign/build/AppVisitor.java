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
 */
package com.jopdesign.build;


import org.apache.bcel.classfile.*;

/**
 * A BCEL type visitor to set ClassInfo. Shall be extended by
 * application specific visitors for easy access to ClassInfo.
 * 
 * @author Martin
 * @deprecated Use ClassVisitor instead
 */
public class AppVisitor extends EmptyVisitor {

	protected OldAppInfo ai;
	// should be private, but we need access in e.g. JOPizerVisitor
	protected OldClassInfo cli;
	protected JavaClass clazz;

	public AppVisitor(OldAppInfo ai) {
		this.ai = ai;
	}
	
	public void visitJavaClass(JavaClass clazz) {
		this.clazz = clazz;
		setCli((OldClassInfo) ai.cliMap.get(clazz.getClassName()));
	}

	protected void setCli(OldClassInfo cli) {
		this.cli = cli;
	}

	protected OldClassInfo getCli() {
		return cli;
	}

}
