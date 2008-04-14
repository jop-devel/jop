CC=gcc
CFLAGS=-pedantic -Wall

ifeq ($(WINDIR),)
	LDLIBS=-lftdi
else
	LDLIBS=FTD2XX.lib
endif

all: USBRunner

USBRunner: usbRunner.o jb_const.o jb_device.o jb_io.o jb_jtag.o compat.o
	$(CC) $(LDFLAGS) -o $@ $^ $(LDLIBS)

compat.o: compat.c compat.h FTD2XX.H
jb_const.o: jb_const.c jb_const.h jb_io.h compat.h
jb_device.o: jb_device.c jb_device.h
jb_io.o: jb_io.c jb_const.h jb_io.h compat.h jb_jtag.h
jb_jtag.o: jb_jtag.c jb_const.h jb_io.h compat.h jb_jtag.h
usbRunner.o: usbRunner.c jb_const.h jb_io.h compat.h jb_device.h jb_jtag.h

clean::
	rm -f *.o *~
