JOPDIR=../../..

OPTIONS=-2008 -quiet -check_synthesis -lint -pedanticerrors -suppress 1236
#OPTIONS=-2008 -quiet -suppress 1236

include Makefile.in

ifeq ($(BATCH),y)
	RUN=vsim -c -do ../sim_batch.do $(TARGET)
else
	RUN=vsim -i -do sim.do $(TARGET)
endif

all: work src
	$(RUN)	

src:
	vcom $(OPTIONS) $(SRC)

work:
	vlib work
	
clean:
	rm -rf work
	rm -f transcript
	rm -f *.wlf
