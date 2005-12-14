/******************************************************************/
/*                                                                */
/* Module:       jb_io.c                                          */
/*                                                                */
/* Descriptions: Manages I/O related routines, file and string    */
/*               processing functions.                            */
/*                                                                */
/* Revisions:    .0 02/22/02                                      */
/*               .1 04/11/02                                      */
/*               A new function, jb_grabdata() has been added     */
/*               .2 07/16/03                                      */
/*               VerifyHardware() function has been splitted into */
/*               two functions, VerifyBBII() and VerifyBBMV()     */
/*               .3 07/01/2004                                    */
/*                                                                */
/******************************************************************/

#include <windows.h>
#include <stdio.h>
#include "jb_io.h"
#include "Ftd2xx.h"
#include "header.h"
#include "jb_jtag.h" 

#if PORT==EMBEDDED
unsigned char byteBuffer [10240] = {0};
extern int sendLastBuffer;
#endif

#if PORT==WINDOWS_NT
#include <windows.h>
#endif /* PORT==WINDOWS_NT */

#if PORT==WINDOWS_NT
#define	PGDC_IOCTL_GET_DEVICE_INFO_PP   0x00166A00L
#define PGDC_IOCTL_READ_PORT_PP         0x00166A04L
#define PGDC_IOCTL_WRITE_PORT_PP        0x0016AA08L
#define PGDC_IOCTL_PROCESS_LIST_PP      0x0016AA1CL
#define PGDC_WRITE_PORT                 0x0a82
#define PGDC_HDLC_NTDRIVER_VERSION      2
#define PORT_IO_BUFFER_SIZE             256
#endif /* PORT==WINDOWS_NT */

#define MAX_FILE_LINE_LENGTH            160

#if PORT==WINDOWS_NT
HANDLE  nt_device_handle     = INVALID_HANDLE_VALUE;
int     port_io_buffer_count = 0;

struct PORT_IO_LIST_STRUCT
{
	USHORT command;
	USHORT data;
} port_io_buffer[PORT_IO_BUFFER_SIZE];
#endif /* PORT==WINDOWS_NT */


#if PORT==WINDOWS_NT
/******************************************************************/
/* Name:         InitNtDriver                                     */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                          */
/* Descriptions: Initialize Windows NT Driver for ByteBlasterMV.  */
/*                                                                */
/******************************************************************/
void InitNtDriver()
{
	int init_ok = 0;	/* Initialization OK */

	ULONG buffer[1];
	ULONG returned_length = 0;
	char nt_lpt_str[] = { '\\', '\\', '.', '\\', 'A', 'L', 'T', 'L', 'P', 'T', '1', '\0' }; 

	nt_device_handle = CreateFile( 
			nt_lpt_str,
			GENERIC_READ | GENERIC_WRITE,
			0,
			NULL,
			OPEN_EXISTING,
			FILE_ATTRIBUTE_NORMAL,
			NULL );

	if ( nt_device_handle == INVALID_HANDLE_VALUE )
		fprintf( stderr, "I/O Error: Cannot open device \"%s\"\n", nt_lpt_str );
	else
	{
		if ( DeviceIoControl(
				nt_device_handle,
				PGDC_IOCTL_GET_DEVICE_INFO_PP,
				(ULONG *) NULL,
				0,
				&buffer,
				sizeof(ULONG),
				&returned_length,
				NULL ))
		{
			if ( returned_length == sizeof( ULONG ) )
			{
				if (buffer[0] == PGDC_HDLC_NTDRIVER_VERSION)
				{
					init_ok = 1;
				}
				else
				{
					fprintf(stderr,
						"I/O Error:  device driver %s is not compatible\n(Driver version is %lu, expected version %lu.\n",
						nt_lpt_str,
						(unsigned long) buffer[0],
						(unsigned long) PGDC_HDLC_NTDRIVER_VERSION );
				}
			}	
			else
				fprintf(stderr, "I/O Error:  device driver %s is not compatible.\n", nt_lpt_str);		
		}

		if ( !init_ok )
		{
			fprintf( stderr, "I/O Error: DeviceIoControl not successful" );
			CloseHandle( nt_device_handle );
			nt_device_handle = INVALID_HANDLE_VALUE;
		}
	}

	if ( !init_ok )
	{
		fprintf( stderr, "Error: Driver initialization fail... Exiting...\n" );
		CloseNtDriver();
		exit(1);
	}
}

/******************************************************************/
/* Name:         CloseNtDriver                                    */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                          */
/* Descriptions: Close Windows NT Driver.                         */
/*                                                                */
/******************************************************************/
void CloseNtDriver()
{
	CloseHandle( nt_device_handle );
	nt_device_handle = INVALID_HANDLE_VALUE;
}

/******************************************************************/
/* Name:         flush_ports                                      */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                          */
/* Descriptions: Flush processes in port_io_buffer and reset      */
/*               buffer size to 0.                                */
/*                                                                */
/******************************************************************/
void flush_ports(void)
{
	ULONG n_writes = 0L;
	BOOL status;

	status = DeviceIoControl(
		nt_device_handle,			/* handle to device */
		PGDC_IOCTL_PROCESS_LIST_PP,	/* IO control code */
		(LPVOID)port_io_buffer,		/* IN buffer (list buffer) */
		port_io_buffer_count * sizeof(struct PORT_IO_LIST_STRUCT),/* length of IN buffer in bytes */
		(LPVOID)port_io_buffer,	/* OUT buffer (list buffer) */
		port_io_buffer_count * sizeof(struct PORT_IO_LIST_STRUCT),/* length of OUT buffer in bytes */
		&n_writes,					/* number of writes performed */
		0);							/* wait for operation to complete */

	if ((!status) || ((port_io_buffer_count * sizeof(struct PORT_IO_LIST_STRUCT)) != n_writes))
	{
		fprintf(stderr, "I/O Error:  Cannot access ByteBlaster hardware\n");
		CloseHandle(nt_device_handle);
		exit(1);
	}

	port_io_buffer_count = 0;
}

/******************************************************************/
/* Name:         VerifyBBII (ByteBlaster II)					  */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: '0' if verification is successful;'1' if not.    */
/*               		                                          */
/* Descriptions: Verify if ByteBlaster II is properly attached to */
/*               the parallel port.                               */
/*                                                                */
/******************************************************************/
int VerifyBBII()
{
	int error = 0;
	int test_count = 0;
	int read_data = 0;
	
	for ( test_count = 0; test_count < 2; test_count++ )
	{
		/* Write '0' to Pin 6 (Data4) for the first test and '1' for the second test */
		int vector = (test_count) ? 0x10 : 0x0;/* 0001 0000:0000 0000... drive to Port0 */
		int expect = (test_count) ? 0x40 : 0x0;/* X1XX 0XXX:X0XX 0XXX... expect from Port1 */

		WritePort( PORT_0, vector, BUFFER_OFF );
		
		/* Expect '0' at Pin 10 (Ack) and Pin 15 (Error) for the first test */
		/* and '1' at Pin 10 (Ack) and '0' Pin 15 (Error) for the second test */
		read_data = ReadPort( PORT_1 ) & 0x40;

		/* If no ByteBlaster II detected, error = 1 */
		if (test_count==0)
		{
			if(read_data==0x00)
				error=0;
			else error=1;
		}

		if (test_count==1)
		{
			if(read_data==0x40)
				error=error|0;
			else error=1;
		}
	}

	
	if (!error)
	{
		fprintf( stdout, "Info: Verifying hardware: ByteBlaster II found...\n" );
		return error;
	}
	else
		return error;
}

/******************************************************************/
/* Name:         VerifyBBMV (ByteBlasterMV) 					  */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: '0' if verification is successful;'1' if not.    */
/*               		                                          */
/* Descriptions: Verify if ByteBlasterMV is properly attached to  */
/*               the parallel port.                               */
/*                                                                */
/******************************************************************/
int VerifyBBMV()
{
	int error = 0;
	int test_count = 0;
	int read_data = 0;
	
	for ( test_count = 0; test_count < 2; test_count++ )
	{
		/* Write '0' to Pin 7 and Pin 9 (Data5,7) for the first test and '1' for the second test */
		int vector = (test_count) ? 0xA0 : 0x0;/* 1010 0000:0000 0000... drive to Port0 */
		int expect = (test_count) ? 0x60 : 0x0;/* X11X XXXX:X00X XXXX... expect from Port1 */

		WritePort( PORT_0, vector, BUFFER_OFF );
		
		/* Expect '0' at Pin 10 and Pin 12 (Ack and Paper End) for the first test and '1' for the second test */
		read_data = ReadPort( PORT_1 ) & 0x60;

		/* If no ByteBlasterMV detected, error = 1 */
		if (test_count==0)
		{
			if(read_data==0x00)
				error=0;
			else error=1;
		}

		if (test_count==1)
		{
			if(read_data==0x60)
				error=error|0;
			else error=1;
		}
	}
	
	if (!error)
	{
		fprintf( stdout, "Info: Verifying hardware: ByteBlasterMV found...\n" );
		return error;
	}
	else
		return error;
}

#endif /* PORT==WINDOWS_NT */

/******************************************************************/
/* Name:         ReadPort                                         */
/*                                                                */
/* Parameters:   port                                             */
/*               -the index of port from the parallel port base   */
/*                address.                                        */
/*                                                                */
/* Return Value: Value of the port.                               */
/*               		                                          */
/* Descriptions: Read the value of the port registers.            */
/*                                                                */
/******************************************************************/
int ReadPort(int port)
{
	int data = 0;
	
#if PORT==WINDOWS_NT
	int status = 0;
	int returned_length = 0;

	status = DeviceIoControl(
			nt_device_handle,			/* Handle to device */
			PGDC_IOCTL_READ_PORT_PP,	/* IO Control code for Read */
			(ULONG *)&port,				/* Buffer to driver. */
			sizeof(int),				/* Length of buffer in bytes. */
			(ULONG *)&data,				/* Buffer from driver. */
			sizeof(int),				/* Length of buffer in bytes. */
			(ULONG *)&returned_length,	/* Bytes placed in data_buffer. */
			NULL);						/* Wait for operation to complete */

	if ((!status) || (returned_length != sizeof(int)))
	{
		fprintf(stderr, "I/O error:  Cannot access ByteBlaster hardware\n");
		CloseHandle(nt_device_handle);
		CloseNtDriver();
		exit(1);
	}

#else if PORT==EMBEDDED
	/* Put your I/O routines here */

	int USB_TD0 = 0;
	int USB_TMS = 0;
	DWORD bytesToRead = 1;
	DWORD bytesReturned = 0;
	UCHAR BitMode;
		
	if (port != 1)
	{
		exit(1);
		fprintf(stdout,"Read Error!");
	}
	else
	{	
		FT_GetBitMode(ftHandle,&BitMode);
		//Sleep(50);
		//FT_Read(ftHandle, &data, bytesToRead, &bytesReturned);
		USB_TD0 = (BitMode&0x4)<<5;
		USB_TD0 = ~USB_TD0;
		data = USB_TD0&0x80;
	}
#endif

	return (data & 0xFF);
}



	
/******************************************************************/
/* Name:         WritePort                                        */
/*                                                                */
/* Parameters:   port,data,buffer_enable                          */
/*               -port is the index from the parallel port base   */
/*                address.                                        */
/*               -data is the value to dump to the port.          */
/*               -purpose of write.                               */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                          */
/* Descriptions: Write "data" to "port" registers. When dump to   */
/*               port 0,if "buffer_enable"=1, processes in		  */
/*				 "port_io_buffer" are flushed when				  */
/*               "PORT_IO_BUFFER_SIZE" is reached			      */
/*               If "buffer_enable"=0,"data" is dumped to port 0  */
/*               at once.                                         */
/*                                                                */
/******************************************************************/
void WritePort(int port,int data,int buffer_enable)
{

#if PORT==WINDOWS_NT
	int status = 0;
	int returned_length = 0;
	int buffer[2];
	
	/* Collect up to [PORT_IO_BUFFER_SIZE] data for Port0, then flush them */
	/* if buffer_enable = 0 or Port = 1 or Port = 2, writing to the ports are done immediately */
	if (port == 0 && buffer_enable == BUFFER_ON)
	{
		port_io_buffer[port_io_buffer_count].data = (USHORT) data;
		port_io_buffer[port_io_buffer_count].command = PGDC_WRITE_PORT;
		++port_io_buffer_count;

		
		if (port_io_buffer_count >= PORT_IO_BUFFER_SIZE) flush_ports();
	}
	else
	{
		buffer[0] = port;
		buffer[1] = data;

		status = DeviceIoControl(
				nt_device_handle,			// Handle to device 
				PGDC_IOCTL_WRITE_PORT_PP,	// IO Control code for write 
				(ULONG *)&buffer,			// Buffer to driver. 
				2 * sizeof(int),			// Length of buffer in bytes. 
				(ULONG *)NULL,				// Buffer from driver.  Not used. 
				0,							// Length of buffer in bytes. 
				(ULONG *)&returned_length,	// Bytes returned.  Should be zero. 
				NULL);						// Wait for operation to complete
	}

	
#else if PORT==EMBEDDED
	/* Put your I/O rountines here */
	int USB_TCK = 0;
	int USB_TDI = 0;
	int USB_TMS = 0;
	DWORD dwWritten;
	static int numBytes = 0;
	

	if (port != 0)
	{
		exit(1);
		fprintf(stdout,"Write Error!");
	}
	else
	{
		// Data umformen
		USB_TCK = data&0x01;
		USB_TDI = (data&0x40)>>5;
		USB_TMS = (data&0x02)<<2;
		data = (USB_TCK | USB_TDI | USB_TMS);	
		
		// Data puffern
		if (buffer_enable == 1)
		{
			byteBuffer[numBytes] = (unsigned char) data;
			numBytes++;
			
			if (numBytes == 10240)
			{
				FT_Write(ftHandle, &byteBuffer, sizeof(byteBuffer), &dwWritten);
				numBytes = 0;
			}	
		}
		else
		{
			if(sendLastBuffer == 1)
			{
				FT_Write(ftHandle, &byteBuffer, numBytes, &dwWritten);
				sendLastBuffer = 0;
			}

			FT_Write(ftHandle, &data, 1, &dwWritten);
		}
	}	
#endif
}

/*****************************/
/*                           */
/* File processing functions */
/*                           */
/*****************************/

int jb_fopen(char* argv,char* mode)
{
	FILE* file_id;

	file_id = fopen( argv, mode );

	return (int) file_id;
}

int	jb_fclose(int file_id)
{
	fclose( (FILE*) file_id);

	return 0;
}

int jb_fseek(int finputid,int start,int end)
{
	int seek_position;

	seek_position = fseek( (FILE*) finputid, start, end );

	return seek_position;
}

int jb_ftell(int finputid)
{
	int file_size;

	file_size = ftell( (FILE*) finputid );

	return file_size;
}

int jb_fgetc(int finputid)
{
	int one_byte;

	one_byte = fgetc( (FILE*) finputid );

	return one_byte;
}

char* jb_fgets(char* buffer, int finputid)
{
	char* test;
	test=fgets(buffer,MAX_FILE_LINE_LENGTH,(FILE*) finputid);

	return test;
}

/*******************************/
/*                             */
/* String processing functions */
/*                             */
/*******************************/

/******************************************************************/
/* Name:         jb_grabdata                                      */
/*                                                                */
/* Parameters:   buffer, start_byte, term, str                    */
/*               -buffer is the line buffer stored when reading   */
/*                the CDF.                                        */
/*               -start_byte is the byte to start with on the     */
/*                buffer parsed from CDF                          */
/*               -term is how many terms to look into from the    */
/*                start_byte parsed from CDF					  */
/*				 -str is the string from start_byte till term in  */
/*				  the buffer parsed from CDF					  */
/*                                                                */
/* Return Value: mark - "0" if all spaces in the buffer, else "1" */
/*               		                                          */
/******************************************************************/
/* [sbng,4/12/02,jb_io.c ver1.1] New function added */
int jb_grabdata(char* buffer,int start_byte,int term,char* str)
{
	unsigned i=0,j=0;
	int mark=0;
	int space=0;

	if(start_byte<0 || start_byte>=(int)(strlen(buffer)-1))
	{		
		str[0]='\0';
		return (-1);
	}

	for(i=start_byte;i<strlen(buffer);i++)
	{
		if(mark==0)
		{
			if( buffer[i]!=' ' && buffer[i]!='\t' && buffer[i]!='\n' )
			{
				if(space==term-1)
					str[j++]=buffer[i];
				mark=1;
			}
			else if( buffer[i]==' ' || buffer[i]=='\t' )
				mark=0;
			else
			{
				if(!((buffer[i]==' ' || buffer[i]=='\t') && (buffer[(i>0)? i-1:0]==' ' || buffer[i]=='\t' )))
					space++;
				if(space>term-1)
					break;
			}
		}
		else if(mark==1)
		{
			if( buffer[i]!=' ' && buffer[i]!='\t' && buffer[i]!='\n' )
			{
				if(space==term-1)
					str[j++]=buffer[i];
			}
			else
			{
				if(!((buffer[i]==' ' || buffer[i]=='\t' ) && (buffer[(i>0)? i-1:0]==' ' || buffer[i]=='\t' )))
					space++;
				if(space>term-1)
					break;
			}
		}
		else;
	}

	str[j]='\0';

	return mark;
}

void jb_strcpy(char* a,char* b)
{
	strcpy(a,b);
}

int jb_str_cmp(char* charset,char* buffer)
{
	char* pc;
	char  temp[MAX_FILE_LINE_LENGTH+1],store;
	unsigned int i;

	for(i=0;i<strlen(buffer);i++)
	{
		strcpy(temp,buffer);
		pc = &temp[i];

		if(*pc==*charset)
		{
			store = temp[i+strlen(charset)];
			temp[i+strlen(charset)]='\0';

			if(!strncmp(pc,charset,strlen(charset)))
			{
				temp[i+strlen(charset)]=store;
				return i+1;
			}
			
			temp[i+strlen(charset)]=store;
		}

		pc++;		
	}

	return 0;
}

int jb_strlen(const char* str)
{
	return strlen(str);
}

/******************************************************************/
/* Name:         jb_strcmp                                        */
/*                                                                */
/* Parameters:   a, b			                                  */
/*               -a and b are strings							  */
/*                                                                */
/* Return Value: -0 if a and b are identical                      */
/*				 -!0 if a and b are not identical				  */
/*               		                                          */
/* Descriptions: Used to compare strings						  */
/*                                                                */
/******************************************************************/
int jb_strcmp(char* a,char* b)
{
	return strcmp(a,b);
}

void jb_strcat(char* dst,char* src)
{
	strcat(dst,src);
}

int jb_atoi(char* number)
{
	return atoi(number);
}

void jb_toupper(char* str)
{
	char* pstr;

	pstr = str;

	while(*pstr)
	{
		if(*pstr>='a' && *pstr<='z')
			*pstr -= 0x20;

		pstr++;
	}
}