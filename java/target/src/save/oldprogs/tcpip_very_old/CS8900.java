package tcpip_old;

/**
*	CS8900.java
*
*	Low level interface to Cirrus Logic ethernet chip CS8900A.
*
*	Adapted from cs89x0.c driver for linux by 
*		Martin Schoeberl (martin.schoeberl@chello.at)
*
*	Written 1996 by Russell Nelson, with reference to skeleton.c
*	written 1993-1994 by Donald Becker.
*	Copyright, 1988-1992, Russell Nelson, Crynwr Software
*
*	This program is free software; you can redistribute it and/or modify
*	it under the terms of the GNU General Public License as published by
*	the Free Software Foundation, version 1.
*
*	This program is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU General Public License for more details.
*
*	You should have received a copy of the GNU General Public License
*	along with this program; if not, write to the Free Software
*	Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*
*		The author may be reached at nelson@crynwr.com, Crynwr
*		Software, 521 Pleasant Valley Rd., Potsdam, NY 13676
*
*   Changelog:
*		2002-06-28	first working version
*/

import util.*;

public class CS8900 {

	public static final int BUF_LEN = 6*256;
/**
*	receive buffer. rx frames goes here and rxCnt is set. Remove data from
*	this buffer asap and set rxCnt to zero.
*/
	public static int[] rxBuf;				// receive buffer
/**
*	counter (and flag) for received data.
*/
	public static int rxCnt;				// cnt of received bytes
/**
*	flag allows new frame to be sent.
*/
	public static boolean txFree;			// ready for next transmit

	private static int[] eth;				// own ethernet address

	private static int chip_type;		/* one of: CS8900, CS8920, CS8920M */
	private static int chip_revision;	/* revision letter of the chip ('A'...) */
	private static int send_cmd;		/* the proper send command: TX_NOW, TX_AFTER_381, or TX_AFTER_ALL */
	private static int rx_mode;		/* what mode are we in? 0, RX_MULTCAST_ACCEPT, or RX_ALL_ACCEPT */
	private static int curr_rx_cfg;	/* a copy of PP_RxCFG */
	private static int send_underrun;	/* keep track of how many underruns in a row we get */

/* some statistics from NS89xx.c */
	private static int tx_packets;
	private static int tx_bytes;
	private static int collisions;

	private static int rx_packets;
	private static int rx_bytes;
	private static int rx_dropped;
	private static int multicast;

	private static int tx_errors;
	private static int tx_aborted_errors;
	private static int tx_carrier_errors;
	private static int tx_fifo_errors;
	private static int tx_heartbeat_errors;
	private static int tx_window_errors;

	private static int rx_errors;
	private static int rx_over_errors;
	private static int rx_length_errors;
	private static int rx_frame_errors;
	private static int rx_crc_errors;
	private static int rx_missed_errors;
	private static int rx_fifo_errors;


/******** cs.h **********/

	private static final int CS8900 = 0x0000;
	private static final int CS8920 = 0x4000;
	private static final int CS8920M = 0x6000;
	private static final int REVISON_BITS = 0x1F00;

	private static final int PP_ChipID = 0x0000;	/* offset   0h -> Corp -ID              */
				/* offset   2h -> Model/Product Number  */
				/* offset   3h -> Chip Revision Number  */

	private static final int PP_ISAIOB = 0x0020;	/*  IO base address */

	private static final int PP_RxCFG = 0x0102;		/*  Rx Bus config */
	private static final int PP_RxCTL = 0x0104;		/*  Receive Control Register */
	private static final int PP_TxCFG = 0x0106;		/*  Transmit Config Register */
	private static final int PP_TxCMD = 0x0108;		/*  Transmit Command Register */
	private static final int PP_BufCFG = 0x010A;	/*  Bus configuration Register */
	private static final int PP_LineCTL = 0x0112;	/*  Line Config Register */
	private static final int PP_SelfCTL = 0x0114;	/*  Self Command Register */
	private static final int PP_BusCTL = 0x0116;	/*  ISA bus control Register */
	private static final int PP_TestCTL = 0x0118;	/*  Test Register */
	private static final int PP_AutoNegCTL = 0x011C;	/*  Auto Negotiation Ctrl */

	private static final int PP_ISQ = 0x0120;		/*  Interrupt Status */
	private static final int PP_RxEvent = 0x0124;	/*  Rx Event Register */
	private static final int PP_TxEvent = 0x0128;	/*  Tx Event Register */
	private static final int PP_BufEvent = 0x012C;	/*  Bus Event Register */
	private static final int PP_RxMiss = 0x0130;	/*  Receive Miss Count */
	private static final int PP_TxCol = 0x0132;		/*  Transmit Collision Count */
	private static final int PP_LineST = 0x0134;	/*  Line State Register */
	private static final int PP_SelfST = 0x0136;	/*  Self State register */
	private static final int PP_BusST = 0x0138;		/*  Bus Status */
	private static final int PP_TDR = 0x013C;		/*  Time Domain Reflectometry */
	private static final int PP_AutoNegST = 0x013E;	/*  Auto Neg Status */
	private static final int PP_TxCommand = 0x0144;	/*  Tx Command */
	private static final int PP_TxLength = 0x0146;	/*  Tx Length */
	private static final int PP_LAF = 0x0150;		/*  Hash Table */
	private static final int PP_IA = 0x0158;		/*  Physical Address Register */

	private static final int PP_RxStatus = 0x0400;	/*  Receive start of frame */
	private static final int PP_RxLength = 0x0402;	/*  Receive Length of frame */
	private static final int PP_RxFrame = 0x0404;	/*  Receive frame pointer */
	private static final int PP_TxFrame = 0x0A00;	/*  Transmit frame pointer */

	private static final int ADD_MASK = 0x3000;		/*  Mask it use of the ADD_PORT register */
	private static final int ADD_SIG = 0x3000;		/*  Expected ID signature */

	private static final int CHIP_EISA_ID_SIG = 0x630E;   /*  Product ID Code for Crystal Chip (CS8900 spec 4.3) */

	private static final int PRODUCT_ID_ADD = 0x0002;   /*  Address of product ID */

/*  Mask to find out the types of  registers */
	private static final int REG_TYPE_MASK = 0x001F;

/*  Defines Control/Config register quintuplet numbers */
	private static final int RX_BUF_CFG = 0x0003;
	private static final int RX_CONTROL = 0x0005;
	private static final int TX_CFG = 0x0007;
	private static final int TX_COMMAND = 0x0009;
	private static final int BUF_CFG = 0x000B;
	private static final int LINE_CONTROL = 0x0013;
	private static final int SELF_CONTROL = 0x0015;
	private static final int BUS_CONTROL = 0x0017;
	private static final int TEST_CONTROL = 0x0019;

/*  Defines Status/Count registers quintuplet numbers */
	private static final int RX_EVENT = 0x0004;
	private static final int TX_EVENT = 0x0008;
	private static final int BUF_EVENT = 0x000C;
	private static final int RX_MISS_COUNT = 0x0010;
	private static final int TX_COL_COUNT = 0x0012;
	private static final int LINE_STATUS = 0x0014;
	private static final int SELF_STATUS = 0x0016;
	private static final int BUS_STATUS = 0x0018;
	private static final int TDR = 0x001C;

/* PP_RxCFG - Receive  Configuration and Interrupt Mask bit definition -  Read/write */
	private static final int SKIP_1 = 0x0040;
	private static final int RX_STREAM_ENBL = 0x0080;
	private static final int RX_OK_ENBL = 0x0100;
	private static final int RX_DMA_ONLY = 0x0200;
	private static final int AUTO_RX_DMA = 0x0400;
	private static final int BUFFER_CRC = 0x0800;
	private static final int RX_CRC_ERROR_ENBL = 0x1000;
	private static final int RX_RUNT_ENBL = 0x2000;
	private static final int RX_EXTRA_DATA_ENBL = 0x4000;

/* PP_RxCTL - Receive Control bit definition - Read/write */
	private static final int RX_IA_HASH_ACCEPT = 0x0040;
	private static final int RX_PROM_ACCEPT = 0x0080;
	private static final int RX_OK_ACCEPT = 0x0100;
	private static final int RX_MULTCAST_ACCEPT = 0x0200;
	private static final int RX_IA_ACCEPT = 0x0400;
	private static final int RX_BROADCAST_ACCEPT = 0x0800;
	private static final int RX_BAD_CRC_ACCEPT = 0x1000;
	private static final int RX_RUNT_ACCEPT = 0x2000;
	private static final int RX_EXTRA_DATA_ACCEPT = 0x4000;
	private static final int RX_ALL_ACCEPT = (RX_PROM_ACCEPT|RX_BAD_CRC_ACCEPT|RX_RUNT_ACCEPT|RX_EXTRA_DATA_ACCEPT);
/*  Default receive mode - individually addressed, broadcast, and error free */
	private static final int DEF_RX_ACCEPT = (RX_IA_ACCEPT | RX_BROADCAST_ACCEPT | RX_OK_ACCEPT);

/* PP_TxCFG - Transmit Configuration Interrupt Mask bit definition - Read/write */
	private static final int TX_LOST_CRS_ENBL = 0x0040;
	private static final int TX_SQE_ERROR_ENBL = 0x0080;
	private static final int TX_OK_ENBL = 0x0100;
	private static final int TX_LATE_COL_ENBL = 0x0200;
	private static final int TX_JBR_ENBL = 0x0400;
	private static final int TX_ANY_COL_ENBL = 0x0800;
	private static final int TX_16_COL_ENBL = 0x8000;

/* PP_TxCMD - Transmit Command bit definition - Read-only */
	private static final int TX_START_4_BYTES = 0x0000;
	private static final int TX_START_64_BYTES = 0x0040;
	private static final int TX_START_128_BYTES = 0x0080;
	private static final int TX_START_ALL_BYTES = 0x00C0;
	private static final int TX_FORCE = 0x0100;
	private static final int TX_ONE_COL = 0x0200;
	private static final int TX_TWO_PART_DEFF_DISABLE = 0x0400;
	private static final int TX_NO_CRC = 0x1000;
	private static final int TX_RUNT = 0x2000;

/* PP_BufCFG - Buffer Configuration Interrupt Mask bit definition - Read/write */
	private static final int GENERATE_SW_INTERRUPT = 0x0040;
	private static final int RX_DMA_ENBL = 0x0080;
	private static final int READY_FOR_TX_ENBL = 0x0100;
	private static final int TX_UNDERRUN_ENBL = 0x0200;
	private static final int RX_MISS_ENBL = 0x0400;
	private static final int RX_128_BYTE_ENBL = 0x0800;
	private static final int TX_COL_COUNT_OVRFLOW_ENBL = 0x1000;
	private static final int RX_MISS_COUNT_OVRFLOW_ENBL = 0x2000;
	private static final int RX_DEST_MATCH_ENBL = 0x8000;

/* PP_LineCTL - Line Control bit definition - Read/write */
	private static final int SERIAL_RX_ON = 0x0040;
	private static final int SERIAL_TX_ON = 0x0080;
	private static final int AUI_ONLY = 0x0100;
	private static final int AUTO_AUI_10BASET = 0x0200;
	private static final int MODIFIED_BACKOFF = 0x0800;
	private static final int NO_AUTO_POLARITY = 0x1000;
	private static final int TWO_PART_DEFDIS = 0x2000;
	private static final int LOW_RX_SQUELCH = 0x4000;

/* PP_SelfCTL - Software Self Control bit definition - Read/write */
	private static final int POWER_ON_RESET = 0x0040;
	private static final int SW_STOP = 0x0100;
	private static final int SLEEP_ON = 0x0200;
	private static final int AUTO_WAKEUP = 0x0400;
	private static final int HCB0_ENBL = 0x1000;
	private static final int HCB1_ENBL = 0x2000;
	private static final int HCB0 = 0x4000;
	private static final int HCB1 = 0x8000;

/* PP_BusCTL - ISA Bus Control bit definition - Read/write */
	private static final int RESET_RX_DMA = 0x0040;
	private static final int MEMORY_ON = 0x0400;
	private static final int DMA_BURST_MODE = 0x0800;
	private static final int IO_CHANNEL_READY_ON = 0x1000;
	private static final int RX_DMA_SIZE_64K = 0x2000;
	private static final int ENABLE_IRQ = 0x8000;

/* PP_TestCTL - Test Control bit definition - Read/write */
	private static final int LINK_OFF = 0x0080;
	private static final int ENDEC_LOOPBACK = 0x0200;
	private static final int AUI_LOOPBACK = 0x0400;
	private static final int BACKOFF_OFF = 0x0800;
	private static final int FDX_8900 = 0x4000;
	private static final int FAST_TEST = 0x8000;

/* PP_RxEvent - Receive Event Bit definition - Read-only */
	private static final int RX_IA_HASHED = 0x0040;
	private static final int RX_DRIBBLE = 0x0080;
	private static final int RX_OK = 0x0100;
	private static final int RX_HASHED = 0x0200;
	private static final int RX_IA = 0x0400;
	private static final int RX_BROADCAST = 0x0800;
	private static final int RX_CRC_ERROR = 0x1000;
	private static final int RX_RUNT = 0x2000;
	private static final int RX_EXTRA_DATA = 0x4000;

	private static final int HASH_INDEX_MASK = 0x0FC00;

/* PP_TxEvent - Transmit Event Bit definition - Read-only */
	private static final int TX_LOST_CRS = 0x0040;
	private static final int TX_SQE_ERROR = 0x0080;
	private static final int TX_OK = 0x0100;
	private static final int TX_LATE_COL = 0x0200;
	private static final int TX_JBR = 0x0400;
	private static final int TX_16_COL = 0x8000;
	private static final int TX_SEND_OK_BITS = (TX_OK|TX_LOST_CRS);
	private static final int TX_COL_COUNT_MASK = 0x7800;

/* PP_BufEvent - Buffer Event Bit definition - Read-only */
	private static final int SW_INTERRUPT = 0x0040;
	private static final int RX_DMA = 0x0080;
	private static final int READY_FOR_TX = 0x0100;
	private static final int TX_UNDERRUN = 0x0200;
	private static final int RX_MISS = 0x0400;
	private static final int RX_128_BYTE = 0x0800;
	private static final int TX_COL_OVRFLW = 0x1000;
	private static final int RX_MISS_OVRFLW = 0x2000;
	private static final int RX_DEST_MATCH = 0x8000;

/* PP_LineST - Ethernet Line Status bit definition - Read-only */
	private static final int LINK_OK = 0x0080;
	private static final int AUI_ON = 0x0100;
	private static final int TENBASET_ON = 0x0200;
	private static final int POLARITY_OK = 0x1000;
	private static final int CRS_OK = 0x4000;

/* PP_SelfST - Chip Software Status bit definition */
	private static final int ACTIVE_33V = 0x0040;
	private static final int INIT_DONE = 0x0080;
	private static final int SI_BUSY = 0x0100;
	private static final int EEPROM_PRESENT = 0x0200;
	private static final int EEPROM_OK = 0x0400;
	private static final int EL_PRESENT = 0x0800;
	private static final int EE_SIZE_64 = 0x1000;

/* PP_BusST - ISA Bus Status bit definition */
	private static final int TX_BID_ERROR = 0x0080;
	private static final int READY_FOR_TX_NOW = 0x0100;

/* PP_AutoNegCTL - Auto Negotiation Control bit definition */
	private static final int RE_NEG_NOW = 0x0040;
	private static final int ALLOW_FDX = 0x0080;
	private static final int AUTO_NEG_ENABLE = 0x0100;
	private static final int NLP_ENABLE = 0x0200;
	private static final int FORCE_FDX = 0x8000;
	private static final int AUTO_NEG_BITS = (FORCE_FDX|NLP_ENABLE|AUTO_NEG_ENABLE);
	private static final int AUTO_NEG_MASK = (FORCE_FDX|NLP_ENABLE|AUTO_NEG_ENABLE|ALLOW_FDX|RE_NEG_NOW);

/* PP_AutoNegST - Auto Negotiation Status bit definition */
	private static final int AUTO_NEG_BUSY = 0x0080;
	private static final int FLP_LINK = 0x0100;
	private static final int FLP_LINK_GOOD = 0x0800;
	private static final int LINK_FAULT = 0x1000;
	private static final int HDX_ACTIVE = 0x4000;
	private static final int FDX_ACTIVE = 0x8000;

/*  The following block defines the ISQ event types */
	private static final int ISQ_RECEIVER_EVENT = 0x04;
	private static final int ISQ_TRANSMITTER_EVENT = 0x08;
	private static final int ISQ_BUFFER_EVENT = 0x0c;
	private static final int ISQ_RX_MISS_EVENT = 0x10;
	private static final int ISQ_TX_COL_EVENT = 0x12;

	private static final int ISQ_EVENT_MASK = 0x003F;   /*  ISQ mask to find out type of event */
	private static final int ISQ_HIST = 16;		/*  small history buffer */
	private static final int AUTOINCREMENT = 0x8000;	/*  Bit mask to set bit-15 for autoincrement */

	private static final int TXRXBUFSIZE = 0x0600;
	private static final int RXDMABUFSIZE = 0x8000;
	private static final int RXDMASIZE = 0x4000;
	private static final int TXRX_LENGTH_MASK = 0x07FF;

/*  rx options bits */
	private static final int RCV_WITH_RXON	= 1;       /*  Set SerRx ON */
	private static final int RCV_COUNTS	= 2;       /*  Use Framecnt1 */
	private static final int RCV_PONG	= 4;       /*  Pong respondent */
	private static final int RCV_DONG	= 8;       /*  Dong operation */
	private static final int RCV_POLLING	= 0x10;	/*  Poll RxEvent */
	private static final int RCV_ISQ		= 0x20;	/*  Use ISQ, int */
	private static final int RCV_AUTO_DMA	= 0x100;	/*  Set AutoRxDMAE */
	private static final int RCV_DMA		= 0x200;	/*  Set RxDMA only */
	private static final int RCV_DMA_ALL	= 0x400;	/*  Copy all DMA'ed */
	private static final int RCV_FIXED_DATA	= 0x800;	/*  Every frame same */
	private static final int RCV_IO		= 0x1000;	/*  Use ISA IO only */
	private static final int RCV_MEMORY	= 0x2000;	/*  Use ISA Memory */

	private static final int RAM_SIZE	= 0x1000;       /*  The card has 4k bytes or RAM */
	private static final int PKT_START = PP_TxFrame;  /*  Start of packet RAM */

	private static final int RX_FRAME_PORT	= 0x0000;
	private static final int TX_FRAME_PORT = RX_FRAME_PORT;
	private static final int TX_CMD_PORT	= 0x0004;
	private static final int TX_NOW		= 0x0000;       /*  Tx packet after   5 bytes copied */
	private static final int TX_AFTER_381	= 0x0040;       /*  Tx packet after 381 bytes copied */
	private static final int TX_AFTER_ALL	= 0x00c0;       /*  Tx packet after all bytes copied */
	private static final int TX_LEN_PORT	= 0x0006;
	private static final int ISQ_PORT	= 0x0008;
	private static final int ADD_PORT	= 0x000A;
	private static final int DATA_PORT	= 0x000C;


/*  Receive Header */
/*  Description of header of each packet in receive area of memory */
	private static final int RBUF_EVENT_LOW	= 0;   /*  Low byte of RxEvent - status of received frame */
	private static final int RBUF_EVENT_HIGH	= 1;   /*  High byte of RxEvent - status of received frame */
	private static final int RBUF_LEN_LOW	= 2;   /*  Length of received data - low byte */
	private static final int RBUF_LEN_HI	= 3;   /*  Length of received data - high byte */
	private static final int RBUF_HEAD_LEN	= 4;   /*  Length of this header */

	private static final int CHIP_READ = 0x1;   /*  Used to mark state of the repins code (chip or dma) */
	private static final int DMA_READ = 0x2;   /*  Used to mark state of the repins code (chip or dma) */


	private static final int IMM_BIT = 0x0040;		/*  ignore missing media	 */

	private static final int A_CNF_10B_T = 0x0001;
	private static final int A_CNF_AUI = 0x0002;
	private static final int A_CNF_10B_2 = 0x0004;
	private static final int A_CNF_MEDIA_TYPE = 0x0060;
	private static final int A_CNF_MEDIA_AUTO = 0x0000;
	private static final int A_CNF_MEDIA_10B_T = 0x0020;
	private static final int A_CNF_MEDIA_AUI = 0x0040;
	private static final int A_CNF_MEDIA_10B_2 = 0x0060;
	private static final int A_CNF_DC_DC_POLARITY = 0x0080;
	private static final int A_CNF_NO_AUTO_POLARITY = 0x2000;
	private static final int A_CNF_LOW_RX_SQUELCH = 0x4000;
	private static final int A_CNF_EXTND_10B_2 = 0x8000;

	private static final int PACKET_PAGE_OFFSET = 0x8;

	private static final int BIT0 = 1;
	private static final int BIT15 = 0x8000;

/******** end cs.h **********/


	public static void main(String[] args) {

		int i;

		Timer.init(20000000, 5);

		int[] e = new int[6];
		e[0] = 0xaa;
		e[1] = 0xbb;
		e[2] = 0xcc;
		e[3] = 0xdd;
		e[4] = 0xee;
		e[5] = 0xff;

		init(e);

Timer.sleepWd(1000);

		for (;;) {
			poll();
			if (rxCnt!=0) {
				Dbg.intVal(rxCnt);
				rxCnt = 0;
			}
		}
	}

	private static int readReg(int reg) {
		Isa.wr(ADD_PORT, reg);
		Isa.wr(ADD_PORT+1, reg>>8);
		return Isa.rd(DATA_PORT) + (Isa.rd(DATA_PORT+1)<<8);
	}
	private static void writeReg(int reg, int value) {
		Isa.wr(ADD_PORT, reg);
		Isa.wr(ADD_PORT+1, reg>>8);
		Isa.wr(DATA_PORT, value);
		Isa.wr(DATA_PORT+1, value>>8);
	}
	private static int readWord(int port) {
		return Isa.rd(port) + (Isa.rd(port+1)<<8);
	}
	private static int readWordHighFirst(int port) {
		return (Isa.rd(port+1)<<8) + Isa.rd(port);
	}
	private static void writeWord(int port, int value) {
		Isa.wr(port, value);
		Isa.wr(port+1, value>>8);
	}
/**
*	write tx data.
*/
	private static void writeData(int[] buf, int length) {

		if ((length & 1) == 1) ++length;		// even bytes

		for (int i=0; i<length; i+=2) {
			Isa.wr(TX_FRAME_PORT, buf[i]);
			Isa.wr(TX_FRAME_PORT+1, buf[i+1]);
		}
	}
/**
*	read rx data.
*/
	private static void readData(int[] buf, int length) {

		if ((length & 1) == 1) ++length;		// even bytes

		for (int i=0; i<length; i+=2) {
			buf[i] = Isa.rd(RX_FRAME_PORT);
			buf[i+1] = Isa.rd(RX_FRAME_PORT+1);
		}
	}

/**
*	allocate buffer and reset chip.
*/
	public static void init(int[] mac) {

		rxBuf = new int[BUF_LEN];
		rxCnt = 0;
		txFree = true;

		eth = new int[6];
		for (int i=0; i<6; ++i) eth[i] = mac[i];

		tx_packets = 0;
		tx_bytes = 0;
		collisions = 0;

		rx_packets = 0;
		rx_bytes = 0;
		rx_dropped = 0;
		multicast = 0;

		tx_errors = 0;
		tx_aborted_errors = 0;
		tx_carrier_errors = 0;
		tx_fifo_errors = 0;
		tx_heartbeat_errors = 0;
		tx_window_errors = 0;

		rx_errors = 0;
		rx_over_errors = 0;
		rx_length_errors = 0;
		rx_frame_errors = 0;
		rx_crc_errors = 0;
		rx_missed_errors = 0;
		rx_fifo_errors = 0;

		reset();
	}

/**
*	reset chip and set registers
*/
	public static void reset() {

		int i;

		Isa.reset();					// isa reset

		// wait for init
		while((readReg(PP_SelfST) & INIT_DONE) == 0)	// && jiffies - reset_start_time < 2)
			;

		/* get the chip type */
		int rev_type = readReg(PRODUCT_ID_ADD);
		chip_type = rev_type &~ REVISON_BITS;
		chip_revision = ((rev_type & REVISON_BITS) >> 8) + 'A';

		/* Check the chip type and revision in order to set the correct send command
		CS8920 revision C and CS8900 revision F can use the faster send. */
		send_cmd = TX_AFTER_381;
		if (chip_type == CS8900 && chip_revision >= 'F')
			send_cmd = TX_NOW;
		if (chip_type != CS8900 && chip_revision >= 'C')
			send_cmd = TX_NOW;

		// no int, no mem and don't use iochrdy pin
		writeReg(PP_BusCTL, IO_CHANNEL_READY_ON);
		// set the Ethernet address
		for (i=0; i < 6/2; i++) {
			writeReg(PP_IA+i*2, eth[i*2] | (eth[i*2+1] << 8));
		}
		// default value, 10BASE-T only, disabled rx, tx
		// set LOW_RX_SQUELCH for longer cables
		writeReg(PP_LineCTL, 0);
		// TODO check attached cable
		// Turn on both receive and transmit operations
		writeReg(PP_LineCTL, readReg(PP_LineCTL) | SERIAL_RX_ON | SERIAL_TX_ON);
	
		// Receive only error free packets addressed to this card 
		// rx_mode = 0;
		writeReg(PP_RxCTL, DEF_RX_ACCEPT);
	
		curr_rx_cfg = RX_OK_ENBL | RX_CRC_ERROR_ENBL;
	
		writeReg(PP_RxCFG, curr_rx_cfg);
	
		writeReg(PP_TxCFG, TX_LOST_CRS_ENBL | TX_SQE_ERROR_ENBL | TX_OK_ENBL |
			TX_LATE_COL_ENBL | TX_JBR_ENBL | TX_ANY_COL_ENBL | TX_16_COL_ENBL);
	
		writeReg(PP_BufCFG, READY_FOR_TX_ENBL | RX_MISS_COUNT_OVRFLOW_ENBL |
			TX_COL_COUNT_OVRFLOW_ENBL | TX_UNDERRUN_ENBL);
	
		send_underrun = 0;

		// netif_start_queue(dev);
	}

/**
*	Sends a packet to an CS8900 network device.
*	old ret was 0 if ok, now true if ok!
*/
	public static boolean send(int[] buf, int length) {
	
	
		/* keep the upload from being interrupted, since we
	                  ask the chip to start transmitting before the
	                  whole packet has been completely uploaded. */
		// spin_lock_irq(&lp->lock);
		// netif_stop_queue(dev);
	
		/* initiate a transmit sequence */
		// writeWord(TX_CMD_PORT, send_cmd);
		writeWord(TX_CMD_PORT, TX_AFTER_ALL);
		writeWord(TX_LEN_PORT, length);
	
		/* Test to see if the chip has allocated memory for the packet */
		if ((readReg(PP_BusST) & READY_FOR_TX_NOW) == 0) {
			/*
			 * Gasp!  It hasn't.  But that shouldn't happen since
			 * we're waiting for TxOk, so return 1 and requeue this packet.
			 */
			
			// spin_unlock_irq(&lock);
			// if (net_debug) printk("cs89x0: Tx buffer not free!\n");
			return false;
		}
		/* Write the contents of the packet */
		writeData(buf, length);
		txFree = false;

		// spin_unlock_irq(&lock);
		// dev->trans_start = jiffies;
	
		/*
		 * We DO NOT call netif_wake_queue() here.
		 * We also DO NOT call netif_start_queue().
		 *
		 * Either of these would cause another bottom half run through
		 * net_send_packet() before this packet has fully gone out.  That causes
		 * us to hit the "Gasp!" above and the send is rescheduled.  it runs like
		 * a dog.  We just return and wait for the Tx completion interrupt handler
		 * to restart the netdevice layer
		 */
	
		return true;
	}

	private static final int EVENT_MASK = 0xffc0;

	private static int pollRegs() {

		int event;
		
		event = readReg(PP_RxEvent);
		if ((event & EVENT_MASK)!=0) return event;
		event = readReg(PP_TxEvent);
		if ((event & EVENT_MASK)!=0) return event;
		event = readReg(PP_BufEvent);
		if ((event & EVENT_MASK)!=0) return event;

		return 0;
	}

/**
*	The typical workload of the driver:
*   We have to poll the chip (in 8 Bit mode)!
*/
	   
	public static void poll() {

		int status, mask;
	 
		/* we MUST read all the events out of the ISQ, otherwise we'll never
	           get interrupted again.  As a consequence, we can't have any limit
	           on the number of times we loop in the interrupt handler.  The
	           hardware guarantees that eventually we'll run out of events.  Of
	           course, if you're on a slow machine, and packets are arriving
	           faster than you can read them off, you're screwed.  Hasta la
	           vista, baby!  */
		while ((status = pollRegs()) != 0) {

			mask = status & ISQ_EVENT_MASK;

			if (mask == ISQ_RECEIVER_EVENT) {
				/* Got a packet(s). */
				net_rx();

			} else if (mask == ISQ_TRANSMITTER_EVENT) {
				tx_packets++;
				// netif_wake_queue(dev);	/* Inform upper layers. */
				txFree = true;
				if ((status & (	TX_OK |
						TX_LOST_CRS |
						TX_SQE_ERROR |
						TX_LATE_COL |
						TX_16_COL)) != TX_OK) {
					if ((status & TX_OK) == 0) tx_errors++;
					if ((status & TX_LOST_CRS) != 0) tx_carrier_errors++;
					if ((status & TX_SQE_ERROR) != 0) tx_heartbeat_errors++;
					if ((status & TX_LATE_COL) != 0) tx_window_errors++;
					if ((status & TX_16_COL) != 0) tx_aborted_errors++;
				}

			} else if (mask == ISQ_BUFFER_EVENT) {

				if ((status & READY_FOR_TX) != 0) {
					/* we tried to transmit a packet earlier,
	                                   but inexplicably ran out of buffers.
	                                   That shouldn't happen since we only ever
	                                   load one packet.  Shrug.  Do the right
	                                   thing anyway. */
					// netif_wake_queue(dev);	/* Inform upper layers. */
					txFree = true;
				}
				if ((status & TX_UNDERRUN)!=0) {
					send_underrun++;
					if (send_underrun == 3) send_cmd = TX_AFTER_381;
					else if (send_underrun == 6) send_cmd = TX_AFTER_ALL;
					/* transmit cycle is done, although
					   frame wasn't transmitted - this
					   avoids having to wait for the upper
					   layers to timeout on us, in the
					   event of a tx underrun */
					// netif_wake_queue(dev);	/* Inform upper layers. */
					txFree = true;
				}
			}
/* not used
			case ISQ_RX_MISS_EVENT:
				rx_missed_errors += (status >>6);
				break;
			case ISQ_TX_COL_EVENT:
				collisions += (status >>6);
				break;
			}
*/
		}
	}
	
	private static void count_rx_errors(int status) {

		rx_errors++;
		if ((status & RX_RUNT)!=0) rx_length_errors++;
		if ((status & RX_EXTRA_DATA)!=0) rx_length_errors++;
		if ((status & RX_CRC_ERROR)!=0) if ((status & (RX_EXTRA_DATA|RX_RUNT))==0)
			/* per str 172 */
			rx_crc_errors++;
		if ((status & RX_DRIBBLE)!=0) rx_frame_errors++;
	}
	
	/* We have a good packet(s), get it/them out of the buffers. */
	private static void net_rx() {

		int status, length;
	
		// read high byte first: see AN181
		status = readWordHighFirst(RX_FRAME_PORT);
		length = readWordHighFirst(RX_FRAME_PORT);
	
		if ((status & RX_OK) == 0) {
			count_rx_errors(status);
			return;
		}
	
		if (rxCnt != 0) {		// buffer not free!!!
			rx_dropped++;
			return;
		}
	
		readData(rxBuf, length);
		rxCnt += length;		// signal upper layer
	
		// netif_rx(skb);
		// dev->last_rx = jiffies;
		rx_packets++;
		rx_bytes += length;
	}
	
	
//	/* The inverse routine to net_open(). */
//	static int
//	net_close(struct net_device *dev)
//	{
//		struct net_local *lp = (struct net_local *)dev->priv;
//	
//		netif_stop_queue(dev);
//		
//		writeReg(PP_RxCFG, 0);
//		writeReg(PP_TxCFG, 0);
//		writeReg(PP_BufCFG, 0);
//		writeReg(PP_BusCTL, 0);
//	
//		free_irq(dev->irq, dev);
//	
//		/* Update the statistics here. */
//		return 0;
//	}
//	
//	/* Get the current statistics.	This may be called with the card open or
//	   closed. */
//	static struct net_device_stats *
//	net_get_stats(struct net_device *dev)
//	{
//		struct net_local *lp = (struct net_local *)dev->priv;
//		unsigned long flags;
//	
//		/* Update the statistics from the device registers. */
//		rx_missed_errors += (readReg(PP_RxMiss) >> 6);
//		collisions += (readReg(PP_TxCol) >> 6);
//		spin_unlock_irqrestore(&lock, flags);
//	
//		return &stats;
//	}
//	
//	static void set_multicast_list(struct net_device *dev)
//	{
//		struct net_local *lp = (struct net_local *)dev->priv;
//		unsigned long flags;
//	
//		if(dev->flags&IFF_PROMISC)
//		{
//			rx_mode = RX_ALL_ACCEPT;
//		}
//		else if((dev->flags&IFF_ALLMULTI)||dev->mc_list)
//		{
//			/* The multicast-accept list is initialized to accept-all, and we
//			   rely on higher-level filtering for now. */
//			rx_mode = RX_MULTCAST_ACCEPT;
//		} 
//		else
//			rx_mode = 0;
//	
//		writeReg(PP_RxCTL, DEF_RX_ACCEPT | rx_mode);
//	
//		/* in promiscuous mode, we accept errored packets, so we have to enable interrupts on them also */
//		writeReg(PP_RxCFG, curr_rx_cfg |
//		     (rx_mode == RX_ALL_ACCEPT? (RX_CRC_ERROR_ENBL|RX_RUNT_ENBL|RX_EXTRA_DATA_ENBL) : 0));
//		spin_unlock_irqrestore(&lock, flags);
//	}
//	
}
