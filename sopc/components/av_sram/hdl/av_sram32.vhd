--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
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
--	av_sram32.vhd
--
--	Avalon compliant external memory interface
--	for 32-bit SRAM (e.g. Cyclone board, Spartan-3 Starter Kit)
--
--	memory mapping
--	
--		000000-x7ffff	external SRAM (w mirror)	max. 512 kW (4*4 MBit)
--
--	RAM: 32 bit word
--
--
--	2006-08-13	Adapted from sc_ram32
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity av_sram is
generic (ram_ws : integer; addr_bits : integer);

port (

	clk, reset	: in std_logic;

-- Avalon interface

	address			: in std_logic_vector(addr_bits-1 downto 0);
	chipselect		: in std_logic; -- do I need it?
	read, write		: in std_logic;
	writedata		: in std_logic_vector(31 downto 0);
	readdata		: out std_logic_vector(31 downto 0);
	waitrequest		: out std_logic;

-- memory interface

	ram_addr	: out std_logic_vector(addr_bits-1 downto 0);
	ram_data	: inout std_logic_vector(31 downto 0);
	ram_ncs		: out std_logic;
	ram_noe		: out std_logic;
	ram_nwe		: out std_logic

);
end av_sram;

architecture rtl of av_sram is

--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, rd2,
							wr1, rdy
						);
	signal state 		: state_type;
	signal next_state	: state_type;

	signal nwr_int		: std_logic;
	signal wait_state	: unsigned(3 downto 0);
	signal cnt			: unsigned(1 downto 0);

	signal dout_ena		: std_logic;
	signal rd_data_ena	: std_logic;

	signal rd, wr		: std_logic;

	signal ram_din		: std_logic_vector(31 downto 0);
	signal ram_dout		: std_logic_vector(31 downto 0);

begin

	process(dout_ena, ram_dout)
	begin
		if dout_ena='1' then
			ram_data <= ram_dout;
		else
			ram_data <= (others => 'Z');
		end if;
	end process;
	ram_din <= ram_data;

--
--	Register memory address, write data and read data
--
process(clk, reset)
begin
	if reset='1' then

		ram_addr <= (others => '0');
		ram_dout <= (others => '0');
		readdata <= (others => '0');

	elsif rising_edge(clk) then

		if rd='1' or wr='1' then
			ram_addr <= address;
		end if;
		if wr='1' then
			ram_dout <= writedata;
		end if;
		if rd_data_ena='1' then
			readdata <= ram_din;
		end if;

	end if;
end process;

--
--	'delay' nwe 1/2 cycle -> change on falling edge
--
process(clk, reset)

begin
	if (reset='1') then
		ram_nwe <= '1';
	elsif falling_edge(clk) then
		ram_nwe <= nwr_int;
	end if;

end process;

process(state, read, write)

begin
	waitrequest <= '1';
	if (state=idl and read='0' and write='0') or state=rdy then
		waitrequest <= '0';
	end if;
end process;

--
--	next state logic
--
process(state, read, write, wait_state)

begin

	next_state <= state;

	rd <= '0';
	wr <= '0';

	case state is

		when idl =>
			if chipselect='1' then
				if read='1' then
					rd <= '1';
					if ram_ws=0 then
						-- then we omit state rd1!
						next_state <= rd2;
					else
						next_state <= rd1;
					end if;
				elsif write='1' then
					wr <= '1';
					next_state <= wr1;
				end if;
			end if;

		-- the WS state
		when rd1 =>
			if wait_state=2 then
				next_state <= rd2;
			end if;

		-- last read state
		when rd2 =>
			next_state <= rdy;
-- avoid this on Avalon - read/write is set until waitrequest goes
-- to 0.
--			-- This should do to give us a pipeline
--			-- level of 2 for read
--			if rd='1' then
--				if ram_ws=0 then
--					-- then we omit state rd1!
--					next_state <= rd2;
--				else
--					next_state <= rd1;
--				end if;
--			elsif wr='1' then
--				next_state <= wr1;
--			end if;
			
		-- the WS state
		when wr1 =>
			if wait_state=1 then
				next_state <= rdy;
			end if;

		-- one extra state as read/write is still active
		-- on the last cycle where we have to deassert
		-- waitrequest
		when rdy =>
			next_state <= idl;

	end case;
				
end process;

--
--	state machine register
--	output register
--
process(clk, reset)

begin
	if (reset='1') then
		state <= idl;
		dout_ena <= '0';
		ram_ncs <= '1';
		ram_noe <= '1';
		rd_data_ena <= '0';
	elsif rising_edge(clk) then

		state <= next_state;
		dout_ena <= '0';
		ram_ncs <= '1';
		ram_noe <= '1';
		rd_data_ena <= '0';

		case next_state is

			when idl =>

			-- the wait state
			when rd1 =>
				ram_ncs <= '0';
				ram_noe <= '0';

			-- last read state
			when rd2 =>
				ram_ncs <= '0';
				ram_noe <= '0';
				rd_data_ena <= '1';
				
				
			-- the WS state
			when wr1 =>
				ram_ncs <= '0';
				dout_ena <= '1';

			when rdy =>

		end case;
					
	end if;
end process;

--
--	nwr combinatorial processing
--	for the negativ edge
--
process(next_state, state)
begin

	nwr_int <= '1';
	if next_state=wr1 then
		nwr_int <= '0';
	end if;

end process;

--
-- wait_state processing
-- cs delay, dout enable
--
process(clk, reset)
begin
	if (reset='1') then
		wait_state <= (others => '1');
		cnt <= "00";
	elsif rising_edge(clk) then

		wait_state <= wait_state-1;

		cnt <= "11";
		if next_state=idl then
			cnt <= "00";
		-- if wait_state<4 then
		elsif wait_state(3 downto 2)="00" then
			cnt <= wait_state(1 downto 0)-1;
		end if;

		if rd='1' or wr='1' then
			wait_state <= to_unsigned(ram_ws+1, 4);
			if ram_ws<3 then
				cnt <= to_unsigned(ram_ws+1, 2);
			else
				cnt <= "11";
			end if;
		end if;

	end if;
end process;

end rtl;
