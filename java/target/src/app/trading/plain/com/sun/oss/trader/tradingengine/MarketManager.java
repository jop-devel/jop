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
package com.sun.oss.trader.tradingengine;

import java.util.*;
import java.io.*;

import joprt.RtThread;
import joprt.SwEvent;

public class MarketManager implements Runnable
{
    static final String SYMBOL_TAG = "<symbol>";
    static final String SYMBOL_END_TAG = "</symbol>";
    static final String PRICE_TAG = "<price>";
    static final String PRICE_END_TAG = "</price>";

    private boolean fDebug = false;
    
    // The Market Book is a hashmap of prices (stored as StringBuffer objects)
    // that are looked up by stock symbol (StringBuffer) as the key
    public static final int INITIAL_CAPACITY = 111;
    HashMap<String,StringBuffer> marketBook = 
        new HashMap<String,StringBuffer>(INITIAL_CAPACITY); 

	private static final int MAX_UPDATE_XML_SIZE = 512;

    public MarketManager()
    {
    }

    public void close()
    {
    }

    public void displayMarketBook()
    {
        System.out.println("************************************************");
        System.out.println("Current Market Book:");

        Set<String> keys = marketBook.keySet();
        Iterator<String> iter = keys.iterator();
        while ( iter.hasNext() )
			{
				String symbol = iter.next();
				double price = getLastTradePrice(symbol);
				System.out.println( "Last trade price for " + 
									symbol + ":" + price);
			}

        System.out.println(" ");
        System.out.println(" ");
    }

    // Display the last trade price for a symbol
    public double getLastTradePrice(String symbol)
    {
        // Get the order book for this symbol if it exists
        try {
            return Double.parseDouble( marketBook.get(symbol).toString() );
        }
        catch ( Exception e ) { }
        
        return 0;
    }

    public void run() 
    {
		JOPReader reader = new JOPReader(2*MAX_UPDATE_XML_SIZE);

		while (true) {

			try {
				if (reader.availableLines() > 0) {
					String msg = reader.readLine();
					onMessage(msg);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			boolean madePeriod = RtThread.currentRtThread().waitForNextPeriod();
			if (!madePeriod) {
				System.out.print('M');
			}

		}

    }
    
	public void onMessage(String msg) 
	{
		try {
			/*
			 * SAMPLE UPDATE XML:
			 <updates>
			 <update>
			 <symbol>SUNW</symbol>
			 <datetime>2006-09-20T13:59:25.993-04:00</datetime>
			 <price>4.9500</price>
			 </update>
			 </updates>
			*/

			// To preserve memory when running within a no-heap realtime thread
			// (NHRT) the XML String is walked manually, without the use of
			// a DOM or SAX parser that would otherwise create lots of objects
			//
			String sUpdate = msg;
			int start = 0;
			boolean fParse = true;
			while ( fParse ) //@WCA loop <= 1
				{
					int sBegin = sUpdate.indexOf(SYMBOL_TAG, start);
					if ( sBegin < 0 )
						break;

					int sEnd = sUpdate.indexOf(SYMBOL_END_TAG, sBegin);
					String symbol = sUpdate.substring(sBegin+(SYMBOL_TAG.length()), sEnd);

					int pBegin = sUpdate.indexOf(PRICE_TAG, start);
					int pEnd = sUpdate.indexOf(PRICE_END_TAG, pBegin);
					String price = sUpdate.substring(pBegin+(PRICE_TAG.length()), pEnd);
					start = pEnd;
                
					onUpdate(symbol, price );
				}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
    
	private void onUpdate(String symbol, String price)
	{
		//log("Symbol: " + symbol + ", Quote: " + price);

		// Look for the symbol in the existing market book. If it's not there
		// then create a new StringBuffer with an initial capacity, so that
		// as updates occur memory will not be constantly allocated
		//
		StringBuffer sbPrice = marketBook.get(symbol);
		if ( sbPrice == null )
			{
				sbPrice = new StringBuffer(15);
				marketBook.put(symbol, sbPrice);
			}

		// Replace the existing contents of the price StringBuffer
		// to avoid allocating more memory
		//
		sbPrice.replace(0, price.length(), price);
	}

    private void log(String m)
    {
        if ( fDebug )
            System.out.println(m);
    }
}
