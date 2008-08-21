set jopdir=../vhdl
rem set options=-93 -quiet -check_synthesis -lint -pedanticerrors
set options=-93 -quiet
rmdir /S/Q work
rmdir /S/Q grlib
rmdir /S/Q gaisler
rmdir /S/Q techmap
vlib work
vlib grlib
vlib gaisler
vlib techmap
rem vcom -work grlib ../ext/gaisler/version.vhd
rem vcom -work grlib ../ext/gaisler/stdlib.vhd
rem vcom -work grlib ../ext/gaisler/amba.vhd
rem vcom -work gaisler ../ext/gaisler/devices.vhd
rem vcom -work techmap ../ext/gaisler/gencomp.vhd
rem vcom -work gaisler ../ext/gaisler/memctrl.vhd
rem vcom -work gaisler ../ext/gaisler/srctrl.vhd
vcom %options% %jopdir%/simulation/sim_jop_config_100.vhd
vcom %options% %jopdir%/core/jop_types.vhd
vcom %options% %jopdir%/simpcon/sc_pack.vhd
rem vcom %options% %jopdir%/simpcon/sc2ahbsl.vhd
vcom %options% %jopdir%/simulation/sim_ram.vhd
vcom %options% %jopdir%/simulation/sim_pll.vhd
vcom %options% %jopdir%/simulation/sim_jbc.vhd
vcom %options% %jopdir%/simulation/sim_rom.vhd
vcom %options% %jopdir%/simulation/sim_memory.vhd
vcom %options% %jopdir%/simulation/sim_sc_uart.vhd
vcom %options% %jopdir%/simulation/bytecode.vhd
vcom %options% %jopdir%/jtbl.vhd
vcom %options% %jopdir%/offtbl.vhd
vcom %options% %jopdir%/core/cache.vhd

vcom %options% %jopdir%/memory/sc_sram32_flash.vhd
rem vcom %options% %jopdir%/memory/sc_sram16.vhd

vcom %options% %jopdir%/memory/mem_sc.vhd
vcom %options% %jopdir%/memory/sdpram.vhd
vcom %options% %jopdir%/core/mul.vhd
vcom %options% %jopdir%/core/extension.vhd
vcom %options% %jopdir%/core/bcfetch.vhd
vcom %options% %jopdir%/core/fetch.vhd
vcom %options% %jopdir%/core/decode.vhd
vcom %options% %jopdir%/core/shift.vhd
vcom %options% %jopdir%/core/stack.vhd
vcom %options% %jopdir%/core/core.vhd
vcom %options% %jopdir%/scio/sc_sys.vhd
vcom %options% %jopdir%/scio/scio_min.vhd
vcom %options% %jopdir%/simpcon/sc_arbiter_pack.vhd
vcom %options% %jopdir%/simpcon/sc_arbiter_fair.vhd
vcom %options% %jopdir%/scio/cmpsync.vhd
vcom %options% %jopdir%/core/jopcpu.vhd

vcom %options% %jopdir%/top/jopmul.vhd
rem vcom %options% %jopdir%/top/jopmul_256x16.vhd

vcom %options% %jopdir%/simulation/tb_jop.vhd
rem vcom %options% %jopdir%/simulation/tb_jop_sram16.vhd

vsim -do sim_cmp.do tb_jop
