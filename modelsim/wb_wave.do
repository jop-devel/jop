onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/int_res
add wave -noupdate -format Literal -radix ascii /tb_jop/cmp_jop/cmp_io/cmp_ua/char
add wave -noupdate -format Literal -radix unsigned /tb_jop/cmp_jop/cmp_core/cmp_bcf/jinstr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_fch/pc_inc
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_fch/pcwait
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_core/cmp_fch/bsy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_dec/ir
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_core/cmp_dec/sel_lmux
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_ext/ext_addr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_dec/wr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_dec/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/dout
add wave -noupdate -divider mem_wb
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/din
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_addr_wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_wr_addr
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_mem/mem_bc_rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_wr
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_mem/state
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_mem/mem_bsy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/wb_out.adr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_rd_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/dout
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/a
add wave -noupdate -divider {bc load}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_len
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_mem_start
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_wr_addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_wr_ena
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_mem/cache_bcstart
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_mem/cache_in_cache
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_mem/cache_rdy
add wave -noupdate -divider wb_mem_if
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_wbm/wb_in
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_wbm/wb_out
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_wbm/early_ack
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_wbm/wr
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_wbm/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_wbm/state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_wbm/wait_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_wbm/ram_addr
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_wbm/ram_dout_en
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_wbm/ram_din
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_wbm/ram_dout
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/a
add wave -noupdate -divider {external signals}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Logic /tb_jop/main_mem/ncs
add wave -noupdate -format Logic /tb_jop/main_mem/noe
add wave -noupdate -format Logic /tb_jop/main_mem/nwr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/fl_a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/fl_d
add wave -noupdate -format Logic /tb_jop/cmp_jop/fl_ncs
add wave -noupdate -format Logic /tb_jop/cmp_jop/fl_ncsb
add wave -noupdate -format Logic /tb_jop/cmp_jop/fl_noe
add wave -noupdate -format Logic /tb_jop/cmp_jop/fl_nwe
add wave -noupdate -format Logic /tb_jop/cmp_jop/fl_rdy
add wave -noupdate -divider {wishbone IO}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_in
add wave -noupdate -format Literal -height 18 -label .dat_i -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_in.dat_i
add wave -noupdate -format Logic -height 18 -label .ack_i -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_in.ack_i
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_out
add wave -noupdate -format Literal -height 18 -label .dat_o -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_out.dat_o
add wave -noupdate -format Literal -height 18 -label .adr_o -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_out.adr_o
add wave -noupdate -format Logic -height 18 -label .we_o -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_out.we_o
add wave -noupdate -format Logic -height 18 -label .cyc_o -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_out.cyc_o
add wave -noupdate -format Logic -height 18 -label .stb_o -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/wbtop/wb_out.stb_o
add wave -noupdate -divider {wb slave}
add wave -noupdate -divider {wishbone IO end}
add wave -noupdate -divider execute
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/din
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/b
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(0)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(1)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(2)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(3)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(4)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(5)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(128)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(129)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(130)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(131)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(132)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(133)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(134)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(135)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(136)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(137)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(138)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(139)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(140)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(141)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(142)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/cmp_ram/ram(143)
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {287726 ps} 0} {{Cursor 2} {1113253 ps} 0}
configure wave -namecolwidth 259
configure wave -valuecolwidth 54
configure wave -justifyvalue left
configure wave -signalnamewidth 0
configure wave -snapdistance 10
configure wave -datasetprefix 0
configure wave -rowmargin 4
configure wave -childrowmargin 2
configure wave -gridoffset 0
configure wave -gridperiod 1
configure wave -griddelta 40
configure wave -timeline 0
update
WaveRestoreZoom {0 ps} {10500 ns}
