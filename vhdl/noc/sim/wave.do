onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Literal -radix hexadecimal /testnoc/addr
add wave -noupdate -format Logic -radix hexadecimal /testnoc/clk
add wave -noupdate -format Literal -radix hexadecimal /testnoc/rd
add wave -noupdate -format Literal -radix hexadecimal /testnoc/rd_data
add wave -noupdate -format Literal -radix hexadecimal /testnoc/rdy_cnt
add wave -noupdate -format Logic -radix hexadecimal /testnoc/rst
add wave -noupdate -format Literal -radix hexadecimal /testnoc/wr
add wave -noupdate -format Literal -radix hexadecimal /testnoc/wr_data
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {0 ps} 0}
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
configure wave -timelineunits ps
update
WaveRestoreZoom {0 ps} {6338 ps}
