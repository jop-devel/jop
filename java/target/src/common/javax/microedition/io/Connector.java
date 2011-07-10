/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Martin Schoeberl (martin@jopdesign.com)

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

package javax.microedition.io;

import java.io.*;

import javax.safetycritical.io.ConsoleConnection;

public class Connector {

	public final static int READ = 1;
	public final static int WRITE = 2;
	public final static int READ_WRITE = (READ | WRITE);

	private Connector() {
	}
	
	private static ConsoleConnection single;
	/**
	 * This method is public, but as no one can create directly
	 * a ConsoleConnection it can practically only be used by
	 * javax.safetycritical.io.ConsoleConnection
	 * @param console
	 */
	public static void setConsoleConnection(ConsoleConnection console) {
		if (console!=null) {
			single = console;
		}
	}

	public static Connection open(String name) throws IOException {
		return open(name, READ_WRITE);
	}

	public static Connection open(String name, int mode) throws IOException {
		return open(name, mode, false);
	}

	/**
	 * Does this method alone make sense?
	 * @param name
	 * @param mode
	 * @param timeouts
	 * @return
	 * @throws IOException
	 */
	public static Connection open(String name, int mode, boolean timeouts)
			throws IOException {
		if (!name.equals("console:")) {
			throw new IOException();
		}
		return single;
	}

	public static DataInputStream openDataInputStream(String name)
			throws IOException {
		throw new IOException();
	}

	/**
	 * Do we need DataOutputStream?
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static DataOutputStream openDataOutputStream(String name)
			throws IOException {
		throw new IOException();
	}

	public static InputStream openInputStream(String name) throws IOException {

		return openDataInputStream(name);
	}

	public static OutputStream openOutputStream(String name) throws IOException {

		// return openDataOutputStream(name);
		if (!name.equals("console:")) {
			throw new IOException();
		}
		return single.openOutputStream();
	}

}
