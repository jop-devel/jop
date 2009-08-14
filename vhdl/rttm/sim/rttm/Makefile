JOPDIR=../../..

#OPTIONS=-93 -quiet -check_synthesis -lint -pedanticerrors
OPTIONS=-93 -quiet

include Makefile.in

ifeq ($(BATCH),true)
	RUN=vsim -c -do ../sim_batch.do $(TARGET)
else
	RUN=vsim -do sim.do $(TARGET)
endif

all: work src
	$(RUN)	

src: $(SRC)

%.vhd: force
	vcom $(OPTIONS) $*.vhd

force: ;

work:
	vlib work

clean:
	-rm -rf work
	-rm transcript
	-rm *.wlf
