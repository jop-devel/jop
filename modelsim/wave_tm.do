onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/int_res
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/wd
add wave -noupdate -format Literal -radix ascii /tb_jop/cmp_jop/cmp_io/cmp_ua/char
add wave -noupdate -divider core0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/b
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_bcf/bc/val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_fch/ir
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_fch/bsy
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_fch/nxt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/sp
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/sp_ov
add wave -noupdate -divider tm0
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/clk
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/reset
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/commit_out_try
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/commit_in_allow
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/broadcast
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/gen_tm__0/cmp_tm/sc_out_cpu
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/sc_in_cpu
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/sc_out_arb
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/sc_in_arb
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/exc_tm_rollback
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/state
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/next_state
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/nesting_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/next_nesting_cnt
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/conflict
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/tm_cmd
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/start_commit
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/committing
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/read_tag_of
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/write_buffer_of
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/reset_on_transaction_start
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/reset_tm
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/sc_out_cpu_filtered
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/sc_in_cpu_filtered
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/sc_out_arb_filtered
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/processing_tm_cmd
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__0/cmp_tm/next_processing_tm_cmd
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/next_tm_cmd_rdy_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/tm_cmd_rdy_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/memory_access_mode
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__0/cmp_tm/next_memory_access_mode
add wave -noupdate -divider core1
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__1/cmp_cpu/cmp_core/cmp_bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__1/cmp_cpu/cmp_core/cmp_bcf/bc/val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__1/cmp_cpu/cmp_core/cmp_fch/pc
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/timer_int
add wave -noupdate -divider tm1
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/clk
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/reset
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/commit_out_try
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/commit_in_allow
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/broadcast
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/gen_tm__1/cmp_tm/sc_out_cpu
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/sc_in_cpu
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/sc_out_arb
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/sc_in_arb
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/exc_tm_rollback
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/state
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/next_state
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/nesting_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/next_nesting_cnt
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/conflict
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/tm_cmd
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/start_commit
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/committing
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/read_tag_of
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/write_buffer_of
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/reset_on_transaction_start
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/reset_tm
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/sc_out_cpu_filtered
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/sc_in_cpu_filtered
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/sc_out_arb_filtered
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/processing_tm_cmd
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_tm__1/cmp_tm/next_processing_tm_cmd
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/next_tm_cmd_rdy_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/tm_cmd_rdy_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/memory_access_mode
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_tm__1/cmp_tm/next_memory_access_mode
add wave -noupdate -divider sc_sys0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/wr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/rdy_cnt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/sync_out
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_io/cmp_sys/sync_in
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/lock_reqest
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/timer_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/int_pend
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/int_ena
add wave -noupdate -divider sc_sys1
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/wr_data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/rd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/rdy_cnt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/sync_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/sync_in
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/lock_reqest
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/timer_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/int_pend
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/int_ena
add wave -noupdate -divider Sync
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_sync/sync_in_array
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_sync/sync_out_array
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_sync/next_state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_sync/next_locked_id
add wave -noupdate -divider Arbiter
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_arbiter/arb_out
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_arbiter/arb_in
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_arbiter/mem_out
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/cmp_jop/cmp_arbiter/mem_in
add wave -noupdate -divider {external signals}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/main_mem/ncs
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/main_mem/noe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/main_mem/nwr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/fl_a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/fl_d
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/fl_ncs
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/fl_ncsb
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/fl_noe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/fl_nwe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/fl_rdy
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {5884462 ns} 0}
configure wave -namecolwidth 438
configure wave -valuecolwidth 146
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
WaveRestoreZoom {5884426 ns} {5884468 ns}
