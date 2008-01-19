package sdcard;


//  Directory Entry Struct

public class DirEntry 
{
	public static int[]	DIR_Name = new int[11];     //8 chars filename
	public static int	DIR_Attr;         //file attributes RSHA, Longname, Drive Label, Directory
	public static int	DIR_NTRes;        //set to zero
	public static int	DIR_CrtTimeTenth; //creation time part in milliseconds
	public static int	DIR_CrtTime;      //creation time
	public static int	DIR_CrtDate;      //creation date
	public static int	DIR_LastAccDate;  //last access date
	public static int	DIR_FstClusHI;    //first cluster high word                 
	public static int	DIR_WrtTime;      //last write time
	public static int	DIR_WrtDate;      //last write date
	public static int	DIR_FstClusLO;    //first cluster low word                 
	public static int	DIR_FileSize;     

	
public static void set(int[] Buffer, int b)
{
	int a;

	for (a=0;a<11;a++)
	{
		DIR_Name[a] = ' ';
	}
	
	
	for (a=0;a<11;a++)
	{
		DIR_Name[a] = Buffer[b+a];
	}
	
//	printf(DIR_Name, 12,'s');
	
	DIR_Attr  = Buffer[b+11];
	DIR_NTRes  = Buffer[b+12];
	DIR_CrtTimeTenth  = Buffer[b+13];
	DIR_CrtTime = ((Buffer[b+14]&0x000000FF)  ) | ((Buffer[b+15]&0x000000FF) <<8);
	DIR_CrtDate = ((Buffer[b+16]&0x000000FF)  ) | ((Buffer[b+17]&0x000000FF) <<8);
	DIR_LastAccDate = ((Buffer[b+18]&0x000000FF)  ) | ((Buffer[b+19]&0x000000FF) <<8);
	DIR_FstClusHI = ((Buffer[b+20]&0x000000FF)  ) | ((Buffer[b+21]&0x000000FF) <<8);
	DIR_WrtTime = ((Buffer[b+22]&0x000000FF)  ) | ((Buffer[b+23]&0x000000FF) <<8);
	DIR_WrtDate = ((Buffer[b+24]&0x000000FF)  ) | ((Buffer[b+25]&0x000000FF) <<8);
	DIR_FstClusLO = ((Buffer[b+26]&0x000000FF)  ) | ((Buffer[b+27]&0x000000FF) <<8);
	DIR_FileSize = ((Buffer[b+28]&0x000000FF)  ) | ((Buffer[b+29]&0x000000FF) <<8) |
						((Buffer[b+30]&0x000000FF) << 16 ) | ((Buffer[b+31]&0x000000FF) <<24);
	
}
	
}