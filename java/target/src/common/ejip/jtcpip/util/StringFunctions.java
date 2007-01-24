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

import ejip.jtcpip.IP;
import ejip.jtcpip.JtcpipException;

/**
 * Some locally useful string manipulation / parsing functions.
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 943 $ $Date: 2007/01/24 19:36:48 $
 */
public class StringFunctions
{
	/** Invalid MAC address format */
//	private static JtcpipException macException = new JtcpipException("Invalid MAC Address String");

	/**
	 * Converts a string "00:00:00:00:00:00" into the corresponding int[]
	 * 
	 * @param macAddr
	 * @param buffer
	 * @throws JtcpipException
	 */
	public static void macStrToByteArr(String macAddr, int[] buffer) 
	{
		char[] c = macAddr.toCharArray();
		int j = 0;
		String s = "";

		if (buffer == null)
			buffer = new int[6];

		for (int i = 0; i < c.length; i++)
		{
			if (c[i] != ':')
				s += c[i];

			if (c[i] == ':' || i == (c.length - 1))
			{
				buffer[j] = Integer.parseInt(s, 16);
				if (buffer[j] < 0 || buffer[j] > 255)
					if (Debug.enabled)
						Debug.println("StringFunctions.macStrToByteArr():Index out of range",Debug.DBG_OTHER);


				s = "";
				j++;

				if (j == buffer.length)
				{
					if (i < (c.length - 1))
						if (Debug.enabled)
							Debug.println("StringFunctions.macStrToByteArr():Index out of range",Debug.DBG_OTHER);

					return;
				}
			}
		}

		if (j != 6)
			if (Debug.enabled)
				Debug.println("StringFunctions.macStrToByteArr():Index out of range",Debug.DBG_OTHER);


	}

	/**
	 * Retrieves the ip address value from a string.
	 * 
	 * @param connectorString
	 *            of the form //ip_addr:port[;option=value...]
	 * @return the ip address as integer
	 */
	public static int getAddrFromConnectorStr(String connectorString)
	{
		int offset = connectorString.indexOf('/');
		if (offset >= 0)
		{
			if (connectorString.charAt(offset + 1) != '/')
				throw new IllegalArgumentException("single '/' in connector string");
			offset += 2;
		}
		else
			offset = 0;

		int colon = connectorString.indexOf(':', offset);

		if (colon < 1)
			throw new IllegalArgumentException("no ':' in connector string");

		if (colon == 1)
			return 0xFFFFFFFF; // 255.255.255.255

		try
		{
			return IP.ipStringToInt(connectorString.substring(offset, colon));
		} catch (JtcpipException e)
		{
			Debug.println("Wrong IP address in connector string (" + connectorString + ")",
					Debug.DBG_OTHER);
			return 0;
		}
	}

	/**
	 * Retrieves the port value from a string.
	 * 
	 * @param connectorString
	 *            of the form //ip_addr:port[;option=value...]
	 * @return the port as int or -1 in case of a failure
	 */
	public static int getPortFromConnectorStr(String connectorString)
	{
		int offset = connectorString.indexOf('/');
		if (offset >= 0)
		{
			if (connectorString.charAt(offset + 1) != '/')
				throw new IllegalArgumentException("single '/' in connector string");
			offset += 2;
		}
		else
			offset = 0;
		
		int colon = connectorString.indexOf(':', offset); //if a full connector string is given there is already a ':' just in front of '//'
		int semic = connectorString.indexOf(';'); // in case there are
													// options...

		if (colon < 1)
			return -1;

		try
		{
			if (semic < 1)
				semic = Integer.parseInt(connectorString.substring(colon + 1));
			else
				semic = Integer.parseInt(connectorString.substring(colon + 1, semic));
		} catch (Exception e)
		{
			semic = -1;
		}

		if (semic < 0 || semic > 0xFFFF)
		{
			Debug.println(
					"Port number out of bounds in connector string (" + connectorString + ")",
					Debug.DBG_OTHER);
			return -1;
		}
		else
			return semic;
	}
}
