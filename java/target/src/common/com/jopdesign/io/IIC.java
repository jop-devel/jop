/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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
	Author: Tórur Biskopstø Strøm (torur.strom@gmail.com)
*/

package com.jopdesign.io;

public final class IIC extends HardwareObject
{	
	public volatile int PRERlo; // Clock Prescale register lo-byte 
	public volatile int PRERhi; // Clock Prescale register hi-byte
	public volatile int CTR; // Control register 
	public volatile int TXR_RXR; // Transmit register (WRITE) and Receive register (READ)
	public volatile int CR_SR; // Command register (WRITE) and Status register (READ)
	public volatile int TXR_DEBUG;
	public volatile int CR_DEBUG;
}
