/*
 * Created on 01.09.2005
 *
 */
package dsp;

import joprt.RtThread;

import com.jopdesign.sys.*;
//import dsp.*;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */


 

 
public class AC97X {






//####################################################################################
//					 AC97 DEF Section BEGIN
//####################################################################################


	public static final int CSR = 0x0/4;	// Main Configuration/Status Register
	public static final int OCC0 = 0x4/4;	// RO Output Channel Configuration Register 0
	public static final int OCC1 = 0x8/4;	// Output Channel Configuration Register 1
	public static final int ICC = 0xc/4;	// Input Channel Configuration Register
	public static final int CRAC = 0x10/4;	// Codec Register Access Command
	public static final int INTM = 0x14/4;	// Interrupt Mask
	public static final int INTS = 0x18/4;	// Interrupt Status Register
	public static final int RES = 0x1c;
	public static final int OCH0 = 0x20/4;	// Output Channel 0
	public static final int OCH1 = 0x24/4;	// Output Channel 1
	public static final int OCH2 = 0x28/4;	// Output Channel 2
	public static final int OCH3 = 0x2c/4;	// Output Channel 3
	public static final int OCH4 = 0x30/4;	// Output Channel 4
	public static final int OCH5 = 0x34/4;	// Output Channel 5
	public static final int ICH0 = 0x38/4;	// Input Channel 0
	public static final int ICH1 = 0x3c/4;	// Input Channel 1
	public static final int ICH2 = 0x40/4;	// Input Channel 2
	
	public static final int MSK_RD_DONE = 0x01;
	public static final int MSK_WR_DONE = 0x02;
	
	
//####################################################################################
//					 AC97 DEF Section END
//####################################################################################
	

	
	
	public static FatItS FatInterface = new FatItS();
	public static FatMmc MmcInterface = new FatMmc();
	public static FileOutputStream FOS;
	public static FileInputStream FIS;

	
	

//####################################################################################
//					 AC97 Section Begin
//####################################################################################	
	
	public static int rd(int wb_reg) {
		
		return Native.rdMem(Const.WB_AC97+wb_reg);
	}

	public static void wr(int wb_reg, int val) {
		
		Native.wrMem(val, Const.WB_AC97+wb_reg);
	}

	
	public static int codecRd(int reg) {
		
		wr(CRAC, 0x80000000+(reg<<16));
		while((rd(INTS) & MSK_RD_DONE)==0) {
			;	// busy wait till value read
		}
		return rd(CRAC)&0xffff;
	}

	
	public static void codecWr(int reg, int val) {
		
		wr(CRAC, (reg<<16)+val);
		while((rd(INTS) & MSK_WR_DONE)==0) {
			;	// busy wait till write done
		}
	}
	

	
	public static void init_ac97() {

		//
		//	check status register and force a reset
		//
		System.out.println("CSR="+rd(CSR));
		wr(CSR, 1);			// cold reset the AC97
		System.out.println("CSR="+rd(CSR));
		RtThread.sleepMs(10);
		System.out.println("CSR="+rd(CSR));
		wr(CSR, 2);			// resume the AC97
		RtThread.sleepMs(10);
		System.out.println("CSR="+rd(CSR));
		
		//
		//	enable stereo in and out in the AC97 interface
		//
		System.out.println("OCC0="+rd(OCC0));
		wr(OCC0, 0x0101);	// enable front left and right output
//		wr(OCC0, 0x0909);	// enable front left and right output
		System.out.println("OCC0="+rd(OCC0));	
//		wr(ICC, 0x2121);	// enable left and right input
		wr(ICC, 0x02323);	// enable left and right input
		System.out.println("ICC="+rd(ICC));

		//
		//	read some vendor information
		//
		System.out.println("VENDOR ID="+codecRd(0x7c));
		System.out.println("VENDOR version="+codecRd(0x7e));
		System.out.println("Imput sampling rate="+codecRd(0x32));
		//
		//	do the initilization
		//
		System.out.println("MASTER VOLUME="+codecRd(0x02));
		codecWr(0x02, 0x0000); // unmute master volume
		System.out.println("MASTER VOLUME="+codecRd(0x02));
		
//		codecWr(0x10, 0x0808); // enable line input
		codecWr(0x18, 0x0808); // PCM-out volume
		codecWr(0x1a, 0x0404); // select line record
		codecWr(0x1c, 0x000); // record gain
		codecWr(0x2A, 0x0605); 

		System.out.println("EXT Reg="+codecRd(0x28) );
		//		codecWr(0x20, 0x0000); // local loopback
//		codecWr(0x64, 0x0000); // mixer adc, input gain
		codecWr(0x32, 0x5622); // imput samplig Rate
		System.out.println("Imput sampling rate="+codecRd(0x32));
	
	}


//####################################################################################
//					 AC97 Section END
//####################################################################################







public static void test()
{
	int fj=0;
	byte[] tes = new byte[518];
	/*
	for ( fj=0; fj < 512; fj++)
	{
		write((int) 'A');
	}
	
	
	write((int) 0x0D);
	write((int) 0x0A);
	*/
	
	for ( fj=0; fj < 512; fj++)
	{
		tes[fj]= (byte) 'O';
	}
	
	
	tes[512]= 0x0d;
	tes[513]= 0x0a;
	tes[514]= 0x0;
	
	FOS.write(tes);
	
	for ( fj=0; fj < 512; fj++)
	{
		FOS.write((int) 'K');
	}
	
	FOS.write((int) 0x0D);
	FOS.write((int) 0x0A);
	
	for ( fj=0; fj < 512; fj++)
	{
		FOS.write((int) '-');
	}
	
	FOS.write((int) 0x0D);
	FOS.write((int) 0x0A);
	
	for ( fj=0; fj < 512; fj++)
	{
		FOS.write((int) '4');
	}	
	
	FOS.write((int) 0x0D);
	FOS.write((int) 0x0A);
	
	for ( fj=0; fj < 512; fj++)
	{
		FOS.write((int) '7');
	}	
	
	FOS.write((int) 0x0D);
	FOS.write((int) 0x0A);

	byte[] tes1 = new byte[518];
	
	for ( fj=0; fj < 512; fj++)
	{
		tes1[fj]= (byte) 'F';
	}
	
	
	tes1[512]= 0x0D;
	tes1[513]= 0x0A;
	
	FOS.write(tes1,3820,513);
	
	}


	
	public static void run() {
		
		int left, right, i, j, v1, v2, x=1;
		int[] Buffer = new int[520];
		int[] name =   new int[13];
		
		

		
		FatInterface.fat_cluster_data_store();

	int CMD[] = new int[6];

		
		
	//	System.out.println("volume_boot_record_addr="+fat_addr(Buffer));
		
	

		name[0] = 'F';
		name[1] = 'I';
		name[2] = 'C';
		name[3] = 'H';
		name[4] = '2';
		name[5] = '2';
		name[6] = '2';
		name[7] = '2';
		name[8] = 'T';
		name[9] = 'X';
		name[10] = 'T';
		name[11] = 0;
		name[12] = 0;

	
	for (int a = 0;a < 515;a++)
	{
		Buffer[a]=0;
	}
		
		
	int Clustervar;
	int[] Dir_Attrib = new int[1]; 
	Dir_Attrib[0] =0;
	long tmp;
	long[] Size = new long[1];
	Size[0]=0;

	
	String namea = new String("TEST/TUCME.TXT");
	

	System.out.println(FOS.FileOutputStream(  namea));
namea=null;
	System.out.println("FOStream_offset_from_start[0]"+FOS.FOStream_offset_from_start[0]);
	
test();
	
	
	byte[] k= new byte[400];
	int y=0;
	
	namea = null;
	
	System.out.println("danach");
	
	String nameb = new String("TEST/TUCME.TXT");

	FatInterface.fat_load_reset();

	System.out.println("danach");
	System.out.println(FIS.FileInputStream(  nameb));
	nameb = null;
	
	y=0;
	y=FIS.read(k,0,300);
	int h=0;
	
	while ((y==300))
	{
		for(int g=0; g<y; g++)
		{
			
		//	FatInterface.printf(k[g],'s');
		}
	
		h++;
		y=FIS.read(k,0,300);
	}
	
		
	for(int g=0; g<y; g++)
	{
		
	//	FatInterface.printf(k[g],'s');
	}
	
	System.out.println("FIS.FIStream_startcluster[0]"+FIS.FIStream_startcluster[0]);
	System.out.println("FOS.FOStream_startcluster[0]"+FOS.FOStream_startcluster[0]);
	
	// FatInterface.printf_fat (FOStream_startcluster[0]);
	
	
	System.out.println("Directory");
	//for (int a = 1;a < 240;a++)
	
	
	int[] Buf2 = new int[513];
	
	
	FatInterface.fat_load_reset();
	for (int a = 0;a < 240;a++)
	{
		System.out.println("a in main = "+a);
		Clustervar = FatInterface.fat_read_dir_ent(FOS.FOStream_Dir_cluster,a,Size,Dir_Attrib,Buf2);
		System.out.println("a in main = "+a);
		if (Clustervar == 0xffff)
			{
				System.out.println("a in main aus = "+a);
				break;
			}
		tmp = (Size[0] & 0x0000FFFF);
		System.out.println("Size = "+Size[0]+"Cluster = "+Clustervar+" DirA = "+Dir_Attrib[0]+" FileName = ");

		FatInterface.printf(Buf2,'s');
		
	}
	System.out.println("Directory Ende");

FatInterface.printf_fat(0);
	
/*	
	int[] Clustervar2= new int[1];
	
	
	FatInterface.fat_find_free_offset(0, Clustervar2, Buffer);
	System.out.println("FatInterface.fat_find_free_offset ="+Clustervar2[0]);
	
	//Lade Cluster für das index.htm File in den Speicher 
	Clustervar = 0;//suche im Root Verzeichnis
	Clustervar2[0] =Clustervar;
	if (FatInterface.fat_search_file(name,Clustervar2,Size,Dir_Attrib,Buffer) == 1)
		{
		System.out.println("File Found!");
		//usart_write("\nFile Found!!\n\n");
		//Lese File und gibt es auf der seriellen Schnittstelle aus
		
		for (int a = 0;a<512;a=a+4)
			{
				Buffer[a]='O';
				Buffer[a+1]='r';
				Buffer[a+2]='g';
				Buffer[a+3]='l';
				}
		
		FatInterface.fat_write_file ( Clustervar2[0],//Angabe des Startclusters vom File
					Buffer,	  //Workingbuffer
					0)	;
		
		
		int wieoft=0;
		long tempsize;
		int end;
		/*
	//	FatInterface.printf_fat(0);
		FatInterface.printf_fat(Clustervar2[0]);
		
		FatInterface.fat_add_cluster_to_end_of_chain(Clustervar2[0]);
		
	//	FatInterface.printf_fat(0);
		FatInterface.printf_fat(Clustervar2[0]);		
		
		FatInterface.fat_set_dir_ent(0,0, 1536,32, Buffer) ;	    
	
Clustervar2[0] =Clustervar;	
FatInterface.fat_search_file(name,Clustervar2,Size,Dir_Attrib,Buffer);
	System.out.println("Size = "+Size[0]+"Cluster = "+Clustervar2[0]+" DirA = "+Dir_Attrib[0]+" FileName = ");
	
	
				for (int a = 0;a<500;a=a+4)
			{
				Buffer[a]='O';
				Buffer[a+1]='r';
				Buffer[a+2]='g';
				Buffer[a+3]='l';
				}

	
		FatInterface.fat_write_file ( Clustervar2[0],//Angabe des Startclusters vom File
					Buffer,	  //Workingbuffer
					0)	;

		FatInterface.fat_write_file ( Clustervar2[0],//Angabe des Startclusters vom File
					Buffer,	  //Workingbuffer
					1)	;
					
				for (int a = 0;a<500;a=a+4)
			{
				Buffer[a]='F';
				Buffer[a+1]='i';
				Buffer[a+2]='C';
				Buffer[a+3]='h';
				}
					
		FatInterface.fat_write_file ( Clustervar2[0],//Angabe des Startclusters vom File
					Buffer,	  //Workingbuffer
					2)	;	
	

	FatInterface.fat_read_file (Clustervar2[0],Buffer,0);
			
			
				
			for (int a = 0;a<512;a++)
				{
				//FatInterface.printf(Buffer[a],'x');
			//	System.out.println();
				FatInterface.printf(Buffer[a],'s');
				//System.out.println();
				}
			FatInterface.fat_read_file (Clustervar2[0],Buffer,1);
			
			System.out.println();
				
			for (int a = 0;a<512;a++)
				{
				//FatInterface.printf(Buffer[a],'x');
			//	System.out.println();
				FatInterface.printf(Buffer[a],'s');
			//	System.out.println();
				}
	//	System.out.println('a');
//			for (int a = 0;a<500;a=a+4)
//			{
//				Buffer[a]='O';
//				Buffer[a+1]='r';
//				Buffer[a+2]='g';
//				Buffer[a+3]='l';
//				}
//System.out.println('a');
//for(;;)
//{			
//for (int u=1;u<2000;u++) 
//				{
//				FatInterface.fat_write_file ( Clustervar2[0],//Angabe des Startclusters vom File
//						Buffer,	  //Workingbuffer
//						u)	;
//				}
		//}	
		
//			 FatInterface.fat_load_cluster_st = -1;
//			FatInterface.fat_load_cluster_tmp = -1;
//			FatInterface.fat_load_a_tmp=0;
		*/
	//	}
/*	
	else
		{
		System.out.println("File not found");
		}
*/
	 /*
	 Clustervar=Clustervar2[0];
		
		
		
	
		
		
		
		for (i=0; i<10; ++i) {
			// flush input FIFOs
			rd(ICH0);
			rd(ICH1);
			rd(INTS);
		}
		
		i = 0;
		int u=0;
		
		System.out.println("Clustervar="+Clustervar);
		System.out.println("cluster_size="+cluster_size);
		System.out.println("cluster_offset="+cluster_offset);
		System.out.println("volume_boot_record_addr="+volume_boot_record_addr);
		
		int status=0;
		
		for (u=2;u<35;u++) {
		int b;
		/*
		x=0;
		for (x=0;x<512;x=x+64)
			{
			// busy wait for input samples
			for (;;) {
				//System.out.println(1);
				status =Native.rdMem(Const.WB_AC97+INTS);
				if ((status&0x2d00000)!=0) break;    // Interupt 8 Samples anwesend & clr interupt
			}
			
			
			for (int n=0;n<64;n=n+4)
			{
				left = Native.rdMem(Const.WB_AC97+ICH0);
				right = Native.rdMem(Const.WB_AC97+ICH1);
		//		if ((Native.rdMem(Const.WB_AC97+INTS)&0x1200000)!=0) {
		//			System.out.println(2);
		//			continue;
		//		}
			
				Buffer[x+n+0]=left&0x000000FF;
				Buffer[x+n+1]=(left&0x0000FF00)>>8;
				Buffer[x+n+2]=(left>>16)&0x00FF;
				Buffer[x+n+3]=(left>>24)&0x00FF;
			}

			//Native.wrMem(left, Const.WB_AC97+OCH0);
			//Native.wrMem(right, Const.WB_AC97+OCH1);
		}
	*/

	//	FatInterface.fat_write_file ( Clustervar,//Angabe des Startclusters vom File
	//				Buffer,	  //Workingbuffer
	//				u)	;

			//System.out.println("MMC write end");

	//	}
	
	}
	
	
	public static void main(String[] args) {
			
		//for(;;)
			//{
		init_ac97();
		System.out.println("MmcInterface.mmc_init="+MmcInterface.mmc_init());
			//}
		run();
	}

}
