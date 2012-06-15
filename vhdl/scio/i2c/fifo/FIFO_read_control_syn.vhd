--
-- VHDL Architecture async_fifo_lib.FIFO_read_control.syn
--
-- Created:
--          by - Juan.UNKNOWN (WIN32)
--          at - 01:37:13 09/23/2009
--
--
--
LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_unsigned.all;

library work;
USE work.fifo_pkg.all;

--use work.i2c_pkg.all;

ENTITY FIFO_read_control IS

port (rclk           : in std_logic;
      reset          : in std_logic;
      flush_fifo	: in std_logic;
      read_enable    : in std_logic;
      wptr_sync      : in std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
      fifo_occu_out  : out std_logic_vector((FIFO_ADD_WIDTH - 1) downto 0);
      empty          : out std_logic;
      rptr           : out std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
      raddr          : out std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
      ren            : out std_logic
      );

END ENTITY FIFO_read_control;

--

ARCHITECTURE comb OF FIFO_read_control IS

signal int_rptr    : std_logic_vector((FIFO_ADD_WIDTH) downto 0);
signal counter_en  : std_logic;
signal  rd_add    : std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
signal  wr_add    : std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
signal  rd_MSB    : std_logic;
signal  wr_MSB    : std_logic;

COMPONENT counter IS

port (clk         : in std_logic;
      reset       : in std_logic;
      flush_fifo	: in std_logic;
      enable      : in std_logic;
      count_value : out std_logic_vector ((FIFO_ADD_WIDTH) downto 0)
      );

END COMPONENT;

	signal ren_reg: std_logic;
	signal ren_i: std_logic;

BEGIN

  rd_add <= int_rptr((FIFO_ADD_WIDTH - 1) downto 0);
  wr_add <= wptr_sync((FIFO_ADD_WIDTH - 1) downto 0);
  rd_MSB <= int_rptr(FIFO_ADD_WIDTH);
  wr_MSB <= wptr_sync(FIFO_ADD_WIDTH);
  
  rptr   <= int_rptr;
  raddr  <= rd_add;
   
  -- NOTE
  -- Dual port ram from xilinx needs that "ren" signal stays high for two clock
  -- cycles. Altera's dual port ram does not require this.

  --ren <= ren_i or ren_reg;
  ren <= ren_i;

-------------------------------------------------------------------
RD_ADDRESS: counter port map (clk         => rclk,
                              reset       => reset,
                              flush_fifo  => flush_fifo,
                              enable      => counter_en,
                              count_value => int_rptr
                              );
--------------------------------------------------------------------             

rd_ctrl:process (rd_add, rd_MSB, read_enable, ren_reg, wr_add, wr_MSB)

begin

	ren_i <= ren_reg; 

    if wr_MSB = rd_MSB then
      fifo_occu_out <= wr_add - rd_add;
      if rd_add = wr_add then
-- Independently of external read_enable, if the FIFO is empty 
-- reading should be stopped 
        empty <= '1';
        counter_en <='0';
        ren_i <= '0';
      else                
        empty <= '0';
-- Read only if the FIFO is not empty and environment wants to read
        if read_enable = '1' then
          ren_i <= '1';
          counter_en <= '1';
        else
          ren_i <= '0';
          counter_en <= '0';
        end if;
      end if;  
    else
      fifo_occu_out <= (FIFO_WORDS) - (rd_add - wr_add);
      empty <= '0';
      if read_enable = '1' then
         ren_i <= '1';
         counter_en <= '1';
      else
        ren_i <= '0';
        counter_en <= '0';         
      end if;      
    end if;
    
end process;

process(rclk, reset)

	begin
	
	if reset = '1' then
		ren_reg <= '0';
	elsif rclk'event and rclk = '1' then
		ren_reg <= ren_i;
	end if;
	
	end process;
		

END ARCHITECTURE comb;