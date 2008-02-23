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
	t.c	test serial line (echo back)

*/

#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#define STEP 80

main(int argc, char *argv[]) {

	unsigned char c;
	int i, j;
	int cnt;

	DCB dcb;
	COMMTIMEOUTS ctim;
	HANDLE hCom;
	DWORD dwError;
	BOOL fSuccess;

	int cc, mc, wc;
	clock_t start, end;
	double d;

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
/*
	if (!SetupComm(hCom, 10000, 10000)) {		// buffer sizes
		printf("shit SetupComm\n");
		exit(-1);
	}
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

	if (!GetCommTimeouts(hCom, &ctim)) {
		printf("shit GetCommTimeouts\n");
		exit(-1);
	}
	ctim.ReadTotalTimeoutConstant = 20;
	if (!SetCommTimeouts(hCom, &ctim)) {
		printf("shit SetCommTimeouts\n");
		exit(-1);
	}

	start = clock();

	cc = mc = wc = 0;
	j = '0';
	for (;!kbhit();) {
		cc += STEP;
		for (i=0; i<STEP; ++i) {
			c = j+i;
			WriteFile(hCom, &c, 1, &cnt, NULL);
		}
		for (i=0; i<STEP; ++i) {
			ReadFile(hCom, &c, 1, &cnt, NULL);
			if (cnt==1) {
				printf("%c", c); fflush(stdout);
				if (c!=j+i) {
					++wc;
				}
			} else {
				printf("."); fflush(stdout);
				++mc;
			}
		}
		j += STEP;
		if (j>'0'+79) j = '0';
	}
	end = clock();

	d = end - start;
	d /= 1000;

	printf("\n\ntime: %lgs\n", d);
	printf("%d characters\n", cc);
	printf("%d ch/s\n", (int) (cc/d) );
	printf("%d missed\n", mc);
	printf("%d%% missed\n", mc*100/cc);
	printf("%d wrong\n", wc);
	printf("%d%% error\n", (mc+wc)*100/cc);

	CloseHandle(hCom);
}
