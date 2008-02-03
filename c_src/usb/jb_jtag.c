/******************************************************************/
/*                                                                */
/* Module:       jb_jtag.c                                        */
/*                                                                */
/* Descriptions: Manages JTAG State Machine (JSM), loading of     */
/*               JTAG instructions and reading of data from TDO.  */
/*                                                                */
/* Revisions:    .0 02/22/02                                      */
/*               .1 07/07/2004                                    */
/*                                                                */
/******************************************************************/

#include <stdio.h>
#include <string.h>

#include "jb_const.h"
#include "jb_io.h"
#include "jb_jtag.h"

#define MAX_JS_CHAR_COUNT 10
#define JSM_RESET_COUNT 5

/* JTAG State Machine */
const int JSM[16][2] = {
  /*-State-      -mode= '0'-    -mode= '1'- */
  /*RESET     */ {JS_RUNIDLE,   JS_RESET    },
  /*RUNIDLE   */ {JS_RUNIDLE,   JS_SELECT_DR},
  /*SELECTIR  */ {JS_CAPTURE_IR,JS_RESET    },
  /*CAPTURE_IR*/ {JS_SHIFT_IR,  JS_EXIT1_IR },
  /*SHIFT_IR  */ {JS_SHIFT_IR,  JS_EXIT1_IR },
  /*EXIT1_IR  */ {JS_PAUSE_IR,  JS_UPDATE_IR},
  /*PAUSE_IR  */ {JS_PAUSE_IR,  JS_EXIT2_IR },
  /*EXIT2_IR  */ {JS_SHIFT_IR,  JS_UPDATE_IR},
  /*UPDATE_IR */ {JS_RUNIDLE,   JS_SELECT_DR},
  /*SELECT_DR */ {JS_CAPTURE_DR,JS_SELECT_IR},
  /*CAPTURE_DR*/ {JS_SHIFT_DR,  JS_EXIT1_DR },
  /*SHIFT_DR  */ {JS_SHIFT_DR,  JS_EXIT1_DR },
  /*EXIT1_DR  */ {JS_PAUSE_DR,  JS_UPDATE_DR},
  /*PAUSE_DR  */ {JS_PAUSE_DR,  JS_EXIT2_DR },
  /*EXIT2_DR  */ {JS_SHIFT_DR,  JS_UPDATE_DR},
  /*UPDATE_DR */ {JS_RUNIDLE,   JS_SELECT_DR} 
};

const char JS_NAME[][MAX_JS_CHAR_COUNT+1] = {
  "RESET",
  "RUN/IDLE",
  "SELECT_IR",
  "CAPTURE_IR",
  "SHIFT_IR",
  "EXIT1_IR",
  "PAUSE_IR",
  "EXIT2_IR",
  "UPDATE_IR",
  "SELECT_DR",
  "CAPTURE_DR",
  "SHIFT_DR",
  "EXIT1_DR",
  "PAUSE_DR",
  "EXIT2_DR",
  "UPDATE_DR",
  "UNDEFINE" };

struct states
{
  int state;
} jtag;

/* JTAG instruction lengths of all devices */
int  ji_info[MAX_DEVICE_ALLOW] = {0};

/******************************************************************/
/* Name:         js_reset                                         */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               	                                          */
/* Descriptions: Reset the JSM by issuing JSM_RESET_COUNT of clock*/
/*               with the TMS at HIGH.                            */
/*                                                                */
/******************************************************************/
void js_reset()
{
  int i;

  for(i=0;i<JSM_RESET_COUNT;i++)
    {
      advance_jsm(1);
    }
}

/******************************************************************/
/* Name:         js_runidle                                       */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               	                                          */
/* Descriptions: If the current JSM is not at UPDATE_DR or        */
/*               UPDATE_IR state, RESET JSM and move to RUNIDLE,  */
/*               if it is, clock once with TMS LOW and move to    */
/*               RUNIDLE.                                         */
/*                                                                */
/******************************************************************/
void js_runidle()
{
  int i=0;
	
  /* If the current state is not UPDATE_DR or UPDATE_IR, reset the JSM and move to RUN/IDLE */
  if(jtag.state!=JS_UPDATE_IR && jtag.state!=JS_UPDATE_DR)
    {
      for(i=0;i<JSM_RESET_COUNT;i++)
	advance_jsm(1);
    }

  advance_jsm(0);
}

/******************************************************************/
/* Name:         advance_jsm                                       */
/*                                                                */
/* Parameters:   mode                                             */
/*               -the input mode to JSM.                          */
/*                                                                */
/* Return Value: The current JSM state.                           */
/*               		                                          */
/* Descriptions: Function that keep track of the JSM state. It    */
/*               drives out signals to TMS associated with a      */
/*               clock pulse at TCK and updates the current state */
/*               variable.                                        */
/*                                                                */
/******************************************************************/
int advance_jsm(int mode)
{
  jb_drive_signal(SIG_TMS,mode,TCK_TOGGLE,BUFFER_OFF);

  jtag.state = JSM[jtag.state][mode];

  return (jtag.state);
}

/******************************************************************/
/* Name:         print_js                                         */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               	                                          */
/* Descriptions: Print the current state of the JSM.              */
/*                                                                */
/******************************************************************/
void print_js()
{
  char state[MAX_JS_CHAR_COUNT+1];

  strcpy(state, JS_NAME[jtag.state]);
	
  fprintf(stdout, "Info: JSM: %s\n", state );
}

/******************************************************************/
/* Name:         setup_chain                                      */
/*                                                                */
/* Parameters:   dev_count,dev_seq,ji_info,action                 */
/*               -device_count is the total device in chain       */
/*               -dev_seq is the device sequence in chain         */
/*               -ji_info is the pointer to an integer array that */
/*                contains the JTAG instruction length for the    */
/*                devices in chain.                               */ 
/*               -action is the JTAG instruction to load          */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                  */
/* Descriptions: Move the JSM to SHIFT_IR. Issue the JTAG         */
/*               instruction, "action" to the target device and   */
/*               BYPASS to the rest of the devices. Then, move    */
/*               the JSM to UPDATE_IR.                            */
/*                                                                */
/******************************************************************/
void setup_chain(int dev_count,int dev_seq,int* ji_info,int action)
{
  int i,record=0;
  /* Move Jtag State Machine (JSM) to RUN/IDLE */
  if(jtag.state!=JS_RUNIDLE && jtag.state!=JS_RESET)
    js_runidle();

  /* Move JSM to SHIFT_IR */
  advance_jsm(0);
  advance_jsm(1);
  advance_jsm(1);
  advance_jsm(0);
  advance_jsm(0);

  for(i=dev_count-1;i>=0;i--)
    {
      if(i==dev_seq-1)
	record = read_tdo(ji_info[i],action,(i==0)? 1:0);
      else
	record = read_tdo(ji_info[i],JI_BYPASS,(i==0)? 1:0);
    }

  /* Move JSM to UPDATE_IR */
  advance_jsm(1);
  advance_jsm(1);
}

/******************************************************************/
/* Name:         verify_chain                                     */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*              	                                          */
/* Descriptions: Putting all devices in BYPASS mode, a 8-bit      */
/*               vector is driven to TDI, the number of '0'       */
/*               detected indicates the number of devices in      */
/*               chain. The 8-bit vector must follows the zeroes. */
/*                                                                */
/******************************************************************/
int verify_chain()
{
  unsigned int data = 0, temp = 0, test_vect = 0x55;
  int i, num = 0, error = 0;
		
  js_reset();

  /* Load BYPASS instruction and test JTAG chain with a few vectors */
  if(ji_bypass(device_count,ji_info))
    return (1);
  js_shiftdr();


  /* Drive a 8-bit vector of "10101010" (right to left) to test */
  data = read_tdo(8+device_count,test_vect,0);

  /* The number of leading '0' detected must equal to the number of devices specified */
  temp = data;
	
  for(i=0;i<device_count;i++)
    {
      temp = temp&1;
      if(temp)
	break;		
      else
	num++;
      temp = data>>(i+1);
    }

  if(temp == test_vect)	
    fprintf(stdout,"Info: Detected %d device(s) in chain...\n", num);
  else
    {
      fprintf(stderr,"Error: JTAG chain broken or #device in chain unmatch!\n");
      return (1);
    }

  js_updatedr();
	
  /* Read device IDCODE */
  ji_idcode(device_count,ji_info);
  js_shiftdr();

  for(i=device_count-1;i>=0;i--)
    {
      data = read_tdo(CDF_IDCODE_LEN,TDI_LOW,0);

      if(device_list[i].idcode)
	{
	  /* The partname specified in CDF must match with its ID Code */
	  if((unsigned)device_list[i].idcode != data)
	    {
	      fprintf(stderr,"Error: Expected 0x%X but detected 0x%X!\n",device_list[i].idcode,data);
	      error=1;
	    }
	  else
	    fprintf(stdout,"Info: Dev%d: Altera: 0x%X\n",i+1,data);
	}
      else
	{
	  fprintf(stdout,"Info: Dev%d: Non-Altera: 0x%X\n",i+1,data);
	}
    }

  js_updatedr();
  js_runidle();
	
  return error;
}

/******************************************************************/
/* Name:         startup                                          */
/*                                                                */
/* Parameters:   dev_seq                                          */
/*               -the device sequence in the chain.               */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                  */
/* Descriptions: Issue STARTUP instruction to the device to       */
/*               be configured and BYPASS for the rest of the     */
/*               devices.                                         */
/*                                                                */
/******************************************************************/
void startup(int dev_seq)
{
  int i;

  /* Load STARTUP instruction to move the device to USER mode */
  setup_chain(device_count,dev_seq,ji_info,JI_STARTUP);

  js_runidle();

  for(i = 0; i < INIT_COUNT; i++)
    {
      jb_drive_signal(SIG_TCK,TCK_LOW,TCK_QUIET,BUFFER_OFF);
      jb_drive_signal(SIG_TCK,TCK_HIGH,TCK_QUIET,BUFFER_OFF);
    }

  /* Reset JSM after the device is in USER mode */
  js_reset();
}

/******************************************************************/
/* Name:         check_status                                     */
/*                                                                */
/* Parameters:   dev_seq                                          */
/*               -dev_seq is the device sequence in chains.       */
/*                                                                */
/* Return Value: '0' if CONF_DONE is HIGH;'1' if it is LOW.       */
/*               		                                  */
/* Descriptions: Issue CHECK_STATUS instruction to the device to  */
/*               be configured and BYPASS for the rest of the     */
/*               devices.                                         */
/*                                                                */
/*               <conf_done_bit> =                                */
/*                  ((<Maximum JTAG sequence> -                   */
/*                    <JTAG sequence for CONF_DONE pin>)*3) + 1   */
/*                                                                */
/*               The formula calculates the number of bits        */
/*               to be shifted out from the device, excluding the */
/*               1-bit register for each device in BYPASS mode.   */
/*                                                                */
/******************************************************************/
int check_status(int dev_seq)
{
  int bit, data=0, error=0;
  int jseq_max=0, jseq_conf_done=0, conf_done_bit=0;

  fprintf( stdout, "Info: Checking Status\n" );

  /* Load CHECK_STATUS instruction */
  setup_chain(device_count,dev_seq,ji_info,JI_CHECK_STATUS);

  js_shiftdr();

  /* Maximum JTAG sequence of the device in chain */
  jseq_max = device_list[dev_seq-1].jseq_max;
  jseq_conf_done = device_list[dev_seq-1].jseq_conf_done;
  conf_done_bit = ((jseq_max-jseq_conf_done)*3)+1;

  /* Compensate for 1 bit unloaded from every Bypass register */
  conf_done_bit += device_count - dev_seq;
	
  for (bit = 0; bit < conf_done_bit; bit++)
    {
      jb_drive_signal(SIG_TDI,TDI_LOW,TCK_TOGGLE,BUFFER_OFF);
    }

  data = read_tdo(PORT_1,TDI_LOW,0);

  if(data == 0)
    {
      error++;
    }

  /* Move JSM to RUNIDLE */
  js_updatedr();
  js_runidle();

  return error;
}

/******************************************************************/
/* Name:         load_ji                                           */
/*                                                                */
/* Parameters:   action,dev_count,ji_info                         */
/*               -action is the JTAG instruction to load          */
/*               -dev_count is the maximum number of devices in   */
/*                chain.                                          */
/*               -ji_info is the pointer to an integer array that */
/*                contains the JTAG instruction length for the    */
/*                devices in chain.                               */
/*                                                                */
/* Return Value: 1 if contains error;0 if not.                    */
/*               		                                          */
/* Descriptions: Move the JSM to SHIFT_IR. Load in the JTAG       */
/*               instruction to all devices in chain. Then        */
/*               advance the JSM to UPDATE_IR. Irrespective of    */
/*                                                                */
/******************************************************************/
int load_ji(int action,int dev_count,int* ji_info)
{
  int i,record=0,error=0;

  /* Move Jtag State Machine (JSM) to RUN/IDLE */
  if(jtag.state!=JS_RUNIDLE && jtag.state!=JS_RESET)
    js_runidle();

  /* Move JSM to SHIFT_IR */
  advance_jsm(0);
  advance_jsm(1);
  advance_jsm(1);
  advance_jsm(0);
  advance_jsm(0);


  for(i=0;i<dev_count;i++)
    {
      record = read_tdo(ji_info[i],action,(i==(dev_count-1))? 1:0);
      if(record!=0x155)
	{
	  error=1;
	  fprintf(stderr,"Error: JTAG chain broken!\nError: Bits unloaded: 0x%X\n", record);
	  return error;
	}
    }

  /* Move JSM to UPDATE_IR */
  advance_jsm(1);
  advance_jsm(1);	
		
  return error;
}

/******************************************************************/
/* Name:         read_tdo                                         */
/*                                                                */
/* Parameters:   bit_count,data,inst                              */
/*               -bit_count is the number of bits to shift out.   */
/*               -data is the value to shift in from lsb to msb.  */
/*               -inst determines if the data is an instruction.  */
/*                if inst=1,the number of bits shifted in/out     */
/*                equals to bit_count-1;if not,the number of bits */
/*                does not change.                                */
/*                                                                */
/* Return Value: The data shifted out from TDO. The first bit     */
/*               shifted out is placed at the lsb of the returned */
/*               integer.                                         */
/*               		                                  */
/* Descriptions: Shift out bit_count bits from TDO while shift in */
/*               data to TDI. During instruction loading, the     */
/*               number of shifting equals to the instruction     */
/*               length minus 1                                   */
/*                                                                */
/******************************************************************/
int read_tdo(int bit_count,int data,int inst)
{
  unsigned int tdi=0,tdo=0,record=0;
  unsigned int i,max = inst? (bit_count-1):bit_count;

  for(i=0;i<max;i++)
    {		
      unsigned int mask=1;

      tdo = jb_read_port(PORT_1) & sig_port_maskbit[SIG_TDO][1];
      tdo = tdo? 0:(1<<i);
      record = record | tdo;
      mask = mask << i;
      tdi = data & mask;
      tdi = tdi >> i;
      jb_drive_signal(SIG_TDI,tdi,TCK_TOGGLE,BUFFER_OFF);
    }

  /* 	fprintf(stdout,"Info: Record = %d \n",record); */
  return record;
}

/******************************************************************/
/* Name:         js_shiftdr                                       */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: 1 if the current state is not UPDATE_DR or       */
/*               UPDATE_IR. 0 if the opeation is successful.      */
/*                    	                                          */
/* Descriptions: Move the JSM to SHIFT_DR. The current state is   */
/*               expected to be UPDATE_DR or UPDATE_IR.           */
/*                                                                */
/******************************************************************/
int js_shiftdr()
{
  /* The current JSM state must be in UPDATE_IR or UPDATE_DR */
  if(jtag.state!=JS_UPDATE_DR && jtag.state!=JS_UPDATE_IR)
    {
      if(jtag.state!=JS_RESET && jtag.state!=JS_RUNIDLE)
	return (1);
      else
	{
	  advance_jsm(0);
	  advance_jsm(0);
	  advance_jsm(1);
	  advance_jsm(0);
	  advance_jsm(0);

	  return (0);
	}
    }

  advance_jsm(1);
  advance_jsm(0);
  advance_jsm(0);

  return (0);
}

/******************************************************************/
/* Name:         js_updatedr                                      */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: 1 if the current state is not SHIFT_DR;0 if the  */
/*               operation is successful.                         */
/*               		                                  */
/* Descriptions: Move the JSM to UPDATE_DR. The current state is  */
/*               expected to be SHIFT_DR                          */
/*                                                                */
/******************************************************************/
int js_updatedr()
{
  /* The current JSM state must be in UPDATE_IR or UPDATE_DR */
  if(jtag.state!=JS_SHIFT_DR)
    return (1);

  advance_jsm(1);
  advance_jsm(1);

  return (0);
}

int ji_extest(int dev_count,int* ji_info)
{
  return load_ji(JI_EXTEST,dev_count,ji_info);
}

int ji_program(int dev_count,int* ji_info)
{
  return load_ji(JI_PROGRAM,dev_count,ji_info);
}

int ji_startup(int dev_count,int* ji_info)
{
  return load_ji(JI_STARTUP,dev_count,ji_info);
}

int ji_checkstatus(int dev_count,int* ji_info)
{
  return load_ji(JI_CHECK_STATUS,dev_count,ji_info);
}

int ji_sample(int dev_count,int* ji_info)
{
  return load_ji(JI_SAMPLE,dev_count,ji_info);
}

int ji_idcode(int dev_count,int* ji_info)
{
  return load_ji(JI_IDCODE,dev_count,ji_info);
}

int ji_usercode(int dev_count,int* ji_info)
{
  return load_ji(JI_USERCODE,dev_count,ji_info);
}

int ji_bypass(int dev_count,int* ji_info)
{
  return load_ji(JI_BYPASS,dev_count,ji_info);
}

int  ji_pulse_nConfig(int dev_count,int* ji_info)
{
  return load_ji(JI_PULSE_NCONFIG,dev_count,ji_info);
}

int  ji_config_IO(int dev_count,int* ji_info)
{
  return load_ji(JI_CONFIG_IO,dev_count,ji_info);
}

int  ji_highZ(int dev_count,int* ji_info)
{
  return load_ji(JI_HIGHZ,dev_count,ji_info);
}

int  ji_clamp(int dev_count,int* ji_info)
{
  return load_ji(JI_CLAMP,dev_count,ji_info);
}
