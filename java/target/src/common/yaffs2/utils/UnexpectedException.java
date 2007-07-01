package yaffs2.utils;

public class UnexpectedException extends RuntimeException
{
	public UnexpectedException()
	{
	}
	
	public UnexpectedException(Throwable cause)
	{
		super(cause);
	}
	
	public UnexpectedException(String message)
	{
		super(message);
	}
	
}
