
package sdcard;

import joprt.RtThread;

import com.jopdesign.sys.*;



public class FatItS {

//####################################################################################
//					 FAT DEF Section BEGIN
//####################################################################################

//Block Size in Bytes
public static final int  BlockSize=512;

//Master Boot Record
public static final int MASTER_BOOT_RECORD=0;

//Volume Boot Record location in Master Boot Record
public static final int VBR_ADDR=0x1C6;

//define ASCII
public static final int SPACE = 0x20;
public static final int DIR_ENTRY_IS_FREE=  0xE5;
public static final int FIRST_LONG_ENTRY	=0x01;
public static final int SECOND_LONG_ENTRY	=0x42;

//define DIR_Attr
public static final int ATTR_LONG_NAME		=0x0F;
public static final int ATTR_READ_ONLY		=0x01;
public static final int ATTR_HIDDEN			=0x02;
public static final int ATTR_SYSTEM			=0x04;
public static final int ATTR_VOLUME_ID		=0x08;
public static final int ATTR_DIRECTORY		=0x10;
public static final int ATTR_ARCHIVE		=0x20;



//FAT12 and FAT16 Structure Starting at Offset 36
public static final int BS_DRVNUM			=36;
public static final int BS_RESERVED1		=37;
public static final int BS_BOOTSIG			=38;
public static final int BS_VOLID			=39;
public static final int BS_VOLLAB			=43;
public static final int BS_FILSYSTYPE		=54;

//FAT32 Structure Starting at Offset 36
public static final int BPB_FATSZ32			=36;
public static final int BPB_EXTFLAGS		=40;
public static final int BPB_FSVER			=42;
public static final int BPB_ROOTCLUS		=44;
public static final int BPB_FSINFO			=48;
public static final int BPB_BKBOOTSEC		=50;
public static final int BPB_RESERVED		=52;

public static final int FAT32_BS_DRVNUM		=64;
public static final int FAT32_BS_RESERVED1	=65;
public static final int FAT32_BS_BOOTSIG	=66;
public static final int FAT32_BS_VOLID		=67;
public static final int FAT32_BS_VOLLAB		=71;
public static final int FAT32_BS_FILSYSTYPE	=82;
//End of Boot Sctor and BPB Structure

public static int cluster_size;
public static int fat_offset;
public static int fat_size;
public static int number_of_fat;
public static int cluster_offset;
public static int volume_boot_record_addr;
public static int[] tmp_buffer = new int[BlockSize];
public static int[] fat_buffer = new int[BlockSize];		
public static int Block_fat_buffer;
public static int[] fat_buffer2 = new int[BlockSize];		
public static int Block_fat_buffer2;
public static int[] fat_buffer3 = new int[BlockSize];		
public static int Block_fat_buffer3;
public static int Block_fat_buffer_akt;
public static int fat_load_cluster_tmp=-1;
public static int fat_load_cluster_st=-1;
public static int fat_load_a_tmp=0;

public static int test=0;



public static FatMmc MmcInterface = new FatMmc();


//####################################################################################
//					 FAT DEF Section END
//####################################################################################










//####################################################################################
//					 FAT Section Begin
//####################################################################################


public static int fat_addr (int[] Buffer)
{
	int volume_boot_record_addr;
    
	//auslesen des Master Boot Record von der MMC/SD Karte (addr = 0)
	MmcInterface.mmc_read_sector (MASTER_BOOT_RECORD,Buffer); //Read Master Boot Record
  //  System.out.println("WO");
	volume_boot_record_addr = Buffer[VBR_ADDR] + (Buffer[VBR_ADDR+1] << 8);
	//Berechnet Volume Boot Record 
	//System.out.println("WO");
	MmcInterface.mmc_read_sector (volume_boot_record_addr,Buffer); //Read Master Boot Record
    return (volume_boot_record_addr);
}


public static int  fat_root_dir_addr (int[] Buffer) 
{
	BootSec bootp = new BootSec(); //Zeiger auf Bootsektor Struktur
	int FirstRootDirSecNum;
	//auslesen des Volume Boot Record von der MMC/SD Karte 
	MmcInterface.mmc_read_sector (volume_boot_record_addr,Buffer);
	bootp.set(Buffer);

	//berechnet den ersten Sector des Root Directory
	FirstRootDirSecNum = ( bootp.BPB_RsvdSecCnt +
	                       (bootp.BPB_NumFATs * bootp.BPB_FATSz16));

	FirstRootDirSecNum+= volume_boot_record_addr;


	fat_size=bootp.BPB_FATSz16;
	number_of_fat=bootp.BPB_NumFATs;
	
	/*
	printf (bootp.BPB_BytesPerSec,'x'); //2 bytes   0x000b
	System.out.println();
	printf (bootp.BPB_SecPerClus,'x'); //1bytes 0x000d
	System.out.println();		
	printf (bootp.BPB_RsvdSecCnt,'x'); //2 bytes 0x000e
	System.out.println();
	printf (bootp.BPB_NumFATs,'x'); //1bytes 0x0010
	System.out.println();
	printf (bootp.BPB_RootEntCnt,'x'); //2 bytes   0x0011
	System.out.println(); 
	printf (bootp.BPB_TotSec16,'x'); //2 bytes   0x0013
	System.out.println(); 
	printf (bootp.BPB_Media,'x'); //1bytes   0x0015
	System.out.println(); 
	printf ((int) ' ' ,'s');
	printf (bootp.BPB_FATSz16,'x'); //2bytes   0x0016
	System.out.println(); 
	printf (bootp.BPB_SecPerTrk,'x'); //2bytes   0x0018
	System.out.println();
	printf (bootp.BPB_NumHeads,'x'); //2bytes   0x001a
	System.out.println();
	printf (bootp.BPB_HiddSec,'x'); //4bytes   0x001c
	System.out.println();
	printf (bootp.BPB_TotSec32,'x'); //4bytes   0x0020
	System.out.println();


*/


	bootp = null;
	return(FirstRootDirSecNum);
}


public static void fat_load (	int Cluster, 		//Angabe Startcluster
				int[] Block,
				int[] TMP_Buffer) 	//Workingbuffer
{
	//Zum Überprüfen ob der FAT Block schon geladen wurde
	int FAT_Block_Store = 0;	

	//Byte Adresse innerhalb des Fat Blocks
	int FAT_Byte_Addresse;	

	//FAT Block Adresse
	int FAT_Block_Addresse;
	//System.out.println("START");
	//Berechnung für den ersten FAT Block (FAT Start Addresse)
	for (int a = 0;;a++)
	{	
		if (a == Block[0])
			{
			Block[0] = (0x0000FFFF & Cluster);
			return;
			}
		
		if (Cluster == 0xFFFF)
			{
			Block[0] = 0xFFFF;
			break; //Ist das Ende des Files erreicht Schleife beenden
			}
		//Berechnung des Bytes innerhalb des FAT Block´s
		FAT_Byte_Addresse = (Cluster*2) % BlockSize;
			
		//Berechnung des Blocks der gelesen werden muß
		FAT_Block_Addresse = ((Cluster*2) / BlockSize) + 
								volume_boot_record_addr + fat_offset;	
		//Lesen des FAT Blocks
		//Überprüfung ob dieser Block schon gelesen wurde
		if (FAT_Block_Addresse != FAT_Block_Store)
			{
			//System.out.println("FAT_Block_Addresse= "+FAT_Block_Addresse);
			//System.out.println("FAT_Block_Store= "+FAT_Block_Store);
			
			FAT_Block_Store = FAT_Block_Addresse;
			//Lesen des FAT Blocks
			MmcInterface.mmc_read_sector (FAT_Block_Addresse,TMP_Buffer);	
			}

		//Lesen der nächsten Clusternummer
		Cluster = (TMP_Buffer[FAT_Byte_Addresse + 1] << 8) + 
					TMP_Buffer[FAT_Byte_Addresse];		
	}
	return;
}


public static void fat_load (	int Cluster, 		//Angabe Startcluster
				int[] Block) 
{
	//Zum Überprüfen ob der FAT Block schon geladen wurde
	//int FAT_Block_Store = 0;	

	//Byte Adresse innerhalb des Fat Blocks
	int FAT_Byte_Addresse;	

	if (fat_load_cluster_st != Cluster)
		{
		fat_load_a_tmp=0;
		fat_load_cluster_st = Cluster;
		}
	
	else 
		{
		Cluster = fat_load_cluster_tmp;
		}
	
	//FAT Block Adresse
	int FAT_Block_Addresse;
	//System.out.println("START");
	//Berechnung für den ersten FAT Block (FAT Start Addresse)
	for (int a = fat_load_a_tmp;;a++)
	{	
		//System.out.println(a);
		if (a == Block[0])
			{
			Block[0] = (0x0000FFFF & Cluster);
			
			fat_load_cluster_tmp = Cluster;
			fat_load_a_tmp=a;
			return;
			}
		
		if (Cluster == 0xFFFF)
			{
			fat_load_cluster_st = -1;
			fat_load_cluster_tmp = -1;
			fat_load_a_tmp=0;
			
			break; //Ist das Ende des Files erreicht Schleife beenden
			}
		//Berechnung des Bytes innerhalb des FAT Block´s
		FAT_Byte_Addresse = (Cluster*2) % BlockSize;
			
		//Berechnung des Blocks der gelesen werden muß
		FAT_Block_Addresse = ((Cluster*2) / BlockSize) + 
								volume_boot_record_addr + fat_offset;	
		//Lesen des FAT Blocks
		//Überprüfung ob dieser Block schon gelesen wurde
		
		if (FAT_Block_Addresse == Block_fat_buffer)
			{
			Cluster = (((int)fat_buffer[FAT_Byte_Addresse + 1] )<< 8) + 
				((int)fat_buffer[FAT_Byte_Addresse]);		
			}
		else if (FAT_Block_Addresse == Block_fat_buffer2)
			{
			Cluster = (((int)fat_buffer2[FAT_Byte_Addresse + 1] )<< 8) + 
				((int)fat_buffer2[FAT_Byte_Addresse]);	
			}
		else if (FAT_Block_Addresse == Block_fat_buffer3)
			{
			Cluster = (((int)fat_buffer3[FAT_Byte_Addresse + 1] )<< 8) + 
				((int)fat_buffer3[FAT_Byte_Addresse]);		
			}

		else
			{
			
			if (Block_fat_buffer_akt==1)
				{
				Block_fat_buffer = FAT_Block_Addresse;
				//Lesen des FAT Blocks
				MmcInterface.mmc_read_sector (FAT_Block_Addresse,fat_buffer);	
				Block_fat_buffer_akt=2;
				//Lesen der nächsten Clusternummer
				Cluster = ((fat_buffer[FAT_Byte_Addresse + 1] )<< 8) + 
				(fat_buffer[FAT_Byte_Addresse]);		

				}
			
			else if (Block_fat_buffer_akt==2)
				{
				Block_fat_buffer2 = FAT_Block_Addresse;
				//Lesen des FAT Blocks
				MmcInterface.mmc_read_sector (FAT_Block_Addresse,fat_buffer2);	
				Block_fat_buffer_akt=3;
				//Lesen der nächsten Clusternummer
				Cluster = ((fat_buffer2[FAT_Byte_Addresse + 1] )<< 8) + 
				(fat_buffer2[FAT_Byte_Addresse]);	

				}
			
			else  
				{
				Block_fat_buffer3 = FAT_Block_Addresse;
				//Lesen des FAT Blocks
				MmcInterface.mmc_read_sector (FAT_Block_Addresse, fat_buffer3);	
				Block_fat_buffer_akt=1;
				//Lesen der nächsten Clusternummer
				Cluster = ((fat_buffer3[FAT_Byte_Addresse + 1] )<< 8) + 
				(fat_buffer3[FAT_Byte_Addresse]);		

				}
			
			}

	}
	return;
}


public static void fat_load_reset()
{
	Block_fat_buffer_akt=1;
	Block_fat_buffer=0;
	Block_fat_buffer2=0;
	Block_fat_buffer3=0;
fat_load_cluster_tmp=-1;
fat_load_cluster_st=-1;
}



public static void fat_cluster_data_store ()
{
	BootSec bootp = new BootSec(); //Zeiger auf Bootsektor Struktur

	Block_fat_buffer_akt=1;
	Block_fat_buffer=0;
	Block_fat_buffer2=0;
	Block_fat_buffer3=0;

	
	volume_boot_record_addr = fat_addr (tmp_buffer);	
 
	MmcInterface.mmc_read_sector (volume_boot_record_addr,tmp_buffer);

    bootp.set(tmp_buffer);

	cluster_size = bootp.BPB_SecPerClus;

	fat_offset = bootp.BPB_RsvdSecCnt;

	cluster_offset = ((bootp.BPB_BytesPerSec * 32)/BlockSize);	
	cluster_offset += fat_root_dir_addr(tmp_buffer);

	bootp = null;
}



public static void fat_read_file (int Cluster,//Angabe des Startclusters vom File
				 int[] Buffer,	  //Workingbuffer
				 int BlockCount)	  //Angabe welcher Bock vom File geladen 
										      //werden soll a 512 Bytes
{
	//Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
	//Berechnung welcher Cluster zu laden ist
	int[] Block = new int[1];
	
	Block[0] = (BlockCount/cluster_size);
	
	//Auslesen der FAT - Tabelle
	fat_load (Cluster,Block);			 
	Block[0] = ((Block[0]-2) * cluster_size) + cluster_offset;
	//Berechnung des Blocks innerhalb des Cluster
	Block[0] += (BlockCount % cluster_size);
	//Read Data Block from Device
	MmcInterface.mmc_read_sector (Block[0],Buffer);	
	Block = null;
	return;
}



public static void fat_write_file (int cluster,//Angabe des Startclusters vom File
					int[] buffer,	  //Workingbuffer
					int blockCount)	  //Angabe welcher Bock vom File gespeichert 
									  //werden soll a 512 Bytes
{
	//Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
	//Berechnung welcher Cluster zu speichern ist

	int[] block = new int[1];
	
	block[0] = (blockCount/cluster_size);
		
	//Auslesen der FAT - Tabelle
	fat_load (cluster,block);			 

	
	block[0] = ((block[0]-2) * cluster_size) + cluster_offset;
	//Berechnung des Blocks innerhalb des Cluster
	block[0] += (blockCount % cluster_size);
	//Write Data Block to Device
	MmcInterface.mmc_write_sector (block[0],buffer);	

	block = null;
	
	return;
}



public static int fat_search_file (char[] File_Name,		//Name des zu suchenden Files
							int[] Cluster, 	//Angabe Dir Cluster welches
											//durchsucht werden soll
											//und Rückgabe des clusters
											//vom File welches gefunden
											//wurde
							long[] Size, 		//Rückgabe der File Größe
							int[] Dir_Attrib,//Rückgabe des Dir Attributs
							int[] Buffer) 	//Working Buffer
{
	int Dir_Cluster_Store = Cluster[0];
	for (int a = 0;a < 100;a++)
	{
		//System.out.println("a in search = "+a);
		Cluster[0] = fat_read_dir_ent(Dir_Cluster_Store,a,Size,Dir_Attrib,Buffer);
		if (Cluster[0] == 0xffff)
			{
			//System.out.println("a in search aus = "+a);
			return(0); //File not Found
			}
		if(strcasecmp(File_Name,Buffer) == 0)
			{
			return(1); //File Found
			}
	}
	return(2); //Error
}




public static int fat_search_file (int[] File_Name,		//Name des zu suchenden Files
							int[] Cluster, 	//Angabe Dir Cluster welches
											//durchsucht werden soll
											//und Rückgabe des clusters
											//vom File welches gefunden
											//wurde
							long[] Size, 		//Rückgabe der File Größe
							int[] Dir_Attrib,//Rückgabe des Dir Attributs
							int[] Buffer) 	//Working Buffer
{
	int Dir_Cluster_Store = Cluster[0];
	for (int a = 0;a < 100;a++)
	{
		//System.out.println("a in search = "+a);
		Cluster[0] = fat_read_dir_ent(Dir_Cluster_Store,a,Size,Dir_Attrib,Buffer);
		if (Cluster[0] == 0xffff)
			{
			//System.out.println("a in search aus = "+a);
			return(0); //File not Found
			}
		if(strcasecmp(File_Name,Buffer) == 0)
			{
			return(1); //File Found
			}
	}
	return(2); //Error
}



public static int strcasecmp(int[] File_Name, int[] Buffer)
{
	int i;

	
for (i=0;i<12;i++)
	{
		if ( File_Name[i] != Buffer[i] )
		{
			return(1);
		}
	
		if ( (File_Name[i]==0 ) && ( Buffer[i]==0))
		{
			return(0);
		}
	
		if ( (File_Name[i]==0 ) || ( Buffer[i]==0))
		{
			return(1);
		}
	}

return(0);
}

public static int strcasecmp(char[] File_Name, int[] Buffer)
{
	int i;

	
for (i=0;i<12;i++)
	{
		if ( File_Name[i] != Buffer[i] )
		{
			return(1);
		}

		if ( (File_Name[i]==0 ) && ( Buffer[i]==0))
		{
			return(0);
		}
	
		if ( (File_Name[i]==0 ) || ( Buffer[i]==0))
		{
			return(1);
		}

	}

return(0);
}

// Filname fehler das nur volle 8.3 char gehn
public static int   fat_read_dir_ent (int dir_cluster, //Angabe Dir Cluster
					int Entry_Count,   //Angabe welcher Direintrag
					long[] Size, 		   //Rückgabe der File Größe
					int[] Dir_Attrib,   //Rückgabe des Dir Attributs
					int[] Buffer) 	   //Working Buffer
{
	int TMP_Entry_Count = 0;
	int[] Block2 = new int[1];
	int Block=0;
	DirEntry dir = new DirEntry(); //Zeiger auf einen Verzeichniseintrag
	int b=0;
	int k=0;
	

	if (dir_cluster == 0)
		{
		Block = fat_root_dir_addr(Buffer);
		}
	else
		{
		//Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
		//Berechnung welcher Cluster zu laden ist
		//Auslesen der FAT - Tabelle
		fat_load (dir_cluster,Block2,Buffer);			 
		Block = Block2[0];
		Block = ((Block-2) * cluster_size) + cluster_offset;
		}

	//auslesen des gesamten Root Directory
	for (int blk = Block;;blk++)
	{
		MmcInterface.mmc_read_sector (blk,Buffer);	//Lesen eines Blocks des Root Directory
		for (int a=0;a<BlockSize; a = a + 32)
		{

		 

		 dir.set(Buffer,a); //Zeiger auf aktuellen Verzeichniseintrag holen
		 
	 
			if (dir.DIR_Name[0] == 0) //Kein weiterer Eintrag wenn erstes Zeichen des Namens 0 ist
			{
			dir=null;
			Block2 =null;
			return (0xFFFF);
			}
			
			//Prüfen ob es ein 8.3 Eintrag ist
			//Das ist der Fall wenn es sich nicht um einen Eintrag für lange Dateinamen
			//oder um einen als gelöscht markierten Eintrag handelt.
   			//System.out.println("dir.DIR_Attr "+dir.DIR_Attr);
			//System.out.println("dir.DIR_Name[0]"+dir.DIR_Name[0]);
			
			if ((dir.DIR_Attr != ATTR_LONG_NAME) &&
				(dir.DIR_Name[0] != DIR_ENTRY_IS_FREE)) 
			{
				//System.out.println("a not long "+a);
				//Ist es der gewünschte Verzeichniseintrag
				if (TMP_Entry_Count == Entry_Count) 
				{
					
					for(b=0;b<12;b++)
					{
						Buffer[b] = 0;
					}
					
					//System.out.println("TMP_Entry_Count Entry_Count "+a);
					//Speichern des Verzeichnis Eintrages in den Rückgabe Buffer
					for(b=0;b<11;b++)
					{
					//System.out.println("FOR IF="+b);
					if (dir.DIR_Name[b] != SPACE)
						{
						if (b < 8)
							{
							Buffer[b]=dir.DIR_Name[b];
							}
						
						if (b == 8)
							{
							Buffer[b-k]= '.';
							Buffer[b+1-k]=dir.DIR_Name[b];
							}
						
						if (b > 8)
							{
							Buffer[b+1-k]=dir.DIR_Name[b];
							}	
						
						}
					
					
					else
						{
						
						if (b < 8)
							{
							Buffer[b]=0;
							}
						
						if (b == 8)
							{
							Buffer[b]=0;
							Buffer[b+1]= 0;
							}
						
						if (b > 8)
							{
							Buffer[b+1]=0;;
							}	
						
						k++;
						}
					
					
					}						
					//b++;
					Buffer[12]=0x00;
					
					//printf(Buffer,'s');
					
					//System.out.println();
					Dir_Attrib[0] = dir.DIR_Attr;

					//Speichern der Filegröße
					Size[0]=dir.DIR_FileSize;
					
					//Speichern des Clusters des Verzeichniseintrages
					dir_cluster = dir.DIR_FstClusLO;

					//Eintrag gefunden Rücksprung mit Cluster File Start
					
					Block2 =null;
					dir=null;
					return(dir_cluster);
				}
			
			TMP_Entry_Count++;
			}
			
		
		}
	}

//	return (0xFFFF); //Kein Eintrag mehr gefunden Rücksprung mit 0xFFFF

}






public static void fat_get_free_cluster (	 		//und setze ihn auf 0xFFFF
				int[] Block,
				int[] TMP_Buffer) 	//Workingbuffer
{
	int Cluster=0;

	//Byte Adresse innerhalb des Fat Blocks
	int FAT_Byte_Addresse;	

	//FAT Block Adresse
	int FAT_Block_Addresse;
	//System.out.println("START");
	//Berechnung für den ersten FAT Block (FAT Start Addresse)

		FAT_Block_Addresse = volume_boot_record_addr + fat_offset;	

	//System.out.println("FAT_Block_Addresse ="+FAT_Block_Addresse);
	for (int a = 0;;a++)
	{	
		if (  !(FAT_Block_Addresse < fat_size) )
			{
			Block[0] = 0xFFFFFFFF ;
			return;
			}
		
		
		
		//System.out.println("a"+a);
		
		//Berechnung des Bytes innerhalb des FAT Block´s
	//	FAT_Byte_Addresse = (Cluster*2) % BlockSize; //praktisch (cluster<<1)&0x01FF
			
		//Lesen des FAT Blocks
		MmcInterface.mmc_read_sector (FAT_Block_Addresse,TMP_Buffer);	
		
		for (int b = 0;b<512;b=b+2)
		{	
			if ( ((TMP_Buffer[b]&0x000000FF)==0x00) && ((TMP_Buffer[b+1]&0x000000FF)==0x00))
			{
				//System.out.println("b"+b);
				
				Block[0] = ( ( (( FAT_Block_Addresse -volume_boot_record_addr - fat_offset)& 0x000000FF )<<8) | ( (b/2) &0x000000FF));  
				
				TMP_Buffer[b+0] = 0xFF;
				TMP_Buffer[b+1] = 0xFF;
				MmcInterface.mmc_write_sector (FAT_Block_Addresse,TMP_Buffer);
				MmcInterface.mmc_write_sector ((FAT_Block_Addresse+fat_size),TMP_Buffer);
				return;
				// return (     (     (    FAT_Block_Addresse & 0x000000FF )<<8) | (  (  b/2  ) &0x000000FF)  );  
			}
		}
		
		FAT_Block_Addresse ++;	
	}
	//return;
}


public static void fat_add_cluster_to_end_of_chain(int Cluster)
{
	int last_cluster = 0xFFFFFFFF;
	//UINT32 next_cluster = StartCluster;
	int[] Block = new int[1];
	int i=0;
	
	
	//System.out.println("START1");
	//System.out.println("Cluster"+Cluster);
	
	fat_load_reset();
	Block[0] = (i);
	fat_load(Cluster, Block);
	last_cluster=(Block[0] & 0x0000FFFF );
	
	//System.out.println("last_cluster"+last_cluster);
	
	// Loop until end of chain
	while ( (Block[0] & 0x0000FFFF )<0x0000FFF8 )
	{
		i++;
		last_cluster=(Block[0] & 0x0000FFFF );
		Block[0] = (i);
		fat_load(Cluster, Block);
		
		//System.out.println("Cluster"+Cluster);
		//System.out.println("Block"+Block[0]);
		//System.out.println("i"+i);
	}

	
	//System.out.println("START2");
	//System.out.println("last_cluster"+last_cluster);
	// Find free Cluster & Mark new cluster as end of chain
	
	fat_load_reset();
	fat_get_free_cluster(Block,tmp_buffer);
	//System.out.println("next_cluster"+Block[0]);
	// Mark new cluster as end of chain
	fat_load_reset();
	fat_set_cluster_value(last_cluster, Block[0], tmp_buffer);

	//System.out.println("end add");
	
	//printf_fat (FOStream_startcluster[0]);
	
	
//	int[] test = new int[512];

//	for(int k=0; k<512; k++)
//	{
//		test[k]='Y';
//	}
	
//	test[511]=0;
	
//	Block[0] = (i/cluster_size);
		
	//Auslesen der FAT - Tabelle
//	fat_load (Cluster,Block);			 

	
//	Block[0] = ((Block[0]-2) * cluster_size) + cluster_offset;
	//Berechnung des Blocks innerhalb des Cluster
//	Block[0] += (i % cluster_size);
	//Write Data Block to Device
//	MmcInterface.mmc_write_sector (Block[0],test);	
	
	

} 



public static void fat_set_cluster_value(int Cluster, int NextCluster, int[] TMP_Buffer)
{
	//Byte Adresse innerhalb des Fat Blocks
	int FAT_Byte_Addresse;	

	//FAT Block Adresse
	int FAT_Block_Addresse;

	FAT_Byte_Addresse = (Cluster*2) % BlockSize;
		
	//Berechnung des Blocks der gelesen werden muß
	FAT_Block_Addresse = ((Cluster*2) / BlockSize) + 
				volume_boot_record_addr + fat_offset;	
	
	//Lesen des FAT Blocks
	//Überprüfung ob dieser Block schon gelesen wurde
	//System.out.println("FAT_Block_Addresse= "+FAT_Block_Addresse);
	//System.out.println("FAT_Block_Store= "+FAT_Block_Store);
		
	//FAT_Block_Store = FAT_Block_Addresse;
	//Lesen des FAT Blocks
	MmcInterface.mmc_read_sector (FAT_Block_Addresse,TMP_Buffer);	
	
	TMP_Buffer[FAT_Byte_Addresse+0] = (0x000000FF & NextCluster);
	TMP_Buffer[FAT_Byte_Addresse+1] =  (0x000000FF & ( NextCluster>>8));
	
	MmcInterface.mmc_write_sector (FAT_Block_Addresse,TMP_Buffer);
	MmcInterface.mmc_write_sector ((FAT_Block_Addresse+fat_size),TMP_Buffer);
	
	
} 


public static int fat_set_dir_ent(int dir_cluster, //Angabe Dir Cluster
					int Entry_Count,   //Angabe welcher Direintrag
					long Size, 		   //setzt die File Größe
					int Dir_Attrib,   //setzt das Dir Attributs
					int[] Buffer) 	   //Working Buffer
{
	int TMP_Entry_Count = 0;
	int[] Block2 = new int[1];
	int Block=0;
	DirEntry dir = new DirEntry(); //Zeiger auf einen Verzeichniseintrag
	int b=0;
	

	if (dir_cluster == 0)
		{
		Block = fat_root_dir_addr(Buffer);
		}
	else
		{
		//Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
		//Berechnung welcher Cluster zu laden ist
		//Auslesen der FAT - Tabelle
		fat_load (dir_cluster,Block2,Buffer);			 
		Block = Block2[0];
		Block = ((Block-2) * cluster_size) + cluster_offset;
		}

	//auslesen des gesamten Directory
	for (int blk = Block;;blk++)
	{
		MmcInterface.mmc_read_sector (blk,Buffer);	//Lesen eines Blocks des Root Directory
		for (int a=0;a<BlockSize; a = a + 32)
		{
		 dir.set(Buffer,a); //Zeiger auf aktuellen Verzeichniseintrag holen
		 
			if (dir.DIR_Name[0] == 0) //Kein weiterer Eintrag wenn erstes Zeichen des Namens 0 ist
			{
			//System.out.println("out a "+a);
			dir=null;
			Block2 =null;
			return (0xFFFF);
			}
			
			//Prüfen ob es ein 8.3 Eintrag ist
			//Das ist der Fall wenn es sich nicht um einen Eintrag für lange Dateinamen
			//oder um einen als gelöscht markierten Eintrag handelt.
   			if ((dir.DIR_Attr != ATTR_LONG_NAME) &&
				(dir.DIR_Name[0] != DIR_ENTRY_IS_FREE)) 
			{
				//System.out.println("a not long "+a);
				
				//Ist es der gewünschte Verzeichniseintrag
				if (TMP_Entry_Count == Entry_Count) 
				{
					
					Buffer[a+11] =Dir_Attrib;
					
					//Speichern der Filegröße
					Buffer[a+28] = ((int)(Size&0x000000FF)  );
					Buffer[a+29] = ((int)(Size&0x0000FF00) >>8);
					Buffer[a+30] = ((int)(Size&0x00FF0000) >>16);						
					Buffer[a+31] = ((int)(Size&0xFF000000) >>24);	
					

					
			//	System.out.println("write dir entr");
					//Eintrag gefunden ändern und speichern
					MmcInterface.mmc_write_sector (blk,Buffer);
					Block2 =null;
					dir=null;
					return(1);
				}
			TMP_Entry_Count++;
			}  
		
		}
	}

//	return (0xFFFF); //Kein Eintrag mehr gefunden Rücksprung mit 0xFFFF

}


//-----------------------------------------------------------------------------
// FAT32_FindFreeOffset: Find a free space in the directory for a new entry 
// which takes up 'entryCount' blocks (or allocate some more)
//-----------------------------------------------------------------------------
public static int  fat_find_free_offset(int dir_cluster, //Angabe Dir Cluster
					int[] Entry_Count,   //Angabe welcher Direintrag
					int[] Buffer)
{
	int TMP_Entry_Count = 0;
	int[] Block2 = new int[1];
	int Block=0;
	DirEntry dir = new DirEntry(); //Zeiger auf einen Verzeichniseintrag
	int b=0;
	

	if (dir_cluster == 0)
		{
		Block = fat_root_dir_addr(Buffer);
		}
	else
		{
		//Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
		//Berechnung welcher Cluster zu laden ist
		//Auslesen der FAT - Tabelle
		fat_load (dir_cluster,Block2,Buffer);			 
		Block = Block2[0];
		Block = ((Block-2) * cluster_size) + cluster_offset;
		}

		
		
//	System.out.println("Block ="+Block);
		
	//auslesen des gesamten Directory
	for (int blk = Block;;blk++)
	{
		MmcInterface.mmc_read_sector (blk,Buffer);	//Lesen eines Blocks des Root Directory
		for (int a=0;a<BlockSize; a = a + 32)
		{
		 dir.set(Buffer,a); //Zeiger auf aktuellen Verzeichniseintrag holen
		 
			if (dir.DIR_Name[0] == 0) //Kein weiterer Eintrag wenn erstes Zeichen des Namens 0 ist
			{
			//System.out.println("out a "+a);
			
			Entry_Count[0] = TMP_Entry_Count;
			
			if ((a+32)<BlockSize)
			{
			Buffer[a+32]=0;
			MmcInterface.mmc_write_sector (blk,Buffer);
			}
			
			else
			{
			//blk++;
			//MmcInterface.mmc_read_sector (blk,Buffer);
			//Buffer[a+32]=0;
			//MmcInterface.mmc_write_sector (blk,Buffer);			
			return (0xFFFF);
			}
			
			
			dir=null;
			Block2 =null;
			return (1);
			}
			
			//Prüfen ob es ein 8.3 Eintrag ist
			//Das ist der Fall wenn es sich nicht um einen Eintrag für lange Dateinamen
			//oder um einen als gelöscht markierten Eintrag handelt.
   			if ((dir.DIR_Attr != ATTR_LONG_NAME) &&
				(dir.DIR_Name[0] == DIR_ENTRY_IS_FREE)) 
			{
				//System.out.println("a not long "+a);
				
				//Ist es der gewünschte Verzeichniseintrag
				Entry_Count[0] = TMP_Entry_Count;
				Block2 =null;
				dir=null;
				return(1);
			}  
		
		TMP_Entry_Count++;
		}
	}

}



//-----------------------------------------------------------------------------
// FAT32_AddFileEntry: Add a directory entry to a location found by FindFreeOffset
//-----------------------------------------------------------------------------
public static int  fat_add_file_ent(int[] dir_cluster, char[] filename, int[] Buffer)
{
	byte item=0;
	int recordoffset = 0;
	byte i=0;
	int x=0;
	int dot=0;
	int[] file = new int[13];
	
	//shortEntry = new DirEntry();

	byte dirtySector = 0;
	int startcluster=0;
	int dirSector = 0;
	int dirOffset = 0;
	byte foundEnd = 0;

	for(x=0; x<10; x++)
	{
		if(filename[x] == '.')
		{
			dot = x;
			break;
		}
	}
	
	
	for(x=0; x<dot; x++)
	{
		file[x] = (int) filename[x];
	}
	
	for(x=dot; x<8; x++)
	{
		file[x] = SPACE;
	}
	
	file[8] = (int) filename[dot+1];
	file[9] = (int) filename[dot+2];
	file[10] = (int) filename[dot+3];
	file[11] = 0;
	

	/*
	System.out.println();
	printf(filename,'s');
	System.out.println();
	for(x=0; x<12; x++)
	{
		printf( (int)file[x],'s');
	}
	System.out.println();
	*/
	x=0;
	
	int checksum;
	int[] Block2 = new int[1];
	int[] Entry_Count = new int[1];
	
	//BLOCK 2[0] = next free cluster = start cluster for new file with size =1
	fat_get_free_cluster ( Block2, Buffer); 
	startcluster = Block2[0];
	
	// Find space in the directory for this filename (or allocate some more)
	if ((fat_find_free_offset( dir_cluster[0], Entry_Count,  Buffer) &0x0000FFFF)== 0x0000FFFF)
		return 0;

		System.out.println("Entry_Count[0]"+Entry_Count[0]);
		
	// Generate checksum of short filename
	checksum = 0;
	for (i=11; i!=0; i--) checksum = (((checksum & 1)==1 )? 0x80 : 0) + (checksum >> 1) + file[11-i];

	// Main cluster following loop
	int TMP_Entry_Count = 0;
	int Block=0;
    Block2[0] =0;
	DirEntry dir = new DirEntry(); //Zeiger auf einen Verzeichniseintrag
	int b=0;
	
	
	System.out.println("dir_cluster[0] ="+dir_cluster[0]);

	if (dir_cluster[0] == 0)
		{
		Block = fat_root_dir_addr(Buffer);
		}
	else
		{
		//Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
		//Berechnung welcher Cluster zu laden ist
		//Auslesen der FAT - Tabelle
		fat_load (dir_cluster[0],Block2,Buffer);			 
		Block = Block2[0];
		Block = ((Block-2) * cluster_size) + cluster_offset;
		}


		
		
	System.out.println("Block ="+Block);
		
	//auslesen des gesamten Directory
	for (int blk = Block;;blk++)
	{
		MmcInterface.mmc_read_sector (blk,Buffer);	//Lesen eines Blocks des Root Directory
		for (int a=0;a<BlockSize; a = a + 32)
		{
   			if (TMP_Entry_Count  == Entry_Count[0] ) 
			{
			
				System.out.println("a ="+a);
			/*
					for (b=0;b<512;b++)
					{
						printf(Buffer[b],'x');
						System.out.println();
					}
					
					*/
					for (b=0;b<11;b++)
					{
						Buffer[b+a] =file[b];
					}

							
					// File Attr = ATTR_ARCHIVE
					Buffer[a+11] = ATTR_ARCHIVE;
					
					Buffer[a+26]= (startcluster&0x000000FF)  ;
					Buffer[a+27]= ((startcluster&0x0000FF00) >>8);
					
					//Speichern der Filegröße = 1
					Buffer[a+28] = ((int)(1&0x000000FF)  );
					Buffer[a+29] = ((int)(1&0x0000FF00) >>8);
					Buffer[a+30] = ((int)(1&0x00FF0000) >>16);						
					Buffer[a+31] = ((int)(1&0xFF000000) >>24);	

					
				System.out.println("write dir entr");
					//Eintrag gefunden ändern und speichern
					MmcInterface.mmc_write_sector (blk,Buffer);

					dir_cluster[0] = startcluster;
				Block2 =null;
				dir=null;
				return(1);
			}  
		
		TMP_Entry_Count++;
		}
	}

	
}










/*
//-----------------------------------------------------------------------------
// FAT32_FreeClusterChain: Follow a chain marking each element as free
//-----------------------------------------------------------------------------

public static bool FAT32_FreeClusterChain(int StartCluster)
{
	int last_cluster;
	int next_cluster = StartCluster;
	
	// Loop until end of chain
	while ( (next_cluster!=0xFFFF) && (next_cluster!=0x00000000) )
	{
		last_cluster = next_cluster;

		// Find next link
		next_cluster = FAT32_FindNextCluster(next_cluster);

		// Clear last link
		FAT32_SetClusterValue(last_cluster, 0x00000000);
	}

	return TRUE;
} 
*/



//####################################################################################
//					 FAT Section End
//####################################################################################





















//####################################################################################
//					 PRINTF Section BEGIN
//####################################################################################

	
public static void printf (int Buffer, char was)
{
	int[] hex = new int[9];
	char[] ding = new char[6]; 
	int i=0;
	
	for (i=0; i<8; i++)
		{
		hex[i]=0;
		}
	

	if (was == 'i' )
		{
		System.out.print(Buffer);
		}
		
	else if (was == 'x' )
		{
		
		ding[0]='A';
		ding[1]='B';
		ding[2]='C';
		ding[3]='D';
		ding[4]='E';
		ding[5]='F';		
		
		for ( i=0; i<8; i++)
			{
			hex[i] = Buffer %16;
			Buffer = Buffer /16;
			}
		
		
		for ( i=7; i>-1; i--)
			{
			if (hex[i] <10)
				{
				System.out.print(hex[i]);
				}
				
			else
				{
				System.out.print(ding[hex[i]-10]);
				}				
				
			}
		
		
		}
		
	else if (was == 'b' )
		{
		System.out.print(Buffer);
		}
		
	else if (was == 's' )
		{
		Buffer = Buffer & 0x000000FF;
		System.out.print( (char) Buffer);
		}

}



public static void printf (int[] Buffer, int an, char was)
{
	
	for (int i=0; i<an; i++)
		{
		printf(Buffer[i], was);
		}

}
	
	
public static void printf (int[] Buffer, char was)
{
	int i=0;
	
	while (Buffer[i] != 0)
		{
		printf(Buffer[i], was);
		i++;
		}

}

public static void printf (char[] Buffer, char was)
{
	int i=0;
	
	while (Buffer[i] != 0)
		{
		printf((int) Buffer[i], was);
		i++;
		}

}
	
	
public static void printf_fat (int cluster)
{
	int i=0;
	int FAT_Block_Addresse ;


			
		//Berechnung des Blocks der gelesen werden muß
		FAT_Block_Addresse = ((cluster>>8)&0x000000FF) + volume_boot_record_addr + fat_offset;	
		//Lesen des FAT Blocks
		//Überprüfung ob dieser Block schon gelesen wurde
System.out.println("FAT_Block_Addresse= "+FAT_Block_Addresse);
		//	FAT_Block_Store = FAT_Block_Addresse;
			//Lesen des FAT Blocks
			MmcInterface.mmc_read_sector (FAT_Block_Addresse,tmp_buffer);	
					
	for (i=0; i<512; i=i+8)
		{
		
			printf(i, 'x');
			printf( (int) ':', 's');
		
		for (int a=0; a<8; a++)
			{
		
				if (a!=0)
					{
					printf( (int) ' ', 's');
					}
				printf(tmp_buffer[i+a], 'x');
				
			}
		
	//	System.out.println();
		}
		
}	
	
	
	

//####################################################################################
//					 PRINTF Section END
//####################################################################################


}