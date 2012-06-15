--
-- VHDL Architecture async_fifo_lib.synchronizer.synth
--
-- Created:
--          by - Juan.UNKNOWN (WIN32)
--          at - 05:16:21 09/23/2009
--
-- 
--
LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_arith.all;

library work;
USE work.fifo_pkg.all;

ENTITY synchronizer IS
  
  port(clk_1    : in std_logic;
       clk_2    : in std_logic;
       reset    : in std_logic;
       ptr      : in std_logic_vector((FIFO_ADD_WIDTH) downto 0);
       sync_ptr : out std_logic_vector((FIFO_ADD_WIDTH) downto 0)
       );
        
END ENTITY synchronizer;

--
ARCHITECTURE synth OF synchronizer IS

signal gray_ptr   : std_logic_vector((FIFO_ADD_WIDTH) downto 0);
signal bin_ptr    : std_logic_vector((FIFO_ADD_WIDTH) downto 0);
signal R1_gray_ptr: std_logic_vector((FIFO_ADD_WIDTH) downto 0);
signal R2_gray_ptr: std_logic_vector((FIFO_ADD_WIDTH) downto 0);
signal R3_gray_ptr: std_logic_vector((FIFO_ADD_WIDTH) downto 0);

BEGIN

  sync_ptr <= bin_ptr;


-- Binary to Gray conversion
BIN_2_GRAY: process(ptr)
  
  begin
    gray_ptr(FIFO_ADD_WIDTH) <= ptr(FIFO_ADD_WIDTH);
    for i in 0 to (FIFO_ADD_WIDTH - 1) loop
      gray_ptr(i) <= ptr(i) XOR ptr(i + 1);
    end loop;
    
  end process BIN_2_GRAY;
  
WT_REG: process(reset,clk_1)

  begin
    if reset ='1' then
      R1_gray_ptr <= (others => '0');
    else
      if (clk_1'EVENT and clk_1 = '1') then
        R1_gray_ptr <= gray_ptr after 2 ns;
      end if;
    end if;
    
  end process WT_REG;
    
	
	
	
	
	
RD_REG: process(reset,clk_2)

  begin
    if reset = '1' then
      R2_gray_ptr <= (others => '0');
      R3_gray_ptr <= (others => '0');
    else
      if (clk_2'EVENT and clk_2 = '1') then
        R2_gray_ptr <= R1_gray_ptr after 2 ns;
        R3_gray_ptr <= R2_gray_ptr after 2 ns;
      end if;
    end if;
    
  end process RD_REG;
    
-- Gray to binary conversion
GRAY_2_BIN: process(R3_gray_ptr)

  variable temp_sum : std_logic;

  begin
     temp_sum := '0'; 
     for i in (FIFO_ADD_WIDTH) downto 0 loop
      temp_sum := temp_sum XOR R3_gray_ptr(i);
      bin_ptr(i) <= temp_sum;
    end loop;
    
  end process GRAY_2_BIN;
 
END ARCHITECTURE synth;

