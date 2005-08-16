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

int wr(HANDLE hCom, unsigned char data) {

	DWORD cnt;
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

	unsigned char c;
	int i, j;
	long l;

	long *ram;
	long len;

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
			if (len>=MAX_MEM) {
				printf("too many words (%d/%d)\n", len, MAX_MEM);
				exit(-1);
			}
		}
	}

	fclose(fp);

//
//	write external RAM
//
	printf("%d words of Java bytecode (%d KB)\n", ram[1]-1, (ram[1]-1)/256);

	for (j=0; j<len; ++j) {
		wr32(hCom, ram[j]);
	}
	printf("%d words external RAM (%d KB)\n", j, j/256);

	printf("download complete\n");
	printf("\n\n");

//
// read serial output of Jop
//
	for (j=0; j<strlen(exitString)+1; ++j) {
		buf[j] = 0;
	}
	if (argc!=3) {
		DWORD rdCnt;
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

