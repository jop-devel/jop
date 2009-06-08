onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/clk
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/reset
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_rttm/from_cpu
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_rttm/to_cpu
add wave -noupdate -divider tag
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/tm/tag/addr
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/tm/tag/wr
add wave -noupdate -format Literal /tb_rttm/tm/tag/h
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/tm/tag/hit
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/tm/tag/line
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/tm/tag/nxt
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/tm/tag/wr_dly
add wave -noupdate -format Literal /tb_rttm/tm/tag/v
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_rttm/tm/tag/tag
add wave -noupdate -divider tm
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/tm/from_cpu_dly
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/tm/rd_hit
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/tm/reg_data
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/tm/save_data
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_rttm/tm/data
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {0 ns} 0}
configure wave -namecolwidth 150
configure wave -valuecolwidth 100
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
WaveRestoreZoom {0 ns} {80 ns}
