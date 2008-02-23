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
--	ajbc.vhd
--
--	byte code memory for JOP3
--	Version for Altera (ACEX compatible)
--
--		wr_addr comes together with address (from A)
--			take start address from data
--		wren comes one clock befor data (from A) ... generated in read/addr stage
--			read 4 byte data and start write with high byte first and
--			address auto increment
--
--	on FPGAs (Cyclone) with different read and write port size
--	write could be a 32 bit single cycle thing, but ACEX does not
--	support it.
--			
--
--	Changes:
--		2003-08-14	load start address with jpc_wr and do autoincrement
--					load 32 bit data and do the 4 byte writes serial
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use ieee.numeric_std.all;

entity jbc is
generic (width : integer; addr_width: integer);
port (
	data		: in std_logic_vector(31 downto 0);

	rdaddress	: in std_logic_vector(addr_width-1 downto 0);
	wr_addr		: in std_logic;									-- load start address (=jpc)
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(width-1 downto 0)
);
end jbc ;

--
--	registered and delayed wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
architecture rtl of jbc is

	COMPONENT lpm_ram_dp
	GENERIC (LPM_WIDTH: POSITIVE;
			LPM_WIDTHAD: POSITIVE;
			LPM_NUMWORDS: NATURAL := 0;
			LPM_TYPE: STRING := "LPM_RAM_DP";
			LPM_INDATA: STRING := "REGISTERED";
			LPM_OUTDATA: STRING := "REGISTERED";
			LPM_RDADDRESS_CONTROL: STRING := "REGISTERED";
			LPM_WRADDRESS_CONTROL: STRING := "REGISTERED";
			LPM_FILE: STRING := "UNUSED";
			LPM_HINT: STRING := "UNUSED"
			);
	PORT (rdaddress, wraddress: IN STD_LOGIC_VECTOR(LPM_WIDTHAD-1 DOWNTO 0);
			rdclock, wrclock: IN STD_LOGIC := '1';
			rden, rdclken, wrclken: IN STD_LOGIC := '1';
			wren: IN STD_LOGIC; 
			data: IN STD_LOGIC_VECTOR(LPM_WIDTH-1 DOWNTO 0);
			q: OUT STD_LOGIC_VECTOR(LPM_WIDTH-1 DOWNTO 0));
	END COMPONENT;

	signal dreg			: std_logic_vector(31 downto 0);
	signal wraddr_dly	: std_logic_vector(addr_width-1 downto 0);
	signal wren_dly		: std_logic;
	signal ram_wr		: std_logic;
	signal ram_din		: std_logic_vector(7 downto 0);
	signal cnt			: std_logic_vector(2 downto 0);
	signal sel			: std_logic_vector(1 downto 0);

begin

--
--
--
process(clock) begin

	if rising_edge(clock) then
		wren_dly <= wren;				-- wren is one cycle befor data
		if wr_addr='1' then
			wraddr_dly <= data(addr_width-1 downto 0);
		end if;

		if (wren_dly='1') then			-- wren_dly starts 4 cycle write
			dreg <= data;				-- register write data
			cnt <= "100";				-- and start write
			sel <= "00";
		end if;

		if (cnt/="000") then
			wraddr_dly <= std_logic_vector(unsigned(wraddr_dly) + 1);
			cnt <= std_logic_vector(unsigned(cnt) - 1);
			sel <= std_logic_vector(unsigned(sel) + 1);
		end if;
	end if;
end process;

process(cnt) begin
	if (cnt/="000") then
		ram_wr <= '1';
	else
		ram_wr <= '0';
	end if;
end process;

process(sel, dreg) begin
	case sel is
		when "00" =>
			ram_din <= dreg(31 downto 24);
		when "01" =>
			ram_din <= dreg(23 downto 16);
		when "10" =>
			ram_din <= dreg(15 downto 8);
		when "11" =>
			ram_din <= dreg(7 downto 0);
		when others =>
			null;
	end case;
end process;

	cmp_ram: lpm_ram_dp 
			generic map (
				LPM_WIDTH => width, 
				LPM_WIDTHAD =>	addr_width, 
				LPM_NUMWORDS =>	2**addr_width,
				LPM_TYPE => "LPM_RAM_DP",
				LPM_OUTDATA => "UNREGISTERED", 
				LPM_INDATA => "REGISTERED", 
				LPM_RDADDRESS_CONTROL => "REGISTERED", 
				LPM_WRADDRESS_CONTROL => "REGISTERED", 
				LPM_HINT => "USE_EAB=ON")
			port map (
				rdaddress => rdaddress,
				wraddress => wraddr_dly,
				data => ram_din,
				wrclock => clock,
				rdclock => clock,
				wren => ram_wr,
				q => q
			); 

end rtl;
