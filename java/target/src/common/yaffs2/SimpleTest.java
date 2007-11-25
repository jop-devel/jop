package yaffs2;

import yaffs2.port.yaffsfs_C;
import yaffs2.port.yaffsfs_H;
import yaffs2.utils.EmulationUtils;
import yaffs2.utils.Utils;

public class SimpleTest {

	protected static final String mountPoint = "/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		yaffs2.utils.Globals.startUp(
				new yaffs2.platform.jop.yaffscfg2k(),
				new yaffs2.platform.jop.PortConfiguration(),
				null);

		yaffs2.utils.Globals.configuration.yaffs_StartUp();

		yaffsfs_C.yaffs_mount(Utils.StringToByteArray(mountPoint), 0);

		int handle = yaffsfs_C.yaffs_open(Utils.StringToByteArray(mountPoint + "/a"), 0, 
				yaffsfs_H.O_RDWR | yaffsfs_H.O_CREAT | yaffsfs_H.O_TRUNC, 
				yaffsfs_H.S_IREAD | yaffsfs_H.S_IWRITE);

		byte[] buf = { '1', '2', '3', '4', 0};

		yaffsfs_C.yaffs_write(handle, buf, 0, buf.length);

		yaffsfs_C.yaffs_lseek(handle, 0, yaffsfs_H.SEEK_SET);

//		yaffsfs_C.yaffs_close(handle);
//
//		handle = yaffsfs_C.yaffs_open(Utils.StringToByteArray(mountPoint + "/a"), 0, 
//				yaffsfs_H.O_RDONLY, 
//				yaffsfs_H.S_IREAD);

		byte[] rbuf = new byte[buf.length];

		yaffsfs_C.yaffs_read(handle, rbuf, 0, rbuf.length);

		System.out.println("Read: " + EmulationUtils.byteArrayToString(rbuf, 0));

		yaffsfs_C.yaffs_close(handle);
		yaffsfs_C.yaffs_unmount(Utils.StringToByteArray(mountPoint), 0);
	}

}
