package yaffs2.platform.emulation;

import yaffs2.port.*;
import yaffs2.utils.Globals;
import yaffs2.utils.UnexpectedException;
import yaffs2.utils.Yaffs1NANDInterface;
import yaffs2.utils.debug.communication.DebugDevice;
import yaffs2.utils.debug.communication.DirectInterfaceClientStub;
import yaffs2.utils.debug.pc.SerialInterface;
import yaffs2.utils.Utils;

public class Simulation_yaffscfg2k_C implements yaffs2.port.yaffscfg2k_C
{
	/*
	 * YAFFS: Yet Another Flash File System. A NAND-flash specific file system.
	 *
	 * Copyright (C) 2002-2007 Aleph One Ltd.
	 *   for Toby Churchill Ltd and Brightstar Engineering
	 *
	 * Created by Charles Manning <charles@aleph1.co.uk>
	 *
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License version 2 as
	 * published by the Free Software Foundation.
	 */

	/*
	 * yaffscfg2k.c  The configuration for the "direct" use of yaffs.
	 *
	 * This file is intended to be modified to your requirements.
	 * There is no need to redistribute this file.
	 */

//	#include "yaffscfg.h"
//	#include "yaffsfs.h"
//	#include "yaffs_fileem2k.h"
//	#include "yaffs_nandemul2k.h"

//	#include <errno.h>

	// PORT CONFIGURATION

	public /*unsigned*/ int yaffs_traceMask()
	{
		return
		yportenv.YAFFS_TRACE_SCAN |  
		yportenv.YAFFS_TRACE_GC | yportenv.YAFFS_TRACE_GC_DETAIL | 
		yportenv.YAFFS_TRACE_WRITE  | yportenv.YAFFS_TRACE_ERASE | 
		yportenv.YAFFS_TRACE_TRACING | 
		yportenv.YAFFS_TRACE_ALLOCATE | 
		yportenv.YAFFS_TRACE_CHECKPOINT |
		yportenv.YAFFS_TRACE_BAD_BLOCKS |

		// PORT user configured

//		yportenv.PORT_TRACE_CHECKSUMS |
//		yportenv.PORT_TRACE_TOPLEVEL |
//		yportenv.PORT_TRACE_TALLNESS |
//		yportenv.PORT_TRACE_NANDSIM |
//		yportenv.PORT_TRACE_TNODE |
		yportenv.YAFFS_TRACE_ALWAYS |
//		(~0) |

		0;
	}



	public void yaffsfs_SetError(int err)
	{
		if (err != 0)
		{
			//Do whatever to set error
			Globals.logStream.println("Error number " + err + "!");
//			new UnexpectedException().printStackTrace();
//			new Exception().getStackTrace()[1].getClassName() + ":" +
//			new Exception().getStackTrace()[1].getMethodName() + ":" +
//			new Exception().getStackTrace()[1].getLineNumber());
		}

//		errno = err;
	}

	public void yaffsfs_Lock()
	{
	}

	public void yaffsfs_Unlock()
	{
	}

	public /*__u32*/ int yaffsfs_CurrentTime()
	{
		return 0;
	}

	public void yaffsfs_LocalInitialisation()
	{
		// Define locking semaphore.
	}

//	Configuration for:
//	/ram  2MB ramdisk
//	/boot 2MB boot disk (flash)
//	/flash 14MB flash disk (flash)
//	NB Though /boot and /flash occupy the same physical device they
//	are still disticnt "yaffs_Devices. You may think of these as "partitions"
//	using non-overlapping areas in the same device.


//	#include "yaffs_ramdisk.h"
//	#include "yaffs_flashif.h"
//	#include "yaffs_nandemul2k.h"

//	public static yaffs_Device ramDev = new yaffs_Device();
	public static yaffs_Device device = DebugDevice.getDebugDevice(); // new yaffs_Device(); 
//	public static yaffs_Device flashDev = new yaffs_Device();
//	public static yaffs_Device ram2kDev = new yaffs_Device();

	public yaffsfs_DeviceConfiguration[] yaffsfs_config()
	{
//		#if 0
//		{ "/ram", &ramDev},
//		{ "/boot", &bootDev},
//		{ "/flash/", &flashDev},
//		{ "/ram2k", &ram2kDev},
//		{(void *)0,(void *)0}
//		#else
		return new yaffsfs_DeviceConfiguration[] {
//				new yaffsfs_DeviceConfiguration(Utils.StringToByteArray("/"), 0, ramDev), // XXX bad style for demo
				new yaffsfs_DeviceConfiguration(Utils.StringToByteArray("/"), 0, device),
//				new yaffsfs_DeviceConfiguration(Utils.StringToByteArray("/flash/flash"), 0, flashDev),
//				new yaffsfs_DeviceConfiguration(Utils.StringToByteArray("/ram2k"), 0, ram2kDev),
				new yaffsfs_DeviceConfiguration(null, 0, null) /* Null entry to terminate list */
		};
//		#endif
	};

	public Simulation_yaffscfg2k_C(String serialPort)
	{
		this.serialPort = serialPort;
	}

	protected String serialPort; 
	
	public int yaffs_StartUp()
	{

		// Stuff to configure YAFFS
		// Stuff to initialise anything special (eg lock semaphore).
		yaffsfs_LocalInitialisation();

		// Set up devices

		// PORT remainder already set up in array initialization

		try
		{
			

			SerialInterface serialInterface = new SerialInterface(serialPort);
			Yaffs1NANDInterface stub = new DirectInterfaceClientStub(device, serialInterface.getInputStream(), serialInterface.getOutputStream(), "PC");
			
			device.subField1.writeChunkToNAND = stub;
			device.subField1.readChunkFromNAND = stub;
			device.subField1.eraseBlockInNAND = stub;
			device.subField1.initialiseNAND = stub;

			yaffs2.port.yaffsfs_C.yaffs_initialise(yaffsfs_config());

			return 0;
		}
		catch (Exception e)
		{
			throw new UnexpectedException(e);
		}
	}
}
