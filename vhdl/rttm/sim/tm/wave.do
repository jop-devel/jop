onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic /tb_tm/finished
add wave -noupdate -format Logic /tb_tm/clk
add wave -noupdate -format Logic /tb_tm/reset
add wave -noupdate -format Logic /tb_tm/commit_out_try
add wave -noupdate -format Logic /tb_tm/commit_in_allow
add wave -noupdate -format Literal -expand /tb_tm/sc_out_cpu
add wave -noupdate -format Literal -expand /tb_tm/sc_in_cpu
add wave -noupdate -format Literal /tb_tm/sc_out_arb
add wave -noupdate -format Literal -expand /tb_tm/sc_in_arb
add wave -noupdate -format Logic /tb_tm/exc_tm_rollback
add wave -noupdate -format Literal /tb_tm/broadcast
add wave -noupdate -format Logic /tb_tm/testing_commit
add wave -noupdate -format Logic /tb_tm/testing_conflict
add wave -noupdate -format Literal /tb_tm/write_buffer
add wave -noupdate -divider tmif
add wave -noupdate -format Logic /tb_tm/dut/clk
add wave -noupdate -format Logic /tb_tm/dut/reset
add wave -noupdate -format Logic /tb_tm/dut/commit_out_try
add wave -noupdate -format Logic /tb_tm/dut/commit_in_allow
add wave -noupdate -format Literal /tb_tm/dut/broadcast
add wave -noupdate -format Literal /tb_tm/dut/sc_out_cpu
add wave -noupdate -format Literal /tb_tm/dut/sc_in_cpu
add wave -noupdate -format Literal /tb_tm/dut/sc_out_arb
add wave -noupdate -format Literal /tb_tm/dut/sc_in_arb
add wave -noupdate -format Logic /tb_tm/dut/exc_tm_rollback
add wave -noupdate -format Literal /tb_tm/dut/state
add wave -noupdate -format Literal /tb_tm/dut/next_state
add wave -noupdate -format Literal /tb_tm/dut/nesting_cnt
add wave -noupdate -format Literal /tb_tm/dut/next_nesting_cnt
add wave -noupdate -format Logic /tb_tm/dut/conflict
add wave -noupdate -format Literal /tb_tm/dut/tm_cmd
add wave -noupdate -format Logic /tb_tm/dut/start_commit
add wave -noupdate -format Logic /tb_tm/dut/committing
add wave -noupdate -format Logic /tb_tm/dut/read_tag_of
add wave -noupdate -format Logic /tb_tm/dut/write_buffer_of
add wave -noupdate -format Logic /tb_tm/dut/reset_on_transaction_start
add wave -noupdate -format Logic /tb_tm/dut/reset_tm
add wave -noupdate -format Literal /tb_tm/dut/sc_out_cpu_filtered
add wave -noupdate -format Literal -expand /tb_tm/dut/sc_in_cpu_filtered
add wave -noupdate -format Literal /tb_tm/dut/sc_out_arb_filtered
add wave -noupdate -format Logic /tb_tm/dut/processing_tm_cmd
add wave -noupdate -format Logic /tb_tm/dut/next_processing_tm_cmd
add wave -noupdate -format Literal /tb_tm/dut/next_tm_cmd_rdy_cnt
add wave -noupdate -format Literal /tb_tm/dut/tm_cmd_rdy_cnt
add wave -noupdate -format Literal /tb_tm/dut/memory_access_mode
add wave -noupdate -format Literal /tb_tm/dut/next_memory_access_mode
add wave -noupdate -divider tm
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/clk
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/reset
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/from_cpu
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/to_cpu
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/to_mem
add wave -noupdate -format Literal -expand /tb_tm/dut/cmp_tm/from_mem
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/broadcast
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/conflict
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/start_commit
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/committing
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tag_of
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_buffer_of
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/state
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/tm_rdy_cnt
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/line_addr
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/newline
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/data
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/hit
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/from_cpu_dly
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/rd_hit
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/rd_miss
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/reg_data
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/save_data
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags_full
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/commit_line
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/shift
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/commit_addr
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags_wr
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags_addr
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags_hit
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags_full
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/is_conflict_check
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/broadcast_addr_del
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/broadcast_check_del
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/next_broadcast_check_del
add wave -noupdate -divider {write tags}
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/clk
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/reset
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/addr
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/wr
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/hit
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/line
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/newline
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/full
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/shift
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/lowest_addr
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/l
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/line_addr
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/tag
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/h
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/v
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/nxt
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/h_res
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/hit_reg
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/write_tags/wr_dly
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/write_tags/addr_dly
add wave -noupdate -divider {read tags}
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/clk
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/reset
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/addr
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/wr
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/hit
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/line
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/newline
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/full
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/shift
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/lowest_addr
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/l
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/line_addr
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/tag
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/h
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/v
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/nxt
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/h_res
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/hit_reg
add wave -noupdate -format Logic /tb_tm/dut/cmp_tm/read_tags/wr_dly
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/read_tags/addr_dly
add wave -noupdate -format Logic /tb_tm/memory/clk
add wave -noupdate -format Logic /tb_tm/memory/reset
add wave -noupdate -format Literal /tb_tm/memory/sc_mem_out
add wave -noupdate -format Literal /tb_tm/memory/sc_mem_in
add wave -noupdate -format Literal /tb_tm/memory/fl_d
add wave -noupdate -format Literal /tb_tm/memory/ram_addr
add wave -noupdate -format Logic /tb_tm/memory/ram_dout_en
add wave -noupdate -format Logic /tb_tm/memory/ram_ncs
add wave -noupdate -format Logic /tb_tm/memory/ram_noe
add wave -noupdate -format Logic /tb_tm/memory/ram_nwe
add wave -noupdate -format Literal /tb_tm/memory/ram_data
add wave -noupdate -divider Memory
add wave -noupdate -format Literal -expand /tb_tm/sc_out_arb
add wave -noupdate -format Literal /tb_tm/dut/cmp_tm/from_mem
add wave -noupdate -format Literal -radix hexadecimal /tb_tm/memory/main_mem/ram(1)
add wave -noupdate -format Literal -radix hexadecimal /tb_tm/memory/main_mem/data
add wave -noupdate -format Literal -radix hexadecimal /tb_tm/memory/cmp_mem_if/sc_mem_in
add wave -noupdate -divider {mem if}
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/clk
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/reset
add wave -noupdate -format Literal -expand /tb_tm/memory/cmp_mem_if/sc_mem_out
add wave -noupdate -format Literal -expand /tb_tm/memory/cmp_mem_if/sc_mem_in
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/ram_addr
add wave -noupdate -format Literal -radix hexadecimal /tb_tm/memory/cmp_mem_if/ram_dout
add wave -noupdate -format Literal -radix hexadecimal /tb_tm/memory/cmp_mem_if/ram_din
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/ram_dout_en
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/ram_ncs
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/ram_noe
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/ram_nwe
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/fl_a
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/fl_d
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/fl_ncs
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/fl_ncsb
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/fl_noe
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/fl_nwe
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/fl_rdy
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/state
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/next_state
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/wait_state
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/cnt
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/dout_ena
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/ram_data
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/ram_data_ena
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/flash_dout
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/fl_dout_ena
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/flash_data
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/flash_data_ena
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/trans_ram
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/trans_flash
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/trans_rdy
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/ram_access
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/sel_flash
add wave -noupdate -format Logic /tb_tm/memory/cmp_mem_if/sel_rdy
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/nand_rdy
add wave -noupdate -divider memory
add wave -noupdate -format Literal /tb_tm/memory/cmp_mem_if/ram_ws_wr
add wave -noupdate -format Literal /tb_tm/memory/main_mem/addr
add wave -noupdate -format Literal /tb_tm/memory/main_mem/data
add wave -noupdate -format Logic /tb_tm/memory/main_mem/ncs
add wave -noupdate -format Logic /tb_tm/memory/main_mem/noe
add wave -noupdate -format Logic /tb_tm/memory/main_mem/nwr
add wave -noupdate -format Literal -radix hexadecimal /tb_tm/memory/main_mem/ram(1)
add wave -noupdate -format Logic /tb_tm/memory/main_mem/cs_ok
add wave -noupdate -format Logic /tb_tm/memory/main_mem/oe_ok
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {297 ns} 0}
configure wave -namecolwidth 258
configure wave -valuecolwidth 255
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
WaveRestoreZoom {118 ns} {362 ns}
