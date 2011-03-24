/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) Martin Schoeberl   <martin@jopdesign.com>
                Thomas B. Preusser <thomas.preusser@tu-dresden.de>

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

package csp.stream;

import csp.NoC;
import csp.PrivateScope;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.engines.AESLightEngine;

import com.jopdesign.sys.Native;

import java.util.Random;

import joprt.RtThread;

/**
 * Streaming benchmark with four processing stages:
 * 
 * 1. Data Generation 2. Encryption 3. Decryption 4. Verification
 * 
 * This benchmark can, by its nature, not scale beyond 4 independent Threads.
 * The measured performance will typically scale even less due to
 * platform-dependent imbalances of the computational complexities of the
 * individual stages. Moreso, the serialized version running within a single
 * Thread may even perform best on platforms with significant cache memory due
 * to the improved locality. Thus, this benchmark also evaluates the performance
 * of the inter-Thread communication.
 * 
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 * @author Martin Schoeberl <martin@jopdesign.com>
 */
public class AESCsp {
	final static int BLOCK_SIZE = 128; // This MUST be a multiple of 16.
	final static int POOL_LENGTH = 8;

	final BufferQueue q1, q2, q3, free;
	final Source source;
	final Encrypt enc;
	final Decrypt dec;
	final Sink sink;
	final Runnable[] runners;

	private int blockCnt;
	static volatile boolean finished;

	public AESCsp() {

		// Pool of Data Blocks
		q1 = new BufferQueue(POOL_LENGTH);
		q2 = new BufferQueue(POOL_LENGTH);
		q3 = new BufferQueue(POOL_LENGTH);
		free = new BufferQueue(POOL_LENGTH);
		for (int i = 0; i < POOL_LENGTH; ++i) {
			final byte[] block = new byte[BLOCK_SIZE];
			free.checkedEnq(block);
		}

		final CipherParameters params;
		{ // Encryption Parameters
			final byte[] key = new byte[16];
			final Random rnd = new Random(127);
			for (int i = key.length; i > 0; key[--i] = (byte) rnd.nextInt())
				;
			params = new KeyParameter(key);
		}

		source = new Source();
		enc = new Encrypt(params);
		dec = new Decrypt(params);
		sink = new Sink();
		runners = new Runnable[] { source, enc, dec, sink };
	}

	public String toString() {
		return "AESSPM";
	}

	protected Runnable[] getWorkers() {
		return runners;
	}

	protected int getDepth() {
		return 4;
	}

	protected void reset(int cnt) {
		finished = false;
		blockCnt = cnt;
	}

	protected boolean isFinished() {
		return finished;
	}

	private class Source implements Runnable {

		public void run() {
			PrivateScope scope = new PrivateScope(1000);
			Runnable r = new Runnable() {
				public void run() {
					Random rnd = new Random();
					rnd.setSeed(127);
					int cnt = 0;
					byte[] block = new byte[BLOCK_SIZE];
					while (cnt < blockCnt) {
						for (int i = 0; i < block.length; i++) {
							block[i] = (byte) rnd.nextInt();
						}
						++cnt;
						while (NoC.isSending()) {
							// wait
						}
						Native.wr(2, NoC.NOC_REG_SNDDST);
						// there is no send function in NoC :-(
						Native.wr(BLOCK_SIZE, NoC.NOC_REG_SNDCNT);
						for (int i = 0; i < BLOCK_SIZE; ++i) {
							Native.wr(i, NoC.NOC_REG_SNDDATA);
						}
//						while ((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_SND) != 0) {
//							// nop
//						}
//						Native.wr(2, NoC.NOC_REG_SNDDST);
//						Native.wr(BLOCK_SIZE, NoC.NOC_REG_SNDCNT);
//
//						for (int i = 0; i < BLOCK_SIZE; ++i) {
//							Native.wr(block[i], NoC.NOC_REG_SNDDATA);
//						}
					}
				}
			};
			// r.run();
			scope.enter(r);
		}
	}

	private class Encrypt implements Runnable {

		private final BlockCipher crypt;
		byte[] ciph = new byte[BLOCK_SIZE];
		byte[] block = new byte[BLOCK_SIZE];

		public Encrypt(CipherParameters params) {
			crypt = new AESLightEngine();
			crypt.init(true, params);
		}

		public void run() {
			PrivateScope scope = new PrivateScope(1000);
			Runnable r = new Runnable() {
				public void run() {
					int cnt = 0;
					while (cnt < blockCnt) {

						while (!NoC.isReceiving()) {
							// wait
						}
						for (int i = 0; i < BLOCK_SIZE; ++i) {					
							int val = Native.rd(NoC.NOC_REG_RCVDATA);
						}
						NoC.writeReset();
						
//						if (!free.empty() && !q2.full()) {
//							while (!((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_RCV) != 0))
//								;
//							for (int i = 0; i < BLOCK_SIZE; ++i) {
//
//								block[i] = (byte) Native.rd(NoC.NOC_REG_RCVDATA);
//							}
//							Native.wr(1, NoC.NOC_REG_RCVRESET); // aka writeReset();

//							int ofs = BLOCK_SIZE;
//							do {
//								ofs -= crypt.getBlockSize();
//								crypt.processBlock(block, ofs, ciph, ofs);
//							} while (ofs > 0);
//							q2.enq(ciph);
//							ciph = free.deq();
							++cnt;
//						}
					}
				}
			};
//			scope.enter(r);
			r.run();
			finished = true;

		}
	}

	private class Decrypt implements Runnable {

		private final BlockCipher decrypt;
		byte[] deciph = new byte[BLOCK_SIZE];

		public Decrypt(CipherParameters params) {
			decrypt = new AESLightEngine();
			decrypt.init(false, params);
		}

		public void run() {

			PrivateScope scope = new PrivateScope(1000);
			Runnable r = new Runnable() {
				public void run() {
					int cnt = 0;
					while (cnt < blockCnt) {
						if (!q2.empty() && !q3.full()) {
							final byte[] block = q2.deq();
							int ofs = BLOCK_SIZE;
							do {
								ofs -= decrypt.getBlockSize();
								decrypt.processBlock(block, ofs, deciph, ofs);
							} while (ofs > 0);
							q3.enq(deciph);
							deciph = block;
							++cnt;
						}
					}
				}
			};
			scope.enter(r);
		}
	}

	private class Sink implements Runnable {

		boolean ok = true;

		public void run() {

			PrivateScope scope = new PrivateScope(1000);
			Runnable r = new Runnable() {
				public void run() {
					Random rnd = new Random();
					rnd.setSeed(127);
					int cnt = 0;
					while (cnt < blockCnt) {
						if (!q3.empty() && !free.full()) {
							final byte[] block = q3.deq();
							for (int i = 0; i < block.length; i++) {
								if (block[i] != (byte) rnd.nextInt())
									ok = false;
							}
							free.enq(block);
							++cnt;
						}
					}

				}
			};
			scope.enter(r);
			finished = true;
		}
	}

	public static void main(String[] args) {

		AESCsp aes = new AESCsp();
		// Initialization for benchmarking
		int start = 0;
		int stop = 0;
		int time = 0;

		System.out.println("AES Benchmark with SPM and NoC");

		int nrCpu = Runtime.getRuntime().availableProcessors();
		if (nrCpu < 3) {
			throw new Error("Not enough CPUs");
		}

		// ni must be translated from proc index to NoC address!
		new RtThread(aes.source, 1, 1000).setProcessor(1);
		new RtThread(aes.enc, 1, 1000).setProcessor(2);
//		new RtThread(aes.dec, 1, 1000).setProcessor(3);
//		new RtThread(aes.sink, 1, 1000).setProcessor(4);

		aes.reset(100);

		// start the other CPUs
		System.out.println("starting cpus.");
		RtThread.startMission();

		start = (int) System.currentTimeMillis();

		while (!finished) {
			;
		}
		// End of measurement
		stop = (int) System.currentTimeMillis();

		System.out.println("StartTime: " + start);
		System.out.println("StopTime: " + stop);
		time = stop - start;
		System.out.println("TimeSpent: " + time);
		System.out.println("Result = " + aes.sink.ok);

	}
}
