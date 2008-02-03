/******************************************************************/
/*                                                                */
/* Module:       jb_jtag.h                                        */
/*                                                                */
/* Descriptions: Manages JTAG State Machine (JSM), loading of     */
/*               JTAG instructions and reading of data from TDO.  */
/*                                                                */
/* Revisions:    .0 02/22/02                                      */
/*               .1 07/07/2004                                    */
/*                                                                */
/******************************************************************/

#ifndef JB_JTAG_H
#define JB_JTAG_H

/* Flag bits for DriveSignal() function */
#define TMS_HIGH   1
#define TMS_LOW    0
#define TDI_HIGH   1
#define TDI_LOW    0
#define TCK_HIGH   1
#define TCK_LOW    0
#define TCK_TOGGLE 1
#define TCK_QUIET  0
#define BUFFER_ON 1
#define BUFFER_OFF 0

/* JTAG Configuration Signals */
#define SIG_TCK  0 /* TCK */
#define SIG_TMS  1 /* TMS */
#define SIG_TDI  2 /* TDI */
#define SIG_TDO  3 /* TDO */

/* States of JTAG State Machine */
#define JS_RESET         0
#define JS_RUNIDLE       1
#define JS_SELECT_IR     2
#define JS_CAPTURE_IR    3
#define JS_SHIFT_IR      4
#define JS_EXIT1_IR      5
#define JS_PAUSE_IR      6
#define JS_EXIT2_IR      7
#define JS_UPDATE_IR     8
#define JS_SELECT_DR     9
#define JS_CAPTURE_DR    10
#define JS_SHIFT_DR      11
#define JS_EXIT1_DR      12
#define JS_PAUSE_DR      13
#define JS_EXIT2_DR      14
#define JS_UPDATE_DR     15
#define JS_UNDEFINE      16

/* Boundary Scan Register type */
#define IN_REG		0
#define OE_REG		1
#define OUT_REG		2

/* JTAG Instructions */
/* Notice that for Stratix II and Cyclone II, EXTEST Instruction code = 0x00F */
#define JI_EXTEST        0x000	
#define JI_PROGRAM       0x002
#define JI_STARTUP       0x003
#define JI_CHECK_STATUS  0x004
#define JI_SAMPLE        0x005
#define JI_IDCODE        0x006
#define JI_USERCODE      0x007
#define JI_BYPASS        0x3FF
#define JI_PULSE_NCONFIG 0x001
#define JI_CONFIG_IO	 0x00D
#define JI_HIGHZ	 0x00B
#define JI_CLAMP	 0x00A

extern int ji_info[MAX_DEVICE_ALLOW];

int  advance_jsm(int);
int  read_tdo(int bits, int data, int inst);
void print_js();

void setup_chain(int dev_count, int dev_seq, int* ji_info, int action);
int  verify_chain();

void startup(int dev_seq);
int  check_status(int dev_seq);

int  load_ji(int inst, int dev_count, int* ji_info);
int  ji_extest(int device, int* ji_info);
int  ji_program(int device, int* ji_info);
int  ji_startup(int device, int* ji_info);
int  ji_checkstatus(int device, int* ji_info);
int  ji_sample(int device, int* ji_info);
int  ji_idcode(int device, int* ji_info);
int  ji_usercode(int device, int* ji_info);
int  ji_bypass(int device, int* ji_info);
int  ji_pulse_nConfig(int device, int* ji_info);
int  ji_config_IO(int device, int* ji_info);
int  ji_highZ(int device, int* ji_info);
int  ji_clamp(int device, int* ji_info);

void js_reset();
void js_runidle();
int  js_shiftdr();
int  js_updatedr();

#endif
