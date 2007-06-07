package yaffs2.utils;

public interface YYIELDInterface
{
	/**
	 * "added for use in scan so processes aren't blocked indefinitely."
	 * XXX PORT schedule() is only called only when compiling for Linux, possible problems?
	 */
	public void YYIELD();
}
