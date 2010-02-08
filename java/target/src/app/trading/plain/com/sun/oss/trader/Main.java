/*
 * Copyright 2009-2010 Eric Bruno, Greg Bollella. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  
 *
 * Neither the name of the Book, 
 * "Real-Time Java Programming with Java RTS"
 * nor the names of its authors may be used to endorse or promote 
 * products derived from this software without specific prior written 
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * See the GNU General Public License version 2 for more details. 
 * You should have received a copy of the GNU General Public License 
 * version 2 along with this work; if not, write to the: 
 * Free Software Foundation, Inc. 
 * 51 Franklin St, Fifth Floor 
 * Boston, MA 02110-1301 USA.
 */
package com.sun.oss.trader;

import com.sun.oss.trader.tradingengine.*;
import com.sun.oss.trader.data.*;

import joprt.RtThread;
import com.jopdesign.sys.GC;

import java.io.*;

public class Main
{
	static class GCRunner implements Runnable {
		public GCRunner() {
			GC.setConcurrent();
		}
                
		public void run() {
			for (;;) {
				GC.gc();
				boolean madePeriod = RtThread.currentRtThread().waitForNextPeriod();
				if (!madePeriod) {
					System.out.print('G');
				}
			}
		}
	}	

    public Main()
    {

        try {
            // Create the Runnable worker classes
            //
			JOPReader reader = new JOPReader(512);
            MarketManager marketMgr = new MarketManager();
            OrderManager orderMgr = new OrderManager(marketMgr);
			orderMgr.displayOrderBook();
            
            // Create and start Threads
            //
			RtThread readerThread = new RtThread(reader, 10, 1380);
            RtThread marketThread = new RtThread(marketMgr, 5, 4800);
            RtThread orderThread = new RtThread(orderMgr, 4, 300000);

			// create GCRunner here so we have the normal GC up to this point
			GCRunner gcRunner = new GCRunner();
 			RtThread gcThread = new RtThread(gcRunner, 1, 330000);

			System.out.println("Starting mission ...");

			RtThread.startMission();

			System.out.println("... up and running");

			for(;;);
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

	public static void main( String[] args )
	{
		Main main = new Main();
	}
}
