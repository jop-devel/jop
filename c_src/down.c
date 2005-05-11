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

// #include "/usr/include/ecpio.h"

#define DOWN_CNT	16384

static int prog_cnt = 0;
static char prog_char[] = {'|','/','-','\\','|','/','-','\\'};

main(int argc, char *argv[]) {

	unsigned char c;
	int i, j;
	long l;

	long ram[DOWN_CNT];
	long len;

	HANDLE hCom;
	DCB dcb;
	COMMTIMEOUTS ctm;
	DWORD dwError;
	BOOL fSuccess;

	FILE *fp;
	char buf[10000];
	char *tok;

	if (argc<3) {
		printf("usage: down [-e] file port\n");
		exit(-1);
	}

//
//	start JVM
//
//	system("acex wrk\\jop.ttf");

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
			if (len>=DOWN_CNT) {
				printf("too many words (%d/%d)\n", len, DOWN_CNT);
				exit(-1);
			}
		}
	}

	fclose(fp);

//
//	write external RAM
//
	printf("%d words of Java bytecode (%d KB)\n", ram[0], ram[0]/256);

//	wr32(hCom, len);
	for (j=0; j<len; ++j) {
		wr32(hCom, ram[j]);
	}
	printf("%d words external RAM (%d KB)\n", j, j/256);
	for (; j<DOWN_CNT; ++j) {
		wr32(hCom, 0);
	}

	printf("download complete\n");
	printf("\n\n");

//
// read ECP output of Jop
//
/*
	printf("reading ECP: don't stop with CTRL^C!!!\n");
	echo_ecp();
*/
//
// read serial output of Jop
//
	if (argc!=3) {
		for (;;) {
			ReadFile(hCom, &c, 1, &i, NULL);	// read without timeout -> kbhit is useless
			printf("%c", c); fflush(stdout);
			//printf("'%c' %d\n", c, c); fflush(stdout);
		}
	}
/*
*/

	CloseHandle(hCom);
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

int wr(HANDLE hCom, unsigned char data) {

	int cnt;
	unsigned char c;

	WriteFile(hCom, &data, 1, &cnt, NULL);
//	printf("%d-", data); fflush(stdout);
	ReadFile(hCom, &c, 1, &cnt, NULL);
//	printf("%d ", c); fflush(stdout);
	if (data != c) {
		printf("error during download\n");
		exit(-1);
	}
	if (cnt!=1) {
		printf("timeout during download\n");
		exit(-1);
	}


	return 0;
}

/*
	 not used in bb project

int start_ecp() {

	ecp_ecr_ps2();			// bidir. mode
	if (!ecp_negotiate()) {
		printf( "error in negotiation\n" );
		return 0;
	}
	ecp_ecr_ecp();			// switch to ecp mode
	ecp_periph2pc();		// disable output, revers request
}
#define BUF_LEN 256
int echo_ecp() {

	int i, j;
	unsigned char buf[BUF_LEN];

	start_ecp();			// causes delay in EcpPrime

	while (!kbhit()) {

		if (i = ecp_read( buf, BUF_LEN)) {
			for (j=0; j<i; ++j) {
				printf("%c", buf[j] );
			}
		}
	}


	ecp_pc2periph();				// end reverse, enable output
	ecp_ecr_ps2();					// back to bidir. mode
	ecp_terminate();				// and set c3 to 0
}
*/
