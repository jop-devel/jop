onerror {resume}
add wave -noupdate /tb_overflow_rollback/* 
add wave -noupdate -divider tmif
add wave -noupdate /tb_overflow_rollback/dut/*
add wave -noupdate -divider tm
add wave -noupdate /tb_overflow_rollback/dut/cmp_tm/*
add wave -noupdate -divider tag
add wave -noupdate /tb_overflow_rollback/dut/cmp_tm/tag/*
