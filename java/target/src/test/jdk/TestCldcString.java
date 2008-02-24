/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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

package jdk;

public class TestCldcString {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// test String and Co

		String encoding = ("iso_8859_1");
		encoding = encoding.toUpperCase();
		System.out.println(encoding);
		
		String umlaut = ("όδ)(&/&");
		umlaut= umlaut.toUpperCase();
		System.out.println(umlaut);
		
		String str1 = new String();
		String str2 = new String();
		str2 = "ames cole";
		str1 = "james cole";
		System.out.println(str1);
		System.out.println(str2);
		int i = 0;
		i = str1.compareTo(str2);
		System.out.println(i);

		String tmp = str1.concat(str2);
		System.out.println(tmp);

		if (tmp.endsWith("cole")) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR String");
		}
		if (str1.equals(str1)) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR String");
		}

		/*
		 * byte[] sbbyte = str1.getBytes(); try { byte[] sbbyte1 =
		 * str1.getBytes("ISO_8859_1"); } catch ( UnsupportedEncodingException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */

		// int hashcode = str1.hashCode();
		// System.out.print(hashcode);
		int indexof = 0;
		indexof = str1.indexOf(99);
		System.out.println("indexof:");
		System.out.println(indexof);
		indexof = str1.indexOf("e", 7);
		System.out.println("indexof:");
		System.out.println(indexof);
		indexof = str1.lastIndexOf(97, 7);
		System.out.println("indexof:");
		System.out.println(indexof);
		tmp = str1.replace('a', 'b');

		System.out.println(tmp);
		System.out.println("substring:\n");

		tmp = str1.substring(3, 7);
		System.out.print(tmp);
		// str1.toLowerCase();

		String str3 = new String(" bloody mary ");
		tmp = str3.trim();
		System.out.print(tmp);

		// System.out.println(sb.toString());

		Long lon = new Long(System.currentTimeMillis());
		System.out.println(lon.toString());
		lon = new Long(System.currentTimeMillis());
		System.out.println(lon.toString());
		lon = new Long(System.currentTimeMillis());
		System.out.println(lon.toString());

		// test String Buffer

		StringBuffer sb = new StringBuffer("dfmasafasa");

		System.out.println(sb.toString());
		sb.append(true);
		System.out.println(sb.toString());
		sb.append("hgfjz");

		System.out.println(sb.toString());
		sb.append(1);
		System.out.println(sb.toString());
		char[] tmp2 = new char[10];
		tmp2[0] = '0';
		tmp2[1] = '1';
		tmp2[2] = '2';
		tmp2[3] = '3';
		sb.append(tmp2, 1, 5);
		System.out.println(sb.toString());
		sb.deleteCharAt(5);
		System.out.println(sb.toString());
		sb.insert(3, true);
		System.out.println(sb.toString());
		System.out.println(sb.reverse().toString());
		sb.append(new Integer(2));
		System.out.println(sb.toString());

		Short sho = new Short((short) 2);
		System.out.println(sho.toString());

		Long lo = new Long(234);
		System.out.println(lo.toString());
		String tmplong = new String("-123234234546");
		long lo1 = Long.parseLong(tmplong);
		Long lo2 = new Long(lo1);
		System.out.println(lo2.toString());

		Integer i1 = new Integer(1);
		System.out.println(i1.toString());
		int i2 = Integer.parseInt("123");
		Integer i3 = new Integer(i2);
		System.out.println(i3.toString());

		char[] charsb;
		charsb = sb.toString().toCharArray();
		String tmp23 = new String(charsb);
		tmp23 = tmp23.concat("test toCharArray");
		System.out.println(tmp23);
		Long lo3 = new Long(234);
		sb.append(lo3);
		System.out.println(sb);

	}
}
