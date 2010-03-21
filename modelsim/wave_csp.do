onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/int_res
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/io/wd
add wave -noupdate -format Literal -radix ascii /tb_jop/joptop/io/ua/char
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
add wave -noupdate -divider noc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/io/sc_io_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/io/sc_io_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/io/sel
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/io/noc_in
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/noc/addr
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/noc/nreg
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/noc/rd
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/noc/rd_data
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/noc/rdy_cnt
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/rst
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/noc/wr
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/noc/wr_data
add wave -noupdate -divider node0
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/addr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/clk
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/deq
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/enq
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/iaddr
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/ird
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/iseod
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/isrcv
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/isrcvbufferempty
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/isrcvbufferfull
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/issnd
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/issndbufferempty
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/issndbufferfull
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/iwr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/iwr_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/myaddr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/nxt_rd_d
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/nxtrdy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/rcvfirst
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/rcvsrc
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/rd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/rd_d
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/rd_data
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/rdy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/rdy_cnt
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/resetrcv
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/rst
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/setsndcount
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/setsnddst
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/sndcount
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/snddata
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/snddst
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/wr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/noc/allnodes__0/node/busif/wr_data
add wave -noupdate -divider Sync
add wave -noupdate -divider Arbiter
add wave -noupdate -divider {external signals}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/main_mem/ncs
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/main_mem/noe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/main_mem/nwr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/fl_a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/fl_d
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/fl_ncs
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/fl_ncsb
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/fl_noe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/fl_nwe
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/fl_rdy
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {9447891000 ps} 0}
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
configure wave -timelineunits ps
update
WaveRestoreZoom {17972927 ps} {18044853 ps}
