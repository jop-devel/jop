onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/int_res
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/wd
add wave -noupdate -format Literal -radix ascii /tb_jop/cmp_jop/cmp_io/cmp_ua/char
add wave -noupdate -divider core0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_bcf/jpc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_bcf/jinstr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_fch/pc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_fch/ir
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_fch/bsy
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_fch/nxt
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/sp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/spp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/spm
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/sp_ov
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/vp3
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/vp2
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/vp1
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_core/cmp_stk/vp0
add wave -noupdate -divider interrupt
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/timer_equ
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/timer_int
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_io/cmp_sys/hwreq
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_io/cmp_sys/swreq
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/prioint
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/intnr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/ack
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_io/cmp_sys/pending
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/int_pend
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/int_ena
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/exc_pend
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/irq_gate
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/irq_dly
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/exc_dly
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_io/cmp_sys/irq_in
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_io/cmp_sys/irq_out
add wave -noupdate -divider {bcfetch interrupt}
add wave -noupdate -divider io
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/sel_reg
add wave -noupdate -divider {io cnt}
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -divider SimpCon
add wave -noupdate -divider sc_mem_if
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/sc_mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/sc_mem_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/ram_addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/ram_dout
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/ram_din
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/ram_dout_en
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/ram_ncs
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/ram_noe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/ram_nwe
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/fl_a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/fl_d
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/fl_ncs
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/fl_ncsb
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/fl_noe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/fl_nwe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/fl_rdy
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/trans_ram
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/trans_flash
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/ram_access
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/cmp_jop/cmp_scm/sel_flash
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
add wave -noupdate -divider sc_sys0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/cmp_io/cmp_sys/wr_data
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/wr
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/rd
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_io/cmp_sys/rdy_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_io/cmp_sys/sync_out
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_io/cmp_sys/sync_in
add wave -noupdate -format Logic /tb_jop/cmp_jop/cmp_io/cmp_sys/lock_reqest
add wave -noupdate -divider sc_sys1
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__1/cmp_io2/wr_data
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_io__1/cmp_io2/rd
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_io__1/cmp_io2/wr
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_io__1/cmp_io2/rdy_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_io__1/cmp_io2/sync_out
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_io__1/cmp_io2/sync_in
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_io__1/cmp_io2/lock_reqest
add wave -noupdate -divider sc_sys2
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__2/cmp_io2/address
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/cmp_jop/gen_io__2/cmp_io2/wr_data
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_io__2/cmp_io2/rd
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_io__2/cmp_io2/wr
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_io__2/cmp_io2/rdy_cnt
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_io__2/cmp_io2/sync_out
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_io__2/cmp_io2/sync_in
add wave -noupdate -format Logic /tb_jop/cmp_jop/gen_io__2/cmp_io2/lock_reqest
add wave -noupdate -divider Sync
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_sync/sync_in_array
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_sync/sync_out_array
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_sync/next_state
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_sync/next_locked_id
add wave -noupdate -divider {New Divider}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Literal /tb_jop/cmp_jop/cmp_io/cmp_ua/char
add wave -noupdate -format Literal /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/cmp_mem/state
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/sc_mem_out
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/gen_cpu__0/cmp_cpu/sc_mem_in
add wave -noupdate -divider Arbiter
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_arbiter/arb_out
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_arbiter/arb_in
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_arbiter/mem_out
add wave -noupdate -format Literal -expand /tb_jop/cmp_jop/cmp_arbiter/mem_in
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {16598139663 ps} 0}
configure wave -namecolwidth 460
configure wave -valuecolwidth 123
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
WaveRestoreZoom {16597788292 ps} {16598521152 ps}
