

public class Echo implements Runnable, SerialPortEventListener {
	static CommPortIdentifier portId;
	static Enumeration portList;

	InputStream inputStream;
	OutputStream outputStream;
	SerialPort serialPort;
	Thread readThread;

	public static void main(String[] args) {

		try {
			portId = CommPortIdentifier.getPortIdentifier("COM1");
			Echo e = new Echo();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public Echo() {
		try {
			serialPort = (SerialPort) portId.open("EchoApp", 2000);
		} catch (PortInUseException e) {}
		try {
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {}
/*
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {}
		serialPort.notifyOnDataAvailable(true);
*/
		try {
			serialPort.setSerialPortParams(115200,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			serialPort.enableReceiveTimeout(20);
		} catch (UnsupportedCommOperationException e) {
			System.out.println(e.getMessage());
		}
/*
		readThread = new Thread(this);
		readThread.start();
*/
		int val = '0';
		try {
			for (;;) {
				for (int j=0; j<4; ++j) {
					outputStream.write(val++);
				}
				for (int j=0; j<4; ++j) {
					int b = inputStream.read();
					if (b==-1) {
						System.out.print('.');
					} else {
						System.out.print((char) b);
					}
				}
				if (val>'0'+79) val = '0';
			}
		} catch (IOException e) {}
	}

	public void run() {
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {}
	}

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
				while (inputStream.available() > 0) {
					int numBytes = inputStream.read(readBuffer);
				}
				System.out.print(new String(readBuffer));
			} catch (IOException e) {}
			break;
		}
	}
}
