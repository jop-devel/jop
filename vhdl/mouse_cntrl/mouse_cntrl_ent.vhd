-- **************************************
-- mouse_cntrl entity
-- **************************************
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

entity mouse_cntrl is
  generic (
    addr_bits 	      : integer := 23;
    CLK_FREQ          : integer := 50000000;
    TIMEOUT_REG_WIDTH : integer := 13
    );
  port ( 
    clk 			: in std_logic;
    rst 			: in std_logic;
    
    address			: in std_logic_vector(addr_bits-1 downto 0);
    wr_data			: in std_logic_vector(31 downto 0);
    rd 				: in std_logic;
    wr				: in std_logic;
    rd_data			: out std_logic_vector(31 downto 0);
    rdy_cnt			: out unsigned(1 downto 0);
    
    ps2_clk         : inout std_logic;
    ps2_data        : inout std_logic;
    
    int_flg         : out std_logic
    );
end entity;
