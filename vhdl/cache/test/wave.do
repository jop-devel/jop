onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/clk
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/reset
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_ocache/ocin
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_ocache/ocout
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/oc/chk_gf_dly
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/oc/clk
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/hit_line_reg
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/oc/hit_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/miss_line_reg
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_ocache/oc/oc_tag_in
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_ocache/oc/oc_tag_out
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/ocin
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/ocin_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/ocout
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_ocache/oc/ram
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/ram_din
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/ram_dout
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/ram_dout_store
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/ram_wraddr
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/oc/reset
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/oc/update_cache
add wave -noupdate -divider tag
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/oc/tag/clk
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/oc/tag/hit
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/tag/line
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/tag/nxt
add wave -noupdate -format Logic -radix hexadecimal /tb_ocache/oc/tag/reset
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_ocache/oc/tag/tag
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/tag/tag_in
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/tag/tag_out
add wave -noupdate -format Literal -radix hexadecimal /tb_ocache/oc/tag/valid
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
WaveRestoreZoom {0 ps} {208936 ps}
