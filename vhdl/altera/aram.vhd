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
--	aram.vhd
--
--	internal memory for JOP3
--	Version for Altera (Acex ok, Cyclone with warinings)
--
--	2006-08-10	signal for inverted clock
--
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use IEEE.std_logic_arith.all ;
use IEEE.std_logic_unsigned.all ;

entity ram is
generic (width : integer := 32; addr_width : integer := 8);
port (
        reset           : in std_logic;
	data		: in std_logic_vector(width-1 downto 0);
	wraddress	: in std_logic_vector(addr_width-1 downto 0);
	rdaddress	: in std_logic_vector(addr_width-1 downto 0);
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(width-1 downto 0)
);
end ram ;

--
--	registered and delayed wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
--	with normal clock on wrclock:
--		=> read during write on same address!!! (ok in ACEX)
--	for Cyclone use not clock for wrclock, but works also on ACEX
--
architecture rtl of ram is

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

	signal wraddr_dly	: std_logic_vector(addr_width-1 downto 0);
	signal wren_dly		: std_logic;

	signal nclk			: std_logic;

begin

	nclk <= not clock;

--
--	delay wr addr and ena because of registerd indata
--
process(clock) begin

	if rising_edge(clock) then
		wraddr_dly <= wraddress;
		wren_dly <= wren;
	end if;
end process;

	cmp_ram: lpm_ram_dp 
			generic map (
				LPM_WIDTH => width, 
				LPM_WIDTHAD =>	addr_width, 
				LPM_NUMWORDS =>	2**addr_width,
				LPM_TYPE => "LPM_RAM_DP",
				LPM_INDATA => "REGISTERED", 
				LPM_OUTDATA => "UNREGISTERED", 
				LPM_RDADDRESS_CONTROL => "REGISTERED", 
				LPM_WRADDRESS_CONTROL => "REGISTERED", 
				LPM_FILE => "../../asm/generated/ram.mif", 
				LPM_HINT => "USE_EAB=ON")
			port map (
				rdaddress => rdaddress,
				wraddress => wraddr_dly,
				data => data,
				rdclock => clock,
				wrclock => nclk,
				wren => wren_dly,
				q => q
			); 

end rtl;
