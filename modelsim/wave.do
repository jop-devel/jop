onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/int_res
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/wd
add wave -noupdate -format Literal -radix ascii /tb_jop/cmp_jop/cmp_io/cmp_ua/char
add wave -noupdate -divider core
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/b
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/sum
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/lt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/jinstr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/ir
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/bsy
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/nxt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/sp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/spp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/spm
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/sp_ov
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/vp3
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/vp2
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/vp1
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/vp0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/cmp_ram/ram
add wave -noupdate -divider io
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/sel_reg
add wave -noupdate -divider {io cnt}
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/mem_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/next_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/state
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/bcl_arr_bsy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/addr_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/index
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/addr_calc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/value
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_mem/was_a_store
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/iastore_nxt
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/null_pointer
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/bounds_error
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -divider SimpCon
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cpm_cpu/sc_mem_out
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cpm_cpu/sc_mem_in
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
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {226600 ps} 0} {{Cursor 2} {693090000 ps} 0} {{Cursor 3} {241391064 ps} 0} {{Cursor 4} {4032743 ps} 0}
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
WaveRestoreZoom {30 ns} {12630 ns}
