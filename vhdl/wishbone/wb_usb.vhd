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
--	wb_usb.vhd
--
--	Interface to FTDI FT2232C parallel port B
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--	resources on Cyclone
--
--		xx LCs, max xx MHz
--
--
--	2005-08-23	first version
--
--	todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.wb_pack.all;

entity wb_usb is

port (
	clk		: in std_logic;
	reset	: in std_logic;
	wb_in	: in wb_slave_in_type;
	wb_out	: out wb_slave_out_type;
	data	: inout std_logic_vector(7 downto 0);
	nrxf	: in std_logic;
	ntxe	: in std_logic;
	nrd		: out std_logic;
	wr		: out std_logic;
	nsi		: out std_logic
);
end wb_usb;

architecture rtl of wb_usb is

	signal usb_out		: std_logic_vector(7 downto 0);
	signal usb_in		: std_logic_vector(7 downto 0);

	signal read_ack		: std_logic;
	signal write_ack	: std_logic;

--
--	
	constant WS		: integer := (clk_freq/20000000)+1;
	signal cnt			: integer range 0 to WS;

--
--	FIFO signals
--
	signal tf_dout		: std_logic_vector(7 downto 0); -- fifo out
	signal tf_rd		: std_logic;
	signal tf_empty		: std_logic;
	signal tf_full		: std_logic;

	signal rf_din		: std_logic_vector(7 downto 0); -- fifo input
	signal rf_wr		: std_logic;
	signal rf_empty		: std_logic;
	signal rf_full		: std_logic;


--
--	USB interface signals
--
	type state_type		is (idle, inact, rx1, rx2, tx1, tx2);
	signal state 		: state_type;

	signal usb_dout		: std_logic_vector(7 downto 0);
	signal usb_din		: std_logic_vector(7 downto 0);

	signal nrxf_buf		: std_logic_vector(1 downto 0);
	signal ntxe_buf		: std_logic_vector(1 downto 0);
	signal rdr, wrr		: std_logic_vector(7 downto 0);
	signal data_oe		: std_logic;

--
--	Wishbone signals
--
	signal ena			: std_logic;
	signal ack			: std_logic;
	signal wb_wr, wb_rd	: std_logic;


begin

	ena <= wb_in.cyc_i and wb_in.stb_i;
	wb_wr <= wb_in.we_i and ena;
	wb_rd <= not wb_in.we_i and ena;

	-- single cycle read and write
	wb_out.ack_o <= ack and ena;
	ack <= ena;

	-- we don't use the send immediate
	nsi <= '1';

--
--	read the usb value and the ready bits
--
process(wb_rd, ack, wb_in.adr_i(0))
begin

	
	read_ack <= '0';
	if wb_rd='1' and ack='1' and wb_in.adr_i(0)='1' then
		read_ack <= '1';
	end if;

end process;

--
--	The MUX is all we need for a read
--
process(wb_in.adr_i(0), rf_empty, tf_full, usb_dout)
begin

	if wb_in.adr_i(0)='0' then
		wb_out.dat_o(1 downto 0) <= not rf_empty & not tf_full;
		wb_out.dat_o(7 downto 2) <= (others => '0');
	else
		wb_out.dat_o(7 downto 0) <= usb_dout;
	end if;
	wb_out.dat_o(31 downto 8) <= (others => '0');
end process;



--
--	WB write is also simple
--
process(clk, reset)

begin

	-- WB requests synchronous reset for the interface.
	-- However, asynchronous reset is more common and in
	-- this case we reset the internal data and not the
	-- WB interface.
	if (reset='1') then

		usb_din <= (others => '0');

	elsif rising_edge(clk) then

		write_ack <= '0';
		if wb_wr='1' and ack='1' then
			write_ack <= '1';
			usb_din <= wb_in.dat_i(7 downto 0);
		end if;
	end if;

end process;

--
--	receive fifo
--
	rxfifo: entity work.fifo generic map (
				width => 8,
				depth => 4,
				thres => 2	-- we don't care about the half signal
			) port map (
				clk => clk,
				reset => reset,
				din => rf_din,
				dout => usb_dout,
				rd => read_ack,
				wr => rf_wr,
				empty => rf_empty,
				full => rf_full,
				half => open
			);

--
--	transmit fifo
--
	txfifo: entity work.fifo generic map (
				width => 8,
				depth => 4,
				thres => 2	-- we don't care about the half signal
			) port map (
				clk => clk,
				reset => reset,
				din => usb_din,
				dout => tf_dout,
				rd => tf_rd,
				wr => write_ack,
				empty => tf_empty,
				full => tf_full,
				half => open
			);


--
--	state machine for the usb bus
--
process(clk, reset)

begin

	if (reset='1') then
		state <= idle;
		nrxf_buf <= "11";
		ntxe_buf <= "11";
		cnt <= WS;

		rdr <= (others => '0');
		wrr <= (others => '0');

		tf_rd <= '0';
		rf_wr <= '0';

		nrd <= '1';
		wr <= '0';

	elsif rising_edge(clk) then

		-- input register
		nrxf_buf(0) <= nrxf;
		nrxf_buf(1) <= nrxf_buf(0);
		ntxe_buf(0) <= ntxe;
		ntxe_buf(1) <= ntxe_buf(0);

		case state is

			when idle =>
				cnt <= WS;
				tf_rd <= '0';
				rf_wr <= '0';
				nrd <= '1';
				wr <= '0';
				data_oe <= '0';
				if rf_full='0' and nrxf_buf(1)='0' then
					nrd <= '0';
					state <= rx1;
				elsif tf_empty='0' and ntxe_buf(1)='0' then
					wr <= '1';
					wrr <= tf_dout;
					tf_rd <= '1';
					state <= tx1;
				end if;

			when inact =>
				tf_rd <= '0';
				rf_wr <= '0';
				nrd <= '1';
				wr <= '0';
				data_oe <= '0';
				cnt <= cnt-1;
				if cnt=0 then
					state <= idle;
				end if;


			when rx1 =>
				cnt <= cnt-1;
				if cnt=0 then
					state <= rx2;
					rdr <= data;
				end if;

			when rx2 =>
				nrd <= '1';
				rf_wr <= '1';
				cnt <= WS;
				state <= inact;
				
			when tx1 =>
				tf_rd <= '0';
				data_oe <= '1';
				cnt <= cnt-1;
				if cnt=0 then
					state <= tx2;
					wr <= '0';
				end if;

			when tx2 =>
				data_oe <= '0';
				cnt <= WS;
				state <= inact;

		end case;
	end if;

end process;

	data <= wrr when data_oe='1' else (others => 'Z');
	rf_din <= data;

end rtl;
