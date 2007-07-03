rem set options=-93 -quiet -check_synthesis -lint -pedanticerrors
set options=-93 -quiet
rmdir /S/Q work
vlib work
vcom %options% ../../simpcon/sc_pack.vhd
vcom %options% ../../simpcon/sc_arbiter_pack.vhd
rem %options% tb_arbiter.vhd
vcom %options% tb_arbiter_3masters.vhd
vcom %options% ../sc_arbiter.vhd

vsim -do sim.do tb_arbiter

