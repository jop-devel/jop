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
--	arom.vhd
--
--	Microinstruction memory for JOP3
--	Version for Altera
--
--	Generated rom.vhd can be used instead
--	if the synthesis tool can instantiate a ROM.
--
--	changelog:
--		2004-04-06	positiv edge registered address and unregisterd data out
--
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use IEEE.std_logic_arith.all ;
use IEEE.std_logic_unsigned.all ;

entity rom is
generic (width : integer; addr_width : integer);
port (
	clk		: in std_logic;

	address	: in std_logic_vector(addr_width-1 downto 0);

	q		: out std_logic_vector(width-1 downto 0)
);
end rom;

architecture rtl of rom is

	COMPONENT lpm_rom
	GENERIC (LPM_WIDTH: POSITIVE;
		LPM_TYPE: STRING := "LPM_ROM";
		LPM_WIDTHAD: POSITIVE;
		LPM_NUMWORDS: NATURAL := 0;
		LPM_FILE: STRING;
		LPM_ADDRESS_CONTROL: STRING := "REGISTERED";
		LPM_OUTDATA: STRING := "REGISTERED";
		LPM_HINT: STRING := "UNUSED");
	PORT (address: IN STD_LOGIC_VECTOR(LPM_WIDTHAD-1 DOWNTO 0);
		inclock: IN STD_LOGIC := '0';
		outclock: IN STD_LOGIC := '0';
		memenab: IN STD_LOGIC := '1';
		q: OUT STD_LOGIC_VECTOR(LPM_WIDTH-1 DOWNTO 0));
	END COMPONENT;

begin

	cmp_rom: lpm_rom
			generic map (
				LPM_WIDTH => width, 
				LPM_TYPE => "LPM_ROM",
				LPM_WIDTHAD =>	addr_width, 
				LPM_NUMWORDS =>	2**addr_width,
				LPM_FILE => "../../asm/generated/rom.mif", 
				LPM_ADDRESS_CONTROL => "REGISTERED",
				LPM_OUTDATA => "UNREGISTERED",
				LPM_HINT => "USE_EAB=ON")
			port map (
				address => address,
				inclock => clk,
				q => q
			); 

end rtl;
