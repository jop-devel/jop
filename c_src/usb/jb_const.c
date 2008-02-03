#include "jb_const.h"

/******************************************************************/
/* Important Notes                                                */
/* ---------------                                                */
/* The following variables are used throughout the program and    */
/* specifically applies to PORT==WINDOWS_NT. To port to other     */
/* platforms, e.g. EMBEDDED, user should modify ReadPort and      */
/* WritePort functions to translate the signals to I/O port       */
/* architecture of your system through software. The summary of   */
/* Port and Bit Position of parallel port architecture is shown   */
/* below:                                                         */
/*                                                                */
/* bit       7    6    5    4    3    2    1    0                 */
/* port 0    -   TDI   -    -    -    -   TMS  TCK                */
/* port 1   TDO#  -    -    -    -    -    -    -                 */
/* port 2    -    -    -    -    -    -    -    -                 */
/* # - inverted                                                   */
/*                                                                */
/******************************************************************/

/******************************************************************/
/* sig_port_maskbit                                               */
/* The variable that tells the port (index from the parallel port */
/* base address) and the bit positions of signals used in JTAG    */
/* configuration.                                                 */
/*                                                                */
/* sig_port_maskbit[X][0]                                         */
/*   where X - SIG_* (e.g. SIG_TCK),tells the port where the      */
/*   signal falls into.                                           */
/* sig_port_maskbit[X][1]                                         */
/*   where X - SIG_* (e.g. SIG_TCK),tells the bit position of the */
/*   signal the sequence is SIG_TCK,SIG_TMS,SIG_TDI and SIG_TDO   */
/*                                                                */
/******************************************************************/

const int sig_port_maskbit[4][2] = { { PORT_0,0x1 }, { PORT_0,0x2 }, { PORT_0,0x40 }, { PORT_1,0x80 } };

/******************************************************************/
/* port_mode_data                                                 */
/* The variable that sets the signals to particular values in     */
/* different modes,namely RESET and USER modes.                   */
/*                                                                */
/* port_mode_data[0][Y]                                           */
/*   where Y - port number,gives the values of each signal for    */
/*   each port in RESET mode.                                     */
/* port_mode_data[1][Y]                                           */
/*   where Y - port number,gives the values of each signal for    */
/*   each port in USER mode.                                      */
/*                                                                */
/******************************************************************/

const int port_mode_data[2][3] = { {0x42, 0x0, 0x0E}, {0x42, 0x0, 0x0C} };

/******************************************************************/
/* port_data                                                      */
/* The variable that holds the current values of signals for      */
/* every port. By default, they hold the values in reset mode     */
/* (PM_RESET_<ByteBlaster used>).                                 */
/*                                                                */
/* port_data[Z]                                                   */
/* where Z - port number, holds the value of the port.            */
/*                                                                */
/******************************************************************/

int port_data[3] = { 0x42, 0x0, 0x0E };/* Initial value for Port 0, 1 and 2*/

int device_count = 0; /* Number of JTAG-comnpatible device in chain */
int device_family = 0; /* Device Family, check jb_device.h for detail */

struct list device_list[MAX_DEVICE_ALLOW];
