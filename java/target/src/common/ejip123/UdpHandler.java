package ejip123;

/** Interface for UDP server classes. Implementing classes can be registered as handler for incoming packets on a UDP port. */
public interface UdpHandler{
/**
 Gets called from UDP whenever a packet arrives at a port the implementing class is registered for.
 <p/>
 The UDP payload can be found beginning at the offset specified by <code>offset</code>. The received packet has status
 {@link Packet#APP}. If this is not changed before returning, the packet will be freed. There are two possibilities how
 one should implement this method:<p> 1. If processing doesn't take too long (realtime wise), use the received packet
 immediately to generate the reply.<br/> 2. Else just save a reference to the packet for later processing (in another
 thread) and set the packet to {@link Packet#ALLOC}.

 @param p      The received packet.
 @param offset The offset in {@link Packet#buf} where the data/udp payload starts. */
public abstract void request(Packet p, int offset);
}
