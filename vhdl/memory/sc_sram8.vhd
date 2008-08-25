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
--	sc_sram16.vhd
--
--	SimpCon compliant external memory interface
--	for 16-bit SRAM (e.g. Altera DE2 board)
--
--	High 16-bit word is at lower address
--
--	Connection between mem_sc and the external memory bus
--
--	memory mapping
--	
--		000000-x7ffff	external SRAM (w mirror)	max. 512 kW (4*4 MBit)
--
--	RAM: 16 bit word
--
--
--	2006-08-01	Adapted from sc_ram32.vhd
--	2006-08-16	Rebuilding the already working (lost) version
--				Use wait_state, din register without MUX
--	2007-06-04	changed SimpCon to records
--	2007-09-09	Additional input register for high data (correct SimpCon violation)
--	2008-05-29	nwe on pos edge, additional wait state for write
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;

entity sc_mem_if is
generic (ram_ws : integer; addr_bits : integer);

port (

	clk, reset	: in std_logic;

--
--	SimpCon memory interface
--
	sc_mem_out		: in sc_out_type;
	sc_mem_in		: out sc_in_type;

-- memory interface

	ram_addr	: out std_logic_vector(addr_bits-1 downto 0);
	ram_dout	: out std_logic_vector(7 downto 0);
	ram_din		: in std_logic_vector(7 downto 0);
	ram_dout_en	: out std_logic;
	ram_ncs		: out std_logic;
	ram_noe		: out std_logic;
	ram_nwe		: out std_logic

);
end sc_mem_if;

architecture rtl of sc_mem_if is

--
--	signals for mem interface
--
	type state_type		is (
							idl,
							rd1_1st, rd2_1st,
							rd1_2nd, rd2_2nd,
							rd1_3rd, rd2_3rd,
							rd1_4th, rd2_4th,							
							wr1_1st, wr2_1st, wr_idl_1st,
							wr1_2nd, wr2_2nd, wr_idl_2nd,
							wr1_3rd, wr2_3rd, wr_idl_3rd,
							wr1_4th, wr2_4th
						);
	signal state 		: state_type;
	signal next_state	: state_type;

	signal wait_state	: unsigned(3 downto 0);
	signal cnt			: unsigned(1 downto 0);

	signal dout_ena			: std_logic;
	signal rd_data_ena_1st	: std_logic;
	signal rd_data_ena_2nd	: std_logic;
	signal rd_data_ena_3rd	: std_logic;
	signal rd_data_ena_4th	: std_logic;
	
	signal addr				: std_logic_vector(addr_bits-1 downto 0);
	signal inc_addr			: std_logic;

	signal wr_2nd			: std_logic;
	signal wr_3rd			: std_logic;
	signal wr_4th			: std_logic;

	signal ram_dout_2nd		: std_logic_vector(7 downto 0);
	signal ram_dout_3rd		: std_logic_vector(7 downto 0);
	signal ram_dout_4th		: std_logic_vector(7 downto 0);

	signal ram_din_1st		: std_logic_vector(7 downto 0);
	signal ram_din_2nd		: std_logic_vector(7 downto 0);
	signal ram_din_3rd		: std_logic_vector(7 downto 0);

	signal ram_din_reg		: std_logic_vector(31 downto 0);
	
	signal ram_ws_wr		: integer;

begin

	ram_ws_wr <= ram_ws+1; -- additional wait state for SRAM
	ram_dout_en <= dout_ena;
	ram_addr <= addr;
	sc_mem_in.rdy_cnt <= cnt;

--
--	Register memory address, write data and read data
--
process(clk, reset)
begin
	if reset='1' then

		addr <= (others => '0');
		ram_dout <= (others => '0');
		ram_dout_2nd <= (others => '0');
		ram_dout_3rd <= (others => '0');
		ram_dout_4th <= (others => '0');

	elsif rising_edge(clk) then

		if sc_mem_out.rd='1' or sc_mem_out.wr='1' then
			addr <= sc_mem_out.address(addr_bits-3 downto 0) & "00";
		end if;
		if inc_addr='1' then
			addr(1 downto 0) <= std_logic_vector(unsigned(addr(1 downto 0)) + 1);
		end if;
		if sc_mem_out.wr='1' then
			ram_dout <= sc_mem_out.wr_data(31 downto 24);
			ram_dout_2nd <= sc_mem_out.wr_data(23 downto 16);
			ram_dout_3rd <= sc_mem_out.wr_data(15 downto 8);
			ram_dout_4th <= sc_mem_out.wr_data(7 downto 0);
		end if;
		if wr_2nd='1' then
			ram_dout <= ram_dout_2nd;
		end if;
		if wr_3rd='1' then
			ram_dout <= ram_dout_3rd;
		end if;
		if wr_4th='1' then
			ram_dout <= ram_dout_4th;
		end if;
		-- use an addtional input register to adhire the SimpCon spec
		-- to not change rd_data untill the full new word is available
		-- results in input MUX at RAM data input
		if rd_data_ena_1st='1' then
			ram_din_1st <= ram_din;
		end if;
		if rd_data_ena_2nd='1' then
			ram_din_2nd <= ram_din;
		end if;
		if rd_data_ena_3rd='1' then
			ram_din_3rd <= ram_din;
		end if;
		if rd_data_ena_4th='1' then
			-- move first bytes to higher bytes
			ram_din_reg(31 downto 24) <= ram_din_1st;
			ram_din_reg(23 downto 16) <= ram_din_2nd;
			ram_din_reg(15 downto 8) <= ram_din_3rd;
			-- read fourth byte
			ram_din_reg(7 downto 0) <= ram_din;
		end if;

	end if;
end process;

	sc_mem_in.rd_data <= ram_din_reg;

--
--	next state logic
--
process(state, sc_mem_out, wait_state)

begin

	next_state <= state;

	case state is

		when idl =>
			if sc_mem_out.rd='1' then
				if ram_ws=0 then
					-- then we omit state rd1!
					next_state <= rd2_1st;
				else
					next_state <= rd1_1st;
				end if;
			elsif sc_mem_out.wr='1' then
				next_state <= wr1_1st;
			end if;

		-- the WS state
		when rd1_1st =>
			if wait_state=2 then
				next_state <= rd2_1st;
			end if;

		when rd2_1st =>
			-- go to read low word
			if ram_ws=0 then
				-- then we omit state rd1!
				next_state <= rd2_2nd;
			else
				next_state <= rd1_2nd;
			end if;

		-- the WS state
		when rd1_2nd =>
			if wait_state=2 then
				next_state <= rd2_2nd;
			end if;

		when rd2_2nd =>
			-- go to read low word
			if ram_ws=0 then
				-- then we omit state rd1!
				next_state <= rd2_3rd;
			else
				next_state <= rd1_3rd;
			end if;

		-- the WS state
		when rd1_3rd =>
			if wait_state=2 then
				next_state <= rd2_3rd;
			end if;

		when rd2_3rd =>
			-- go to read low word
			if ram_ws=0 then
				-- then we omit state rd1!
				next_state <= rd2_4th;
			else
				next_state <= rd1_4th;
			end if;

		-- the WS state
		when rd1_4th =>
			if wait_state=2 then
				next_state <= rd2_4th;
			end if;

		-- last read state
		when rd2_4th =>
			next_state <= idl;
			-- This should do to give us a pipeline
			-- level of 2 for read
			if sc_mem_out.rd='1' then
				if ram_ws=0 then
					-- then we omit state rd1!
					next_state <= rd2_1st;
				else
					next_state <= rd1_1st;
				end if;
			elsif sc_mem_out.wr='1' then
				next_state <= wr1_1st;
			end if;
			
		when wr1_1st =>
			if wait_state=2 then
				next_state <= wr2_1st;
			end if;
			
		when wr2_1st =>
			next_state <= wr_idl_1st;
			
		when wr_idl_1st =>
			next_state <= wr1_2nd;

		when wr1_2nd =>
			if wait_state=2 then
				next_state <= wr2_2nd;
			end if;
			
		when wr2_2nd =>
			next_state <= wr_idl_2nd;
			
		when wr_idl_2nd =>
			next_state <= wr1_3rd;

		when wr1_3rd =>
			if wait_state=2 then
				next_state <= wr2_3rd;
			end if;
			
		when wr2_3rd =>
			next_state <= wr_idl_3rd;
			
		when wr_idl_3rd =>
			next_state <= wr1_4th;

		when wr1_4th =>
			if wait_state=2 then
				next_state <= wr2_4th;
			end if;
			
		when wr2_4th =>
			next_state <= idl;
			-- This should do to give us a pipeline
			-- level of 2 for read
			if sc_mem_out.rd='1' then
				if ram_ws=0 then
					-- then we omit state rd1!
					next_state <= rd2_1st;
				else
					next_state <= rd1_1st;
				end if;
			elsif sc_mem_out.wr='1' then
				next_state <= wr1_1st;
			end if;

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
		rd_data_ena_1st <= '0';
		rd_data_ena_2nd <= '0';
		rd_data_ena_3rd <= '0';
		rd_data_ena_4th <= '0';
		inc_addr <= '0';
		wr_2nd <= '0';
		wr_3rd <= '0';
		wr_4th <= '0';
		ram_nwe <= '1';
	elsif rising_edge(clk) then

		state <= next_state;
		dout_ena <= '0';
		ram_ncs <= '1';
		ram_noe <= '1';
		rd_data_ena_1st <= '0';
		rd_data_ena_2nd <= '0';
		rd_data_ena_3rd <= '0';
		rd_data_ena_4th <= '0';
		inc_addr <= '0';
		wr_2nd <= '0';
		wr_3rd <= '0';
		wr_4th <= '0';
		ram_nwe <= '1';

		case next_state is

			when idl =>

			-- the wait state
			when rd1_1st =>
				ram_ncs <= '0';
				ram_noe <= '0';

			-- high word last read state
			when rd2_1st =>
				ram_ncs <= '0';
				ram_noe <= '0';
				rd_data_ena_1st <= '1';
				inc_addr <= '1';

			-- the wait state
			when rd1_2nd =>
				ram_ncs <= '0';
				ram_noe <= '0';

			-- high word last read state
			when rd2_2nd =>
				ram_ncs <= '0';
				ram_noe <= '0';
				rd_data_ena_2nd <= '1';
				inc_addr <= '1';

			-- the wait state
			when rd1_3rd =>
				ram_ncs <= '0';
				ram_noe <= '0';

			-- high word last read state
			when rd2_3rd =>
				ram_ncs <= '0';
				ram_noe <= '0';
				rd_data_ena_3rd <= '1';
				inc_addr <= '1';

			-- the wait state
			when rd1_4th =>
				ram_ncs <= '0';
				ram_noe <= '0';

			-- low word last read state
			when rd2_4th =>
				ram_ncs <= '0';
				ram_noe <= '0';
				rd_data_ena_4th <= '1';
				
			when wr1_1st =>
				ram_nwe <= '0';
				dout_ena <= '1';
				ram_ncs <= '0';
				
			when wr2_1st =>
				ram_ncs <= '0';

			-- high word last write state
			when wr_idl_1st =>
				ram_ncs <= '1';
				dout_ena <= '1';
				inc_addr <= '1';
				wr_2nd <= '1';

			when wr1_2nd =>
				ram_nwe <= '0';
				dout_ena <= '1';
				ram_ncs <= '0';
				
			when wr2_2nd =>
				ram_ncs <= '0';

			-- high word last write state
			when wr_idl_2nd =>
				ram_ncs <= '1';
				dout_ena <= '1';
				inc_addr <= '1';
				wr_3rd <= '1';

			when wr1_3rd =>
				ram_nwe <= '0';
				dout_ena <= '1';
				ram_ncs <= '0';
				
			when wr2_3rd =>
				ram_ncs <= '0';

			-- high word last write state
			when wr_idl_3rd =>
				ram_ncs <= '1';
				dout_ena <= '1';
				inc_addr <= '1';
				wr_4th <= '1';
				
			when wr1_4th =>
				ram_nwe <= '0';
				dout_ena <= '1';
				ram_ncs <= '0';
				
			when wr2_4th =>
				ram_ncs <= '0';

		end case;
					
	end if;
end process;


--
-- wait_state processing
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
		end if;

		if sc_mem_out.rd='1' then
			wait_state <= to_unsigned(ram_ws+1, 4);
		end if;
		
		if sc_mem_out.wr='1' then
			wait_state <= to_unsigned(ram_ws_wr+1, 4);
		end if;

		
		if state=rd2_1st or state=rd2_2nd or state=rd2_3rd then
			wait_state <= to_unsigned(ram_ws+1, 4);
			cnt <= "11";
		end if;

		if state=rd2_4th then
			wait_state <= to_unsigned(ram_ws+1, 4);
			if ram_ws<3 then
				cnt <= to_unsigned(ram_ws+1, 2);
			else
				cnt <= "11";
			end if;
		end if;
			
		if state=wr_idl_1st or state=wr_idl_2nd then
			wait_state <= to_unsigned(ram_ws_wr+1, 4);
			cnt <= "11";
		end if;

		if state=wr_idl_3rd then
			wait_state <= to_unsigned(ram_ws_wr+1, 4);
			if ram_ws_wr<3 then
				cnt <= to_unsigned(ram_ws_wr+1, 2);
			else
				cnt <= "11";
			end if;
		end if;

		if state=rd1_4th or state=rd2_4th or state=wr1_4th or state=wr2_4th then
			-- take care for pipelined cache transfer
			-- there is no idl state and cnt should
			-- go back to "11"
			if sc_mem_out.rd='0' and sc_mem_out.wr='0' then
				-- if wait_state<4 then
				if wait_state(3 downto 2)="00" then
					cnt <= wait_state(1 downto 0)-1;
				end if;
			end if;
		end if;

	end if;
end process;

end rtl;
