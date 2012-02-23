library IEEE;
use IEEE.STD_LOGIC_1164.all;
use ieee.std_logic_unsigned.all;

package i2c_pkg is

  constant RESET_LEVEL : std_logic := '1'; 
  constant ADDRES_SIZE : integer   := 7;

--   constant COUNT_BASE : integer := 20;
--   constant HOLD_START : integer range 0 to (5*COUNT_BASE-1) := (5*COUNT_BASE-1);
--   constant T_LOW      : integer range 0 to (5*COUNT_BASE-1) := (5*COUNT_BASE-1);
--   constant T_HIGH     : integer range 0 to (5*COUNT_BASE-1) := (5*COUNT_BASE-1); 
--   constant T_SUSTO    : integer range 0 to 4*COUNT_BASE := 4*COUNT_BASE;
--   constant TBUF       : integer range 0 to 5*COUNT_BASE := 5*COUNT_BASE;
--   constant T_WAIT     : integer range 0 to 9*COUNT_BASE := 9*COUNT_BASE;

  -- constant HI_TIME     : integer range 0 to 10 := 5;
  -- constant DATA_HOLD   : integer range 0 to 5  := 2;
--   constant HDR_LENGTH  : integer range 0 to 8  := 7;
--   constant DATA_LENGTH : integer range 0 to 8  := 7;
  
--   constant RD : std_logic := '1';
--   constant WR : std_logic := '0';

--   type scl_state_type is (idle, start, scl_falling, scl_rising, scl_low, scl_high, scl_stop);
--   type master_state_type is (idle, tx_header, tx_data, rx_data, wait_ack, send_ack);
--   type slave_state_type is (wait_start, tx_data, rx_data, rx_header, send_ack, ack_header, wait_ack);
--   type tx_fifo_control_state_type is (idle, read, waiting);
--   type rx_fifo_control_state_type is (idle, write, waiting);
--   type command_type is (init_conf, master_write, master_read, get_status, nop);
  
  constant TRIGGER_SIZE : integer := 18;

 type monitor is
 record
 	control : std_logic_vector(35 downto 0);
 	trigger : std_logic_vector(TRIGGER_SIZE downto 0);
 end record;
 
 type timming is
 record
 	HOLD_START : std_logic_vector(7 downto 0);
 	T_LOW      : std_logic_vector(7 downto 0);
 	T_HIGH     : std_logic_vector(7 downto 0);
 	DELAY_STOP : std_logic_vector(7 downto 0);  
 	T_SUSTO    : std_logic_vector(7 downto 0);
 	T_WAIT     : std_logic_vector(7 downto 0);
 end record;

--! Status register type 
--   type status_type is
--   record
--     ack : std_logic;
--     BUS_BUSY : std_logic; 
--   end record;

--! Control register type
--   type control_type is
--   record
--     STRT : std_logic; -- Start condition.
--     MASL : std_logic; -- MAster (1) SLave operation (0).
--     TXRX : std_logic; -- Transmit(1)/Receive(0) operation.
--     RSTA: std_logic; -- Repeated start condition.
--   end record;

--! Internal signals between SCL and MASTER processes   
--   type scl_ma_t is
--   record
--     STOP      : std_logic;
--     --STOP_ENTER : std_logic;
--     --BIT_COUNT : integer range 0 to 10;
--     ZERO_BIT : std_logic;
--     
--   end record;

--! Signals that go into the buffer     
--   type buffer_write is
--   record
--     wr_ena  : std_logic;
--     wr_data : std_logic_vector (7 downto 0);
--     full    : std_logic;
--     dummy   : std_logic_vector(3 downto 0);
--   end record;

--! Signals that leave the buffer       
--   type buffer_read is
--   record
--     rd_ena  : std_logic;
--     rd_data : std_logic_vector (7 downto 0); 
--     empty   : std_logic;
--     dummy   : std_logic_vector(3 downto 0);
--   end record;
-- 
-- 
--   type scl_control_in is
--   record
--     st_stp : std_logic;
--   end record;
-- 
--   type scl_control_out is
--   record
--     st_stp : std_logic;
--   end record;
-- 
--   type scl_control_i is
--   record
--     sda : std_logic;
--     
--   end record;
-- 
--   type scl_control_o is
--   record
--     sda_scl  : std_logic;
--     sda_scma : std_logic;
--   end record;


end package i2c_pkg;
