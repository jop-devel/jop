
//**************** ne.c


//**************** 8390.c

/**
 * txTimeout - handle transmit time out condition
 * @dev: network device which has apparently fallen asleep
 *
 * Called by kernel when device never acknowledges a transmit has
 * completed (or failed) - i.e. never posted a Tx related interrupt.
 */

	public static void txTimeout() {

		int txsr, isr
		// int tickssofar = jiffies - dev->trans_start;

		tx_errors++;

		txsr = rd(EN0_TSR);
		isr = rd(EN0_ISR);

/*
		printk(KERN_DEBUG "%s: Tx timed out, %s TSR=%#2x, ISR=%#2x, t=%d.\n",
			dev->name, (txsr & ENTSR_ABT) ? "excess collisions." :
			(isr) ? "lost interrupt?" : "cable problem?", txsr, isr, tickssofar);
*/

		if (!isr && !tx_packets) {
			/* The 8390 probably hasn't gotten on the cable yet. */
//			interface_num ^= 1;   /* Try a different xcvr.  */
		}

			
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

		// wr(0x00, EN0_IMR);
		
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
			// wr(ENISR_ALL, EN0_IMR);
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
				lasttx = -1;
			} else {
				tx2 = -1;
				lasttx = -2;
			}
		}

		/*
		if (tx1!=0  &&  tx2!=0)
			netif_stop_queue(dev);
		else
			netif_start_queue(dev);
		*/

		/* Turn 8390 interrupts back on. */
		// wr(ENISR_ALL, EN0_IMR);
		
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

		int interrupts, nr_serviced = 0;
    
		/* Change to page 0 and read the intr status reg. */
		wr(E8390_NODMA+E8390_PAGE0, E8390_CMD);	// no START means STOP ?
    
		/* !!Assumption!! -- we stay in page 0.	 Don't break this. */
		while ((interrupts = rd(EN0_ISR)) != 0 && ++nr_serviced < MAX_SERVICE) {

			if (interrupts & ENISR_OVER) {
				rxOverrun();
			} else if (interrupts & (ENISR_RX+ENISR_RX_ERR)) {
				/* Got a good (?) packet. */
				receive();
			}
			/* Push the next to-transmit packet through. */
			if (interrupts & ENISR_TX) {
				txIntr();
			} else if (interrupts & ENISR_TX_ERR) {
				txErr();
			}

			if (interrupts & ENISR_COUNTERS) {
				rx_frame_errors += rd(EN0_COUNTER0);
				rx_crc_errors   += rd(EN0_COUNTER1);
				rx_missed_errors+= rd(EN0_COUNTER2);
				wr(ENISR_COUNTERS, EN0_ISR); /* Ack intr. */
			}
			
			/* Ignore any RDC interrupts that make it back to here. */
			if (interrupts & ENISR_RDC) 
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
		int tx_was_aborted = txsr & (ENTSR_ABT+ENTSR_FU);

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
			if (txsr & ENTSR_CRS) tx_carrier_errors++;
			if (txsr & ENTSR_CDH) tx_heartbeat_errors++;
			if (txsr & ENTSR_OWC) tx_window_errors++;
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
/*
			if (lasttx != 1 && lasttx != -1)
				printk(KERN_ERR "%s: bogus last_tx_buffer %d, tx1=%d.\n",
					name, lasttx, tx1);
*/
			tx1 = 0;
			if (tx2 > 0) {
				txing = true;
				triggerSend(tx2, tx_start_page + TX_PAGES/2);	// second buffer
				// dev->trans_start = jiffies;
				tx2 = -1,
				lasttx = 2;
			} else {
				lasttx = 20;
				txing = 0;	
			}
		} else if (tx2 < 0) {
/*
			if (lasttx != 2  &&  lasttx != -2)
				printk("%s: bogus last_tx_buffer %d, tx2=%d.\n",
					name, lasttx, tx2);
*/
			tx2 = 0;
			if (tx1 > 0) {
				txing = 1;
				triggerSend(tx1, tx_start_page);
				// dev->trans_start = jiffies;
				tx1 = -1;
				lasttx = 1;
			} else {
				lasttx = 10;
				txing = 0;
			}
		}
//	else printk(KERN_WARNING "%s: unexpected TX-done interrupt, lasttx=%d.\n",
//			dev->name, lasttx);

		/* Minimize Tx latency: update the statistics after we restart TXing. */
		if (status & ENTSR_COL) collisions++;
		if (status & ENTSR_PTX) {
			tx_packets++;
		} else {
			tx_errors++;
			if (status & ENTSR_ABT) 
			{
				tx_aborted_errors++;
				collisions += 16;
			}
			if (status & ENTSR_CRS) tx_carrier_errors++;
			if (status & ENTSR_FU) tx_fifo_errors++;
			if (status & ENTSR_CDH) tx_heartbeat_errors++;
			if (status & ENTSR_OWC) tx_window_errors++;
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

		int num_rx_pages = stop_page-rx_start_page;
    
		while (++rx_pkt_count < 10) {

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
			
			if (this_frame == rxing_page)	/* Read all the frames? */
				break;				/* Done for now */
			
			current_offset = this_frame << 8;

			rdMem(current_offset, buf, 4);
			int rx_frame_status = buf[0];
			int rx_frame_next = buf[1];
			int rx_frame_count = (buf[4]<<8) + buf[3];
			
			pkt_len = rx_frame_count - 4;
			pkt_stat = rx_frame_status;
			
			next_frame = this_frame + 1 + ((pkt_len+4)>>8);
			
			/* Check for bogosity warned by 3c503 book: the status byte is never
			   written.  This happened a lot during testing! This code should be
			   cleaned up someday. */
			if (rx_frame_next != next_frame
				&& rx_frame_next != next_frame + 1
				&& rx_frame_next != next_frame - num_rx_pages
				&& rx_frame_next != next_frame + 1 - num_rx_pages) {
				current_page = rxing_page;
				wr(current_page-1, EN0_BOUNDARY);
				rx_errors++;
				continue;
			}

			if (pkt_len < 60  ||  pkt_len > 1518) {
				/*
				if (ei_debug)
					printk(KERN_DEBUG "%s: bogus packet size: %d, status=%#2x nxpg=%#2x.\n",
						   dev->name, rx_frame_count, rx_frame_status,
						   rx_frame_next);
				*/
				rx_errors++;
				rx_length_errors++;

			} else if ((pkt_stat & 0x0F) == ENRSR_RXOK) {

				struct sk_buff *skb;
				
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
for (int i=0; i<pkt_len; ++i) intVal(buf[i]);
					// skb->protocol=eth_type_trans(skb,dev);
					// netif_rx(skb);
					// dev->last_rx = jiffies;
					rx_packets++;
					rx_bytes += pkt_len;
					if (pkt_stat & ENRSR_PHY)
						multicast++;
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
				if (pkt_stat & ENRSR_FO)
					rx_fifo_errors++;
			}
			next_frame = rx_frame_next;
			
			/* This _should_ never happen: it's here for avoiding bad clones. */
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
			byte >>>= 1;
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
    
		if (rd(E8390_CMD) & E8390_TRANS) {
			// "trigger_send() called with the transmitter busy.\n",
			return;
		}
		wr(length & 0xff, EN0_TCNTLO);
		wr(length >> 8, EN0_TCNTHI);
		wr(start_page, EN0_TPSR);
		wr(E8390_NODMA+E8390_TRANS+E8390_START, E8390_CMD);
	}
