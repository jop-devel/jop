onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/clk
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/reset
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_rttm/from_cpu
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_rttm/to_cpu
add wave -noupdate -divider tm
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/cmp_tm/line_addr
add wave -noupdate -format Logic -radix hexadecimal /tb_rttm/cmp_tm/hit
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_rttm/cmp_tm/tag
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_rttm/cmp_tm/data
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/cmp_tm/valid
add wave -noupdate -format Literal -radix hexadecimal /tb_rttm/cmp_tm/nxt
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
WaveRestoreZoom {0 ns} {214 ns}
