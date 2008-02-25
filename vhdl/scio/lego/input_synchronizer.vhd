--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2007, Peter Hilber
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


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
