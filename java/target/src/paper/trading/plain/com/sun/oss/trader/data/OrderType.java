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
package com.sun.oss.trader.data;

public class OrderType
{
    public static final int LIMIT_BUY = 1;
    public static final int LIMIT_SELL = 2;
    public static final int STOP_BUY = 3;
    public static final int STOP_SELL = 4;
    public static final int MARKET_BUY = 5;
    public static final int MARKET_SELL = 6;
    public static boolean isValid(int t) {
        if ( t >= LIMIT_BUY && t <= MARKET_SELL )
            return true;
        return false;
    }
    public static String getAsString(int t) {
        switch ( t ) {
        case LIMIT_BUY:
            return "Limit Buy Order";
        case LIMIT_SELL:
            return "Limit Sell Order";
        case STOP_BUY:
            return "Stop Buy Order";
        case STOP_SELL:
            return "Stop Sell Order";
        case MARKET_BUY:
            return "Market Buy Order";
        case MARKET_SELL:
            return "Market Sell Order";
        }
        return "Invalid order type";
    }
}
