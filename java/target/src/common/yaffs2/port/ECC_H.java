package yaffs2.port;

abstract public class ECC_H
{
	/*
	 * YAFFS: Yet another Flash File System . A NAND-flash specific file system. 
	 *
	 * Copyright (C) 2002-2007 Aleph One Ltd.
	 *   for Toby Churchill Ltd and Brightstar Engineering
	 *
	 * Created by Charles Manning <charles@aleph1.co.uk>
	 *
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU Lesser General Public License version 2.1 as
	 * published by the Free Software Foundation.
	 *
	 * Note: Only YAFFS headers are LGPL, YAFFS C code is covered by GPL.
	 */

	 /*
	  * This code implements the ECC algorithm used in SmartMedia.
	  *
	  * The ECC comprises 22 bits of parity information and is stuffed into 3 bytes. 
	  * The two unused bit are set to 1.
	  * The ECC can correct single bit errors in a 256-byte page of data. Thus, two such ECC 
	  * blocks are used on a 512-byte NAND page.
	  *
	  */

	/**
	 * @param data const unsigned char *
	 * @param ecc unsigned char *
	 */
	abstract void yaffs_ECCCalculate(byte[] data, byte[] ecc);

	/**
	 * @param data unsigned char *
	 * @param read_ecc unsigned char *
	 * @param test_ecc const unsigned char *
	 * @return
	 */
	abstract int yaffs_ECCCorrect(byte[] data, byte[] read_ecc,
			     byte[] test_ecc);

	/**
	 * @param data const unsigned char
	 * @param nBytes unsigned
	 * @param ecc yaffs_ECCOther * 
	 */
	abstract void yaffs_ECCCalculateOther(byte[] data, long nBytes,
				     yaffs_ECCOther ecc);
	/**
	 * @param data unsigned char *
	 * @param nBytes unsigned
	 * @param read_ecc yaffs_ECCOther *
	 * @param test_ecc const yaffs_ECCOther *  
	 * @return
	 */
	abstract int yaffs_ECCCorrectOther(byte[] data, long nBytes,
				  yaffs_ECCOther read_ecc,
				  yaffs_ECCOther test_ecc);

}
