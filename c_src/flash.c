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
	flash.c

	read ttf and write in upper half of flash for config

	Mem.java must be running on JOP


**********
	read file for flash (Motorola S records) and write flash
	attached to JOP3

	Author: Martin Schoeberl martin@good-ear.com

*/

#include <windows.h>
#include <stdio.h>
#include <string.h>

#define FLASH_SIZE 0x20000

main(int argc, char *argv[]) {

	char c;
	int i, j;
	char inbuf[100];
	int cnt;

	unsigned char mem[2*FLASH_SIZE];

	FILE *fp;
	char buf[1000];
	unsigned char *cp;
	int s_type, s_len, s_a, s_d;
	int val;
	int data, adr;
	int end;

	DCB dcb;
	HANDLE hCom;
	DWORD dwError;
	BOOL fSuccess;


	if (argc!=2) {
		printf("usage: flash file\n");
		exit(-1);
	}
	if ((fp=fopen(argv[1],"r"))==NULL) {
		printf("Error opening %s\n", argv[1]);
		exit(-1);
	}

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

//	config via Max+ (jopbb)
//	system("acex ttf\\flash.ttf");

	for (i=0; i<2*FLASH_SIZE; ++i) {
		mem[i] = 0xff;
	}

/******** read S format ***********
	for( ; cp = fgets(buf, sizeof(buf), fp); cp!=NULL) {
		if (*cp++!='S') { printf("no 'S'\n"); exit(-1); }
		s_type = *cp++;
		sscanf(cp,"%2x", &s_len);
		s_len -= 1;
		cp +=2;
		switch (s_type) {
			case '0':
			case '1':
			case '9':
				sscanf(cp,"%4x", &s_a);
				cp += 4;
				s_len -= 2;
				break;
			case '2':
				sscanf(cp,"%6x", &s_a);
				cp += 6;
				s_len -= 3;
				break;
			case '3':
				sscanf(cp,"%8x", &s_a);
				cp += 8;
				s_len -= 4;
				break;
			case '5':
				break;
			case '7':
				break;
			default:
				printf("not a valid S-record\n");
				exit(-1);
				break;
		}
		switch (s_type) {
			case '0':
				for (i=0; i<s_len; ++i) {
					sscanf(cp, "%2x", &s_d);
					printf("%c", s_d);
					cp += 2;
				}
				printf("\n");
				break;
			case '1':
			case '2':
			case '3':
//				printf("%04x ", s_a);
				for (i=0; i<s_len; ++i) {
					sscanf(cp, "%2x", &s_d);
//					printf("%02x ", s_d);
					mem[s_a++] = s_d;
					cp += 2;
				}
//				printf("\n");
				break;
			case '9':
				printf("start address: %04x\n", s_a);
				break;
			default:
				printf("typ %c ???\n", s_type);
				break;
		}
	}
**************** end read S format **********/


/********* read ttf ***********/


	for ( i=0; fscanf(fp, "%d", &val)==1; ++i) {
		mem[i] = val;
		fscanf(fp, ",");
	}
	fclose(fp);
	end = i+128;

/********* end read ttf ***********/

//
//	program flash
//
	for (i=0; i<end; i+=128) {
		sprintf(buf, "a%x\n", i);
		wr(hCom, buf);
		for (j=0; j<128; ++j) {
			adr = i+j;
			data = mem[adr];
			sprintf(buf, "d%xw", data);
			wr(hCom, buf);
			printf("\r");
		}
		sprintf(buf, "a%xp", adr);
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
	printf("%d %x data\n", i, i);
//
//	compare
//
	wr(hCom, "a0 ");
	for (i=0; i<end; ++i) {
		wr(hCom, "i");
		printf("%d\r", i);
		ReadFile(hCom, inbuf, 1, &cnt, NULL);
		ReadFile(hCom, inbuf+1, 1, &cnt, NULL);
		inbuf[2] = 0;
		sscanf(inbuf, "%x", &val);
		if (val!=mem[i]) {
			printf("wrong data at %x: %02x-%02x\n", i, mem[i], val);
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
