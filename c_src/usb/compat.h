#ifndef _COMPAT_H_
#define _COMPAT_H_

#define VENDOR_ID         0x0403
#define PRODUCT_ID        0x6010

#define BAUDRATE          96000

#define BITMASK           0xFB
#define RESET_MODE        0x00
#define ASYN_BITBANG_MODE 0x01

#ifdef __linux__

#include <ftdi.h>

#define FTDI_HANDLE_T struct ftdi_context
#define FTDI_OK 0

#else

#include <windows.h>
#include "FTD2XX.H"

#define FTDI_HANDLE_T HANDLE
#define FTDI_OK FT_OK

#endif

int ftdi_setup(FTDI_HANDLE_T *handle);
int ftdi_teardown(FTDI_HANDLE_T *handle);

int ftdi_get_bitmode(FTDI_HANDLE_T *handle, unsigned char *mode);

int ftdi_write(FTDI_HANDLE_T *handle, void *buffer, int len);

#endif /* _COMPAT_H_ */
