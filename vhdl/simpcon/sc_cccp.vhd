-- 
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2011 Wolfgang Puffitsch
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

---------------------------------------------------------------------------
-- Concurrent copy unit
---------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.jop_types.all;

entity cccp is
	
	generic (
		addr_bits : integer;
		cpu_cnt   : integer);

	port (
		clk, reset : in	 std_logic;
		
		arb_out    : in  arb_out_type;
		arb_in     : out arb_in_type;
		
		mem_out    : out sc_out_type;
		mem_in     : in  sc_in_type;
		
		config_out : in	 sc_out_type;
		config_in  : out sc_in_type);

end cccp;

architecture rtl of cccp is

	type state_type is (idle, rd1, rd2, wr1, wr2, wr3);
	signal state, next_state : state_type;
	
	signal src  : std_logic_vector(addr_bits-1 downto 0);
	signal dest : std_logic_vector(addr_bits-1 downto 0);
	signal pos  : std_logic_vector(addr_bits-1 downto 0);
	signal active : std_logic;

	signal vpos, next_vpos : std_logic_vector(addr_bits-1 downto 0);	
	signal buf, next_buf : std_logic_vector(31 downto 0);

	signal arb_front_out : arb_out_type(0 to cpu_cnt-1);
	signal arb_front_in  : arb_in_type(0 to cpu_cnt-1);

	signal arb_back_out : sc_out_type;
	signal arb_back_in  : sc_in_type;
	
begin

	arbiter: entity work.arbiter
		generic map(
			addr_bits => SC_ADDR_SIZE,
			cpu_cnt => cpu_cnt,
			write_gap => 2,
			read_gap => 2,
			slot_length => 3
		)
		port map (
			clk		=> clk,
			reset	=> reset,
			arb_out => arb_front_out,
			arb_in	=> arb_front_in,
			mem_out => arb_back_out,
			mem_in	=> arb_back_in);
		
	config: process (clk, reset)
	begin  -- process config
		if reset = '1' then  			-- asynchronous reset (active high)			

			state <= idle;
			vpos <= (others => '0');
			buf <= (others => '0');
			
			src <= (others => '0');
			dest <= (others => '0');
			pos <= (others => '0');			
			active <= '0';
			
		elsif clk'event and clk = '1' then  -- rising clock edge

			state <= next_state;
			vpos <= next_vpos;
			buf <= next_buf;
			
			if config_out.rd = '1' then
				config_in.rd_data(31 downto 0) <= (others => '0');
				case config_out.address(1 downto 0) is
					when "00" =>
						config_in.rd_data(addr_bits-1 downto 0) <= src;
					when "01" =>
						config_in.rd_data(addr_bits-1 downto 0) <= dest;
					when "10" =>
						config_in.rd_data(addr_bits-1 downto 0) <= pos;
					when "11" =>
						config_in.rd_data(0) <= active;						
					when others => null;
				end case;
			end if;
			if config_out.wr = '1' then
				case config_out.address(1 downto 0) is
					when "00" =>
						src <= config_out.wr_data(addr_bits-1 downto 0);
					when "01" =>
						dest <= config_out.wr_data(addr_bits-1 downto 0);
					when "10" =>
						pos <= config_out.wr_data(addr_bits-1 downto 0);
						if active = '1' then
							state <= rd1;  -- start copying							
						end if;
					when "11" =>
						active <= config_out.wr_data(0);
						vpos <= (others => '0');  -- automatically reset vpos
					when others => null;
				end case;
			end if;
		end if;
	end process config;

	frontmux: process (state,
					   config_out, arb_out, arb_front_in, arb_back_out,
					   src, dest, pos, vpos, buf)
	begin  -- process frontmux

		next_state <= state;
		next_vpos <= vpos;
		next_buf <= buf;

		arb_front_out(0).wr_data <= (others => '0');
		arb_front_out(0).address <= (others => '0');
		arb_front_out(0).rd <= '0';
		arb_front_out(0).wr <= '0';
		
		config_in.rdy_cnt <= "00";

		arb_in(0).rd_data <= (others => '0');
		arb_in(0).rdy_cnt <= "00";
		
		case state is
			when idle =>
				arb_front_out(0) <= arb_out(0);
				arb_in(0) <= arb_front_in(0);
			when rd1 =>
				arb_front_out(0).address <= std_logic_vector(unsigned(src)+unsigned(pos));
				arb_front_out(0).rd <= '1';
				config_in.rdy_cnt <= "11";
				next_state <= rd2;
			when rd2 =>
				if arb_front_in(0).rdy_cnt = 0 then
					next_buf <= arb_front_in(0).rd_data;
					next_state <= wr1;
				end if;
				config_in.rdy_cnt <= "11";
			when wr1 =>
				arb_front_out(0).address <= std_logic_vector(unsigned(dest)+unsigned(pos));
				arb_front_out(0).wr_data <= buf;
				arb_front_out(0).wr <= '1';
				config_in.rdy_cnt <= "11";
				next_state <= wr2;
			when wr2 =>
				config_in.rdy_cnt <= arb_front_in(0).rdy_cnt;
				if arb_front_in(0).rdy_cnt <= 2 then					
					next_state <= wr3;
					next_vpos <= std_logic_vector(unsigned(pos)+1);
				end if;
			when wr3 =>
				config_in.rdy_cnt <= arb_front_in(0).rdy_cnt;
				arb_front_out(0) <= arb_out(0);
				arb_in(0) <= arb_front_in(0);
				next_state <= idle;
			when others => null;
		end case;

		-- just pass on signals from other cores
		for i in 1 to cpu_cnt-1 loop
			arb_front_out(i) <= arb_out(i);
			arb_in(i) <= arb_front_in(i);
		end loop;  -- i

		-- catch writes to the location we are about to copy
		if arb_back_out.wr = '1'
			and arb_back_out.address = std_logic_vector(unsigned(src)+unsigned(pos)) then
			next_buf <= arb_back_out.wr_data;
		end if;
		
	end process frontmux;

	redirect: process(arb_back_out, mem_in,
					  src, dest, active, vpos)
		variable offset : unsigned(addr_bits downto 0);
	begin  -- process backmux
		arb_back_in <= mem_in;
		mem_out <= arb_back_out;

		offset := unsigned('0' & arb_back_out.address) - unsigned('0' & src);
		if active='1' and offset < unsigned(vpos) then
			mem_out.address <= std_logic_vector(unsigned(dest) + offset(addr_bits-1 downto 0));
		end if;
	end process redirect;

end rtl;
