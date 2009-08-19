/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet.jop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.ProjectConfig;

/** 
 * Build map for data addresses, provided by the linker
 * 
 * @author Benedikt Huber (benedikt.huber@gmail.com) 
 *
 */
public class LinkerInfo {
	public static class LinkInfo {
		private ClassInfo klass;
		private int clinfoAddress;
		private int constsAddress;
		private Map <String,Integer> staticAddresses;
		private Map <Integer,Integer> constMap = new TreeMap<Integer,Integer>();
		
		public LinkInfo(ClassInfo ci, int clinfoAddress, int constsAddress) {
			this.klass = ci;
			this.clinfoAddress = clinfoAddress;
			this.constsAddress = constsAddress;
			this.staticAddresses = new HashMap<String,Integer>();
		}
		public ClassInfo getTargetClass() {
			return klass;
		}
		public int getClassInfoAddress() {
			return clinfoAddress;
		}
		public int getMTabAddress() {
			return clinfoAddress + 5;
		}
		public int getMTabLength() {
			return constsAddress - getMTabAddress();
		}
		public int getConstTableAddress() {
			return constsAddress;
		}
		public void setStaticFieldAddress(String name, int address) {
			if(staticAddresses.containsKey(name)) {
				throw new AssertionError("LinkerInfo.addStaticLinkInfo: Double entry for "+klass+"."+name);
			}
			staticAddresses.put(name, address);			
		}
		public int getStaticFieldAddress(String name) {
			return staticAddresses.get(name);
		}
		@Override
		public String toString() {
			return "LinkInfo@"+this.klass.clazz.getClassName()+".LinkInfo: CLINFO @ "+clinfoAddress+
			       ", CONSTANTS @ "+constsAddress + ", " + staticAddresses.size() + " static fields";
		}
		public Integer getConstAddress(int constIndex) {
			if(! constMap.containsKey(constIndex)) return null;
			return(constsAddress + constMap.get(constIndex) + 1);
		}
		public void parseInfo(String[] tks) {
			String key = tks[0];
			if(key.equals("-constmap")) {
				int keyIx = Integer.parseInt(tks[1]);
				int valIx = Integer.parseInt(tks[2]);
				constMap.put(keyIx,valIx);
			} else {
				throw new AssertionError("Bad format for class info: "+Arrays.toString(tks));
			}
		}
	}
	private Project project;
	private Map<String, LinkInfo> classLinkInfo;
	
	public Map<String, LinkInfo>getClassLinkInfo() {
		return classLinkInfo;
	}

	public LinkerInfo(Project p) {
		this.project = p;
	}
	
	public void loadLinkInfo() throws IOException, ClassNotFoundException {
		classLinkInfo = new HashMap<String, LinkInfo>();
		readClassLinkInfo(project.getProjectConfig().getClassLinkInfoFile());
		readStaticLinkInfo(project.getProjectConfig().getStaticLinkInfoFile());
//		for(LinkInfo li : classLinkInfo.values()) {
//			System.out.println("  "+li);
//		}
	}

	/** Read link info for classes
	 *  Format: {@code class clinfoAddress constantsAddress} 
	 * @throws IOException if an IOError occurs while reading the file
	 * @throws ClassNotFoundException if a specified class could not be found */
	private void readClassLinkInfo(File classLinkInfoFile) 
			throws IOException, ClassNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader(classLinkInfoFile));		
		String l = br.readLine();
		while(l != null) {
			String tks[] = l.split("\\s+");
			String classname = tks[0];
			ClassInfo klass = project.getWcetAppInfo().getClassInfo(classname);
			if(klass == null) throw new ClassNotFoundException(classname);
			LinkInfo linkInfo =
				new LinkInfo(klass,Integer.parseInt(tks[1]),Integer.parseInt(tks[2]));
			classLinkInfo.put(classname, linkInfo);
			while((l=br.readLine()) != null) {
				l = l.trim();
				if(! l.startsWith("-")) break;
				linkInfo.parseInfo(l.split("\\s"));
			}
		}
		br.close();
	}

	/** Read link info for static data
	 *  Format: {@code constantName address}
	 * @param staticLinkInfoFile
	 * @return a map from classes to (name -> address) maps
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	private void readStaticLinkInfo(File staticLinkInfoFile) throws NumberFormatException, IOException, ClassNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader(staticLinkInfoFile));		
		String l;
		while((l = br.readLine()) != null) {
			
			String tks[] = l.split("\\s+");
			String nameParts[] = ProjectConfig.splitClassName(tks[0]);
			ClassInfo cli = project.getWcetAppInfo().getClassInfo(nameParts[0]);
			if(cli == null) throw new ClassNotFoundException(tks[0]);
			String objectName = nameParts[1];
			int address = Integer.parseInt(tks[1]);
			LinkInfo linkInfo = getLinkInfo(cli);
			if(linkInfo == null) throw new ClassNotFoundException("No link info for" + cli);
			linkInfo.setStaticFieldAddress(objectName, address);
		}
		br.close();
	}

	public LinkInfo getLinkInfo(ClassInfo cli) {
		return classLinkInfo.get(cli.clazz.getClassName());
	}

	public Integer getStaticFieldAddress(String className, String fieldNameAndSig) {
		LinkInfo info = classLinkInfo.get(className);
		if(info == null) throw new AssertionError("No linker info for "+className);
		return info.getStaticFieldAddress(fieldNameAndSig);
	}
	
}
