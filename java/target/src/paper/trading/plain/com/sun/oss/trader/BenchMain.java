package com.sun.oss.trader;

import com.sun.oss.trader.tradingengine.*;
import com.sun.oss.trader.data.*;

import joprt.RtThread;
import com.jopdesign.sys.GC;

import java.io.*;

public class BenchMain
{
    public BenchMain()
    {

        try {
            // Create the Runnable worker classes
            //
            MarketManager marketMgr = new MarketManager();
            OrderManager orderMgr = new OrderManager(marketMgr);
            
			System.out.println("MarketManager.onMessage()");

			marketMgr.onMessage(
"<symbol>X</symbol><price>4.950                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         </price>"
);

			System.out.println("================================");

			OrderEntry o = new OrderEntry(new StringBuffer("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"), 1.23456789, 999999999, OrderType.STOP_SELL);

			System.out.println("OrderManager.checkForTrade()");

			orderMgr.checkForTrade(o, 1.23456789);

			System.out.println("================================");
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

	public static void main( String[] args )
	{
		BenchMain main = new BenchMain();
	}
}
