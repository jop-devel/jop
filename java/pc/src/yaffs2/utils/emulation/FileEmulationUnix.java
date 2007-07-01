package yaffs2.utils.emulation;

import java.io.*;

import yaffs2.port.*;

public abstract class FileEmulationUnix
{
	/**
	 * 
	 * @param path
	 * @param pathIndex
	 * @param oflag Ignored. Access mode is always "rw".
	 * @param permission Ignored.
	 * @return
	 */
	public static RandomAccessFile open(String path, int oflag, int permission)
	{
		return open(path, oflag);
	}
	
	public static RandomAccessFile open(String path, int oflag)
	{
		try
		{
			return new RandomAccessFile(path, "rw");
		}
		catch (FileNotFoundException e)
		{
			throw new HiddenException();			
		}
	}

	public static int write(RandomAccessFile fildes, byte[] buf, int bufIndex, int nbyte)
	{
		try
		{
			fildes.write(buf, bufIndex, nbyte);
		}
		catch(IOException e)
		{
			throw new HiddenException();
		}

		return nbyte;
	}

	public static int read(RandomAccessFile fildes, byte[] buf, int bufIndex, int nbyte)
	{
		try
		{
			return fildes.read(buf, bufIndex, nbyte);
		}
		catch(IOException e)
		{
			throw new HiddenException();
		}
	}

	public static void close(RandomAccessFile f)
	{
		try
		{
			f.close();
		}
		catch(IOException e)
		{
			throw new HiddenException();
		}
	}

	public static int lseek(RandomAccessFile fildes, int offset, int whence)
	{
		try
		{
			fildes.seek(((whence == yaffsfs_H.SEEK_END) ? fildes.length() : 
				((whence == yaffsfs_H.SEEK_CUR) ? fildes.getFilePointer() : + 
						0)) + offset);

			return (int)fildes.getFilePointer();
		}
		catch(IOException e)
		{
			throw new HiddenException();
		}
	}
	
	public static byte[] IntToByteArray(int value)
	{
		byte[] result = new byte[yaffs2.utils.Constants.SIZEOF_INT];
		yaffs2.utils.Utils.writeIntToByteArray(result, 0, value);
		
		return result;
	}
}
