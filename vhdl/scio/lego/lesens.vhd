--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2005-2007, Martin Schoeberl (martin@jopdesign.com)
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
--	lesens.vhd
--
--	ADC converter for LEGO MindStorms
--
--
--	Original Author: Martin Schoeberl	martin@jopdesign.com
--	Author: Peter Hilber				peter.hilber@student.tuwien.ac.at
--
--	2005-12-22	adapted for SimpCon interface
--	2007-03-26	moved into separate file
--
--	todo:
--
--

--
--	lesens
--	
--	Sigma delta AD converter and power switch
--	for the LEGO sensor.
--	



library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity lesens is

generic (clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	dout	: out std_logic_vector(8 downto 0);

	sp		: out std_logic;
	sdi		: in std_logic;
	sdo		: out std_logic
);
end lesens ;

architecture rtl of lesens is

	signal clkint		: unsigned(8 downto 0);			-- 9 bit ADC
	signal val			: unsigned(8 downto 0);

	signal rx_d			: std_logic;
	signal serdata		: std_logic;

	signal spike		: std_logic_vector(2 downto 0);	-- sync in, filter

	signal pow_cnt		: unsigned(2 downto 0);

	signal clksd		: std_logic;
	signal prescale		: unsigned(7 downto 0);
	constant sd_clk_cnt	: integer := ((clk_freq+1000000)/2000000)-1;

begin


--
--	power switch for sensor
--
process(pow_cnt)

begin
	sp <= '1';
	if pow_cnt=0 or pow_cnt=1 then
		sp <= '0';
	end if;
end process;

	sdo <= serdata;

--
--	prescaler (2 MHz clock)
--
process(clk, reset)

begin
	if (reset='1') then
		prescale <= (others => '0');
		clksd <= '0';
	elsif rising_edge(clk) then
		clksd <= '0';
		prescale <= prescale + 1;
		if prescale = sd_clk_cnt then
			prescale <= (others => '0');
			clksd <= '1';
		end if;
	end if;

end process;

--
--	sigma delta converter
--
process(clk, reset)

begin
	if (reset='1') then
		spike <= "000";
		dout <= (others => '0');
		val <= (others => '0');
		clkint <= (others => '0');
		serdata <= '0';
		pow_cnt <= (others => '0');

	elsif rising_edge(clk) then

		if clksd='1' then

--
--	delay
--
			spike(0) <= sdi;
			spike(2 downto 1) <= spike(1 downto 0);
			serdata <= rx_d;		-- no inverter, using an invert. comperator

--
--	integrate
--

			if serdata='0' then		-- 'invert' value
				val <= val+1;
			end if;

			if clkint=0 then		-- 256 us
				if pow_cnt=1 then
					dout <= std_logic_vector(val);
				end if;
				val <= (others => '0');
				pow_cnt <= pow_cnt + 1;
			end if;

			clkint <= clkint+1;		-- free running counter

		end if;

	end if;

end process;


--
--	filter input
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
end rtl;
