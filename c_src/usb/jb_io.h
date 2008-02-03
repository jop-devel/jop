/******************************************************************/
/*                                                                */
/* Module:       jb_io.h                                          */
/*                                                                */
/* Descriptions: Manages I/O related routines, file and string    */
/*               processing functions.                            */
/*                                                                */
/* Revisions:    .0 02/22/2002                                    */
/*               .1 07/01/2004                                    */
/*                                                                */
/******************************************************************/

#ifndef JB_IO_H
#define JB_IO_H

#include "compat.h"

/* Macro for port number */
#define PORT_0	   0
#define PORT_1	   1
#define PORT_2	   2

extern int sendLastBuffer;

void  jb_io_init(FTDI_HANDLE_T *ftdi);

int   jb_read_port(int port);
void  jb_write_port(int port,int data,int buffer_enable);
void  jb_drive_signal(int signal,int data,int clk,int buffer_enable);

int   jb_grabdata(char* buffer,int start_byte,int term,char* str);
int   jb_str_cmp(char* charset,char* buffer);
void  jb_toupper(char* str);

#endif
