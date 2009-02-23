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
	amd.c

	for jopcore board with 512kB flash (AM29LV040B)

	read ttf and write in upper part of flash for config
	confppa2 uses a0-a16, a17 and a18 are pulled up by ACEX

	read .jop and write to flash
	read .html and write to flash

	Mem.java must be running on JOP

	Author: Martin Schoeberl martin@good-ear.com

*		.ttf	ACEX config start at 0x60000
*				Cyclone config start at 0x40000
*		.jop	Java files start at 0x18000		(history of BB project)
*		.html	start at 0x00000

*/

#include <windows.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>

#define MAX_ACEX 32768*3		// 96kB (acex 1k50)

#define FLASH_SIZE 0x80000
#define SECTOR_SIZE 0x10000
#define CONFIG_START 0x60000
#define CONFIG_CYC_START 0x40000
// #define JAVA_START 0x10000 old ACEX configuration
#define JAVA_START 0x00000
#define JAVA_CNT	16384
#define HTML_START 0x10000

HANDLE hCom;

main(int argc, char *argv[]) {

	char c;
	int i, j;
	char inbuf[100];
	int cnt;

	unsigned char mem[FLASH_SIZE];

	FILE *fp;
	int fn;
	char buf[1000];
	unsigned char *cp;
	int s_type, s_len, s_a, s_d;
	int val;
	int data, adr;
	int end;

	DCB dcb;
	DWORD dwError;
	BOOL fSuccess;


	if (argc!=3) {
		printf("usage: amd file port\n");
		exit(-1);
	}
	if ((fp=fopen(argv[1],"r"))==NULL) {
		printf("Error opening %s\n", argv[1]);
		exit(-1);
	}

	// com ports > COM9 need a special name!
	sprintf(buf, "\\\\.\\%s",argv[argc-1]); 

	hCom = CreateFile(buf,
		GENERIC_READ | GENERIC_WRITE,
		0,    /* comm devices must be opened w/exclusive-access */
		NULL, /* no security attrs */
		OPEN_EXISTING, /* comm devices must use OPEN_EXISTING */
		0,    /* not overlapped I/O */
		NULL  /* hTemplate must be NULL for comm devices */
		);

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

	for (i=0; i<FLASH_SIZE; ++i) {
		mem[i] = 0xff;
	}

	for (i = strlen(argv[1]); i>0 && argv[1][i]!='.'; --i)
		;
	

	// FPGA config file
	if (strcmp(argv[1]+i, ".ttf")==0) {

		for (i=0; fscanf(fp, "%d", &val)==1; ++i) {
			mem[i] = val;
			fscanf(fp, ",");
		}
		fclose(fp);
		// file size does not work anymore to distiguish
		// between Cyclone and ACEX as the Cyclone configuration
		// is compressed.
//		if (i>MAX_ACEX) {							// file is big, so it must be for Cyclone
			program(mem, CONFIG_CYC_START, i);
			compare(mem, CONFIG_CYC_START, i);
//		} else {
//			program(mem, CONFIG_START, i);
//			compare(mem, CONFIG_START, i);
//		}

	// Java file
	} else if (strcmp(argv[1]+i, ".jop")==0) {

		i = readBin(fp, mem);
		program(mem, JAVA_START, i);
		compare(mem, JAVA_START, i);

	// Html file
	} else if (strcmp(argv[1]+i, ".html")==0) {

		fclose(fp);
		fn = open(argv[1], O_RDONLY | O_BINARY);
		i = read(fn, mem, sizeof(mem));
		close(fn);
		mem[i] = 0;			// EOF
		++i;
		if (i>=FLASH_SIZE) {
			printf("file to big (%d)\n", i);
			exit(-1);
		}
		program(mem, HTML_START, i);
		compare(mem, HTML_START, i);

	} else {
		printf("unknown file type\n");
	}

	CloseHandle(hCom);
}

int program(unsigned char *mem, int addr, int len) {

	int i;

	for (i=0; i<len; i+=SECTOR_SIZE) {
		eraseSector(addr+i);
	}

	setAddr(addr);
	
	for (i=0; i<len; ++i) {
		if (wr_fast(mem[i])!=mem[i]) {
			wr(addr+i, mem[i]);
		} else {
			printf("%d   \r", i);
		}
	}

	printf("\n%d %x bytes data\n", i, i);

/*  slow version

	for (i=0; i<len; ++i) {
		wr(addr+i, mem[i]);
		printf("            \r");
	}
*/

	return 0;
}

int compare(unsigned char *mem, int addr, int len) {

	char buf[100];
	int i;
	int cnt;
	int trycnt;
	unsigned val;

	setAddr(addr);
	
	for (i=0; i<len; ++i) {
		printf("%d", i);
		wrCmd("i");
		val = readVal();
		printf("\r");
		if (val!=mem[i]) {
			printf("wrong data at %x: %02x-%02x\n", i, mem[i], val);
		}
	}
	printf("\ncompare ok\n");
	return 0;
}


int wrCmd(char *buf) {

	char inbuf[100];
	int i;
	int cnt;

	inbuf[0] = 0;
	for (i=0; i<strlen(buf); ++i) {
		WriteFile(hCom, buf+i, 1, &cnt, NULL);
		ReadFile(hCom, inbuf, 1, &cnt, NULL); printf("%c", *inbuf);
	}
	return 0;
}

int readVal() {

	char inbuf[3];
	int cnt;
	unsigned val;

	ReadFile(hCom, inbuf, 1, &cnt, NULL);
	if (cnt!=1) {
		printf("Error in readVal()\n");
		exit(-1);
	}
	ReadFile(hCom, inbuf+1, 1, &cnt, NULL);
	if (cnt!=1) {
		printf("Error in readVal()\n");
		exit(-1);
	}
	inbuf[2] = 0;
	sscanf(inbuf, "%x", &val);
	printf("%02x", val);

	return val;
}

#define MAX_CNT 50

int wr(int adr,int data) {

	char buf[100];
	int i;
	int cnt;
	int trycnt;
	unsigned val;

	sprintf(buf, "a%x d%xm", adr, data);
	wrCmd(buf);
	for (trycnt=0; trycnt<MAX_CNT; ++trycnt) {
		wrCmd("r");
		val = readVal();
		if (val==data) {
			break;
		}
	}
	if (trycnt==MAX_CNT) {
		printf("error programing flash\n");
		exit(-1);
	}

	return 0;
}


int wr_fast(int data) {

	unsigned char buf[100];
	unsigned char inbuf[100];
	int i;
	int cnt;
	int trycnt;
	unsigned val;

	inbuf[0] = 0;
	buf[0] = '!';
	buf[1] = data;
	WriteFile(hCom, buf, 1, &cnt, NULL);
	ReadFile(hCom, inbuf, 1, &cnt, NULL);
	WriteFile(hCom, buf+1, 1, &cnt, NULL);
	ReadFile(hCom, inbuf, 1, &cnt, NULL);
	return *inbuf;
}


int eraseChip() {

	char buf[100];
	unsigned val;

	wrCmd("x");
	for (;;) {
		wrCmd("r");
		val = readVal();
		if (val==0x0ff) {
			break;
		}
	}
	printf("\n");

	return 0;
}

int eraseSector(int addr) {

	char buf[100];
	unsigned val;

	sprintf(buf, "a%x s", addr);
	wrCmd(buf);
	printf("\n");
	for (;;) {
		wrCmd("r");
		val = readVal();
		printf("\r");
		if (val==0x0ff) {
			break;
		}
	}
	printf("\n");

	return 0;
}

int setAddr(int addr) {

	char buf[100];
	unsigned val;

	sprintf(buf, "a%x ", addr);
	wrCmd(buf);
	printf("\n");

	return 0;
}

int readBin(FILE *fp, unsigned char *mem) {

	unsigned char c;
	int i, j;
	long l;

	long ram[JAVA_CNT];
	long len;
	int data;

	char buf[256];
	char *tok;

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

	if (len>JAVA_CNT) {
		printf("len to high (for now) %d\n", len);
		exit(-1);
	}

//
//	copy data to bytes
//
	for (i=0; i<len; ++i) {
		data = ram[i];
		for (j=0; j<4; ++j) {
			mem[i*4+j] = data>>((3-j)*8);
		}
	}

	return len*4;
}
