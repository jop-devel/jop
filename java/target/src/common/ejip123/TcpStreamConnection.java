package ejip123;

import joprt.RtThread;
import ejip123.util.Dbg;

/** A wrapper class emulating (non-blocking) sockets. */
public class TcpStreamConnection implements TcpHandler{
final static public int ESTABLISHED = 0;
final static public int CLOSED = 1;

private int status = CLOSED;
private TcpConnection tc = null;
private Packet in = null;
private int inOff = 0;
private Packet out = null;
private int outOff = 0;
private int timeout = Integer.MAX_VALUE;

/**
 @param prio Priority
 @param us   Period in microseconds */
public TcpStreamConnection(int prio, int us){
	new RtThread(prio, us){
		public void run(){
			for(; ;){
				waitForNextPeriod();
				loop();
			}
		}
	};
}

private void loop(){
	synchronized(this){
		if(out == null){
			out = PacketPool.getFreshPacket();
			outOff = Tcp.OFFSET<<2;
		} else{
			if((timeout - ((int)(System.currentTimeMillis()))) < 0)
				flush();

		}
	}
}

public boolean isBusy(TcpConnection newCon){
	synchronized(this){
		return status == ESTABLISHED;
	}
}

public boolean request(TcpConnection con, Packet p, int off){
	synchronized(this){
		if(con != tc || in != null)
			return false;

		p.setStatus(Packet.ALLOC);
		in = p;
		inOff = off<<2;
		return true;
	}
}

public void established(TcpConnection newCon){
	synchronized(this){
		if(tc == null){
			tc = newCon;
			status = ESTABLISHED;
		} else
			newCon.close();
	}
}

public void closed(TcpConnection closedCon){
	synchronized(this){
		if(closedCon == tc){
			status = CLOSED;
			tc = null;
			Dbg.wr("con closed in stream\n");
		}
	}
}

public void reset(TcpConnection closedCon){
	synchronized(this){
		if(closedCon == tc){
			status = CLOSED;
			tc = null;
			Dbg.wr("con reset in stream\n");
		}
	}
}

/**
 Checks for unread data.

 @return Number of unread bytes ready to be read. */
public int freeToRead(){
	synchronized(this){
		if(in == null)
			return 0;
		else
			return (in.len()) - inOff;
	}
}

/**
 Reads one byte from the stream.

 @return The next byte in the stream, or -1 if there is currently nothing to be read. */
public int read(){
	synchronized(this){
		if(in == null)
			return -1;
		int ret = ((in.buf[inOff>>2]>>>(24 - ((inOff&3)<<3)))&0xff);
		inOff++;
		if(inOff >= in.len()){
			in.free();
			in = null;
		}
		return ret;
	}
}

/**
 Checks the send buffer.

 @return Number of free bytes in the send buffer. */
public int freeToWrite(){
	synchronized(this){
		if(out == null || tc == null)
			return 0;
		else
			return out.buf.length - outOff;
	}
}

/**
 Writes one byte to the stream.

 @param octet The octet to be written.
 @return True on success, false if there is no free buffer. */
public boolean write(int octet){
	synchronized(this){
		if(out == null)
			return false;
		int place = outOff&0x3;
		int off = outOff>>2;
		int t = out.buf[off];
		switch(place){
			case 1:
				out.buf[off] = (t&0xff000000)&((octet&0xff)<<16);
				break;
			case 2:
				out.buf[off] = (t&0xff0000)&((octet&0xff)<<8);
				break;
			case 3:
				out.buf[off] = t&0xff00&(octet&0xff);
				break;
			default:
				out.buf[off] = octet<<24;
				break;
		}

		if(++outOff >= PacketPool.PACKET_SIZE()){
			flush();
		}
		return true;
	}
}

/** Opens a connections to with the given parameters. */
public boolean open(int remIp, int locIp, int remPort, int locPort){
	return tc.open(remIp, locIp, remPort, locPort, this);
}

public int getStatus(){
	return status;
}

/**
 Writes the whole content of a String to the stream.

 @param cs The CharSequence
 @return Number of bytes written (0 on failure). */
public int write(CharSequence cs){
	if(out == null)
		return 0;
	int cnt = out.setData(outOff, cs);
	Dbg.wr("wrote ");
	Dbg.wr(cnt);
	Dbg.lf();
	outOff += cnt;
	int nxtOff = outOff + 1;
	if(nxtOff >= PacketPool.PACKET_SIZE()){
		flush();
	}
	return cnt;
}

/** Ensures that all previous written data is sent to the remote host. */
public void flush(){
	synchronized(this){
		if(out != null){
			out.setLen(outOff);
//			out.print(0);
			if(tc.send(out, true)){
				out = null;
				Dbg.wr("sent ok\n");
			} else{
				Dbg.wr("sent failed! setting timeout\n");
				timeout = (int)(System.currentTimeMillis() + 500);
			}
		}
	}
}
}