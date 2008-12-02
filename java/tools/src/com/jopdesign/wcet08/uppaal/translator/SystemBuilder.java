/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet08.uppaal.translator;

import java.util.Hashtable;
import java.util.Map.Entry;

import org.w3c.dom.Document;

import com.jopdesign.wcet08.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet08.uppaal.model.NTASystem;
import com.jopdesign.wcet08.uppaal.model.Template;
import com.jopdesign.wcet08.uppaal.model.XmlSerializationException;

/**
 * Builder for the system modeling the java program
 */
public class SystemBuilder {
	
	public static final String CLOCK = "t";
	public static final String INVOKE_CHAN = "invoke";
	public static final String RETURN_CHAN = "ret";
	public static final String MAX_CALL_STACK_DEPTH = "max_csd";
	public static final String NUM_METHODS = "num_methods";
	public static final String ACTIVE_METHOD = "active_method";
	public static final String CURRENT_CALL_STACK_DEPTH = "cur_csd";

	private NTASystem system;
	public NTASystem getNTASystem() { return system; }
	
	private Hashtable<Template,Integer> templates = new Hashtable<Template,Integer>();
	/**
	 * Create a new top-level UPPAAL System builder
	 * @param name               the name of the system
	 * @param readDelay          the read delay of the processor's memory
	 * @param writeDelay         the write delay of the processor's memory
	 * @param maxCallStackDepth  the maximal call stack depth
	 * @param numMethods         the number of methods of the program
	 */
	public SystemBuilder(String name,
						 int readDelay, int writeDelay, int maxCallStackDepth, int numMethods) {
		this.system = new NTASystem(name);
		initialize(readDelay, writeDelay, maxCallStackDepth, numMethods);
	}
	private void initialize(int readDelay, int writeDelay, int maxCallStackDepth, int numMethods) {
		system.appendDeclaration("clock " + CLOCK +";");
		system.appendDeclaration("const int " + MAX_CALL_STACK_DEPTH + " = "+maxCallStackDepth+";"); 
		system.appendDeclaration("const int " + NUM_METHODS + " = "+numMethods+";"); 
		system.appendDeclaration("chan "+INVOKE_CHAN+";");
		system.appendDeclaration("chan "+RETURN_CHAN+";");
		system.appendDeclaration("int[0,"+ NUM_METHODS+ "] "+ ACTIVE_METHOD  +";");
		system.appendDeclaration("int[0,"+ MAX_CALL_STACK_DEPTH +"] "+CURRENT_CALL_STACK_DEPTH+";");
	}
	
	public void buildSystem() {
		StringBuilder sys = new StringBuilder();
		/* Create processes */
		for(Entry<Template,Integer> tmpl : templates.entrySet()) {
			sys.append(String.format("M%s = %s() ;\n", 
					   tmpl.getValue(), tmpl.getKey().getId()));
		}
		/* Instantiate processes */
		sys.append("system ");
		
		/* Please change the first/else idiom by one similar to ruby's join  */
		boolean first = true;
		for(Entry<Template,Integer> tmpl : templates.entrySet()) {
			if(first) first = false;
			else      sys.append(", ");
			sys.append("M"+tmpl.getValue());
		}		
		sys.append(";\n");
		this.system.setSystem(sys.toString());
	}

	public void addTemplate(int procid, Template templ) throws DuplicateKeyException {
		this.templates.put(templ,procid);
		this.system.addTemplate(templ);
	}

	public Document toXML() throws XmlSerializationException {
		return this.system.toXML();
	}
}
