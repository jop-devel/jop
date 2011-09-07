onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /tb_acache/clk
add wave -noupdate -radix hexadecimal /tb_acache/reset
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag_in
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag_out
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/line
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/idx
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/wridx
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/valid
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/nxt
add wave -noupdate -divider tag
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {0 ps} 0}
configure wave -namecolwidth 215
configure wave -valuecolwidth 40
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
WaveRestoreZoom {0 ps} {16400 ps}
