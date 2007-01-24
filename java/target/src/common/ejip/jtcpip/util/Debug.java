/* 
 * Copyright  (c) 2006-2007 Graz University of Technology. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The names "Graz University of Technology" and "IAIK of Graz University of
 *    Technology" must not be used to endorse or promote products derived from
 *    this software without prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE LICENSOR BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY  OF SUCH DAMAGE.
 */

package ejip.jtcpip.util;

//import java.io.BufferedReader;

/**
 * Some useful static methods for debugging.
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 976 $ $Date: 2007/01/24 19:36:48 $
 */
public class Debug
{
	/** */
	private Debug()
	{
	};

	/** Debugging enabled? */
	final static public boolean enabled = true;
	
	/** Debug flag: ethernet */
	final static public short DBG_ETH = 0x01;

	/** Debug flag: IP */
	final static public short DBG_IP = 0x02;

	/** Debug flag: TCP */
	final static public short DBG_TCP = 0x04;

	/** Debug flag: UDP */
	final static public short DBG_UDP = 0x08;

	/** Debug flag: ICMP */
	final static public short DBG_ICMP = 0x10;

	/** Debug flag: HTTP */
	final static public short DBG_HTTP = 0x20;

	/** Debug flag: Other */
	final static public short DBG_OTHER = 0x80;

	/** Show all debug messages */
	final static private short DBG_ALL = 0xFF;

	/** Show no debug messages */
	final static private short DBG_NONE = 0x00;

	/**
	 * Enable debugging for certain parts. Possible values:
	 * <ul>
	 * <li>00000001 0x01 Ethernet (driver)
	 * <li>00000010 0x02 IP
	 * <li>00000100 0x04 TCP
	 * <li>00001000 0x08 UDP
	 * <li>00010000 0x10 ICMP
	 * <li>00100000 0x20 HTTP
	 * <li>01000000 0x40
	 * <li>10000000 0x80 Other
	 * <li>11111111 0xFF All
	 * <li>00000000 0x00 None
	 * </ul>
	 * 
	 * Should be set during runtime! Therefore dbgFlagsToDisplay 
	 * is public and writeable...
	 */
	static public short dbgFlagsToDisplay = DBG_ALL;

	/** Is the next message the first debug message on the line? */
	static private boolean firstMsg = true;

	/**
	 * Convert a byte[] array to readable string format. This makes the "hex"
	 * readable!
	 * 
	 * @return result String buffer in String format
	 * @param in
	 *            byte[] buffer to convert to string format
	 */
	static public String byteArrayToHexString(byte in[])
	{

		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
				"E", "F" };

		StringBuffer out = new StringBuffer(in.length * 2);
		while (i < in.length)
		{
			ch = (byte) (in[i] & 0xF0); // Strip off high nibble
			ch = (byte) (ch >>> 4); // shift the bits down
			ch = (byte) (ch & 0x0F); // must do this is high order bit is on!
			out.append(pseudo[(int) ch]); // convert the nibble to a String
											// Character
			ch = (byte) (in[i] & 0x0F); // Strip off low nibble
			out.append(pseudo[(int) ch]); // convert the nibble to a String
											// Character
			out.append(" "); // add a space between bytes
			i++;
		}
		String rslt = new String(out);
		return rslt;
	}

	/**
	 * Convert an int[] array to readable string format. This makes the "hex"
	 * readable!
	 * 
	 * @return result String buffer in String format
	 * @param in
	 *            int[] buffer to convert to string format
	 * @param length
	 */
	static public String intArrayToHexString(int[] in, int length)
	{
		String out = new String();
		if (length == 0)
			length = in.length;
		for (int i = 0; i < length; i++)
			out += intToHexString(in[i]) + " ";
		return out;
	}

	/**
	 * Convert a integer to readable string format. This makes the "hex"
	 * readable!
	 * 
	 * @return result String buffer in String format
	 * @param in
	 *            int to convert to string format
	 */
	static public String intToHexString(int in)
	{
		byte[] b_array = new byte[4];
		for (int i = 0; i < 4; i++)
			b_array[3 - i] = (byte) ((in >> (i * 8)) & 0xFF);
		return byteArrayToHexString(b_array);
	}

	/**
	 * Print a debug message if the corresponding debug flag is set. Appends a
	 * newline.
	 * 
	 * @param msg
	 *            The message
	 * @param flag
	 *            The debug flag
	 */
	static public void println(String msg, short flag)
	{
		if(flag == Debug.DBG_TCP)
		System.out.println(msg);
//		if ((dbgFlagsToDisplay & flag) != 0)
//			System.out.println(firstMsg ? prependDebugFlag(msg, flag) : msg);
//		firstMsg = true;
	}

	/**
	 * Print a debug message if the corresponding debug flag is set.
	 * 
	 * @param msg
	 *            The message
	 * @param flag
	 *            The debug flag
	 */
	static public void print(String msg, short flag)
	{
		if(flag == Debug.DBG_TCP)
		System.out.print(msg);
//		if ((dbgFlagsToDisplay & flag) != 0)
//			System.out.print(firstMsg ? prependDebugFlag(msg, flag) : msg);
//		firstMsg = false;
	}

	/**
	 * Prefixes the given message with a String containing the given debug flag.
	 * 
	 * @param msg
	 *            The message
	 * @param flag
	 *            The debug flag
	 * @return The string with the prefix
	 */
	static private String prependDebugFlag(String msg, short flag)
	{
		switch (flag)
		{
			case DBG_ETH:
				return "Eth: " + msg;

			case DBG_IP:
				return "IP:  " + msg;

			case DBG_OTHER:
				return "Dbg: " + msg;

			case DBG_TCP:
				return "TCP: " + msg;

			case DBG_UDP:
				return "UDP: " + msg;

			case DBG_ICMP:
				return "ICMP:" + msg;

			case DBG_HTTP:
				return "HTTP:" + msg;

			default:
				return "???: " + msg;
		}
	}

	/**
	 * If the flags are set right it will be invoked. The method waits for a
	 * user input and returns it as String
	 * 
	 * @param flag
	 *            The debug flag
	 * @return The user input or null
	 */
	/*
	 * static public String readln(short flag) { if ((DBG & flag) != 0) {
	 * BufferedReader br = new BufferedReader(new
	 * InputStreamReader(java.lang.System.in)); String cmd = null; try { cmd =
	 * br.readLine(); } catch (IOException ioe) { System.out.println("IO error
	 * trying to read your command!"); } return cmd; } return null; }
	 */
}
