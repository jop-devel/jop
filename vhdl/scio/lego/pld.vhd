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
--      pld.vhd
--
--      PLD that provides additional pins for simple inputs/outputs
--		(Belongs to the PLD Quartus project)
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

--
--	pld
--
--	The purpose of the PLD is to provide additional pins for simple inputs/outputs
--	since the FPGA's pins are too few.
--	Depending on whether the pin is configured as input or output in lego_pld_pack.vhd,
--	the pin state is transferred between the PLD and the FPGA in the appropriate direction
--	over the serial 'data' line.
--	The clock signal is the FPGA clock signal.
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.lego_pld_pack.all;

entity pld is
    
    port (
        clk    : in  std_logic;

        pld_strobe : in  std_logic;
        data : inout std_logic;
        
        pins : inout std_logic_vector(FORWARDED_PINS'high-2 downto 0);
        input_only_pins : in std_logic_vector(1 downto 0);	-- these can only be read :(

		ignored_pins:  out std_logic_vector(1 downto 0)		-- xxx
        );

end pld;

architecture rtl of pld is
    
    signal state: FORWARDED_PINS_INDEX_TYPE;
    signal next_state : FORWARDED_PINS_INDEX_TYPE;

    constant reset : std_logic := '0';

    signal in_pins              : FORWARDED_PINS;
    signal out_pins             : FORWARDED_PINS;

    component input_synchronizer
        port (
            clk    : in  std_logic;
            reset  : in  std_logic;
            input  : in  std_logic;
            output : out std_logic);
    end component;
    
begin  -- rtl
	ignored_pins(1 downto 0) <= (others => 'Z');	
    
    async: process (state, pld_strobe)
    begin  -- process async
        if pld_strobe = '1' or state = FORWARDED_PINS'high then
            next_state <= FORWARDED_PINS'low;
        else
            next_state <= state+1;
        end if;
    end process async;

    sync: process (clk)
    begin  -- process sync
        if reset = '1' then           -- asynchronous reset (active high)
            -- TODO
        elsif rising_edge(clk) then     -- rising clock edge
            if FORWARDED_PINS_DIRECTIONS(state) = dout then
                out_pins(state) <= data;                
            end if;
            
            if FORWARDED_PINS_DIRECTIONS(next_state) = din then
				case next_state is
					when others => data <= in_pins(next_state);
				end case;
            else
                data <= 'Z';
            end if;

            state <= next_state;
        end if;
    end process sync;

    g1: for i in FORWARDED_PINS'high-2 downto 0 generate
        g2: if FORWARDED_PINS_DIRECTIONS(i) = din generate
            input_synchronizer_0: input_synchronizer
                port map (
                        clk    => clk,
                        reset  => reset,
                        input  => pins(i),
                        output => in_pins(i));
        end generate g2;

        g3: if FORWARDED_PINS_DIRECTIONS(i) = dout generate
            pins(i) <= out_pins(i);
        end generate g3;
    end generate g1;

    g4: for i in 1 downto 0 generate
        input_synchronizer_1: input_synchronizer
            port map (
                    clk    => clk,
                    reset  => reset,
                    input  => input_only_pins(i),
                    output => in_pins(FORWARDED_PINS'high-1 + i));
    end generate g4;
    
end rtl;
