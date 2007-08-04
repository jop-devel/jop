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
	
	// Constants for the class structure
	
	/**
	 * Size of class header part.
	 * Difference between class struct and method table
	 */
	static final int CLASS_HEADR = 5;
	/**
	 * GC_INFO field relativ to start of MTAB.
	 */
	static final int MTAB2GC_INFO = -3;
	/**
	 * Pointer to super class relativ to class struct
	 */
	static final int CLASS_SUPER = 3;
	
	// Constants for JVM registers (on-chip RAM)
	
	/**
	 * Constant pool pointer
	 */
	public static final int RAM_CP = 1;

	// Exception numbers - 1-7 reserved for HW generated exceptions
	public static final int EXC_SPOV = 1;
	public static final int EXC_NP = 2;
	public static final int EXC_AB = 3;
	
	public static final int EXC_DIVZ = 8;
	
	// use neagitve base address for fast constant load
	// with bipush
	public static final int IO_BASE = 0xffffff80;

	// all IO devices are decoded from address(6 downto 4)
	//	depends on scio_*.vhd
	//	=> 8 different IO devices
	//	=> each device can contain up to 16 registers

	public static final int IO_SYS_DEVICE = IO_BASE+0;
	public static final int IO_USB = IO_BASE+0x20;
	
	// scio_min.vhd
	public static final int IO_CNT = IO_BASE+0;
	public static final int IO_INT_ENA = IO_BASE+0;
	public static final int IO_US_CNT = IO_BASE+1;
	public static final int IO_TIMER = IO_BASE+1;
	public static final int IO_SWINT = IO_BASE+2;
	public static final int IO_WD = IO_BASE+3;
	public static final int IO_EXCPT = IO_BASE+4;
	public static final int IO_CPU_ID = IO_BASE+6;
	public static final int IO_SIGNAL = IO_BASE+7;

	public static final int IO_STATUS = IO_BASE+0x10;
	public static final int IO_UART = IO_BASE+0x10+1;

	public static final int MSK_UA_TDRE = 1;
	public static final int MSK_UA_RDRF = 2;
	
	// FPU
	public static final int IO_FPU = IO_BASE+0x70;
	public static final int FPU_A = IO_FPU+0;
	public static final int FPU_B = IO_FPU+1;
	public static final int FPU_OP = IO_FPU+2;
	public static final int FPU_RES = IO_FPU+3;

	public static final int FPU_OP_ADD = 0;
	public static final int FPU_OP_SUB = 1;
	public static final int FPU_OP_MUL = 2;
	public static final int FPU_OP_DIV = 3;

	// TAL, baseio (scio_baseio.vhd)
	public static final int IO_IN = IO_BASE+0x40+0;
	public static final int IO_LED = IO_BASE+0x40+0;
	public static final int IO_OUT = IO_BASE+0x40+1;
	public static final int IO_ADC1 = IO_BASE+0x40+1;
	public static final int IO_ADC2 = IO_BASE+0x40+2;
	public static final int IO_ADC3 = IO_BASE+0x40+3;
	// ISA bus for the CS8900
	public static final int IO_CTRL = IO_BASE+0x50+0;
	public static final int IO_DATA = IO_BASE+0x50+1;

	// dspio (scio_dpsio.vhd)
	//
	// FTDI USB interface
	// We use the same status/data interface as for the
	// UART connected to SimpCon
	//
	// Take care, System.out (JVWHelp.wr()) writes to
	// the USB port!
	//
	// use a better address some time (higher address)
	//
	public static final int IO_USB_STATUS = IO_USB;
	public static final int IO_USB_DATA = IO_USB+1;

	// Wishbone base address
	public static final int WB_BASE = IO_BASE;
	// AC97 interface
	public static final int WB_AC97 = WB_BASE+0x30;

	// LEGO stuff (scio_lego.vhd)
	public static final int IO_LEGO = IO_BASE+0x30;

	// MAC for rup (scio_usb.vhd)
	public static final int IO_MAC = IO_BASE+0x60;
	public static final int IO_MAC_A = IO_BASE+0x60;
	public static final int IO_MAC_B = IO_BASE+0x60+1;
	
	// mic IO for Mikael & Jens
	public static final int IO_MICRO = IO_BASE+0x60;

	// BG263
	// TODO: change iobg
	// new naming for UART base address
	public static final int IO_UART1_BASE = IO_BASE+0x10;
	// avoid 0x20, the USB port
	public static final int IO_UART_BG_MODEM_BASE = IO_BASE+0x30;
	public static final int IO_UART_BG_GPS_BASE = IO_BASE+0x40;
	// these are used in Testprog - substitute them
	// some time by the new names
	public static final int IO_STATUS2 = IO_UART_BG_MODEM_BASE;
	public static final int IO_UART2 = IO_UART_BG_MODEM_BASE+1;
	public static final int IO_STATUS3 = IO_UART_BG_GPS_BASE;
	public static final int IO_UART3 = IO_UART_BG_GPS_BASE+1;
	//
	public static final int IO_DISP = IO_BASE+0x50;
	public static final int IO_BG = IO_BASE+0x60;
	
	// OSSI
	public static final int IO_PWM = IO_BASE+0x30+6;
	
	// test salve addresses
	public static final int WB_TS0 = WB_BASE+0x30;
	public static final int WB_TS1 = WB_BASE+0x72;
	public static final int WB_TS2 = WB_BASE+0x74;
	public static final int WB_TS3 = WB_BASE+0x76;

	
}
