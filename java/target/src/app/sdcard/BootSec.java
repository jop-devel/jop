/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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

package sdcard;

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

}