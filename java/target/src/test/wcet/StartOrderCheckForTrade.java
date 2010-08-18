/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package wcet;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.sun.oss.trader.data.OrderEntry;
import com.sun.oss.trader.data.OrderType;
import com.sun.oss.trader.tradingengine.MarketManager;
import com.sun.oss.trader.tradingengine.OrderManager;

/**
 * Purpose: Measure the checkForTrade method of the OrderManager benchmark
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class StartOrderCheckForTrade {
	   /* Debugging signals to manipulate the cache */
    final static int CACHE_FLUSH = -51;
    final static int CACHE_DUMP = -53;

	/**
	 * Set to false for the WCET analysis, true for measurement
	 */
	final static boolean MEASURE = true;
    final static boolean MEASURE_CACHE = false;
	static int ts, te, to;
	private static OrderManager orderMgr;
	private static OrderEntry orderEntry;
	private static double orderPrice;


	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// initialization
        MarketManager marketMgr = new MarketManager();
        try {
			orderMgr = new OrderManager(marketMgr);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
        
		marketMgr.onMessage("<symbol>X</symbol><price>4.950                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         </price>");
		StringBuffer longStringBuf = new StringBuffer("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		int min = 0x7fffffff;
		int max = 0;
		int val = 0;
		for (int i=0; i<1000; ++i) {
		    if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
		    orderEntry = new OrderEntry(longStringBuf, 
		    							1.23456789 * i, 
		    							123456789 * i, 1 + (i % 6));
			orderPrice = 1.23456789 * i;
		    invoke();
			val = te-ts-to;
			if (val<min) min = val;
			if (val>max) max = val;
		}
		if (MEASURE) {
                    System.out.print("bcet:");
                    System.out.println(min);
                    System.out.print("wcet:");
                    System.out.println(max);
                }
	}
	
	static void invoke() {
		measure();
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
		if (MEASURE_CACHE) Native.rdMem(CACHE_DUMP);
	}

	static void measure() {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		orderMgr.checkForTrade(orderEntry, orderPrice);
	}
}
