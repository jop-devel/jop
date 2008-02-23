--
--
--  This file is a part of JOP, the Java Optimized Processor
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
--	wb_mem_sram32.vhd
--
--	WISHBONE compliant external memory interface
--	for 32-bit SRAM (e.g. Cyclone board)
--
--	Connects between mem_wb and the external memory bus
--
--	memory mapping
--	
--		000000-x7ffff	external SRAM (w mirror)	max. 512 kW (4*4 MBit)
--		080000-xfffff	external Flash (w mirror)	max. 512 kB (4 MBit)
--		100000-xfffff	external NAND flash
--
--	RAM: 32 bit word
--	ROM: 8 bit word (for flash programming)
--
--	todo:
--		
--
--	2005-11-12	first version
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.wb_pack.all;

entity wb_mem_if is
generic (ram_cnt : integer; rom_cnt : integer);

port (

-- jop interface

	clk, reset	: in std_logic;

-- internal ack that is one cycle earlier than wb ack

	early_ack	: out std_logic;

-- wishbone interface

	wb_in		: in wb_mem_out_type;
	wb_out		: out wb_mem_in_type;

-- memory interface

	ram_addr	: out std_logic_vector(17 downto 0);
	ram_dout	: out std_logic_vector(31 downto 0);
	ram_din		: in std_logic_vector(31 downto 0);
	ram_dout_en	: out std_logic;
	ram_ncs		: out std_logic;
	ram_noe		: out std_logic;
	ram_nwe		: out std_logic;

--
--	config/program flash and big nand flash interface
--
	fl_a	: out std_logic_vector(18 downto 0);
	fl_d	: inout std_logic_vector(7 downto 0);
	fl_ncs	: out std_logic;
	fl_ncsb	: out std_logic;
	fl_noe	: out std_logic;
	fl_nwe	: out std_logic;
	fl_rdy	: in std_logic

);
end wb_mem_if;

architecture rtl of wb_mem_if is

--
--	Wishbone signals
--
	signal ena			: std_logic;
	signal ack			: std_logic;
	signal wr, rd		: std_logic;

--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, rd2,
							wr1, wr2
						);
	signal state 		: state_type;
	signal next_state	: state_type;

	signal nwr_int		: std_logic;
	signal wait_state	: unsigned(3 downto 0);
	signal dout_ena		: std_logic;

	constant bc_ram_cnt		: integer := ram_cnt;				-- a different constant for perf. tests

begin

	-- Do we need this and of stb and cyc?
	-- In the simple SLAVE I/O example only stb is used
	-- However, when it's generated with one signal in
	-- the master it will get optimized away anyway.

	ena <= wb_in.cyc and wb_in.stb;

	wr <= wb_in.we and ena;
	rd <= not wb_in.we and ena;

	ram_ncs <= not ena;

	wb_out.ack <= ack;

	-- we don't care about wb_in.sel

	wb_out.dat <= ram_din;

	ram_dout_en <= dout_ena;
	ram_dout <= wb_in.dat;



--
--	Register memory address, ncs, and noe
--	directly from wishbone control signals.
--
--process(clk, reset)
--begin
--	if reset='1' then
--
--		ram_addr <= (others => '0');
--		ram_ncs <= '1';
--		ram_noe <= '1';
--
--	elsif rising_edge(clk) then
--
--		if (ena='1') then
--			ram_addr <= wb_in.adr(17 downto 0);
--		end if;
--		ncs <= not ena;
--		noe <= not rd;
--
--	end if;
--end process;

--
--	address and control are registered in mem_wb
--

	ram_addr <= wb_in.adr(17 downto 0);


--
--	'delay' nwe 1/2 cycle -> change on falling edge
--
process(clk, reset)

begin
	if (reset='1') then
		ram_nwe <= '1';
		ram_noe <= '1';
	elsif falling_edge(clk) then
		ram_nwe <= nwr_int;
		ram_noe <= not rd;
	end if;

end process;


--
--	next state logic
--		+ early_ack
--
process(state, rd, wr, wait_state)

begin

	next_state <= state;

	early_ack <= '0';

	case state is

		when idl =>
			if rd='1' then
				if ram_cnt=2 then
					-- then we omit state rd1!
					next_state <= rd2;
					early_ack <= '1';
				else
					next_state <= rd1;
				end if;
			elsif wr='1' then
				next_state <= wr1;
			end if;

		-- the WS state
		when rd1 =>
			-- release mem_bsy one cycle earlier
			if wait_state=1 then
				next_state <= rd2;
				early_ack <= '1';
			end if;

		-- last read state for ack
		when rd2 =>
			next_state <= idl;
			
		-- the WS state
		when wr1 =>
			-- release mem_bsy two cycle earlier
			if wait_state=2 then
				early_ack <= '1';
			end if;
			-- early ack will be two cycles for
			-- ram_cnt=3
			if wait_state=1 then
				early_ack <= '1';
				next_state <= wr2;
			end if;

		-- last write state for ack
		when wr2 =>
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
		ack <= '0';
		dout_ena <= '0';
	elsif rising_edge(clk) then

		state <= next_state;
		ack <= '0';
		dout_ena <= '0';

		case next_state is

			when idl =>

			-- the WS state
			when rd1 =>

			-- last read state for ack
			when rd2 =>
				ack <= '1';
				
			-- the WS state
			when wr1 =>
				dout_ena <= '1';

			-- last write state for ack
			when wr2 =>
				ack <= '1';
				dout_ena <= '1';
				
		end case;
					
	end if;
end process;

--
--	nwr combinatorial processing
--	for the negativ edge
--
process(next_state)
begin

	nwr_int <= '1';
	if next_state=wr1 or next_state=wr2 then
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
		wait_state <= (others => '0');
	elsif rising_edge(clk) then

		wait_state <= wait_state-1;
		if state=idl or state=rd2 or state=wr2 then
			if rd='1' then
				wait_state <= to_unsigned(ram_cnt-2, 4);
			elsif wr='1' then
				-- one more cycle for the write
				-- But in original mem32 this was only true
				-- for ram_cnt=2!
				wait_state <= to_unsigned(ram_cnt-1, 4);
			end if;
		end if;

	end if;
end process;

-- TODO: move Flash interface to a second WB interface

	fl_a <= (others => '0');
	fl_d <= (others => 'Z');
	fl_ncs <= '1';
	fl_ncsb <= '1';
	fl_noe <= '1';
	fl_nwe <= '1';
--	fl_rdy	: in std_logic

end rtl;
