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
 * Created on 12.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package simhw;

import java.io.*;

import gnu.io.*;

import com.jopdesign.sys.Const;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TALSim extends BaseSim {

	private String portName = null;
	private CommPortIdentifier portId;
	private InputStream is = null;
	private OutputStream os = null;
	private SerialPort serialPort;
	private TALWindow twin;

	/**
	 * 
	 */
	public TALSim() {
		super();
		twin = new TALWindow();
		twin.setTsim(this);
setPortName("COM4");
	}
	
	private void openSerialPort() {
		try {
			if (portId!=null) {
				try {
					is.close();
					os.close();
					is = null;
					os = null;
				} catch (Exception e1) {
				}
				serialPort.close();
			}
			portId = CommPortIdentifier.getPortIdentifier(portName);
			serialPort = (SerialPort) portId.open(getClass().toString(), 2000);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT
										| SerialPort.FLOWCONTROL_RTSCTS_IN);
			serialPort.setSerialPortParams(115200,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			is = serialPort.getInputStream();
			os = serialPort.getOutputStream();
		
		} catch (Exception e) {
			is = null;
			os = null;
			System.out.println("Problem with serial port "+portName);
			System.out.println(e.getMessage());
			// System.exit(-1);
		}
	}
	public int rd(int address) {

		int i = 0;

		switch (address) {
			case Const.IO_STATUS:
			case Const.IO_STATUS2:
				i = 0;
				if (is!=null) {
					try {
						if (is.available()!=0) i |= Const.MSK_UA_RDRF;
					} catch (IOException e1) {
						e1.printStackTrace();
					}	// rdrf
				}
				i |= Const.MSK_UA_TDRE;							// tdre is alwais true on OutputStream
				return i;
			case Const.IO_UART:
//	   System.out.println("Native: read() '"+(char) i+"'");
				break;
			case Const.IO_UART2:
				try {
					i =  is.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return i;
			case Const.IO_IN:
				return twin.getInPort();
			case Const.IO_ADC1:
				return twin.getAdc1();
			case Const.IO_ADC2:
				return twin.getAdc2();
			case Const.IO_ADC3:
				return twin.getAdc3();
			default:
				return super.rd(address);
		}
		return 0;
	}

	public void wr(int val, int address) {
		switch (address) {
			case Const.IO_STATUS:
System.out.println("setDTR on System.out()! "+val);
				break;
			case Const.IO_UART:
				System.out.print((char) val);				// debug serial
				break;
			case Const.IO_STATUS2:
System.out.println("setDTR "+val);
				if (serialPort!=null) {
					serialPort.setDTR(val==1);
					try {
						if ((val&0x04)==0) {
							serialPort.setSerialPortParams(2400,
								SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						} else {
							serialPort.setSerialPortParams(115200,
								SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						}
						if ((val&0x02)==0) {
							serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
						} else {
							serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT
														| SerialPort.FLOWCONTROL_RTSCTS_IN);
						}
					} catch (UnsupportedCommOperationException e1) {
						e1.printStackTrace();
					}
				}
				break;
			case Const.IO_UART2:
				if (os==null) return;
				try {
					os.write(val);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case Const.IO_WD:
				twin.setWd(val!=0);
				break;
			case Const.IO_OUT:
				twin.setOutPort(val);
				break;
			case Const.IO_LED:
				twin.setLedPort(val);
				break;
			default :
				super.wr(val, address);
				break;
		}
	}
	/**
	 * @return
	 */
	public String getPortName() {
		return portName;
	}

	/**
	 * @param string
	 */
	public void setPortName(String string) {
System.out.println("set port: "+string);
		portName = string;
		openSerialPort();
	}

}
