/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Wolfgang Puffitsch <wpuffits@mail.tuwien.ac.at>

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

package cruiser.common;

public abstract class WireMessage {

	public enum Type {
		SPEED_FRONT_LEFT(1, 4), SPEED_FRONT_RIGHT(2, 4),
			SPEED_REAR_LEFT(3, 4), SPEED_REAR_RIGHT(4, 4),
			TARGET_SPEED(7, 8),
			THROTTLE(8, 4), BRAKE(9, 4);

		final int id;
		final int length;
		Type(int id, int length) { this.id = id; this.length = length; }

		// reverse lookup; take care when adding new members!
		public static Type fromId(int id) {
			switch(id) {
			case 1: return SPEED_FRONT_LEFT;
			case 2: return SPEED_FRONT_RIGHT;
			case 3: return SPEED_REAR_LEFT;
			case 4: return SPEED_REAR_RIGHT;
			case 7: return TARGET_SPEED;
			case 8: return THROTTLE;
			case 9: return BRAKE;
			default: return null;
			}
		}
	}

	// common field for all subtypes
	private final Type type;

	WireMessage(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	private static final int OVERHEAD_LENGTH = 7;
	public static final int MAX_LENGTH = 15+OVERHEAD_LENGTH;

	static String buildMessage(Type type, long data) {
		char[] buffer = new char[OVERHEAD_LENGTH+type.length];
		
		int pos = 0;

		buffer[pos++] = ':';
		buffer[pos++] = hexDigit((type.id >>> 4) & 0x0f);
		buffer[pos++] = hexDigit(type.id & 0x0f);
		buffer[pos++] = hexDigit(type.length);		

		int checksum = (type.id >>> 4) ^ type.id ^ type.length;

		for (int i = type.length-1; i >= 0; i--) { //@WCA loop <= 15
			int d = (int)(data >>> (4*i));
			buffer[pos++] = hexDigit(d & 0x0f);
			checksum ^= d;
		}

		buffer[pos++] = hexDigit(checksum & 0x0f);
		buffer[pos++] = '\r';
		buffer[pos] = '\n';

		return new String(buffer);
	}

	public static boolean checkMessage(String msg) {
		// check message start
		if (msg.charAt(0) != ':') {
			// System.err.print("^");
			return false;
		}
		// check type
		Type t = parseType(msg);
		if (t == null) {
			// System.err.print("T");
			return false;
		}
		// check length of payload
		int len = parseLength(msg);
		if (len < 0 || len != t.length) {
			// System.err.print("L");
			return false;
		}
		// check length of overall message
		if (msg.length() != len+OVERHEAD_LENGTH) {
			// System.err.print("S");
			return false;
		}
		// checking checksum
		int chk = 0;
		int chkpos = msg.length()-3;
		for (int i = 1; i < chkpos; i++) { //@WCA loop <= 18
			chk ^= hexNum(msg.charAt(i));
		}
		if (hexDigit(chk) != msg.charAt(chkpos)) {
			// System.err.print("C");
			return false;
		}
		// check message end
		if (msg.charAt(msg.length()-2) != '\r') {
			// System.err.print("R");
			return false;
		}
		if (msg.charAt(msg.length()-1) != '\n') {
			// System.err.print("N");
			return false;
		}		
		return true;
	}

	public static Type parseType(String msg) {
		int a = hexNum(msg.charAt(1));
		int b = hexNum(msg.charAt(2));
		if (a < 0 || b < 0) {
			return null;
		}
		int t = (a << 4) | (b & 0x0f);
		return Type.fromId(t);
	}

	public static int parseLength(String msg) {
		int a = hexNum(msg.charAt(3));
		return a;
	}

	public static long parseData(String msg, int len) {
		long data = 0;
		for (int i = 0; i < len; i++) { //@WCA loop <= 15
			int a = hexNum(msg.charAt(4+i));
			if (a < 0) {
				return -1;
			}				
			data <<= 4;
			data |= a & 0x0f;
		}
		return data;
	}

	private static char hexDigit(int v) {
		if (v >= 0 && v <= 9) {
			return (char)(v+'0');
		}
		if (v >= 10 && v <= 15) {
			return (char)(v-10+'A');
		}
		return '#';
	}

	private static int hexNum(char c) {
		if (c >= '0' && c <= '9') {
			return c-'0';
		}
		if (c >= 'A' && c <= 'F') {
			return c-'A'+10;
		}
		if (c >= 'a' && c <= 'f') {
			return c-'a'+10;
		}
		return -1;
	}

}
