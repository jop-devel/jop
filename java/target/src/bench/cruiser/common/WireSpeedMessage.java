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

public class WireSpeedMessage extends WireMessage {

	// meters per second * 100
	private final short speed;

	public WireSpeedMessage(WireMessage.Type type, short speed) {
		super(type);
		this.speed = speed;
	}

	public short getSpeed() {
		return speed;
	}

	public String toString() {
		return buildMessage(getType(), speed);
	}

	public static WireSpeedMessage fromString(String msg) {
		if (!WireMessage.checkMessage(msg)) {
			return null;
		}
		Type t = WireMessage.parseType(msg);
		return new WireSpeedMessage(t, (short)WireMessage.parseData(msg, t.length));
	}
}