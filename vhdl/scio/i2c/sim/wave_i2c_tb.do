onerror {resume}
quietly WaveActivateNextPane {} 0

add wave -noupdate -divider {i2c bus signals}
add wave -noupdate -format Logic /i2c_tb/sda_int
add wave -noupdate -format Logic /i2c_tb/scl_int

add wave -noupdate -divider {MASTER SIGNALS}
add wave -noupdate -format Logic /i2c_tb/clk_m
add wave -noupdate -format Logic /i2c_tb/reset_m

add wave -noupdate -divider {Configuration & control}
add wave -noupdate -format Logic /i2c_tb/masl_m
add wave -noupdate -format Logic /i2c_tb/strt_m
add wave -noupdate -format Logic /i2c_tb/txrx_m
add wave -noupdate -format Logic /i2c_tb/busy_m
add wave -noupdate -format Logic /i2c_tb/I2C_MA/tr_progress
add wave -noupdate -format Logic /i2c_tb/rep_start_m
add wave -noupdate -format Logic /i2c_tb/I2C_MA/reset_rep_start
add wave -noupdate -format Logic /i2c_tb/slave_addressed_m
add wave -noupdate -format Logic /i2c_tb/data_valid_m
add wave -noupdate -format Logic /i2c_tb/transaction_m
add wave -noupdate -format Logic /i2c_tb/err_m

add wave -noupdate -divider {SCL process}
add wave -noupdate -format Logic /i2c_tb/i2c_ma/scl_proc/scl_state
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_proc/i2c_state
add wave -noupdate -format Logic /i2c_tb/i2c_ma/scl_int
add wave -noupdate -format Logic /i2c_tb/i2c_ma/received_byte_count
add wave -noupdate -format Logic /i2c_tb/i2c_ma/message_size
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_proc/received_byte_count
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_proc/inc_received_bytes
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_proc/RX_ALL
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_rep_strt_int
add wave -noupdate -format Logic /i2c_tb/i2c_ma/scl_proc/gen_rep_start
add wave -noupdate -format Logic /i2c_tb/i2c_ma/scl_proc/gen_rep_start_reg
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_proc/load
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_proc/store
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_proc/i2c_stop
add wave -noupdate -format Logic /i2c_tb/i2c_ma/i2c_proc/inc_received_bytes

add wave -noupdate -divider {FIFO signals}
add wave -noupdate -format Logic tx_fifo_wr_ena_m
add wave -noupdate -format Logic tx_fifo_full_m
add wave -noupdate -format Logic data_in_m
add wave -noupdate -format Logic tx_fifo_occ_in_m
add wave -noupdate -format Logic tx_fifo_occ_out_m
add wave -noupdate -format Logic rx_fifo_rd_ena_m
add wave -noupdate -format Logic rx_fifo_empty_m
add wave -noupdate -format Logic data_out_m
add wave -noupdate -format Logic rx_fifo_occ_in_m
add wave -noupdate -format Logic rx_fifo_occ_out_m
add wave -noupdate -format Logic /i2c_tb/I2C_MA/RX_FIFO/int_wen

add wave -noupdate -divider {SLAVE SIGNALS}
add wave -noupdate -format Logic /i2c_tb/clk_s
add wave -noupdate -format Logic /i2c_tb/reset_s

add wave -noupdate -divider {Configuration & control}
add wave -noupdate -format Logic /i2c_tb/masl_s
add wave -noupdate -format Logic /i2c_tb/strt_s
add wave -noupdate -format Logic /i2c_tb/txrx_s
add wave -noupdate -format Logic /i2c_tb/ busy_s
add wave -noupdate -format Logic /i2c_tb/slave_addressed_s
add wave -noupdate -format Logic /i2c_tb/data_valid_s
add wave -noupdate -format Logic /i2c_tb/transaction_s
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/rst_valid
  
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/tx_data_reg
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/header_ok
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/rx_data_reg
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/shift_ena
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/i2c_state
add wave -noupdate -format Logic /i2c_tb/i2c_sl/scl_proc/scl_state
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/stop_scl
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/stop_scl_a
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/tx_fifo_empty
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/tx_fifo_empty_reg
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/load
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/tx_fifo_rd_ena
add wave -noupdate -format Logic /i2c_tb/i2c_sl/i2c_proc/data_in

add wave -noupdate -divider {FIFO signals}
add wave -noupdate -format Logic tx_fifo_wr_ena_s
add wave -noupdate -format Logic tx_fifo_full_s
add wave -noupdate -format Logic data_in_s
add wave -noupdate -format Logic tx_fifo_occ_in_s
add wave -noupdate -format Logic tx_fifo_occ_out_s
add wave -noupdate -format Logic rx_fifo_rd_ena_s
add wave -noupdate -format Logic rx_fifo_empty_s
add wave -noupdate -format Logic data_out_s
add wave -noupdate -format Logic rx_fifo_occ_in_s
add wave -noupdate -format Logic rx_fifo_occ_out_s
