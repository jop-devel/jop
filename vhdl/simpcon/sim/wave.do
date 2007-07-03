onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic /tb_arbiter/s_clk
add wave -noupdate -format Logic /tb_arbiter/s_reset
add wave -noupdate -color Gold -format Logic -itemcolor Gold -radix hexadecimal /tb_arbiter/s_rd_m0
add wave -noupdate -color Gold -format Logic -itemcolor Gold -radix hexadecimal /tb_arbiter/s_wr_m0
add wave -noupdate -color Gold -format Literal -itemcolor Gold -radix hexadecimal /tb_arbiter/s_wr_data_m0
add wave -noupdate -color Gold -format Literal -itemcolor Gold -radix hexadecimal /tb_arbiter/s_address_m0
add wave -noupdate -color Gold -format Literal -itemcolor Gold -radix hexadecimal /tb_arbiter/r_rdy_cnt_m0
add wave -noupdate -color Gold -format Literal -itemcolor Gold -radix hexadecimal /tb_arbiter/r_rd_data_m0
add wave -noupdate -color {Cornflower Blue} -format Logic -itemcolor {Cornflower Blue} -radix hexadecimal /tb_arbiter/s_rd_m1
add wave -noupdate -color {Cornflower Blue} -format Logic -itemcolor {Cornflower Blue} -radix hexadecimal /tb_arbiter/s_wr_m1
add wave -noupdate -color {Cornflower Blue} -format Literal -itemcolor {Cornflower Blue} -radix hexadecimal /tb_arbiter/s_wr_data_m1
add wave -noupdate -color {Cornflower Blue} -format Literal -itemcolor {Cornflower Blue} -radix hexadecimal /tb_arbiter/s_address_m1
add wave -noupdate -color {Cornflower Blue} -format Literal -itemcolor {Cornflower Blue} -radix hexadecimal /tb_arbiter/r_rdy_cnt_m1
add wave -noupdate -color {Cornflower Blue} -format Literal -itemcolor {Cornflower Blue} -radix hexadecimal /tb_arbiter/r_rd_data_m1
add wave -noupdate -color {Orange Red} -format Logic -itemcolor {Orange Red} -radix hexadecimal /tb_arbiter/s_rd_m2
add wave -noupdate -color {Orange Red} -format Logic -itemcolor {Orange Red} -radix hexadecimal /tb_arbiter/s_wr_m2
add wave -noupdate -color {Orange Red} -format Literal -itemcolor {Orange Red} -radix hexadecimal /tb_arbiter/s_wr_data_m2
add wave -noupdate -color {Orange Red} -format Literal -itemcolor {Orange Red} -radix hexadecimal /tb_arbiter/s_address_m2
add wave -noupdate -color {Orange Red} -format Literal -itemcolor {Orange Red} -radix hexadecimal /tb_arbiter/r_rdy_cnt_m2
add wave -noupdate -color {Orange Red} -format Literal -itemcolor {Orange Red} -radix hexadecimal /tb_arbiter/r_rd_data_m2
add wave -noupdate -color Green -format Logic -itemcolor Green /tb_arbiter/r_rd_mem
add wave -noupdate -color Green -format Logic -itemcolor Green /tb_arbiter/r_wr_mem
add wave -noupdate -color Green -format Literal -itemcolor Green -radix hexadecimal /tb_arbiter/r_wr_data_mem
add wave -noupdate -color Green -format Literal -itemcolor Green -radix hexadecimal /tb_arbiter/r_address_mem
add wave -noupdate -color Green -format Literal -itemcolor Green -radix unsigned /tb_arbiter/s_rdy_cnt_mem
add wave -noupdate -color Green -format Literal -itemcolor Green -radix hexadecimal /tb_arbiter/s_rd_data_mem
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/this_state
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/next_state
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/follow_state
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/state
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/reg_in
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/set
add wave -noupdate -format Literal /tb_arbiter/arbiter1/waiting
add wave -noupdate -format Logic /tb_arbiter/arbiter1/masterwaiting
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {475000 ps} 0}
configure wave -namecolwidth 279
configure wave -valuecolwidth 142
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
WaveRestoreZoom {262572 ps} {705169 ps}
