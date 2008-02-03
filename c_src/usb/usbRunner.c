/******************************************************************/
/*                                                                */
/* Module:       USBRunner.c                                      */
/*                                                                */
/* Descriptions: Main source file that manages the configuration  */
/*               processes.                                       */
/*                                                                */
/* Revisions:    1.0 02/22/02                                     */
/*               1.1 04/12/02                                     */
/*               Use jb_grabdata function instead of jb_str_cmp   */
/*               1.2 10/23/02					  */
/*		 Stratix devices JTAG ID-Code added.		  */
/*		 Change data[50] to data[CDF_PATH_LEN] because    */
/*		 length of data array must be smaller than	  */	
/*		 CDF_PATH_LEN.					  */ 		  	
/*		 1.3 1/24/2003					  */
/*		 Cyclone devices support added.			  */
/*		 1.4 5/30/2003					  */
/*		 Stratix Gx and 1C4 devices support added.	  */
/*		 ByteBlaster II support added.			  */
/*		 1.5 7/7/2004					  */
/*		 EP2S60 devices support added.			  */
/*		 SAMPLE_BSC function added.			  */
/*               MACROs added for easier source code reading      */
/*		 1.6 1/28/2005					  */
/*               Cyclone II EP2C35 support added.		  */
/*		 1.7 7/12/2005 by Christof Pitter		  */
/*		 Stratix II devices support added.		  */
/*		 Cyclone II EPM7064AET44 support added.           */
/*		 FTDI device support added.			  */
/*		 USB support added via BitBangMode		  */
/*		 1.8 27/10/2006 by Christof Pitter 		  */
/*		 USBRunner takes only the .rbf-File not the .cdf  */
/*               anymore. The .cdf-File will be selfcreated for   */
/*		 dspio setup. At the end it will be deleted again */
/*               1.9 31/1/2008 by Wolfgang Puffitsch              */
/*               Extended to be usable on Linux and Windows       */
/******************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "jb_const.h"
#include "jb_io.h"
#include "jb_device.h"
#include "jb_jtag.h"

#include "compat.h"

/* JRunner Controller Functions */
void configure          (FILE *file,int dev_seq,int idcode);
void process_file_input (FILE *infile, int idcode);

/* CDF Parser Functions */
int  read_cdf           (FILE *file);
int  get_data           (char* charset, char* buffer, char* data);
void search_keyword     (char* buffer, char* data);
int  check_action_code  (char* data);
void check_alt_dev      (int dev_seq);

FTDI_HANDLE_T            ftdi_handle;

int main(int argc, char **argv)
{
  int   retval = 0;
  int   config_count = 0;
  FILE *outfile = NULL;
  FILE *file = NULL;

  int i;

  fprintf(stdout,"\n===================================\n");
  fprintf(stdout," USBRunner Version %s", VERSION);
  fprintf(stdout,"\n===================================\n\n");

  if (argc == 1)
    {
      fprintf(stderr,"Error: No argument could be found! \n");
      fprintf(stderr,"Provide a .rbf-File as argument when using USBRunner!\n");
      return EXIT_FAILURE;
    }

  if (ftdi_setup(&ftdi_handle) != 0)
    {
      exit(EXIT_FAILURE);
    }
  jb_io_init(&ftdi_handle);

  outfile = fopen( "jop.cdf", "w");
  if (outfile == NULL) 
    {
      fprintf(stderr, "Error: fopen of jop.cdf failed\n");
      exit(EXIT_FAILURE);
    }

  /* Writing into the .cdf-File */
  retval = fprintf(outfile, 
		   "JedecChain;\n"
		   "	FileRevision(JESD32A);\n"
		   "	DefaultMfr(6E);\n\n"
		   "	P ActionCode(Ign)\n"
		   "		Device PartName(EPM7064AET44) MfrSpec(OpMask(0));\n"
		   "	P ActionCode(Cfg)\n"
		   "		Device PartName(EP1C12Q240) Path(\"./\") File(\"%s\") MfrSpec(OpMask(1));\n\n"
		   "ChainEnd;\n\n"
		   "AlteraBegin;\n"
		   "	ChainType(JTAG);\n"
		   "AlteraEnd;\n", argv[1]);
  fclose(outfile);

  fprintf(stdout, "Info: Parsing CDF...\n");

  file = fopen("jop.cdf","rb");
  if(!read_cdf(file))
    {
      fclose(file);
      return EXIT_FAILURE;
    }
  fclose(file);

  if (device_count == 0)
    {
      return EXIT_FAILURE;
    }

  for (i = 0; i < device_count; i++)
    {
      if(!ji_info[i])
	{
	  fprintf(stderr,"Error: JTAG instruction length of device #%d NOT specified!\n",i);
	  return -1;
	}
    }

  fprintf( stdout, "Info: Verifying device chain...\n" );

  if(verify_chain())
    {
      return EXIT_FAILURE;
    }

  fprintf(stdout,"Debug: (1)jseq_max (2)jseq_conf_done (3)action (4)partname (5)path (6)file (7)inst_len\n");
  for(i = 0; i < device_count; i++)
    {
      fprintf(stdout,"Debug: (1)%d (2)%d (3)%c (4)%s (5)%s (6)%s (7)%d\n",
	      device_list[i].jseq_max,device_list[i].jseq_conf_done,device_list[i].action,device_list[i].partname,
	      device_list[i].path,device_list[i].file,device_list[i].inst_len);
    }


  for(i = 1; i < device_count+1; i++)
    {
      char fullpath[CDF_PATH_LEN+CDF_FILE_LEN];

      config_count=0;

      if(device_list[i-1].action != 'P')
	continue;

      while(config_count < MAX_CONFIG_COUNT)
	{
	  if(config_count == 0)
	    fprintf(stdout, "Info: Configuration setup device #%d\n",i);
	  else
	    fprintf(stdout, "Info: Configuration setup device #%d (Retry# %d)\n",i, config_count);

	  config_count++;
			
	  /* Open programming file as READ and in BINARY */
	  strcpy(fullpath, device_list[i-1].path);
	  strcat(fullpath, device_list[i-1].file);
	       	
	  file = fopen( argv[1], "rb" );

	  if (file != 0)
	    {
	      fprintf(stdout, "Info: Programming file #%d: \"%s\" opened...\n", i, fullpath);
	    }
	  else
	    {
	      fprintf(stderr, "Error: Could not open programming file #%d: \"%s\"\n", i, fullpath);
	      exit(EXIT_FAILURE);
	    }
			
	  configure(file, i, device_list[i-1].idcode);

	  fclose(file);
			
	  if (i == device_count)
	    {
	      int k;
	      
	      for (k = 1; k < device_count+1; k++)
		{
		  startup(k);
		}
	    }

	  if(check_status(i))
	    {
	      fprintf( stdout, "Warning: Configuration of device #%d NOT successful!\n",i );
	    }
	  else
	    {
	      config_count = MAX_CONFIG_COUNT;
	      fprintf( stdout, "Info: Configuration of device #%d successful...\n",i );
	    }  
	}
    }

  if (ftdi_teardown(&ftdi_handle) != 0)
    {
      exit(EXIT_FAILURE);
    }
  
  remove("jop.cdf");

  return EXIT_SUCCESS;
}

/******************************************************************/
/* Name:         read_cdf                                         */
/*                                                                */
/* Parameters:   file                                             */
/*               -file handler                                    */
/*                                                                */
/* Return Value: The number of devices in chain found in CDF      */
/*                                                                */
/* Descriptions: read_cdf parses through the CDF and keeps the    */
/*               device records. It looks for 'P' or 'N' when     */
/*               mark=0. Once found, it searches for "ActionCode" */
/*               when mark=1 and once mark=2, it starts to        */
/*               retrieve the data in device record declarations. */
/*                                                                */
/******************************************************************/
int read_cdf(FILE *file)
{
  char buffer[1024];    /* line buffer */
  char  data[CDF_PATH_LEN];       /* device record data between '(' and ')' */
  int   mark= 0;
		
  while(fgets(buffer, 1024, file))
    {
      if (mark==1)
	{
	  mark =2;

	  if(get_data("ACTIONCODE",buffer,data))
	    {
	      if(!check_action_code(data))
		return 0;
	    }

	  device_count++;

	  search_keyword(buffer,data);
			

	  /* End of device record and reset flag */
	  if(jb_str_cmp(";",buffer))
	    mark=0;
	}
      else if (mark==2)
	{
	  search_keyword(buffer,data);
			
	  /* End of device record and reset flag */
	  if(jb_str_cmp(";",buffer))
	    mark=0;
	}
      else
	{
	  char c_temp[50];
			
	  jb_grabdata(buffer,0,1,c_temp);
						
	  if(!jb_str_cmp(c_temp,"P") && !jb_str_cmp(c_temp,"N"))
	    continue;
	  /***********************************/

	  mark++;
			
	  if(get_data("ACTIONCODE",buffer,data))
	    {
	      if(!check_action_code(data))
		return 0;

	      /* End of device record and reset flag */
	      if(jb_str_cmp(";",buffer))
		{
		  device_count++;

		  search_keyword(buffer,data);
									
		  mark=0;
		}
	    }
	}
    }

  return device_count;
}

/******************************************************************/
/* Name:         get_data                                         */
/*                                                                */
/* Parameters:   charset, buffer, data                            */
/*               -charset is the character string or keyword to   */
/*                look for.                                       */
/*               -buffer is the line buffer stored when reading   */
/*                the CDF.                                        */
/*               -data is the string between brackets, '(' and ')'*/
/*                                                                */
/* Return Value: The position of the first character of charset   */
/*               found in buffer.                                 */
/*               		                                  */
/* Descriptions: The function copies the string between brackets  */
/*               right after charset into data. If charset is not */
/*               found in buffer, '0' is returned.                */
/*                                                                */
/******************************************************************/
int get_data(char* charset,char* buffer,char* data)
{
  int   char_count=0,i;
  char* buff_pointer;
  char* data_pointer;
  int   mark=0;

  jb_toupper(charset);
  jb_toupper(buffer);
	
  /* looking for charset in buffer */
  char_count = jb_str_cmp(charset,buffer);

  /* charset not found in buffer */
  if(!char_count)
    return 0;

  data_pointer= data;
  buff_pointer= buffer;
  buff_pointer+= char_count-1+strlen(charset);

  for(i=0;i<strlen(buffer)-1;i++)
    {
      if(*buff_pointer=='(')
	{
	  mark++;
	}
      else if(*buff_pointer==')')
	{
	  if(mark==1)
	    {
	      fprintf(stderr,"Error: Invalid Action Code!\n");
	      return 0;
	    }

	  /* put a null-zero to indicate the end of string to data */
	  *data_pointer= '\0';
	  break;
	}
      else if(mark)
	{
	  mark=2;
	  /* ignore '"' character */
	  if(*buff_pointer!='"')
	    {
	      *data_pointer = *buff_pointer;
	      data_pointer++;
	    }
	}
      else
	return 0;

      buff_pointer++;
    }

  jb_toupper(data);

  return char_count;
}

/******************************************************************/
/* Name:         check_action_code                                */
/*                                                                */
/* Parameters:   data                                             */
/*               -The 3 character string indicating the action    */
/*                                                                */
/* Return Value: '0' if valid action code is detected, '1' if not */
/*                                                                */
/* Descriptions: Update the action to take in device list.        */
/*               A 'B' or a 'P' is stored for BYPASS and PORGRAM/ */
/*               CONFIGURE respectively.                          */
/*                                                                */
/******************************************************************/
int check_action_code(char* data)
{
  if(strcmp(data,"IGN") == 0)
    device_list[device_count].action= 'B';
  else if(strcmp(data,"CFG") == 0)
    device_list[device_count].action= 'P';
  else
    {
      fprintf(stderr,"Error: Invalid ActionCode: %s\n",data);
      return 0;
    }

  return 1;
}

/******************************************************************/
/* Name:         search_keyword                                   */
/*                                                                */
/* Parameters:   buffer, data                                     */
/*               -buffer is the line buffer stored when reading   */
/*                the CDF.                                        */
/*               -data is the string between brackets, '(' and ')'*/
/*                found in buffer.                                */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                  */
/* Descriptions: The function search for device records corres-   */
/*               pond to part name, path, file name and           */
/*               instruction length.                              */
/*                                                                */
/******************************************************************/
void search_keyword(char* buffer,char* data)
{
  char  Info_name[4][20] = { "PARTNAME","PATH","FILE","INSTRUCTIONREG" };
  int   i;

  for(i=0;i<4;i++)
    {
      if(get_data(Info_name[i],buffer,data))
	{
	  switch(i)
	    {
	    case 0:
	      strcpy(device_list[device_count-1].partname,data);
	      check_alt_dev(device_count);
	      break;
	    case 1:
	      strcpy(device_list[device_count-1].path,data);
	      break;
	    case 2:
	      strcpy(device_list[device_count-1].file,data);
	      break;
	    case 3:
	      device_list[device_count-1].inst_len = atoi(data);
	      ji_info[device_count-1]=device_list[device_count-1].inst_len;
	      break;
	    default:
	      break;
	    }
	}
    }
}

/******************************************************************/
/* Name:         check_alt_dev                                    */
/*                                                                */
/* Parameters:   dev_seq                                          */
/*               -dev_seq is the device sequence in JTAG chain.   */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                  */
/* Descriptions: The function matches the partname specified in   */
/*               CDF with Altera devices list. If matches, the    */
/*               JTAG chain information will be updated in device */
/*               _list. The action code is updated by ActionCode. */
/*               If the partname is not recognized, the device    */
/*               will be bypassed.                                */
/*                                                                */
/******************************************************************/
void check_alt_dev(int dev_seq)
{
  int i, j, altera = 0;
  dev_seq--;

  for(i = 0; i < MAX_DEV_LIST; i++)
    {
      if(strcmp(device_list[dev_seq].partname,device_name[i]) == 0)
	{
	  if(!(device_info[i][0] && device_info[i][1] && device_info[i][2]))
	    {
	      device_list[dev_seq].inst_len = device_info[i][3];
	      ji_info[dev_seq] = device_list[dev_seq].inst_len;
	      fprintf(stdout,"Warning: Device #%d not supported! Bypassed!\n",dev_seq+1);
	    }
	  else
	    {
	      device_list[dev_seq].idcode         = device_info[i][0];
	      device_list[dev_seq].jseq_max       = device_info[i][1];
	      device_list[dev_seq].jseq_conf_done = device_info[i][2];
	      device_list[dev_seq].inst_len       = device_info[i][3];
	      ji_info[dev_seq]                    = device_list[dev_seq].inst_len;
	      altera = 1;
	      
	      break;
	    }
	}
    }

  for(j=0;j<MAX_DEV_FAMILY;j++)
    {
      if(i<start_of_device_family[j])
	{
	  device_family = j-1;
	  fprintf(stdout,"Info: family: %s(%d)\n",family_name[j-1],j);
	  break;
	}
      else if (j==MAX_DEV_FAMILY-1 && i>=start_of_device_family[MAX_DEV_FAMILY-1] && altera ==1)
	{
	  device_family = MAX_DEV_FAMILY-1;
	  fprintf(stdout, "Info: family: %s(%d)\n",family_name[MAX_DEV_FAMILY-1],MAX_DEV_FAMILY);
	}
    }
  
  if(!altera)
    {
      device_list[dev_seq].idcode=0;
      device_list[dev_seq].jseq_max=0;
      device_list[dev_seq].jseq_conf_done=0;
      device_list[dev_seq].action='B';
    }
}

/******************************************************************/
/* Name:         configure                                        */
/*                                                                */
/* Parameters:   file,dev_seq,action                              */
/*               -file is the file.                               */
/*               -dev_seq is the device sequence in chains.       */
/*               -action is the action to take:BYPASS or PROGRAM  */
/*                                                                */
/* Return Value: None.                                            */
/*               		                                  */
/* Descriptions: Issue PROGRAM instruction to the device to be    */
/*               configured and BYPASS for the rest of the devices*/
/*               Call function that processes the source file.    */
/*                                                                */
/******************************************************************/
void configure(FILE *file, int dev_seq, int idcode)
{
  int i;
  int action = idcode? JI_PROGRAM:JI_BYPASS;

  /* Load PROGRAM instruction */
  setup_chain(device_count, dev_seq, ji_info,action);

  if (action == JI_PROGRAM)
    {
      /* Drive TDI HIGH while moving JSM to SHIFTDR */
      jb_drive_signal(SIG_TDI, TDI_HIGH, TCK_QUIET, BUFFER_OFF);
      js_shiftdr();
      /* Issue MAX_JTAG_INIT_CLOCK clocks in SHIFTDR state */
      for(i = 0; i < MAX_JTAG_INIT_CLOCK[device_family]; i++)
	{
	  jb_drive_signal(SIG_TDI, TDI_HIGH, TCK_TOGGLE, BUFFER_ON);
	}
      
      /* Start dumping configuration bits into TDI and clock with TCK */
      process_file_input(file, idcode);
      
      /* Move JSM to RUNIDLE */
      js_updatedr();
      js_runidle();
    }
}

/******************************************************************/
/* Name:         process_file_input                               */
/*                                                                */
/* Parameters:   finputid                                         */           
/*               -programming file pointer.                       */
/*                                                                */
/* Return Value: None.                                            */
/*                                                                */
/* Descriptions: Get programming file size, parse through every   */
/*               single byte and dump to parallel port.           */
/*                                                                */
/******************************************************************/
void process_file_input(FILE *infile, int idcode)
{
  int seek_position=0, one_byte=0;
  long int file_size=0, i=0;
	
  /* Get file size */
  seek_position = fseek(infile, 0, SEEK_END);

  if(seek_position != 0)
    {
      fprintf( stderr, "Error: End of file could not be located!" );
      return;
    }

  file_size = ftell(infile);

  /* sends the notfull bytebuffer at the end */
  sendLastBuffer = 1;

  fprintf(stdout, "Info: Programming file size: %ld\n", file_size);
  /* Start configuration */
  /* Reset file pointer */
  fseek(infile, 0, SEEK_SET);

  fprintf(stdout,"Info: Start configuration process.\nPlease wait...");

  /* Loop through every single byte */
  for(i = 0; i < file_size; i++)
    {
      /*Ignore first 44 bytes in the rbf file for Cyclone device*/
      if(i < 44 && ((idcode > 0x2080000 && idcode < 0x2086000) || idcode == 0x20B40DD))
	{
	  one_byte = fgetc(infile);
	}
      else
	{
	  int bit = 0, j;
	  one_byte = fgetc(infile);
	  
	  /* Program a byte,from LSb to MSb */
	  for (j = 0; j < 8; j++ )
	    {
	      bit = one_byte >> j;
	      bit = bit & 0x1;
	      
	      /* Dump to TDI and drive a positive edge pulse at the same time */
	      jb_drive_signal(SIG_TDI,bit,TCK_TOGGLE,BUFFER_ON);	
	    }
	}

/*       if ((i % 1024) == 0) */
/* 	{ */
/* 	  fprintf(stdout, "%3ld%%\r", (i*100)/file_size); */
/* 	  fflush(stdout); */
/* 	} */
    }

  fprintf(stdout,"done\n");
}
