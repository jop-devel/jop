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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "jb_const.h"
#include "jb_io.h"
#include "jb_jtag.h"

#include "compat.h" 

static FTDI_HANDLE_T *jb_ftdi;

void jb_io_init(FTDI_HANDLE_T *ftdi)
{
  jb_ftdi = ftdi;
}

#define BUFFER_SIZE 10240
unsigned char byteBuffer [BUFFER_SIZE] = {0};
int sendLastBuffer = 0;

/******************************************************************/
/* Name:         jb_read_port                                     */
/*                                                                */
/* Parameters:   port                                             */
/*               -the index of port from the parallel port base   */
/*                address.                                        */
/*                                                                */
/* Return Value: Value of the port.                               */
/*                                                                */
/* Descriptions: Read the value of the port registers.            */
/*                                                                */
/******************************************************************/
int jb_read_port(int port)
{
  int data = 0;
	
  /* Put your I/O routines here */
  int USB_TD0 = 0;
  unsigned char bitmode;

  if (port != 1)
    {
      fprintf(stderr,"Error: read error\n");
      exit(EXIT_FAILURE);
    }
  else
    {	
      ftdi_get_bitmode(jb_ftdi, &bitmode);
      USB_TD0 = (bitmode&0x4)<<5;
      USB_TD0 = ~USB_TD0;
      data = USB_TD0&0x80;
    }

  return (data & 0xFF);
}

/******************************************************************/
/* Name:         jb_write_port                                    */
/*                                                                */
/* Parameters:   port,data,buffer_enable                          */
/*               -port is the index from the parallel port base   */
/*                address.                                        */
/*               -data is the value to dump to the port.          */
/*               -purpose of write.                               */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                  */
/* Descriptions: Write "data" to "port" registers. When dump to   */
/*               port 0,if "buffer_enable"=1, processes in	  */
/*		 "port_io_buffer" are flushed when                */
/*               "PORT_IO_BUFFER_SIZE" is reached	          */
/*               If "buffer_enable"=0,"data" is dumped to port 0  */
/*               at once.                                         */
/*                                                                */
/******************************************************************/
void jb_write_port(int port, int data, int buffer_enable)
{
  /* Put your I/O rountines here */
  int USB_TCK = 0;
  int USB_TDI = 0;
  int USB_TMS = 0;

  int status = 0;
  
  static int numBytes = 0;
	
  if (port != 0)
    {
      fprintf(stderr,"Error: write error\n");
      exit(EXIT_FAILURE);
    }
  else
    {
      /* Data umformen */
      USB_TCK = data&0x01;
      USB_TDI = (data&0x40)>>5;
      USB_TMS = (data&0x02)<<2;
      data = (USB_TCK | USB_TDI | USB_TMS);	
		
      /* Data puffern */
      if (buffer_enable == 1)
	{
	  byteBuffer[numBytes] = (unsigned char) data;
	  numBytes++;
			
	  if (numBytes == BUFFER_SIZE)
	    {
	      status = ftdi_write(jb_ftdi, byteBuffer, sizeof(byteBuffer));
	      if (status < 0)
		{
		  fprintf(stderr, "error: write failed (%d)\n", status);
		}
	      numBytes = 0;
	    }	
	}
      else
	{
	  unsigned char data_buf [1];

	  if(sendLastBuffer == 1)
	    {
	      status = ftdi_write(jb_ftdi, byteBuffer, sizeof(byteBuffer));
	      if (status < 0)
		{
		  fprintf(stderr, "error: write failed (%d)\n", status);
		}
	      sendLastBuffer = 0;
	    }

	  data_buf[0] = data;
	  status = ftdi_write(jb_ftdi, data_buf, 1);
	  if (status < 0)
	    {
	      fprintf(stderr, "error: write failed (%d)\n", status);
	    }
	}
    }	
}

/******************************************************************/
/* Name:         jb_drive_signal                                  */
/*                                                                */
/* Parameters:   signal,data,clk,buffer_enable                    */
/*               -the name of the signal (SIG_*).                 */
/*               -the value to be dumped to the signal,'1' or '0' */
/*               -driving a LOW to HIGH transition to SIG_TCK     */
/*                together with signal.                           */
/*               -buffer_enable is used by jb_write_port function.*/
/*		 -If "buffer_enable"=1,				  */
/*		 -processes in "port_io_buffer" are flushed when  */
/*               -"PORT_IO_BUFFER_SIZE" is reached.   		  */	
/*		 -If "buffer_enable"=0,		      		  */
/*               -"data" is dumped to port 0 at once		  */
/*                                                                */
/* Return Value: None.                                            */
/*                                                                */
/* Descriptions: Dump data to signal. If clk is '1', a clock pulse*/
/*               is driven after the data is dumped to signal.    */
/*                                                                */
/******************************************************************/
void jb_drive_signal(int signal,int data,int clk,int buffer_enable)
{
	/* Get signal port number */
	int port = sig_port_maskbit[signal][0];

	/* Get signal mask bit*/
	int mask;
	
	/* If clk == 1, drive signal with [data] and drive SIG_TCK with '0' together. Then drive SIG_TCK with '1' */
	/* That is to create a positive edge pulse */
	if(clk)
		mask = sig_port_maskbit[signal][1] | sig_port_maskbit[SIG_TCK][1];
	else
		mask = sig_port_maskbit[signal][1];
	
	/* AND signal bit with '0', then OR with [data] */
	mask = ~mask;
	port_data[port] = (port_data[port]&mask) | (data*sig_port_maskbit[signal][1]);
	
	jb_write_port(port, port_data[port], buffer_enable);

	if(clk)
	{
	  jb_write_port(port, (port_data[port] | sig_port_maskbit[SIG_TCK][1]), buffer_enable);
	  jb_write_port(port, port_data[port], buffer_enable);
	}
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

int jb_str_cmp(char* charset,char* buffer)
{
  char* pc;
  char  temp[1024],store;
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
