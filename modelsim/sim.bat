set jopdir=../vhdl
rem set options=-93 -quiet -check_synthesis -lint -pedanticerrors
set options=-93 -quiet
rmdir /S/Q work
vlib work
vcom %options% %jopdir%/simulation/sim_jop_types_100.vhd
vcom %options% %jopdir%/simulation/sim_ram.vhd
vcom %options% %jopdir%/simulation/sim_pll.vhd
vcom %options% %jopdir%/simulation/sim_jbc.vhd
vcom %options% %jopdir%/simulation/sim_rom.vhd
vcom %options% %jopdir%/simulation/sim_memory.vhd
vcom %options% %jopdir%/simulation/sim_uart.vhd
vcom %options% %jopdir%/jtbl.vhd
vcom %options% %jopdir%/offtbl.vhd
vcom %options% %jopdir%/core/cache.vhd
vcom %options% %jopdir%/core/mem32.vhd
vcom %options% %jopdir%/core/mul.vhd
vcom %options% %jopdir%/core/extension.vhd
vcom %options% %jopdir%/core/bcfetch.vhd
vcom %options% %jopdir%/core/fetch.vhd
vcom %options% %jopdir%/core/decode.vhd
vcom %options% %jopdir%/core/shift.vhd
vcom %options% %jopdir%/core/stack.vhd
vcom %options% %jopdir%/core/core.vhd
rem vcom %options% %jopdir%/io/fifo.vhd
rem vcom %options% %jopdir%/io/uart.vhd
vcom %options% %jopdir%/io/cnt.vhd
vcom %options% %jopdir%/io/iomin.vhd
vcom %options% %jopdir%/top/jopcyc.vhd
vcom %options% %jopdir%/simulation/tb_jop.vhd
pause Start simulation?
vsim -do sim.do tb_jop
