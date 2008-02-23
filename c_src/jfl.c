/*
  This file is a part of JOP, the Java Optimized Processor

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
	down.c

	read file for java bytecodes, constant pool and method table
	and download it to ACEX via serial line

	Author: Martin Schoeberl martin@good-ear.com

	2001-10-31	adapted from jop for jop3 (download code in jvm)
	2001-12-01	download 'class file' to external memory
*/

#include <windows.h>
#include <stdio.h>
#include <string.h>

#define DOWN_CNT	16384
#define FLASH_SIZE 0x20000
#define START 0x10000


main(int argc, char *argv[]) {

	unsigned char c;
	int i, j;
	long l;
	int cnt;

	long ram[DOWN_CNT];
	unsigned char mem[FLASH_SIZE];
	long len;
	char inbuf[100];
	int val;
	int data, adr;
	int end;

	DCB dcb;
	HANDLE hCom;
	DWORD dwError;
	BOOL fSuccess;

	FILE *fp;
	char buf[256];
	char *tok;

	if (argc!=2) {
		printf("usage: down file\n");
		exit(-1);
	}

//
//	start JVM
//
//	system("acex wrk\\jop.ttf");

//
//	open serial line after start of JOP
//
	hCom = CreateFile("COM1",
		GENERIC_READ | GENERIC_WRITE,
		0,    /* comm devices must be opened w/exclusive-access */
		NULL, /* no security attrs */
		OPEN_EXISTING, /* comm devices must use OPEN_EXISTING */
		0,    /* not overlapped I/O */
		NULL  /* hTemplate must be NULL for comm devices */
		);

	if (hCom == INVALID_HANDLE_VALUE) {	/* try COM2 */
		hCom = CreateFile("COM2", GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
	}

	if (hCom == INVALID_HANDLE_VALUE) {
		dwError = GetLastError();

		printf("shit CreateFile\n");
		exit(-1);
	}

/*
 * Omit the call to SetupComm to use the default queue sizes.
 * Get the current configuration.
 */

	fSuccess = GetCommState(hCom, &dcb);

	if (!fSuccess) {
		printf("shit GetCommState\n");
		exit(-1);
	}

	dcb.BaudRate = 115200;
	dcb.ByteSize = 8;
	dcb.Parity = NOPARITY;
	dcb.StopBits = ONESTOPBIT;

	/* use RTS/CTS handshake */
	dcb.fOutxCtsFlow = TRUE;
	dcb.fRtsControl = RTS_CONTROL_ENABLE;

	dcb.fOutxDsrFlow = FALSE;
	dcb.fDtrControl = DTR_CONTROL_DISABLE;
	dcb.fDsrSensitivity = FALSE;

	fSuccess = SetCommState(hCom, &dcb);

	if (!fSuccess) {
		printf("shit SetCommState\n");
		exit(-1);
	}

//
//	read file
//
	if ((fp=fopen(argv[1],"r"))==NULL) {
		printf("Error opening %s\n", argv[1]);
		exit(-1);
	}

	len = 0;

	while (fgets(buf, sizeof(buf), fp)!=NULL) {
		for (i=0; i<strlen(buf); ++i) {
			if (buf[i]=='/') {
				buf[i]=0;
				break;
			}
		}
		tok = strtok(buf, " ,\t");
		while (tok!=NULL) {
			if (sscanf(tok, "%ld", &l)==1) {
				ram[len++] = l;
			}
			tok = strtok(NULL, " ,\t");
		}
	}

	fclose(fp);
	printf("%d words of byte code\n", ram[0]);

	for (i=0; i<FLASH_SIZE; ++i) {
		mem[i] = 0xff;
	}
	if (len>DOWN_CNT) {
		printf("len to high (for now) %d\n", len);
		exit(-1);
	}

//
//	copy data to bytes
//
	for (i=0; i<len; ++i) {
		data = ram[i];
		for (j=0; j<4; ++j) {
			mem[START+i*4+j] = data>>((3-j)*8);
		}
	}

	end = i*4+128;

//
//	program flash
//
	for (i=0; i<end; i+=128) {
		sprintf(buf, "a%x\n", START+i);
		wr(hCom, buf);
		for (j=0; j<128; ++j) {
			adr = i+j;
			data = mem[START+adr];
			sprintf(buf, "d%xw", data);
			wr(hCom, buf);
			printf("\r");
		}
		sprintf(buf, "a%xp", START+adr);
		wr(hCom, buf);
		for (;;) {
			wr(hCom, "r");
			ReadFile(hCom, inbuf, 1, &cnt, NULL);
			ReadFile(hCom, inbuf+1, 1, &cnt, NULL);
			inbuf[2] = 0;
			sscanf(inbuf, "%x", &val);
			printf("%02x", val);
			if (val==data) {
				break;
			}
		}
		printf("\n");
	}
	printf("start at %d %x\n", 0x80000+START, 0x80000+START);
	printf("%d %x data\n", i, i);
//
//	compare
//
	sprintf(buf, "a%x\n", START);
	wr(hCom, buf);
	for (i=0; i<end; ++i) {
		wr(hCom, "i");
		printf("%d\r", i);
		ReadFile(hCom, inbuf, 1, &cnt, NULL);
		ReadFile(hCom, inbuf+1, 1, &cnt, NULL);
		inbuf[2] = 0;
		sscanf(inbuf, "%x", &val);
		if (val!=mem[START+i]) {
			printf("wrong data at %x: %02x-%02x\n", START+i, mem[START+i], val);
		}
	}

	fclose(fp);
	CloseHandle(hCom);
}

#define MAX_CNT 10

int wr(HANDLE hCom, char *buf) {

	char inbuf[100];
	int i;
	int cnt;

	inbuf[0] = 0;
	for (i=0; i<strlen(buf); ++i) {
		WriteFile(hCom, buf+i, 1, &cnt, NULL);
		ReadFile(hCom, inbuf, 1, &cnt, NULL); printf("%c", *inbuf);
	}
}
