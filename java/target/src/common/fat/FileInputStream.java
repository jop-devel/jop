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

import java.io.IOException;

public class FileInputStream extends FileStream {

	private int[] buffer = new int[FatItS.BlockSize];
	private DirEntry entryBuffer = new DirEntry();

	private long size = 0;
	private int startcluster = 0;
	private long offset = 0;

	/**
	 * Creates a FileInputStream by opening a connection to an actual file, the
	 * file named by the path name name in the file system.
	 *
	 * If the named file does not exist, is a directory rather than a regular
	 * file, or for some other reason cannot be opened for reading then an
	 * Exception is thrown.
	 *
	 * Directories and files are seperated with "/". Do not write the leading
	 * "/" in the filename string.
	 */

	public FileInputStream(int streamtype, String filename) throws IOException {
		super(streamtype);
		char[] f = new char[255];
		for (int i = 0; i < filename.length() && i < 255; i++) {
			f[i] = filename.charAt(i);
		}
		init(f);
	}

	public FileInputStream(int streamtpye, StringBuffer filename) throws IOException {
		super(streamtpye);
		char[] f = new char[255];
		for (int i = 0; i < filename.length() && i < 255; i++) {
			f[i] = filename.charAt(i);
		}
		init(f);
	}

	private void init(char[] filename) throws IOException {

		char[] subdir = new char[13];

		int inroot = 0;
		int i;

		FatItS.fat_init(fatlowlevel);

		if (filename[0] == 0) {
			throw new IOException();
		}

		for (i = 0; filename[i] != 0; i++) {
			if (filename[i] == '/') {
				inroot++;
			}
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

		entryBuffer = FatItS.fat_search_file(fatlowlevel, entryBuffer.getCluster(), filename, entryBuffer);
		size = entryBuffer.getSize();
		startcluster = entryBuffer.getCluster();
		offset = 0;
	}

	/***************************************************************************
	 * public int read() Reads up to one byte of data from this input stream.
	 * This method blocks until some input is available.
	 *
	 * Parameters:
	 *
	 * Returns: the next byte of data, or -1 if the end of the file is reached.
	 **************************************************************************/
	public int read() {
		int block;
		int byte_count;

		if (offset >= size) {
			return -1;
		}

		block = ((int) offset) / FatItS.BlockSize;
		byte_count = ((int) offset) % FatItS.BlockSize;

		FatItS.fat_read_file(fatlowlevel, startcluster, buffer, block);

		offset++;

		return (int) buffer[byte_count];
	}

	/***************************************************************************
	 * public int read(byte[] b) Reads up to b.length bytes of data from this
	 * input stream into an array of bytes. This method blocks until some input
	 * is available.
	 *
	 * Parameters: b - the buffer into which the data is read. Returns: the
	 * total number of bytes read into the buffer, or -1 if there is no more
	 * data because the end of the file has been reached.
	 **************************************************************************/
	public int read(byte b[]) {
		int block = 0xFFFFFFFF, block_alt;
		int byte_count, i = 0;

		if (offset >= size) {
			return -1;
		}

		while (!(offset >= size) && (i < b.length)) {

			block_alt = block;
			block = ((int) offset) / FatItS.BlockSize;
			byte_count = ((int) offset) % FatItS.BlockSize;

			if (block_alt != block) {
				FatItS.fat_read_file(fatlowlevel, startcluster, buffer, block);
			}

			offset++;

			b[i] = (byte) buffer[byte_count];

			i++;
		}

		return i;
	}

	/***************************************************************************
	 * public int read(byte[] b, int off, int len) Reads up to len bytes of data
	 * from this input stream into an array of bytes. This method blocks until
	 * some input is available.
	 *
	 * Parameters: b - the buffer into which the data is read. off - the start
	 * offset of the data. len - the maximum number of bytes read. Returns: the
	 * total number of bytes read into the buffer, or -1 if there is no more
	 * data because the end of the file has been reached.
	 **************************************************************************/
	public int read(byte b[], int off, int len) {

		if (offset >= size) {
			return -1;
		}

		if ((offset + off) >= size) {
			return -1;
		}

		offset = offset + off;

		int block = 0xFFFFFFFF, block_alt;
		int byte_count, i = 0;

		while ((!(offset >= size)) && (i < len) && (i < b.length)) {

			block_alt = block;
			block = ((int) offset) / FatItS.BlockSize;
			byte_count = ((int) offset) % FatItS.BlockSize;

			if (block_alt != block) {
				FatItS.fat_read_file(fatlowlevel, startcluster, buffer, block);
			}

			offset++;

			b[i] = (byte) buffer[byte_count];

			i++;
		}

		return i;
	}

	/***************************************************************************
	 * public long skip(long n) Skips over and discards n bytes of data from the
	 * input stream. The skip method may, for a variety of reasons, end up
	 * skipping over some smaller number of bytes, possibly 0. The actual number
	 * of bytes skipped is returned.
	 *
	 *
	 * Parameters: n - the number of bytes to be skipped. Returns: the actual
	 * number of bytes skipped.
	 **************************************************************************/

	public long skip(long n) {
		long k = 0;

		if (offset >= size) {
			return (0);
		}

		if ((offset + n) > size) {
			k = size - offset;
			offset = offset + k;
			return (k);
		}

		offset = offset + n;

		return (n);
	}

	/***************************************************************************
	 * public void available() returns the number of the number of bytes that
	 * can be read from this file input stream without blocking.
	 **************************************************************************/
	
	public int available() {
		return (int)(size - offset);
	}
	
	/***************************************************************************
	 * public void close() Closes this file input stream and releases any system
	 * resources associated with the stream.
	 **************************************************************************/

	public void close() {
	}

}