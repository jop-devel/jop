package yaffs2.port;

import yaffs2.utils.SerializableObject;

//XXX byte? // ???
public class yaffs_Spare extends yaffs2.utils.SerializableObject
{

	public yaffs_Spare(SerializableObject owner, int offset)
	{
		super(owner, offset);
	}

	public yaffs_Spare()
	{
		super(SERIALIZED_LENGTH);
	}
	
	/**
	 * Only for simulation.
	 */
	public yaffs_Spare(byte[] array, int offset)
	{
		super(array, offset);
	}

	public static final int SERIALIZED_LENGTH = 16; 

	/* Spare structure for YAFFS1 */
	public byte tagByte0()
	{
		return serialized[offset+0];
	}
	public void setTagByte0(byte value)
	{
		 serialized[offset+0] = value;
	}
	
	public byte tagByte1()
	{
		return serialized[offset+1];
	}
	public void setTagByte1(byte value)
	{
		 serialized[offset+1] = value;
	}
	
	public byte tagByte2()
	{
		return serialized[offset+2];
	}
	public void setTagByte2(byte value)
	{
		 serialized[offset+2] = value;
	}
	
	public byte tagByte3()
	{
		return serialized[offset+3];
	}
	public void setTagByte3(byte value)
	{
		 serialized[offset+3] = value;
	}
	
	public byte pageStatus()	/* set to 0 to delete the chunk */
	{
		return serialized[offset+4];
	}
	public void setPageStatus(byte value)
	{
		 serialized[offset+4] = value;
	}
	
	public byte blockStatus()
	{
		return serialized[offset+5];
	}
	public void setBlockStatus(byte value)
	{
		 serialized[offset+5] = value;
	}
	
	public byte tagByte4()
	{
		return serialized[offset+6];
	}
	public void setTagByte4(byte value)
	{
		 serialized[offset+6] = value;
	}
	
	public byte tagByte5()
	{
		return serialized[offset+7];
	}
	public void setTagByte5(byte value)
	{
		 serialized[offset+7] = value;
	}
	
	public byte[] ecc1() // = new byte[3];
	{
		return serialized;
	}
	public int ecc1Index()
	{
		return offset+8;
	}
	
	public byte tagByte6()
	{
		return serialized[offset+11];
	}
	public void setTagByte6(byte value)
	{
		 serialized[offset+11] = value;
	}
	
	public byte tagByte7()
	{
		return serialized[offset+12];
	}
	public void setTagByte7(byte value)
	{
		 serialized[offset+12] = value;
	}
	
	public byte[] ecc2() // = new byte[3];
	{
		return serialized;
	}
	public int ecc2Index()
	{
		return offset+13;
	}

	public int getSerializedLength()
	{
		return SERIALIZED_LENGTH;
	}
}
