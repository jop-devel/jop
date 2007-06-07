package yaffs2.port;

// PORT No need to be serializable.
public class yaffs_NANDSpare {
	
	/*Special structure for passing through to mtd */
	
	public yaffs_Spare spare = new yaffs_Spare();
	public int eccres1;
	public int eccres2;
}
