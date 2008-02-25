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


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.lego_pld_pack.all;             -- todo change package name

--xxx
--from vhdl mini reference
--Concurrent Signal Assignment
--A signal assignment statement represents a process that assigns values to signals. It has three basic formats. 
--A <= B; A <= B when condition1 elseC when condition2 else D when condition3 else E;
--with expression select A <= B when choice1, C when choice2, D when choice3, E when others;

entity pldtest is
    
    port (
        clk                 : in    std_logic;
        reset               : in    std_logic;
		simulated_input_pins	: in	FORWARDED_PINS;
		in_pins_i			: out 	FORWARDED_PINS);
end pldtest;

architecture rtl of pldtest is

    component pld_interface
        port (
            clk            : in     std_logic;
            reset          : in     std_logic;
            out_pins : in  FORWARDED_PINS;
            in_pins : out FORWARDED_PINS;
            pld_strobe     : buffer std_logic;
            pld_clk        : out    std_logic;
            data           : inout  std_logic);
    end component;
    
    component pld
        port (
            clk        : in    std_logic;
            pld_strobe : in    std_logic;
            data       : inout std_logic;
            pins: inout std_logic_vector(FORWARDED_PINS'high-2 downto 0);
            input_only_pins : in std_logic_vector(1 downto 0));		-- these can only be read :(
    end component;

    --signal in_pins_i        :        FORWARDED_PINS;
    signal out_pins_i       :        FORWARDED_PINS;
    signal pld_strobe_i     :        std_logic;
    signal pld_clk_i        :        std_logic;
    signal data_i           :        std_logic;

    signal pins_i : FORWARDED_PINS;

    signal joined_pins_i : FORWARDED_PINS;
begin

    DUT_JOP: pld_interface
        port map (
            clk            => clk,
            reset          => reset,
            out_pins => out_pins_i,
            in_pins => in_pins_i,
            pld_strobe     => pld_strobe_i,
            pld_clk        => pld_clk_i,
            data           => data_i);

    DUT_PLD: pld
        port map (
            clk        => clk,        -- HACK w/ pld_clk_i, it does not simulate correctly
            pld_strobe => pld_strobe_i,
            data       => data_i,
            pins => joined_pins_i(FORWARDED_PINS'high-2 downto 0),
            input_only_pins => joined_pins_i(FORWARDED_PINS'high downto FORWARDED_PINS'high-1)
            );

	joined_pins_i <= simulated_input_pins;

    -- TODO implement in scio_lego.vhd
    p_scio_on_reset: process (reset)
    begin  -- process p_scio_on_reset
        if reset = '1' then
            out_pins_i <= (others => '0');
        end if;
    end process p_scio_on_reset;
end rtl;
