--! @file fifo_pkg.vhd
--! @brief Auxiliary package for the fifo design (constants, type definitions)  
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2009-2012
--! @copyright GNU Public License.
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
USE ieee.numeric_std.all;

PACKAGE fifo_pkg IS
  
--    constant ADDRESS  	: integer  := 3; -- ADDRESS + 1 = Bits in address, MSB used to check wrap around
    constant DATA_SIZE  : integer  := 8; -- Bits per word
--    constant FIFO_SIZE  : integer  := (2**ADDRESS);-- std_logic_vector((ADDRESS - 1) downto 0) := (others => '1'); 
    
    type fifo_in_type is 
    record
        write_enable : std_logic;
        read_enable  : std_logic;
        write_data_in : std_logic_vector(DATA_SIZE-1 downto 0);
        flush	: std_logic;
   	end record;
   	
    type fifo_out_type is 
    record
        occupancy_rd  	  : std_logic_vector(15 downto 0);
        occupancy_wr  	  : std_logic_vector(15 downto 0);
        read_data_out : std_logic_vector(DATA_SIZE-1 downto 0);
        full          : std_logic; 
        empty         : std_logic;
    end record;
    
END fifo_pkg;
       