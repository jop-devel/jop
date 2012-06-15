--
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
USE ieee.std_logic_unsigned.all;

library work;
USE work.fifo_pkg.all;


ENTITY FIFO_write_control IS

port (wclk           : in std_logic;
      reset          : in std_logic;
            flush_fifo	: in std_logic;
      write_enable   : in std_logic;
      rptr_sync      : in std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
      fifo_occu_in   : out std_logic_vector((FIFO_ADD_WIDTH - 1) downto 0);
      full           : out std_logic;
      wptr           : out std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
      waddr          : out std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
      --wen            : out std_logic_vector(0 downto 0)
      wen            : out std_logic
      );

END ENTITY FIFO_write_control;

ARCHITECTURE comb OF FIFO_write_control IS

signal int_wptr   : std_logic_vector((FIFO_ADD_WIDTH) downto 0);
--signal int_full   : std_logic;
signal counter_en : std_logic;
signal  rd_add    : std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
signal  wr_add    : std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
signal  rd_MSB    : std_logic;
signal  wr_MSB    : std_logic;

COMPONENT counter IS

  port (clk        : in std_logic;
       reset       : in std_logic;
             flush_fifo	: in std_logic;
       enable      : in std_logic;
       count_value : out std_logic_vector ((FIFO_ADD_WIDTH) downto 0)
       );
       
END COMPONENT;

BEGIN
  
  wr_add <= int_wptr((FIFO_ADD_WIDTH - 1) downto 0);
  rd_add <= rptr_sync((FIFO_ADD_WIDTH - 1) downto 0);
  wr_MSB <= int_wptr(FIFO_ADD_WIDTH);
  rd_MSB <= rptr_sync(FIFO_ADD_WIDTH);
    
  wptr  <= int_wptr;
  waddr <= wr_add; 
  
-------------------------------------------------------------------
WR_ADDRESS: counter port map (clk         => wclk,
                              reset       => reset,
                              flush_fifo	=>  flush_fifo,
                              enable      => counter_en,
                              count_value => int_wptr
                              );
--------------------------------------------------------------------             
           

		   
wr_ctrl:process (rd_add, rd_MSB, wr_add, wr_MSB, write_enable)
   
begin

      if wr_MSB /= rd_MSB then 
        fifo_occu_in <= (FIFO_WORDS) - (rd_add - wr_add);
-- Independently of external write_enable, if the FIFO is full 
-- writting should be stopped
        if rd_add = wr_add then
          full <= '1';
          --wen(0) <= '0';
          wen <= '0';
          counter_en <= '0';
        else
          full <= '0';
          if write_enable = '1' then
            --wen(0) <= '1';
            wen <= '1';
            counter_en <= '1';
          else
            --wen(0) <= '0';
            wen <= '0';
            counter_en <= '0';
          end if;
        end if;
      else
        fifo_occu_in <= wr_add - rd_add;
        full <= '0';
-- Write only if the FIFO is not full and environment wants to write
        if write_enable = '1' then
		--wen(0) <= '1';
            wen <= '1';
            counter_en <= '1';
          else
		--wen(0) <= '0';
            wen <= '0';
            counter_en <= '0';
          end if;
      end if;
  
end process;
  
END ARCHITECTURE comb;
