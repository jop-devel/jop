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
 * Created on 27.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package tal;

import joprt.RtThread;
import util.Dbg;
import util.Serial;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Modbus extends RtThread {

	private Serial ser;
	private int[] outReg;
	
	final static int MAX_LEN = 256;
	private int[] msg;
	private int len;
	private StringBuffer str;
	
	public static final int ILLEGAL_FUNCTION = 1;
	public static final int ILLEGAL_ADDRESS = 2;
	public static final int ILLEGAL_VALUE = 3;

	/**
	 * @param prio
	 * @param us
	 */
	public Modbus(int prio, int us, Serial serial, int[] or) {
		super(prio, us);
		ser = serial;
		msg = new int[MAX_LEN];
		str = new StringBuffer(100);
		len = 0;
		outReg = or;
	}
	
	public void run() {
		
		for (;;) {
			
			readMsg();
			
			if (len != 0) {
				processMsg();
				if (len!=0) sendMsg();
			}
		}
	}


	/**
	 * 
	 */
	private void processMsg() {
		
		int devAddr = msg[0];
		int cmd = msg[1];
		switch (cmd) {
			case 1:
				readCoils();
				break;
			case 2:
				readInputDiscrete();
				break;
			case 3:
				readMultipleRegisters();
				break;
			case 4:
				readInputRegister();
				break;
			case 5:
				writeSingleCoil();
				break;
			case 6:
				writeSingleRegister();
				break;
			case 15:
				writeMultipleCoils();
				break;
			case 16:
				writeMultipleRegisters();
				break;
			case 20:
				readFileRecord();
				break;
			case 21:
				writeFileRecord();
				break;
			case 22:
				maskWriteRegister();
				break;
			case 23:
				readWriteMultipleRegisters();
				break;
			case 43:
				readDeviceIdentification();
				break;
			default:
				msg[1] |= 128;
				msg[2] = ILLEGAL_FUNCTION;
				len = 3;
		}
		
	}

	/**
	 * 
	 */
	private void readCoils() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void readInputDiscrete() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void readMultipleRegisters() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}
	
	
	static int abc;

	/**
	 * 
	 */
	private void readInputRegister() {

		int addr = (msg[2]<<8) + msg[3];
		int cnt = (msg[4]<<8) + msg[5];

		if (cnt<1 || cnt>5) { 		
			msg[1] |= 128;
			msg[2] = ILLEGAL_VALUE;
			len = 3;
			return;
		}
		if (addr+cnt>5) {
			msg[1] |= 128;
			msg[2] = ILLEGAL_ADDRESS;
			len = 3;
			return;
		}
		msg[2] = cnt*2;
		for (int i=0; i<cnt; ++i) {
			int idx = addr+i;
			int val = 0;
			if (idx==0) {
				val = Native.rd(Const.IO_IN);
			} else if (idx==1) {
				val = Native.rd(Const.IO_ADC1);
			} else if (idx==2) {
				val = Native.rd(Const.IO_ADC2);
			} else if (idx==3) {
				val = Native.rd(Const.IO_ADC3);
			} else if (idx==4) {
				val = abc++;
			}
			msg[3+(i<<1)] = val>>8;
			msg[4+(i<<1)] = val;
		}
		len = 3+(cnt<<1);
	}

	/**
	 * 
	 */
	private void writeSingleCoil() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void writeSingleRegister() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void writeMultipleCoils() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void writeMultipleRegisters() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void readFileRecord() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void writeFileRecord() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void maskWriteRegister() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void readWriteMultipleRegisters() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void readDeviceIdentification() {
		// TODO Auto-generated method stub
		msg[1] |= 128;
		msg[2] = ILLEGAL_FUNCTION;
		len = 3;
	}

	/**
	 * 
	 */
	private void sendMsg() {
		
		int i;
		str.setLength(0);
		str.append(':');
		int sum = 0;
		for (i=0; i<len; ++i) {
			int val = msg[i];
			sum += val;
			str.append(int2hex(val>>4));
			str.append(int2hex(val));
		}
		sum = -sum;
		str.append(int2hex(sum>>4));
		str.append(int2hex(sum));
		str.append("\r\n");
		
		len = 0;
		int slen = str.length();
		i = 0;
Dbg.wr('<');
		for (;;) {
			int j = ser.txFreeCnt();
			for (int k=0; k<j; ++k) {
				ser.wr(str.charAt(i));
Dbg.wr(str.charAt(i));
				++i;
				if (i==slen) return;
			}
			waitForNextPeriod();
		}
	}

	/**
	 * @return
	 */
	private void readMsg() {

		int val, cnt;
		str.setLength(0);
		waitForStart();
		for (;;) {
			waitForNextPeriod();
			cnt = ser.rxCnt();
			if (cnt==0) continue;
			
			val = 0;
			for (int i = cnt; i>0; --i) {
				val = ser.rd();
				str.append((char) val);
				if (val=='\n') break;
			}
			if (val=='\n') break;
		}
Dbg.wr('>');
Dbg.wr(str);
		
		len = convertMsg();
	}

	/**
	 * @return
	 */
	private int convertMsg() {
		
		int i;
		int mlen = str.length()-2;
		// minimum function code and checksum
		if (mlen<4) return 0;
		// parity check and mask, when using 7 bits
		int sum = 0;
		for (i=0; i<mlen; i+=2) {
			int val = Param.readHexByte(str, i);
			sum += val;
			msg[i>>1] = val;
		}
		sum &= 0xff;
		if (sum!=0) {
			Dbg.wr("wrong checksum\n");
			return 0;
		}
		
		return (mlen-2)>>1;
	}

	/**
	 * @return
	 */
	private void waitForStart() {

		int cnt;
		for (;;) {
			waitForNextPeriod();
			cnt = ser.rxCnt();
			if (cnt==0) continue;
			
			for (int i = cnt; i>0; --i) {
				if (ser.rd()==':') return;
			}
		}
	}
	
	/**
	 * @param i
	 * @return
	 */
	private char int2hex(int i) {
		
		i &= 0x0f;
		if (i<10) {
			return (char) ('0'+i);
		} else {
			return (char) ('A'+i-10);
		}
	}


}
