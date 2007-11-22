rem set options=-93 -quiet -check_synthesis -lint -pedanticerrors
set options=-93 -quiet
rmdir /S/Q work
vlib work
vcom %options% ../../../core/jop_types.vhd
vcom %options% tb_cmpsync_3masters.vhd
vcom %options% ../../cmpsync.vhd

vsim -do sim.do tb_cmpsync