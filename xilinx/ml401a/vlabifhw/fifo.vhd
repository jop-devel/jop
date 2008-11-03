--
--  
--  fifo.vhd
--  This FIFO interface always uses fifo_ms as an implementation.
--
--  Copyright (C) 2008, Jack Whitham
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

entity fifo is

generic (
    width   : integer := 8; 
    depth   : integer := 4; 
    thres   : integer := 2);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	din		: in std_logic_vector(width-1 downto 0);
	dout	: out std_logic_vector(width-1 downto 0);

	rd		: in std_logic;
	wr		: in std_logic;

	empty	: out std_logic;
	full	: out std_logic;
	half	: out std_logic
);
end fifo ;

architecture rtl of fifo is

begin

    msf : entity fifo_ms
        generic map (
            width => width,
            depth => depth,
            thres => thres )
        port map (
            clk => clk,
            reset => reset,
            din => din,
            dout => dout,
            rd => rd,
            wr => wr,
            empty => empty,
            full => full,
            half => half);
	
end rtl;

