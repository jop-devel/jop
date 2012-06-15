-- VHDL Entity async_fifo_lib.counter.symbol
--
-- Created:
--          by - Juan.UNKNOWN (WIN32)
--          at - 12:06:06 09/20/2009
--
--
--
LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_arith.all;
USE ieee.std_logic_unsigned.all;
USE work.fifo_pkg.all;

ENTITY counter IS
   PORT(
      clk         : IN     std_logic;
      reset       : IN     std_logic;
      flush_fifo	: in std_logic;
      enable      : IN     std_logic;
      count_value : OUT    std_logic_vector ((FIFO_ADD_WIDTH) DOWNTO 0)
       );

END counter ;

ARCHITECTURE synth OF counter IS
   
signal int_count_value : std_logic_vector ((FIFO_ADD_WIDTH) downto 0);


BEGIN

count_value <= int_count_value((FIFO_ADD_WIDTH) downto 0);

  PROCESS (clk,reset, flush_fifo)

   BEGIN
      if  reset = '1' or flush_fifo = '1' then
         int_count_value <= (others => '0');
      elsif (clk'EVENT and clk = '1')  then
            if enable = '1' then
              --if int_count_value = MAX_COUNT then
              --  int_count_value <= (others => '0');
              --else
                int_count_value <= int_count_value + 1;
              end if;
         --   end if;
         --end if;
      end if;
      
   END PROCESS;

END synth;
