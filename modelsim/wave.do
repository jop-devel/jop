onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/int_res
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/wd
add wave -noupdate -format Literal -radix ascii /tb_jop/cmp_jop/cmp_io/cmp_ua/char
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_bcf/jpc
add wave -noupdate -format Literal -radix unsigned /tb_jop/cmp_jop/cmp_core/cmp_bcf/jinstr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_fch/pc_inc
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_fch/pcwait
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_core/cmp_fch/bsy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_dec/ir
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_core/cmp_dec/sel_lmux
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_dec/wr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_dec/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/a
add wave -noupdate -divider extension
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/ext_addr
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_ext/was_a_mem_rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/exr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/scio_address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/scio_wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/scio_rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/scio_wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/scio_rd_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_ext/scio_rdy_cnt
add wave -noupdate -divider io
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/wr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/sel_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/rd_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/rdy_cnt
add wave -noupdate -divider {io cnt}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/wr_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/rd_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/rdy_cnt
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/clock_cnt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/irq_cnt
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/din
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_addr_wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_wr_addr
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_mem/mem_bc_rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/mem_wr
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_mem/bsy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/dout
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_mem/state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/a
add wave -noupdate -divider sc_mem_if
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/rd_data
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_scm/ram_data_ena
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/rdy_cnt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/wait_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/state
add wave -noupdate -divider {bc load}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_len
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_mem_start
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_wr_addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/bc_wr_ena
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cache_bcstart
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_mem/cache_in_cache
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_mem/cache_rdy
add wave -noupdate -divider cache
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/clr_val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/nr_of_blks
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/nxt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/tag
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/use_addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/block_addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/state
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/in_cache
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/rdy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/bcstart
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/find
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/bc_addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_mem/cmp_cache/bc_len
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
add wave -noupdate -divider {wb slave}
add wave -noupdate -divider exception
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_core/sp_ov
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_core/cmp_bcf/exc_int
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_core/cmp_bcf/exc_pend
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_core/cmp_bcf/sys_exc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_bcf/jinstr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_bcf/jpaddr
add wave -noupdate -divider execute
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/din
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/b
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_core/cmp_stk/sp
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
WaveRestoreCursors {{Cursor 1} {3267139 ps} 0} {{Cursor 2} {693090000 ps} 0} {{Cursor 3} {3450000 ps} 0} {{Cursor 4} {3339704 ps} 0}
configure wave -namecolwidth 259
configure wave -valuecolwidth 47
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
WaveRestoreZoom {3247805 ps} {3391177 ps}
