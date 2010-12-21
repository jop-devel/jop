--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2010, Martin Schoeberl (martin@jopdesign.com)
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


--  sc_cmpuart.vhd
--
--  CMP interface to UART
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;

entity sc_cmpuart is
	
	generic (
		addr_bits : integer;
		cpu_cnt   : integer);

	port (
		clk	  : in std_logic;
		reset : in std_logic;

		-- SimpCon interfaces
		arb_out : in arb_out_type(0 to CPU_CNT-1);
		arb_in	: out arb_in_type(0 to CPU_CNT-1);
		
		uart_out : out sc_out_type;
		uart_in : in sc_in_type;

		-- Direct signals from UART
		tf_ready : in std_logic;
		rf_avail : in std_logic;
		pa_error : in std_logic
		);

end sc_cmpuart;

architecture rtl of sc_cmpuart is

	type wrbuf_elem_type is record
		address : std_logic_vector(addr_bits-1 downto 0);			
		data  : std_logic_vector(31 downto 0);		
		valid : std_logic;
	end record;
	type wrbuf_type is array (0 to cpu_cnt-1) of wrbuf_elem_type;
	
	signal wrbuf : wrbuf_type;

	subtype counter_type is integer range 0 to cpu_cnt-1;
	signal wrcounter : counter_type;
	
	signal lastrd : counter_type;
	signal lastrd_passed : std_logic;

begin  -- rtl
	
	sync: process (clk, reset)
	begin  -- process sync
		if reset = '1' then  			-- asynchronous reset (active high)
			wrbuf <= (others => ((others => '0'), (others => '0'), '0'));
			wrcounter <= 0;
			lastrd <= 0;
			lastrd_passed <= '0';
			
			uart_out.address <= (others => '0');
			uart_out.rd <= '0';
			uart_out.wr <= '0';
			uart_out.wr_data <= (others => '0');

			for i in 0 to cpu_cnt-1 loop
				arb_in(i).rdy_cnt <= "00";
			end loop;  -- i

		elsif clk'event and clk = '1' then  -- rising clock edge

			-- pass buffered writes on to UART
			uart_out.wr <= '0';
			if wrbuf(wrcounter).valid = '1' then
				if wrbuf(wrcounter).address(0) = '0' or tf_ready = '1' then
					uart_out.address(addr_bits-1 downto 0) <= wrbuf(wrcounter).address;
					uart_out.wr <= '1';
					uart_out.wr_data <= wrbuf(wrcounter).data;
					wrbuf(wrcounter).valid <= '0';
				end if;
			else
				-- nothing to do, check next core in next cycle
				if wrcounter = cpu_cnt-1 then
					wrcounter <= 0;
				else
					wrcounter <= wrcounter+1;					
				end if;
			end if;

			-- default rdy_cnt
			arb_in(lastrd).rdy_cnt <= "00";

			-- return data of last read
			if lastrd_passed = '1' then
				arb_in(lastrd).rd_data <= uart_in.rd_data;
			end if;

			uart_out.rd <= '0';
			for i in 0 to cpu_cnt-1 loop
				-- latch data writes in wrbuf
				if arb_out(i).wr = '1' then
					wrbuf(i).address <= arb_out(i).address(addr_bits-1 downto 0);
					wrbuf(i).data <= arb_out(i).wr_data;
					wrbuf(i).valid <= '1';
				end if;

				-- handle reads and pass them on to UART if necessary
				if arb_out(i).rd = '1' then
					if arb_out(i).address(0) = '0' then
						-- serve status reads locally
						arb_in(i).rd_data <= (others => '0');
						arb_in(i).rd_data(2) <= pa_error;
						arb_in(i).rd_data(1) <= rf_avail;
						arb_in(i).rd_data(0) <= not wrbuf(i).valid;
						lastrd_passed <= '0';
					else
						-- pass data reads on to UART
						uart_out.rd <= '1';
						uart_out.address <= arb_out(i).address;
						arb_in(i).rdy_cnt <= "01";
						lastrd <= i;
						lastrd_passed <= '1';
					end if;
				end if;
			end loop;  -- i
			
		end if;
	end process sync;
	
end rtl;
