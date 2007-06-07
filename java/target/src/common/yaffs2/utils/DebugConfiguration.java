package yaffs2.utils;

public interface DebugConfiguration
{
	public int __LINE__();
//	{			
//		return new Exception().getStackTrace()[1].getLineNumber();
//	}
	
	public String __FILE__();
//	{
//		return new Exception().getStackTrace()[1].getFileName();
//	}
}
