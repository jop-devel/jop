package ejip123;

/**
 Interface for pinging other hosts. This interface needs to be implemented by classes, that wanna send ping requests and
 be informed later.
 */
public interface PingReplyHandler{
/**
 Callback method for ICMP ping replies. Gets called by Icmp, when a reply is received, if there is a registered handler
 and the identification matches.

 @param ms Time between sending the last request and receiving a reply. */
public abstract void pingReply(int ms);

/** Callback method for ICMP ping timeouts. Gets calls by Icmp, when a ping request times out. */
public abstract void pingTimeout();
}
