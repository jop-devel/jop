package sdcard;

import joprt.RtThread;

import com.jopdesign.sys.*;

public class FatMmc {



//####################################################################################
//					 MMC Section BEGIN
//####################################################################################
	
public static int mmc_read_byte ()
{
	int i = 0;

	Native.wrMem(0xff, Const.WB_SPI+0x02);
	while((Native.rdMem(Const.WB_SPI+1)&0x80) == 0){};
	i=Native.rdMem(Const.WB_SPI+0x02);
	Native.wrMem(0x80, Const.WB_SPI+0x1);
	
	return (i);
}
	
	
public static void mmc_read_nybble (int[] k,int n)
{
	
//	System.out.println("OO");
	
	Native.wrMem(0xff, Const.WB_SPI+0x02);
	Native.wrMem(0xff, Const.WB_SPI+0x02);
	Native.wrMem(0xff, Const.WB_SPI+0x02);
	Native.wrMem(0xff, Const.WB_SPI+0x02);
	Native.wrMem(0xff, Const.WB_SPI+0x02);
	Native.wrMem(0xff, Const.WB_SPI+0x02);
	Native.wrMem(0xff, Const.WB_SPI+0x02);
	Native.wrMem(0xff, Const.WB_SPI+0x02);
//System.out.println("SS");
	Native.wrMem(0xe0, Const.WB_SPI+3);


//System.out.println("XX");
	while((Native.rdMem(Const.WB_SPI+1)&0x80) == 0){};
	k[n+0]=Native.rdMem(Const.WB_SPI+0x02);
	k[n+1]=Native.rdMem(Const.WB_SPI+0x02);
	k[n+2]=Native.rdMem(Const.WB_SPI+0x02);
	k[n+3]=Native.rdMem(Const.WB_SPI+0x02);
	k[n+4]=Native.rdMem(Const.WB_SPI+0x02);
	k[n+5]=Native.rdMem(Const.WB_SPI+0x02);
	k[n+6]=Native.rdMem(Const.WB_SPI+0x02);
	k[n+7]=Native.rdMem(Const.WB_SPI+0x02);
	Native.wrMem(0xe0, Const.WB_SPI+3);
		Native.wrMem(0x80, Const.WB_SPI+0x1);
//	System.out.println("CC");
	
}	
	

public static void mmc_write_byte (int k)
{
	Native.wrMem(k, Const.WB_SPI+0x02);
	while((Native.rdMem(Const.WB_SPI+1)&0x80) == 0){};
	Native.rdMem(Const.WB_SPI+0x02);
	Native.wrMem(0x80, Const.WB_SPI+0x1);
	
}	
	

public static void mmc_write_nybble (int[] k,int n)
{
	Native.wrMem(k[n+0], Const.WB_SPI+0x02);
	Native.wrMem(k[n+1], Const.WB_SPI+0x02);
	Native.wrMem(k[n+2], Const.WB_SPI+0x02);
	Native.wrMem(k[n+3], Const.WB_SPI+0x02);
	Native.wrMem(k[n+4], Const.WB_SPI+0x02);
	Native.wrMem(k[n+5], Const.WB_SPI+0x02);
	Native.wrMem(k[n+6], Const.WB_SPI+0x02);
	Native.wrMem(k[n+7], Const.WB_SPI+0x02);
Native.wrMem(0xe0, Const.WB_SPI+3);
	while((Native.rdMem(Const.WB_SPI+1)&0x80) == 0){};
	//Native.rdMem(Const.WB_SPI+0x02);
	//Native.rdMem(Const.WB_SPI+0x02);
	//Native.rdMem(Const.WB_SPI+0x02);
	//Native.rdMem(Const.WB_SPI+0x02);
	Native.wrMem(0xe0, Const.WB_SPI+3);
	Native.wrMem(0x80, Const.WB_SPI+0x1);	
	
	
}	
	

public static void mmc_write_all (int[] k)
{
	
	for (int n=0; n<512; n++)
		{
		Native.wrMem(k[n], Const.WB_SPI+0x02);
		}
	Native.wrMem(0x04, Const.WB_SPI+3);
	while((Native.rdMem(Const.WB_SPI+1)&0x80) == 0){};
	//Native.rdMem(Const.WB_SPI+0x02);
	Native.wrMem(0x80, Const.WB_SPI+0x1);
	
	Native.wrMem(0x00, Const.WB_SPI+3);
	Native.wrMem(0x80, Const.WB_SPI+0x1);	
	
}	


public static void mmc_read_all (int[] k)
{
	
	for (int n=0; n<512; n++)
		{
		Native.wrMem(0xff, Const.WB_SPI+0x02);
		}
	
	while((Native.rdMem(Const.WB_SPI+1)&0x80) == 0){};
	//Native.rdMem(Const.WB_SPI+0x02);

	for (int n=0; n<512; n++)
		{
		k[n]=Native.rdMem(Const.WB_SPI+0x02);
		}

	Native.wrMem(0x80, Const.WB_SPI+0x1);
	
}	


public static void mmc_read_all (char[] k)
{
	
	for (char n=0; n<512; n++)
		{
		Native.wrMem(0xff, Const.WB_SPI+0x02);
		}
	
	while((Native.rdMem(Const.WB_SPI+1)&0x80) == 0){};
	//Native.rdMem(Const.WB_SPI+0x02);

	for (char n=0; n<512; n++)
		{
		k[n]=(char) Native.rdMem(Const.WB_SPI+0x02);
		}

	Native.wrMem(0x80, Const.WB_SPI+0x1);
	
}	


public static int  mmc_write_command (int[] cmd)
{
	int tmp = 0xff;
	int Timeout = 0;
	int a;
	
	//set MMC_Chip_Select to high (MMC/SD-Karte Inaktiv) 
	Native.wrMem(0x10, Const.WB_SPI+3);
	
	//sendet 8 Clock Impulse
	mmc_write_byte(0xFF);

	//set MMC_Chip_Select to low (MMC/SD-Karte Aktiv)
	Native.wrMem(0x00, Const.WB_SPI+3);

	//sendet 6 Byte Commando
	for (a = 0;a<0x06;a++) //sendet 6 Byte Commando zur MMC/SD-Karte
		{
		mmc_write_byte(cmd[a]);
		}

	//Native.wrMem(0xa0, Const.WB_SPI+3);
	//Native.wrMem(cmd[0], Const.WB_SPI+0x02);
	//Native.wrMem(cmd[1], Const.WB_SPI+0x02);
	//Native.wrMem(cmd[2], Const.WB_SPI+0x02);
	//Native.wrMem(cmd[3], Const.WB_SPI+0x02);
	//Native.wrMem(cmd[4], Const.WB_SPI+0x02);
//	Native.wrMem(cmd[5], Const.WB_SPI+0x02);
	//Native.wrMem(0xA0, Const.WB_SPI+3);
//	while((Native.rdMem(Const.WB_SPI+1)&0x80) == 0){};

//	Native.wrMem(0x00, Const.WB_SPI+3);
//	Native.wrMem(0x80, Const.WB_SPI+0x1);	


	//Wartet auf ein gültige Antwort von der MMC/SD-Karte
	while (tmp == 0xff)	
		{
		tmp = mmc_read_byte();
		if (Timeout++ > 500)
			{
			break; //Abbruch da die MMC/SD-Karte nicht Antwortet
			}
		}
	return(tmp);
}	
	
	
	
public static int mmc_init ()
{
	int a,b;
	int Timeout=0;
	int CMD[] = new int[6];
	
	Native.wrMem(0x10, Const.WB_SPI+3);			//Setzt den Pin MMC_Chip_Select auf High Pegel

	RtThread.sleepMs(1);

	Native.wrMem(0x41, Const.WB_SPI);
	
	//Initialisiere MMC/SD-Karte in den SPI-Mode
	for (b = 0;b<0x0f;b++) //Sendet min 74+ Clocks an die MMC/SD-Karte
		{
		mmc_write_byte(0xff);
		}
	
	//Sendet Commando CMD0 an MMC/SD-Karte
	CMD[0]=0x40;
	CMD[1]=0x00;
	CMD[2]=0x00;
	CMD[3]=0x00;
	CMD[4]=0x00;
	CMD[5]=0x95;
	
	while(mmc_write_command (CMD) !=1)
	{
		if (Timeout++ > 2000)
			{
			Native.wrMem(0x00, Const.WB_SPI+3);
			return(1); //Abbruch bei Commando1 (Return Code1)
			}
	}
	//Sendet Commando CMD1 an MMC/SD-Karte
	Timeout = 0;
	CMD[0] = 0x41;//Commando 1
	CMD[5] = 0xFF;
	while( mmc_write_command (CMD) !=0)
	{
		if (Timeout++ > 40000)
			{
			Native.wrMem(0x00, Const.WB_SPI+3);
			return(2); //Abbruch bei Commando2 (Return Code2)
			}
	}
	
	//set MMC_Chip_Select to high (MMC/SD-Karte Inaktiv)
	Native.wrMem(0x00, Const.WB_SPI+3);
	return(0);
}
	
	
	
public static int mmc_write_sector (int addr,int[] Buffer)
{
	int CMD[] = new int[6];
	int tmp,a;


		
	//Commando 24 zum schreiben eines Blocks auf die MMC/SD - Karte
	CMD[0]=0x58;
	CMD[4]=0x00;
	CMD[5]=0xFF;

	/*Die Adressierung der MMC/SD-Karte wird in Bytes angegeben,
	  addr wird von Blocks zu Bytes umgerechnet danach werden 
	  diese in das Commando eingefügt*/
	  
	addr = addr << 9; //addr = addr * 512
	
	CMD[1] = ((addr & 0xFF000000) >>24 );
	CMD[2] = ((addr & 0x00FF0000) >>16 );
	CMD[3] = ((addr & 0x0000FF00) >>8 );

	//Sendet Commando cmd24 an MMC/SD-Karte (Write 1 Block/512 Bytes)
	tmp = mmc_write_command (CMD);
	if (tmp != 0)
		{
		return(tmp);
		}
			
	//Wartet einen Moment und sendet einen Clock an die MMC/SD-Karte
	for (a=0;a<2;a++)
		{
		mmc_read_byte();
		}
	
	//Sendet Start Byte an MMC/SD-Karte
	mmc_write_byte(0xFE);	
	
	
	Native.wrMem(0x04, Const.WB_SPI+3);

	//Schreiben des Bolcks (512Bytes) auf MMC/SD-Karte
	mmc_write_all (Buffer);
	
	
		Native.wrMem(0x00, Const.WB_SPI+3);
	
		//CRC-Byte schreiben
		mmc_write_byte(0xFF); //Schreibt Dummy CRC
		mmc_write_byte(0xFF); //CRC Code wird nicht benutzt
	
		//Fehler beim schreiben? (Data Response XXX00101 = OK)
		if((mmc_read_byte()&0x1F) != 0x05) return(1);

	
		//Wartet auf MMC/SD-Karte Bussy
		Native.wrMem(0xe0, Const.WB_SPI+3);
		for(;;)
		{
		mmc_read_nybble(Buffer,0);
	

		if (Buffer[0] == 0xff)
			break;
		if (Buffer[1] == 0xff)
			break;
		if (Buffer[2] == 0xff)
			break;
		if (Buffer[3] == 0xff)  
			break;
		if (Buffer[4] == 0xff)
			break;
		if (Buffer[5] == 0xff)
			break;
		if (Buffer[6] == 0xff)
			break;
		if (Buffer[7] == 0xff)
			break;		
		}
	
	//set MMC_Chip_Select to high (MMC/SD-Karte Inaktiv)
	//Native.wrMem(0x10, Const.WB_SPI+3);	
	CMD=null;
return(0);
}


	
	
public static void MMC_Disable()
{
Native.wrMem(0x10, Const.WB_SPI+3);	
}
	
	
	
public static void  mmc_read_block(int[] CMD,int[] Buffer,int Bytes)
{	
	
	int tmp,a;
//	int[] CMD1 = new int [4];
	
/*	
	CMD1[1] = ((( 558 << 9) & 0xFF000000) >>24 );
	CMD1[2] = ((( 558 << 9) & 0x00FF0000) >>16 );
	CMD1[3] = ((( 558 << 9) & 0x0000FF00) >>8 );

	if ((CMD1[1]==CMD[1])&&(CMD1[2]==CMD[2])&&(CMD1[3]==CMD[3]))
	{
		System.out.println("in read block1");
	} 
*/	
	//Sendet Commando cmd an MMC/SD-Karte
	if (mmc_write_command (CMD) != 0)
			{
			 return;
			}

	//Wartet auf Start Byte von der MMC/SD-Karte (FEh/Start Byte)
/*
	if ((CMD1[1]==CMD[1])&&(CMD1[2]==CMD[2])&&(CMD1[3]==CMD[3]))
	{
		System.out.println("in read block2");
	} 
*/	
	while (mmc_read_byte() != 0xfe){};

	/*
	if ((CMD1[1]==CMD[1])&&(CMD1[2]==CMD[2])&&(CMD1[3]==CMD[3]))
	{
		System.out.println("in read block3");
	} 
*/
	
	//Native.wrMem(0x80, Const.WB_SPI+0x1);
	Native.wrMem(0xe0, Const.WB_SPI+3);
/*	
	if ((CMD1[1]==CMD[1])&&(CMD1[2]==CMD[2])&&(CMD1[3]==CMD[3]))
	{
		System.out.println("in read block4");
	} 
*/
	
	for (a=0;a<Bytes;a=a+8)
		{
		//Native.wrMem(0xC0, Const.WB_SPI+3);
		mmc_read_nybble(Buffer,a);
		}
	
	
	/*if ((CMD1[1]==CMD[1])&&(CMD1[2]==CMD[2])&&(CMD1[3]==CMD[3]))
	{
		System.out.println("in read block5");
	} 
*/
	
	Native.wrMem(0x00, Const.WB_SPI+3);

	//CRC-Byte auslesen
	mmc_read_byte();//CRC - Byte wird nicht ausgewertet
	mmc_read_byte();//CRC - Byte wird nicht ausgewertet
	
	//set MMC_Chip_Select to high (MMC/SD-Karte Inaktiv)
	MMC_Disable();
	
	return;
}	
	
	
	
public static int mmc_read_sector (int addr,int[] Buffer)
{	
	
	int CMD[] = new int[6];
	int tmp,a;
	/*
	if (addr==558)
	{
		System.out.println("in read sector1");
	}
	*/
	//Commando 16 zum lesen eines Blocks von der MMC/SD - Karte

	CMD[0]=0x51;
	CMD[1]=0x00;
	CMD[2]=0x00;
	CMD[3]=0x00;
	CMD[4]=0x00;
	CMD[5]=0xFF;
	
	
	/*Die Adressierung der MMC/SD-Karte wird in Bytes angegeben,
	  addr wird von Blocks zu Bytes umgerechnet danach werden 
	  diese in das Commando eingefügt*/
	  /*
	if (addr==558)
	{
		System.out.println("in read sector2");
	}
	  */
	addr = addr << 9; //addr = addr * 512

	CMD[1] = ((addr & 0xFF000000) >>24 );
	CMD[2] = ((addr & 0x00FF0000) >>16 );
	CMD[3] = ((addr & 0x0000FF00) >>8 );
/*
	if (addr==(558<<9))
	{
		System.out.println("in read sector3");
	}
	*/
    mmc_read_block(CMD,Buffer,512);
/*
	if (addr==(558<<9))
	{
		System.out.println("in read sector4");
	}
	*/
	return(0);
}
	


public static int  mmc_read_cid (int[] Buffer)
{
	int CMD[] = new int[6];

	//Commando zum lesen des CID Registers
	CMD[0]=0x4A;
	CMD[1]=0x00;
	CMD[2]=0x00;
	CMD[3]=0x00;
	CMD[4]=0x00;
	CMD[5]=0xFF;
	
	mmc_read_block(CMD,Buffer,16);

	return(0);
}



public static int  mmc_read_csd (int[] Buffer)
{	
	int CMD[] = new int[6];

	//Commando zum lesen des CSD Registers
	CMD[0]=0x49;
	CMD[1]=0x00;
	CMD[2]=0x00;
	CMD[3]=0x00;
	CMD[4]=0x00;
	CMD[5]=0xFF;

		
	mmc_read_block(CMD,Buffer,16);

	return(0);
}


//####################################################################################
//					 MMC Section END
//####################################################################################


}







