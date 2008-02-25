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
--      pld_interface.vhd
--
--      Communicates with PLD over serial line
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


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.lego_pld_pack.all;

entity pld_interface is
    
    port (
        clk                  : in    std_logic;
        reset                : in    std_logic;
        out_pins              : in FORWARDED_PINS;
        in_pins             : out FORWARDED_PINS;

        -- to pld

        pld_strobe           : out std_logic;
        pld_clk              : out   std_logic;
        data                 : inout std_logic);

end pld_interface;

architecture rtl of pld_interface is

    signal state: FORWARDED_PINS_INDEX_TYPE;
    signal next_state : FORWARDED_PINS_INDEX_TYPE;

    signal in_pins_only_inputs : FORWARDED_PINS;

	signal cnt	: unsigned(2 downto 0);
	signal stb	: std_logic;

begin  -- rtl

	
	process(clk,reset)
	begin
        if reset = '1' then
            cnt <= "000";
        elsif rising_edge(clk) then
			cnt <= cnt+1;
		end if;
	end process;

	pld_clk <= not cnt(2);

	pld_strobe <= stb;
    
    async: process (state)
    begin  -- process async
        if state = FORWARDED_PINS'high then
            next_state <= FORWARDED_PINS'low;
        else
            next_state <= state+1;
        end if;
    end process async;

    sync: process (clk, reset)
    begin  -- process sync
        if reset = '1' then           -- asynchronous reset (active high)
            state <= FORWARDED_PINS'low;
        elsif rising_edge(clk) then     -- rising clock edge

			-- output part
			if cnt=5 then
				if state=FORWARDED_PINS'high then
					stb <= '1';
				else
					stb <= '0';
				end if;
            	if FORWARDED_PINS_DIRECTIONS(next_state) = dout then
                	data <= out_pins(next_state);
            	else
                	data <= 'Z';
            	end if;            
			end if;

			-- input part
			if cnt=1 then
            	if FORWARDED_PINS_DIRECTIONS(state) = din then
                	in_pins_only_inputs(state) <= data;
            	end if;
			end if;

			if cnt=7 then
            	state <= next_state;
			end if;
        end if;
    end process sync;

    g_pins: for i in FORWARDED_PINS'range generate
        forward_input_pins: if FORWARDED_PINS_DIRECTIONS(i) = din generate
            in_pins(i) <= '0' when reset = '1' else in_pins_only_inputs(i);
        end generate forward_input_pins;
        
        reset_unused_input_pins: if FORWARDED_PINS_DIRECTIONS(i) = dout generate
            in_pins(i) <= '0';
        end generate reset_unused_input_pins;
    end generate g_pins;
end rtl;
