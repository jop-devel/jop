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
/* [chtong,1/24/2003,jrunner.c,ver1.3] JRunner is supported in */
/* Windows NT, Windows 2000 and Windows XP platform */
#ifndef WINDOWS_NT
#define WINDOWS_NT 1
#endif

#ifndef EMBEDDED
#define EMBEDDED 2
#endif

/*put #define PORT EMBEDDED if embedded configurations is used*/
//#define PORT WINDOWS_NT
#define PORT EMBEDDED

/* Macro for buffer_enable argument in WritePort function */
#define BUFFER_ON 1
#define BUFFER_OFF 0

/* Macro for port number */
#define PORT_0	   0
#define PORT_1	   1
#define PORT_2	   2

#if PORT==WINDOWS_NT
void  flush_ports(void);
void  CloseNtDriver(void);
void  InitNtDriver(void);
int   VerifyBBII(void);
int   VerifyBBMV(void);
#endif /* PORT==WINDOWS_NT */

int   ReadPort(int port);
void  WritePort(int port,int data,int buffer_enable);

int   jb_fopen(char*,char* );
int   jb_fclose(int file_id );
int   jb_fseek(int,int,int );
int   jb_ftell(int finputid );
int   jb_fgetc(int finputid );
char* jb_fgets(char* buffer,int finputid);

int   jb_grabdata(char* buffer,int start_byte,int term,char* str);
void  jb_strcpy(char* a,char* b);
int   jb_str_cmp(char* charset,char* buffer);
int   jb_strlen(const char* str);
int   jb_strcmp(char* a,char* b);
void  jb_strcat(char* dst,char* src);
int   jb_atoi(char* number);
void  jb_toupper(char* str);


#endif