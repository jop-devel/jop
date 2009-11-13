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

import java.io.FileNotFoundException;
import java.io.IOException;

public class FileOutputStream extends FileStream {

	private int[] buffer = new int[FatItS.BlockSize];
	private DirEntry entryBuffer = new DirEntry();

	private long size = 0;
	private int attribs = 0;
	private int startcluster = 0;
	private long offset = 0;

	private char[] filename = new char[255];

	private int dirCluster;

	/***************************************************************************
	 * public FileOutputStream(String file) Creates an output file stream to
	 * write to the file with the specified name.
	 *
	 * If the file exists but is a directory rather than a regular file, does
	 * not exist but cannot be created, or cannot be opened for any other reason
	 * then a ?????????????????????????????.
	 *
	 * Parameters: file - Directories and files are seperated with "/". Do not
	 * write the leading "/" in the filename string. append - if true, then
	 * bytes will be written to the end of the file rather than the beginning
	 **************************************************************************/
	public FileOutputStream(int streamtype, String file, boolean append) throws IOException {
		super(streamtype);
		
		int i = 0;
		char[] subdir = new char[13];
		int inroot = 0;

		while (i < file.length()) {
			filename[i] = file.charAt(i);
			i++;
		}

		i = 0;

		FatItS.fat_init(fatlowlevel);

		if (filename[0] == 0) {
			throw new IOException();
		}

		while (filename[i] != 0) {
			if (filename[i] == '/') {
				inroot++;
			}
			i++;
		}

		if ((i > 12) && (inroot == 0)) {
			throw new IOException();
		}

		i = 0;
		int j = 0;

		for (int b = 0; b < inroot; b++) {
			j = 0;

			while (filename[i] != '/') {
				subdir[j] = filename[i];
				if (i > 11) {
					throw new IOException();
				}

				j++;
				i++;
			}

			j++;
			i++;
			subdir[j] = 0;

			entryBuffer = FatItS.fat_search_file(fatlowlevel, entryBuffer.getCluster(), subdir, entryBuffer);
		}

		if (inroot != 0) {
			for (int b = 0; b < 13; b++) {
				filename[b] = filename[b + i];
				if (filename[b + i] == 0) {
					inroot = 0;
					break;
				}
			}

			if (inroot != 0) {
				throw new IOException();
			}

		}

		dirCluster = entryBuffer.getCluster();

		try {
			entryBuffer = FatItS.fat_search_file(fatlowlevel, dirCluster, filename, entryBuffer);
		} catch (FileNotFoundException exc) {
			entryBuffer = FatItS.fat_add_file(fatlowlevel, dirCluster, filename, entryBuffer);
		}

		size = entryBuffer.getSize();
		startcluster = entryBuffer.getCluster();
		attribs = entryBuffer.getAttribs();
		offset = 0;

		if (append == false) {
			size = 0;
			entryBuffer = FatItS.fat_mod_file(fatlowlevel, dirCluster, filename, size, attribs, entryBuffer);
		} else {
			offset = size;
		}
	}

	/***************************************************************************
	 * public FileOutputStream(String filename, boolean append) Creates an
	 * output file stream to write to the file with the specified name. If the
	 * second argument is true, then bytes will be written to the end of the
	 * file rather than the beginning.
	 *
	 * If the file exists but is a directory rather than a regular file, does
	 * not exist but cannot be created, or cannot be opened for any other reason
	 * then a ?????????????????????????????.
	 *
	 * Parameters: file - Directories and files are seperated with "/". Do not
	 * write the leading "/" in the filename string.
	 **************************************************************************/
	public FileOutputStream(int streamtype, String filename) throws IOException {
		this(streamtype, filename, false);
	}

	/***************************************************************************
	 * public void close() Closes this file output stream and releases any
	 * system resources associated with this stream. This file output stream may
	 * no longer be used for writing bytes.
	 **************************************************************************/
	public void close() {
		fatlowlevel.Flush();
	}

	/***************************************************************************
	 * public void write(byte[] b) Writes b.length or while (b[i++] != 0) bytes
	 * from the specified byte array to this file output stream.
	 *
	 * Parameters: b - the data.
	 **************************************************************************/
	public void write(byte[] b) throws IOException {
		write(b, (int) offset, b.length);
	}

	/***************************************************************************
	 * public void write(byte[] b, int off, int len) Writes len bytes from the
	 * specified byte array starting at offset off to this file output stream.
	 *
	 * Overrides: write in class OutputStream Parameters: b - the data. off -
	 * the start offset in the data. len - the number of bytes to write.
	 **************************************************************************/
	public void write(byte[] b, int off, int len) throws IOException {

		int block;
		int byte_count;

		if (b.length < len) {
			len = b.length;
		}

		if (((offset + (long) (off + len))) > (size + 1)) {

			if (((offset + (long) (off + len)) / FatItS.BlockSize) < (size / FatItS.BlockSize)) {

				int diff = ((int) (offset + (long) (off + len)) / FatItS.BlockSize)	- (int) (size / FatItS.BlockSize);

				for (int i = 0; i < diff; i++) {
					FatItS.fat_grow_file(fatlowlevel, startcluster);
				}

				size = offset + (long) (off + len - 1);
				entryBuffer = FatItS.fat_mod_file(fatlowlevel, dirCluster, filename, size, attribs, entryBuffer);
			}
		}

		offset = offset + (long) off;

		int k = 0;

		for (int i = 0; i < ((len / FatItS.BlockSize) + 1); i++) {

			block = ((int) offset) / FatItS.BlockSize;
			byte_count = ((int) offset) % FatItS.BlockSize;

			FatItS.fat_read_file(fatlowlevel, startcluster, buffer, block);

			while ((byte_count < 512) && (k < len)) {
				buffer[byte_count] = (int) b[k];
				k++;
				byte_count++;
				offset++;
				if ((int) size <= (int) offset) {
					size++;
				}
			}

			offset--;

			FatItS.fat_write_file(fatlowlevel, startcluster, buffer, block);

			offset++;

			if (size <= offset) {
				size++;

				if (((offset) % FatItS.BlockSize) == 0) {
					FatItS.fat_grow_file(fatlowlevel, startcluster);
				}

				entryBuffer = FatItS.fat_mod_file(fatlowlevel, dirCluster, filename, size-1, attribs, entryBuffer);
			}

			if (!(k < len)) {
				break;
			}
		}
	}

	/***************************************************************************
	 * public void write(int b) Writes the specified byte to this file output
	 * stream. Implements the write method of OutputStream.
	 *
	 * Parameters: b - the byte to be written.
	 **************************************************************************/
	public void write(int b) throws IOException {

		int block;
		int byte_count;

		block = ((int) offset) / FatItS.BlockSize;
		byte_count = ((int) offset) % FatItS.BlockSize;

		FatItS.fat_read_file(fatlowlevel, startcluster, buffer, block);

		buffer[byte_count] = b;
		FatItS.fat_write_file(fatlowlevel, startcluster, buffer, block);

		offset++;

		if (size <= offset) {
			size++;

			if (((offset) % FatItS.BlockSize) == 0) {
				FatItS.fat_grow_file(fatlowlevel, startcluster);
			}

			entryBuffer = FatItS.fat_mod_file(fatlowlevel, dirCluster, filename, size-1, attribs, entryBuffer);
		}
	}

	public void delete() {
		// TODO implement delete of this file -> call fatits.unlink
	}

}
