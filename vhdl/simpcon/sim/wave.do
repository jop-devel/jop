onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic /tb_arbiter/s_clk
add wave -noupdate -format Logic /tb_arbiter/s_reset
add wave -noupdate -color Gold -format Logic -itemcolor Gold /tb_arbiter/s_rd_vga
add wave -noupdate -color Gold -format Logic -itemcolor Gold /tb_arbiter/s_wr_vga
add wave -noupdate -color Gold -format Literal -itemcolor Gold -radix hexadecimal /tb_arbiter/s_address_vga
add wave -noupdate -color Gold -format Literal -itemcolor Gold /tb_arbiter/r_rdy_cnt_vga
add wave -noupdate -color Gold -format Literal -itemcolor Gold -radix hexadecimal /tb_arbiter/r_rd_data_vga
add wave -noupdate -color {Cornflower Blue} -format Logic -itemcolor {Cornflower Blue} /tb_arbiter/s_rd_jop
add wave -noupdate -color {Cornflower Blue} -format Logic -itemcolor {Cornflower Blue} /tb_arbiter/s_wr_jop
add wave -noupdate -color {Cornflower Blue} -format Literal -itemcolor {Cornflower Blue} -radix hexadecimal /tb_arbiter/s_address_jop
add wave -noupdate -color {Cornflower Blue} -format Literal -itemcolor {Cornflower Blue} /tb_arbiter/r_rdy_cnt_jop
add wave -noupdate -color {Cornflower Blue} -format Literal -itemcolor {Cornflower Blue} -radix hexadecimal /tb_arbiter/r_rd_data_jop
add wave -noupdate -format Logic /tb_arbiter/s_rd_m3
add wave -noupdate -format Literal -radix hexadecimal /tb_arbiter/s_address_m3
add wave -noupdate -format Literal /tb_arbiter/r_rdy_cnt_m3
add wave -noupdate -color Green -format Logic -itemcolor Green /tb_arbiter/r_rd_mem
add wave -noupdate -color Green -format Logic -itemcolor Green /tb_arbiter/r_wr_mem
add wave -noupdate -color Green -format Literal -itemcolor Green -radix hexadecimal /tb_arbiter/r_wr_data_mem
add wave -noupdate -color Green -format Literal -itemcolor Green -radix hexadecimal /tb_arbiter/r_address_mem
add wave -noupdate -color Green -format Literal -itemcolor Green /tb_arbiter/s_rdy_cnt_mem
add wave -noupdate -color Green -format Literal -itemcolor Green -radix hexadecimal /tb_arbiter/s_rd_data_mem
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/next_state
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/follow_state
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/state
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/this_state
add wave -noupdate -format Literal -expand /tb_arbiter/arbiter1/reg_in
add wave -noupdate -format Literal /tb_arbiter/arbiter1/set
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {6997 ps} 0}
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
WaveRestoreZoom {0 ps} {123765 ps}
