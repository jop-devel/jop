--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2005,2006, Martin Schoeberl (martin@jopdesign.com)
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
--	sigma_delta.vhd
--
--	Motor and sensor interface for LEGO MindStorms
--	
--	Original author: Martin Schoeberl	martin@jopdesign.com
--  Author: Peter Hilber                peter.hilber@student.tuwien.ac.at
--
--
--	2005-12-22	adapted for SimpCon interface
--  2006-12-05  converted lesens to sigma_delta: tried to build a general sigma delta AC
--
--

--
--	sigma_delta
--
--	sigma delta AD converter
--	



library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity sigma_delta is
    generic (
        dout_width : integer);
    port (
	clk		: in std_logic;
	reset           : in std_logic;

	clksd		: in std_logic;         -- '1' to read sdi
        clkint          : in std_logic;         -- '1' to integrate value

	dout            : out std_logic_vector((dout_width-1) downto 0);

	sdi		: in std_logic;
	sdo		: out std_logic
        );
    
end sigma_delta;

architecture rtl of sigma_delta is

    signal val			: unsigned((dout_width-1) downto 0);

    signal rx_d			: std_logic;
    signal serdata		: std_logic;

    signal spike		: std_logic_vector(2 downto 0);	-- sync in, filter

begin


    sdo <= serdata;

--
--	sigma delta converter
--
    process(clk, reset)

    begin
	if (reset='1') then
            spike <= (others => '0');
            dout <= (others => '0');
            val <= (others => '0');
            serdata <= '0';

	elsif rising_edge(clk) then

            if clksd='1' then

--
--	delay
--
                spike(0) <= sdi;
                spike(2 downto 1) <= spike(1 downto 0);
                serdata <= rx_d;		-- no inverter, using an invert. comperator	-- todo analyze timing

--
--	integrate
--

                if serdata='0' then		-- 'invert' value
                    val <= val+1;
                end if;

                if clkint='1' then
                    val <= (others => '0');

--
--	output
--
				  	dout <= std_logic_vector(val);
                end if;

            end if;

	end if;

    end process;



--
--	filter input
--
    with spike select
        rx_d <=	
        '0' when "000",
        '0' when "001",
        '0' when "010",
        '1' when "011",
        '0' when "100",
        '1' when "101",
        '1' when "110",
        '1' when "111",
        'X' when others;
end rtl;
