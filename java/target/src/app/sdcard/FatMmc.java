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

import com.jopdesign.sys.*;

public class FatMmc {

	public static int mmc_init() {

		int x = 0;

		Native.wrMem(0x01, Const.WB_SPI + 1); // spi core reset
		Native.wrMem(0x04, Const.WB_SPI + 0x0b); // SPI_CLK_DEL_REG
		Native.wrMem(0x01, Const.WB_SPI + 2); // TRANS_TYPE = INIT_SD
		Native.wrMem(0x01, Const.WB_SPI + 3); // TRANS_CTRL_REG =
												// SPI_TRANS_START
		do {
			x = Native.rdMem(Const.WB_SPI + 4);
		} while ((x & 0x01) == 1); // TRANS_STS_REG==TRANS_BUSY

		// TRANS_ERROR_REG[5:4] == 00
		x = Native.rdMem(Const.WB_SPI + 5);

		if ((x & 0xFF) == 0) {
			return 0x00;
		}

		// System.out.print("ERROR mmc_init() = ");
		// System.out.println(x);

		return x;
	}

	public static int mmc_read_sector(int addr, int[] buffer) {

		int i;

		Native.wrMem(0, Const.WB_SPI + 0x07); // SD_ADDR_7_0
		Native.wrMem((addr << 1) & 0xfe, Const.WB_SPI + 0x08); // SD_ADDR_15_8
		Native.wrMem((addr >> 7) & 0xff, Const.WB_SPI + 0x09); // SD_ADDR_23_16
		Native.wrMem((addr >> 15) & 0xff, Const.WB_SPI + 0x0a); // SD_ADDR_31_24

		Native.wrMem(0x02, Const.WB_SPI + 2); // TRANS_TYPE = RW_READ_SD_BLOCK
		Native.wrMem(0x01, Const.WB_SPI + 3); // TRANS_CTRL_REG =
												// SPI_TRANS_START
		while ((Native.rdMem(Const.WB_SPI + 4) & 0x01) == 1)
			; // TRANS_STS_REG==TRANS_BUSY

		// TRANS_ERROR_REG[5:4] == 00
		if ((Native.rdMem(Const.WB_SPI + 5) & 0x3F) == 0) {
			for (i = 0; i < 512; i++) {
				buffer[i] = Native.rdMem(Const.WB_SPI + 0x10) & 0xFF;
				//RX_FIFO_DATA_REG
			}
		} else {
			// System.out.println("ERROR mmc_read_sector");
			return 1;
		}

		Native.wrMem(0xFF, Const.WB_SPI + 0x14); // Clear rx fifo
		return 0;
	}

	public static int mmc_write_sector(int addr, int[] buffer) {

		int i;

		Native.wrMem(0x01, Const.WB_SPI + 0x24); // RX_FIFO_DATA_REG

		for (i = 0; i < 512; i++) {
			Native.wrMem(buffer[i], Const.WB_SPI + 0x20); // TX_FIFO_DATA_REG
		}

		Native.wrMem(0, Const.WB_SPI + 0x07); // SD_ADDR_7_0
		Native.wrMem((addr << 1) & 0xfe, Const.WB_SPI + 0x08); // SD_ADDR_15_8
		Native.wrMem((addr >> 7) & 0xff, Const.WB_SPI + 0x09); // SD_ADDR_23_16
		Native.wrMem((addr >> 15) & 0xff, Const.WB_SPI + 0x0a); // SD_ADDR_31_24

		Native.wrMem(0x03, Const.WB_SPI + 2); // TRANS_TYPE = RW_WRITE_SD_BLOCK
		Native.wrMem(0x01, Const.WB_SPI + 3); // TRANS_CTRL_REG = SPI_TRANS_START
		while ((Native.rdMem(Const.WB_SPI + 4) & 0x01) == 1)
			; // TRANS_STS_REG==TRANS_BUSY

		// TRANS_ERROR_REG[5:4] == 00
		i = Native.rdMem(Const.WB_SPI + 5);
		if (i != 0) {
			// System.out.println("ERROR mmc_write_sector");
			return 1;
		}

		return 0;
	}

}
