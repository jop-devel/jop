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
	and download it to JOP via serial line

	Author: Martin Schoeberl martin@good-ear.com

	2001-10-31	adapted from jop for jop3 (download code in jvm)
	2001-12-01	download 'class file' to external memory
*/

#include <windows.h>
#include <stdio.h>
#include <string.h>

// maximum of 1MB
#define MAX_MEM	(1048576/4)

static int prog_cnt = 0;
static char prog_char[] = {'|','/','-','\\','|','/','-','\\'};
static char *exitString = "JVM exit!";

static int echo = 0;
static int usb = 0;

int wr(HANDLE hCom, unsigned char data) {

	DWORD cnt;
//	unsigned char c;

	WriteFile(hCom, &data, 1, &cnt, NULL);
//	printf("%d-", data); fflush(stdout);
	/*if (!usb) {
		ReadFile(hCom, &c, 1, &cnt, NULL);
//		printf("%d ", c); fflush(stdout);
		if (data != c) {
			printf("error during download\n");
			exit(-1);
		}
		if (cnt!=1) {
			printf("timeout during download\n");
			exit(-1);
		}
	}*/

	return 0;
}

int wr32(HANDLE hCom, long data) {

	int j;

	for (j=0; j<4; ++j) {
		wr(hCom, data>>((3-j)*8));
	}

	++prog_cnt;
	if ((prog_cnt&0x3f) == 0) {
		printf("%c\r", prog_char[(prog_cnt>>6)&0x07]);
		fflush(stdout);
	}
	return 0;
}

int main(int argc, char *argv[]) {

	unsigned char c, c_in;
	int i, j;
	long l;

	long *ram;
	long len;

	unsigned char *byt_buf;

	DWORD rdCnt;
	HANDLE hCom;
	DCB dcb;
	COMMTIMEOUTS ctm;
	DWORD dwError;
	BOOL fSuccess;

	FILE *fp;
	char buf[10000];
	char *tok;

	ram = calloc(MAX_MEM, 4);
	if (ram==NULL) {
		printf("error with allocation\n");
		exit(-1);
	}
	byt_buf = malloc(MAX_MEM*4);
	if (byt_buf==NULL) {
		printf("error with allocation\n");
		exit(-1);
	}

	if (argc<3) {
		printf("usage: down [-e] [-usb] file port\n");
		exit(-1);
	}

	if (strcmp(argv[1],"-e")==0 || strcmp(argv[2],"-e")==0) {
		echo = 1;
	}
	if (strcmp(argv[1],"-usb")==0 || strcmp(argv[2],"-usb")==0) {
		usb = 1;
	}

//
//	start JVM
//
//	system("acex wrk\\jop.ttf");

	// com ports > COM9 need a special name!
	sprintf(buf, "\\\\.\\%s",argv[argc-1]); 
//
//	open serial line after start of JOP
//
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

	/* don't use RTS/CTS handshake */
//	dcb.fOutxCtsFlow = TRUE;
//	dcb.fRtsControl = RTS_CONTROL_ENABLE;
	dcb.fOutxCtsFlow = FALSE;
	dcb.fRtsControl = RTS_CONTROL_DISABLE;

	dcb.fOutxDsrFlow = FALSE;
	dcb.fDtrControl = DTR_CONTROL_DISABLE;
	dcb.fDsrSensitivity = FALSE;

	fSuccess = SetCommState(hCom, &dcb);

	if (!fSuccess) {
		printf("shit SetCommState\n");
		exit(-1);
	}

/*
doesn't work
	GetCommTimeouts(hCom,&ctm);
	ctm.ReadIntervalTimeout = 0;
	ctm.ReadTotalTimeoutMultiplier = 0;
	ctm.ReadTotalTimeoutConstant = 100;
	SetCommTimeouts(hCom,&ctm);
and should be changed after download for the echo
*/

//
//	read file
//
	if ((fp=fopen(argv[argc-2],"r"))==NULL) {
		printf("Error opening %s\n", argv[argc-2]);
		exit(-1);
	}

	len = 0;

	//while (len = fgets(buf, sizeof(buf), fp)!=NULL) {
	while (feof(fp)==0) {
			c = fgetc(fp);
			//printf("0x%x, ",c);
			wr(hCom, c);
			//len++;
			//if(len == 4){
				//printf("wait ack\n");
				ReadFile(hCom, &c_in, 1, &rdCnt, NULL);
				//len = 0;
				if(c != c_in) {
					break;//printf("wrong acknowledge: %c \n", c);
					//exit(-1);
				}
			//}
		/*
		tok = strtok(buf, " ,\t");
		while (tok!=NULL) {
			if (sscanf(tok, "%ld", &l)==1) {
				ram[len++] = l;
			}
			tok = strtok(NULL, " ,\t");
			if (len>=MAX_MEM) {
				printf("too many words (%d/%d)\n", len, MAX_MEM);
				exit(-1);
			}
		}*/
	}

	fclose(fp);

/*	for (i=0; i<len; ++i) {
		l = ram[i];
		for (j=0; j<4; ++j) {
			byt_buf[i*4+j] = l>>((3-j)*8);
		}
	}*/
//
//	write external RAM
//
	/*printf("%d words of Java bytecode (%d KB)\n", ram[1]-1, (ram[1]-1)/256);

	if (usb) {
		WriteFile(hCom, byt_buf, len*4, &i, NULL);
		if (i!=len*4) {
			printf("download error %ld %ld\n", len*4, i);
			exit(-1);
		}
	} else {
		for (j=0; j<len; ++j) {
			wr32(hCom, ram[j]);
		}
	}
	printf("%d words external RAM (%d KB)\n", len, len/256);

	printf("download complete\n");
	printf("\n\n");

//
// read serial output of Jop
//
	for (j=0; j<strlen(exitString)+1; ++j) {
		buf[j] = 0;
	}*/
	if (echo) {
		for (;;) {
			ReadFile(hCom, &c, 1, &rdCnt, NULL);	// read without timeout -> kbhit is useless
			printf("%c", c); fflush(stdout);
			for (j=0; j<strlen(exitString)-1; ++j) {
				buf[j] = buf[j+1];
			}
			buf[strlen(exitString)-1] = c;
			if (strcmp(buf, exitString)==0) {
				break;
			}
			//printf("'%c' %d\n", c, c); fflush(stdout);
		}
	}
/*
*/

	CloseHandle(hCom);

	return 0;
}

