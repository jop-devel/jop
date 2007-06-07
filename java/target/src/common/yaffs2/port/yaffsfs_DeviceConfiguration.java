package yaffs2.port;

public class yaffsfs_DeviceConfiguration
{
	public yaffsfs_DeviceConfiguration(byte[] prefix, int prefixIndex, yaffs_Device dev)
	{
		this.prefix = prefix;
		this.prefixIndex = prefixIndex;
		this.dev = dev;
	}
	
	byte[] prefix; int prefixIndex; 
	yaffs_Device dev;
}
