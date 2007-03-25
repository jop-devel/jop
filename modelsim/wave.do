onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/int_res
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/wd
add wave -noupdate -format Literal -radix ascii /tb_jop/cmp_jop/cmp_io/cmp_ua/char
add wave -noupdate -divider core
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/jinstr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/ir
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/bsy
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/nxt
add wave -noupdate -divider io
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/sel_reg
add wave -noupdate -divider {io cnt}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/wr_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/rd_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/rdy_cnt
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/clock_cnt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_cnt/irq_cnt
add wave -noupdate -divider SimpCon
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cpm_cpu/sc_mem_out
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cpm_cpu/sc_mem_in
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -divider sc_mem_if
add wave -noupdate -divider {bc load}
add wave -noupdate -divider cache
add wave -noupdate -divider amba
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_s2a/state
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_s2a/next_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_s2a/reg_wr_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_s2a/reg_rd_data
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_s2a/ahbsi
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_s2a/ahbso
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_s2a/scmi
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_s2a/scmo
add wave -noupdate -divider ahb-memctrl
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/srctrl0/r
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
add wave -noupdate -divider execute
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {226600 ps} 0} {{Cursor 2} {693090000 ps} 0} {{Cursor 3} {3450000 ps} 0} {{Cursor 4} {3339704 ps} 0}
configure wave -namecolwidth 228
configure wave -valuecolwidth 144
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
WaveRestoreZoom {148539 ps} {287265 ps}
