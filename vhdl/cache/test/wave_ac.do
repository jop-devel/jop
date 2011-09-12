onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /tb_acache/clk
add wave -noupdate -radix hexadecimal /tb_acache/reset
add wave -noupdate -radix hexadecimal -expand -subitemconfig {/tb_acache/ac/acin.handle {-radix hexadecimal} /tb_acache/ac/acin.index {-radix hexadecimal} /tb_acache/ac/acin.gf_val {-radix hexadecimal} /tb_acache/ac/acin.pf_val {-radix hexadecimal} /tb_acache/ac/acin.chk_gf {-radix hexadecimal} /tb_acache/ac/acin.chk_pf {-radix hexadecimal} /tb_acache/ac/acin.wr_gf {-radix hexadecimal} /tb_acache/ac/acin.wr_gf_idx {-radix hexadecimal} /tb_acache/ac/acin.wr_pf {-radix hexadecimal} /tb_acache/ac/acin.inval {-radix hexadecimal}} /tb_acache/ac/acin
add wave -noupdate -radix hexadecimal -expand -subitemconfig {/tb_acache/ac/acout.hit {-radix hexadecimal} /tb_acache/ac/acout.dout {-radix hexadecimal}} /tb_acache/ac/acout
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
add wave -noupdate -radix hexadecimal -subitemconfig {/tb_acache/ac/ram(0) {-radix hexadecimal} /tb_acache/ac/ram(1) {-radix hexadecimal} /tb_acache/ac/ram(2) {-radix hexadecimal} /tb_acache/ac/ram(3) {-radix hexadecimal} /tb_acache/ac/ram(4) {-radix hexadecimal} /tb_acache/ac/ram(5) {-radix hexadecimal} /tb_acache/ac/ram(6) {-radix hexadecimal} /tb_acache/ac/ram(7) {-radix hexadecimal} /tb_acache/ac/ram(8) {-radix hexadecimal} /tb_acache/ac/ram(9) {-radix hexadecimal} /tb_acache/ac/ram(10) {-radix hexadecimal} /tb_acache/ac/ram(11) {-radix hexadecimal} /tb_acache/ac/ram(12) {-radix hexadecimal} /tb_acache/ac/ram(13) {-radix hexadecimal} /tb_acache/ac/ram(14) {-radix hexadecimal} /tb_acache/ac/ram(15) {-radix hexadecimal} /tb_acache/ac/ram(16) {-radix hexadecimal} /tb_acache/ac/ram(17) {-radix hexadecimal} /tb_acache/ac/ram(18) {-radix hexadecimal} /tb_acache/ac/ram(19) {-radix hexadecimal} /tb_acache/ac/ram(20) {-radix hexadecimal} /tb_acache/ac/ram(21) {-radix hexadecimal} /tb_acache/ac/ram(22) {-radix hexadecimal} /tb_acache/ac/ram(23) {-radix hexadecimal} /tb_acache/ac/ram(24) {-radix hexadecimal} /tb_acache/ac/ram(25) {-radix hexadecimal} /tb_acache/ac/ram(26) {-radix hexadecimal} /tb_acache/ac/ram(27) {-radix hexadecimal} /tb_acache/ac/ram(28) {-radix hexadecimal} /tb_acache/ac/ram(29) {-radix hexadecimal} /tb_acache/ac/ram(30) {-radix hexadecimal} /tb_acache/ac/ram(31) {-radix hexadecimal} /tb_acache/ac/ram(32) {-radix hexadecimal} /tb_acache/ac/ram(33) {-radix hexadecimal} /tb_acache/ac/ram(34) {-radix hexadecimal} /tb_acache/ac/ram(35) {-radix hexadecimal} /tb_acache/ac/ram(36) {-radix hexadecimal} /tb_acache/ac/ram(37) {-radix hexadecimal} /tb_acache/ac/ram(38) {-radix hexadecimal} /tb_acache/ac/ram(39) {-radix hexadecimal} /tb_acache/ac/ram(40) {-radix hexadecimal} /tb_acache/ac/ram(41) {-radix hexadecimal} /tb_acache/ac/ram(42) {-radix hexadecimal} /tb_acache/ac/ram(43) {-radix hexadecimal} /tb_acache/ac/ram(44) {-radix hexadecimal} /tb_acache/ac/ram(45) {-radix hexadecimal} /tb_acache/ac/ram(46) {-radix hexadecimal} /tb_acache/ac/ram(47) {-radix hexadecimal} /tb_acache/ac/ram(48) {-radix hexadecimal} /tb_acache/ac/ram(49) {-radix hexadecimal} /tb_acache/ac/ram(50) {-radix hexadecimal} /tb_acache/ac/ram(51) {-radix hexadecimal} /tb_acache/ac/ram(52) {-radix hexadecimal} /tb_acache/ac/ram(53) {-radix hexadecimal} /tb_acache/ac/ram(54) {-radix hexadecimal} /tb_acache/ac/ram(55) {-radix hexadecimal} /tb_acache/ac/ram(56) {-radix hexadecimal} /tb_acache/ac/ram(57) {-radix hexadecimal} /tb_acache/ac/ram(58) {-radix hexadecimal} /tb_acache/ac/ram(59) {-radix hexadecimal} /tb_acache/ac/ram(60) {-radix hexadecimal} /tb_acache/ac/ram(61) {-radix hexadecimal} /tb_acache/ac/ram(62) {-radix hexadecimal} /tb_acache/ac/ram(63) {-radix hexadecimal}} /tb_acache/ac/ram
add wave -noupdate -radix hexadecimal /tb_acache/ac/ram_din
add wave -noupdate -radix hexadecimal /tb_acache/ac/ram_dout
add wave -noupdate -radix hexadecimal /tb_acache/ac/ram_wraddr
add wave -noupdate -divider tag
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag_in
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag_out
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/line
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/idx_upper
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/wr_idx_upper
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag
add wave -noupdate -radix hexadecimal /tb_acache/ac/tag/tag_idx
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
WaveRestoreZoom {0 ps} {24618 ps}
