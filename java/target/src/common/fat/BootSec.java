/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Jens Kager, Fritz Praus
  Copyright (C) 2008-2009, Rainhard Raschbauer

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fat;

//Boot Sector Struct

public final class BootSec {

	public static int[]	BS_jmpBoot = new int[3];
	public static int[]	BS_OEMName = new int[8];
	public static int 	BPB_BytesPerSec; //2 bytes
	public static int	BPB_SecPerClus;
	public static int	BPB_RsvdSecCnt; //2 bytes
	public static int	BPB_NumFATs;
	public static int	BPB_RootEntCnt; //2 bytes
	public static int	BPB_TotSec16; //2 bytes
	public static int	BPB_Media;
	public static int	BPB_FATSz16; //2 bytes
	public static int	BPB_SecPerTrk; //2 bytes
	public static int	BPB_NumHeads; //2 bytes
	public static int	BPB_HiddSec; //4 bytes
	public static int	BPB_TotSec32; //4 bytes

	public static void set(int[] Buffer) {
		BS_jmpBoot[0] =Buffer[0];
		BS_jmpBoot[1] =Buffer[1];
		BS_jmpBoot[2] =Buffer[2];

		BS_OEMName[0] =Buffer[3];
		BS_OEMName[1] =Buffer[4];
		BS_OEMName[2] =Buffer[5];
		BS_OEMName[3] =Buffer[6];
		BS_OEMName[4] =Buffer[7];
		BS_OEMName[5] =Buffer[8];
		BS_OEMName[6] =Buffer[9];
		BS_OEMName[7] =Buffer[10];

		BPB_BytesPerSec = ((Buffer[11] & 0x000000FF))
				| ((Buffer[12] & 0x000000FF) << 8);
		BPB_SecPerClus = Buffer[13];
		BPB_RsvdSecCnt = ((Buffer[14] & 0x000000FF))
				| ((Buffer[15] & 0x000000FF) << 8);
		BPB_NumFATs = Buffer[16];
		BPB_RootEntCnt = ((Buffer[17] & 0x000000FF))
				| ((Buffer[18] & 0x000000FF) << 8);
		BPB_TotSec16 = ((Buffer[19] & 0x000000FF))
				| ((Buffer[20] & 0x000000FF) << 8);
		BPB_Media = Buffer[21];
		BPB_FATSz16 = ((Buffer[22] & 0x000000FF))
				| ((Buffer[23] & 0x000000FF) << 8);
		BPB_SecPerTrk = ((Buffer[24] & 0x000000FF))
				| ((Buffer[25] & 0x000000FF) << 8);
		BPB_NumHeads = ((Buffer[26] & 0x000000FF))
				| ((Buffer[27] & 0x000000FF) << 8);
		BPB_HiddSec = ((Buffer[28] & 0x000000FF))
				| ((Buffer[29] & 0x000000FF) << 8)
				| ((Buffer[30] & 0x000000FF) << 16)
				| ((Buffer[31] & 0x000000FF) << 24);
		BPB_TotSec32 = ((Buffer[32] & 0x000000FF))
				| ((Buffer[33] & 0x000000FF) << 8)
				| ((Buffer[34] & 0x000000FF) << 16)
				| ((Buffer[35] & 0x000000FF) << 24);
	}
	
	public static int[] construct(int TotalBytes) {
		int[] Buffer = new int[512];
		
		// Definition for sectors per cluster
		BPB_SecPerClus = 0x04;
		
		// Calculate number of sectors
		int TotalSectors = TotalBytes / FatItS.BlockSize;
		
		// Total number of clusters
		int TotalClusters = TotalSectors / BPB_SecPerClus;
		
		// FAT16 stores 2 bytes per cluster; calculate size of FAT in sectors
		// FIXME: Use Math.ceil() or equivalent function!
		int FATSectors = (int) (TotalClusters * 2 / FatItS.BlockSize) + 1;

		
		// Jump instruction
		Buffer[0]  = 0xEB; 
		Buffer[1]  = 0x3C;
		Buffer[2]  = 0x90;
		
		// OEM Name
		Buffer[3]  = 0x00;
		Buffer[4]  = 0x00;
		Buffer[5]  = 0x00;
		Buffer[6]  = 0x00;
		Buffer[7]  = 0x00;
		Buffer[8]  = 0x00;
		Buffer[9]  = 0x00;
		Buffer[10] = 0x00;
		
		// Bytes per sector (= 512 = 0x200)
		Buffer[11] = 0x00;
		Buffer[12] = 0x02;
		
		// Sectors per cluster (= 4*512 Bytes = 2 KB)
		Buffer[13] = BPB_SecPerClus;
		
		// Reserved sector count (1 sector)
		Buffer[14] = 0x01;
		Buffer[15] = 0x00;
		
		// Number of FATs
		Buffer[16] = 0x02;
		
		// Maximum number of root directory entries
		Buffer[17] = 0x00;
		Buffer[18] = 0x02;

		
		// Initialize all sector count fields to 0x00
		Buffer[19] = 0x00;
		Buffer[20] = 0x00;
		Buffer[32] = 0x00;
		Buffer[33] = 0x00;
		Buffer[34] = 0x00;
		Buffer[35] = 0x00;
		
		// Total sectors (if zero, use 4 byte value at offset 0x20)
		if (TotalSectors > 65535) {
			// Use offset 0x20 for sector count
			Buffer[32] = (TotalSectors)       & 0xFF;
			Buffer[33] = (TotalSectors >>  8) & 0xFF;
			Buffer[34] = (TotalSectors >> 16) & 0xFF;
			Buffer[35] = (TotalSectors >> 24) & 0xFF;
	
		} else {
			// Use offset 0x13 for sector count
			Buffer[19] = (TotalSectors)      & 0xFF;
			Buffer[20] = (TotalSectors >> 8) & 0xFF;
		}
		
		// Media descriptor (0xF8 = Fixed Disk)
		Buffer[21] = 0xF8;
		
		// Sectors per FAT
		Buffer[22] = (FATSectors)      & 0xFF;
		Buffer[23] = (FATSectors >> 8) & 0xFF;
		
		// FIXME: Sectors per Track
		Buffer[24] = 0x3C;
		Buffer[25] = 0x00;
		
		// FIXME: Number of heads
		Buffer[26] = 0x02;
		Buffer[27] = 0x00;
		
		// Hidden Sectors
		Buffer[28] = 0x00;
		Buffer[29] = 0x00;
		Buffer[30] = 0x00;
		Buffer[31] = 0x00;
		
		// Boot Sector Signature
		Buffer[510] = 0x55;
		Buffer[511] = 0xAA;

		
		return Buffer;
	}

}