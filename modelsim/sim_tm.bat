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

vcom %options% ^
%jopdir%/simulation/sim_jop_config_100.vhd ^
%jopdir%/core/jop_types.vhd ^
%jopdir%/simpcon/sc_pack.vhd ^
%jopdir%/simulation/sim_ram.vhd ^
%jopdir%/simulation/sim_pll.vhd ^
%jopdir%/simulation/sim_jbc.vhd ^
%jopdir%/simulation/sim_rom.vhd ^
%jopdir%/simulation/sim_memory.vhd ^
%jopdir%/simulation/sim_sc_uart.vhd ^
%jopdir%/simulation/bytecode.vhd ^
%jopdir%/simulation/microcode.vhd ^
%jopdir%/jtbl.vhd ^
%jopdir%/core/cache.vhd ^
%jopdir%/cache/ocache.vhd ^
%jopdir%/memory/sc_sram32_flash.vhd ^
%jopdir%/memory/mem_sc.vhd ^
%jopdir%/memory/sdpram.vhd ^
%jopdir%/core/mul.vhd ^
%jopdir%/core/bcfetch.vhd ^
%jopdir%/core/fetch.vhd ^
%jopdir%/core/decode.vhd ^
%jopdir%/core/shift.vhd ^
%jopdir%/core/stack.vhd ^
%jopdir%/core/core.vhd ^
%jopdir%/scio/sc_sys.vhd ^
%jopdir%/scio/scio_min.vhd ^
%jopdir%/simpcon/sc_arbiter_pack.vhd ^
%jopdir%/simpcon/sc_arbiter_rttm_tdma.vhd ^
%jopdir%/scio/cmpsync.vhd ^
%jopdir%/core/jopcpu.vhd ^
%jopdir%/rttm/tm_pack.vhd ^
%jopdir%/rttm/tm_internal_pack.vhd ^
%jopdir%/rttm/tm_coordinator.vhd ^
%jopdir%/rttm/tag.vhd ^
%jopdir%/rttm/write_fifo_buffer.vhd ^
%jopdir%/rttm/flags_ram.vhd ^
%jopdir%/rttm/tm.vhd ^
%jopdir%/rttm/tm_state_machine.vhd ^
%jopdir%/top/joprttm.vhd ^
%jopdir%/simulation/tb_jop.vhd

vsim %* tb_jop
