
package sdcard;

import joprt.RtThread;

import com.jopdesign.sys.*;


public class FileInputStream {


	public static FatItS FatInterface = new FatItS();
	public static FatMmc MmcInterface = new FatMmc();


	public static int[] FIStream_Buffer  = new int [512];
	public static long[] FIStream_size  = new long [1];
	public static int[] FIStream_startcluster = new int [1];
	public static long[] FIStream_offset_from_start = new long [1];


//####################################################################################
//					 FileInputStream Section Begin
//####################################################################################	




/*****************************************************************
*  public FileInputStream(String name)
*	Creates a FileInputStream by opening a connection to an actual file, the file named by the path name name in the file system.
*
*	If the named file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading then ??????????????????????? 
*
*	Parameters:
*	name 		Directories and files are seperated with "/". Do not write the leading "/" in the filename string.
*****************************************************************/
public static boolean FileInputStream(String file)
{
	int [] Clustervar = new int[1];
	long [] Size = new long[1];
	int [] Dir_Attrib = new int[1];
	int i = 0;
	char [] subdir= new char[13];
	int inroot = 0;
	char [] filename = new char[25]; 
	
	
	while (i<file.length())
	{
		filename[i]= (char) (file.getBytes())[i];
		i++;
	}
		
	i=0;
	
	
	
	
	FatInterface.fat_cluster_data_store();

	if (filename[0] == 0)
	{
		return (false);
	}
	
	while (filename[i] != 0)
	{
		if (filename[i] == '/')
		{
			inroot ++;
		}
	
		i++;
	}
	
	//System.out.println("inroot ="+inroot);
	//System.out.println("i ="+i);
	
	if ( (i > 12) && ( inroot == 0) ) 
	{
		return (false);
	}
	
	
	
	Clustervar[0] = 0;
	i = 0; 
	int j =0;
	
	for(int b=0; b<inroot; b++)
	{
		j=0;
		
		//System.out.println("b ="+b);
		
		while (filename[i] != '/')
		{
			subdir[j]=filename[i];
			if ( i > 11 ) 
			{
				return (false);
			}
			
			j++;
			i++;
		}
		
		j++;
		i++;
		subdir[j] =0;
		
		if (FatInterface.fat_search_file(subdir,Clustervar,Size,Dir_Attrib,FIStream_Buffer) != 1)
		{
			return (false);
		}
	
	}
	
	//System.out.println("i ="+i);
	//System.out.println("Vor inroot != 0");
	if (inroot != 0)
	{
		for(int b=0; b<13; b++)
		{
			
			//System.out.println("b ="+b);
			filename[b] = filename[b+i];
			
			if (filename[b+i] == 0)
			{
				//System.out.println("b ="+b);
				inroot = 0;
				break;
			}
		}
	
		if ( inroot != 0)
		{
			return (false);
		}
		
	}
	
	//FatInterface.printf( filename,'s');
	//System.out.println();
	
	//System.out.println("Vor File Found!");
	
	if (FatInterface.fat_search_file(filename,Clustervar,Size,Dir_Attrib,FIStream_Buffer) == 1)
	{
		//System.out.println("File Found!");
		
		FIStream_size[0] = Size[0];
		FIStream_startcluster[0] = Clustervar[0];
		FIStream_offset_from_start[0]=0;

	}
	
	else
	{
		return (false);
	}
	
	return (true);
}




/*****************************************************************
*  public int read(byte[] b)
*    	Reads up to b.length bytes of data from this input stream into an array of bytes. 
*	This method blocks until some input is available. 
*
*  Parameters:
*	b - the buffer into which the data is read. 
*  Returns:
*	the total number of bytes read into the buffer, or -1 if there is no more data because the end of the file has been reached. 
*****************************************************************/
public static int read ()
{
int block;
int byte_count;

if (FIStream_offset_from_start[0] >=  FIStream_size[0])
{
	return (-1);
}


block = ((int)FIStream_offset_from_start[0])/FatInterface.BlockSize;
byte_count = ((int)FIStream_offset_from_start[0])%FatInterface.BlockSize;

FatInterface.fat_read_file (FIStream_startcluster[0],FIStream_Buffer,block);

FIStream_offset_from_start[0]++;

return ((int) FIStream_Buffer[byte_count]);

}




/****************************************************************
*  public int read(byte[] b)
*   	Reads up to b.length bytes of data from this input stream into an array of bytes. 
*	This method blocks until some input is available. 
*
*  Parameters:
*	b - the buffer into which the data is read. 
*  Returns:
*	the total number of bytes read into the buffer, or -1 if there is no more data because the end of the file has been reached. 
****************************************************************/
public static int read (byte b[])
{
int block=0xFFFFFFFF, block_alt;
int byte_count, i=0;



if (FIStream_offset_from_start[0] >=  FIStream_size[0])
{
	return (-1);
}


while (! (FIStream_offset_from_start[0] >=  FIStream_size[0]) && (i<b.length))
{


	block_alt =block;
	block = ((int)FIStream_offset_from_start[0])/FatInterface.BlockSize;
	byte_count = ((int)FIStream_offset_from_start[0])%FatInterface.BlockSize;

	if (block_alt != block)
	{
		FatInterface.fat_read_file (FIStream_startcluster[0],FIStream_Buffer,block);
	}

	
	FIStream_offset_from_start[0]++;

	b[i]= (byte) FIStream_Buffer[byte_count];
	i++;
}

return(i);

}





/****************************************************************
*  public int read(byte[] b,
*      	int off,
*      	int len)
*	Reads up to len bytes of data from this input stream into an array of bytes. 
*	This method blocks until some input is available. 
*
*  Parameters:
*	b - the buffer into which the data is read.
*	off - the start offset of the data.
*	len - the maximum number of bytes read. 
*  Returns:
*	the total number of bytes read into the buffer, or -1 if there is no more data because the end of the file has been reached. 
****************************************************************/
public static int read (byte b[], int off, int len)
{


if (FIStream_offset_from_start[0] >=  FIStream_size[0])
{
	return (-1);
}

if ((FIStream_offset_from_start[0]+off) >= FIStream_size[0])
{
	return (-1);
}

FIStream_offset_from_start[0] =FIStream_offset_from_start[0]+off;


int block=0xFFFFFFFF, block_alt;
int byte_count, i=0;


while ( (! (FIStream_offset_from_start[0] >=  FIStream_size[0])) && (i<len) && (i<b.length))
{


	block_alt =block;
	block = ((int)FIStream_offset_from_start[0])/FatInterface.BlockSize;
	byte_count = ((int)FIStream_offset_from_start[0])%FatInterface.BlockSize;

	if (block_alt != block)
	{
		FatInterface.fat_read_file (FIStream_startcluster[0],FIStream_Buffer,block);
	}

	
	FIStream_offset_from_start[0]++;

	b[i]= (byte) FIStream_Buffer[byte_count];
	i++;
}

return(i);


}


/*****************************************************************
*  public long skip(long n)
*      	Skips over and discards n bytes of data from the input stream. 
*	The skip method may, for a variety of reasons, end up skipping over some smaller number of bytes, possibly 0. 
*	The actual number of bytes skipped is returned. 
* 
* 
*  Parameters:
*  n - the number of bytes to be skipped. 
*  Returns:
*  the actual number of bytes skipped. 
***************************************************************/

public static long skip (long n)
{
long k=0;

if (FIStream_offset_from_start[0] >=FIStream_size[0])
{
	return (0);
}


if ((FIStream_offset_from_start[0]+n) > FIStream_size[0])
{
	k= FIStream_size[0] - FIStream_offset_from_start[0];
	FIStream_offset_from_start[0]=FIStream_offset_from_start[0]+k;
	return (k);
}

FIStream_offset_from_start[0] =FIStream_offset_from_start[0]+n;

return (n);
}


/**************************************************************
*  public void close()
 *     	Closes this file input stream and releases any system resources associated with the stream. 
**************************************************************/

public static void close()
{
FIStream_Buffer =null;
FIStream_size =null;
FIStream_startcluster =null;
FIStream_offset_from_start =null;
FatInterface =null;
MmcInterface =null;
}


//####################################################################################
//					 FileOutputStream Section End
//####################################################################################	


	
	
	
}