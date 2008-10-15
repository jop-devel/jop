package ejip123;

import ejip123.util.Dbg;

/** TCP main class. Used for managing TcpConnections and handlers. */
public class Tcp{

/** TCP protocol number. */
public static final int PROTOCOL = 6;

/** Offset of data in words when no options present. */
public static final int OFFSET = Ip.OFFSET + 5;

private static final Object lock = new Object();

private static int MAX_HANDLER = 0;
private static TcpHandler[] list = null;
private static int[] ports = null;
private static TcpConnection[] connections = null;
/** Maximum number of active TCP connections. */
private static int MAX_CON = 0;

private Tcp(){
}

public static void init(int handlerCnt, int concurrConnCnt){
	MAX_HANDLER = handlerCnt <= 0 ? 4 : handlerCnt;
	MAX_CON = handlerCnt <= 0 ? MAX_HANDLER<<1 : concurrConnCnt;

	list = new TcpHandler[MAX_HANDLER];
	ports = new int[MAX_HANDLER];
	connections = new TcpConnection[Tcp.MAX_CON];
	for(int i = 0; i < Tcp.MAX_CON; ++i){
		connections[i] = new TcpConnection();
	}
}

/* Called periodic from Net for timeout processing. */
static void loop(int cur){
	synchronized(lock){
		for(int i = 0; i < MAX_CON; ++i){
			TcpConnection tc = connections[i];
			tc.loop(cur);

		}
	}
}

/* Processes packet and generate reply if necessary. */
static void process(Packet p, int off){
	int[] buf = p.buf;
	int src = buf[3];
	int dst = buf[4];
	// inject pseudo header for tcp checksum into original packet
	// for packets w/o options: ttl, prot and ip checksum are overwritten
	buf[off - 1] = dst;
	buf[off - 2] = src;
	buf[off - 3] = (PROTOCOL<<16) + p.len() - (off<<2);
	if(Ip.chkSum(buf, off - 3, p.len() - ((off - 3)<<2)) != 0){
		Dbg.wr("tcp checksum failed!\n");
		p.free();
		return;
	}

	int dstPort = buf[off];
	int srcPort = dstPort>>>16;
	dstPort &= 0xffff;

	synchronized(lock){
		TcpConnection free = null;
		for(int i = 0; i < MAX_CON; ++i){
			TcpConnection tc = connections[i];
			if(tc.isUsed()){
				if(tc.processPacket(src, dst, srcPort, dstPort, p, off)){
					return;
				}
			} else{
				free = tc;
			}
		}
		// if not found get a new one if possible or drop the packet
		if(free != null){
//			Dbg.wr("new con\n");
			free.newIncoming(src, dst, srcPort, dstPort, p, off);
		} else{
			// TODO rst? port unreachable? protocol unreachable?
			p.free();
		}
	}
}

static int getMaxPayload(int dstIp){
	//  int max = Ip.getMaxPayload(dstIp) - ((Tcp.OFFSET - Ip.OFFSET)<<2);
	//	Dbg.wr("max local link payload=");
	//	Dbg.intVal(max);
	return Ip.getMaxPayload(dstIp) - ((Tcp.OFFSET - Ip.OFFSET)<<2);
}

/**
 Opens a client connection. Fetches an available connection from the pool, sets it up and tries to connect to the remote
 host. Local IP and port will be chosen by the implementation.

 @param dstIp   The remote IP.
 @param dstPort The remote port.
 @param th      The TcpHandler, that will handle events for this connection.
 @return The fetched connection of null if there was an error. */
public static TcpConnection open(int dstIp, int dstPort, TcpHandler th){
	return open(dstIp, dstPort, dstPort, th);
}

/**
 Opens a client connection. Fetches an available connection from the pool, sets it up and tries to connect to the remote
 host. Local IP will be chosen by the implementation.

 @param dstIp   The remote IP.
 @param srcPort The local port.
 @param dstPort The remote port.
 @param th      The TcpHandler, that will handle events for this connection.
 @return The fetched connection of null if there was an error. */
public static TcpConnection open(int dstIp, int srcPort, int dstPort, TcpHandler th){
	int srcIp = Ip.getSrcIp(dstIp);
	return open(srcIp, dstIp, srcPort, dstPort, th);
}

/**
 Opens a client connection. Fetches an available connection from the pool, sets it up and tries to connect to the remote
 host.

 @param srcIp   The local IP.
 @param dstIp   The remote IP.
 @param srcPort The local port.
 @param dstPort The remote port.
 @param th      The TcpHandler, that will handle events for this connection.
 @return The fetched connection of null if there was an error. */
public static TcpConnection open(int srcIp, int dstIp, int srcPort, int dstPort, TcpHandler th){
	synchronized(lock){
		// check that there is no for that port
		if(th == null || getHandler(srcPort) != null)
			return null;

		// check that there is no matching connection yet
		TcpConnection free = null;
		for(int i = 0; i < MAX_CON; ++i){
			TcpConnection tc = connections[i];
			if(tc.isUsed()){
				if(tc.matches(srcIp, dstIp, srcPort, dstPort)){
					return null;
				}
			} else{
				free = tc;
			}
		}
		if(free != null && free.open(dstIp, srcIp, dstPort, srcPort, th))
			return free;
	}
	return null;
}

/**
 Registers a TcpHandler for a port.

 @param port The port.
 @param h    The handler.
 @return True, if successfully registered. False, if list is full or there is already a handler registered for that
 port. */
public static boolean addHandler(int port, TcpHandler h){
	synchronized(lock){
		int free = -1;
		for(int i = 0; i < MAX_HANDLER; ++i){
			if(list[i] == null){
				free = i;
			} else if(ports[i] == port){
				return false;
			}
		}

		if(free != -1){
			ports[free] = port;
			list[free] = h;
			return true;
		}
		return false;
	}
}

/**
 Removes a TCP handler from the list. No new connections will be forwarded to that handler. Existing connection (even not
 yet fully established ones) are not touched by this. A handler has to close all existing non reset connections before it
 totally abandons them.

 @param port The port the handler wants to be listening to.
 @return false if it was not in the list. */
public static boolean removeHandler(int port){
	synchronized(lock){
		for(int i = 0; i < MAX_HANDLER; ++i){
			if(list[i] != null && ports[i] == port){
				list[i] = null;
				return true;
			}
		}
	}
	return false;
}

/**
 Fetches a handler.

 @param port The TCP port in question.
 @return The TcpHandler registered for that port or null if there is none. */
static TcpHandler getHandler(int port){
	TcpHandler th = null;
	// is a handler registered for that port?
	synchronized(lock){
		for(int i = 0; i < MAX_HANDLER; ++i){
			if(list[i] != null && ports[i] == port){
				th = list[i];
				break;
			}
		}
	}
	return th;
}
}
