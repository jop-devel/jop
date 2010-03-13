package csp;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;
// Network on Chip access primitives
// it should allow sending and receiving at the same time!!
public class NoC {
	// hardcoded, could be put in Const
	static final int NOC_MASK_ADDR = 0x00FF; // LS byte
	static final int NOC_MASK_BUSY = 0x0100;
	static final int NOC_MASK_SND = 0x0200;
	static final int NOC_MASK_RCV = 0x0400;
	static final int NOC_MASK_EOD = 0x0800;
	static final int NOC_MASK_SNDEMPTY = 0x1000;
	static final int NOC_MASK_SNDFULL = 0x2000;
	static final int NOC_MASK_RCVEMPTY = 0x4000;
	static final int NOC_MASK_RCVFULL = 0x8000;
	// registers read
	static final int NOC_REG_STATUS = Const.NOC_ADDR;
	static final int NOC_REG_RCVSRC = Const.NOC_ADDR | 0x01;
	static final int NOC_REG_RCVCNT = Const.NOC_ADDR | 0x02;
	static final int NOC_REG_RCVDATA = Const.NOC_ADDR | 0x03;
	// registers write
	static final int NOC_REG_RCVRESET = Const.NOC_ADDR;
	static final int NOC_REG_SNDDST = Const.NOC_ADDR | 0x01;
	static final int NOC_REG_SNDCNT = Const.NOC_ADDR | 0x02;
	static final int NOC_REG_SNDDATA = Const.NOC_ADDR | 0x03;
	
	// this node's address, must be initialized
	private static int myAddress = -1;
	
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
	public static boolean isEoD() {
		return (Native.rd(NOC_REG_STATUS) & NOC_MASK_EOD) != 0;
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
	
	
	// not that interesting really
	public static int getSourceAddress() {
		return Native.rd(NOC_REG_RCVSRC);
	}
		
	// static synchronized is not supported on JOP
	public static boolean sendIfFree(
				int dstAddr, int header, int cnt, int buf[]) {
		if(isSending()) return false;
		// can send data here
		Native.wr(NOC_REG_SNDDST, dstAddr);
		Native.wr(NOC_REG_SNDCNT, cnt+1); // account for the header
		Native.wr(NOC_REG_SNDDATA, header);
		int i=0;
		while(i<cnt) {
			while(!isSendBufferFull());
			Native.wr(NOC_REG_SNDDATA, buf[i]);
			i++;
		}
		return true;
	}
	
	public static int readData() {
		return Native.rd(NOC_REG_RCVDATA);
	}
	
	// call this to reset EoD flag and allow more receive!
	public static void writeReset() {
		Native.wr(NOC_REG_RCVRESET,0);
	}

	
	// Single word send and receive.
	// Params:
	//	- dstAddr : the NoC node address. this is automatically
	//              assigned to each node in Hw at bootup.
	//              For each node can be detected with NoC.thisAddress();
	//              It will only have values from 0 to N-1
	//  - d : any data
	public static void nb_send1(int dstAddr, int d) {
		Native.wr(NOC_REG_SNDDST, dstAddr);
		Native.wr(NOC_REG_SNDCNT, 1);
		Native.wr(NOC_REG_SNDDATA, d);
	}
	
	// receives from any source
	// check NoC.getSourceAddress() for the NoC node address of the
	// sender.
	public static int b_receive1() {
		while(!isReceiving());
		int d = Native.rd(NOC_REG_RCVDATA);
		// NoC.isEoD() should be true here for single word messages!
		//
		// does reset for more receive. after this,
		// the source, count and everything else may be faulty
		Native.wr(NOC_REG_RCVRESET,0); // aka writeReset();		
		return d;
	}
		
}