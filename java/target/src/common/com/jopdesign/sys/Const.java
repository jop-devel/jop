/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

/*
 * Created on 12.04.2004
 *
 */
package com.jopdesign.sys;

/**
 * Constants for the class structure, hardware configuration,
 * and IO addresses.
 *
 * @author martin
 *
 */
public class Const {
	
	// Constants for the class structure
	
	/**
	 * Size of class header part.
	 * Difference between class struct and method table.
	 */
	static final int CLASS_HEADR = 5;
	/**
	 * Size of instance relative to class struct.
	 */
	public static final int CLASS_SIZE = 0;
	/**
	 * Pointer to super class relative to class struct.
	 */
	public static final int CLASS_SUPER = 3;
	/**
	 * Pointer to super class relative to class struct.
	 */
	static final int CLASS_IFTAB = 4;
	
	/**
	 * Class info start relative to start of MTAB.
	 */
	public static final int MTAB2CLINFO = -5;
	/**
	 * GC_INFO field relative to start of MTAB.
	 */
	static final int MTAB2GC_INFO = -3;
	
	/**
	 * Size of the on-chip stack cache including the area
	 * for JVM microcode scratch registers and microcode
	 * constant.
	 * <p>
	 * Change also (dependent on):<ul>
	 * 	<li>RAM_LEN in Jopa.java</li>
	 * 	<li>ram_width in jop_config_xxx.vhd</li>
	 * </ul>
	 * <p>
	 * Used in @link RtThreadImpl
	 * 
	 */
	public static final int STACK_SIZE = 256;
	
	/**
	 * Set to true if support for double bytecodes should be
	 * provided.
	 */
	public static final boolean SUPPORT_DOUBLE = true;
	/**
	 * Set to true if support for float bytecodes should be
	 * provided.
	 */
	public static final boolean SUPPORT_FLOAT = true;
	
	/**
	 * Set to true if RTTM is used. 
	 */
	public static final boolean USE_RTTM = false;

	/**
	 * Offset of the real stack in the on-chip RAM. Is set in
	 * <code>jvm.asm</code>.
	 */
	public static final int STACK_OFF = 64;
	
	/**
	 * Start address of scratchpad RAM. Depends on number of
	 * address bits used to decode (see sc_pack.vhd).
	 */
	public static final int SCRATCHPAD_ADDRESS = 0x400000;
	

	/**
	 * Constant pool pointer offset in on-chip stack cache.
	 * Constants for JVM register (on-chip RAM)
	 */
	public static final int RAM_CP = 1;

	// Exception numbers - 1-7 reserved for HW generated exceptions
	/**
	 * Hardware generated excpetion number for stack overflow.
	 */
	public static final int EXC_SPOV = 1;
	/**
	 * Hardware generated excpetion number for
	 * null pointer assignment.
	 */
	public static final int EXC_NP = 2;
	/**
	 * Hardware generated excpetion number for
	 * out of bounds exception.
	 */
	public static final int EXC_AB = 3;
	/**
	 * Hardware generated exception number for
	 * RTTM rollback exception.
	 */
	public static final int EXC_ROLLBACK = 4;
	
	/**
	 * Software generated divide by zero exception.
	 */
	public static final int EXC_DIVZ = 8;
	
	/**
	 * Base address for all IO devices. We use a negative
	 * address for a fast constant load with <code>bipush</code>.
	 * 
	 * All IO devices are decoded from address(6 downto 4).
	 *
	 * Depends on scio_*.vhd
	 *	    => 8 different IO devices
	 *  	=> each device can contain up to 16 registers
	 * 
	 */
	public static final int IO_BASE = 0xffffff80;


	public static final int IO_SYS_DEVICE = IO_BASE+0;
	public static final int IO_USB = IO_BASE+0x20;
	
	// output port for SD DAC
	public static final int IO_DSPIO_OUT = IO_BASE+0x40;
	
	// scio_min.vhd
	public static final int IO_CNT = IO_BASE+0;
	/**
	 * Global interrupt enable
	 */
	public static final int IO_INT_ENA = IO_BASE+0;
	public static final int IO_US_CNT = IO_BASE+1;
	public static final int IO_TIMER = IO_BASE+1;
	/**
	 * Trigger a SW interrupt
	 */
	public static final int IO_SWINT = IO_BASE+2;
	public static final int IO_INTNR = IO_BASE+2;
	public static final int IO_WD = IO_BASE+3;
	public static final int IO_EXCPT = IO_BASE+4;
	public static final int IO_LOCK = IO_BASE+5;
	public static final int IO_CPU_ID = IO_BASE+6;
	public static final int IO_SIGNAL = IO_BASE+7;
	/**
	 * Interrupt mask for individual interrupts
	 */
	public static final int IO_INTMASK = IO_BASE+8;
	/**
	 * Clear all pending interrupts
	 */
	public static final int IO_INTCLEARALL = IO_BASE+9;
	
	/**
	 * Read RAM counter
	 */
	// remove the comment for RAM access counting
	public static final int IO_RAMCNT = IO_BASE+10;
	public static final int IO_DEADLINE = IO_BASE+10;

	public static final int IO_CPUCNT = IO_BASE+11;
	public static final int IO_PERFCNT = IO_BASE+12;

	/**
	 * Number of available interrupts depends on the parameter
	 * in sc_sys.vhd. 3 is the default: one timer interrupt
	 * and 2 software interrupts.
	 */
	public static final int NUM_INTERRUPTS = 3;

	public static final int IO_STATUS = IO_BASE+0x10;
	public static final int IO_UART = IO_BASE+0x10+1;

	public static final int MSK_UA_TDRE = 1;
	public static final int MSK_UA_RDRF = 2;
	
	// LED & SWITCH on DE2
	public static final int LS_BASE = IO_BASE+0x40;
	
	// Expansion header on DE2-70
	public static final int EH_BASE = IO_BASE+0x30;
	public static final int IIC_BASE = IO_BASE+0x50;
	
	// Ethernet interface on DE2
	public static final int DM9000 = IO_BASE+0x50;
	
	/* "Sensors" on hwScope Example.
	 */
	public static final int SENS_M_BASE = IO_BASE+0x30;
	public static final int SENS_C_BASE = IO_BASE+0x50;

	// I2C ports
	public static final int I2C_A_BASE = IO_BASE+0x30;
	public static final int I2C_B_BASE = IO_BASE+0x60;
	
	// Keyboard
	public static final int KB_CTRL = IO_BASE+0x30+0;
	public static final int KB_DATA = IO_BASE+0x30+1;
	public static final int KB_SCANCODE = IO_BASE+0x30+2;

	// Mouse
	public static final int MOUSE_STATUS 	= IO_BASE+0x40+0;
	public static final int MOUSE_FLAG 	= IO_BASE+0x40+1;
	public static final int MOUSE_X_INC	= IO_BASE+0x40+2;
	public static final int MOUSE_Y_INC	= IO_BASE+0x40+3;
		
	public static final int MSK_DTA_RDY  	= 0x01;
	public static final int MSK_BTN_LEFT	= 0x02;
	public static final int MSK_BTN_RIGHT	= 0x04;
	public static final int MSK_BTN_MIDDLE	= 0x08;
	public static final int MSK_X_OVFLOW	= 0x10;
	public static final int MSK_Y_OVFLOW	= 0x20;

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
	// public static final int WB_AC97 = WB_BASE+0x30;
	// new version with SPI interface - scio_dspio.vhd is missing!
	public static final int WB_AC97 = WB_BASE+0x40;
	public static final int WB_SPI = WB_BASE+0x40;


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
	
	public static final int NOC_ADDR = IO_BASE+0x40;

	
	// OSSI
	public static final int IO_PWM = IO_BASE+0x30+6;
	
	// test salve addresses
	public static final int WB_TS0 = WB_BASE+0x30;
	public static final int WB_TS1 = WB_BASE+0x72;
	public static final int WB_TS2 = WB_BASE+0x74;
	public static final int WB_TS3 = WB_BASE+0x76;

	// RTTM
	
	// Adapt magic address to available SRAM: 2 MiB or 1 MiB
	public static final boolean USE_RTTM_BIGMEM = true;
	
	// Keep in synch with VHDL tm_state_machine.tm_magic_detect generic.
	public static final int MEM_TM_MAGIC = USE_RTTM_BIGMEM ? 
			0x0C0000 : 0x060000;

	public static final int TM_END_TRANSACTION = 0;
	public static final int TM_START_TRANSACTION = 1;
	public static final int TM_ABORTED = 2;
	public static final int TM_EARLY_COMMIT = 3;

	// RTTM instrumentation
	// Keep in synch with VHDL tm_state_machine constants.
	public static final int MEM_TM_RETRIES = MEM_TM_MAGIC+0;
	public static final int MEM_TM_COMMITS = MEM_TM_MAGIC+1;
	public static final int MEM_TM_EARLY_COMMITS = MEM_TM_MAGIC+2;
	public static final int MEM_TM_READ_SET = MEM_TM_MAGIC+3;
	public static final int MEM_TM_WRITE_SET = MEM_TM_MAGIC+4;
	public static final int MEM_TM_READ_OR_WRITE_SET = MEM_TM_MAGIC+5;

	
}
