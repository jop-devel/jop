onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/int_res
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/wd
add wave -noupdate -format Literal -radix ascii /tb_jop/cmp_jop/cmp_io/cmp_ua/char
add wave -noupdate -divider core
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/b
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/jinstr
add wave -noupdate -format Literal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/bc/val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/ir
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/bsy
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_fch/nxt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/sp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/spp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/spm
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/sp_ov
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/vp0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_stk/cmp_ram/ram
add wave -noupdate -divider interrupt
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/timer_equ
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/timer_int
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_io/cmp_sys/hwreq
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_io/cmp_sys/swreq
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/prioint
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/intnr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/ack
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_io/cmp_sys/pending
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/int_pend
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/int_ena
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/exc_pend
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/irq_gate
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/irq_dly
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/exc_dly
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_io/cmp_sys/irq_in
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_io/cmp_sys/irq_out
add wave -noupdate -divider {bcfetch interrupt}
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/int_pend
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/int_taken
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/exc_pend
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/exc_taken
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_core/cmp_bcf/jfetch
add wave -noupdate -divider io
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/sel_reg
add wave -noupdate -divider {io cnt}
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/mem_in
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cpm_cpu/cmp_mem/mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/next_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/addr_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/index
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/value
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_mem/was_a_store
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/null_pointer
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/bounds_error
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_mem/np_exc
add wave -noupdate -format Logic /tb_jop/cmp_jop/cpm_cpu/cmp_mem/ab_exc
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cpm_cpu/cmp_mem/sc_mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/cmp_mem/sc_mem_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -divider spm
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cpm_cpu/sc_scratch_out
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cpm_cpu/sc_scratch_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cpm_cpu/mux_mem
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
WaveRestoreCursors {{Cursor 1} {35999 ns} 0}
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
WaveRestoreZoom {7021558 ns} {7089094 ns}
