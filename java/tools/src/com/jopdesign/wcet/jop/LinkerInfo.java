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

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.wcet.WCETTool;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Build map for data addresses, provided by the linker
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 * FIXME: [processor-model] Abstract LinkerInfo (even if jamuth support will never be accomplished)
 */
public class LinkerInfo {
	public class LinkInfo {
		private ClassInfo klass;
		private int clinfoAddress;
		private int constsAddress;
		private int instSize;
		private int superAddress;

		private Map <String,Integer> staticAddresses = new HashMap<String,Integer>();
		private Map <String,Integer> fieldOffsets = new HashMap<String,Integer>();    // ID (name+signature) to field index */
		private Map <String,Integer> codeAddresses = new HashMap<String,Integer>();
		private Map <String,Integer> mtabAddresses = new HashMap<String,Integer>();
		private Map <Integer,Integer> constMap = new TreeMap<Integer,Integer>();
		private Map<String, Integer> superClassFields = null; // loaded on demand

		public LinkInfo(ClassInfo ci, int mtabAddress, int constsAddress) {
			this.klass = ci;
			this.clinfoAddress = mtabAddress - 5;
			this.constsAddress = constsAddress;
			this.superAddress = -1;
			this.instSize = -1;
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
		
		public int getSuperAddress() {
			return superAddress;
		}
		
		private <K,V> void addAddress(String ctx, Map<K,V> amap, K key, V value) {
			if(amap.containsKey(key)) {
				throw new AssertionError("LinkerInfo.setAddress"+ctx+": Double entry for "+klass+"."+key);
			}
			amap.put(key, value);
		}

		private void setStaticFieldAddress(String name, int address) {
			addAddress("StaticAddresses",staticAddresses,name,address);
		}
		
		private void setCodeAddress(String name, int address) {
			addAddress("CodeAddresses",codeAddresses,name,address);
		}
		
		public int getInstanceSize() {
			return this.instSize;
		}
		
		public int getStaticFieldAddress(String name) {
			return staticAddresses.get(name);
		}
		
		public Integer getCodeAddress(String name) {
			return codeAddresses.get(name);
		}
		
		public Integer getMTabAddress(String name) {
			return mtabAddresses.get(name);
		}

		@Override
		public String toString() {
			return "LinkInfo "+this.klass.getClassName()+" "+clinfoAddress;
		}

		public void dump(PrintStream out) {
			StringBuilder sb = new StringBuilder();
			dump(sb);
			out.println(sb);
		}
		
		public void dump(StringBuilder sb) {
			sb.append("LinkInfo: "+this.klass.getClassName()+"\n");
			sb.append("  instSize: "+this.instSize+"\n");
			sb.append("  classInfo @ "+this.clinfoAddress+"\n");
			sb.append("  mtab @ "+this.getMTabAddress()+"\n");
			sb.append("  cpool @ "+this.constsAddress+"\n");

			sb.append("  Static Addresses"+"\n");
			for(Entry<String, Integer> entry : staticAddresses.entrySet()) {
				sb.append("    " + entry.getKey() + "  ==>  " + entry.getValue()+"\n");
			}
			sb.append("  Code Addresses"+"\n");
			for(Entry<String, Integer> entry : codeAddresses.entrySet()) {
				sb.append("    " + entry.getKey() + "  ==>  " + entry.getValue()+"\n");
			}
			sb.append("  MTab Addresses"+"\n");
			for(Entry<String, Integer> entry : mtabAddresses.entrySet()) {
				sb.append("    " + entry.getKey()+"  ==>  " + entry.getValue()+"\n");
			}
		}

		public Integer getConstAddress(int constIndex) {
			if(! constMap.containsKey(constIndex)) return null;
			return(constsAddress + constMap.get(constIndex) + 1);
		}
		
		public int getFieldOffset(String fieldName) {
			loadSuperClassFields();
			if(! fieldOffsets.containsKey(fieldName)) {
				Logger.getLogger(LinkerInfo.class).error("No offset field for '"+fieldName+"' in "+klass+" only "+fieldOffsets);
				return 0; 
			}
			return(fieldOffsets.get(fieldName));
		}

		private Map<String, Integer> getFieldOffsets() {
			loadSuperClassFields();
			return fieldOffsets;
		}

		private void loadSuperClassFields() {
			if(this.superClassFields != null) return;
			if(this.klass.getSuperClassInfo() == null) return;
			LinkInfo superLinkInfo;
			try {
				superLinkInfo = getOrCreateLinkInfo(klass.getSuperClassName());
			} catch (ClassNotFoundException ignored) {
				throw new AssertionError("Superclass not found: "+klass.getSuperClassName());
			}
			superClassFields = superLinkInfo.getFieldOffsets();
			this.fieldOffsets.putAll(superClassFields);
		}


		public void parseInfo(String[] tks) {
			String key = tks[0];
			if(key.equals("-constmap")) {
				int keyIx = Integer.parseInt(tks[1]);
				int valIx = Integer.parseInt(tks[2]);
				constMap.put(keyIx,valIx);
			} else if(key.equals("-super")) {
				this.superAddress = Integer.parseInt(tks[1]);
			} else if(key.equals("-instSize")) {
				this.instSize = Integer.parseInt(tks[1]);
			} else if(key.equals("-field")) {
				addAddress("FieldOffsets", fieldOffsets, tks[1], Integer.parseInt(tks[2]));
			} else if (key.equals("-mtab")) {
				MemberID nameParts = MemberID.parse(tks[1], true);
				int valIx = Integer.parseInt(tks[2]);
				addAddress("MTabAddresses",mtabAddresses,nameParts.getMethodSignature(), valIx);
			} else {
				throw new AssertionError("Bad format for class info (Unknown key: '" + key + "')" + Arrays.toString(tks));
		    }
		}

	}
	
	private WCETTool project;
	private Map<String, LinkInfo> classLinkInfo;

	public Map<String, LinkInfo> getClassLinkInfo() {
		return classLinkInfo;
	}

	public LinkerInfo(WCETTool p) {
		this.project = p;
	}
	
	/** Load the linker info.
	 * Currently, we support the following entries in the Link file:
	 * <ul><li/>{@code static} fully-qualified-static-field-name address
	 *     <li/>{@code bytecode} fully-qualified-method-name bytecode-start
	 *     <li/>{@code class} class-name mtab-start cpool-start <br/>
	 *       with subinfo
	 *     <ul><li/> {@code -super} super-class-address
	 *         <li/> {@code -mtab} method-name mtab-address
	 *         <li/> {@code -constmap} constant-classfile-index constant-actual-index
	 *         <li/> {@code -field} field-name field-offset
	 *     </ul>
	 * </ul>
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadLinkInfo() throws IOException, ClassNotFoundException {
		//File jopFile = project.getProjectConfig().getBinaryFile());

//		for(LinkInfo li : classLinkInfo.values()) {
//			System.out.println("  "+li);
//		}
		classLinkInfo = new HashMap<String, LinkInfo>();
		BufferedReader br = new BufferedReader(new FileReader(project.getProjectConfig().getLinkInfoFile()));
		String l = br.readLine();
		try {
			while(l != null) {
				LinkInfo linkInfo;
				String[] tks = l.split("\\s+");
				if(tks[0].equals("static") || tks[0].equals("bytecode")) {
					MemberID nameParts = MemberID.parse(tks[1],true);
					linkInfo = getOrCreateLinkInfo(nameParts.getClassName());
					String objectName = nameParts.getMethodSignature();
					int address = Integer.parseInt(tks[2]);
					if(tks[0].equals("bytecode")) {
						linkInfo.setCodeAddress(objectName, address);
					} else {
						linkInfo.setStaticFieldAddress(objectName, address);
					}
				} else if(tks[0].equals("class")) {
					String classname = tks[1];
					linkInfo = getOrCreateLinkInfo(classname);
					linkInfo.clinfoAddress = Integer.parseInt(tks[2]) - 5;
					linkInfo.constsAddress = Integer.parseInt(tks[3]);
				} else {
					throw new IOException("Bad format in link info file: "+l);
				}
				while((l=br.readLine()) != null) {
					l = l.trim();
					if(! l.startsWith("-")) break;
					linkInfo.parseInfo(l.split("\\s"));
				}
			}
		} finally {
			br.close();
		}
	}

	private LinkInfo getOrCreateLinkInfo(String classname) throws ClassNotFoundException {
		ClassInfo klass = project.getAppInfo().getClassInfo(classname);
		if(klass == null) throw new ClassNotFoundException(classname);
		LinkInfo linkInfo = classLinkInfo.get(classname);
		if(linkInfo == null) {
			linkInfo = new LinkInfo(klass, 0, 0);
			classLinkInfo.put(classname,linkInfo);
		}
		return linkInfo;
	}

	public LinkInfo getLinkInfo(ClassInfo cli) {
		return classLinkInfo.get(cli.getClassName());
	}

	public Integer getStaticFieldAddress(String className, String fieldName) {
		LinkInfo li;
		try {
			li = this.getOrCreateLinkInfo(className);
		} catch (ClassNotFoundException ignored) {
			return null;
		}
		return li.getStaticFieldAddress(fieldName);
	}

	/**
	 * Compute the (physical) index of a given field (using linker infos)
     * @param klassName
     * @param fieldName
     * @return
	 */
	public int getFieldIndex(String klassName, String fieldName)
	{
		return classLinkInfo.get(klassName).getFieldOffset(fieldName);
	}

	public void dump(PrintStream out) {
		for(Entry<String, LinkInfo> linkInfo : project.getLinkerInfo().getClassLinkInfo().entrySet()) {
			linkInfo.getValue().dump(System.out);
		}
	}

}
