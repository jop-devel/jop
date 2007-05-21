--
--      input_synchronizer.vhd
--
--      To avoid metastability
--      
--      Author: Peter Hilber                    peter.hilber@student.tuwien.ac.at
--
--
--
--      2007-03-26      created for Lego PCB
--
--      todo:
--
--


library IEEE;
use IEEE.std_logic_1164.all;

entity input_synchronizer is
    port (
        clk : in std_logic;
        reset: in std_logic;
        input: in std_logic;
        output: out std_logic
        );
end input_synchronizer;

architecture rtl of input_synchronizer is

signal buf : std_logic;
    
begin  -- rtl

    synchronize: process (clk, reset)
    begin  -- process synchronize
        if reset = '1' then            
            buf <= '0';
            output <= '0';
        elsif rising_edge(clk) then
            buf <= input;
            output <= buf;
        end if;
    end process synchronize;

end rtl;
