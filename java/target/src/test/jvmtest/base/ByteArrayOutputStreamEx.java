/* jvmtest - Testing your VM 
  Copyright (C) 20009, Guenther Wimpassinger

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
package jvmtest.base;

import java.io.ByteArrayOutputStream;

/**
 * Extends the ByteArrayOutputStream with methods to write primitive
 * types to the stream and not only single bytes and array of bytes.
 * 
 * @author Administrator
 *
 */
public class ByteArrayOutputStreamEx extends ByteArrayOutputStream {
	
	public void writeBoolean(boolean bValue) {
		if (bValue) {
			write(1);
		} else {
			write(0);
		}
	}

	public void writeChar(char cValue) {
		write((cValue      ) & 0xFF);
		write((cValue >>> 8) & 0xFF);
	}
	
	public void writeShort(short sValue) {
		write((sValue      ) & 0xFF);
		write((sValue >>> 8) & 0xFF);
	}
	
	public void writeInt(int iValue) {
		write((iValue       ) & 0xFF);
		write((iValue >>>  8) & 0xFF);
		write((iValue >>> 16) & 0xFF);
		write((iValue >>> 24) & 0xFF);
	}
	
	public void writeLong(long lValue) {
		writeInt((int)(lValue        & 0xFFFFFFFF));
		writeInt((int)(lValue >>> 32 & 0xFFFFFFFF));
	}
	
	public void writeFloat(float fValue) {
		writeInt(Float.floatToRawIntBits(fValue));
	}
	
	public void writeDouble(double dValue) {
		writeLong(Double.doubleToRawLongBits(dValue));
	}
	
	public void writeString(String sValue) {
		byte[] data = sValue.getBytes();
		write(data,0,data.length);
	}
	
	public void writeByte(byte bValue) {
		write(bValue);
	}
	
}