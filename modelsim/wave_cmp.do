onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/int_res
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/wd
add wave -noupdate -divider core0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/stk/b
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/bcf/bc/val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/fch/ir
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/fch/bsy
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/fch/nxt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/stk/sp
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/gen_cpu__0/cpu/core/stk/sp_ov
add wave -noupdate -divider core1
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__1/cpu/core/bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__1/cpu/core/bcf/bc/val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__1/cpu/core/fch/pc
add wave -noupdate -divider core2
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__2/cpu/core/bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__2/cpu/core/bcf/bc/val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_cpu__2/cpu/core/fch/pc
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/sys/timer_int
add wave -noupdate -divider sc_sys0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/io/sys/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/io/sys/wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/sys/wr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/sys/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/io/sys/rdy_cnt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/io/sys/sync_out
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/io/sys/sync_in
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/sys/lock_reqest
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/sys/timer_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/sys/int_pend
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/sys/int_ena
add wave -noupdate -divider sc_sys1
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_io__1/io2/sync_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_io__1/io2/sync_in
add wave -noupdate -divider sc_sys2
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_io__2/io2/sync_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/gen_io__2/io2/sync_in
add wave -noupdate -divider Sync
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/sync/sync_in_array
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/sync/sync_out_array
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/sync/next_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/sync/next_locked_id
add wave -noupdate -divider Arbiter
add wave -noupdate -divider CCCP
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cccp/active
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/arb_back_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/arb_back_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/arb_front_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/arb_front_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/arb_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/arb_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/buf
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/config_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/config_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/dest
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/mem_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/next_buf
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/next_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/next_vpos
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/pos
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cccp/reset
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/src
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cccp/vpos
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {9447891 ns} 0}
configure wave -namecolwidth 240
configure wave -valuecolwidth 108
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
configure wave -timelineunits ns
update
WaveRestoreZoom {0 ns} {2678046 ns}
