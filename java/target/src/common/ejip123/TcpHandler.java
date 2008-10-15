package ejip123;

/**
 Interface for classes using TcpConnections. Every class, that uses TcpConnections, be it as client or as server need to
 implement this interface.
 */
public interface TcpHandler{
/**
 Checks if the handler will accept more connections.

 @param newCon Another connection.
 @return True, if the handler won't accept further connections at the moment. False, if he will. *///TODO isBusy sinnvoll? is sowieso nicht threadsafe
public boolean isBusy(TcpConnection newCon);

/**
 Handles one received packet. The implementation should return as soon as possible and do its work in its own thread. If
 the status of the passed packet is not changed from {@link Packet#APP}, it will be reused by the underlying stack. An
 acknowledgment to the remote host is only sent, if the method returns true. Therefore, if the implementation returns
 false, the packet will be resent by the remote host after a while.

 @param con The connection the packet originates from.
 @param p   The received packet.
 @param off The offset in 32b-words where the payload starts.
 @return True, if the packet was processed. False, if the process could not handle it yet. */
public abstract boolean request(TcpConnection con, Packet p, int off);

/**
 A connection is fully established. Transfer can start now.

 @param newCon The newly established connection. */
public abstract void established(TcpConnection newCon);

/**
 The connection was closed by agreement. All data was received.

 @param closedCon The now closed connection. */
public void closed(TcpConnection closedCon);

/**
 The connection was closed by a failure. Data loss may have occurred.

 @param closedCon The now closed connection. */
public void reset(TcpConnection closedCon);

}
