/*
 * Created on 01.09.2005
 *
 */
package sdcard;

import joprt.RtThread;

import com.jopdesign.sys.*;



public class FatTest {



	// ####################################################################################
	// FileIn/OutputStream def Begin
	// ####################################################################################

	public static FatItS FatInterface = new FatItS();
	public static FatMmc MmcInterface = new FatMmc();
	public static FileOutputStream FOS;
	public static FileInputStream FIS;

	// ####################################################################################
	// FileIn/OutputStream def End
	// ####################################################################################


	public static void read_test() {
	
		byte[] k = new byte[400];
		int y = 0;
		
		
		
		// 1 Byte read
		System.out.print((char) FIS.read());
		System.out.print((char) FIS.read());
		System.out.print((char) FIS.read());
		
		
		// Read till array full or EOF
		y = FIS.read(k);
		int h = 0;

		while (y != -1) {
			for (int g = 0; g < y; g++) {

				System.out.print((char) k[g]);
				// FatInterface.printf(k[g],'s');
			}

			h++;
			
			// Read 300 byte from actual offset
			y = FIS.read(k, 0, 300);
		}

		for (int g = 0; g < y; g++) {

			System.out.print((char) k[g]);
			// FatInterface.printf(k[g],'s');
		}

	}
	
	

	public static void write_test() {
		int fj = 0;
		byte[] tes = new byte[518];
		/*
		 * for ( fj=0; fj < 512; fj++) { write((int) 'A'); }
		 * 
		 * 
		 * write((int) 0x0D); write((int) 0x0A);
		 */

		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) 'F';
		}

		tes[512] = 0x0d;
		tes[513] = 0x0a;
		tes[514] = 0x0;


		// write till tes[x]==0
		FOS.write(tes);



/*		 512* 1Byte write      very very slow
		for (fj = 0; fj < 512; fj++) {
			
			// 512* 1Byte write	
			FOS.write((int) 'A');
		}

		FOS.write((int) 0x0d);
		FOS.write((int) 0x0a);
*/		
		
		
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) 'A';
		}

		// write 514 Bytes with offset 0
		FOS.write(tes,0,512);
	

		// 1 Byte write 
		FOS.write((int) 0x0d);
		FOS.write((int) 0x0a);

	
		
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) 'T';
		}

		tes[512] = 0x0d;
		tes[513] = 0x0a;

		
		// write 514 Bytes with offset 0
		FOS.write(tes,0,514);
		
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) '1';
		}

		tes[512] = 0x0d;
		tes[513] = 0x0a;

		
		// write 514 Bytes with offset 0
		FOS.write(tes,0,514);
		
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) '6';
		}

		tes[512] = 0x0d;
		tes[513] = 0x0a;

		
		// write 514 Bytes with offset 0
		FOS.write(tes,0,514);
		
		FOS.write((int) 0x0d);
		FOS.write((int) 0x0a);
		FOS.write((int) 'E');
		FOS.write((int) 'N');
		FOS.write((int) 'D');
		
	

	}

	
	
	public static void run() {

		int i, j, x = 1;
		int[] Buffer = new int[520];
		int Clustervar;
		int[] Dir_Attrib = new int[1];
		Dir_Attrib[0] = 0;
		long tmp;
		long[] Size = new long[1];
		Size[0] = 0;
		
		// needed for FatInterface.printf_fat(0);
		//FatInterface.fat_cluster_data_store();
	
		
/*	"FAT-TEST.TXT" -> /FAT-TEST.TXT
*	"TEST/FAT-TEST.TXT"  ->  /TEST/FAT-TEST.TXT 
*	
*	Do not start the filename string with "/" !
*/	
		
		String namea = new String("FAT-TEST.TXT");
		System.out.print("Open File ");
		System.out.print(namea);
		System.out.print(" for write = ");
		System.out.println(FOS.FileOutputStream(namea));
		namea = null;
		
		System.out.println();
		System.out.println("Start write_test();");
		write_test();
		System.out.println("Finished write_test();");
		System.out.println();
		System.out.println();
		//FatInterface.fat_load_reset();
		
		
		System.out.println();
		String nameb = new String("FAT-TEST.TXT");
		System.out.print("Open File ");
		System.out.print(nameb);
		System.out.print(" for read = ");
		System.out.println(FIS.FileInputStream(nameb));
		nameb = null;
		
		
		System.out.println();
		System.out.println("Start read_test();");
		System.out.println();
		System.out.println();
		read_test();
		System.out.println();
		System.out.println();
		System.out.println("Finished read_test();");
		System.out.println();
				
				
		System.out.println();
		System.out.println();
		System.out.println("         print Directory-listing of write file dir");
		System.out.println();
		System.out.println();
		
		int[] Buf2 = new int[513];
		
		FatInterface.fat_load_reset();
		for (int a = 0; a < 240; a++) {
			System.out.println("a in main = " + a);
			Clustervar = FatInterface.fat_read_dir_ent(
					FOS.FOStream_Dir_cluster, a, Size, Dir_Attrib, Buf2);
			System.out.println("a in main = " + a);
			if (Clustervar == 0xffff) {
				System.out.println("a in main aus = " + a);
				break;
			}
			tmp = (Size[0] & 0x0000FFFF);
			System.out.println("Size = " + Size[0] + "Cluster = " + Clustervar
					+ " DirA = " + Dir_Attrib[0] + " FileName = ");
				
			FatInterface.printf(Buf2, 's');
		
		}
		System.out.println("Directory Print End");


	
/*	
	System.out.println();
	System.out.println();
	System.out.println("              Print first 512 Bytes of FAT");
	System.out.println();
	System.out.println();
	FatInterface.printf_fat(0);
*/

	}

	
	public static void main(String[] args) {

				
		System.out.println("MmcInterface.mmc_init  start" );
		
		if ( MmcInterface.mmc_init() != 0)
		{
			System.out.println("MmcInterface.mmc_init  failed or no Card connected" );
		}
		
		else
		{
			System.out.println("MmcInterface.mmc_init  done" );
			System.out.println();
			System.out.println();
			run();
		}
	}

}
