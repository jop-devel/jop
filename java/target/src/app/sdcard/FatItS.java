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

import java.io.FileNotFoundException;
import java.io.IOException;

public class FatItS {

	// Block Size in Bytes
	public static final int BlockSize = 512;

	// maximum number of entries in a directory (arbitrary)
	public static final int MAX_ENTRIES = 1024;

	// Master Boot Record
	public static final int MASTER_BOOT_RECORD = 0;

	// Volume Boot Record location in Master Boot Record
	public static final int VBR_ADDR = 0x1C6;

	// define ASCII
	public static final int SPACE = 0x20;
	public static final int DIR_ENTRY_IS_FREE = 0xE5;
	public static final int FIRST_LONG_ENTRY = 0x01;
	public static final int SECOND_LONG_ENTRY = 0x42;

	// define DIR_Attr
	public static final int ATTR_LONG_NAME = 0x0F;
	public static final int ATTR_READ_ONLY = 0x01;
	public static final int ATTR_HIDDEN = 0x02;
	public static final int ATTR_SYSTEM = 0x04;
	public static final int ATTR_VOLUME_ID = 0x08;
	public static final int ATTR_DIRECTORY = 0x10;
	public static final int ATTR_ARCHIVE = 0x20;

	// FAT12 and FAT16 Structure Starting at Offset 36
	public static final int BS_DRVNUM = 36;
	public static final int BS_RESERVED1 = 37;
	public static final int BS_BOOTSIG = 38;
	public static final int BS_VOLID = 39;
	public static final int BS_VOLLAB = 43;
	public static final int BS_FILSYSTYPE = 54;

	// FAT32 Structure Starting at Offset 36
	public static final int BPB_FATSZ32 = 36;
	public static final int BPB_EXTFLAGS = 40;
	public static final int BPB_FSVER = 42;
	public static final int BPB_ROOTCLUS = 44;
	public static final int BPB_FSINFO = 48;
	public static final int BPB_BKBOOTSEC = 50;
	public static final int BPB_RESERVED = 52;
	public static final int FAT32_BS_DRVNUM = 64;
	public static final int FAT32_BS_RESERVED1 = 65;
	public static final int FAT32_BS_BOOTSIG = 66;
	public static final int FAT32_BS_VOLID = 67;
	public static final int FAT32_BS_VOLLAB = 71;
	public static final int FAT32_BS_FILSYSTYPE = 82;

	// End of Boot Sctor and BPB Structure

	private static int volume_boot_record_addr;
	private static int fat_offset;
	private static int fat_size;
	private static int cluster_offset;
	private static int cluster_size;

	private static int[] tmp_buffer = new int[BlockSize];

	// preallocate exceptions
	private static IOException IOExc = new IOException();
	private static FileNotFoundException FNFExc = new FileNotFoundException();

	private static int fat_addr() {
		// auslesen des Master Boot Record von der MMC/SD Karte (addr = 0)
		FatMmc.mmc_read_sector(MASTER_BOOT_RECORD, tmp_buffer); // Read Master Boot Record
		int volume_boot_record_addr = tmp_buffer[VBR_ADDR] + (tmp_buffer[VBR_ADDR + 1] << 8);
		return volume_boot_record_addr;
	}

	private static int fat_root_dir_addr() {
		// auslesen des Volume Boot Record von der MMC/SD Karte
		FatMmc.mmc_read_sector(volume_boot_record_addr, tmp_buffer);
		BootSec.set(tmp_buffer);

		// berechnet den ersten Sector des Root Directory
		int firstRootDirSecNum = BootSec.BPB_RsvdSecCnt + (BootSec.BPB_NumFATs * BootSec.BPB_FATSz16);
		firstRootDirSecNum += volume_boot_record_addr;

		fat_size = BootSec.BPB_FATSz16;

		return firstRootDirSecNum;
	}

	private static int fat_load(int cluster, int block) {
		// Zum berprfen ob der FAT Block schon geladen wurde
		int blockStore = 0;
		// Byte Adresse innerhalb des Fat Blocks
		int byteAddress;
		// FAT Block Adresse
		int blockAddress;
		
		// Berechnung fr den ersten FAT Block (FAT Start Addresse)
		for (int a = 0;; a++) {
			
			if (a == block) {
				block = (0x0000FFFF & cluster);
				return block;
			}
			
			if (cluster == 0xFFFF) {
				block = 0xFFFF;
				break; // Ist das Ende des Files erreicht Schleife beenden
			}
			
			// Berechnung des Bytes innerhalb des FAT Blocks
			byteAddress = (cluster * 2) % BlockSize;
			// Berechnung des Blocks der gelesen werden mu
			blockAddress = ((cluster * 2) / BlockSize) + volume_boot_record_addr + fat_offset;
			// Lesen des FAT Blocks
			// berprfung ob dieser Block schon gelesen wurde
			if (blockAddress != blockStore) {
				blockStore = blockAddress;
				// Lesen des FAT Blocks
				FatMmc.mmc_read_sector(blockAddress, tmp_buffer);
			}

			// Lesen der nchsten Clusternummer
			cluster = (tmp_buffer[byteAddress + 1] << 8) + tmp_buffer[byteAddress];
		}

		return block;
	}

	public static void fat_init() throws IOException {

		// Get SD-Card and SPI up and running
		FatMmc.mmc_init();

		volume_boot_record_addr = fat_addr();

		FatMmc.mmc_read_sector(volume_boot_record_addr, tmp_buffer);
		BootSec.set(tmp_buffer);

		fat_offset = BootSec.BPB_RsvdSecCnt;

		cluster_size = BootSec.BPB_SecPerClus;
		if (cluster_size == 0) { // something went terribly wrong
			throw IOExc;
		}
		
		cluster_offset = ((BootSec.BPB_BytesPerSec * 32) / BlockSize);
		cluster_offset += fat_root_dir_addr();
	}

	public static void fat_read_file(int cluster,// Angabe des Startclusters vom File
			int[] buffer, // Workingbuffer
			int blockCount) // Angabe welcher Bock vom File geladen/ werden soll a 512 Bytes
	{
		// Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
		// Berechnung welcher Cluster zu laden ist
		int block = (blockCount / cluster_size);

		// Auslesen der FAT - Tabelle
		block = fat_load(cluster, block);
		block = ((block - 2) * cluster_size) + cluster_offset;
		// Berechnung des Blocks innerhalb des Cluster
		block += (blockCount % cluster_size);
		// Read Data Block from Device
		FatMmc.mmc_read_sector(block, buffer);
		return;
	}

	public static void fat_write_file(int cluster,// Angabe des Startclusters vom File
			int[] buffer, // Workingbuffer
			int blockCount) // Angabe welcher Bock vom File gespeichert werden soll a 512 Bytes
	{
		// Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
		// Berechnung welcher Cluster zu speichern ist
		int block = (blockCount / cluster_size);

		// Auslesen der FAT - Tabelle
		block = fat_load(cluster, block);

		block = ((block - 2) * cluster_size) + cluster_offset;
		// Berechnung des Blocks innerhalb des Cluster
		block += (blockCount % cluster_size);
		// Write Data Block to Device
		FatMmc.mmc_write_sector(block, buffer);

		return;
	}

	public static DirEntry fat_search_file(int dirCluster, char[] filename, DirEntry entry) throws IOException
	{
		for (int a = 0; a < MAX_ENTRIES; a++) {
			entry = fat_read_dir_ent(dirCluster, a, entry);
			if (entry.matchName(filename)) {
				return entry;
			}
		}
		throw IOExc; // some strange error occurred
	}

	public static DirEntry fat_mod_file(int dirCluster, char[] filename, long size, int attr, DirEntry entry) throws IOException
	{
		for (int a = 0; a < MAX_ENTRIES; a++) {
			entry = FatItS.fat_read_dir_ent(dirCluster, a, entry);
			if (entry.matchName(filename)) {
				entry = FatItS.fat_set_dir_ent(dirCluster, a, size, attr, entry);
				return entry;
			}
		}
		throw IOExc;
	}

	// Filname fehler das nur volle 8.3 char gehn
	private static DirEntry fat_read_dir_ent(int cluster, int index, DirEntry entry) throws IOException
	{
		int currentIndex = 0;
		int block = 0;

		if (cluster == 0) {
			block = fat_root_dir_addr();
		} else {
			// Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
			// Berechnung welcher Cluster zu laden ist
			// Auslesen der FAT - Tabelle
			block = fat_load(cluster, block);
			block = ((block - 2) * cluster_size) + cluster_offset;
		}

		// auslesen des gesamten Root Directory
		for (int blk = block;; blk++) {
			FatMmc.mmc_read_sector(blk, tmp_buffer); // Lesen eines Blocks des
													// Root Directory
			for (int a = 0; a < BlockSize; a = a + 32) {

				entry.initialize(tmp_buffer, a); // Zeiger auf aktuellen Verzeichniseintrag holen

				// Kein weiterer Eintrag wenn erstes Zeichen des Namens 0 ist
				if (entry.DIR_Name[0] == 0)
				{
					throw FNFExc;
				}

				// Prfen ob es ein 8.3 Eintrag ist
				// Das ist der Fall wenn es sich nicht um einen Eintrag fr lange
				// Dateinamen oder um einen als gelscht markierten Eintrag handelt.
				if ((entry.DIR_Attr != ATTR_LONG_NAME)	&& (entry.DIR_Name[0] != DIR_ENTRY_IS_FREE)) {
					// Ist es der gewnschte Verzeichniseintrag
					if (currentIndex == index) {
						return entry;
					}

					currentIndex++;
				}
			}
		}
	}

	private static int fat_get_free_cluster(int block)
	{
		// FAT Block Adresse
		int blockAddress;
		// Berechnung fr den ersten FAT Block (FAT Start Addresse)

		blockAddress = volume_boot_record_addr + fat_offset;

		for (int a = 0;; a++) {
			if (!(blockAddress < fat_size)) {
				block = 0xFFFFFFFF;
				return block;
			}

			// Lesen des FAT Blocks
			FatMmc.mmc_read_sector(blockAddress, tmp_buffer);

			for (int b = 0; b < BlockSize; b = b + 2) {
				if (((tmp_buffer[b] & 0x000000FF) == 0x00)
						&& ((tmp_buffer[b + 1] & 0x000000FF) == 0x00)) {

					block = ((((blockAddress - volume_boot_record_addr - fat_offset) & 0x000000FF) << 8) | ((b / 2) & 0x000000FF));

					tmp_buffer[b + 0] = 0xFF;
					tmp_buffer[b + 1] = 0xFF;

					FatMmc.mmc_write_sector(blockAddress, tmp_buffer);
					FatMmc.mmc_write_sector((blockAddress + fat_size),	tmp_buffer);
					return block;
				}
			}

			blockAddress++;
		}
	}

	public static void fat_grow_file(int cluster) {
		int last_cluster = 0xFFFFFFFF;
		int i = 0;

		int block = i;
		block = fat_load(cluster, block);
		last_cluster = (block & 0x0000FFFF);

		// Loop until end of chain
		while ((block & 0x0000FFFF) < 0x0000FFF8) {
			i++;
			last_cluster = (block & 0x0000FFFF);
			block = i;
			block = fat_load(cluster, block);
		}

		// Find free Cluster & Mark new cluster as end of chain
		block = fat_get_free_cluster(block);
		// Mark new cluster as end of chain
		fat_set_cluster_value(last_cluster, block);
	}

	private static void fat_set_cluster_value(int cluster, int nextCluster) {
		// Byte Adresse innerhalb des Fat Blocks
		int byteAddress;

		// FAT Block Adresse
		int blockAddress;

		byteAddress = (cluster * 2) % BlockSize;

		// Berechnung des Blocks der gelesen werden mu
		blockAddress = ((cluster * 2) / BlockSize) + volume_boot_record_addr + fat_offset;

		// FAT_Block_Store = FAT_Block_Addresse;
		// Lesen des FAT Blocks
		FatMmc.mmc_read_sector(blockAddress, tmp_buffer);

		tmp_buffer[byteAddress + 0] = (0x000000FF & nextCluster);
		tmp_buffer[byteAddress + 1] = (0x000000FF & (nextCluster >> 8));

		FatMmc.mmc_write_sector(blockAddress, tmp_buffer);
		FatMmc.mmc_write_sector((blockAddress + fat_size), tmp_buffer);
	}

	private static DirEntry fat_set_dir_ent(int dir_cluster, int index, long size, int dirAttrib, DirEntry entry) throws IOException
	{
		int currentIndex = 0;
		int block = 0;

		if (dir_cluster == 0) {
			block = fat_root_dir_addr();
		} else {
			// Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
			// Berechnung welcher Cluster zu laden ist
			// Auslesen der FAT - Tabelle
			block = fat_load(dir_cluster, block);
			block = ((block - 2) * cluster_size) + cluster_offset;
		}

		// auslesen des gesamten Directory
		for (int blk = block;; blk++) {
			FatMmc.mmc_read_sector(blk, tmp_buffer); // Lesen eines Blocks des
													// Root Directory
			for (int a = 0; a < BlockSize; a = a + 32) {
				entry.initialize(tmp_buffer, a); // Zeiger auf aktuellen Verzeichniseintrag holen

				if (entry.DIR_Name[0] == 0) // Kein weiterer Eintrag wenn erstes
											// Zeichen des Namens 0 ist
				{
					throw IOExc;
				}

				// Prfen ob es ein 8.3 Eintrag ist
				// Das ist der Fall wenn es sich nicht um einen Eintrag fr lange
				// Dateinamen
				// oder um einen als gelscht markierten Eintrag handelt.
				if ((entry.DIR_Attr != ATTR_LONG_NAME) && (entry.DIR_Name[0] != DIR_ENTRY_IS_FREE)) {
					// System.out.println("a not long "+a);

					// Ist es der gewnschte Verzeichniseintrag
					if (currentIndex == index) {

						entry.DIR_Attr = dirAttrib;
						entry.DIR_FileSize = (int)size;

						entry.dump(tmp_buffer, a);

						// Eintrag gefunden ndern und speichern
						FatMmc.mmc_write_sector(blk, tmp_buffer);

						return entry;
					}
					currentIndex++;
				}

			}
		}
	}

	// -----------------------------------------------------------------------------
	// FAT32_FindFreeOffset: Find a free space in the directory for a new entry
	// which takes up 'entryCount' blocks (or allocate some more)
	// -----------------------------------------------------------------------------
	private static int fat_find_free_offset(int dir_cluster, DirEntry entry) {
		int currentIndex = 0;
		int block = 0;

		if (dir_cluster == 0) {
			block = fat_root_dir_addr();
		} else {
			// Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
			// Berechnung welcher Cluster zu laden ist
			// Auslesen der FAT - Tabelle
			block = fat_load(dir_cluster, block);
			block = ((block - 2) * cluster_size) + cluster_offset;
		}

		// auslesen des gesamten Directory
		for (int blk = block;; blk++) {
			FatMmc.mmc_read_sector(blk, tmp_buffer); // Lesen eines Blocks des Root Directory
			for (int a = 0; a < BlockSize; a = a + 32) {
				entry.initialize(tmp_buffer, a); // Zeiger auf aktuellen Verzeichniseintrag holen

				if (entry.DIR_Name[0] == 0) // Kein weiterer Eintrag wenn erstes
											// Zeichen des Namens 0 ist
				{
					if ((a + 32) < BlockSize) {
						tmp_buffer[a + 32] = 0;
						FatMmc.mmc_write_sector(blk, tmp_buffer);
						return currentIndex;
					} else {
						return -1;
					}
				}

				// Prfen ob es ein 8.3 Eintrag ist
				// Das ist der Fall wenn es sich nicht um einen Eintrag fr lange
				// Dateinamen
				// oder um einen als gelscht markierten Eintrag handelt.
				if ((entry.DIR_Attr != ATTR_LONG_NAME) && (entry.DIR_Name[0] == DIR_ENTRY_IS_FREE)) {
					// Ist es der gewnschte Verzeichniseintrag
					return currentIndex;
				}

				currentIndex++;
			}
		}

	}

	// -----------------------------------------------------------------------------
	// FAT32_AddFileEntry: Add a directory entry to a location found by
	// FindFreeOffset
	// -----------------------------------------------------------------------------
	public static DirEntry fat_add_file(int dirCluster, char[] filename, DirEntry entry) throws IOException {

		int block = 0;
		int startcluster = 0;
		int index = 0;

		// BLOCK 2[0] = next free cluster = start cluster for new file with size
		// =1
		block = fat_get_free_cluster(block);
		startcluster = block;

		// Find space in the directory for this filename (or allocate some more)
		if ((index = fat_find_free_offset(dirCluster, entry)) < 0) {
			throw IOExc;
		}

		// Main cluster following loop
		int currentIndex = 0;

		// System.out.println("dir_cluster[0] ="+dir_cluster[0]);

		if (dirCluster == 0) {
			block = fat_root_dir_addr();
		} else {
			// Berechnung des Blocks aus BlockCount und Cluster aus FATTabelle
			// Berechnung welcher Cluster zu laden ist
			// Auslesen der FAT - Tabelle
			block = fat_load(dirCluster, block);
			block = ((block - 2) * cluster_size) + cluster_offset;
		}

		// auslesen des gesamten Directory
		for (int blk = block;; blk++) {
			FatMmc.mmc_read_sector(blk, tmp_buffer); // Lesen eines Blocks des Root Directory
			for (int a = 0; a < BlockSize; a = a + 32) {
				if (currentIndex == index) {
					entry.setName(filename);
					entry.DIR_Attr = ATTR_ARCHIVE;
					entry.DIR_FstClusLO = startcluster;
					entry.DIR_FileSize = 0;

					entry.dump(tmp_buffer, a);

					// Eintrag gefunden ndern und speichern
					FatMmc.mmc_write_sector(blk, tmp_buffer);

					return entry;
				}

				currentIndex++;
			}
		}

	}

}