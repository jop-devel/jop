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

package jembench.stream;

import jembench.StreamBenchmark;

import  org.bouncycastle.crypto.BlockCipher;
import  org.bouncycastle.crypto.CipherParameters;
import  org.bouncycastle.crypto.params.KeyParameter;
import  org.bouncycastle.crypto.engines.AESLightEngine;

import  java.util.Random;

public class AES extends StreamBenchmark {
  private final static int  BLOCK_SIZE  = 512;
  private final static int  BLOCK_COUNT =  47;

  private int  result;

  public AES() {}

  public String toString() {
    return "AES";
  }

  protected int        getDepth() {
    return  4;
  }

  protected Runnable[] getWorkers() {
    class BlockFifo {
      private final byte[][]  queue;
      private int  rdPtr;
      private int  wrPtr;

      public BlockFifo() {
	queue = new byte[4][];
	reset();
      }

      public synchronized byte[] getBlock() {

	// Wait until !empty
	while(rdPtr < 0) {
	  if(rdPtr < -1)  return  null;
	  try { wait(); } catch(InterruptedException e) {}
	}
	final byte[]  res = queue[rdPtr];

	// Was full?
	if(rdPtr == wrPtr)  notifyAll();
	rdPtr = (rdPtr+1)&3;
	if(rdPtr == wrPtr) {
	  rdPtr = -1;
	  notifyAll();
	}

	return  res;
      }
      public synchronized void putBlock(final byte[]  block) {

	// Wait until !full
	while(rdPtr == wrPtr) {
	  try { wait(); } catch(InterruptedException e) {}
	}
	queue[wrPtr] = block;

	// Was empty?
	if(rdPtr < 0) {
	  rdPtr = wrPtr;
	  notifyAll();
	}
	wrPtr = (wrPtr+1)&3;
      }
      public synchronized void close() {

	while(rdPtr >= 0) {
	  try { wait(); } catch(InterruptedException e) {}
	}
	rdPtr = -2;
	notifyAll();
      }
      public synchronized void reset() {
	rdPtr = -1;
      }
    }

    final CipherParameters  params; {
      final byte[]  key = new byte[16];
      final Random  rnd = new Random(127);
      for(int i = key.length; i > 0; key[--i] = (byte)rnd.nextInt());
      params = new KeyParameter(key);
    }
    final BlockFifo  f1 = new BlockFifo();
    final BlockFifo  f2 = new BlockFifo();
    final BlockFifo  f3 = new BlockFifo();

    return  new Runnable[] {
      new Runnable() {
	public void run() {
	  final Random  rnd = new Random(1L);
	  
	  for(int  c = BLOCK_COUNT; --c >= 0;) {
	    final byte[]  block = new byte[BLOCK_SIZE];
	    for(int  i = BLOCK_SIZE; --i >= 0; block[i] = (byte)rnd.nextInt());
	    f1.putBlock(block);
	  }
	  f1.close();
	}
      },
      new Runnable() {
	public void run() {
	  final BlockCipher  crypt = new AESLightEngine();
	  crypt.init(true,  params);

	  byte[]  block;
	  while((block = f1.getBlock()) != null) {
	    final byte[]  ciph = new byte[block.length];
	    crypt.processBlock(block, 0, ciph, 0);
	    f2.putBlock(ciph);
	  }
	  f2.close();
	}
      },
      new Runnable() {
	public void run() {
	  int  c = 0;
	  final BlockCipher  decrypt = new AESLightEngine();
	  decrypt.init(false,  params);

	  byte[]  block;
	  while((block = f2.getBlock()) != null) {
	    final byte[]  deciph = new byte[block.length];
	    decrypt.processBlock(block, 0, deciph, 0);
	    f3.putBlock(deciph);
	  }
	  f3.close();
	}
      },
      new Runnable() {
	public void run() {
	  int  c = 0;
	  int  check = 0;

	  byte[]  block;
	  while((block = f3.getBlock()) != null) {
	    for(int  i = block.length; --i >= 0;) {
	      check += block[i];
	      if(check < 0)  check = (check >>> 1) + (check & 1);
	    }
	  }
	  result = check;
	}
      }
    };
  }

  public int getCheckSum() {
    return  result;
  }
}
