package yaffs2.utils.debug.pc;

import java.io.*;
import gnu.io.*;

public class SerialInterface /* implements Runnable, SerialPortEventListener */ {
    CommPortIdentifier portId;

    SerialPort serialPort;
//    Thread readThread;

    public InputStream getInputStream() throws IOException
    {
    	return serialPort.getInputStream();    	
    }
    
    public OutputStream getOutputStream() throws IOException
    {
    	return serialPort.getOutputStream();
    }
    
    public SerialInterface(String port) throws IOException, PortInUseException, NoSuchPortException {
    	portId = CommPortIdentifier.getPortIdentifier(port);
    	
//
//    public SerialInterface() {
//        try {
            serialPort = (SerialPort) portId.open("SerialInterface", 2000);
//        } catch (PortInUseException e) {}
//        try {
//            inputStream = serialPort.getInputStream();
//        } catch (IOException e) {}
//	try {
//            serialPort.addEventListener(this);
//	} catch (TooManyListenersException e) {}
        serialPort.notifyOnDataAvailable(true);
        try {
            serialPort.setSerialPortParams(115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) {}
        
        // XXX flow control, etc. settings?
        serialPort.setInputBufferSize(10000); // XXX no idea
        serialPort.setOutputBufferSize(10000);
        
//        readThread = new Thread(this);
//        readThread.start();
    }

//    public void run() {
//        try {
//            Thread.sleep(20000);
//        } catch (InterruptedException e) {}
//    }
//
//    public void serialEvent(SerialPortEvent event) {
//        switch(event.getEventType()) {
//        case SerialPortEvent.BI:
//        case SerialPortEvent.OE:
//        case SerialPortEvent.FE:
//        case SerialPortEvent.PE:
//        case SerialPortEvent.CD:
//        case SerialPortEvent.CTS:
//        case SerialPortEvent.DSR:
//        case SerialPortEvent.RI:
//        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
//            break;
//        case SerialPortEvent.DATA_AVAILABLE:
//            byte[] readBuffer = new byte[20];
//
//            try {
//                while (inputStream.available() > 0) {
//                    int numBytes = inputStream.read(readBuffer);
//                }
//                System.out.print(new String(readBuffer));
//            } catch (IOException e) {}
//            break;
//        }
//    }
} 