/*
  This file is part of JOP, the Java Optimized Processor (http://www.jopdesign.com/)

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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
 * Created on 12.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ui;

import udp.Tftp;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Connection {

	private String address = "";
	private int[] buf = new int[65536/4];
	private Tftp tftp;
	
	public static Connection single = new Connection();
	
	private Connection() {}

	/**
	 * @return
	 */
	public int[] getBuf() {
		return buf;
	}

	/**
	 * @param string
	 */
	public void setAddress(String string) {
		address = string;
		tftp = new Tftp(address);
	}

	/**
	 * @return
	 */
	public Tftp getTftp() {
		return tftp;
	}

	/**
	 * @param string
	 * @return
	 */
	public int read(String string) {
		return tftp.read(string, buf);
	}

	/**
	 * @return
	 */
	public String getAddress() {
		return address;
	}

}
