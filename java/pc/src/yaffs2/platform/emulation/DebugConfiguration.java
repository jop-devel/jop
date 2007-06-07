package yaffs2.platform.emulation;

public class DebugConfiguration implements yaffs2.utils.DebugConfiguration
{
	public int __LINE__()
	{
		return new Exception().getStackTrace()[2].getLineNumber();
	}

	public String __FILE__()
	{
		return new Exception().getStackTrace()[2].getFileName();
	}

}
