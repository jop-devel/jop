onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/int_res
add wave -noupdate -format Logic /tb_jop/joptop/io/wd
add wave -noupdate -format Literal -radix ascii /tb_jop/joptop/io/ua/char
add wave -noupdate -divider core
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/b
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/bcf/jinstr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/bcf/opd
add wave -noupdate -format Literal /tb_jop/joptop/cpu/core/bcf/bc/val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/fch/ir
add wave -noupdate -format Literal /tb_jop/joptop/cpu/core/fch/uc/val
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/core/fch/bsy
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/core/fch/nxt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/sp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/spp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/spm
add wave -noupdate -format Logic /tb_jop/joptop/cpu/core/stk/sp_ov
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/vp0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/stkram/ram
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/mem_in
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/next_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/addr_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/index
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/value
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/was_a_store
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/mem/null_pointer
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/mem/bounds_error
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/np_exc
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/ab_exc
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/sc_mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/sc_mem_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -divider ocache
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/oc/ocin
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/oc/wait4data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/ocout
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/oc/ocin_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/tag
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/index
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/valid
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/data
add wave -noupdate -divider SimpCon
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/sc_mem_out
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/sc_mem_in
add wave -noupdate -divider {external signals}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Logic /tb_jop/main_mem/ncs
add wave -noupdate -format Logic /tb_jop/main_mem/noe
add wave -noupdate -format Logic /tb_jop/main_mem/nwr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/fl_a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/fl_d
add wave -noupdate -format Logic /tb_jop/joptop/fl_ncs
add wave -noupdate -format Logic /tb_jop/joptop/fl_ncsb
add wave -noupdate -format Logic /tb_jop/joptop/fl_noe
add wave -noupdate -format Logic /tb_jop/joptop/fl_nwe
add wave -noupdate -format Logic /tb_jop/joptop/fl_rdy
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
WaveRestoreZoom {5546321 ns} {5546457 ns}
