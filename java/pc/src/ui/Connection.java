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
