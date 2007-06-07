package yaffs2.port;

public class yaffs_TempBuffer
{
	/*--------------------- Temporary buffers ----------------
	 *
	 * These are chunk-sized working buffers. Each device has a few
	 */

	//typedef struct {
	/**__u8 **/ byte[] buffer;
		int line;	/* track from whence this buffer was allocated */
		int maxLine;
	//} yaffs_TempBuffer;
}
