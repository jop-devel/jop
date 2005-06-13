/*
 * Created on Dec 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package com.jopdesign.build;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Falvius
 *
 */
public class StringInfo {
	
	final static int STR_OBJ_LEN = 2;

	public static HashMap usedStrings = new HashMap();
	public static List list = new LinkedList();
	public static ClassInfo cli;
	public static int stringTableAddress = -1;
	public static int length = 0;

	/**
	 * relative address to start of the String table
	 */
	public int startAddress;
	public String string;
	
	public static void addString(String s) {
		if(usedStrings.containsKey(s)) return;
		// System.err.println("// Constant String: " + s);
		StringInfo si = new StringInfo(s, length);
		usedStrings.put(s, si);
		list.add(si);
		
		if (JOPizer.useHandle) {
			/* flavius version
			length += HANDLE_Words; // jump the string handle
			length += HEADER_Words; // jump the string object header
			length += STRING_Words; // jump the string object
			length += HANDLE_Words; // jump the char[] handle
			length += HEADER_Words; // jump the array header
			length += s.length();   // jump the array contents
			*/
			length += STR_OBJ_LEN+1+s.length();
			length += 2;	// two handles, no GC info for now
		} else {
			length += STR_OBJ_LEN+1+s.length();
		}
	}
	public static StringInfo getStringInfo(String s) {
		StringInfo si = (StringInfo) usedStrings.get(s);
		return si;
	}
	
	/**
	 * Get the address of the String object.
	 * Internal startAddress points to the beginning of the object (the mtab pointer),
	 * but the returned reference is the the pointer to the first field.
	 * @return
	 */
	public int getAddress() {
		if (JOPizer.useHandle) {
			return startAddress;
		} else {
			return startAddress+1;			
		}
	}


	// FIXME Should actually include ObjectImage
	//	   to be common to GC
	// the number of words needed before the actual pointer
	// (or reference) to the string
	// these are part of an actual handle
	public static final int PREHANDLE_Words = 1;
	// words needed in any object header
	public static final int HEADER_Words = 2; // changed from 4; 
				// (MP moved to the handle, GCIptr moved to the class info)
	// string object size (no header)
	// ... it is just a value -> handle to a char array
	public static final int STRING_Words = 1;
	
	// handle size
	public static final int HANDLE_Words = 2;
	
	/*
	public int getHandle() {
		return startAddress + PREHANDLE_Words;
	}
	*/
	
	public StringInfo(String s, int addr) {
		string = s;
		startAddress = addr;
	}
	
	
	private final static int maxCom = 20;
	
	public String getSaveString() {
		
		StringBuffer sb = new StringBuffer("\"");
		for (int i=0; i<string.length() && i<maxCom; ++i) {
			char ch = string.charAt(i);
			if (ch<' ' || ch>'~') {
				sb.append('\\');
				if (ch=='\r') sb.append("r");
				else if (ch=='\n') sb.append("n");
				else {
					sb.append('0');
					sb.append(Integer.toOctalString((int) ch));
				}			
			} else {
				sb.append(ch);
			}
		}
		if (string.length() > maxCom) { 
			sb.append("...");
		}
		sb.append("\"");
		
		return sb.toString();
	}

//	private void commentary(String s, int addrCnt, CCodeWriter out ) {
	private void commentary(String s, int addrCnt, PrintWriter out) {
		out.println("\t//\t"+addrCnt+"\t"+getSaveString());
	}
	
	public void dump(PrintWriter out, ClassInfo strcli, int arrygcinfo) {
		if (JOPizer.useHandle) {
			int addr = stringTableAddress+startAddress;
			commentary(string, addr, out);
			out.println("\t"+(addr+3)+",\t//\tString handle points to the first field");
			out.println("\t"+(addr+5)+",\t//\tchar[] handle points to the first element");
			out.println("\t"+strcli.methodsAddress+",\t//\t pointer to String mtab ");
			out.println("\t"+(addr+1)+",\t//\tchar ref. points to char[] handle");
			
			/* flavius version
//			String disp = string.replaceAll("\n","/n");
//			out.println("// ADDR:"+startAddress+" Constant String: \""+disp+"\"");
			commentary(string, startAddress, out);   		
			out.println("// Handle :"+getHandle());
			// write out the string handle
			int objptr = getHandle()+HEADER_Words+1;
			out.println("\t"+(strcli.methodsAddress-2)+", \t// String class info"); 
			out.println("\t"+objptr+", \t// instance ptr");
			// write the string object
			// out.println("\t"+strcli.methodsAddress+",\t// methods pointer");
			// out.println("\t"+strcli.gcinfoAddress+",\t// gc info address");
			out.println("\t"+getHandle()+",\t// handle");
			out.println("\t"+STRING_Words+",\t// object size");
			int charptr = objptr+STRING_Words+PREHANDLE_Words;
			out.println("\t"+charptr+",\t// string content handle");
			out.println("\t"+arrygcinfo+", \t// non-ref array fake class info");
			out.println("\t"+(objptr+STRING_Words+HANDLE_Words+HEADER_Words)+",\t// char[] pointer");
			// write also the header for the array!
			// out.println("\t0,\t// methods pointer - not used for arrays");
			// out.println("\t"+arrygcinfo+",\t// gc info for arrays of non-refs");
			out.println("\t"+charptr+",\t// handle");
			*/
		} else {
			out.print("\t"+strcli.methodsAddress+", "+
					(stringTableAddress+startAddress+STR_OBJ_LEN+1)+",");
			commentary(string, stringTableAddress+startAddress, out);   		
			
		}

		// out.println("\t"+string.length()+",\t// array length");
		out.println("\t"+string.length()+",");
		byte chrsp[] = string.getBytes();
		out.print("\t");
		for(int i=0;i<chrsp.length;i++) {
			out.print(chrsp[i]+", ");
			if ((i&0x07)==7) {
				out.println();
				out.print("\t");
			}
		}
		out.println();

	}
}
