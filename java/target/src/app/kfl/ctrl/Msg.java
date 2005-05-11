/**
*	Msg. java
*
*	communicate with jopbb via serial line.
*/

import BBSys;

public class Msg implements SerialPortEventListener {

	private static final int TIMEOUT = 20;

	private static final int ADDR_MSK = 0x7c0000;
	private static final int CMD_MSK  = 0x03f000;
	private static final int DATA_MSK = 0x000fff;

	private CommPortIdentifier portId;
	private InputStream is;
	private OutputStream os;
	private SerialPort serialPort;

	private byte[] buf = new byte[4];

	public static void main(String[] args) {

		Msg e = new Msg();

		for (int i=1; true; ++i) {
if (i!=BBSys.CMD_UP && i!=BBSys.CMD_DOWN) {
			int j = e.exchg(1, i, 0);
			if (j==-1) e.clear();
			if (j!=-1 && j!=i) {
				System.out.print(i+":"+j+" ");
			} else {
				System.out.print(j+" ");
			}
}
try {
Thread.sleep(1000);
} catch (Exception x) {}
		}
	}

	public Msg() {

		try {
			portId = CommPortIdentifier.getPortIdentifier("COM2");
			serialPort = (SerialPort) portId.open("Msg", 2000);
			is = serialPort.getInputStream();
			os = serialPort.getOutputStream();
/*
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
*/
			serialPort.setSerialPortParams(115200,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			serialPort.enableReceiveTimeout(TIMEOUT);
			serialPort.enableReceiveThreshold(4);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}

/**
*	claculate crc value with polynom x^8 + x^2 + x + 1
*	and initial value 0xff
*	on 32 bit data
*/
	int crc(int val) {

		int reg = -1;
		int poly = 0x07;

		for (int i=0; i<32; ++i) {
			reg <<= 1;
			if (val<0) reg |= 1;
			val <<=1;
			if ((reg & 0x100) != 0) reg ^= 0x07;
		}
		reg &= 0xff;

		return reg;
	}

/**
*	send and receive a 24 bit message (high byte first).
*	-1 means timeout, -2 crc error.
*/
	synchronized public int exchg(int addr, int cmd, int data) {

		int val;
		int b;

		addr <<= 18;
		cmd <<= 12;
		data &= DATA_MSK;
		val = 0x800000 | addr | cmd | data;
		val <<= 8;
		val |= crc(val);		// append crc

		for (int j=0; j<4; ++j) {
			buf[j] = (byte) (val>>>((3-j)*8));
		}

		try {
			os.write(buf);
			int cnt = 0;
			for (int i=0; i<100/TIMEOUT && cnt<4; ++i) {		// try max. 100 ms
				cnt += is.read(buf, cnt, 4-cnt);
			}

			if (cnt<4) {
				return -1;
			}
		} catch (Exception e) {}
			
		val = 0;
		for (int j=0; j<4; ++j) {
			val <<= 8;
			val |= ((int) buf[j])&0xff;
		}

		if (crc(val)!=0) {
			return -2;
		}
		if (val<0) {			// response have a 0 as MSB
			return -3;
		}
		val >>>= 8;
		if ((val & ADDR_MSK) != addr) {
			return -4;
		}

// System.out.println("cmd: "+(cmd>>>12)+" cmdval: "+((val&CMD_MSK)>>>12));
		if ((val & CMD_MSK) != cmd) {
			return -5;
		}
		val &= DATA_MSK;		// mask out cmd and address field
		return val;
	}

/**
*	try to read 4 pending bytes from serial line after timeout.
*/
// wenn das serial timeout hoch genug ist, ist clear nicht notwendig!
	synchronized public void clear() {

		int cnt = 0;
		try {
			for (int j=0;cnt<4 && j<6; ++j) {
				if (is.read()!=-1) ++cnt;
			}
		} catch (IOException e) {}
	}






/** not used */
	public void serialEvent(SerialPortEvent event) {
		switch(event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[20];

			try {
				while (is.available() > 0) {
					int numBytes = is.read(readBuffer);
				}
				System.out.print(new String(readBuffer));
			} catch (IOException e) {}
			break;
		}
	}
}
