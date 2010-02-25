onerror {resume}
add wave -noupdate /tb_tm/* 
add wave -noupdate -divider tmif
add wave -noupdate /tb_tm/dut/*
add wave -noupdate -divider tm
add wave -noupdate /tb_tm/dut/tm/*
add wave -noupdate -divider tag
add wave -noupdate /tb_tm/dut/tm/tag/*
add wave -noupdate -divider write_fifo_buffer
add wave -noupdate /tb_tm/dut/tm/write_fifo_buffer_inst/*
