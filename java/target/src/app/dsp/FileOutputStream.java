
package dsp;

import joprt.RtThread;

import com.jopdesign.sys.*;


public class FileOutputStream {


	public static FatItS FatInterface = new FatItS();
	public static FatMmc MmcInterface = new FatMmc();





//####################################################################################
//					 FileOutputStream Section Begin
//####################################################################################	

public static boolean FileOutputStream(String file)
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
	
	FatInterface.printf( filename,'s');
	System.out.println();
	
	//System.out.println("Vor File Found!");
	
	int tmp = Clustervar[0];
	FOStream_Dir_cluster = Clustervar[0];
	
	if (FatInterface.fat_search_file(filename,Clustervar,Size,Dir_Attrib,FOStream_Buffer) == 1)
	{
		System.out.println("File Found!");
		
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
		
		System.out.println("Try to Create File !"+tmp);
		
		Clustervar[0] = tmp;
		//Clustervar[0] = 0;
		
		
		if (FatInterface.fat_add_file_ent( Clustervar, filename,  FOStream_Buffer) == 1)
		{
			System.out.println("Create File !");
			
			for (int h=0; h<13;h++)
			{
				FOStream_Filename[h]=filename[h];
				
				if(filename[h] == 0)
				{
					break;
				}
			}
			
			FOStream_size[0] = 1;
			FOStream_startcluster[0] = Clustervar[0];
			FOStream_offset_from_start[0] =0;
			FOStream_Dir_Attr = 0x20;
		}
		
		else
		{
			return (false);
		}
	}
	
	return (true);
}

public static boolean FileOutputStream(String filename, boolean append)
{

	if (!append)
	{
		return( FileOutputStream( filename));
	}

	else
	{
		if (!FileOutputStream( filename))
		{
			return (false);
		}
		
		FOStream_offset_from_start[0] = FOStream_size[0];
	}
	
	return (true);
}


public static boolean close ()
{

FOStream_Buffer  = null;
FOStream_size  = null;
FOStream_startcluster = null;
FOStream_offset_from_start = null;
FOStream_Filename = null;
return (true);
}


public static boolean write (byte[] b)
{
int i=0;

while (b[i] != 0)
{
	i++;
}

write (b,(int) FOStream_offset_from_start[0], i);

return (true);
}




public static boolean write (byte[] b, int off , int len)
{

int block;
int byte_count;
int Cluster=0;
long [] tmp1 = new long [1];
int [] tmp2 = new int [1];



//System.out.println("FOStream_size[0] = "+FOStream_size[0]);
//System.out.println("off = "+off);
if ((((long)(off+len)) )> (FOStream_size[0]+1 ))
{
	
	//System.out.println("in here ");
	
	if ((((long)(off+len))/FatInterface.BlockSize )!= (FOStream_size[0]/FatInterface.BlockSize ))
	{
		
		
		
		int diff = ((int)((long)(off+len))/FatInterface.BlockSize ) - (int)(FOStream_size[0]/FatInterface.BlockSize );
	
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
		
				//System.out.println("off = "+off);
				FOStream_size[0]=(long) (off+len-1);
				FatInterface.fat_set_dir_ent(FOStream_Dir_cluster,a,(long) (off+len-1),FOStream_Dir_Attr, FOStream_Buffer) ;	
				FatInterface.fat_load_reset();
				//System.out.println("a in search found = "+a);
				break;
			}
}
	
	
	}
}





FOStream_offset_from_start[0]=(long) off;
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
	// System.out.println("we");   
}


return (true);
}



public static int[] FOStream_Buffer  = new int [512];
public static long[] FOStream_size  = new long [1];
public static int[] FOStream_startcluster = new int [1];
public static long[] FOStream_offset_from_start = new long [1];
public static int[] FOStream_Filename = new int [13];
public static int FOStream_Dir_Attr;
public static int FOStream_Dir_cluster;
//####################################################################################
//					 FileOutputStream Section End
//####################################################################################	

}

