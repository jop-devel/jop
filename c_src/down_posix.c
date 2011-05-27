#include <stdlib.h>
#include <limits.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <string.h>
#include <stdint.h>

#include <sys/mman.h>
#include <sys/stat.h>

// maximum of 1MB
#define MAX_MEM	(1048576/4)

static int prog_cnt = 0;
static char prog_char[] = {'|','/','-','\\','|','/','-','\\'};
static char *exitString = "JVM exit!";

static int echo = 0;
static int usb = 0;
static int cont = 0;

int serial_open(char *fname) {
	struct termios opts;
	int fd;
	
	fd = open(fname, O_RDWR);
	if (fd < 0) {
		perror("Error opening serial port");
		exit(1);
	}
	
	// get current port options
	if (tcgetattr(fd, &opts)) {
		perror("Error getting serial options");
		exit(1);
	}
	
	// set baud rate
	cfsetispeed(&opts, B115200);
	cfsetospeed(&opts, B115200);
	
	// local port, enable receiver
	opts.c_cflag |= (CLOCAL | CREAD);
	
	// set to 8-bit mode
	opts.c_cflag &= ~CSIZE;
	opts.c_cflag |= CS8;
	
	// no parity, 1 stop bit
	opts.c_cflag &= ~(PARENB | CSTOPB);
	
	// disable hardware flow control if available
#ifdef CNEW_RTSCTS
	opts.c_cflag &= ~CNEW_RTSCTS;
#endif

	// disable any processing; full raw mode
	opts.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
	opts.c_oflag &= ~OPOST;
	
	// no software flow control either
	opts.c_iflag &= ~(IXON | IXOFF | IXANY);
	
	if (tcsetattr(fd, TCSANOW, &opts)) {
		perror("Error setting serial options");
		exit(1);
	}
	
	return fd;
}

void write32_check(int fd, unsigned long data) {
	int j;
	ssize_t c;
	unsigned char send[4];
	unsigned char echo[4];

	for (j=0; j<4; ++j) {
		send[j] = data>>((3-j)*8);
	}
	
	c = 0;
	while (c < 4)
		c += write(fd, send+c, 4-c);
	c = 0;
	while (c < 4)
		c += read(fd, echo+c, 4-c);

	for (j=0; j<4; ++j) {
		if (echo[j] != send[j]) {
			if (cont) {
				fprintf(stderr, "\necho error, continuing\n");
			} else {
				fprintf(stderr, "\nsent: %d, received: %d\n", send[j], echo[j]);
				fprintf(stderr, "\nRemote end did not echo char correctly\n");
				exit(-1);
			}
		}
	}
}

int is_number(char *p) {
	return ((*p >= '0' && *p <= '9') || 
		(*p == 'x' && *(p-1) == '0') ||
		(*p == '-' && (*(p+1) >= '0' && *(p+1) <= '9')) || 
		(*p == '+' && (*(p+1) >= '0' && *(p+1) <= '9')));
}

void parse_text(int64_t *array, int64_t *len, char *ptr, off_t maxSize)
{
	char *p = ptr;
	char *numStackBase = malloc(64);
	char *numStack;
	
	*len = 0;
	while ((p - ptr) < maxSize) {
		if (is_number(p)) {
			long num;
			
			numStack = numStackBase;
			while (is_number(p) && (p - ptr) < maxSize) {
				*(numStack++) = *(p++);
			}
			
			// now pointing at a non-number
			*numStack = 0;
			num = strtol(numStackBase, 0, 0);
			array[(*len)++] = num;
			
		} else if (*p == '/' && *(p+1) == '/') {
			// skip until a newline
			while (*p != '\n' && (p - ptr) < maxSize)
				p++;
		} else if (*p == '/' && *(p+1) == '*') {
			// skip until comment end
			while (!(*p == '*' && *(p+1) == '/') && (p - ptr) < maxSize)
				p++;
		} else {
			p++;
		}
	}
}

void print_progress(long done, long total)
{
	int hashCount = (done * 60)/total;
	int i;
	fprintf(stderr, " [");
	for (i = 0; i < hashCount; i++)
		fprintf(stderr, "#");
	for (; i < 60; i++)
		fprintf(stderr, " ");
	fprintf(stderr, "] %ld / %ld\r", done, total);
}

int main(int argc, char *argv[])
{
	unsigned char c;
	int i, j;
	int64_t l;
	
	int fdSerial;
	int fdFile;

	int64_t *ram;
	int64_t len;

	uint8_t *byt_buf;
	uint8_t *buf;
	
	char *ptr;
	struct stat statbuf;
	
	// allocate arrays
	ram = calloc(MAX_MEM, sizeof(int64_t));
	if (ram==NULL) {
		printf("error with allocation\n");
		exit(-1);
	}
	
	buf = calloc(10000, sizeof(char));
	if (buf == NULL) {
		fprintf(stderr, "Could not allocate memory\n");
		exit(-1);
	}
	
	byt_buf = calloc(MAX_MEM, sizeof(int64_t));
	if (byt_buf==NULL) {
		printf("error with allocation\n");
		exit(-1);
	}

	// process arguments
	if (argc<3) {
		fprintf(stderr, "usage: down [-e] [-c] [-usb] file port\n");
		exit(-1);
	}

	for (i=1; i<argc-2; i++) {
		if (strcmp(argv[i], "-e") == 0)
			echo = 1;
		else if (strcmp(argv[i], "-usb") == 0)
			usb = 1;
		else if (strcmp(argv[i], "-c") == 0)
			cont = 1;
	}

	// open word file and map it into memory
	fdFile = open(argv[argc-2], O_RDONLY);
	if (fdFile < 0) {
		perror("Error opening file");
		exit(-1);
	}
	
	if (fstat(fdFile, &statbuf)) {
		perror("Error stat'ing file");
		exit(-1);
	}
	
	ptr = mmap(0, statbuf.st_size, PROT_READ, MAP_PRIVATE, fdFile, 0);
	if (ptr == MAP_FAILED) {
		perror("Error mapping file");
		exit(-1);
	}
	
	// now parse the text data and build the mem array
	parse_text(ram, &len, ptr, statbuf.st_size);
	
	if (ram[0] != len) {
		fprintf(stderr, "JOP header says length = %ld, parsed length = %ld\n", (long int)ram[0], (long int)len);
		fprintf(stderr, "Maybe a bad JOP file?");
		exit(-1);
	}
	
	fprintf(stdout, "Parsed JOP file ok.\n");
	
	munmap(ptr, statbuf.st_size);
	close(fdFile);

	for (i=0; i<len; ++i) {
		l = ram[i];
		for (j=0; j<4; ++j) {
			byt_buf[i*4+j] = l>>((3-j)*8);
		}
	}
//
//	write external RAM
//
	// open serial port
	fdSerial = serial_open(argv[argc-1]);

	printf("* %ld words of Java bytecode (%ld KB)\n", 
		(long int)ram[1]-1, (long int)(ram[1]-1)/256);
	printf("* %ld words external RAM (%ld KB)\n", (long int)len, (long int)len/256);
	
	fprintf(stdout, "\nTransmitting data via serial...\n");

	if (usb) {
		ssize_t count = 0;
		ssize_t max = len * 4;
		while (count < max) {
			print_progress(count/4, max/4);
			count += write(fdSerial, byt_buf + count, (max - count > 128) ? 128 : max-count);
		}
		fprintf(stderr, "\nDone.\n");
	} else {
		for (j=0; j<len; ++j) {
			if (j % 128 == 0)
				print_progress(j, len);
			write32_check(fdSerial, ram[j]);
		}
		fprintf(stderr, "\nDone.\n");
	}

//
// read serial output of Jop
//
	for (j=0; j<strlen(exitString)+1; ++j) {
		buf[j] = 0;
	}
	if (echo) {
		long rdCnt;
		for (;;) {
		        if(read(fdSerial, &c, 1)<=0) continue;
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

	close(fdSerial);

	return 0;
}
