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

public class OrderFile {

	private static String [] orders = {
		"GM,30.4900,1000,stopbuy",
		"GM,30.4700,29500,stopsell",
		"GM,31.0000,85500,stopbuy",
		"GM,32.0000,85500,stopbuy",
		"GM,33.0000,85500,stopbuy",
		"GM,34.0000,85500,stopbuy",
		"GM,35.0000,85500,stopbuy",
		"GM,36.0000,85500,stopbuy",
		"GM,37.0000,85500,stopbuy",
		"GM,38.0000,85500,stopbuy",
		"GM,39.0000,85500,stopbuy",
		"GM,40.0000,85500,stopbuy",
		"SUNW,4.9600,1000,stopbuy",
		"SUNW,4.9900,10000,stopbuy",
		"SUNW,5.0300,10000,stopbuy",
		"SUNW,5.0300,10000,stopbuy",
		"SUNW,5.0300,10000,stopbuy",
		"SUNW,4.9500,10000,stopsell",
		"SUNW,4.9400,10000,stopsell",
		"SUNW,5.9400,10000,stopbuy",
		"SUNW,6.9400,10000,stopbuy",
		"SUNW,7.9400,10000,stopbuy",
		"SUNW,8.9400,10000,stopbuy",
		"SUNW,9.9400,10000,stopbuy",
		"SUNW,10.9400,10000,stopbuy",
		"SUNW,11.9400,10000,stopbuy",
		"SUNW,12.9400,10000,stopbuy",
		"SUNW,13.9400,10000,stopbuy",
		"SUNW,14.9400,10000,stopbuy",
		"GE,33.9100,26100,stopsell",
		"GE,33.9000,52900,stopsell",
		"GE,34.0200,54700,stopbuy",
		"GE,34.0200,54700,stopbuy",
		"GE,34.0400,17100,stopbuy",
		"GE,34.0400,17100,stopbuy",
		"GE,35.0400,17100,stopbuy",
		"GE,35.1400,17100,stopbuy",
		"GE,35.2600,17100,stopbuy",
		"GE,35.3400,17100,stopbuy",
		"GE,35.4400,17100,stopbuy",
		"GE,35.5400,17100,stopbuy",
		"GM,30.4900,1000,limitbuy",
		"GM,30.4700,29500,limitsell",
		"GM,31.0000,85500,limitbuy",
		"GM,32.0000,85500,limitbuy",
		"GM,33.0000,85500,limitbuy",
		"GM,34.0000,85500,limitbuy",
		"GM,35.0000,85500,limitbuy",
		"GM,36.0000,85500,limitbuy",
		"GM,37.0000,85500,limitbuy",
		"GM,38.0000,85500,limitbuy",
		"GM,39.0000,85500,limitbuy",
		"GM,40.0000,85500,limitbuy",
		"SUNW,4.9600,1000,limitbuy",
		"SUNW,5.0300,10000,limitbuy",
		"SUNW,5.0300,10000,limitbuy",
		"SUNW,4.9400,10000,limitsell",
		"SUNW,5.9400,10000,limitbuy",
		"SUNW,6.9400,10000,limitbuy",
		"SUNW,7.9400,10000,limitbuy",
		"SUNW,8.9400,10000,limitbuy",
		"SUNW,9.9400,10000,limitbuy",
		"SUNW,10.9400,10000,limitbuy",
		"SUNW,11.9400,10000,limitbuy",
		"SUNW,12.9400,10000,limitbuy",
		"SUNW,13.9400,10000,limitbuy",
		"SUNW,14.9400,10000,limitbuy",
		"GE,33.9100,26100,limitsell",
		"GE,34.0200,54700,limitbuy",
		"GE,34.0400,17100,limitbuy",
		"GE,35.0400,17100,limitbuy",
		"GE,35.1400,17100,limitbuy",
		"GE,35.2600,17100,limitbuy",
		"GE,35.3400,17100,limitbuy",
		"GE,35.4400,17100,limitbuy",
		"GE,35.5400,17100,limitbuy"
	};

	private int line = 0;

	public String readLine() {
		return orders[line++];
	}

}