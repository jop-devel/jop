onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/clk_int
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/int_res
add wave -noupdate -format Logic /tb_jop/joptop/io/wd

add wave -noupdate -divider {i2c signals}
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/control_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/status_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/dev_address_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/msg_size_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/tx_occu_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/rx_occu_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/th_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/tl_reg

#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/IIC/I2C_PROC/i2c_state

add wave -noupdate -format Logic /tb_jop/sda_int
add wave -noupdate -format Logic /tb_jop/scl_int


#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/iic/scl_proc/sda_out_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/iic/scl_proc/sda_out
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/iic/scl_proc/gen_stop
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/iic/scl_proc/gen_stop_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/busy_read
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/sc_rd_reg
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/sc_rd_reg_1


add wave -noupdate -divider {IIC device A}

add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/state
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/byte_count
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/msg_size

add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/t_hold_start_reg

add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/irq
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/control_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/status_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/dev_address_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/msg_size_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/tx_occu_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/rx_occu_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/th_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/tl_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/timing_t
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/tx_fifo_wren
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/tx_fifo_in_int.read_enable
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/aux_load
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/load
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/load_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/rx_fifo_rden
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/op_mode
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/op_mode_reg

add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/gen_rep_start
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/I2C/SDA_CTRL/aux_rep_start

add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/IIC/I2C_PROC/transaction

add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/IIC/transaction
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/tx_fifo_wren_int
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/rx_fifo_rden_int
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/IIC/rx_fifo_write
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/IIC/data_valid
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/IIC/I2C_PROC/data_valid_s


#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/scl_proc/scl_state
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/scl_proc/scl_next_state
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/scl_proc/scl_count
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/mas_proc/master_state
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/mas_proc/master_next_state
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/mas_proc/ma_scl.STOP
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/mas_proc/ma_scl.ZERO_BIT

add wave -noupdate -divider {IIC device B}

add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/SDA_CTRL/state
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/sda_out
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/SDA_CTRL/byte_count
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/SDA_CTRL/msg_size

add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/irq
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/int_state
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/control_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/status_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/dev_address_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/msg_size_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/tx_occu_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/rx_occu_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/th_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/tl_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/tx_fifo_wren
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/tx_fifo_in_int.read_enable
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/SDA_CTRL/aux_load
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/SDA_CTRL/load
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/SDA_CTRL/load_reg
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/rx_fifo_rden
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/rx_fifo_in_int.write_enable
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/I2C/SDA_CTRL/sda_data_in



#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/IIC/I2C_PROC/transaction
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_a/IIC/transaction
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/tx_fifo_wren_int
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/rx_fifo_rden_int
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/IIC/rx_fifo_write
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/IIC/data_valid
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/IIC/I2C_PROC/data_valid_s
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/IIC/I2C_PROC/header_ok

#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/i2c/ma_tx_fifo_read.rd_data
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/i2c/ma_tx_fifo_read.empty
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/i2c/ma_tx_fifo_read.rd_ena

add wave -noupdate -format Logic/tb_jop/joptop/io/iic_b/rx_fifo_rden_int
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/i2c/ma_rx_fifo_write.wr_data
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/i2c/ma_rx_fifo_write.full
add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/i2c/ma_rx_fifo_write.wr_ena
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/i2c/control_wr_ena
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic_b/i2c/control_rd_ena

add wave -noupdate -divider {Buffer memory signals}

add wave -noupdate -format Logic /tb_jop/joptop/io/iic/iic/rx_fifo/read_enable
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/iic/rx_fifo/int_ren
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/iic/rx_fifo/read_data_out
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/rx_fifo_rd_data_int
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/state

#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/tx_fifo/mem/rden
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/tx_fifo/mem/wren
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/rx_fifo/mem/rden
#add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/rx_fifo/mem/wren

add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/tx_fifo/int_wen
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/tx_fifo/write_data_in
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/tx_fifo/int_ren
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/tx_fifo/read_data_out
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/rx_fifo/int_wen
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/rx_fifo/write_data_in
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/rx_fifo/int_ren
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/i2c/rx_fifo/read_data_out


#/tb_jop/joptop/io/iic/rx_fifo_rd_data_int

add wave -noupdate -divider {SimpCon signals}
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/sc_wr
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/sc_wr_data
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/sc_rd
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/sc_rd_data
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/address
add wave -noupdate -format Logic /tb_jop/joptop/io/iic/sc_rdy_cnt

add wave -noupdate -format Literal -radix ascii /tb_jop/joptop/io/ua/char
add wave -noupdate -divider {java pc}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/bcf/jpc
add wave -noupdate -divider bcfetch
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/bcf/jinstr
add wave -noupdate -format Literal /tb_jop/joptop/cpu/core/bcf/bc/val
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/bcf/opd
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/fch/pc
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/core/fch/nxt
add wave -noupdate -divider fetch
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/fch/ir
add wave -noupdate -format Literal /tb_jop/joptop/cpu/core/fch/uc/val
add wave -noupdate -divider decode
add wave -noupdate -format Logic /tb_jop/joptop/cpu/core/dec/br
add wave -noupdate -format Logic /tb_jop/joptop/cpu/core/dec/jmp
add wave -noupdate -format Logic /tb_jop/joptop/cpu/core/dec/jbr
add wave -noupdate -format Logic /tb_jop/joptop/cpu/core/dec/ena_a
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/core/fch/bsy
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/sp
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/vp0
add wave -noupdate -divider execute
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/a
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/core/stk/b
add wave -noupdate -divider mem_sc
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/mem_in
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/state
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/addr_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/index
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/value
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/putref_reg
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/putref_next
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/dest_level_reg
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/was_a_store
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/was_a_stidx
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/was_a_hwo
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/read_ocache
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/mem/null_pointer
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/mem/bounds_error
add wave -noupdate -format Logic -radix hexadecimal /tb_jop/joptop/cpu/mem/illegal_assignment
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/np_exc
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/ab_exc
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/ia_exc
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/sc_mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/sc_mem_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -divider ocache
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/oc/ocin
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/oc/chk_gf_dly
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/oc/cacheable
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/oc/cacheable_reg
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/ram_dout_store
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/oc/ocout
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/oc/hit_reg
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/oc/ocin_reg
add wave -noupdate -format Logic /tb_jop/joptop/cpu/mem/oc/update_cache
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/ram_wraddr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/ram
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/ram_din
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/oc_tag_out.hit_line
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/ram_dout
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/tag/nxt
add wave -noupdate -format Literal -radix hexadecimal -expand /tb_jop/joptop/cpu/mem/oc/tag/tag_in
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/tag/tag
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/tag/valid
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/mem/oc/tag/tag_out
add wave -noupdate -divider SimpCon
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/sc_mem_out
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/joptop/cpu/sc_mem_in
add wave -noupdate -divider {external signals}
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/addr
add wave -noupdate -format Literal -radix hexadecimal /tb_jop/main_mem/data
add wave -noupdate -format Logic /tb_jop/main_mem/ncs
add wave -noupdate -format Logic /tb_jop/main_mem/noe
add wave -noupdate -format Logic /tb_jop/main_mem/nwr
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {4083368934 ps} 0}
configure wave -namecolwidth 233
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
WaveRestoreZoom {583125892 ps} {603224954 ps}
