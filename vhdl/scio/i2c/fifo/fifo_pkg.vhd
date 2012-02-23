--
-- VHDL Package Header async_fifo_lib.types
--
-- Created:
--          by - Juan.UNKNOWN (WIN32)
--          at - 13:38:54 09/18/2009
--
-- 
--
LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_unsigned.all;
USE ieee.numeric_std.all;


PACKAGE fifo_pkg IS
  
    constant FIFO_ADD_WIDTH  : integer  := 4; -- FIFO_ADD_WIDTH + 1 = number of bits in each address
    constant FIFO_WORDS      : std_logic_vector((FIFO_ADD_WIDTH - 1) downto 0) := (others => '1'); 
    constant MAX_COUNT       : std_logic_vector(FIFO_ADD_WIDTH downto 0) := (others => '1');
    
END fifo_pkg;
       