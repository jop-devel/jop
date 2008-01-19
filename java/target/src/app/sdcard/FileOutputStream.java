
package sdcard;

import joprt.RtThread;

import com.jopdesign.sys.*;


public class FileOutputStream {


	public static FatItS FatInterface = new FatItS();
	public static FatMmc MmcInterface = new FatMmc();
	public static int[] FOStream_Buffer  = new int [512];
	public static long[] FOStream_size  = new long [1];
	public static int[] FOStream_startcluster = new int [1];
	public static long[] FOStream_offset_from_start = new long [1];
	public static int[] FOStream_Filename = new int [13];
	public static int FOStream_Dir_Attr;
	public static int FOStream_Dir_cluster;




//####################################################################################
//					 FileOutputStream Section Begin
//####################################################################################	



/******************************************************************
*  public FileOutputStream(String file)
*  	Creates an output file stream to write to the file with the specified name. 
*
*	If the file exists but is a directory rather than a regular file, does not exist but cannot be created, 
*	or cannot be opened for any other reason then a ?????????????????????????????. 
*
*  Parameters:
*	file - Directories and files are seperated with "/". Do not write the leading "/" in the filename string.
******************************************************************/
public static boolean FileOutputStream(String file, boolean append)
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
		
		if (FatInterface.fat_search_file(subdir,Clustervar,Size,Dir_Attrib,FOStream_Buffer) != 1)
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
	
	//xxxxxxxFatInterface.printf( filename,'s');
	//xxxxxxxSystem.out.println();
	
	//System.out.println("Vor File Found!");
	
	int tmp = Clustervar[0];
	FOStream_Dir_cluster = Clustervar[0];
	
	if (FatInterface.fat_search_file(filename,Clustervar,Size,Dir_Attrib,FOStream_Buffer) == 1)
	{
		//xxxxxxxxxxxxxSystem.out.println("File Found!");
		
		for (int h=0; h<13;h++)
		{
			FOStream_Filename[h]=filename[h];
			
			if(filename[h] == 0)
			{
				break;
			}
		}
		
		FOStream_Dir_Attr = Dir_Attrib[0]; 
		FOStream_size[0] = Size[0];
		FOStream_startcluster[0] = Clustervar[0];
		FOStream_offset_from_start[0] =0;
	}
	
	else
	{
		
		//xxxxxxxxSystem.out.println("Try to Create File !"+tmp);
		
		Clustervar[0] = tmp;
		//Clustervar[0] = 0;
		
		
		if (FatInterface.fat_add_file_ent( Clustervar, filename,  FOStream_Buffer) == 1)
		{
			//xxxxxxxxxxxxSystem.out.println("Create File !");
			
			for (int h=0; h<13;h++)
			{
				FOStream_Filename[h]=filename[h];
				
				if(filename[h] == 0)
				{
					break;
				}
			}
			
			FOStream_size[0] = 0;
			FOStream_startcluster[0] = Clustervar[0];
			FOStream_offset_from_start[0] =0;
			FOStream_Dir_Attr = 0x20;
		}
		
		else
		{
			return (false);
		}
	}
	
	

if (append == false)
{	
	
	int Cluster=0;
	long [] tmp1 = new long [1];
	int [] tmp2 = new int [1];
	
	
	for (int a = 0;a < 100;a++)
{
	//System.out.println("a in search = "+a);
	Cluster = FatInterface.fat_read_dir_ent(FOStream_Dir_cluster,a,tmp1,tmp2,FOStream_Buffer);
	if (Cluster == 0xffff)
		{
		//System.out.println("a in search aus = "+a);
		tmp1 = null;
		tmp2 = null;
		return(false); //File not Found
		}
	if(FatInterface.strcasecmp(FOStream_Filename,FOStream_Buffer) == 0)
		{
		FatInterface.fat_set_dir_ent(FOStream_Dir_cluster,a, 0,FOStream_Dir_Attr, FOStream_Buffer) ;	
		FatInterface.fat_load_reset();
		//System.out.println("a in search found = "+a);
		FOStream_size[0] = 0;
		break;
		}
}
	
	tmp1 = null;
	tmp2 = null;
}
	
if (append == true)	
	{
	FOStream_offset_from_start[0] = FOStream_size[0];
	}
	
	return (true);
}




/******************************************************************
*  public FileOutputStream(String filename,
*                        boolean append)
*    	Creates an output file stream to write to the file with the specified name. 
*	If the second argument is true, then bytes will be written to the end of the file rather than the beginning. 
*
*	If the file exists but is a directory rather than a regular file, does not exist but cannot be created, 
*	or cannot be opened for any other reason then a ?????????????????????????????. 
*
*  Parameters:
*	file - Directories and files are seperated with "/". Do not write the leading "/" in the filename string.
*	append - if true, then bytes will be written to the end of the file rather than the beginning 
******************************************************************/
public static boolean FileOutputStream(String filename)
{
	return( FileOutputStream( filename, false));
}







/******************************************************************
*  public void close()
*	Closes this file output stream and releases any system resources associated with this stream. 
*	This file output stream may no longer be used for writing bytes. 
******************************************************************/
public static boolean close ()
{




FOStream_Buffer  = null;
FOStream_size  = null;
FOStream_startcluster = null;
FOStream_offset_from_start = null;
FOStream_Filename = null;
return (true);
}






/******************************************************************
*  public void write(byte[] b)
*   	Writes b.length or  while (b[i++] != 0) bytes from the specified byte array to this file output stream. 
*
*  Parameters:
*	b - the data. 
******************************************************************/
public static boolean write (byte[] b)
{
int i=0;

while ((b[i] != 0)&&(i<b.length))
{
	i++;
}

write (b,(int) FOStream_offset_from_start[0], i);

return (true);
}







/******************************************************************
*  public void write(byte[] b,
*                  int off,
*                  int len)
*	Writes len bytes from the specified byte array starting at offset off to this file output stream. 
*
*  Overrides:
*	write in class OutputStream
*  Parameters:
*	b - the data.
*	off - the start offset in the data.
*	len - the number of bytes to write. 
******************************************************************/
public static boolean write (byte[] b, int off , int len)
{

int block;
int byte_count;
int Cluster=0;
long [] tmp1 = new long [1];
int [] tmp2 = new int [1];



if (b.length < len)
{
	len = b.length;
}


//System.out.println("FOStream_size[0] = "+FOStream_size[0]);
//System.out.println("off = "+off);
if ((( FOStream_offset_from_start[0]+(long)(off+len)) )> (FOStream_size[0]+1 ))
{
	
	//System.out.println("in here ");
	
	if ((( FOStream_offset_from_start[0]+(long)(off+len))/FatInterface.BlockSize )!= (FOStream_size[0]/FatInterface.BlockSize ))
	{
		
		
		
		int diff = ((int)( FOStream_offset_from_start[0]+(long)(off+len))/FatInterface.BlockSize ) - (int)(FOStream_size[0]/FatInterface.BlockSize );
	
		//System.out.println("in there diff="+diff);
		
		for (int i=0; i<diff; i++ )
		{
			FatInterface.fat_add_cluster_to_end_of_chain(FOStream_startcluster[0]);
			FatInterface.fat_load_reset();
		}
	



		for (int a = 0;a < 100;a++)
		{
			//System.out.println("a in search = "+a);
			Cluster = FatInterface.fat_read_dir_ent(FOStream_Dir_cluster,a,tmp1,tmp2,FOStream_Buffer);
			if (Cluster == 0xffff)
			{
				//System.out.println("a in search aus = "+a);
				return(false); //File not Found
			}
		
			if(FatInterface.strcasecmp(FOStream_Filename,FOStream_Buffer) == 0)
			{
				//System.out.println("in set");
				//System.out.println("FOStream_size[0] = "+FOStream_size[0]);
				//System.out.println("off = "+off);
				//System.out.println("FOStream_offset_from_start[0] = "+FOStream_offset_from_start[0]);
				
				
				FOStream_size[0]= FOStream_offset_from_start[0]+(long) (off+len-1);
				FatInterface.fat_set_dir_ent(FOStream_Dir_cluster,a,FOStream_size[0],FOStream_Dir_Attr, FOStream_Buffer) ;	
				FatInterface.fat_load_reset();
				//System.out.println("a in search found = "+a);
				break;
			}
}
	
	
	}
}





FOStream_offset_from_start[0]= FOStream_offset_from_start[0] +(long)off;
//System.out.println("for for");

int k=0;

for (int i=0; i< ((len/FatInterface.BlockSize)+1); i++)
{

	block = ((int)FOStream_offset_from_start[0])/FatInterface.BlockSize;
	byte_count = ((int)FOStream_offset_from_start[0])%FatInterface.BlockSize;

	//System.out.println("for fat_read_file");
	FatInterface.fat_read_file (FOStream_startcluster[0],FOStream_Buffer,block);

	//System.out.println("for while"+byte_count);
	while ((byte_count<512) && (k<len))
	{
		//System.out.println("ks"+k);
		FOStream_Buffer[byte_count] = (int)b[k];
		//System.out.println("k"+k);
		k++;
		byte_count++;
		FOStream_offset_from_start[0]++;
		//System.out.println("k"+k);
		if ((int) FOStream_size[0] <= (int) FOStream_offset_from_start[0]) 
		{
			
			FOStream_size[0]++;
		}
		//System.out.println("ke"+k);
	}
	

	FOStream_offset_from_start[0]--;
	
	
	
	//System.out.println("for FatInterface.fat_write_file");
	FatInterface.fat_write_file (FOStream_startcluster[0],FOStream_Buffer,block);

	//System.out.println("FOStream_offset_from_start[0]++;");
	FOStream_offset_from_start[0]++;

	if (FOStream_size[0] <= FOStream_offset_from_start[0]) 
	{

		FOStream_size[0]++;

		
		//System.out.println("for if ( ((FOStream_offs");
		if ( ((FOStream_offset_from_start[0])% FatInterface.BlockSize) == 0)
		{	
			//System.out.println("FOStream_size[0] = "+FOStream_size[0]);
			//System.out.println("FOStream_offset_from_start[0] = "+FOStream_offset_from_start[0]);
			FatInterface.fat_add_cluster_to_end_of_chain(FOStream_startcluster[0]);
			FatInterface.fat_load_reset();
			//System.out.println("sd");
		}	


		//System.out.println("for for (int a = 0;a ");
		
		for (int a = 0;a < 100;a++)
		{
			//System.out.println("a in search = "+a);
			Cluster = FatInterface.fat_read_dir_ent(FOStream_Dir_cluster,a,tmp1,tmp2,FOStream_Buffer);
			if (Cluster == 0xffff)
			{
				//System.out.println("a in search aus = "+a);
				return(false); //File not Found
			}
		
			if(FatInterface.strcasecmp(FOStream_Filename,FOStream_Buffer) == 0)
			{
				FatInterface.fat_set_dir_ent(FOStream_Dir_cluster,a, (FOStream_size[0]-1),FOStream_Dir_Attr, FOStream_Buffer) ;	
				FatInterface.fat_load_reset();
				//System.out.println("a in search found = "+a);
				break;
			}
		}
	}

	if ( !(k<len))
	{
		break;
	}
}

return (true);
}






/******************************************************************
*  public void write(int b)
*   	Writes the specified byte to this file output stream. Implements the write method of OutputStream. 
*
*  Parameters:
*	b - the byte to be written. 
******************************************************************/
public static boolean write (int b)
{


int block;
int byte_count;


block = ((int)FOStream_offset_from_start[0])/FatInterface.BlockSize;
byte_count = ((int)FOStream_offset_from_start[0])%FatInterface.BlockSize;

FatInterface.fat_read_file (FOStream_startcluster[0],FOStream_Buffer,block);



FOStream_Buffer[byte_count] = b;
FatInterface.fat_write_file (FOStream_startcluster[0],FOStream_Buffer,block);




FOStream_offset_from_start[0]++;

if (FOStream_size[0] <= FOStream_offset_from_start[0]) 
{

 


	FOStream_size[0]++;

	if ( ((FOStream_offset_from_start[0])% FatInterface.BlockSize) == 0)
	{
		//System.out.println("FOStream_size[0] = "+FOStream_size[0]);
		//System.out.println("FOStream_offset_from_start[0] = "+FOStream_offset_from_start[0]);
		FatInterface.fat_add_cluster_to_end_of_chain(FOStream_startcluster[0]);
		FatInterface.fat_load_reset();
		//System.out.println("sd");
	}	

int Cluster=0;
long [] tmp1 = new long [1];
int [] tmp2 = new int [1];

for (int a = 0;a < 100;a++)
{
	//System.out.println("a in search = "+a);
	Cluster = FatInterface.fat_read_dir_ent(FOStream_Dir_cluster,a,tmp1,tmp2,FOStream_Buffer);
	if (Cluster == 0xffff)
		{
		//System.out.println("a in search aus = "+a);
		tmp1 = null; 
		tmp2 = null;
		return(false); //File not Found
		}
	if(FatInterface.strcasecmp(FOStream_Filename,FOStream_Buffer) == 0)
		{
		FatInterface.fat_set_dir_ent(FOStream_Dir_cluster,a, (FOStream_size[0]-1),FOStream_Dir_Attr, FOStream_Buffer) ;	
		FatInterface.fat_load_reset();
		//System.out.println("a in search found = "+a);
		tmp1 = null; 
		tmp2 = null;
		break;
		}
}
	// System.out.println("we");   


}



return (true);
}




//####################################################################################
//					 FileOutputStream Section End
//####################################################################################	

}

