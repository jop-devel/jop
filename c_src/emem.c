
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
// read serial output of Jop
//
j=0;
	for (;;) {

printf("\n%04x ", j++); fflush(stdout);	// write four bytes
for (k=0; k<4; ++k) {
		ReadFile(hCom, &c, 1, &i, NULL);	// read without timeout -> kbhit is useless
		// printf("%04x %02x\n", j++, c); fflush(stdout);
printf(" %02x", c); fflush(stdout);	// write four bytes
}
		// printf("'%c' %d\n", c, c); fflush(stdout);
		// printf("%c", c);
	}

	CloseHandle(hCom);
}

