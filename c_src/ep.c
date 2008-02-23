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


#include <windows.h>
#include <stdio.h>
#include <string.h>


main(int argc, char *argv[]) {

	unsigned char c;
	int i, j, k;
	long l;

	DCB dcb;
	HANDLE hCom;
	DWORD dwError;
	BOOL fSuccess;

	FILE *fp;
	char buf[256];
	char *tok;

	if (argc<2) {
		printf("usage: e port\n");
		exit(-1);
	}

//
//	open serial line after start of JOP
//
	hCom = CreateFile(argv[argc-1],
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
// read serial line
//
	for (;;) {

		ReadFile(hCom, &c, 1, &i, NULL);	// read without timeout -> kbhit is useless
		printf(" %02x", c); fflush(stdout);	// write four bytes
		if (c==0x7e) printf("\n");
	}

	CloseHandle(hCom);
}

