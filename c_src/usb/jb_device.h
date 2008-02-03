/******************************************************************/
/*                                                                */
/* Module:       jb_device_info.h                                 */
/*                                                                */
/* Descriptions: Contain information of Altera devices.           */
/*                                                                */
/* Revisions:    .0 02/22/02                                      */
/*				 .2 10/23/02									  */
/*				 Stratix JTAG ID-Code added						  */
/*				 .3 1/24/03										  */
/*				 Cyclone JTAG ID-Code added						  */
/*				 .4 6/3/03										  */
/*				 Stratix GX JTAG ID-Code added					  */
/*				 1C4 JTAG ID-Code added							  */
/*				 .5 6/1/04										  */
/*				 Stratix II EP2S60 JTAG ID-Code added			  */
/*				 .6 1/28/05										  */
/*				 Stratix II JTAG ID-Code added					  */
/*               Cyclone II EP2C35 JTAG ID-Code added			  */
/******************************************************************/

#ifndef JB_DEVICE_INFO_H
#define JB_DEVICE_INFO_H

#define MAX_DEV_FAMILY 12
#define MAX_DEV_LIST   177

extern int MAX_JTAG_INIT_CLOCK[MAX_DEV_FAMILY];

extern int start_of_device_family[MAX_DEV_FAMILY];
extern char family_name[MAX_DEV_FAMILY][12];
extern char device_name[MAX_DEV_LIST][20];
extern unsigned int device_info[MAX_DEV_LIST][4];

#endif
