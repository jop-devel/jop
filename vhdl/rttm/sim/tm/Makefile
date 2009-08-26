JOPDIR=../../..

OPTIONS=-2008 -quiet -check_synthesis -lint -pedanticerrors -suppress 1236
#OPTIONS=-2008 -quiet -suppress 1236

include Makefile.in

ifeq ($(BATCH),y)
	RUN=vsim -c -do ../sim_batch.do $(TARGET)
else
	RUN=vsim -i -do sim.do $(TARGET)
endif

all: work src mem_main.dat
	$(RUN)	

src:
	vcom $(OPTIONS) $(SRC)

work:
	vlib work
	
mem_main.dat:
	touch $@

clean:
	-rm -rf work
	-rm transcript
	-rm *.wlf
	-rm mem_main.dat
