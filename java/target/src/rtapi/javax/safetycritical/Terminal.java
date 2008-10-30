/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package javax.safetycritical;

/**
 * A simple Terminal that puts out UTF8 version of String/StringBuilder,....
 * Does not allocate memory. The output device is implementation dependent and
 * writing to /dev/nul is a a valid implementation.
 * 
 * @author Martin Schoeberl
 *
 */
public class Terminal {

	private static Terminal single = new Terminal();
	
	private Terminal() {	
	}
	
	/**
	 * Get the single output device.
	 * @return
	 */
	public static Terminal getTerminal() {
		return single;
	}
	
	/**
	 * Write the character sequence to the implementation dependent
	 * output device in UTF8.
	 * @param s
	 * 
	 */
	public void write(CharSequence s) {
		for (int i=0; i<s.length(); ++i) {
			char c = s.charAt(i);
			if (c<128) {
				write((byte) (c & 0x7f)); 
			} else if (c<0x800) {
				write((byte) (0xc0 | (c>>>6)));
				write((byte) (0x80 | (c&0x3f)));
			} else if (c<0x1000) {
				write((byte) (0xe0 | (c>>>12)));
				write((byte) (0x80 | ((c>>>6)&0x3f)));
				write((byte) (0x80 | (c&0x3f)));
			} else {
				// TODO: we don't care on unicode that needs an escape itself
			}
		}
	}
	
	/**
	 * Same as write, but add a newline. CRLF does not hurt on a
	 * Unix terminal.
	 * @param s
	 */
	public void writeln(CharSequence s) {
		write(s);
		writeln();
	}
	
	/**
	 * Just a CRLF output.
	 */
	public void writeln() {
		write("\r\n");
	}
	/**
	 * Does the actual work. Change for your implementation.
	 * @param b A UTF8 byte to be written.
	 */
	private void write(byte b) {
		System.out.write(b);
	}
}
