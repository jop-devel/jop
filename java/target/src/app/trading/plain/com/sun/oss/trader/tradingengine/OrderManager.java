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

import com.sun.oss.trader.data.*;
import com.sun.oss.trader.mq.*;

import joprt.RtThread;

import java.util.*;

public class OrderManager implements Runnable
{
    private static final String TRADES_TAG = "<trades>";
    private static final String TRADES_CLOSE = "</trades>";
    private static final String TRADE_TAG = "<trade>";
    private static final String TRADE_CLOSE = "</trade>";
    private static final String TYPE_TAG = "<type>";
    private static final String TYPE_CLOSE = "</type>";
    private static final String SYMBOL_TAG = "<symbol>";
    private static final String SYMBOL_CLOSE = "</symbol>";
    private static final String DATETIME_TAG = "<datetime>";
    private static final String DATETIME_CLOSE = "</datetime>";
    private static final String TRADEPRICE_TAG = "<tradeprice>";
    private static final String TRADEPRICE_CLOSE = "</tradeprice>";
    private static final String LIMITPRICE_TAG = "<limitprice>";
    private static final String LIMITPRICE_CLOSE = "</limitprice>";
    private static final String STOPPRICE_TAG = "<stopprice>";
    private static final String STOPPRICE_CLOSE = "</stopprice>";
    private static final String VOLUME_TAG = "<volume>";
    private static final String VOLUME_CLOSE = "</volume>";
    private static final String BUY = "Buy";
    private static final String SELL = "Sell";

	private static final boolean ENABLE_DEBUG = false;

    private static final int MAX_TRADE_XML_SIZE = 512;
    private BoundedStringBuffer tradeXML = new BoundedStringBuffer(MAX_TRADE_XML_SIZE); 
    //private String cannedTrade = "<trades><trade><type>Sell</type><symbol>GM</symbol><datetime>2007-02-06T14:49:03.800-05:00</datetime><tradeprice>30.47</tradeprice><limitprice>30.47</limitprice><volume>29500</volume></trade></trades>";

    private MarketManager marketMgr = null;    
    private TradeManagement tradeMgr = null; 

    // The order book is a HashMap of linked lists. Each HashMap bucket
    // is keyed by a stock name (a StringBuffer). The linked list pointed to 
    // by each bucket represents the list of open orders for that
    // particular stock, in value, low to high for buy orders, and
    // high to low for sell orders
    //
    public static final int INITIAL_CAPACITY = 128;
    class SubBook {
        LinkedList<OrderEntry> buy = new LinkedList<OrderEntry>();
        LinkedList<OrderEntry> sell = new LinkedList<OrderEntry>();
    }

    HashMap<StringBuffer,SubBook> orderBook = 
        new HashMap<StringBuffer,SubBook>(INITIAL_CAPACITY);
    
    StringBuffer[] orderBookKeys = null;


    
    // Constructors
    //
    public OrderManager(MarketManager marketMgr) throws Exception
    {
        this.marketMgr = marketMgr;
        this.tradeMgr = new TradeManagement("LimitStopTrades");
        
        // Read the local file "orders.csv" and add the orders
        try {
            // Read update data from file
            OrderFile reader = new OrderFile();
            
            String line = reader.readLine();
            while ( line != null )
            {
                int seperator = line.indexOf(',');
                String symbol = line.substring(0, seperator);
                int begin = seperator;
                seperator = line.indexOf(',', begin+1);
                String price = line.substring(begin+1, seperator);
                begin = seperator;
                seperator = line.indexOf(',', begin+1);
                String qty = line.substring(begin+1, seperator);
                begin = seperator;
                seperator = line.indexOf(',', begin+1);
                String type = line.substring(begin+1);
                int orderType = 0;
                if ( type.equalsIgnoreCase("limitbuy"))
                    orderType = OrderType.LIMIT_BUY;
                else if ( type.equalsIgnoreCase("limitsell"))
                    orderType = OrderType.LIMIT_SELL;
                else if ( type.equalsIgnoreCase("stopbuy"))
                    orderType = OrderType.STOP_BUY;
                else if ( type.equalsIgnoreCase("stopsell"))
                    orderType = OrderType.STOP_SELL;

                // Read the next line of fake update data
                line = reader.readLine();

                enterOrder( Double.parseDouble(price), 
                            Integer.parseInt(qty), 
                            new StringBuffer(symbol), 
                            orderType);
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    // Public methods
    //
    public void enterOrder( double price, int quantity, 
                            StringBuffer symbol, int type)
        throws Exception
    {
        OrderEntry newEntry = new OrderEntry(symbol, price, quantity, type);

        // Get the order book for this symbol if it exists
        SubBook sb = orderBook.get(symbol);
        if ( sb == null )
        {
            // Create a new order book for this symbol
            sb = new SubBook();
            orderBook.put(symbol, sb);
        }

        // Get the right sub book (but or sell orders)
        LinkedList<OrderEntry> orders = null;
        if ( newEntry.isBuyOrder() )
            orders = sb.buy;
        else
            orders = sb.sell;

        // Walk the sub book and find the correct location for this order
        for ( int i = 0; i < orders.size(); i++ )
        {
            OrderEntry entry = orders.get(i);
            if ( newEntry.comesBefore(entry.getPrice()) )
            {
                orders.add( i, newEntry );
                return;
            }
        }

        // Just add the order to the end of the list
        orders.addLast(newEntry);
	}
	
    // Display the entire order book for all symbols 
    public void displayOrderBook()
    {
        /*
        Set<StringBuffer> keys = orderBook.keySet();
        Iterator<StringBuffer> iter = keys.iterator();
        while ( iter.hasNext() )
        {
            StringBuffer key = iter.next();
            displayOrderBook(key);
        }
        */
        
        if ( orderBookKeys == null )
        {
            Set<StringBuffer> keys = orderBook.keySet();
            orderBookKeys = new StringBuffer[orderBook.size()];  
			int i = 0;
			for (StringBuffer k : keys) {
				orderBookKeys[i++] = k;
			}
        }
        
        for ( int i = 0; i < orderBook.size(); i++ )
            displayOrderBook(orderBookKeys[i]);
    }

    // Display the order book for a symbol
    public void displayOrderBook(StringBuffer symbol)
    {
        // Get the order book for this symbol if it exists
        SubBook sb = orderBook.get(symbol);
        if ( sb != null )
        {
            System.out.println("******************************************");
            System.out.println("Buy orders for " + symbol);
            displaySubBook(sb.buy);
            System.out.println("Sell orders for " + symbol);
            displaySubBook(sb.sell);
            System.out.println(" ");
        }
    }

    public void run() 
    {
        try {
            while ( true ) 
            {
 				long time = System.currentTimeMillis();

				// Make sure the global order book index is up-to-date
                // The call is optimized to ensure the work is only done
                // once unless the order book is updated
                //
                getOrderBookKeys();

                //
                // This loop continually compares the order book to the
                // market book to see if any trades should be executed
                //

                // Loop to compare market to order prices
                //
                for ( int i = 0; i < orderBookKeys.length; i++ )
                {
                    StringBuffer symbol = orderBookKeys[i];

                    StringBuffer sPrice = marketMgr.marketBook.get(symbol.toString());
                    if ( sPrice == null || sPrice.length() == 0 )
                        continue;

                    double marketPrice = 
                        Double.parseDouble( sPrice.toString() );

                    // Get the sub book for this symbol
                    //
                    SubBook sb = orderBook.get(symbol);

                    // Walk the list of sell orders to see if the market price
                    // has hit or passed a limit or stop order threshold
                    //
					for ( int x = 0; x < sb.sell.size(); x++ )
                        {
                            OrderEntry entry = sb.sell.get(x);
                            if ( checkForTrade( entry, marketPrice ) == true )
                                break;
                        }
                    
                    // Walk the list of buy orders to see if the market price
                    // has hit or passed a limit or stop order threshold
                    //
					for ( int x = 0; x < sb.buy.size(); x++ )
                        {
                            OrderEntry entry = sb.buy.get(x);
                            if ( checkForTrade( entry, marketPrice ) == true )
                                break;
                        }
                } 

				time = System.currentTimeMillis() - time;
				System.out.print("O: ");
				System.out.println(time);

				boolean madePeriod = RtThread.currentRtThread().waitForNextPeriod();
                if (!madePeriod)
                    System.out.print('O');
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
        
    private boolean checkForTrade(OrderEntry entry, double marketPrice)
    {
        if ( ! entry.isActive() )
            return false;

        switch ( entry.getType() )
        {
        case OrderType.STOP_SELL:
            if ( marketPrice <= entry.getPrice() )
            {
                /*
                System.out.println(
                        "Limit sell occured. Limit price: " + 
                        entry.getPrice() +
                        ", Market price: " + marketPrice +
                        ", Stock: " + entry.getSymbol() );
                 */

                generateTradeXML(entry, marketPrice);
                entry.setActive(false);
                return true;
            }
            break;

        case OrderType.LIMIT_SELL:
            if ( marketPrice == entry.getPrice()  )
            {
                /*
                System.out.println(
                        "Stop sell occured. Limit price: " + 
                        entry.getPrice() +
                        ", Market price: " + marketPrice +
                        ", Stock: " + entry.getSymbol() );
                 */

                generateTradeXML(entry, marketPrice);
                entry.setActive(false);
                return true;
            }
            else if ( marketPrice < entry.getPrice() )
            {
				if (ENABLE_DEBUG) {
					System.out.println(
                        "*** MISSED LIMIT SELL: Order price: " +
                        entry.getPrice() +
                        ", Market price=" + marketPrice +
                        ", Stock: " + entry.getSymbol() );
				}
//                 entry.setActive(false);
            }
            break;
            
        case OrderType.STOP_BUY:
            if ( marketPrice >= entry.getPrice() )
            {
                /*
                System.out.println(
                        "Stop buy occured. Order price: " +
                        entry.getPrice() +
                        ", Market price: " + marketPrice +
                        ", Stock: " + entry.getSymbol() );
                 */

                generateTradeXML(entry, marketPrice);
                entry.setActive(false);
                return true;
            }
            break;

        case OrderType.LIMIT_BUY:
            if ( marketPrice == entry.getPrice()  )
            {
                /*
                System.out.println(
                        "Limit buy occured. Order price: " +
                        entry.getPrice() +
                        ", Market price: " + marketPrice +
                        ", Stock: " + entry.getSymbol() );
                 */

                generateTradeXML(entry, marketPrice);
                entry.setActive(false);
                return true;
            }
            else if ( marketPrice > entry.getPrice() )
            {
				if (ENABLE_DEBUG) {
					System.out.println(
                        "*** MISSED LIMIT BUY: Order price: " +
                        entry.getPrice() +
                        ", Market price=" + marketPrice +
                        ", Stock: " + entry.getSymbol() );
				}
//                 entry.setActive(false);
            }
            break;
        }

        return false;
    }
    
    // Private methods
    //
    private BoundedStringBuffer generateTradeXML(OrderEntry entry, double tradePrice)
    {
        /*
         * SAMPLE:
            <trades>
                <trade>
                   <type>Buy</type>
                   <symbol>SUNW</Symbol>
                   <datetime>2006-09-20T13:59:25.993-04:00</datetime>
                   <tradeprice>4.9500</tradeprice>
                   <limitprice>4.9000</limitprice>
                   <volume>1000</volume>
                </trade>
            </trades>
        */

        try {
            tradeXML.delete(0,MAX_TRADE_XML_SIZE);

            // Root tags
            tradeXML.append(TRADES_TAG);
            tradeXML.append(TRADE_TAG);

            // Trade type: Buy or Sell
            tradeXML.append(TYPE_TAG);
            if ( entry.isBuyOrder() )
                tradeXML.append(BUY);
            else
                tradeXML.append(SELL);
            tradeXML.append(TYPE_CLOSE);

            // Stock Symbol
            tradeXML.append(SYMBOL_TAG);
            tradeXML.append(entry.getSymbol());
            tradeXML.append(SYMBOL_CLOSE);

            // Date/time stamp of trade
            tradeXML.append(DATETIME_TAG);
            tradeXML.append(System.currentTimeMillis()); 
            tradeXML.append(DATETIME_CLOSE);
            
            // Trade execution price
            tradeXML.append(TRADEPRICE_TAG);
            tradeXML.append(entry.getPrice()/*tradePrice*/);
            tradeXML.append(TRADEPRICE_CLOSE);

            // Indicate limit or stop order, and the original order price
            if ( entry.isLimitOrder() )
            {
                tradeXML.append(LIMITPRICE_TAG);
                tradeXML.append(entry.getPrice());
                tradeXML.append(LIMITPRICE_CLOSE);
            }
            else
            {
                tradeXML.append(LIMITPRICE_TAG);
                tradeXML.append(entry.getPrice());
                tradeXML.append(LIMITPRICE_CLOSE);
            }

            // Trade volume
            tradeXML.append(VOLUME_TAG);
            tradeXML.append(entry.getQuantity());
            tradeXML.append(VOLUME_CLOSE);

            // Close the root tags
            tradeXML.append(TRADE_CLOSE);
            tradeXML.append(TRADES_CLOSE);
            
            // Send the XML using an MBean
            //
            tradeMgr.notifyTrade( tradeXML.toString() );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        //System.out.println("Trade Data: " + tradeXML.toString() );
        return tradeXML;
    }
    
    private StringBuffer[] getOrderBookKeys()
    {
        if ( orderBookKeys == null )
        {
            Set<StringBuffer> keys = orderBook.keySet();
            orderBookKeys = new StringBuffer[orderBook.size()];
			int i = 0;
			for (StringBuffer k : keys) {
				orderBookKeys[i++] = k;
			}
        }

        return orderBookKeys;
    }

    // Display the orders within a sub book
    private void displaySubBook(LinkedList<OrderEntry> orders)
    {
        for ( int i = 0; i < orders.size(); i++)
        {
            OrderEntry entry = orders.get(i);
            System.out.println( "Price: " + entry.getPrice() + 
                                ", Qty: " + entry.getQuantity() +
                                ", Type: " + 
                                OrderType.getAsString( entry.getType() ) );
        }
    }
}
