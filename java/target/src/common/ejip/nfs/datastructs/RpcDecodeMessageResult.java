package ejip.nfs.datastructs;

public class RpcDecodeMessageResult {
	
	//these constants are not defined by some standard - they are used as return values 
	public static final byte RPC_DECODE_MSG_RESULT_OK = 0;
	public static final byte RPC_DECODE_MSG_RESULT_UNSUPPORTED_AUTH_FLAVOR 	= 1;
	public static final byte RPC_DECODE_MSG_RESULT_AUTH_BODY_NOT_0 	= 2;
	public static final byte RPC_DECODE_MSG_RESULT_PROG_UNAVAIL = 3;
	public static final byte RPC_DECODE_MSG_RESULT_PROG_VERSION_MISMATCH = 4;
	public static final byte RPC_DECODE_MSG_RESULT_PROC_UNAVAIL = 5;
	public static final byte RPC_DECODE_MSG_RESULT_GARBAGE_ARGS = 6;
	public static final byte RPC_DECODE_MSG_RESULT_STATUS_DENIED = 7;
	public static final byte RPC_DECODE_MSG_RESULT_MSG_TYPE_NOT_REPLY = 8;
	public static final byte RPC_DECODE_MSG_RESULT_MSG_EMPTY = 9;



	public int xid;
	public byte error;
}
