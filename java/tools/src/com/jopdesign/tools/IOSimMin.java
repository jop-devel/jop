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

package com.jopdesign.tools;

import com.jopdesign.sys.Const;

// uncomment for usage of the PCs com port
// import javax.comm.CommPortIdentifier;
// import javax.comm.SerialPort;
// import javax.comm.UnsupportedCommOperationException;

/**
 * Simulation of the minimal IO devices
 * 
 * @author martin
 * 
 */
public class IOSimMin {
	protected JopSim js;

	// must not interfere with constants (check at compile time) and
	// within the interval [-128,-1]
	protected static final int SIM_CACHE_FLUSH = -51;
	protected static final int SIM_CACHE_COST = -52;
	protected static final int SIM_CACHE_DUMP = -53;

	// find JVM exit
	protected static String exitStr = "JVM exit!";
	protected char[] exitBuf = new char[exitStr.length()];

	protected int cpuId;
	protected static int cpuCnt = 1;
	protected static boolean startCMP = false;
	static boolean globalLock = false;
	
	int moncnt = 0;

	/**
	 * The interrupt register
	 */
	protected int interrupt;
	/**
	 * Interrupt mask
	 */
	protected int mask;
	/**
	 * The global enable
	 */
	protected boolean intEna;
	/**
	 * Timer was triggered
	 */
	protected boolean timeShot;
	/**
	 * Time for the next timer interrupt
	 */
	protected int nextTimerInt;
	/**
	 * Interrupt as a number
	 */
	protected int intNr;

	/**
	 * Set reference to simulation
	 * 
	 * @param jsRef
	 *            Simulation class reference
	 */
	public void setJopSimRef(JopSim jsRef) {
		js = jsRef;
	}
	
	/**
	 * Set CPU ID
	 * @param id
	 *            CPU id for CMP simulation
	 */
	public void setCpuId(int id) {
		cpuId = id;
		if (id + 1 > cpuCnt) {
			cpuCnt = id + 1;
		}		
	}

	//
	// Mapping of the second serial line to the PCs
	// com port. See ejip.MainSlipUart2 for an example.
	// Uncommented as javax.comm is NOT part of the standard
	// JDK - Blame Sun!

	// private String portName;
	// private CommPortIdentifier portId;
	// private InputStream is = null;
	// private OutputStream os = null;
	// private SerialPort serialPort;
	//
	// private void openSerialPort() {
	// try {
	// if (portId!=null) {
	// try {
	// is.close();
	// os.close();
	// is = null;
	// os = null;
	// } catch (Exception e1) {
	// }
	// serialPort.close();
	// }
	// portId = CommPortIdentifier.getPortIdentifier(portName);
	// serialPort = (SerialPort) portId.open(getClass().toString(), 2000);
	// serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT
	// | SerialPort.FLOWCONTROL_RTSCTS_IN);
	// serialPort.setSerialPortParams(115200,
	// SerialPort.DATABITS_8,
	// SerialPort.STOPBITS_1,
	// SerialPort.PARITY_NONE);
	// is = serialPort.getInputStream();
	// os = serialPort.getOutputStream();
	// System.out.println("open"+portName);
	// } catch (Exception e) {
	// is = null;
	// os = null;
	// System.out.println("Problem with serial port "+portName);
	// System.out.println(e.getMessage());
	// // System.exit(-1);
	// }
	// }

	public int read(int addr) {

		int val;
		int i;

		try {
			switch (addr) {
			case Const.IO_STATUS:
				val = Const.MSK_UA_TDRE;
				if (System.in.available() != 0) {
					val |= Const.MSK_UA_RDRF;
				}
				break;
			// for sure when USB TX status is used
			case Const.IO_USB_STATUS:
				val = Const.MSK_UA_TDRE;
				break;
			case Const.IO_STATUS2:
				i = 0;
				// if (is!=null) {
				// try {
				// if (is.available()!=0) {
				// i |= Const.MSK_UA_RDRF;
				// }
				// } catch (IOException e1) {
				// e1.printStackTrace();
				// } // rdrf
				// }
				// i |= Const.MSK_UA_TDRE; // tdre is alwais true on
				// OutputStream
				val = i;
				break;
			case Const.IO_UART:
				if (System.in.available() != 0) {
					val = System.in.read();
				} else {
					val = '_';
				}
				break;
			case Const.IO_UART2:
				i = 0;
				// try {
				// i = is.read();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				val = i;
				break;
			//
			// System device
			//
			case Const.IO_CNT:
				val = (int)js.clkCnt;
				break;
			case Const.IO_US_CNT:
				val = usCnt();
				break;
			case Const.IO_INTNR:
				val = intNr;
				break;
			case Const.IO_EXCPT:
				val = js.exceptReason;
				break;
			case Const.IO_CPU_ID:
				val = cpuId;
				break;
			case Const.IO_CPUCNT:
				val = cpuCnt;
				break;
			case SIM_CACHE_COST:
				val = js.cacheCost;
				break;
			case SIM_CACHE_DUMP:
				// trigger cache debug output
				// cache.rawData();
				// cache.resetCnt();
				js.objectCacheSim.dumpStats();
				val = js.objectCacheSim.getStats().accessCount;
				break;
			default:
				val = 0;
				System.out.println("Default read " + addr);
			}
		} catch (Exception e) {
			System.out.println(e);
			val = 0;
		}

		return val;
	}

	public void write(int addr, int val) {

		switch (addr) {
		case Const.IO_UART:
			if (JopSim.log)
				System.out.print("\t->");
			System.out.print((char) val);
			if (JopSim.log)
				System.out.println("<-");
			// check the output for JVM exit!
			for (int i = 0; i < exitStr.length() - 1; ++i) {
				exitBuf[i] = exitBuf[i + 1];
			}
			exitBuf[exitBuf.length - 1] = (char) val;
			if (new String(exitBuf).equals(exitStr)) {
				JopSim.exit();
			}
			break;
		case Const.IO_USB_DATA:
			// Ignore USB output
			break;
		case Const.IO_UART2:
			// if (os==null) return;
			// try {
			// os.write(val&0xff);
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			break;
		case Const.IO_STATUS2:
			// if (serialPort!=null) {
			// serialPort.setDTR(val==1);
			// try {
			// if ((val&0x04)==0) {
			// serialPort.setSerialPortParams(2400,
			// SerialPort.DATABITS_8,
			// SerialPort.STOPBITS_1,
			// SerialPort.PARITY_NONE);
			// } else {
			// serialPort.setSerialPortParams(115200,
			// SerialPort.DATABITS_8,
			// SerialPort.STOPBITS_1,
			// SerialPort.PARITY_NONE);
			// }
			// if ((val&0x02)==0) {
			// serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			// } else {
			// serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT
			// | SerialPort.FLOWCONTROL_RTSCTS_IN);
			// }
			// } catch (UnsupportedCommOperationException e1) {
			// e1.printStackTrace();
			// }
			// }
			break;

		case Const.IO_INT_ENA:
			intEna = (val == 0) ? false : true;
			break;
		case Const.IO_TIMER:
			nextTimerInt = val;
			timeShot = false;
			break;
		case Const.IO_SWINT:
			interrupt |= 1 << val;
			break;
		case Const.IO_WD:
			break;
		case Const.IO_EXCPT:
			js.intExcept = true;
			js.exceptReason = val;
			break;
		case Const.IO_LOCK:
			// only used in microcode for monitor enter and exit
			break;
		case Const.IO_SIGNAL:
			startCMP = (val != 0);
			break;
		case Const.IO_INTMASK:
			mask = val;
			break;
		case Const.IO_INTCLEARALL:
			interrupt = 0;
			break;
		case Const.IO_PERFCNT:
			js.resetStat();
			break;
		case SIM_CACHE_COST:
			js.cacheCost = val;
			break;
		case SIM_CACHE_FLUSH:
			js.cache.flushCache();
			js.objectCacheSim.flushCache();
			break;
		case Const.IO_DEADLINE:
			js.localCnt += (val-((int) js.clkCnt));
			break;
		default:
			System.out.println("Default write " + addr + " " + val);
		}
	}

	/**
	 * Monitor enter returns true when either the global lock
	 * is grabbed or it is already held (moncnt>0).
	 * @return true if successfully entered
	 */
	boolean monEnter() {
		intEna = false;
		if (moncnt == 0) {
			if (globalLock) {
				return false;
			} else {
				++moncnt;						
				globalLock = true;
				return true;
			}
		} else {
			++moncnt;
			return true;
		}
	}

	void monExit() {
		--moncnt;
		if (moncnt == 0) {
			intEna = true;
			globalLock = false;
		}
	}

	boolean intPending() {
		int i;
		// do the timer interrupt
		if ((nextTimerInt - usCnt() < 0) && !timeShot) {
			timeShot = true;
			interrupt |= 1;
		}
		// check interrupts
		int val = interrupt & mask;
		if (val != 0 && intEna) {
			for (i = 0; val != 0; ++i) {
				if ((val & 1) != 0) {
					break;
				}
				val >>>= 1;
			}
			intNr = i;
			interrupt &= ~(1 << i);
			intEna = false;
			return true;
		}
		return false;
	}

	public int usCnt() {
		// return ((int) (System.nanoTime()/1000)); // does not really work as
		// expected
		return ((int) System.currentTimeMillis()) * 1000;
	}

}
