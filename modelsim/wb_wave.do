onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/int_res
add wave -noupdate -format Literal -radix ascii /tb_jop/jop/io/ua/char
add wave -noupdate -format Literal -radix unsigned /tb_jop/jop/core/bcf/jinstr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/fch/pc_inc
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/core/fch/pcwait
add wave -noupdate -format Logic /tb_jop/jop/core/fch/bsy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/dec/ir
add wave -noupdate -format Literal /tb_jop/jop/core/dec/sel_lmux
add wave -noupdate -format Literal /tb_jop/jop/ext/ext_addr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/core/dec/wr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/core/dec/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/ext/dout
add wave -noupdate -divider mem_wb
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/din
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/mem/mem_rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/mem/mem_addr_wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/mem_wr_addr
add wave -noupdate -format Logic /tb_jop/jop/mem/mem_bc_rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/mem/mem_wr
add wave -noupdate -format Literal /tb_jop/jop/mem/state
add wave -noupdate -format Logic /tb_jop/jop/mem/mem_bsy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/wb_out.adr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/mem_rd_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/dout
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/a
add wave -noupdate -divider {bc load}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/bc_len
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/bc_mem_start
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/bc_wr_addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/bc_wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/jop/mem/bc_wr_ena
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/mem/bc_cnt
add wave -noupdate -format Literal /tb_jop/jop/mem/cache_bcstart
add wave -noupdate -format Logic /tb_jop/jop/mem/cache_in_cache
add wave -noupdate -format Logic /tb_jop/jop/mem/cache_rdy
add wave -noupdate -divider wb_mem_if
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/jop/wbm/wb_in
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/jop/wbm/wb_out
add wave -noupdate -format Logic /tb_jop/jop/wbm/early_ack
add wave -noupdate -format Logic /tb_jop/jop/wbm/wr
add wave -noupdate -format Logic /tb_jop/jop/wbm/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/wbm/state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/wbm/wait_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/wbm/ram_addr
add wave -noupdate -format Logic /tb_jop/jop/wbm/ram_dout_en
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/wbm/ram_din
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/wbm/ram_dout
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/a
add wave -noupdate -divider {external signals}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Logic /tb_jop/main_mem/ncs
add wave -noupdate -format Logic /tb_jop/main_mem/noe
add wave -noupdate -format Logic /tb_jop/main_mem/nwr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/fl_a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/fl_d
add wave -noupdate -format Logic /tb_jop/jop/fl_ncs
add wave -noupdate -format Logic /tb_jop/jop/fl_ncsb
add wave -noupdate -format Logic /tb_jop/jop/fl_noe
add wave -noupdate -format Logic /tb_jop/jop/fl_nwe
add wave -noupdate -format Logic /tb_jop/jop/fl_rdy
add wave -noupdate -divider {wishbone IO}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_in
add wave -noupdate -format Literal -height 18 -label .dat_i -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_in.dat_i
add wave -noupdate -format Logic -height 18 -label .ack_i -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_in.ack_i
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_out
add wave -noupdate -format Literal -height 18 -label .dat_o -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_out.dat_o
add wave -noupdate -format Literal -height 18 -label .adr_o -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_out.adr_o
add wave -noupdate -format Logic -height 18 -label .we_o -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_out.we_o
add wave -noupdate -format Logic -height 18 -label .cyc_o -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_out.cyc_o
add wave -noupdate -format Logic -height 18 -label .stb_o -radix hexadecimal /tb_jop/jop/ext/wbtop/wb_out.stb_o
add wave -noupdate -divider {wb slave}
add wave -noupdate -divider {wishbone IO end}
add wave -noupdate -divider execute
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/din
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/b
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(0)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(1)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(2)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(3)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(4)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(5)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(128)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(129)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(130)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(131)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(132)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(133)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(134)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(135)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(136)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(137)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(138)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(139)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(140)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(141)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(142)
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/jop/core/stk/ram/ram(143)
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
