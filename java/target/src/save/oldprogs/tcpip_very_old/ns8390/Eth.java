/**
*	Eth.java: A general non-shared-memory NS8390 ethernet driver.
*
*	Adapted by Martin Schoeberl (martin.schoeberl@chello.at)
*
*	from by Donald Becker's Linux driver (8390.h, 8390.c and ne.c)
*
*   Copyright 1993 United States Government as represented by the
*   Director, National Security Agency.
*
*   This software may be used and distributed according to the terms
*   of the GNU General Public License, incorporated herein by reference.
*
*   The author may be reached as becker@scyld.com, or C/O
*   Scyld Computing Corporation, 410 Severn Ave., Suite 210, Annapolis MD 21403
*
*   This driver should work with many programmed-I/O 8390-based ethernet
*   boards.
*
*	Martin Schoeberl:
*
*	NS8390x, RTL8019AS are used in 8 Bit mode for simpler connection to an
*	embedded system.
*
*   Changelog:
*		2002-03-15	ARP works!
*
*
*/

public class Eth {

	public static final int IO_ISA_CTRL = 5;
	public static final int IO_ISA_DATA = 6;

	public static final int ISA_RESET = 0x20;
	public static final int ISA_RD = 0x40;
	public static final int ISA_WR = 0x80;
	public static final int ISA_DIR = 0x100;
/*
			isa_a <= din(4 downto 0);
			isa_reset <= din(5);
			isa_nior <= not din(6);
			isa_niow <= not din(7);
			isa_dir <= din(8);
			isa_nc <= '0';
*/

	private static int[] serBuf;			// a generic buffer
	private static final int MAX_SER = 32;
	private static final int BUF_LEN = 6*256;
	private static int[] buf;				// send AND receive buffer
	private static int[] eth;				// own ethernet address
	private static int[] ip;				// own ip address

	private static int tx_start_page;
	private static int rx_start_page;
	private static int stop_page;
	private static int current_page;

	private static int tx1, tx2;
	private static boolean txing;

/* some statistics */
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

/************/

/* Generic NS8390 register definitions. */
/* This file is part of Donald Becker's 8390 drivers, and is distributed
   under the same license. 
   Some of these names and comments originated from the Crynwr
   packet drivers, which are distributed under the GPL. */


//#define ETHER_ADDR_LEN 6

/* The 8390 specific per-packet-header format. */
//struct e8390_pkt_hdr {
//  unsigned char status; /* status */
//  unsigned char next;   /* pointer to next packet. */
//  unsigned short count; /* header + packet length in bytes */
//};


/* Some generic ethernet register configurations. */
	public static final int E8390_TX_IRQ_MASK = 0xa;	/* For register EN0_ISR */
	public static final int E8390_RX_IRQ_MASK = 0x5;
	public static final int E8390_RXCONFIG = 0x4;	/* EN0_RXCR: broadcasts, no multicast,errors */
	public static final int E8390_RXOFF = 0x20;	/* EN0_RXCR: Accept no packets */
	public static final int E8390_TXCONFIG = 0x00;	/* EN0_TXCR: Normal transmit mode */
	public static final int E8390_TXOFF = 0x02;	/* EN0_TXCR: Transmitter off */

/*  Register accessed at EN_CMD, the 8390 base addr.  */
	public static final int E8390_STOP = 0x01;	/* Stop and reset the chip */
	public static final int E8390_START = 0x02;	/* Start the chip, clear reset */
	public static final int E8390_TRANS = 0x04;	/* Transmit a frame */
	public static final int E8390_RREAD = 0x08;	/* Remote read */
	public static final int E8390_RWRITE = 0x10;	/* Remote write  */
	public static final int E8390_SEND = 0x18;	/* 'Send Packet' */
	public static final int E8390_NODMA = 0x20;	/* Remote DMA */
	public static final int E8390_PAGE0 = 0x00;	/* Select page chip registers */
	public static final int E8390_PAGE1 = 0x40;	/* using the two high-order bits */
	public static final int E8390_PAGE2 = 0x80;	/* Page 3 is invalid. */

	public static final int E8390_CMD = 0x00;  /* The command register (for all pages) */
/* Page 0 register offsets. */
	public static final int EN0_CLDALO = 0x01;	/* Low byte of current local dma addr  RD */
	public static final int EN0_STARTPG = 0x01;	/* Starting page of ring bfr WR */
	public static final int EN0_CLDAHI = 0x02;	/* High byte of current local dma addr  RD */
	public static final int EN0_STOPPG = 0x02;	/* Ending page +1 of ring bfr WR */
	public static final int EN0_BOUNDARY = 0x03;	/* Boundary page of ring bfr RD WR */
	public static final int EN0_TSR = 0x04;		/* Transmit status reg RD */
	public static final int EN0_TPSR = 0x04;	/* Transmit starting page WR */
	public static final int EN0_NCR = 0x05;		/* Number of collision reg RD */
	public static final int EN0_TCNTLO = 0x05;	/* Low  byte of tx byte count WR */
	public static final int EN0_FIFO = 0x06;	/* FIFO RD */
	public static final int EN0_TCNTHI = 0x06;	/* High byte of tx byte count WR */
	public static final int EN0_ISR = 0x07;		/* Interrupt status reg RD WR */
	public static final int EN0_CRDALO = 0x08;	/* low byte of current remote dma address RD */
	public static final int EN0_RSARLO = 0x08;	/* Remote start address reg 0 */
	public static final int EN0_CRDAHI = 0x09;	/* high byte, current remote dma address RD */
	public static final int EN0_RSARHI = 0x09;	/* Remote start address reg 1 */
	public static final int EN0_RCNTLO = 0x0a;	/* Remote byte count reg WR */
	public static final int EN0_RCNTHI = 0x0b;	/* Remote byte count reg WR */
	public static final int EN0_RSR = 0x0c;		/* rx status reg RD */
	public static final int EN0_RXCR = 0x0c;	/* RX configuration reg WR */
	public static final int EN0_TXCR = 0x0d;	/* TX configuration reg WR */
	public static final int EN0_COUNTER0 = 0x0d;	/* Rcv alignment error counter RD */
	public static final int EN0_DCFG = 0x0e;	/* Data configuration reg WR */
	public static final int EN0_COUNTER1 = 0x0e;	/* Rcv CRC error counter RD */
	public static final int EN0_IMR = 0x0f;		/* Interrupt mask reg WR */
	public static final int EN0_COUNTER2 = 0x0f;	/* Rcv missed frame error counter RD */

/* Bits in EN0_ISR - Interrupt status register */
	public static final int ENISR_RX = 0x01;	/* Receiver, no error */
	public static final int ENISR_TX = 0x02;	/* Transmitter, no error */
	public static final int ENISR_RX_ERR = 0x04;	/* Receiver, with error */
	public static final int ENISR_TX_ERR = 0x08;	/* Transmitter, with error */
	public static final int ENISR_OVER = 0x10;	/* Receiver overwrote the ring */
	public static final int ENISR_COUNTERS = 0x20;	/* Counters need emptying */
	public static final int ENISR_RDC = 0x40;	/* remote dma complete */
	public static final int ENISR_RESET = 0x80;	/* Reset completed */
	public static final int ENISR_ALL = 0x3f;	/* Interrupts we will enable */

/* Bits in EN0_DCFG - Data config register */
	public static final int ENDCFG_WTS = 0x01;	/* word transfer mode selection */
	public static final int ENDCFG_BOS = 0x02;	/* byte order selection */

/* Page 1 register offsets. */
	public static final int EN1_PHYS = 0x01;	/* This board's physical enet addr RD WR */
//	public static final int EN1_PHYS_SHIFT(i)  (i+1) /* Get and set mac address */
	public static final int EN1_CURPAG = 0x07;	/* Current memory page RD WR */
	public static final int EN1_MULT = 0x08;	/* Multicast filter mask array (8 bytes) RD WR */
//	public static final int EN1_MULT_SHIFT(i)  (8+i) /* Get and set multicast filter */

/* Bits in received packet status byte and EN0_RSR*/
	public static final int ENRSR_RXOK = 0x01;	/* Received a good packet */
	public static final int ENRSR_CRC = 0x02;	/* CRC error */
	public static final int ENRSR_FAE = 0x04;	/* frame alignment error */
	public static final int ENRSR_FO = 0x08;	/* FIFO overrun */
	public static final int ENRSR_MPA = 0x10;	/* missed pkt */
	public static final int ENRSR_PHY = 0x20;	/* physical/multicast address */
	public static final int ENRSR_DIS = 0x40;	/* receiver disable. set in monitor mode */
	public static final int ENRSR_DEF = 0x80;	/* deferring */

/* Transmitted packet status, EN0_TSR. */
	public static final int ENTSR_PTX = 0x01;	/* Packet transmitted without error */
	public static final int ENTSR_ND  = 0x02;	/* The transmit wasn't deferred. */
	public static final int ENTSR_COL = 0x04;	/* The transmit collided at least once. */
	public static final int ENTSR_ABT = 0x08;	/* The transmit collided 16 times, and was deferred. */
	public static final int ENTSR_CRS = 0x10;	/* The carrier sense was lost. */
	public static final int ENTSR_FU  = 0x20;	/* A "FIFO underrun" occurred during transmit. */
	public static final int ENTSR_CDH = 0x40;	/* The collision detect "heartbeat" signal was lost. */
	public static final int ENTSR_OWC = 0x80;	/* There was an out-of-window collision. */

/* The maximum number of 8390 interrupt service routines called per IRQ. */
	//public static final int MAX_SERVICE = 12;
	public static final int MAX_SERVICE = 1;	// only one buffer

/* The maximum time waited before assuming a Tx failed. (20ms) */
	public static final int TX_TIMEOUT = 20;

/************/
/* from ne.c */
	public static final int NE_DATAPORT = 0x10;	/* NatSemi-defined port window offset. */
	public static final int NE_RESET    = 0x1f;	/* Issue a read to reset, a write to clear. */

// really 0x40 ????? or 0x60 !!! --- byte versus word access ???
//	public static final int NE1SM_START_PG = 0x20;	/* First page of TX buffer */
//	public static final int NE1SM_STOP_PG  = 0x40;	/* Last page +1 of RX ring */

	public static final int NE_START_PG = 0x40;	/* First page of TX buffer */
	public static final int NE_STOP_PG  = 0x60;	/* Last page +1 of RX ring */
/* Should always use two Tx slots to get back-to-back transmits. */
	public static final int TX_PAGES = 12;		// two transmit buffers a 6 pages

/* from ../include/linux/if_ether.h */
	public static final int ETH_ALEN = 6;		/* Octets in one ethernet addr	 */
	public static final int ETH_HLEN = 14;		/* Total octets in header.	 */
	public static final int ETH_ZLEN = 60;		/* Min. octets in frame sans FCS */
	public static final int ETH_DATA_LEN = 1500;		/* Max. octets in payload	 */
	public static final int ETH_FRAME_LEN = 1514;		/* Max. octets in frame sans FCS */



	private static int rcv_len;
/**
*	test main.
*/
	public static void main(String[] args) {

		buf = new int[BUF_LEN];
		serBuf = new int[MAX_SER];
		eth = new int[6];
		ip = new int[4];
		ip[0] = 192;
		ip[1] = 168;
		ip[2] = 0;
		ip[3] = 4;
		int i, j;

		rcv_len = 0;

		TcpIp.init();
		Timer.init();			// just for the watch dog

		if (!init()) {
			wrSer('f');
			for (;;) ;
		}

		int old_cur = -1;

		for (;;) {
			// are there unread pages?
			wr(E8390_NODMA+E8390_PAGE1+E8390_START, E8390_CMD);
			i = rd(EN1_CURPAG);
			wr(E8390_NODMA+E8390_PAGE0+E8390_START, E8390_CMD);
			j = rd(EN0_BOUNDARY) + 1;

			if (i!=old_cur) {
				old_cur = i;
				wrSer('c');
				wrSer(' ');
				hexVal(i);
				wrSer('\n');
			}
			if (rd(EN0_ISR) != 0) {		// activ poll
				wrSer('\n');
				wrSer('i');
				wrSer(' ');
				interrupt();
			} else if (i!=j) {			// read more pages
				receive();
			}
			if (rcv_len!=0) {
				process();
			}

			Timer.wd();
		}
	}

	private static void process() {

		int i;
		int len;

		i = (buf[12]<<8) + buf[13];
		if (i == 0x0806) {					// ARP type
			arp();
		} else if (i == 0x0800) {			// IP type
			len = TcpIp.receive(buf, ETH_HLEN, rcv_len-ETH_HLEN);
			if (len!=0) {
				for (i=0; i<6; ++i) {
					buf[0+i] = buf[6+i];	// old src->dest
					buf[6+i] = eth[i];		// src
				}
/*
wrSer('I');
wrSer(' ');
for (i=0; i<ETH_HLEN+len; ++i) hexVal(buf[i]);
wrSer('\n');
*/
				startXmit(buf, ETH_HLEN+len);		// send back changed packet
			}
		}
		// ignore
		rcv_len = 0;
	}

	private static void arp() {

		int i;

		wrSer('A');

		intVal(buf[38]);
		intVal(buf[39]);
		intVal(buf[40]);
		intVal(buf[41]);

		if (buf[38]==ip[0] &&
			buf[39]==ip[1] &&
			buf[40]==ip[2] &&
			buf[41]==ip[3]) {


/*
    Ethernet packet data:
	16.bit: (ar$hrd) Hardware address space (e.g., Ethernet,
			 Packet Radio Net.)
	16.bit: (ar$pro) Protocol address space.  For Ethernet
			 hardware, this is from the set of type
			 fields ether_typ$<protocol>.
	 8.bit: (ar$hln) byte length of each hardware address
	 8.bit: (ar$pln) byte length of each protocol address
	16.bit: (ar$op)  opcode (ares_op$REQUEST | ares_op$REPLY)
	nbytes: (ar$sha) Hardware address of sender of this
			 packet, n from the ar$hln field.
	mbytes: (ar$spa) Protocol address of sender of this
			 packet, m from the ar$pln field.
	nbytes: (ar$tha) Hardware address of target of this
			 packet (if known).
	mbytes: (ar$tpa) Protocol address of target.
*/
			/*
			Swap hardware and protocol fields, putting the local
	    	hardware and protocol addresses in the sender fields.
			*/
			swapAdr();

			/*
			Set the ar$op field to ares_op$REPLY
			*/
			buf[21] = 0x02;		// ARP replay

			/*
			Send the packet to the (new) target hardware address on
	    	the same hardware on which the request was received.
			*/

			wrSer(' ');
			wrSer('r');
			wrSer('\n');

			startXmit(buf, 60);
		}

		wrSer('\n');
	}

	private static void swapAdr() {

		int i;

		for (i=0; i<6; ++i) {
			buf[0+i] = buf[6+i];	// old src->dest
			buf[32+i] = buf[6+i];	// arp dest addr
			buf[6+i] = eth[i];		// src
			buf[22+i] = eth[i];		// arp src addr
		}
		for (i=0; i<4; ++i) {		// set ip fields
			buf[38+i] = buf[28+i];
			buf[28+i] = ip[i];
		}
	}

	private static void ip() {

		wrSer('I');
	}

			

/*********************/

/**
*	'ISA Bus' io write cycle.
*/
	private static void wr(int data, int reg) {

		JopSys.wr(data, IO_ISA_DATA);					// data in buffer

		JopSys.wr(reg, IO_ISA_CTRL);					// addr
		JopSys.wr(reg | ISA_WR, IO_ISA_CTRL);			// iow low
		JopSys.wr(reg | ISA_WR | ISA_DIR, IO_ISA_CTRL);	// drive data out
		JopSys.wr(reg | ISA_DIR, IO_ISA_CTRL);			// iow high again
		JopSys.wr(reg, IO_ISA_CTRL);					// disable dout
	}

/**
*	'ISA Bus' io read cycle.
*/
	private static int rd(int reg) {

		JopSys.wr(reg, IO_ISA_CTRL);					// addr
		JopSys.wr(reg | ISA_RD, IO_ISA_CTRL);			// ior low
		int ret = JopSys.rd(IO_ISA_DATA);				// read data
		JopSys.wr(reg, IO_ISA_CTRL);					// ior high again

		return ret;
	}

/**
*	DMA read from card memory.
*/
	private static void rdMem(int addr, int[] buf, int cnt) {

		int i;

		/* We should already be in page 0, but to be safe... */
		wr(E8390_PAGE0+E8390_START+E8390_NODMA, E8390_CMD);

		wr(cnt & 0xff, EN0_RCNTLO);
		wr(cnt >>> 8,  EN0_RCNTHI);
		wr(addr & 0xff, EN0_RSARLO);	/* DMA start */
		wr(addr >>> 8, EN0_RSARHI);
		wr(E8390_RREAD+E8390_START, E8390_CMD);
	    
		for(i = 0; i<cnt; ++i) {
			buf[i] = rd(NE_DATAPORT);
		}
// is used in block_input and get header, do I need this?
		wr(ENISR_RDC, EN0_ISR);	/* Ack intr. */
	}

/**
*	DMA write to card memory.
*	uses page numbers instead of addr!
*/
	private static void wrMem(int page, int[] buf, int cnt) {

		int i;

		/* We should already be in page 0, but to be safe... */
		wr(E8390_PAGE0+E8390_START+E8390_NODMA, E8390_CMD);

//#ifdef NE8390_RW_BUGFIX
//	/* Handle the read-before-write bug the same way as the
//	   Crynwr packet driver -- the NatSemi method doesn't work.
//	   Actually this doesn't always work either, but if you have
//	   problems with your NEx000 this is better than nothing! */
//
//	wr(0x42, EN0_RCNTLO);
//	wr(0x00,   EN0_RCNTHI);
//	wr(0x42, EN0_RSARLO);
//	wr(0x00, EN0_RSARHI);
//	wr(E8390_RREAD+E8390_START, E8390_CMD);
//	/* Make certain that the dummy read has occurred. */
//	udelay(6);
//#endif
//
//	wr(ENISR_RDC, EN0_ISR); // one more acc ???

	/* Now the normal output. */
		wr(cnt & 0xff, EN0_RCNTLO);
		wr(cnt >>> 8,  EN0_RCNTHI);
		wr(0x00, EN0_RSARLO);	/* DMA start */
		wr(page, EN0_RSARHI);
		wr(E8390_RWRITE+E8390_START, E8390_CMD);
	    
		for(i = 0; i<cnt; ++i) {
			wr(buf[i], NE_DATAPORT);
		}
/******
	dma_start = jiffies;

	while ((rd(EN0_ISR) & ENISR_RDC) == 0)
		if (jiffies - dma_start > 2*HZ/100) {		// 20ms
			printk(KERN_WARNING "%s: timeout waiting for Tx RDC.\n", dev->name);
			ne_reset_8390(dev);
			NS8390_init(dev,1);
			break;
		}
*/

// do I need this???
		wr(ENISR_RDC, EN0_ISR);	/* Ack intr. */
	}

/**
 * txTimeout - handle transmit time out condition
 *
 * Called by kernel when device never acknowledges a transmit has
 * completed (or failed) - i.e. never posted a Tx related interrupt.
 */

	public static void txTimeout() {

		int txsr, isr;
		// int tickssofar = jiffies - dev->trans_start;

		tx_errors++;

		txsr = rd(EN0_TSR);
		isr = rd(EN0_ISR);

/*
		printk(KERN_DEBUG "%s: Tx timed out, %s TSR=%#2x, ISR=%#2x, t=%d.\n",
			dev->name, (txsr & ENTSR_ABT) ? "excess collisions." :
			(isr) ? "lost interrupt?" : "cable problem?", txsr, isr, tickssofar);
*/

//		if (!isr && !tx_packets) {
			/* The 8390 probably hasn't gotten on the cable yet. */
//			interface_num ^= 1;   /* Try a different xcvr.  */
//		}

			
		/* Try to restart the card.  Perhaps the user has fixed something. */
		init();
			
		// netif_wake_queue(dev);
	}
    
/**
* Sends a packet to an 8390 network device.
old ret was 0 if ok, now true if ok!
*/
	public static boolean startXmit(int[] buf, int length) {

		int send_length, output_page;

		wr(0x00, EN0_IMR);
		
		send_length = ETH_ZLEN < length ? length : ETH_ZLEN;
    
		/*
		 * We have two Tx slots available for use. Find the first free
		 * slot, and then perform some sanity checks. With two Tx bufs,
		 * you get very close to transmitting back-to-back packets. With
		 * only one Tx buf, the transmitter sits idle while you reload the
		 * card, leaving a substantial gap between each transmitted packet.
		 */

		if (tx1 == 0) {
			output_page = tx_start_page;
			tx1 = send_length;
		} else if (tx2 == 0) {
			output_page = tx_start_page + TX_PAGES/2;	// second buffer
			tx2 = send_length;
		} else {	/* We should never get here. */
			// No Tx buffers free!
			// netif_stop_queue(dev);
			wr(ENISR_ALL, EN0_IMR);
			tx_errors++;
			return false;
		}

		/*
		 * Okay, now upload the packet and trigger a send if the transmitter
		 * isn't already sending. If it is busy, the interrupt handler will
		 * trigger the send later, upon receiving a Tx done interrupt.
		 */

		wrMem(output_page, buf, length);
		if (!txing) {
			txing = true;
			triggerSend(send_length, output_page);
			// dev->trans_start = jiffies;
			if (output_page == tx_start_page) {
				tx1 = -1;
			} else {
				tx2 = -1;
			}
		}

		/*
		if (tx1!=0  &&  tx2!=0)
			netif_stop_queue(dev);
		else
			netif_start_queue(dev);
		*/

		/* Turn 8390 interrupts back on. */
		wr(ENISR_ALL, EN0_IMR);
		
		tx_bytes += send_length;
    
		return true;
}

/**
* Handle the ether interface interrupts. We pull packets from
* the 8390 via the card specific functions and fire them at the networking
* stack. We also handle transmit completions and wake the transmit path if
* neccessary. We also update the counters and do other housekeeping as
* needed.
*/

// when will this be called on my polling version!!!

	public static void interrupt() {

		int interrupts, nr_serviced;
    
		/* Change to page 0 and read the intr status reg. */
		wr(E8390_NODMA+E8390_PAGE0, E8390_CMD);	// no START means STOP ?
    
		/* !!Assumption!! -- we stay in page 0.	 Don't break this. */
		for (nr_serviced=0; (interrupts = rd(EN0_ISR)) != 0 && nr_serviced<MAX_SERVICE; ++nr_serviced) {

hexVal(interrupts);

			/* Push the next to-transmit packet through. */
			if ((interrupts & ENISR_TX)!=0) {
				txIntr();
			} else if ((interrupts & ENISR_TX_ERR)!=0) {
				txErr();
			}
			if ((interrupts & ENISR_OVER)!=0) {
				rxOverrun();
			} else if ((interrupts & (ENISR_RX+ENISR_RX_ERR))!=0) {
				/* Got a good (?) packet. */
				receive();
			}

			if ((interrupts & ENISR_COUNTERS)!=0) {
				rx_frame_errors += rd(EN0_COUNTER0);
				rx_crc_errors   += rd(EN0_COUNTER1);
				rx_missed_errors+= rd(EN0_COUNTER2);
				wr(ENISR_COUNTERS, EN0_ISR); /* Ack intr. */
			}
			
			/* Ignore any RDC interrupts that make it back to here. */
			if ((interrupts & ENISR_RDC)!=0) 
			{
				wr(ENISR_RDC, EN0_ISR);
			}

			wr(E8390_NODMA+E8390_PAGE0+E8390_START, E8390_CMD);
		}
    
//		if (interrupts && ei_debug) 
//		{
//			wr(E8390_NODMA+E8390_PAGE0+E8390_START, E8390_CMD);
//			if (nr_serviced >= MAX_SERVICE) 
//			{
//				/* 0xFF is valid for a card removal */
//				if(interrupts!=0xFF)
//					printk(KERN_WARNING "%s: Too much work at interrupt, status %#2.2x\n",
//					   dev->name, interrupts);
//				wr(ENISR_ALL, EN0_ISR); /* Ack. most intrs. */
//			} else {
//				printk(KERN_WARNING "%s: unknown interrupt %#2x\n", dev->name, interrupts);
//				wr(0xff, EN0_ISR); /* Ack. all intrs. */
//			}
//		}
	}

/**
* txErr - handle transmitter error
*
* A transmitter error has happened. Most likely excess collisions (which
* is a fairly normal condition). If the error is one where the Tx will
* have been aborted, we try and send another one right away, instead of
* letting the failed packet sit and collect dust in the Tx buffer. This
* is a much better solution as it avoids kernel based Tx timeouts, and
* an unnecessary card reset.
*
*/

	private static void txErr() {

		int txsr = rd(EN0_TSR);
		boolean tx_was_aborted = (txsr & (ENTSR_ABT+ENTSR_FU))!=0;

/*
#ifdef VERBOSE_ERROR_DUMP
		printk(KERN_DEBUG "%s: transmitter error (%#2x): ", dev->name, txsr);
		if (txsr & ENTSR_ABT)
			printk("excess-collisions ");
		if (txsr & ENTSR_ND)
			printk("non-deferral ");
		if (txsr & ENTSR_CRS)
			printk("lost-carrier ");
		if (txsr & ENTSR_FU)
			printk("FIFO-underrun ");
		if (txsr & ENTSR_CDH)
			printk("lost-heartbeat ");
		printk("\n");
#endif
*/

		wr(ENISR_TX_ERR, EN0_ISR); /* Ack intr. */

		if (tx_was_aborted) {
			txIntr();
		} else {
			tx_errors++;
			if ((txsr & ENTSR_CRS)!=0) tx_carrier_errors++;
			if ((txsr & ENTSR_CDH)!=0) tx_heartbeat_errors++;
			if ((txsr & ENTSR_OWC)!=0) tx_window_errors++;
		}
	}

/**
* txIntr - transmit interrupt handler
*
* We have finished a transmit: check for errors and then trigger the next
* packet to be sent.
*/

	private static void txIntr() {

		int status = rd(EN0_TSR);
    
		wr(ENISR_TX, EN0_ISR); /* Ack intr. */

		/*
		 * There are two Tx buffers, see which one finished, and trigger
		 * the send of another one if it exists.
		 */

		if (tx1 < 0) {
			tx1 = 0;
			if (tx2 > 0) {
				txing = true;
				triggerSend(tx2, tx_start_page + TX_PAGES/2);	// second buffer
				// dev->trans_start = jiffies;
				tx2 = -1;
			} else {
				txing = false;	
			}
		} else if (tx2 < 0) {
			tx2 = 0;
			if (tx1 > 0) {
				txing = true;
				triggerSend(tx1, tx_start_page);
				// dev->trans_start = jiffies;
				tx1 = -1;
			} else {
				txing = false;
			}
		}

		/* Minimize Tx latency: update the statistics after we restart TXing. */
		if ((status & ENTSR_COL)!=0) collisions++;
		if ((status & ENTSR_PTX)!=0) {
			tx_packets++;
		} else {
			tx_errors++;
			if ((status & ENTSR_ABT)!=0) 
			{
				tx_aborted_errors++;
				collisions += 16;
			}
			if ((status & ENTSR_CRS)!=0) tx_carrier_errors++;
			if ((status & ENTSR_FU)!=0) tx_fifo_errors++;
			if ((status & ENTSR_CDH)!=0) tx_heartbeat_errors++;
			if ((status & ENTSR_OWC)!=0) tx_window_errors++;
		}
		// netif_wake_queue(dev);
	}

/**
 * We have a good packet(s), get it/them out of the buffers. 
 * Called with lock held.
 */

	private static void receive() {

		int rxing_page, this_frame, next_frame;
		int current_offset;
		int rx_pkt_count = 0;

		// int num_rx_pages = stop_page-rx_start_page;
    
wrSer('r');
		//while (++rx_pkt_count < 10) {
		{	// only one packet for now!!!

			int pkt_len, pkt_stat;
			
			/* Get the rx page (incoming packet pointer). */
			wr(E8390_NODMA+E8390_PAGE1, E8390_CMD);
			rxing_page = rd(EN1_CURPAG);
			wr(E8390_NODMA+E8390_PAGE0, E8390_CMD);
			
			/* Remove one frame from the ring.  Boundary is always a page behind. */
			this_frame = rd(EN0_BOUNDARY) + 1;
			if (this_frame >= stop_page)
				this_frame = rx_start_page;
			
			/* Someday we'll omit the previous, iff we never get this message.
			   (There is at least one clone claimed to have a problem.)  
			   
			   Keep quiet if it looks like a card removal. One problem here
			   is that some clones crash in roughly the same way.
			 */
			/*
			if (ei_debug > 0  &&  this_frame != current_page && (this_frame!=0x0 || rxing_page!=0xFF))
				printk(KERN_ERR "%s: mismatched read page pointers %2x vs %2x.\n",
					   dev->name, this_frame, current_page);
			*/
			
// no while loop!
			// if (this_frame == rxing_page)	/* Read all the frames? */
			//	break;				/* Done for now */
			
			current_offset = this_frame << 8;

			rdMem(current_offset, buf, 4);
			pkt_stat = buf[0];
			int rx_frame_next = buf[1];
			pkt_len = (buf[3]<<8) + buf[2] - 4;
// intVal(pkt_len);
			
			next_frame = this_frame + 1 + ((pkt_len+4)>>8);
			
			/* Check for bogosity warned by 3c503 book: the status byte is never
			   written.  This happened a lot during testing! This code should be
			   cleaned up someday. */
/* strange!!!
			if (rx_frame_next != next_frame
				&& rx_frame_next != next_frame + 1
				&& rx_frame_next != next_frame - num_rx_pages
				&& rx_frame_next != next_frame + 1 - num_rx_pages) {
				current_page = rxing_page;
				wr(current_page-1, EN0_BOUNDARY);
				rx_errors++;
				continue;
			}
*/

/* I don't care for now
			if (pkt_len < 60  ||  pkt_len > 1518) {
				if (ei_debug)
					printk(KERN_DEBUG "%s: bogus packet size: %d, status=%#2x nxpg=%#2x.\n",
						   dev->name, rx_frame_count, rx_frame_status,
						   rx_frame_next);
				rx_errors++;
				rx_length_errors++;

			} else if ((pkt_stat & 0x0F) == ENRSR_RXOK) {
*/
			if ((pkt_stat & 0x0F) == ENRSR_RXOK) {

				/* no buffer available -> drop
				skb = dev_alloc_skb(pkt_len+2);
				if (skb == NULL) 
				{
					if (ei_debug > 1)
						printk(KERN_DEBUG "%s: Couldn't allocate a sk_buff of size %d.\n",
							   dev->name, pkt_len);
					rx_dropped++;
					break;
				}
				else
				*/
				{
					// whou cares about ring buffer bounderies!!!!
					rdMem(current_offset + 4 /* sizeof(rx_frame)*/, buf, pkt_len);

					// rcv_len is the 'signal' for the loop
					rcv_len = pkt_len;
/*
wrSer('\n');
wrSer('p');
wrSer(' ');
for (int i=0; i<pkt_len; ++i) hexVal(buf[i]);
wrSer('\n');
*/
					// skb->protocol=eth_type_trans(skb,dev);
					// netif_rx(skb);
					// dev->last_rx = jiffies;
					/*
					rx_packets++;
					rx_bytes += pkt_len;
					if ((pkt_stat & ENRSR_PHY)!=0)
						multicast++;
					*/
				}
			} else {
				/*
				if (ei_debug)
					printk(KERN_DEBUG "%s: bogus packet: status=%#2x nxpg=%#2x size=%d\n",
						   dev->name, rx_frame_status, rx_frame_next,
						   rx_frame_count);
				*/
				rx_errors++;
				/* NB: The NIC counts CRC, frame and missed errors. */
				/*
				if ((pkt_stat & ENRSR_FO)!=0)
					rx_fifo_errors++;
				*/
			}
			next_frame = rx_frame_next;
			
			/* This _should_ never happen: it's here for avoiding bad clones. */
			// if the code would handle the ring buffer correct this CAN happen
			if (next_frame >= stop_page) {
				/*
				printk("%s: next frame inconsistency, %#2x\n", dev->name,
					   next_frame);
				*/
				next_frame = rx_start_page;
			}
			current_page = next_frame;
			wr(next_frame-1, EN0_BOUNDARY);
		}

		/* We used to also ack ENISR_OVER here, but that would sometimes mask
		   a real overrun, leaving the 8390 in a stopped state with rec'vr off. */
		wr(ENISR_RX+ENISR_RX_ERR, EN0_ISR);
	}

/**
 * rxOverrun - handle receiver overrun
 *
 * We have a receiver overrun: we have to kick the 8390 to get it started
 * again. Problem is that you have to kick it exactly as NS prescribes in
 * the updated datasheets, or "the NIC may act in an unpredictable manner."
 * This includes causing "the NIC to defer indefinitely when it is stopped
 * on a busy network."  Ugh.
 * Called with lock held. Don't call this with the interrupts off or your
 * computer will hate you - it takes 10ms or so. 
 */

	private static void rxOverrun() {

		boolean was_txing, must_resend = false;
    
		/*
		 * Record whether a Tx was in progress and then issue the
		 * stop command.
		 */
		was_txing = (rd(E8390_CMD) & E8390_TRANS) != 0;
		wr(E8390_NODMA+E8390_PAGE0+E8390_STOP, E8390_CMD);
    
		/*
		if (ei_debug > 1)
			printk(KERN_DEBUG "%s: Receiver overrun.\n", dev->name);
		*/
		rx_over_errors++;
    
		/* 
		 * Wait a full Tx time (1.2ms) + some guard time, NS says 1.6ms total.
		 * Early datasheets said to poll the reset bit, but now they say that
		 * it "is not a reliable indicator and subsequently should be ignored."
		 * We wait at least 10ms.
		 */

		Timer.sleep(10);

		/*
		 * Reset RBCR[01] back to zero as per magic incantation.
		 */
		wr(0x00, EN0_RCNTLO);
		wr(0x00, EN0_RCNTHI);

		/*
		 * See if any Tx was interrupted or not. According to NS, this
		 * step is vital, and skipping it will cause no end of havoc.
		 */

		if (was_txing) { 

			boolean tx_completed = (rd(EN0_ISR) & (ENISR_TX+ENISR_TX_ERR)) != 0;
			if (!tx_completed)
				must_resend = true;
		}

		/*
		 * Have to enter loopback mode and then restart the NIC before
		 * you are allowed to slurp packets up off the ring.
		 */
		wr(E8390_TXOFF, EN0_TXCR);
		wr(E8390_NODMA + E8390_PAGE0 + E8390_START, E8390_CMD);

		/*
		 * Clear the Rx ring of all the debris, and ack the interrupt.
		 */
		receive();
		wr(ENISR_OVER, EN0_ISR);

		/*
		 * Leave loopback mode, and resend any packet that got stopped.
		 */
		wr(E8390_TXCONFIG, EN0_TXCR); 
		if (must_resend)
    		wr(E8390_NODMA + E8390_PAGE0 + E8390_START + E8390_TRANS, E8390_CMD);
}

/*
*	Collect the stats.
*/
 
	public static void getStats() {

		/* Read the counter registers, assuming we are in page 0. */
		rx_frame_errors += rd(EN0_COUNTER0);
		rx_crc_errors   += rd(EN0_COUNTER1);
		rx_missed_errors+= rd(EN0_COUNTER2);
    
		// return &stat;
	}

/*
 * Update the given Autodin II CRC value with another data byte.
 */

	private static int update_crc(int by, int current_crc) {

		int bit;
		int ah = 0;
		for (bit=0; bit<8; bit++) 
		{
			int carry = (current_crc>>>31);
			current_crc <<= 1;
			ah = ((ah<<1) | carry) ^ by;
			ah &= 0xff;
			if ((ah&1) != 0)
				current_crc ^= 0x04C11DB7;	/* CRC polynomial */
			ah >>>= 1;
			by >>>= 1;
		}
		return current_crc;
	}

/*
 * Form the 64 bit 8390 multicast table from the linked list of addresses
 * associated with this dev structure.
 */
 
//static inline void make_mc_bits(u8 *bits, struct net_device *dev)
//{
//		struct dev_mc_list *dmi;
//
//		for (dmi=dev->mc_list; dmi; dmi=dmi->next) 
//		{
//			int i;
//			u32 crc;
//			if (dmi->dmi_addrlen != ETH_ALEN) 
//			{
//				printk(KERN_INFO "%s: invalid multicast address length given.\n", dev->name);
//				continue;
//			}
//			crc = 0xffffffff;	/* initial CRC value */
//			for (i=0; i<ETH_ALEN; i++)
//				crc = update_crc(dmi->dmi_addr[i], crc);
//			/* 
//			 * The 8390 uses the 6 most significant bits of the
//			 * CRC to index the multicast table.
//			 */
//			bits[crc>>29] |= (1<<((crc>>26)&7));
//		}
//}
//
///**
// * do_set_multicast_list - set/clear multicast filter
// * @dev: net device for which multicast filter is adjusted
// *
// *	Set or clear the multicast filter for this adaptor. May be called
// *	from a BH in 2.1.x. Must be called with lock held. 
// */
// 
//static void do_set_multicast_list(struct net_device *dev)
//{
//		int i;
//		struct ei_device *ei_local = (struct ei_device*)dev->priv;
//
//		if (!(dev->flags&(IFF_PROMISC|IFF_ALLMULTI))) 
//		{
//			memset(mcfilter, 0, 8);
//			if (dev->mc_list)
//				make_mc_bits(mcfilter, dev);
//		}
//		else
//			memset(mcfilter, 0xFF, 8);	/* mcast set to accept-all */
//
//		/* 
//		 * DP8390 manuals don't specify any magic sequence for altering
//		 * the multicast regs on an already running card. To be safe, we
//		 * ensure multicast mode is off prior to loading up the new hash
//		 * table. If this proves to be not enough, we can always resort
//		 * to stopping the NIC, loading the table and then restarting.
//		 *
//		 * Bug Alert!  The MC regs on the SMC 83C690 (SMC Elite and SMC 
//		 * Elite16) appear to be write-only. The NS 8390 data sheet lists
//		 * them as r/w so this is a bug.  The SMC 83C790 (SMC Ultra and
//		 * Ultra32 EISA) appears to have this bug fixed.
//		 */
//		 
//		if (netif_running(dev))
//			wr(E8390_RXCONFIG, EN0_RXCR);
//		wr(E8390_NODMA + E8390_PAGE1, E8390_CMD);
//		for(i = 0; i < 8; i++) 
//		{
//			wr(mcfilter[i], EN1_MULT_SHIFT(i));
//#ifndef BUG_83C690
//			if(rd(EN1_MULT_SHIFT(i))!=mcfilter[i])
//				printk(KERN_ERR "Multicast filter read/write mismap %d\n",i);
//#endif
//		}
//		wr(E8390_NODMA + E8390_PAGE0, E8390_CMD);
//
//  	if(dev->flags&IFF_PROMISC)
//  		wr(E8390_RXCONFIG | 0x18, EN0_RXCR);
//		else if(dev->flags&IFF_ALLMULTI || dev->mc_list)
//  		wr(E8390_RXCONFIG | 0x08, EN0_RXCR);
//  	else
//  		wr(E8390_RXCONFIG, EN0_RXCR);
// }
//
///*
// *	Called without lock held. This is invoked from user context and may
// *	be parallel to just about everything else. Its also fairly quick and
// *	not called too often. Must protect against both bh and irq users
// */
// 
//static void set_multicast_list(struct net_device *dev)
//{
//		struct ei_device *ei_local = (struct ei_device*)dev->priv;
//		
//		do_set_multicast_list(dev);
//}	

/**
* Trigger a transmit start, assuming the length is valid. 
*/
   
	private static void triggerSend(int length, int start_page) {

		wr(E8390_NODMA+E8390_PAGE0, E8390_CMD);
    
		if ((rd(E8390_CMD) & E8390_TRANS)!=0) {
			// "trigger_send() called with the transmitter busy.\n",
			return;
		}
		wr(length & 0xff, EN0_TCNTLO);
		wr(length >> 8, EN0_TCNTHI);
		wr(start_page, EN0_TPSR);
		wr(E8390_NODMA+E8390_TRANS+E8390_START, E8390_CMD);
wrSer('x');
// wrSer(' ');
// intVal(length);
wrSer('\n');
	}

/**
*	initialize 8390 network card.
*/
	public static boolean init() {

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

		boolean ret = init0();
		init1();
		return ret;
	}

/**
*	initialize first step.
*/
	private static boolean init0() {

		int i;

		JopSys.wr(0x20, IO_ISA_CTRL);	// isa bus reset
		Timer.sleep(5);
		JopSys.wr(0x00, IO_ISA_CTRL);	// disable reset
		Timer.sleep(5);

		i = rd(0);
		if (i!=0x21) return false;		// should be 0x21 after a 'real' reset

		tx_start_page = NE_START_PG;
		rx_start_page = NE_START_PG+TX_PAGES;
		stop_page = NE_STOP_PG;

		/* Follow National Semi's recommendations for initing the DP83902. */
		wr(E8390_NODMA+E8390_PAGE0+E8390_STOP, E8390_CMD); /* 0x21 */
		wr(0x48, EN0_DCFG);		/* 0x48 or 0x49, byte mode */
		/* Clear the remote byte count registers. */
		wr(0x00,  EN0_RCNTLO);
		wr(0x00,  EN0_RCNTHI);
		/* Set to monitor and loopback mode -- this is vital!. */
		wr(E8390_RXOFF, EN0_RXCR); /* 0x20 */
		wr(E8390_TXOFF, EN0_TXCR); /* 0x02 */
		/* Set the transmit page and receive ring. */
		wr(tx_start_page, EN0_TPSR);

		wr(rx_start_page, EN0_STARTPG);
		wr(stop_page-1, EN0_BOUNDARY);	/* 3c503 says 0x3f,NS0x26*/
		current_page = rx_start_page;
		wr(stop_page, EN0_STOPPG);

		/* Clear the pending interrupts and mask. */
		wr(0xFF, EN0_ISR);
		wr(0x00,  EN0_IMR);

		/* Read the 16 bytes of station address PROM (after initialized registers!). */
		rdMem(0, buf, 32);		/* DMA starting at 0x0000 */
		if (buf[0x1e]!=0x42) return false;	// should be in 8-Bit mode

		/* Clear the remote byte count registers. (again ?) */
		wr(0x00,  EN0_RCNTLO);
		wr(0x00,  EN0_RCNTHI);
	
		/* Copy the station address into the DS8390 registers. */
		wr(E8390_NODMA + E8390_PAGE1 + E8390_STOP, E8390_CMD); /* 0x61 */
		for(i=0; i<6; i++) {
			wr(buf[i<<1], EN1_PHYS+i);		// i*2 becaus of 8-Bit mode
			eth[i] = buf[i<<1];
hexVal(buf[i<<1]);
		}
wrSer('\n');
	
		wr(rx_start_page, EN1_CURPAG);
		wr(E8390_NODMA+E8390_PAGE0+E8390_STOP, E8390_CMD);

		return true;
	}

/**
*	initiate chip processing.
*/
	private static void init1() {
	
		// netif_start_queue(dev);
		tx1 = 0;
		tx2 = 0;
		txing = false;
	
		wr(0xff,  EN0_ISR);
		wr(ENISR_ALL,  EN0_IMR);		// I don't like ints, or what?
		wr(E8390_NODMA+E8390_PAGE0+E8390_START, E8390_CMD);
		wr(E8390_TXCONFIG, EN0_TXCR); /* xmit on. */
		/* 3c503 TechMan says rxconfig only after the NIC is started. */
		wr(E8390_RXCONFIG, EN0_RXCR); /* rx on,  */
// TODO
//		do_set_multicast_list(dev);	/* (re)load the mcast table */
	}




/*
	serial output for debug
*/
	private static final int IO_STATUS = 1;
	private static final int IO_UART = 2;

	static void wrSer(int c) {
		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr(c, IO_UART);
	}

	static void intVal(int val) {

		int i;
		for (i=0; i<MAX_SER-1; ++i) {
			serBuf[i] = val%10;
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wrSer('0'+serBuf[val]);
		}
		wrSer(' ');
	}

	static void hexVal(int val) {

		int i, j;
		if (val<16) wrSer('0');
		for (i=0; i<MAX_SER-1; ++i) {
			j = val & 0x0f;
			if (j<10) {
				j += '0';
			} else {
				j += 'a'-10;
			}
			serBuf[i] = j;
			val >>>= 4;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wrSer(serBuf[val]);
		}
		wrSer(' ');
	}
}
