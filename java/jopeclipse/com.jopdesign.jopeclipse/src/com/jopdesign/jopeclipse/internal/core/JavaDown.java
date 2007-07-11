package com.jopdesign.jopeclipse.internal.core;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamTokenizer;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.jopdesign.jopeclipse.internal.ui.launchConfigurations.IJOPLaunchConfigurationConstants;

public class JavaDown implements IApplication {
    protected static int SERIAL_PORT_BAUDRATE = 115200;

    protected static int SERIAL_PORT_DATABITS = SerialPort.DATABITS_8;

    protected static int SERIAL_PORT_PARITY = SerialPort.PARITY_NONE;

    protected static int SERIAL_PORT_STOPBITS = SerialPort.STOPBITS_1;

    /** Serial port timeout (in ms) */
    protected static final int SERIAL_PORT_TIMEOUT = 2000;

    private String commPortId;

    private IPath jopFile;

    private IProgressMonitor progressMonitor;

    private boolean usb;

    public String getCommPortId() {
        return commPortId;
    }

    public IProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    public void run(IProgressMonitor monitor) throws NoSuchPortException,
            PortInUseException, UnsupportedCommOperationException,
            FileNotFoundException, IOException {
        final CommPortIdentifier commPortId = CommPortIdentifier
                .getPortIdentifier(this.commPortId);
        final SerialPort serialPort = openSerialPort(commPortId);

        download(jopFile, serialPort, usb, monitor);

        serialPort.close();
    }

    public void setCommPortId(String commPortId) {
        this.commPortId = commPortId;
    }

    public void setJopFile(IPath jopFile) {
        this.jopFile = jopFile;
    }

    public void setProgressMonitor(IProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    public Object start(IApplicationContext context) throws Exception {
        Map contextArguments = context.getArguments();

        return contextArguments.get(IApplicationContext.APPLICATION_ARGS);
    }

    public void stop() {
        // TODO Auto-generated method stub

    }

    public void useUSB(boolean useUSB) {
        usb = useUSB;
    }

    /**
     * Downloads a JOPized file to JOP
     * 
     * @param file
     * @param serialPort
     * @param usb
     * @param monitor
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void download(IPath file, SerialPort serialPort, boolean usb,
            IProgressMonitor monitor) throws FileNotFoundException, IOException {
        OutputStream serialOut = serialPort.getOutputStream();
        InputStream serialIn = serialPort.getInputStream();
        FileReader fileIn = new FileReader(file.toFile());

        StreamTokenizer tok = new StreamTokenizer(fileIn);

        tok.slashSlashComments(true);
        tok.whitespaceChars(',', ',');

        int numReplies = 0; // Replies to read back
        int nwords = 0; // Total number of words to process

        // FIXME debug
        serialOut = new FileOutputStream(file.addFileExtension(".down").toFile());
        serialIn = new InputStream() {

            @Override
            public int read() throws IOException {
                return 0;
            }

        };

        for (int numProcessedTokens = 0; tok.nextToken() != StreamTokenizer.TT_EOF; ++numProcessedTokens) {
            // Get the 32 bit word to be sent
            int word = (int) tok.nval;

            // Java code length at index 1 position in .jop
            if (numProcessedTokens == 1) {
                /*sysoutStream.println(word + " words of Java bytecode ("
                        + (word / 256) + " KB)");
                */
                nwords = word;

                monitor.beginTask(
                        IJOPLaunchConfigurationConstants.DOWNLOAD_TASK, nwords);
            }

            for (int i = 0; i < 4; i++) {
                byte b = (byte) (word >> ((3 - i) * 8));

                serialOut.write(b);
                ++numReplies;

                if (!usb) {
                    // TODO merge the two branches?
                    if (numProcessedTokens == i) {
                        // TODO check reply
                        serialIn.read();
                        --numReplies;
                    } else if (serialIn.available() != 0) {
                        serialIn.read();
                        --numReplies;
                    }
                }
            }

            monitor.worked(1); // So much work
        }

        // Finalize
        while (numReplies > 0) {
            serialIn.read();
            --numReplies;
        }
    }

    /**
     * Opens and configures a serial port 
     * 
     * @param commPortId
     * @return
     * @throws NoSuchPortException if the identifier does not specify a serial port
     * @throws PortInUseException if the port is in use
     * @throws UnsupportedCommOperationException if the settings are not supported
     */
    private SerialPort openSerialPort(CommPortIdentifier commPortId)
            throws NoSuchPortException, PortInUseException,
            UnsupportedCommOperationException {
        if (commPortId.getPortType() != CommPortIdentifier.PORT_SERIAL) {
            throw new gnu.io.NoSuchPortException();
        }

        SerialPort serialPort = (SerialPort) commPortId.open(
                IJOPLaunchConfigurationConstants.COMM_PORT_OWNER_ID,
                SERIAL_PORT_TIMEOUT);

        serialPort.setSerialPortParams(SERIAL_PORT_BAUDRATE,
                SERIAL_PORT_DATABITS, SERIAL_PORT_STOPBITS, SERIAL_PORT_PARITY);

        return serialPort;
    }
}
