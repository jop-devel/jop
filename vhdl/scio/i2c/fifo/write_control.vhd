--! @file write_control.vhd
--! @brief Asynchronous fifo write control  
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2009-2012
--! @copyright GNU Public License.

-- VHDL Architecture async_fifo_lib.FIFO_write_control.comb
--
-- Created:
--          by - Juan.UNKNOWN (WIN32)
--          at - 10:34:36 09/20/2009
--
--
--
LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.numeric_std.all;
USE work.fifo_pkg.all;

ENTITY write_control IS

generic(ADDRESS  	: integer  := 3);
  
port (wclk           : in std_logic;
      reset          : in std_logic;
      write_enable   : in std_logic;
      rptr_sync      : in std_logic_vector ((ADDRESS) downto 0);
      flush		: in std_logic;
      occupancy_wr   : out std_logic_vector(15 downto 0);
      full           : out std_logic;
      wptr           : out std_logic_vector ((ADDRESS) downto 0);
      waddr          : out std_logic_vector ((ADDRESS - 1) downto 0);
      wen            : out std_logic
      --wen            : out std_logic
      );
        
END ENTITY write_control;

ARCHITECTURE wr_ctrl_arch OF write_control IS

constant FIFO_SIZE  : integer  := (2**ADDRESS);

signal int_wptr   : std_logic_vector((ADDRESS) downto 0);
signal counter_en : std_logic;
signal  rd_add    : std_logic_vector ((ADDRESS - 1) downto 0);
signal  wr_add    : std_logic_vector ((ADDRESS - 1) downto 0);
signal  rd_MSB    : std_logic;
signal  wr_MSB    : std_logic;


BEGIN
  
  wr_add <= int_wptr((ADDRESS - 1) downto 0);
  rd_add <= rptr_sync((ADDRESS - 1) downto 0);
  wr_MSB <= int_wptr(ADDRESS);
  rd_MSB <= rptr_sync(ADDRESS);
    
  wptr  <= int_wptr;
  waddr <= wr_add; 
  
wr_ctrl:process (rd_add, rd_MSB, wr_add, wr_MSB, write_enable)
   
begin

	  occupancy_wr(15 downto ADDRESS) <= (others => '0');
      
      if wr_MSB = rd_MSB then 
      	occupancy_wr(ADDRESS-1 downto 0) <= std_logic_vector(unsigned(wr_add) - unsigned(rd_add));
        full <= '0';
        wen <= write_enable;
        counter_en <= write_enable;

	  else
        
        if rd_add = wr_add then
          full <= '1';
          wen <= '0';
          counter_en <= '0';
          occupancy_wr(ADDRESS-1 downto 0) <= (others => '1');
        else               
          full <= '0'; 
          wen <= write_enable;
          counter_en <= write_enable;
          occupancy_wr(ADDRESS-1 downto 0) <= std_logic_vector(FIFO_SIZE - unsigned(rd_add) + unsigned(wr_add));
        end if;
      
      end if;
  
end process;

process(wclk, reset)

	begin
	
	if reset = '1' then
		int_wptr <= (others  => '0');
		
	elsif wclk'event and wclk = '1' then
		
		if flush = '1' then
			int_wptr <= (others  => '0');
		elsif counter_en = '1' then
			int_wptr <= std_logic_vector(unsigned(int_wptr) + 1);
		end if;
		
	end if;

	end process;
  
END ARCHITECTURE wr_ctrl_arch;
