#include "compat.h"

#include <stdio.h>

#ifdef __linux__

#include <ftdi.h>
#include <usb.h>

static struct ftdi_device_list *ftdi_devlist;

int ftdi_setup(FTDI_HANDLE_T *handle)
{
  int status;
  struct ftdi_device_list *list;

  status = ftdi_init(handle);
  if (status != FTDI_OK)
    {
      fprintf(stderr, "Error: ftdi_init() failed (%d)\n", status);
      return -1;
    }
  handle->usb_write_timeout = 1000;

  status = ftdi_usb_find_all(handle, &ftdi_devlist, VENDOR_ID, PRODUCT_ID);
  if (status < FTDI_OK)
    {
      fprintf(stderr, "Error: ftdi_usb_find_all() failed (%d)\n", status);
      return -1;
    }

  for (list = ftdi_devlist; list != NULL; list = list->next)
    {
      char manufacturer[100], description[100], serial[20];
      status = ftdi_usb_get_strings(handle, list->dev, manufacturer, 100,
                                    description, 100, serial, 20);
      if (status != FTDI_OK)
        {
          fprintf(stderr, "Error: ftdi_usb_get_strings() failed (%d)\n", status);
          fprintf(stderr, "Error: %s\n", ftdi_get_error_string(handle));
          return -1;
        }

      fprintf(stdout,"Info: manufacturer: %s\nInfo: description: %s\n"
              "Info: serial: %s\n", manufacturer, description, serial);

      status = ftdi_usb_open_dev(handle, list->dev);
      if (status != FTDI_OK)
        {
          fprintf(stderr, "Error: ftdi_usb_open_dev() failed (%d)\n", status);
          fprintf(stderr, "Error: %s\n", ftdi_get_error_string(handle));
          return -1;
        }
      status = ftdi_set_baudrate(handle, BAUDRATE);
      if (status != FTDI_OK)
        {
          fprintf(stderr, "Error: ftdi_set_baudrate() failed (%d)\n", status);
          fprintf(stderr, "Error: %s\n", ftdi_get_error_string(handle));
          return -1;
        }
      
      status = ftdi_set_bitmode(handle, BITMASK, RESET_MODE);
      status = ftdi_set_bitmode(handle, BITMASK, ASYN_BITBANG_MODE);
      if (status != FTDI_OK)
        {
          fprintf(stderr,"Error: Asynchronous Bit Bang Mode inactive! \n");
          fprintf(stderr, "Error: %s\n", ftdi_get_error_string(handle));
        }
      else
        {
          fprintf(stdout,"Info: Asynchronous Bit Bang Mode active! \n");
        }
    }

  return 0;
}

int ftdi_teardown(FTDI_HANDLE_T *handle)
{
  int status;

  status = ftdi_set_bitmode(handle, BITMASK, RESET_MODE);
  if (status != 0)
    {
      fprintf(stderr, "Error: Resetting bitbang mode failed (%d)\n", status);
    }
  
  ftdi_list_free(&ftdi_devlist);
  ftdi_usb_close(handle);
  ftdi_deinit(handle);

  return 0;
}

int ftdi_get_bitmode(FTDI_HANDLE_T *handle, unsigned char *mode)
{
  return ftdi_read_pins(handle, mode);
}

int ftdi_write(FTDI_HANDLE_T *handle, void *buffer, int len)
{
  return ftdi_write_data(handle, buffer, len);
}

#else

#include <windows.h>
#include "FTD2XX.H"

int ftdi_setup(FTDI_HANDLE_T *handle)
{
  int status;
  char buf[64];

  status = FT_ListDevices(0,buf,FT_LIST_BY_INDEX|FT_OPEN_BY_SERIAL_NUMBER);
  *handle = FT_W32_CreateFile(buf,GENERIC_READ|GENERIC_WRITE,0,0,OPEN_EXISTING,
				FILE_ATTRIBUTE_NORMAL|FT_OPEN_BY_SERIAL_NUMBER,0);

  if (*handle == INVALID_HANDLE_VALUE) /* FT_W32_CreateDevice failed */
    {
      fprintf(stderr, "Error: No FTDI USB Device found! \n");
      return -1;
    }

  status = FT_SetBitMode(*handle,BITMASK,RESET_MODE);
  status = FT_SetBitMode(*handle,BITMASK,ASYN_BITBANG_MODE);
  if(status == FT_OK)
    {
      fprintf(stderr, "Info: Asynchronous Bit Bang Mode active! \n");
    }
  else
    {
      fprintf(stderr, "Error: Asynchronous Bit Bang Mode inactive! \n");
    }

  return 0;
}

int ftdi_teardown(FTDI_HANDLE_T *handle)
{
  int status;

  status = FT_SetBitMode(*handle, BITMASK, RESET_MODE);
  if(status != FT_OK)
    {
      fprintf( stdout, "Error: Bit Bang Mode Reset!");
    }

  status = FT_W32_CloseHandle(*handle);
  if(status == FT_OK)
    {
      fprintf( stdout, "Error: Closing FT_Handle!");
    }

  return 0;
}

int ftdi_get_bitmode(FTDI_HANDLE_T *handle, unsigned char *mode)
{
  return FT_GetBitMode(*handle, mode);
}

int ftdi_write(FTDI_HANDLE_T *handle, void *buffer, int len)
{
  unsigned long int written;
  return FT_Write(*handle, buffer, len, &written);
}


#endif
