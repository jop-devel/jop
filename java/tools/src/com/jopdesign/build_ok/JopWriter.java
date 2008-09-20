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

/*
 * Created on 05.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.jopdesign.build_ok;

import java.io.*;
import java.util.*;


/**
 * @author Falvius, Martin
 */
public class JopWriter {

	private JOPizer jz;
	private PrintWriter out;
	

	public JopWriter(JOPizer jz) {
		this.jz = jz;
		out = jz.out;
	}
	
	public void write() {
		
		out.print("\t"+jz.length+",");
		out.println("//\tlength of the application in words (including this word)");
		out.print("\t"+jz.pointerAddr+",");
		out.println("//\tpointer to special pointers = address of 'special' pointer");
		
		System.out.println(((jz.pointerAddr*4-8+512)/1024)+"KB instruction");
			
		int nrOfMethods = dumpByteCode();
		
		System.out.println(nrOfMethods+" number of methods");
		
		out.println("//");
		out.println("//\tspecial pointer at "+jz.pointerAddr+":");
		out.println("//");
		out.println("\t\t"+ClassInfo.bootAddress+",\t// pointer to boot method struct: "+
				JOPizer.startupClass+":"+JOPizer.bootMethod);
//		int jvmptr = appinfo.fistNonObjectMethod(AppInfo.jvmClass);
//		int helpptr = appinfo.fistNonObjectMethod(AppInfo.helpClass);

		out.println("\t\t"+ClassInfo.jvmAddress+",\t// pointer to first non Object method struct of class JVM");
		out.println("\t\t"+ClassInfo.jvmHelpAddress+",\t// pointer to first non Object method struct of of class JVMHelp");
		out.println("\t\t"+ClassInfo.mainAddress+",\t// pointer to main method struct");
		out.println("\t\t"+ClassInfo.addrRefStatic+",\t// pointer to static reference fields");
		out.println("\t\t"+ClassInfo.cntRefStatic+",\t// number of static reference fields");

		if (ClassInfo.mainAddress==0 || ClassInfo.mainAddress==-1) {
			System.out.println("Error: no main() method found");
			System.exit(-1);
		}
		
		dumpClinit();
		out.println("//TODO: GC info");
		
		dumpStrings();
		
		dumpStaticFields();

		dumpClassInfo();
		
		out.close();

	}

	private int dumpByteCode() {
		
		int cnt = 0;
		Iterator it = jz.clazzes.iterator();
		while (it.hasNext()) {
			ClassInfo cli = (ClassInfo) it.next();
			
			out.println("//\t"+cli.clazz.getClassName());
			List methods = cli.getMethods();
			
			for(int i=0; i < methods.size(); i++) {
				if(JOPizer.dumpMgci){
				  // GCRT: dump the words before the method bytecode
				  GCRTMethodInfo.dumpMethodGcis(((MethodInfo) methods.get(i)), out);
				}
				
				((MethodInfo) methods.get(i)).dumpByteCode(out);
				++cnt;
			}
		}
		return cnt;
	}
	
	private void dumpStaticFields() {
		
		Iterator it;
		// dump the static value fields
		it = jz.clazzes.iterator();
		while (it.hasNext()) {
			ClassInfo cli = (ClassInfo) it.next();
			cli.dumpStaticFields(out, false);			
		}	
		// dump the static ref fields
		it = jz.clazzes.iterator();
		while (it.hasNext()) {
			ClassInfo cli = (ClassInfo) it.next();
			cli.dumpStaticFields(out, true);			
		}	
	}
	
	private void dumpClassInfo() {
		
		Iterator it;
		it = jz.clazzes.iterator();
		while (it.hasNext()) {
			ClassInfo cli = (ClassInfo) it.next();
			cli.dump(out);			
		}	

	}

	private void dumpClinit() {
		
		out.println("//");
		out.println("//\t<clinit> pointer to method struct");
		out.println("//");
		out.println("\t\t"+MethodInfo.clinitList.size()+",\t//\tnumber of methods");
		Iterator it = MethodInfo.clinitList.iterator();
		while (it.hasNext()) {
			MethodInfo mi = ((ClassInfo) it.next()).getMethodInfo(JOPizer.clinitSig);
			out.println("\t\t"+mi.structAddress+",\t//\t"+mi.cli.clazz.getClassName());
		}
	}
 
	private void dumpStrings() {
		// find the string class
		ClassInfo strcli = StringInfo.cli;
		out.println("//");
		out.println("//\tString table: "+StringInfo.usedStrings.size()+" strings");
		out.println("//");
		out.println("//\tfirst a String object (with pointer to mtab and pointer to char arr.)");
		out.println("//\tfollowed by a character array (len + data)");
		out.println("//");
		out.println("//\t"+strcli.methodsAddress+"\tpointer to method table of class String");
		out.println("//");
		Iterator i = StringInfo.list.iterator();
		while(i.hasNext()) {
			StringInfo si = (StringInfo)i.next();
			si.dump(out, strcli, StringInfo.stringTableAddress+JOPizer.CLASSINFO_NONREFARRY);
		}
	}
}
