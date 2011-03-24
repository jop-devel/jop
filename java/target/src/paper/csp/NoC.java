package csp;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;
// Network on Chip access primitives
// it should allow sending and receiving at the same time!!
public class NoC {
	// hardcoded, could be put in Const
	public static final int NOC_MASK_ADDR = 0x00FF; // LS byte
	public static final int NOC_MASK_BUSY = 0x0100;
	public static final int NOC_MASK_SND = 0x0200;
	public static final int NOC_MASK_RCV = 0x0400;
	public static final int NOC_MASK_EOD = 0x0800;
	public static final int NOC_MASK_SNDEMPTY = 0x1000;
	public static final int NOC_MASK_SNDFULL = 0x2000;
	public static final int NOC_MASK_RCVEMPTY = 0x4000;
	public static final int NOC_MASK_RCVFULL = 0x8000;
	// registers read
	public static final int NOC_REG_STATUS = Const.NOC_ADDR;
//  deprecated
//	static final int NOC_REG_RCVCNT = Const.NOC_ADDR | 0x01;
	public static final int NOC_REG_RCVSLOTS = Const.NOC_ADDR | 0x01;
	public static final int NOC_REG_RCVSRC = Const.NOC_ADDR | 0x02;
	public static final int NOC_REG_RCVDATA = Const.NOC_ADDR | 0x03;
	// registers write
	public static final int NOC_REG_RCVRESET = Const.NOC_ADDR;
	public static final int NOC_REG_SNDCNT = Const.NOC_ADDR | 0x01;
	public static final int NOC_REG_SNDDST = Const.NOC_ADDR | 0x02;
	public static final int NOC_REG_SNDDATA = Const.NOC_ADDR | 0x03;
	
	// this node's address, must be initialized
//	private static int myAddress = -1;
	
	// just take the current node address for later use
/*
	public static void initialize() {
		myAddress = Native.rd(NOC_REG_STATUS) & NOC_MASK_ADDR;
	}
*/	
	public static int thisAddress() {		
//		throw new Error("Static fields are shared for all cores!");
// bummer
//		return myAddress;
		return Native.rd(NOC_REG_STATUS) & NOC_MASK_ADDR;
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
	
	// shows the slots containing data as a bit map
	// 1 means data present, 0 means not Data or EoD
	// use to implement ALT or PRI ALT
	public static int getDataSlots() {
		return Native.rd(NOC_REG_RCVSLOTS);
	}
		
	public static boolean sendIfFree(
				int dstAddr, int header, int cnt, int buf[]) {
		if(isSending()) return false;
		// can send data here
		Native.wr(dstAddr, NOC_REG_SNDDST);
		Native.wr(cnt+1, NOC_REG_SNDCNT); // account for the header
		Native.wr(header, NOC_REG_SNDDATA);
		int i=0;
		while(i<cnt) {
			while(!isSendBufferFull());
			Native.wr(buf[i], NOC_REG_SNDDATA);
			i++;
		}
		return true;
	}
	
	public static int readData() {
		return Native.rd(NOC_REG_RCVDATA);
	}
	
	// call this to reset EoD flag and allow more receive!
	public static void writeReset() {
		Native.wr(0, NOC_REG_RCVRESET);
	}

	// same as writeReset, but with the possibility to ignore slots
	// 1 on position K means ignore slots from node K
	// 0 means receive, to be backward compatible
	public static void writeResetMask(int bitmap) {
		Native.wr(bitmap, NOC_REG_RCVRESET);
	}

	
	// Single word send and receive.
	// Params:
	//	- dstAddr : the NoC node address. this is automatically
	//              assigned to each node in Hw at bootup.
	//              For each node can be detected with NoC.thisAddress();
	//              It will only have values from 0 to N-1
	//  - d : any data
	public static void nb_send1(int dstAddr, int d) {
		Native.wr(dstAddr, NOC_REG_SNDDST);
		Native.wr(1, NOC_REG_SNDCNT);
		Native.wr(d, NOC_REG_SNDDATA);
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
		Native.wr(0, NOC_REG_RCVRESET); // aka writeReset();		
		return d;
	}
		
}