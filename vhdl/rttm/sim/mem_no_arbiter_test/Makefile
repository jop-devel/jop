JOPDIR=../../..

#OPTIONS=-93 -quiet -check_synthesis -lint -pedanticerrors
OPTIONS=-93 -quiet

include Makefile.in

ifeq ($(BATCH),true)
	RUN=vsim -c -do ../sim_batch.do $(TARGET)
else
	RUN=vsim -i -do sim.do $(TARGET)
endif

all: work src mem_main.dat
	$(RUN)	

src: $(SRC)

%.vhd: force
	vcom $(OPTIONS) $*.vhd

force: ;

work:
	vlib work
	
mem_main.dat:
	touch $@

clean:
	-rm -rf work
	-rm transcript
	-rm *.wlf
	-rm mem_main.dat
