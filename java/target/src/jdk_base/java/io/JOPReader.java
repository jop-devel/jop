package java.io;

import joprt.RtThread;
import rtlib.Buffer;

public class JOPReader extends JOPInputStream implements Runnable {

	private static Buffer buf;
	private static int lines = 0;

	public JOPReader(int size) {
		Buffer nbuf;
		if (buf == null || buf.capacity() < size) {
			buf = new Buffer(size);			
		}
	}

	public void run() {
		while (true) {
			loop();
			boolean madePeriod = RtThread.currentRtThread().waitForNextPeriod();
// 			if (!madePeriod) {
// 				System.out.print('R');
// 				System.out.println(i);
// 			}
		}
	}

	private void loop() {
		int i = 0;
		while (i < 32
			   && available() > 0
			   && !buf.full()) {
			int c = read_unchecked();
			buf.write(c);
			if (c < 0 || c == '\n') {
				synchronized(buf) {
					lines++;
				}
			}
			i++;
		}
	}

	public String readLine() throws IOException {
		BoundedStringBuffer sb = new BoundedStringBuffer(buf.capacity());
		boolean eol = false;
		while (!eol) {
			// wait until data becomes available
			while (buf.empty()); 
			int c = buf.read();			
			if (c >= 0 && c != '\n') {
				sb.append((char)c);
			} else {
				eol = true;
			}

		}
		synchronized(buf) {
			lines--;
		}
		return sb.toString();
	}

	public int availableLines() {
		return lines;
	}

}