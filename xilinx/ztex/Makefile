DESIGN		= ztex
TOP		= top

ZTEX		= ~/ztex/java

SRC_MAIN	= \
	../../vhdl/top/jop_config_global.vhd\
	../../vhdl/simpcon/sc_pack.vhd\
	../../vhdl/simpcon/sc2mig.vhd\
	../../vhdl/scio/fifo.vhd\
	../../vhdl/scio/sc_uart.vhd\
	../../vhdl/scio/sc_sys.vhd\
	../../vhdl/scio/scio_min.vhd\
	../../vhdl/core/jop_types.vhd\
	../../vhdl/core/bcfetch.vhd\
	../../vhdl/core/fetch.vhd\
	../../vhdl/core/shift.vhd\
	../../vhdl/core/cache.vhd\
	../../vhdl/core/stack.vhd\
	../../vhdl/core/mul.vhd\
	../../vhdl/core/core.vhd\
	../../vhdl/core/decode.vhd\
	../../vhdl/core/jopcpu.vhd\
	../../vhdl/cache/ocache.vhd\
	../../vhdl/xilinx/xs3_jbc.vhd\
	../../vhdl/memory/mem_sc.vhd\
	../../vhdl/memory/sdpram.vhd\
	../../vhdl/xilinx/xram.vhd\
	../../vhdl/jtbl.vhd\
	../../vhdl/rom.vhd\
	../../vhdl/xv4ram_block.vhd\
	../../vhdl/top/jop_config_ztex.vhd\
	../../vhdl/top/top_ztex.vhd

SRC_MIG 	= \
	mig_37/iodrp_controller.vhd\
	mig_37/iodrp_mcb_controller.vhd\
	mig_37/mcb_raw_wrapper.vhd\
	mig_37/mcb_soft_calibration_top.vhd\
	mig_37/mcb_soft_calibration.vhd\
	mig_37/memc3_infrastructure.vhd\
	mig_37/memc3_wrapper.vhd\
	mig_37/mig_37.vhd

SRC	= $(SRC_MAIN) $(SRC_MIG)

DEVICE		= xc6slx25-ftg256-3

all:		$(DESIGN).bit

clean:
		rm -f *~

cleanall:	clean
		rm -rf build $(DESIGN).bit

check:		build/$(DESIGN).ngc

build/$(DESIGN).src:
		@[ -d build ] || mkdir build
		@rm -f $@
		for i in $(SRC); do echo vhdl work ../$$i >> $@; done

build/$(DESIGN).xst: build/$(DESIGN).src
		echo "run" > $@
		echo "-top $(TOP)" >> $@
		echo "-p $(DEVICE)" >> $@
		echo "-ifn $(DESIGN).src" >> $@
		echo "-ofn $(DESIGN)" >> $@
		echo "-ofmt NGC" >> $@
		echo "-lso ../$(DESIGN).lso" >>$@
		echo "-ifmt mixed" >> $@
		echo "-ofn counter_3bit" >> $@
		echo "-opt_mode speed" >> $@
		echo "-opt_level 2" >> $@
		echo "-iuc NO" >> $@
		echo "-keep_hierarchy NO" >> $@
		echo "-netlist_hierarchy as_optimized" >> $@
		echo "-rtlview Yes" >> $@
		echo "-glob_opt AllClockNets" >> $@
		echo "-read_cores YES" >> $@
		echo "-write_timing_constraints NO" >> $@
		echo "-cross_clock_analysis NO" >> $@
		echo "-hierarchy_separator /" >> $@
		echo "-bus_delimiter <>" >> $@
		echo "-case maintain" >> $@
		echo "-slice_utilization_ratio 100" >> $@
		echo "-bram_utilization_ratio 100" >> $@
#		echo "-fsm_extract YES -fsm_encoding Auto" >> $@
#		echo "-safe_implementation No" >> $@
#		echo "-fsm_style lut" >> $@
#		echo "-ram_extract Yes" >> $@
#		echo "-ram_style Auto" >> $@
#		echo "-rom_extract Yes" >> $@
#		echo "-shreg_extract YES" >> $@
#		echo "-rom_style Auto" >> $@
#		echo "-auto_bram_packing NO" >> $@
#		echo "-resource_sharing YES" >> $@
#		echo "-async_to_sync NO" >> $@
#		echo "-iobuf YES" >> $@
#		echo "-max_fanout 500" >> $@
#		echo "-bufg 32" >> $@
#		echo "-register_duplication YES" >> $@
#		echo "-register_balancing No" >> $@
#		echo "-optimize_primitives NO" >> $@
#		echo "-use_clock_enable Auto" >> $@
#		echo "-use_sync_set Auto" >> $@
#		echo "-use_sync_reset Auto" >> $@
#		echo "-iob auto" >> $@
#		echo "-equivalent_register_removal YES" >> $@
#		echo "-slice_utilization_ratio_maxmargin 5" >> $@

build/$(DESIGN).ngc: build/$(DESIGN).xst $(SRC)
		cd build && xst -intstyle ise -ifn $(DESIGN).xst -ofn $(DESIGN).syr

build/$(DESIGN).ngd: build/$(DESIGN).ngc $(DESIGN).ucf
		cd build && ngdbuild -intstyle ise -dd _ngo -nt timestamp -uc ../$(DESIGN).ucf -p $(DEVICE) $(DESIGN).ngc $(DESIGN).ngd

build/$(DESIGN).ncd: build/$(DESIGN).ngd
		cd build && map -w -intstyle ise -p $(DEVICE) -pr off -c 100 -timing -o $(DESIGN)_map.ncd $(DESIGN).ngd $(DESIGN).pcf

build/$(DESIGN)_r.ncd: build/$(DESIGN).ncd
		cd build && par -w -intstyle ise -ol std $(DESIGN)_map.ncd $(DESIGN).ncd $(DESIGN).pcf

build/$(DESIGN)_r.twr: build/$(DESIGN)_r.ncd
		cd build && trce -intstyle ise -e 3 -xml $(DESIGN) $(DESIGN).ncd -o $(DESIGN).twr $(DESIGN).pcf

$(DESIGN).bit:	build/$(DESIGN)_r.ncd build/$(DESIGN)_r.twr
		cd build && bitgen -w -intstyle ise -f ../$(DESIGN).ut $(DESIGN).ncd
		@cp -f build/$(DESIGN).bit $@

flash:
		sudo $(ZTEX)/FWLoader -c -rf -uf $(DESIGN).bit
