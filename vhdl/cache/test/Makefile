#
#	Makefile for ModelSim simulation
#


JOPDIR=../../../vhdl
#OPTIONS=-93 -quiet -check_synthesis -lint -pedanticerrors
OPTIONS=-93 -quiet

ifeq ($(WINDIR),)
	WINE=wine
	S=:
else
	WINE=
	S=\;
endif

all: clean base
	$(WINE) vcom $(OPTIONS) tb_ocache.vhd
	$(WINE) vsim -do sim.do tb_ocache

acache: clean base
	$(WINE) vcom $(OPTIONS) tb_acache.vhd
	$(WINE) vsim -do sim_ac.do tb_acache

base:
	$(WINE) vlib work
	$(WINE) vcom $(OPTIONS) $(JOPDIR)/simpcon/sc_pack.vhd
	$(WINE) vcom $(OPTIONS) $(JOPDIR)/top/jop_config_global.vhd
#	$(WINE) vcom $(OPTIONS) $(JOPDIR)/simulation/sim_jop_config_100.vhd
	$(WINE) vcom $(OPTIONS) $(JOPDIR)/core/jop_types.vhd
	$(WINE) vcom $(OPTIONS) $(JOPDIR)/simpcon/sc_pack.vhd
	$(WINE) vcom $(OPTIONS) $(JOPDIR)/cache/ocache.vhd
	$(WINE) vcom $(OPTIONS) $(JOPDIR)/cache/acache.vhd

clean:
	-rm -rf work
	-rm -rf transcript
#	-rm -rf techmap
	-rm *.wlf
