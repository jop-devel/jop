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
--	wb_guitar.vhd
--
--	A simple ADC and DAC converter for the
--	guitar effect processing project.
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--	resources on Cyclone
--
--		xx LCs, max xx MHz
--
--
--	2005-06-30	first version
--
--	todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.wb_pack.all;

entity wb_guitar is

port (
	clk		: in std_logic;
	reset	: in std_logic;
	wb_in	: in wb_slave_in_type;
	wb_out	: out wb_slave_out_type;
	adc_in	: in std_logic;
	adc_out	: out std_logic;
	dac_l	: out std_logic;
	dac_r	: out std_logic
);
end wb_guitar;

architecture rtl of wb_guitar is

	signal ana_out		: std_logic_vector(31 downto 0);
	signal ana_in		: std_logic_vector(15 downto 0);

	signal rx_d			: std_logic;
	signal serdata		: std_logic;
	signal spike		: std_logic_vector(2 downto 0);	-- sync in, filter

--	perhaps we need some noise sometime...
	signal lsfr			: std_logic_vector(35 downto 1);
	signal noise		: std_logic;

	signal read_ack		: std_logic;
	signal write_ack	: std_logic;

--
--	
	constant FSAMP		: integer := 30000;
	constant CNT_MAX	: integer := clk_freq / FSAMP;
	signal cnt			: integer range 0 to CNT_MAX;
	signal sum			: unsigned(15 downto 0);
	signal sample_rdy	: std_logic;
	signal delta_l		: unsigned(15 downto 0);
	signal delta_r		: unsigned(15 downto 0);

	signal outl, outr	: std_logic;

signal div : integer range 0 to 1000;
--
--	Wishbone signals
--
	signal ena			: std_logic;
	signal ack			: std_logic;
	signal wr, rd		: std_logic;


begin

	ena <= wb_in.cyc_i and wb_in.stb_i;
	wr <= wb_in.we_i and ena;
	rd <= not wb_in.we_i and ena;

	-- single cycle read and write
	wb_out.ack_o <= ack and ena;
	ack <= ena;

--
--	read the analog value and the ready bit
--
process(rd, ack)
begin

	
	read_ack <= '0';
	if rd='1' and ack='1' then
		read_ack <= '1';
	end if;

end process;

	wb_out.dat_o(15 downto 0) <= ana_in;
	wb_out.dat_o(31) <= sample_rdy;
	wb_out.dat_o(30 downto 16) <= (others => '0');

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

		ana_out <= (others => '0');

	elsif rising_edge(clk) then

		write_ack <= '0';
		if wr='1' and ack='1' then
			write_ack <= '1';
			ana_out <= wb_in.dat_i;
		end if;
	end if;

end process;

--
--	This is the simple bit stream copy
--	for delta measurements
--
--	dac_l <= serdata;
--	dac_r <= serdata;

--
--	This is the output from the delta
--	modulator
---
	dac_l <= outl;
	dac_r <= outr;

	noise <= lsfr(1);

--
--	Here we go with the converter:
--
process(clk, reset)

begin

	if reset='1' then

		lsfr <= (others => '1');
-- div <= 0;

	elsif rising_edge(clk) then

-- if div=200 then
-- div <= 0;
		spike(0) <= adc_in;
		spike(2 downto 1) <= spike(1 downto 0);
--		adc_out <= not adc_in;
		adc_out <= not spike(2);
		serdata <= spike(2);
--		adc_out <= not rx_d;

		
		-- LSFR as RNG
		lsfr <= (others => '1');	-- unused bits
		lsfr(33 downto 1) <= lsfr(32 downto 1) & (lsfr(33) xor lsfr(20));
-- else
-- div <= div+1;
-- end if;

	end if;

end process;

--
--	filter input - not used
--
	with spike select
		rx_d <=	'0' when "000",
				'0' when "001",
				'0' when "010",
				'1' when "011",
				'0' when "100",
				'1' when "101",
				'1' when "110",
				'1' when "111",
				'X' when others;

--
--	Now get a PCM from the delta stream
--		First try is VERY primitve
--
process(clk, reset)

begin
	if reset='1' then

		cnt <= 0;
		sample_rdy <= '0';
		sum <= (others => '0');

	elsif rising_edge(clk) then

		if cnt=0 then
			cnt <= CNT_MAX;
			ana_in <= std_logic_vector(sum);
			sample_rdy <= '1';
			sum <= (others => '0');
		else
			cnt <= cnt-1;
			if serdata='1' then
				sum <= sum+1;
			end if;
		end if;

		-- reset ready flag after read
		if read_ack='1' then
			sample_rdy <= '0';
		end if;


	end if;
end process;

--
--	and here comes the primitive version of the
--	digital delta part - not really delta...
--
process(clk, reset)

begin
	if reset='1' then

		delta_l <= (others => '0');
		delta_r <= (others => '0');

	elsif rising_edge(clk) then

		if cnt=0 then		-- recycle the adc counter
			delta_l <= unsigned(ana_out(15 downto 0));
			delta_r <= unsigned(ana_out(31 downto 16));
		else
			outl <= '0';
			if delta_l /= 0 then
				delta_l <= delta_l-1;
				outl <= '1';
			end if;

			outr <= '0';
			if delta_r /= 0 then
				delta_r <= delta_r-1;
				outr <= '1';
			end if;

		end if;

	end if;
end process;




end rtl;
