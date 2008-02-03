/******************************************************************/
/*                                                                */
/* Module:       jb_const.h                                       */
/*                                                                */
/* Descriptions: Contain USER and PROGRAM variables use by        */
/*               jrunner.c                                        */
/*                                                                */
/* Revisions:    .0 02/22/02                                      */
/*               .1 04/12/02                                      */
/*	       	 .2 10/23/02					  */
/*		 .3 07/16/03					  */
/*               .4 07/01/2004                                    */
/*                                                                */
/******************************************************************/

#ifndef JB_CONST_H
#define JB_CONST_H

#include "jb_io.h"

/* Version Number */
#define VERSION "1.9"

/* User Variables */
#define MAX_DEVICE_ALLOW 10
#define MAX_CONFIG_COUNT 3
#define INIT_COUNT       200

/* Chain Description File (CDF) records string length */
#define CDF_IDCODE_LEN 32
#define CDF_PNAME_LEN  20
#define CDF_PATH_LEN   60	/*If the path length is too large, increase the value of this constant*/
#define CDF_FILE_LEN   20   /*If the file name is too long, increase the value of this constant*/

extern const int sig_port_maskbit[4][2];
extern const int port_mode_data[2][3];

extern int port_data[3];

extern int device_count;
extern int device_family;

/* a structure (list) that stores the records of a device */
struct list{
	int   idcode;
	int   jseq_max;
	int   jseq_conf_done;
	char  action;
	char  partname[CDF_PNAME_LEN];
	char  path[CDF_PATH_LEN];
	char  file[CDF_FILE_LEN];
	int   inst_len;
};

extern struct list device_list[MAX_DEVICE_ALLOW];

#endif
