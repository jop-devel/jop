-- 
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2007,2008, 2009, 2010 Christof Pitter
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


-- 150407: first working version with records
-- 170407: produce number of registers depending on the cpu_cnt
-- 110507: * arbiter that can be used with prefered number of masters
--				 * full functional arbiter with two masters
--				 * short modelsim test with 3 masters carried out
-- 190607: Problem found: Both CPU1 and CPU2 start to read cache line!!!
-- 030707: Several bugs are fixed now. CMP with 3 running masters functions!
-- 150108: Quasi Round Robin Arbiter -- added sync signal to arbiter
-- 160108: First tests running with new Round Robin Arbiter
-- 190208: Development of TDMA Arbiter
-- 130308: * Renaming of this_state to mode, follow_state to next_mode
--				 * counter dependencies moved from FSM to slot generation
--				 * changed set to 2 bits
--				 * changed serv to servR and servW
--				 * added signal pipelined
--				 * added rd_data register for each CPU
-- 140308: Working version
-- 070808: removed combinatorial loop (pipelined bug)
-- 210808: - reg_in_rd_data(i) also gets loaded when rdy_cnt = 3 using pipelined access
--		   - arb_in(i).rd_data gets mem_in.rd_data when rdy_cnt 3 using pipelined access
-- 240610: WP: Rewriting quite a few bits to fix bug with cross-core pipelining

-- TODO:  - Add atomic for Wolfgang
--		  - add period and time slots from software using RAM

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.jop_types.all;

-- read and write gaps

-- Constants for read_gap, write_gap, slot_length:
-- DE2 board:    6 cycle memory write, 4 cycle memory read
--               write_gap <= 5; read_gap <= 3;
--               minimal slot_length <= 6;
-- DE2-70 board: 3 cycle memory write, 3 cycle memory read
--               write_gap <= 2; read_gap <= 2;
--               minimal slot_length <= 3;
-- cyc12 board:  3 cycle memory write, 2 cycle memory read
--               write_gap <= 2; read_gap <= 1;
--               minimal slot_length <= 3;
-- Nexys2: 24, 24, 32 (number assigned by Flavius)

entity arbiter_tdma is
	generic(
		addr_bits : integer;
		cpu_cnt	: integer;
		write_gap : integer; -- remove defaults, each project shall
		read_gap  : integer; -- set the correct numbers in the top level
		slot_length : integer
		);		-- number of masters for the arbiter
	port (
		clk, reset	: in std_logic;			
		arb_out			: in arb_out_type(0 to cpu_cnt-1);
		arb_in			: out arb_in_type(0 to cpu_cnt-1);
		mem_out			: out sc_out_type;
		mem_in			: in sc_in_type
		);
end arbiter_tdma;


architecture rtl of arbiter_tdma is

-- stores the signals in a register of each master
	signal reg_out, next_reg_out : arb_out_type(0 to cpu_cnt-1);
	
-- register to CPU for rd_data
	type reg_in_type is array (0 to cpu_cnt-1) of std_logic_vector(31 downto 0);
	signal reg_in, next_reg_in : reg_in_type;
	
-- one fsm for each CPU
	type state_type is (idle, pending, waitR, waitW);
	type state_array is array (0 to cpu_cnt-1) of state_type;
	signal state, next_state : state_array;
-- stae machines for pipelining
	type waitstate_type is (idle, wait1, wait0);
	type waitstate_array is array (0 to cpu_cnt-1) of waitstate_type;
	signal waitstate, next_waitstate : waitstate_array;

	constant period : integer := cpu_cnt*slot_length;

-- counter
	subtype counter_type is integer range 0 to period;
	signal counter : counter_type;
	type time_type is array (0 to cpu_cnt-1) of counter_type;
	signal cpu_time : time_type; -- how much clock cycles each CPU
	type slot_type is array (0 to cpu_cnt-1) of std_logic;
	signal rdslot : slot_type; -- defines which CPU may read
	signal wrslot : slot_type; -- defines which CPU may write
	
begin

	-- generate counter
	gen_counter: process(clk, reset)
	begin
		if reset = '1' then
			counter <= 0;
		elsif rising_edge(clk) then
			counter <= counter + 1;
			if counter = period-1 then
				counter <= 0;
			end if;
		end if;
	end process;
	
	-- generate slot information
	gen_timing: for i in 0 to cpu_cnt-1 generate
		cpu_time(i) <= (i+1)*slot_length;
	end generate;	

	-- a time slot is assigned to each CPU 
	gen_slots: process(counter, cpu_time)
		variable lower_limit : integer;
	begin
		for i in 0 to cpu_cnt-1 loop
			rdslot(i) <= '0';
			wrslot(i) <= '0';
		end loop;

		lower_limit := 0;
		for i in 0 to cpu_cnt-1 loop
			if (counter >= lower_limit) and (counter < cpu_time(i)-write_gap) then
				wrslot(i) <= '1';
			end if;
			if (counter >= lower_limit) and (counter < cpu_time(i)-read_gap) then
				rdslot(i) <= '1';
			end if;
			lower_limit := cpu_time(i);
		end loop;
	end process;	

	sync: for i in 0 to cpu_cnt-1 generate
		process (clk, reset)
		begin  -- process sync
			if reset = '1' then  				-- asynchronous reset (active low)

				state(i) <= idle;
				waitstate(i) <= idle;

				reg_out(i).rd <= '0';
				reg_out(i).wr <= '0';
				reg_out(i).wr_data <= (others => '0'); 
				reg_out(i).address <= (others => '0');
				
				reg_in(i) <= (others => '0');
				
			elsif clk'event and clk = '1' then  -- rising clock edge
				
				state(i) <= next_state(i);
				waitstate(i) <= next_waitstate(i);
				reg_out(i) <= next_reg_out(i);
				reg_in(i) <= next_reg_in(i);
				
			end if;		
		end process;
	end generate;

	-- state machine for each master
	async: for i in 0 to cpu_cnt-1 generate
		process(rdslot, wrslot, state, waitstate, reg_in, reg_out, arb_out, mem_in)
		begin

			next_state(i) <= state(i);
			next_waitstate(i) <= waitstate(i);
			next_reg_out(i) <= reg_out(i);
			next_reg_in(i) <= reg_in(i);

			arb_in(i).rd_data <= reg_in(i);

			-- the main state machine
			case state(i) is
				when idle =>
					arb_in(i).rdy_cnt <= "00";
				when pending =>
					arb_in(i).rdy_cnt <= "11";
					if rdslot(i) = '1' and reg_out(i).rd = '1' then
						next_state(i) <= waitR;
						next_reg_out(i).rd <= '0';						
					end if;
					if wrslot(i) = '1' and reg_out(i).wr = '1' then
						next_state(i) <= waitW;
						next_reg_out(i).wr <= '0';
					end if;
				when waitR =>
					arb_in(i).rdy_cnt <= mem_in.rdy_cnt;
					if mem_in.rdy_cnt = 2 then
						next_waitstate(i) <= wait1;
					elsif mem_in.rdy_cnt = 1 then
						next_waitstate(i) <= wait0;
					elsif mem_in.rdy_cnt = 0 then
						arb_in(i).rd_data <= mem_in.rd_data;
						next_reg_in(i) <= mem_in.rd_data;						
					end if;					
					if mem_in.rdy_cnt <= 1 then
						next_state(i) <= idle;
					end if;
				when waitW =>
					arb_in(i).rdy_cnt <= mem_in.rdy_cnt;
					if mem_in.rdy_cnt <= 1 then
						next_state(i) <= idle;
					end if;
				when others =>
					null;					
			end case;

			-- CPUs better know when to issue reads or write
			if arb_out(i).rd = '1' then
				next_state(i) <= pending;
				next_reg_out(i) <= arb_out(i);
				if rdslot(i) = '1' then
					next_state(i) <= waitR;
					next_reg_out(i).rd <= '0';
				end if;
			end if;
			if arb_out(i).wr = '1' then
				next_state(i) <= pending;
				next_reg_out(i) <= arb_out(i);
				if wrslot(i) = '1' then
					next_state(i) <= waitW;
					next_reg_out(i).wr <= '0';
				end if;
			end if;

			-- "phase out" accesses for pipelining
			case waitstate(i) is
				when idle =>
				when wait1 =>
					next_waitstate(i) <= wait0;
					if mem_in.rdy_cnt = 0 then
						next_waitstate(i) <= idle;
						arb_in(i).rd_data <= mem_in.rd_data;
						next_reg_in(i) <= mem_in.rd_data;
					end if;
				when wait0 =>
					next_waitstate(i) <= idle;
					arb_in(i).rd_data <= mem_in.rd_data;
					next_reg_in(i) <= mem_in.rd_data;
				when others => null;
			end case;

		end process;
	end generate;

	mux: process (rdslot, wrslot, arb_out, reg_out)
	begin  -- process muxdemux

		mem_out.address <= (others => '0');
		mem_out.wr_data <= (others => '0'); 
		mem_out.rd <= '0';
		mem_out.wr <= '0';
		
		for i in 0 to cpu_cnt-1 loop
			-- pass on registered value
			if rdslot(i) = '1' and reg_out(i).rd = '1' then
				mem_out <= reg_out(i);
			end if;
			if wrslot(i) = '1' and reg_out(i).wr = '1' then
				mem_out <= reg_out(i);
			end if;
			-- direct pass through
			if rdslot(i) = '1' and arb_out(i).rd = '1' then
				mem_out <= arb_out(i);
			end if;
			if wrslot(i) = '1' and arb_out(i).wr = '1' then
				mem_out <= arb_out(i);
			end if;
		end loop;  -- i		
	end process mux;

end rtl;
