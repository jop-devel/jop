onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /tb_acache/clk
add wave -noupdate -radix hexadecimal /tb_acache/reset
add wave -noupdate -radix hexadecimal /tb_acache/ac/acin
add wave -noupdate -radix hexadecimal /tb_acache/ac/acout
add wave -noupdate -radix hexadecimal /tb_acache/ac/acin_reg
add wave -noupdate -radix hexadecimal /tb_acache/ac/line_reg
add wave -noupdate -radix hexadecimal /tb_acache/ac/hit_reg
add wave -noupdate -radix hexadecimal /tb_acache/ac/hit_tag_reg
add wave -noupdate -radix hexadecimal /tb_acache/ac/inc_nxt_reg
add wave -noupdate -radix hexadecimal /tb_acache/ac/cacheable
add wave -noupdate -radix hexadecimal /tb_acache/ac/cacheable_reg
add wave -noupdate -radix hexadecimal /tb_acache/ac/update_cache
add wave -noupdate -radix hexadecimal /tb_acache/ac/chk_gf_dly
add wave -noupdate -radix hexadecimal /tb_acache/ac/ram_dout_store
add wave -noupdate -radix hexadecimal /tb_acache/ac/ram
add wave -noupdate -radix hexadecimal /tb_acache/ac/ram_din
add wave -noupdate -radix hexadecimal /tb_acache/ac/ram_dout
add wave -noupdate -radix hexadecimal /tb_acache/ac/ram_wraddr
add wave -noupdate -divider tag
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag_in
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag_out
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/line
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/valid
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/nxt
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
WaveRestoreZoom {0 ps} {10703 ps}
