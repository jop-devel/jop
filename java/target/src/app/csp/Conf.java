package csp;

public class Conf {

	public static final boolean CSP_USE_QOS = false;


	/** CSP Socket options */
	public static final boolean  CSP_USE_RDP  	= false;	// Require RDP
	public static final boolean  CSP_USE_XTEA	= false;	// Prohibit RDP
	public static final boolean  CSP_USE_HMAC 	= false;	// Require HMAC
	public static final boolean  CSP_USE_CRC32	= false;	// Dont use CRC

	public static final int CSP_MAX_BIND_PORT = 31;

	public static final int MAX_BUFFER_COUNT = 5;
	public static final int BUFFER_SIZE = 11;


}
