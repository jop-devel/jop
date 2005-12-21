/*
 * Created on 12.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.jopdesign.sys;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Const {
	
	// use neagitve base address for fast constant load
	// with bipush
	public static final int IO_BASE = 0xffffff80;

	// all IO devices are decoded from address(6 downto 4)
	//	depends on scio_*.vhd
	//	=> 8 different IO devices
	//	=> each device can contain up to 16 registers

	// scio_min.vhd
	public static final int IO_CNT = IO_BASE+0;
	public static final int IO_INT_ENA = IO_BASE+0;
	public static final int IO_US_CNT = IO_BASE+1;
	public static final int IO_TIMER = IO_BASE+1;
	public static final int IO_SWINT = IO_BASE+2;
	public static final int IO_WD = IO_BASE+3;

	public static final int IO_STATUS = IO_BASE+0x10;
	public static final int IO_UART = IO_BASE+0x10+1;

	public static final int MSK_UA_TDRE = 1;
	public static final int MSK_UA_RDRF = 2;

	// dspio (scio_dpsio.vhd)
	//
	// FTDI USB interface
	// We use the same status/data interface as for the
	// UART connected to SimpCon
	public static final int IO_USB_STATUS = IO_BASE+0x20;
	public static final int IO_USB_DATA = IO_BASE+0x20+1;

	// use neagitve base address for fast constant load in Java
	public static final int WB_BASE = IO_BASE;
	// AC97 interface
	public static final int WB_AC97 = WB_BASE+0x30;

	// BG263
	// TODO: change iobg
	// new naming for UART base address
	public static final int IO_UART1_BASE = IO_BASE+0x10;
	public static final int IO_UART_BG_MODEM_BASE = IO_BASE+0x20;
	public static final int IO_UART_BG_GPS_BASE = IO_BASE+0x30;
	// these are used in Testprog - substitute them
	// some time by the new names
	public static final int IO_STATUS2 = IO_UART_BG_MODEM_BASE;
	public static final int IO_UART2 = IO_UART_BG_MODEM_BASE+1;
	public static final int IO_STATUS3 = IO_UART_BG_GPS_BASE;
	public static final int IO_UART3 = IO_UART_BG_GPS_BASE+1;
	//
	public static final int IO_DISP = IO_BASE+0x40;
	public static final int IO_BG = IO_BASE+0x50;
	// TAL
	// TODO: change iobaseio
	// TODO: change constants - 0x30 is a dummy value
	public static final int IO_IN = IO_BASE+0x30+10;
	public static final int IO_LED = IO_BASE+0x30+10;
	public static final int IO_OUT = IO_BASE+0x30+11;
	public static final int IO_ADC1 = IO_BASE+0x30+12;
	public static final int IO_ADC2 = IO_BASE+0x30+13;
	public static final int IO_ADC3 = IO_BASE+0x30+8;
	public static final int IO_CTRL = IO_BASE+0x30+14;
	public static final int IO_DATA = IO_BASE+0x30+15;
	// OSSI
	public static final int IO_PWM = IO_BASE+0x30+6;
	
	// test salve addresses
	public static final int WB_TS0 = WB_BASE+0x30;
	public static final int WB_TS1 = WB_BASE+0x72;
	public static final int WB_TS2 = WB_BASE+0x74;
	public static final int WB_TS3 = WB_BASE+0x76;

	
}
