package yaffs2.utils;

public interface YYIELDInterface
{
	/**
	 * "added for use in scan so processes aren't blocked indefinitely."
	 * PORT Originally, schedule() is only called only when compiling for Linux.
	 */
	public void YYIELD();
}
