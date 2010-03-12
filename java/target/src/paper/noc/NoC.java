package noc;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

// Network on Chip access primitives
// it should allow sending and receiving at the same time!!
public class NoC {
	// hardcoded, could be put in Const
	final static int NOC_MASK_ADDR = 0x00FF; // LS byte
	final static int NOC_MASK_BUSY = 0x0100;
	final static int NOC_MASK_SND = 0x0200;
	final static int NOC_MASK_RCV = 0x0400;
	final static int NOC_MASK_SNDEMPTY = 0x1000;
	final static int NOC_MASK_SNDFULL = 0x2000;
	final static int NOC_MASK_RCVEMPTY = 0x4000;
	final static int NOC_MASK_RCVFULL = 0x8000;
	// registers read
	final static int NOC_REG_STATUS = Const.NOC_ADDR;
	final static int NOC_REG_RCVSRC = Const.NOC_ADDR | 0x01;
	final static int NOC_REG_RCVCNT = Const.NOC_ADDR | 0x02;
	final static int NOC_REG_RCVDATA = Const.NOC_ADDR | 0x03;
	// registers write
	final static int NOC_REG_RESET = Const.NOC_ADDR;
	final static int NOC_REG_SNDDST = Const.NOC_ADDR | 0x01;
	final static int NOC_REG_SNDCNT = Const.NOC_ADDR | 0x02;
	final static int NOC_REG_SNDDATA = Const.NOC_ADDR | 0x03;
	
	// this node's address, must be initialized
	private static int myAddress = -1;
	// send used variables
	private static Thread sndowner = null;
	
	// receive used variables
	private static boolean noHeader = true;
	private static Thread rcvowner = null;
	private static int rcvheader = 0;
	private static int rcvsource = 0;
	
	// just take the current node address for later use
	public static void initialize() {
		myAddress = Native.rd(NOC_REG_STATUS) & NOC_MASK_ADDR;
	}
	
	public static int thisAddress() {
		return myAddress;
	}
	
	// some flag accesses
	public static boolean isBusy() {
		return (Native.rd(NOC_REG_STATUS) & NOC_MASK_BUSY) != 0;
	}
	public static boolean isSending() {
		return (Native.rd(NOC_REG_STATUS) & NOC_MASK_SND) != 0;
	}
	public static boolean isReceiving() {
		return (Native.rd(NOC_REG_STATUS) & NOC_MASK_RCV) != 0;
	}
	public static boolean isSendBufferFull() {
		return (Native.rd(NOC_REG_STATUS) & NOC_MASK_SNDFULL) != 0;
	}
	public static boolean isSendBufferEmpty() {
		return (Native.rd(NOC_REG_STATUS) & NOC_MASK_SNDEMPTY) != 0;
	}
	public static boolean isReceiveBufferFull() {
		return (Native.rd(NOC_REG_STATUS) & NOC_MASK_RCVFULL) != 0;
	}
	public static boolean isReceiveBufferEmpty() {
		return (Native.rd(NOC_REG_STATUS) & NOC_MASK_RCVEMPTY) != 0;
	}
	
	public static int getSourceAddress() {
		return Native.rd(NOC_REG_RCVSRC);
	}
	// fast functions, 1 word, no header, no busy check, no finish check
	public static void nb_send1(int dstAddr, int d) {		
		Native.wr(NOC_REG_SNDDST, dstAddr);
		Native.wr(NOC_REG_SNDCNT, 1);
		Native.wr(NOC_REG_SNDDATA, d);
	}
	
	public static boolean nb_receive1(int srcAddr, int[] w) {
		System.out.println(isReceiving());
		System.out.println(getSourceAddress());
		if(!isReceiving()) return false;
		if(srcAddr != getSourceAddress()) return false;
		w[0] = Native.rd(NOC_REG_RCVDATA);
		return true;
	}
	
	// MS: to make it compile Thread. CurrentThread is commented out.
	// it's not part of java.lang.Thread and we don't have j.l.T on JOP
	
	// this is a function that is called repeatedly to send
	// a few words from a message
	// the header is used for matching at destination
	// (could be the local channel id)
	public static int sendWords(int header, int dstAddr, 
				int n, int buffer[]) {
//	public static synchronized int sendWords(int header, int dstAddr, 
//			int n, int buffer[]) {
		int i = 0;
		if(sndowner == null) {
			// nobody sends anything at this time
			// also the send FIFO must be empty
			if(isSending()) return n;
			// must initialize sending channel
//			sndowner = Thread.CurrentThread;
			// initialize hardware send
			// specify address
			Native.wr(NOC_REG_SNDDST, dstAddr);
			// specify total count
			Native.wr(NOC_REG_SNDCNT, n+1);
			// ready to push data
			// send the header first!
			Native.wr(NOC_REG_SNDDATA, header);			
		}
		
//		if(sndowner == Thread.CurrentThread) {
//			// keep on sending
//			while(!isSendBufferFull() && i < n) {
//				Native.wr(NOC_REG_SNDDATA, buffer[i]);
//				i++;
//			}
//			
//			// we might have reached the end of the message
//			if(i==n) {
//				// it is not sure all the words in the send FIFO
//				// were sent, but isSending should take care of that
//				sndowner = null;
//			}
//		}
		
		return n-i;		
	}
	
	// now a function to receive messages
	// This is more tricky...
	// A thread initiates reception only if it is the destination
	// of the message! header and source must match
	public static int receiveWords(int header, int srcAddr, 
			int n, int buffer[]) {
//	public static synchronized int receiveWords(int header, int srcAddr, 
//			int n, int buffer[]) {
		int i = 0;
		if(noHeader) {
			// nobody receiving anything yet
			// if is not receiving, then this is not interesting
			if(!isReceiving()) return n;
			// there is data avilable, first one is the header!
			rcvheader = Native.rd(NOC_REG_RCVDATA);
			// also save the source
			rcvsource = Native.rd(NOC_REG_RCVSRC);
			// we got the header
			noHeader = false;
		} else {
			// there is a header, but not necessarily a destination
			if(rcvowner == null) {
				// see if we can match the destination
				if(header == rcvheader && srcAddr == rcvsource) {
					// not sure we need srcAddr too, but safer this way
//					rcvowner = Thread.CurrentThread;
				} else
					// not this destination, just return
					return n;
			}
			
//			if(rcvowner == Thread.CurrentThread) {
//				// now we can receive some
//				// keep on sending
//				while(!isReceiveBufferEmpty() && i < n) {
//					buffer[i] = Native.rd(NOC_REG_RCVDATA);
//					i++;
//				}
//				
//				if(i == n) {
//					// received all the wanted words.
//					// hopefully there is nothing more in
//					// the FIFO, or we're screwed
//					rcvowner = null;
//					noHeader = true;
//				}
//			}
		}
		return n-i;
	}
	
	
}
