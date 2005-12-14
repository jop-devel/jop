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
#include "jb_jtag.h"

#define MAX_JS_CHAR_COUNT 10
#define JSM_RESET_COUNT 5

extern void DriveSignal(int signal,int data,int clk,int buffer_enable);
extern int  ReadPort(int port);
extern int  sig_port_maskbit[4][2];

/* JTAG State Machine */
const int JSM[16][2] = {
/*-State-      -mode= '0'-    -mode= '1'-
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

/* JTAG Instructions */
/* Notice that for Stratix II and Cyclone II, EXTEST Instruction code = 0x00F */
const int JI_EXTEST       = 0x000;	
const int JI_PROGRAM      = 0x002;
const int JI_STARTUP      = 0x003;
const int JI_CHECK_STATUS = 0x004;
const int JI_SAMPLE       = 0x005;
const int JI_IDCODE       = 0x006;
const int JI_USERCODE     = 0x007;
const int JI_BYPASS       = 0x3FF;
const int JI_PULSE_NCONFIG= 0x001;
const int JI_CONFIG_IO	  = 0x00D;
const int JI_HIGHZ		  = 0x00B;
const int JI_CLAMP		  = 0x00A;

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

/******************************************************************/
/* Name:         Js_Reset                                         */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                          */
/* Descriptions: Reset the JSM by issuing JSM_RESET_COUNT of clock*/
/*               with the TMS at HIGH.                            */
/*                                                                */
/******************************************************************/
void Js_Reset()
{
	int i;

	for(i=0;i<JSM_RESET_COUNT;i++)
		AdvanceJSM(1);
}

/******************************************************************/
/* Name:         Runidle                                          */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                          */
/* Descriptions: If the current JSM is not at UPDATE_DR or        */
/*               UPDATE_IR state, RESET JSM and move to RUNIDLE,  */
/*               if it is, clock once with TMS LOW and move to    */
/*               RUNIDLE.                                         */
/*                                                                */
/******************************************************************/
void Js_Runidle()
{
	int i=0;
	
	/* If the current state is not UPDATE_DR or UPDATE_IR, reset the JSM and move to RUN/IDLE */
	if(jtag.state!=JS_UPDATE_IR && jtag.state!=JS_UPDATE_DR)
	{
		for(i=0;i<JSM_RESET_COUNT;i++)
			AdvanceJSM(1);
	}

	AdvanceJSM(0);
}

/******************************************************************/
/* Name:         AdvanceJSM                                       */
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
int AdvanceJSM(int mode)
{
	DriveSignal(SIG_TMS,mode,TCK_TOGGLE,BUFFER_OFF);
		
	jtag.state = JSM[jtag.state][mode];

	return (jtag.state);
}

/******************************************************************/
/* Name:         PrintJS                                          */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                          */
/* Descriptions: Print the current state of the JSM.              */
/*                                                                */
/******************************************************************/
void PrintJS()
{
	char state[MAX_JS_CHAR_COUNT+1];

	strcpy(state, JS_NAME[jtag.state]);
	
	fprintf(stdout, "Info: JSM: %s\n", state );
}

/******************************************************************/
/* Name:         SetupChain                                       */
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
/*               		                                          */
/* Descriptions: Move the JSM to SHIFT_IR. Issue the JTAG         */
/*               instruction, "action" to the target device and   */
/*               BYPASS to the rest of the devices. Then, move    */
/*               the JSM to UPDATE_IR.                            */
/*                                                                */
/******************************************************************/
void SetupChain(int dev_count,int dev_seq,int* ji_info,int action)
{
	int i,record=0;
	/* Move Jtag State Machine (JSM) to RUN/IDLE */
	if(jtag.state!=JS_RUNIDLE && jtag.state!=JS_RESET)
		Js_Runidle();

	/* Move JSM to SHIFT_IR */
	AdvanceJSM(0);
	AdvanceJSM(1);
	AdvanceJSM(1);
	AdvanceJSM(0);
	AdvanceJSM(0);

	for(i=dev_count-1;i>=0;i--)
	{
		if(i==dev_seq-1)
			record = ReadTDO(ji_info[i],action,(i==0)? 1:0);
		else
			record = ReadTDO(ji_info[i],JI_BYPASS,(i==0)? 1:0);
	}

	/* Move JSM to UPDATE_IR */
	AdvanceJSM(1);
	AdvanceJSM(1);
}

/******************************************************************/
/* Name:         LoadJI                                           */
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
int LoadJI(int action,int dev_count,int* ji_info)
{
	int i,record=0,error=0;

	/* Move Jtag State Machine (JSM) to RUN/IDLE */
	if(jtag.state!=JS_RUNIDLE && jtag.state!=JS_RESET)
		Js_Runidle();

	/* Move JSM to SHIFT_IR */
	AdvanceJSM(0);
	AdvanceJSM(1);
	AdvanceJSM(1);
	AdvanceJSM(0);
	AdvanceJSM(0);


	for(i=0;i<dev_count;i++)
	{
		record = ReadTDO(ji_info[i],action,(i==(dev_count-1))? 1:0);
		if(record!=0x155)
		{
			error=1;
			fprintf(stderr,"Error: JTAG chain broken!\nError: Bits unloaded: 0x%X\n", record);
			return error;
		}
	}

	/* Move JSM to UPDATE_IR */
	AdvanceJSM(1);
	AdvanceJSM(1);	
		
	return error;
}

/******************************************************************/
/* Name:         ReadTDO                                          */
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
/*               		                                          */
/* Descriptions: Shift out bit_count bits from TDO while shift in */
/*               data to TDI. During instruction loading, the     */
/*               number of shifting equals to the instruction     */
/*               length minus 1                                   */
/*                                                                */
/******************************************************************/
int ReadTDO(int bit_count,int data,int inst)
{
	unsigned int tdi=0,tdo=0,record=0;
	unsigned int i,max = inst? (bit_count-1):bit_count;

	for(i=0;i<max;i++)
	{		
		unsigned int mask=1;

		tdo = ReadPort(PORT_1) & sig_port_maskbit[SIG_TDO][1];
		tdo = tdo? 0:(1<<i);
		record = record | tdo;
		mask = mask << i;
		tdi = data & mask;
		tdi = tdi >> i;
		DriveSignal(SIG_TDI,tdi,TCK_TOGGLE,BUFFER_OFF);
	}

	return record;
	fprintf(stdout,"Info: Record = %d \n",record);
}

/******************************************************************/
/* Name:         Js_Shiftdr                                       */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: 1 if the current state is not UPDATE_DR or       */
/*               UPDATE_IR. 0 if the opeation is successful.      */
/*               		                                          */
/* Descriptions: Move the JSM to SHIFT_DR. The current state is   */
/*               expected to be UPDATE_DR or UPDATE_IR.           */
/*                                                                */
/******************************************************************/
int Js_Shiftdr()
{
	/* The current JSM state must be in UPDATE_IR or UPDATE_DR */
	if(jtag.state!=JS_UPDATE_DR && jtag.state!=JS_UPDATE_IR)
	{
		if(jtag.state!=JS_RESET && jtag.state!=JS_RUNIDLE)
			return (1);
		else
		{
			AdvanceJSM(0);
			AdvanceJSM(0);
			AdvanceJSM(1);
			AdvanceJSM(0);
			AdvanceJSM(0);

			return (0);
		}
	}

	AdvanceJSM(1);
	AdvanceJSM(0);
	AdvanceJSM(0);

	return (0);
}

/******************************************************************/
/* Name:         Js_Updatedr                                      */
/*                                                                */
/* Parameters:   None.                                            */
/*                                                                */
/* Return Value: 1 if the current state is not SHIFT_DR;0 if the  */
/*               operation is successful.                         */
/*               		                                          */
/* Descriptions: Move the JSM to UPDATE_DR. The current state is  */
/*               expected to be SHIFT_DR                          */
/*                                                                */
/******************************************************************/
int Js_Updatedr()
{
	/* The current JSM state must be in UPDATE_IR or UPDATE_DR */
	if(jtag.state!=JS_SHIFT_DR)
		return (1);

	AdvanceJSM(1);
	AdvanceJSM(1);

	return (0);
}

int Ji_Extest(int dev_count,int* ji_info)
{
	return LoadJI(JI_EXTEST,dev_count,ji_info);
}

int Ji_Program(int dev_count,int* ji_info)
{
	return LoadJI(JI_PROGRAM,dev_count,ji_info);
}

int Ji_Startup(int dev_count,int* ji_info)
{
	return LoadJI(JI_STARTUP,dev_count,ji_info);
}

int Ji_Checkstatus(int dev_count,int* ji_info)
{
	return LoadJI(JI_CHECK_STATUS,dev_count,ji_info);
}

int Ji_Sample(int dev_count,int* ji_info)
{
	return LoadJI(JI_SAMPLE,dev_count,ji_info);
}

int Ji_Idcode(int dev_count,int* ji_info)
{
	return LoadJI(JI_IDCODE,dev_count,ji_info);
}

int Ji_Usercode(int dev_count,int* ji_info)
{
	return LoadJI(JI_USERCODE,dev_count,ji_info);
}

int Ji_Bypass(int dev_count,int* ji_info)
{
	return LoadJI(JI_BYPASS,dev_count,ji_info);
}

int  Ji_Pulse_nConfig(int dev_count,int* ji_info)
{
	return LoadJI(JI_PULSE_NCONFIG,dev_count,ji_info);
}

int  Ji_Config_IO(int dev_count,int* ji_info)
{
	return LoadJI(JI_CONFIG_IO,dev_count,ji_info);
}

int  Ji_HighZ(int dev_count,int* ji_info)
{
	return LoadJI(JI_HIGHZ,dev_count,ji_info);
}

int  Ji_Clamp(int dev_count,int* ji_info)
{
	return LoadJI(JI_CLAMP,dev_count,ji_info);
}
